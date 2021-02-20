package page

import bean.Account
import bean.AccountSmall
import bean.DataItem
import bean.TalkItem
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import java.sql.DriverManager
import java.text.SimpleDateFormat
import java.util.*
import javax.servlet.*
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServletRequest

@WebServlet(name = "page.Counter4ServletKtSql")
class Counter4ServletKtSql : Servlet {


    companion object {
        private const val emptyUsrPic = "https://huaban.com/img/error_page/img_404.png"
        private var lastInfo = "null"
        private const val announcement = "[7月17日更新]祝红岩的学长学姐们，学业进步，考研成功，面试顺利！！！"

        private fun timestampToDateStr(timeStamp: Long): String {
            val timeString: String
            val sdf = SimpleDateFormat("yyyy年MM月dd日ahh点")
            timeString = sdf.format(Date(timeStamp)) //单位秒
            return timeString
        }
    }


    private var servletConfig: ServletConfig? = null
    private var random: Random? = null


    private var accounts: ArrayList<Account>? = null
    private var talkList: ArrayList<TalkItem>? = null
    private fun login(accountNum: String, password: String): Int { //return token
        for (i in accounts!!.indices) {
            if (accounts!![i].accountNum == accountNum) {
                if (accounts!![i].password == password) {
                    var flag: Boolean
                    var tokenTmp: Int
                    do {
                        tokenTmp = random!!.nextInt(8999) + 1000
                        flag = false
                        for (account in accounts!!) {
                            if (account.token == tokenTmp) {
                                flag = true
                            }
                        }
                    } while (flag)
                    accounts!![i].token = tokenTmp
                    return tokenTmp
                }
            }
        }
        return -1
    }

    private fun register(accountNum: String, password: String): Int {
        for (account in accounts!!) {
            if (account.accountNum == accountNum) {
                return -1
            }
        }
        accounts!!.add(Account(accountNum, accountNum, password, "保密", "这个人还没有介绍自己哦~", -1, emptyUsrPic))
        return 1
    }

    private fun uploadAccountData(accountNum: String, dataString: String): Int {
        var pos = -1
        for (i in accounts!!.indices) {
            if (accountNum == accounts!![i].accountNum) {
                pos = i
                break
            }
        }
        return if (pos == -1) {
            -1
        } else try {
            //            JsonParser parser = new JsonParser();
            //            JsonArray jsonArray = parser.parse(dataString).getAsJsonArray();
            val dataItem: DataItem = Gson().fromJson(dataString, DataItem::class.java)
            //JSonEval.getInstance().fromJson(dataString, DataItem::class.java)
            accounts!![pos].dataItems.add(dataItem)
            1
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }

    private fun uploadData(token: Int, dataString: String): Int {
        var pos = -1
        for (i in accounts!!.indices) {
            if (token == accounts!![i].token) {
                pos = i
                break
            }
        }
        return if (pos == -1) {
            -1
        } else {
            uploadAccountData(accounts!![pos].accountNum, dataString)
        }
    }

    private fun listToJsonAdmin(account: Account): String {
        val tmp = StringBuilder()
        for (i in account.dataItems.indices) {
            tmp.append("<h4>金额：").append(account.dataItems[i].money)
            tmp.append(" 方式：").append(account.dataItems[i].type)
            tmp.append(" 时间：").append(timestampToDateStr(account.dataItems[i].time))
            tmp.append(" 备注：").append(account.dataItems[i].tips)
            tmp.append("</h4>")
        }
        return tmp.toString()
    }

    private fun listToJson(account: Account): String {
        val jsonArray = JsonArray()
        var jsonObject: JsonObject
        for (i in account.dataItems.indices) {
            jsonObject = JsonObject()
            jsonObject.addProperty("money", account.dataItems[i].money)
            jsonObject.addProperty("time", account.dataItems[i].time)
            jsonObject.addProperty("tips", account.dataItems[i].tips)
            jsonObject.addProperty("type", account.dataItems[i].type)
            jsonArray.add(jsonObject)
        }
        return jsonArray.toString()
    }

    private fun clrAccountList(): Int {
        accounts = ArrayList()
        accounts!!.add(Account("admin", "admin", "sandyzhang", "男", "无敌是多么寂寞", -1, emptyUsrPic))
        return 1
    }

    private fun getData(token: Int): String {
        for (account in accounts!!) {
            if (token == account.token) {
                return listToJson(account)
            }
        }
        return "-1"
    }

    private fun getAccountInfo(accountNum: String): String {
        for (account in accounts!!) {
            if (account.accountNum == accountNum) {
                try {
                    return getAccountJsonObject(account).toString()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return "-1"
    }

    private fun getAccountList(view: String): String {
        val sb = StringBuilder()
        for (j in accounts!!.indices) {
            sb.append("<h1>====================").append(j + 1).append("====================</h1>")
            sb.append("<h1>账户：").append(accounts!![j].accountNum).append(" 密码：").append(accounts!![j].password).append("</h1>")
            sb.append("<h1>昵称：").append(accounts!![j].usrName).append(" 头像地址：").append(accounts!![j].picUrl).append("</h1>")
            sb.append("<h1>性别：").append(accounts!![j].sex).append(" 个性签名：").append(accounts!![j].text).append("</h1>")
            if (view == "text") {
                sb.append("<h2>").append(listToJsonAdmin(accounts!![j])).append("</h2>")
            } else {
                sb.append("<h2>").append(listToJson(accounts!![j])).append("</h2>")
            }
        }
        return sb.toString()
    }

    private fun clrAccountData(token: Int): Int {
        for (account in accounts!!) {
            if (token == account.token) {
                account.dataItems = ArrayList()
                return 1
            }
        }
        return -1
    }

    private fun refresh(): Int {
        destroy()
        return try {
            init(servletConfig!!)
            1
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }

    private fun findAccount(accountNum: String): Account {
        for (account in accounts!!) {
            if (account.accountNum == accountNum) {
                return account
            }
        }
        return Account("", "", "", "", "", -1, "")
    }

    private val talk: String
        get() {
            val jsonArray = JsonArray()
            for (talkItem in talkList!!) {
                jsonArray.add(getTalkItemJsonObject(talkItem))
            }
            return jsonArray.toString()
        }

    private fun talk(array: ArrayList<TalkItem>?, token: Int, text: String, picUrl: String): String {
        var pos = -1
        for (i in accounts!!.indices) {
            if (token == accounts!![i].token) {
                pos = i
                break
            }
        }
        return if (pos != -1) {
            array!!.add(TalkItem(Calendar.getInstance().timeInMillis, accounts!![pos].accountNum, picUrl, text))
            "1"
        } else {
            "-1"
        }
    }

    private fun getAccountJsonObject(account: Account): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.addProperty("accountNum", account.accountNum)
        jsonObject.addProperty("usrName", account.usrName)
        jsonObject.addProperty("sex", account.sex)
        jsonObject.addProperty("text", account.text)
        jsonObject.addProperty("picUrl", account.picUrl)
        jsonObject.addProperty("likes", account.likes)
        return jsonObject
    }

    private fun getTalkItemJsonObject(t: TalkItem): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.addProperty("time", t.time)
        jsonObject.addProperty("accountNum", t.accountNum)
        jsonObject.addProperty("picUrl", t.picUrl)
        jsonObject.addProperty("text", t.text)
        jsonObject.addProperty("usrName", findAccount(t.accountNum).usrName)
        jsonObject.addProperty("usrPic", findAccount(t.accountNum).picUrl)
        jsonObject.addProperty("sex", findAccount(t.accountNum).sex)
        var jsonArray = JsonArray()
        for (talkItem in t.replies) {
            jsonArray.add(getTalkItemJsonObject(talkItem))
        }
        jsonObject.add("replies", jsonArray)
        jsonArray = JsonArray()
        for (account in t.likeAccounts) {
            jsonArray.add(getAccountJsonObject(account))
        }
        jsonObject.add("likeAccounts", jsonArray)
        return jsonObject
    }

    private fun getAccountTalk(acccounNum: String): String {
        val jsonArray = JsonArray()
        for (t in talkList!!) {
            if (t.accountNum == acccounNum) {
                jsonArray.add(getTalkItemJsonObject(t))
            }
        }
        return jsonArray.toString()
    }

    private fun like(token: Int, talkId: Long): String {
        val a = findAccountByToken(token) ?: return "-2"
        val t = findTalkItemById(talkList, talkId)
        if (t != null) {
            t.likeAccounts.add(a)
            findAccount(t.accountNum).likes++
            return "1"
        }
        return "-1"
    }

    private fun findAccountByToken(token: Int): Account? {
        var a: Account? = null
        for (account in accounts!!) {
            if (account.token == token) {
                a = account
            }
        }
        return a
    }

    private fun delLikeInTalkItem(talkItem: TalkItem, account: Account): String {
        for (i in talkItem.likeAccounts.indices) {
            if (talkItem.likeAccounts[i].accountNum == account.accountNum) {
                talkItem.likeAccounts.removeAt(i)
                return "1"
            }
        }
        return "-1"
    }

    private fun cancelLike(token: Int, talkId: Long): String {
        val a = findAccountByToken(token) ?: return "-1"
        val t = findTalkItemById(talkList, talkId)
        if (t != null) {
            findAccount(t.accountNum).likes--
            return delLikeInTalkItem(t, a)
        }
        return "-1"
    }

    private fun findTalkItemById(list: ArrayList<TalkItem>?, id: Long): TalkItem? {
        for (t in list!!) {
            if (t.time == id) {
                return t
            }
            val talkItem = findTalkItemById(t.replies, id)
            if (talkItem != null) {
                return talkItem
            }
        }
        return null
    }

    private fun delTalkItemById(account: Account?, list: ArrayList<TalkItem>?, id: Long): String {
        for (i in list!!.indices) {
            val t = list[i]
            if (t.time == id && account!!.accountNum == t.accountNum) {
                list.removeAt(i)
                return "1"
            }
            if (delTalkItemById(account, t.replies, id) == "1") {
                return "1"
            }
        }
        return "-1"
    }

    private fun delTalk(token: Int, id: String): String {
        val d: Long = id.toLong()
        val a = findAccountByToken(token)
        return delTalkItemById(a, talkList, d)
    }

    private fun findTalk(id: String): String {
        val d: Long = id.toLong()
        val talkItem = findTalkItemById(talkList, d)
        return if (talkItem != null) {
            getTalkItemJsonObject(talkItem).toString()
        } else "-1"
    }

    private fun uploadUsrPic(token: Int, url: String): String {
        for (account in accounts!!) {
            if (token == account.token) {
                account.picUrl = url
                return "1"
            }
        }
        return "-1"
    }

    private fun setUsrInfo(token: Int, info: String): String {
        lastInfo = info
        val a = findAccountByToken(token) ?: return "-1"
        try {
            val gson = Gson()
            val b: AccountSmall = gson.fromJson(info, object : TypeToken<AccountSmall?>() {}.type)
            a.usrName = b.usrName
            a.text = b.text
            a.picUrl = b.picUrl
            a.sex = b.sex
            return "1"
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "-2"
    }
    @kotlin.Throws(ServletException::class)
    override fun init(arg0: ServletConfig?) {
        random = Random()
        accounts = ArrayList()
        accounts!!.add(Account("admin", "管理员", "sandyzhang", "男", "无敌是多么寂寞", -1, emptyUsrPic))
        talkList = ArrayList()
        servletConfig = arg0
    }

    override fun service(request: ServletRequest, response: ServletResponse) {
        val token = (request as HttpServletRequest).getHeader("token")
        request.characterEncoding = "UTF-8"
        response.characterEncoding = "UTF-8"
        response.contentType = "text/html;charset=UTF-8"
        val out = response.writer
        val action = request.getParameter("action")

        if (action == "login") {
            val accountNum = request.getParameter("accountnum")
            val password = request.getParameter("password")
            out.println(login(accountNum, password))
        }
        if (action == "register") {
            val accountNum = request.getParameter("accountnum")
            val password = request.getParameter("password")
            out.println(register(accountNum, password))
        }
        if (action == "uploaddata") {
            val data = request.getParameter("data")
            out.println(uploadData(Integer.valueOf(token), data))
        }
        if (action == "clraccountlist") {
            out.println(clrAccountList())
        }
        if (action == "getdata") {
            out.println(getData(Integer.valueOf(token)))
        }
        if (action == "getaccountlist") {
            val view = request.getParameter("view")
            out.println(getAccountList(view))
        }
        if (action == "clraccountdata") {
            out.println(clrAccountData(Integer.valueOf(token)))
        }
        if (action == "refresh") {
            out.println(refresh())
        }
        if (action == "announcement") {
            out.println(announcement)
        }
        if (action == "talk") {
            val text = request.getParameter("text")
            val picUrl = request.getParameter("picurl")
            out.println(talk(talkList, Integer.valueOf(token), text, picUrl))
        }
        if (action == "deltalk") {
            val id = request.getParameter("id")
            out.println(delTalk(Integer.valueOf(token), id))
        }
        if (action == "getaccountinfo") {
            val accountNum = request.getParameter("accountnum")
            out.println(getAccountInfo(accountNum))
        }
        if (action == "findtalk") {
            val id = request.getParameter("id")
            out.println(findTalk(id))
        }
        if (action == "reply") {
            val id = request.getParameter("id")
            val text = request.getParameter("text")
            val picUrl = request.getParameter("picurl")
            val talkI = findTalkItemById(talkList, id.toLong())
            if (talkI != null) {
                out.println(talk(talkI.replies, Integer.valueOf(token), text, picUrl))
            } else {
                out.println("-1")
            }
        }
        if (action == "gettalk") {
            out.println(talk)
        }
        if (action == "uploadusrpic") {
            val url = request.getParameter("url")
            out.println(uploadUsrPic(Integer.valueOf(token), url))
        }
        if (action == "like") {
            val talkId = request.getParameter("talkid")
            out.println(like(Integer.valueOf(token), talkId.toLong()))
        }
        if (action == "cancellike") {
            val talkId = request.getParameter("talkid")
            out.println(cancelLike(Integer.valueOf(token), talkId.toLong()))
        }
        if (action == "setusrinfo") {
            val name = request.getParameter("info")
            out.println(setUsrInfo(Integer.valueOf(token), name))
        }
        if (action == "getaccounttalk") {
            val accountnum = request.getParameter("accountnum")
            out.println(getAccountTalk(accountnum))
        }
        if (action == "lastinfo") {
            out.println(lastInfo)
        }

    }

    override fun destroy() {

    }


    override fun getServletConfig(): ServletConfig? {
        return servletConfig
    }

    override fun getServletInfo(): String? {
        return null!!
    }


}

