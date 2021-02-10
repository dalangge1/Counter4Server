


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import utils.JSonEvalUtils.JSonEval;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;


@WebServlet(name = "Counter4Servlet")
public class Counter4Servlet implements Servlet {
    private static String path = "/usr/local/tomcat/tomcat-8.5/webapps/mweb/sav/msav.txt";
    //private static String path="D:/sav/msav.txt";
    //private static String logPath="D:/sav/";
    private static String logPath = "/usr/local/tomcat/tomcat-8.5/webapps/mweb/sav/";
    private static char enter = '\n';
    private static String emptyUsrPic = "https://huaban.com/img/error_page/img_404.png";

    private static String lastInfo = "null";
    private static String announcement = "[7月17日更新]祝红岩的学长学姐们，学业进步，考研成功，面试顺利！！！";

    private ServletConfig servletConfig;

    private Random random;


    class DataItem {
        Double money;
        Long time;
        String tips;//用户输入备注
        String type;

        public DataItem(Double money, Long time, String tips, String type) {
            this.money = money;
            this.time = time;
            this.tips = tips;
            this.type = type;
        }
    }

    class TalkItem {
        Long time;
        String accountNum;
        String picUrl;
        String text;
        ArrayList<TalkItem> replies;
        ArrayList<Account> likeAccounts;

        TalkItem(Long time, String accountNum, String picUrl, String text) {
            this.time = time;
            this.accountNum = accountNum;
            this.picUrl = picUrl;
            this.text = text;
            replies = new ArrayList<>();
            likeAccounts = new ArrayList<>();
        }
    }

    class AccountSmall{
        String text;
        String usrName;
        String picUrl;
        String sex;
    }

    class Account {
        String accountNum;
        String usrName;
        String password;
        String picUrl;
        String text;
        String sex;
        int likes;
        int token;
        ArrayList<DataItem> dataItems;


        Account(String accountNum, String usrName, String password, String sex, String text, int token, String picUrl) {
            this.accountNum = accountNum;
            this.usrName = usrName;
            this.sex = sex;
            this.text = text;
            this.password = password;
            this.token = token;
            this.picUrl = picUrl;
            likes = 0;
            dataItems = new ArrayList<>();

        }

    }

    private ArrayList<Account> accounts;

    private ArrayList<TalkItem> talkList;


    private int login(String accountNum, String password) {//return token
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).accountNum.equals(accountNum)) {
                if (accounts.get(i).password.equals(password)) {
                    boolean flag;
                    int tokenTmp;
                    do {
                        tokenTmp = random.nextInt(8999) + 1000;
                        flag = false;
                        for (Account account : accounts) {
                            if (account.token == tokenTmp) {
                                flag = true;
                            }
                        }
                    } while (flag);
                    accounts.get(i).token = tokenTmp;
                    return tokenTmp;
                }
            }
        }
        return -1;
    }

    private int register(String accountNum, String password) {
        for (Account account : accounts) {
            if (account.accountNum.equals(accountNum)) {
                return -1;
            }
        }
        accounts.add(new Account(accountNum, accountNum, password, "保密", "这个人还没有介绍自己哦~", -1, emptyUsrPic));
        return 1;
    }

//    String resetString(String a) {
//        if (a.charAt(0) == '\"' && a.charAt(a.length() - 1) == '\"') {
//            return a.substring(1, a.length() - 1);
//        } else {
//            return a;
//        }
//    }

    private int uploadAccountData(String accountNum, String dataString) {
        int pos = -1;
        for (int i = 0; i < accounts.size(); i++) {
            if (accountNum.equals(accounts.get(i).accountNum)) {
                pos = i;
                break;
            }
        }
        if (pos == -1) {
            return -1;
        }
        try {
//            JsonParser parser = new JsonParser();
//            JsonArray jsonArray = parser.parse(dataString).getAsJsonArray();
            DataItem dataItem = JSonEval.getInstance().fromJson(dataString, DataItem.class);
            accounts.get(pos).dataItems.add(dataItem);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private int uploadData(int token, String dataString) {
        int pos = -1;
        for (int i = 0; i < accounts.size(); i++) {
            if (token == accounts.get(i).token) {
                pos = i;
                break;
            }
        }
        if (pos == -1) {
            return -1;
        } else {
            return uploadAccountData(accounts.get(pos).accountNum, dataString);
        }

    }


    private String listToJsonAdmin(Account account) {
        StringBuilder tmp = new StringBuilder();
        for (int i = 0; i < account.dataItems.size(); i++) {
            tmp.append("<h4>金额：").append(account.dataItems.get(i).money);
            tmp.append(" 方式：").append(account.dataItems.get(i).type);
            tmp.append(" 时间：").append(timestampToDateStr(account.dataItems.get(i).time));
            tmp.append(" 备注：").append(account.dataItems.get(i).tips);
            tmp.append("</h4>");
        }
        return tmp.toString();

    }

    private String listToJson(Account account) {
        JsonArray jsonArray = new JsonArray();
        JsonObject jsonObject;
        for (int i = 0; i < account.dataItems.size(); i++) {
            jsonObject = new JsonObject();
            jsonObject.addProperty("money", account.dataItems.get(i).money);
            jsonObject.addProperty("time", account.dataItems.get(i).time);
            jsonObject.addProperty("tips", account.dataItems.get(i).tips);
            jsonObject.addProperty("type", account.dataItems.get(i).type);
            jsonArray.add(jsonObject);
        }
        return jsonArray.toString();

    }

    private int clrAccountList() {
        accounts = new ArrayList<>();
        accounts.add(new Account("admin", "admin", "sandyzhang", "男", "无敌是多么寂寞", -1, emptyUsrPic));
        return 1;
    }

    private String getData(int token) {
        for (Account account : accounts) {
            if (token == account.token) {
                return listToJson(account);
            }
        }
        return "-1";
    }


    private String getAccountInfo(String accountNum) {
        for (Account account : accounts) {
            if (account.accountNum.equals(accountNum)) {
                try {
                    return getAccountJsonObject(account).toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return "-1";
    }

    private String getAccountList(String view) {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < accounts.size(); j++) {
            sb.append("<h1>====================").append(j + 1).append("====================</h1>");
            sb.append("<h1>账户：").append(accounts.get(j).accountNum).append(" 密码：").append(accounts.get(j).password).append("</h1>");
            sb.append("<h1>昵称：").append(accounts.get(j).usrName).append(" 头像地址：").append(accounts.get(j).picUrl).append("</h1>");
            sb.append("<h1>性别：").append(accounts.get(j).sex).append(" 个性签名：").append(accounts.get(j).text).append("</h1>");
            if (view.equals("text")) {
                sb.append("<h2>").append(listToJsonAdmin(accounts.get(j))).append("</h2>");
            } else {
                sb.append("<h2>").append(listToJson(accounts.get(j))).append("</h2>");
            }

        }
        return sb.toString();
    }

    private int clrAccountData(int token) {
        for (Account account : accounts) {
            if (token == account.token) {
                account.dataItems = new ArrayList<>();
                return 1;
            }
        }
        return -1;
    }

    private static String timestampToDateStr(Long timeStamp) {
        String timeString;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日ahh点");
        timeString = sdf.format(new Date(timeStamp));//单位秒
        return timeString;
    }

    private int refresh() {

        destroy();
        try {
            init(servletConfig);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

    }

    private Account findAccount(String accountNum){
        for (Account account : accounts) {
            if(account.accountNum.equals(accountNum)){
                return account;
            }
        }
        return new Account("","","","","",-1,"");

    }

    private String getTalk() {
        JsonArray jsonArray = new JsonArray();
        for (TalkItem talkItem : talkList) {

            jsonArray.add(getTalkItemJsonObject(talkItem));
        }

        return jsonArray.toString();
    }

    private String talk(ArrayList<TalkItem> array, int token, String text,String picUrl) {
        int pos = -1;
        for (int i = 0; i < accounts.size(); i++) {
            if (token == accounts.get(i).token) {
                pos = i;
                break;
            }
        }
        if (pos != -1) {
            array.add(new TalkItem(Calendar.getInstance().getTimeInMillis(), accounts.get(pos).accountNum, picUrl, text));
            return "1";
        } else {
            return "-1";
        }

    }

    private JsonObject getAccountJsonObject(Account account){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("accountNum", account.accountNum);
        jsonObject.addProperty("usrName", account.usrName);
        jsonObject.addProperty("sex", account.sex);
        jsonObject.addProperty("text", account.text);
        jsonObject.addProperty("picUrl", account.picUrl);
        jsonObject.addProperty("likes", account.likes);
        return jsonObject;
    }

    private JsonObject getTalkItemJsonObject(TalkItem t){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("time", t.time);
        jsonObject.addProperty("accountNum", t.accountNum);
        jsonObject.addProperty("picUrl", t.picUrl);
        jsonObject.addProperty("text", t.text);
        jsonObject.addProperty("usrName", findAccount(t.accountNum).usrName);
        jsonObject.addProperty("usrPic", findAccount(t.accountNum).picUrl);
        jsonObject.addProperty("sex", findAccount(t.accountNum).sex);
        JsonArray jsonArray = new JsonArray();
        for (TalkItem talkItem : t.replies){
            jsonArray.add(getTalkItemJsonObject(talkItem));
        }
        jsonObject.add("replies",jsonArray);
        jsonArray = new JsonArray();
        for (Account account : t.likeAccounts){
            jsonArray.add(getAccountJsonObject(account));
        }
        jsonObject.add("likeAccounts",jsonArray);

        return jsonObject;
    }

    private String getAccountTalk(String acccounNum){
        JsonArray jsonArray = new JsonArray();
        for (TalkItem t :
                talkList) {
            if (t.accountNum.equals(acccounNum)) {
                jsonArray.add(getTalkItemJsonObject(t));
            }
        }
        return jsonArray.toString();
    }


    private String like(int token,Long talkId){
        Account a = findAccountByToken(token);
        if (a == null){
            return "-2";
        }
        TalkItem t = findTalkItemById(talkList,talkId);

        if(t!=null){
            t.likeAccounts.add(a);
            findAccount(t.accountNum).likes++;
            return "1";
        }
        return "-1";
    }

    private Account findAccountByToken(int token){
        Account a = null;
        for (Account account:accounts) {
            if (account.token == token){
                a = account;
            }
        }
        return a;
    }

    private String delLikeInTalkItem(TalkItem talkItem,Account account){
        for(int i =0;i<talkItem.likeAccounts.size();i++){
            if (talkItem.likeAccounts.get(i).accountNum.equals(account.accountNum)){
                talkItem.likeAccounts.remove(i);
                return "1";
            }
        }
        return "-1";
    }

    private String cancelLike(int token,Long talkId){
        Account a = findAccountByToken(token);
        if (a == null){
            return "-1";
        }
        TalkItem t = findTalkItemById(talkList,talkId);
        if(t!=null){
            findAccount(t.accountNum).likes--;
            return delLikeInTalkItem(t,a);
        }
        return "-1";
    }

    private TalkItem findTalkItemById(ArrayList<TalkItem> list,Long id){
        for(TalkItem t: list){
            if(t.time.equals(id)){
                return t;
            }
            TalkItem talkItem = findTalkItemById(t.replies,id);
            if(talkItem!=null){
                return talkItem;
            }
        }
        return null;
    }







    private String delTalkItemById(Account account,ArrayList<TalkItem> list,Long id){
        for (int i = 0; i < list.size(); i++) {
            TalkItem t = list.get(i);
            if (t.time.equals(id) && account.accountNum.equals(t.accountNum)) {
                list.remove(i);
                return "1";
            }
            if(delTalkItemById(account,t.replies, id).equals("1")){
                return "1";
            }
        }
        return "-1";
    }

    private String delTalk(int token, String id) {
        Long d = Long.parseLong(id);
        Account a = findAccountByToken(token);
        return delTalkItemById(a,talkList,d);

    }

    private String findTalk(String id) {
        Long d = Long.parseLong(id);
        TalkItem talkItem = findTalkItemById(talkList,d);
        if(talkItem!=null){
            return getTalkItemJsonObject(talkItem).toString();
        }
        return "-1";

    }

    private String uploadUsrPic(int token, String url) {
        for (Account account : accounts) {
            if (token == account.token) {
                account.picUrl = url;
                return "1";
            }
        }
        return "-1";
    }

    private String setUsrInfo(int token, String info) {
        lastInfo = info;
        Account a = findAccountByToken(token);
        if (a == null){
            return "-1";
        }
        try{
            Gson gson = new Gson();
            AccountSmall b = gson.fromJson(info, new TypeToken<AccountSmall>(){}.getType());
            a.usrName = b.usrName;
            a.text = b.text;
            a.picUrl = b.picUrl;
            a.sex = b.sex;
            return "1";
        }catch (Exception e){
            e.printStackTrace();
        }
        return "-2";
    }


    //===========================================================================


    public void init(ServletConfig arg0) throws ServletException {
        random = new Random();
        accounts = new ArrayList<>();
        accounts.add(new Account("admin", "管理员", "sandyzhang", "男", "无敌是多么寂寞", -1, emptyUsrPic));
        talkList = new ArrayList<>();
        servletConfig = getServletConfig();
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


    public void service(ServletRequest arg0, ServletResponse arg1)
            throws ServletException, IOException {
        arg0.setCharacterEncoding("UTF-8");
        arg1.setCharacterEncoding("UTF-8");
        String arg0String = arg0.getParameter("page");

        arg1.setContentType("text/html;charset=UTF-8");
        PrintWriter out = arg1.getWriter();

        if (arg0String.equals("test")) {
            String post = arg0.getParameter("post");

            if (post.equals("login")) {
                String accountNum = arg0.getParameter("accountnum");
                String password = arg0.getParameter("password");
                out.println(login(accountNum, password));
            }
            if (post.equals("register")) {
                String accountNum = arg0.getParameter("accountnum");
                String password = arg0.getParameter("password");
                out.println(register(accountNum, password));
            }
            if (post.equals("uploaddata")) {
                String token = arg0.getParameter("token");
                String data = arg0.getParameter("data");
                out.println(uploadData(Integer.valueOf(token), data));
            }
            if (post.equals("clraccountlist")) {
                out.println(clrAccountList());
            }
            if (post.equals("getdata")) {
                String token = arg0.getParameter("token");
                out.println(getData(Integer.valueOf(token)));
            }
            if (post.equals("getaccountlist")) {
                String view = arg0.getParameter("view");
                out.println(getAccountList(view));
            }
            if (post.equals("clraccountdata")) {
                String token = arg0.getParameter("token");
                out.println(clrAccountData(Integer.valueOf(token)));
            }
            if (post.equals("refresh")) {
                out.println(refresh());
            }
            if (post.equals("announcement")) {
                out.println(announcement);
            }
            if (post.equals("talk")) {
                String token = arg0.getParameter("token");
                String text = arg0.getParameter("text");
                String picUrl = arg0.getParameter("picurl");
                out.println(talk(talkList, Integer.valueOf(token), text,picUrl));
            }
            if (post.equals("deltalk")) {
                String token = arg0.getParameter("token");
                String id = arg0.getParameter("id");
                out.println(delTalk(Integer.valueOf(token), id));
            }
            if (post.equals("getaccountinfo")) {
                String accountNum = arg0.getParameter("accountnum");
                out.println(getAccountInfo(accountNum));
            }
            if (post.equals("findtalk")) {
                String id = arg0.getParameter("id");
                out.println(findTalk(id));
            }
            if (post.equals("reply")) {
                String token = arg0.getParameter("token");
                String id = arg0.getParameter("id");
                String text = arg0.getParameter("text");
                String picUrl = arg0.getParameter("picurl");
                TalkItem talkI = findTalkItemById(talkList,Long.parseLong(id));
                if (talkI != null){
                    out.println(talk(talkI.replies, Integer.valueOf(token), text,picUrl));
                }else {
                    out.println("-1");
                }



            }
            if (post.equals("gettalk")) {
                out.println(getTalk());
            }
            if (post.equals("uploadusrpic")) {
                String token = arg0.getParameter("token");
                String url = arg0.getParameter("url");
                out.println(uploadUsrPic(Integer.valueOf(token), url));
            }
            if (post.equals("like")) {
                String token = arg0.getParameter("token");
                String talkId = arg0.getParameter("talkid");
                out.println(like(Integer.valueOf(token), Long.parseLong(talkId)));
            }
            if (post.equals("cancellike")) {
                String token = arg0.getParameter("token");
                String talkId = arg0.getParameter("talkid");
                out.println(cancelLike(Integer.valueOf(token), Long.parseLong(talkId)));
            }
            if (post.equals("setusrinfo")) {
                String token = arg0.getParameter("token");
                String name = arg0.getParameter("info");
                out.println(setUsrInfo(Integer.valueOf(token), name));
            }
            if (post.equals("getaccounttalk")) {
                String accountnum = arg0.getParameter("accountnum");
                out.println(getAccountTalk(accountnum));
            }
            if (post.equals("lastinfo")) {
                out.println(lastInfo);
            }


        } else {
            out.println("施工中..." + arg0.getParameter("txt"));
        }


    }

    public void destroy() {
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

    public ServletConfig getServletConfig() {

        return null;
    }

    public String getServletInfo() {

        return null;
    }
}