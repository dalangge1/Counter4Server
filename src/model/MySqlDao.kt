package model

import beannew.DynamicItem
import beannew.User
import java.lang.Exception
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement


object MySqlDao {
    private const val JDBC_DRIVER = "com.mysql.cj.jdbc.Driver"
    private const val DB_URL = "jdbc:mysql://localhost?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC"
    private const val emptyUsrPic = "https://huaban.com/img/error_page/img_404.png"

    private var statement: Statement

    private val loginMap = HashMap<Int, String>()

    init {

        Class.forName(JDBC_DRIVER);
        val conn = DriverManager.getConnection(DB_URL, "root", "7424855920");
        conn.createStatement().execute("CREATE DATABASE IF NOT EXISTS counter4_database DEFAULT CHARSET utf8 COLLATE utf8_general_ci;")
        statement = conn.createStatement()
        statement.execute("use counter4_database;")

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

        try {
            val userId = getUserId(token)
            val resultSet = statement.executeQuery("SELECT * FROM user_list WHERE user_id='$userId';")
            while (resultSet.next()) {
                return User(
                        userId = resultSet.getString(1),
                        nickname = resultSet.getString(2),
                        password = resultSet.getString(3),
                        registerDate = resultSet.getTimestamp(4).time,
                        sex = resultSet.getString(5),
                        text = resultSet.getString(6),
                        avatarUrl = resultSet.getString(7)
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        return null
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

        try {
            statement.execute("INSERT INTO user_list " +
                    "(user_id, nickname, password, register_date, sex, text, avatar_url) " +
                    "VALUES " +
                    "('$userId', '$userId', '$password', NOW(), '保密', '', '$emptyUsrPic' );")
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

        return statement.updateCount != 0
    }

    fun releaseDynamic(token: Int, text: String, topic: String, vararg picUrl: String): Int {
        val user = getUser(token)?: return -3

        var id = -1
        // 发布动态
        try {
            statement.execute("INSERT INTO dynamic_list " +
                    "(user_id, text, topic, submit_time) " +
                    "VALUES " +
                    "('${user.userId}', '$text', '$topic', NOW());", Statement.RETURN_GENERATED_KEYS)

        } catch (e: Exception) {
            e.printStackTrace()
            return -1
        }



        // 找到刚刚插入的动态的id
        val rs = statement.generatedKeys
        if (rs.next()) {
            id = rs.getInt(1)
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


    fun getAllDynamic(pos: Int, size: Int, topic: String?): List<DynamicItem>? {

        val list = ArrayList<DynamicItem>()
        try {
            val resultSet: ResultSet = if (topic == null) {
                statement.executeQuery("SELECT * FROM `dynamic_list` order by dynamic_id desc limit $pos,$size;")
            } else {
                statement.executeQuery("SELECT * FROM `dynamic_list` where topic='$topic' order by dynamic_id desc limit $pos,$size;")
            }

            while (resultSet.next()) {

                list.add(
                        DynamicItem(
                                dynamicId = resultSet.getInt(1),
                                userId = resultSet.getString(2),
                                submitTime = resultSet.getTimestamp(3).time,
                                text = resultSet.getString(4)
                        )
                )

            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }


        return list
    }


}