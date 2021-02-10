package page

import bean.Account
import bean.AccountSmall
import bean.DataItem
import bean.TalkItem
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import utils.jsonevalutils.JSonEval
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.servlet.*
import javax.servlet.annotation.WebServlet

@WebServlet(name = "page.Counter4ServletKt")
class Counter4ServletKt : Servlet {
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

    //    String resetString(String a) {
    //        if (a.charAt(0) == '\"' && a.charAt(a.length() - 1) == '\"') {
    //            return a.substring(1, a.length() - 1);
    //        } else {
    //            return a;
    //        }
    //    }
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
            val dataItem: DataItem = JSonEval.getInstance().fromJson(dataString, DataItem::class.java)
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

    //===========================================================================
    @kotlin.Throws(ServletException::class)
    override fun init(arg0: ServletConfig) {
        random = Random()
        accounts = ArrayList()
        accounts!!.add(Account("admin", "管理员", "sandyzhang", "男", "无敌是多么寂寞", -1, emptyUsrPic))
        talkList = ArrayList()
        servletConfig = getServletConfig()
        //
//        try{
//            File f = new File(path);
//            FileInputStream fip = new FileInputStream(f);
//            InputStreamReader reader = new InputStreamReader(fip, StandardCharsets.UTF_8);
//            StringBuffer sb = new StringBuffer();
//            while (reader.ready()) {
//                sb.append((char) reader.read());
//            }
//            System.out.println(sb.toString());
//            reader.close();
//            fip.close();
//
//            //sb为txt中的内容了
//
//            ArrayList<String> line=new ArrayList<>();
//            StringBuffer sTmp=new StringBuffer();
//            for (int i=0;i<sb.length();i++){
//                if(sb.charAt(i)!=enter){
//                    sTmp.append(sb.charAt(i));
//                }else {
//                    line.add(new String(sTmp));
//                    sTmp=new StringBuffer();
//                }
//            }
//            //line[n]是第n行的内容
//
//            for (int i=0;i<line.size();i+=6){
//
//                accounts.add(new Account(line.get(i),line.get(i+1),line.get(i+2),line.get(i+3),line.get(i+4),-1,line.get(i+5)));
//
//            }
//
//
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//        try{
//            for (int i=0;i<accounts.size();i++){
//                File fTmp = new File(logPath+accounts.get(i).accountNum+".txt");
//                FileInputStream fTmpip = new FileInputStream(fTmp);
//                InputStreamReader reader2 = new InputStreamReader(fTmpip, StandardCharsets.UTF_8);
//                StringBuffer sb2 = new StringBuffer();
//                while (reader2.ready()) {
//                    sb2.append((char) reader2.read());
//                }
//                uploadAccountData(accounts.get(i).accountNum,sb2.toString());
//                reader2.close();
//                fTmpip.close();
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//        Timer timer = new Timer();
//        TimerTask task = new TimerTask() {
//            @Override
//            public void run() {
//                destroy();
//                try {
//                    init(servletConfig);
//
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//        };
//        timer.schedule(task,12*60*60000,12*60*60000);
    }

    override fun service(arg0: ServletRequest, arg1: ServletResponse) {
        arg0.characterEncoding = "UTF-8"
        arg1.characterEncoding = "UTF-8"
        val arg0String = arg0.getParameter("page")
        arg1.contentType = "text/html;charset=UTF-8"
        val out = arg1.writer
        if (arg0String == "test") {
            val post = arg0.getParameter("post")
            if (post == "login") {
                val accountNum = arg0.getParameter("accountnum")
                val password = arg0.getParameter("password")
                out.println(login(accountNum, password))
            }
            if (post == "register") {
                val accountNum = arg0.getParameter("accountnum")
                val password = arg0.getParameter("password")
                out.println(register(accountNum, password))
            }
            if (post == "uploaddata") {
                val token = arg0.getParameter("token")
                val data = arg0.getParameter("data")
                out.println(uploadData(Integer.valueOf(token), data))
            }
            if (post == "clraccountlist") {
                out.println(clrAccountList())
            }
            if (post == "getdata") {
                val token = arg0.getParameter("token")
                out.println(getData(Integer.valueOf(token)))
            }
            if (post == "getaccountlist") {
                val view = arg0.getParameter("view")
                out.println(getAccountList(view))
            }
            if (post == "clraccountdata") {
                val token = arg0.getParameter("token")
                out.println(clrAccountData(Integer.valueOf(token)))
            }
            if (post == "refresh") {
                out.println(refresh())
            }
            if (post == "announcement") {
                out.println(announcement)
            }
            if (post == "talk") {
                val token = arg0.getParameter("token")
                val text = arg0.getParameter("text")
                val picUrl = arg0.getParameter("picurl")
                out.println(talk(talkList, Integer.valueOf(token), text, picUrl))
            }
            if (post == "deltalk") {
                val token = arg0.getParameter("token")
                val id = arg0.getParameter("id")
                out.println(delTalk(Integer.valueOf(token), id))
            }
            if (post == "getaccountinfo") {
                val accountNum = arg0.getParameter("accountnum")
                out.println(getAccountInfo(accountNum))
            }
            if (post == "findtalk") {
                val id = arg0.getParameter("id")
                out.println(findTalk(id))
            }
            if (post == "reply") {
                val token = arg0.getParameter("token")
                val id = arg0.getParameter("id")
                val text = arg0.getParameter("text")
                val picUrl = arg0.getParameter("picurl")
                val talkI = findTalkItemById(talkList, id.toLong())
                if (talkI != null) {
                    out.println(talk(talkI.replies, Integer.valueOf(token), text, picUrl))
                } else {
                    out.println("-1")
                }
            }
            if (post == "gettalk") {
                out.println(talk)
            }
            if (post == "uploadusrpic") {
                val token = arg0.getParameter("token")
                val url = arg0.getParameter("url")
                out.println(uploadUsrPic(Integer.valueOf(token), url))
            }
            if (post == "like") {
                val token = arg0.getParameter("token")
                val talkId = arg0.getParameter("talkid")
                out.println(like(Integer.valueOf(token), talkId.toLong()))
            }
            if (post == "cancellike") {
                val token = arg0.getParameter("token")
                val talkId = arg0.getParameter("talkid")
                out.println(cancelLike(Integer.valueOf(token), talkId.toLong()))
            }
            if (post == "setusrinfo") {
                val token = arg0.getParameter("token")
                val name = arg0.getParameter("info")
                out.println(setUsrInfo(Integer.valueOf(token), name))
            }
            if (post == "getaccounttalk") {
                val accountnum = arg0.getParameter("accountnum")
                out.println(getAccountTalk(accountnum))
            }
            if (post == "lastinfo") {
                out.println(lastInfo)
            }
        } else {
            out.println("施工中..." + arg0.getParameter("txt"))
        }
    }

    override fun destroy() {
//        try {
//            File f = new File(path);
//            FileOutputStream fop = new FileOutputStream(f);
//            OutputStreamWriter writer = new OutputStreamWriter(fop, StandardCharsets.UTF_8);
//
//            //中间填要覆写的内容==============
//            for(int i=0;i<accounts.size();i++){
//                writer.write(accounts.get(i).accountNum);
//                writer.write(enter);
//                writer.write(accounts.get(i).usrName);
//                writer.write(enter);
//                writer.write(accounts.get(i).password);
//                writer.write(enter);
//                writer.write(accounts.get(i).sex);
//                writer.write(enter);
//                writer.write(accounts.get(i).text);
//                writer.write(enter);
//                writer.write(accounts.get(i).picUrl);
//                writer.write(enter);
//                System.out.println(accounts.get(i).accountNum);
//                System.out.println(accounts.get(i).password);
//                System.out.println(accounts.get(i).picUrl);
//
//            }
//            //===============================
//
//            writer.close();
//            fop.close();
//
//            for (int i=0;i<accounts.size();i++){
//                String strTmp=listToJson(accounts.get(i));
//                File fTmp = new File(logPath+accounts.get(i).accountNum+".txt");
//                FileOutputStream fTmpop = new FileOutputStream(fTmp);
//                OutputStreamWriter writer2 = new OutputStreamWriter(fTmpop, StandardCharsets.UTF_8);
//                writer2.write(strTmp);
//                writer2.close();
//                fTmpop.close();
//            }
//
//        }catch (Exception e){
//            e.printStackTrace();
//        }
    }

    override fun getServletConfig(): ServletConfig {
        return null!!
    }

    override fun getServletInfo(): String {
        return null!!
    }

    companion object {
        private const val path = "/usr/local/tomcat/tomcat-8.5/webapps/mweb/sav/msav.txt"

        //private static String path="D:/sav/msav.txt";
        //private static String logPath="D:/sav/";
        private const val logPath = "/usr/local/tomcat/tomcat-8.5/webapps/mweb/sav/"
        private const val enter = '\n'
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
}