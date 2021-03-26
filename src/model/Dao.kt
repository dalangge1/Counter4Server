package model

import beannew.CommentItem
import beannew.DynamicItem
import beannew.User
import utils.SqlConverter
import java.lang.Exception
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement


object Dao {
    private const val JDBC_DRIVER = "com.mysql.cj.jdbc.Driver"
    const val DB_URL = "jdbc:mysql://localhost?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC"
    private const val emptyUsrPic = "https://huaban.com/img/error_page/img_404.png"

    private val statement: Statement get() = DriverManager.getConnection(DB_URL, "root", "7424855920").createStatement().apply {
        execute("CREATE DATABASE IF NOT EXISTS counter4_database DEFAULT CHARSET utf8 COLLATE utf8_general_ci;")

        execute("use counter4_database;")
    }
//    private val conn: Connection

    private val loginMap = HashMap<Int, String>()

    init {

        Class.forName(JDBC_DRIVER)

        // 执行数据库的初始化
        initSql(statement)


    }

    private fun logoutAll() {
        loginMap.clear()
    }

    private fun logout(userId: String) {
        val iter = loginMap.iterator()
        while (iter.hasNext()) {
            if (iter.next().value == userId) {
                iter.remove()
                break
            }
        }
    }

    private fun getUserId(token: Int): String? {
        return loginMap[token]
    }

    private fun getUser(token: Int): User? {

        return getUserId(token)?.let { getUser(it) }
    }

    fun getUser(userId: String): User? {

        try {
            val list = SqlConverter.getListFromSql<User>("SELECT * FROM user_list WHERE user_id='$userId';")
            list[0].let {
                return it
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun getPicList(dynamicId: Int): List<String> {
        val list = ArrayList<String>()
        try {
            val resultSet = statement.executeQuery("SELECT * FROM pic_list WHERE dynamic_id='$dynamicId';")
            while (resultSet.next()) {
                list.add(resultSet.getString(2))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return list
        }

        return list
    }


    /**
     * 登录，传入用户名和密码，得到token Int
     * TODO: 每天5时刷新token
     */
    fun login(userId: String, password: String): Int {

        try {
            val resultSet = statement.executeQuery("SELECT * FROM user_list WHERE user_id='$userId' AND password='$password';")
            if (resultSet.next()) { // 有记录，则登录成功
                val token = (10000000..99999999).random()
                logout(userId)
                loginMap[token] = userId
                return token
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return -1
        }
        return -1
    }

    /**
     * 注册，传入用户名和密码
     */
    fun register(userId: String, password: String): Boolean {
        var count: Int
        try {
            statement.apply {
                execute("INSERT INTO user_list " +
                        "(user_id, nickname, password, register_date, sex, text, avatar_url) " +
                        "VALUES " +
                        "('$userId', '$userId', '$password', NOW(), '保密', '', '$emptyUsrPic' );")
                count = updateCount
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

        return count != 0
    }

    /**
     * 发布动态
     * @param token 登录获得的token
     * @param text 发的文字
     * @param topic 发的圈子
     * @param picUrl 图片列表
     */

    fun releaseDynamic(token: Int, text: String, topic: String, vararg picUrl: String): Int {
        val user = getUser(token) ?: return -3

        var id = -1
        var rs: ResultSet?
        // 发布动态
        try {
            statement.apply {
                execute("INSERT INTO `dynamic_list` " +
                        "(user_id, text, topic, submit_time) " +
                        "VALUES " +
                        "('${user.userId}', '$text', '$topic', NOW());", Statement.RETURN_GENERATED_KEYS)
                rs = generatedKeys
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return -1
        }


        rs ?: return -2

        if (rs!!.next()) {
            id = rs!!.getInt(1)
        }

        if (id == -1) {
            return -2
        }
        picUrl.forEach {
            statement.execute("INSERT INTO pic_list " +
                    "(dynamic_id, pic_url) " +
                    "VALUES " +
                    "('${id}', '${it}');")
        }
        // 插入图片
        try {

        } catch (e: Exception) {
            e.printStackTrace()
            return -2
        }

        return id
    }

//    inline fun <reified T> getBeanList(sql: String): List<T> {
//        val clazz: Class<T> = T::class.java
//    }
    /**
     * 点赞/取消点赞
     */
    fun reversePraise(token: Int, id: Int, which: Int): Boolean {
        try {
            val count = statement.executeUpdate("DELETE FROM `praise_list` where which='$which' and id='$id';")
            if (count != 0) {
                return true
            } else {
                throw Exception()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 如果没有点赞则添加点赞记录
            try {
                val userId = getUser(token)?.userId
                if (userId.isNullOrBlank()) {
                    return false
                }
                val count2 = statement.executeUpdate("INSERT INTO `praise_list` (id, user_id, which) VALUES ('$id', '$userId', '$which');")
                return count2 != 0
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        }

    }

    /**
     * 获得点赞的user列表
     */
    private fun getPraise(id: Int, which: Int): List<User> {
        val list = SqlConverter.getListFromSql<User>("SELECT * FROM `praise_list` where id='$id' and which='$which';")
        val userList = ArrayList<User>()
        list.forEach {
            getUser(it.userId)?.let { it1 -> userList.add(it1) }
        }
        return userList
    }

    /**
     * @param which 获取一级还是二级评论
     * @param replyId 被回复的id，会根据id寻找
     */
    private fun getCommentList(replyId: Int, which: Int): List<CommentItem> {
        val list: List<CommentItem>
        try {
            list = SqlConverter.getListFromSql("SELECT * FROM `comment_list` where reply_id='$replyId' and which='$which';")

            list.forEach {
                val user = getUser(it.userId)
                val praiseListT = getPraise(it.id, which)
                val replyListT = if (which == 1) { // 获得当前评论的回复列表
                    getCommentList(it.id, 2)
                } else {
                    listOf()
                }
                it.apply {
                    nickname = user?.nickname ?: ""
                    avatarUrl = user?.avatarUrl ?: ""
                    replyList = replyListT
                    praise = praiseListT
                }

            }

        } catch (e: Exception) {
            e.printStackTrace()
            return listOf()
        }


        return list
    }

    /**
     * @param pos 从 最新发布的动态 中起始的位置
     * @param size 获得多少条记录
     * @param topic 圈子，null为获取全部
     */
    fun getAllDynamic(pos: Int, size: Int, topic: String?): List<DynamicItem>? {

        val list: List<DynamicItem>
        try {
            list = if (topic.isNullOrBlank()) {
                SqlConverter.getListFromSql("SELECT * FROM `dynamic_list` order by dynamic_id desc limit $pos,$size;")

            } else {
                SqlConverter.getListFromSql("SELECT * FROM `dynamic_list` where topic='$topic' order by dynamic_id desc limit $pos,$size;")
            }

            list.forEach {
                val user = getUser(it.userId)
                val picList = getPicList(it.dynamicId)
                val praiseListT = getPraise(it.dynamicId, 0)
                val commentListT = getCommentList(it.dynamicId, 1)
                it.apply {
                    nickname = user?.nickname ?: ""
                    avatarUrl = user?.avatarUrl ?: ""
                    picUrl = picList
                    commentList = commentListT
                    praise = praiseListT
                }

            }


        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }


        return list
    }

    /**
     * @param dynamicId 动态的id
     */
    fun deleteDynamic(dynamicId: Int): Boolean {
        return try {
            val count = statement.executeUpdate("DELETE FROM `dynamic_list` where dynamic_id='$dynamicId';")
//            // 删除附属图片
//            statement.executeUpdate("DELETE FROM `pic_list` where dynamic_id=$dynamicId;")
//            // 删除一级评论
//            statement.executeUpdate("DELETE FROM `comment_list` where which='1' and reply_id not in (select dynamic_id as id from dynamic_list);")
//            // 删除二级评论
//            statement.executeUpdate("DELETE FROM `comment_list` where which='2' and reply_id not in (select id from comment_list);")
//            // 删除点赞
//            statement.executeUpdate("DELETE FROM `praise_list` where which='0' and id=$dynamicId")
//            // 删除一级评论的点赞
//            statement.executeUpdate("DELETE FROM `praise_list` where which='1' and id not in (select dynamic_id as id from dynamic_list);")
//            // 删除二级评论的点赞
//            statement.executeUpdate("DELETE FROM `praise_list` where which='2' and id not in (select id from comment_list where which='1');")

            count != 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 删除一二级回复
     * @param which 1为删除一级评论 2为删除二级评论
     */
    fun deleteComment(id: Int, which: Int): Boolean {
        return try {
            val count = statement.executeUpdate("DELETE FROM `comment_list` where id='$id' and which='$which';")
            count != 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * @param token
     * @param reply_id 要回复的动态，或者一级评论的id，或者二级评论的id
     * @param which 0回复动态 1回复一级评论 2回复二级评论
     * @param text 要说的话
     */
    fun reply(token: Int, reply_id: Int, which: Int, text: String): Boolean {
        val userId = getUserId(token) ?: return false
        try {
            when (which) {
                0 -> { // 回复动态
                    val count = statement.executeUpdate("INSERT INTO `comment_list` " +
                            "(reply_id, user_id, submit_time, text, which, reply_user_id, reply_inner_id)" +
                            "value" +
                            "('$reply_id', '$userId', NOW(), '$text', '1', '', '0');")
                    return count != 0
                }
                1 -> { // 回复一级评论
                    val count = statement.executeUpdate("INSERT INTO `comment_list` " +
                            "(reply_id, user_id, submit_time, text, which, reply_user_id, reply_inner_id)" +
                            "value" +
                            "('$reply_id', '$userId', NOW(), '$text', '2', '', '0');")
                    return count != 0
                }
                2 -> { // 回复二级评论
                    val count = statement.executeUpdate("INSERT INTO `comment_list` " +
                            "(reply_id, user_id, submit_time, text, which, reply_user_id, reply_inner_id)" +
                            "value" +
                            "((SELECT a.reply_id from (SELECT reply_id from comment_list where which='2' and id='$reply_id')a), '$userId', NOW(), '$text', '2', (SELECT a.user_id from (SELECT user_id from comment_list where which='2' and id='$reply_id')a), '$reply_id');")
                    return count != 0
                }
                else -> {
                    return false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }


}