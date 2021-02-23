package page

import beannew.ApiWrapper
import beannew.InfoWrapper
import beannew.Token
import model.Dao
import java.lang.Exception
import javax.servlet.*
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServletRequest

@WebServlet(name = "page.Counter4ServletKtSql")
class Counter4ServletKtSql : Servlet {


    private var servletConfig: ServletConfig? = null


    @kotlin.Throws(ServletException::class)
    override fun init(arg0: ServletConfig?) {
        servletConfig = arg0
    }

    override fun service(request: ServletRequest, response: ServletResponse) {
        val tokenGlobal = (request as HttpServletRequest).getHeader("token") ?: null
        request.characterEncoding = "UTF-8"
        response.characterEncoding = "UTF-8"
        response.contentType = "text/html;charset=UTF-8"
        val out = response.writer
        val action = request.getParameter("action") ?: ""

        if (action == "") {
            out.println("无法匹配请求")
        }

        if (action == "register") {
            val userId = request.getParameter("user_id")
            val password = request.getParameter("password")
            if (userId.isNullOrBlank() || password.isNullOrBlank()) {
                out.println(InfoWrapper.newInfo(info = "用户名或密码不能为空"))
            }
            val isSuccess = Dao.register(userId, password)
            if (isSuccess) {
                out.println(InfoWrapper.newInfo(status = 200, info = "注册成功"))
            } else {
                out.println(InfoWrapper.newInfo(info = "注册失败"))
            }
        }

        if (action == "login") {
            val userId = request.getParameter("user_id")
            val password = request.getParameter("password")
            if (userId.isNullOrBlank() || password.isNullOrBlank()) {
                out.println(InfoWrapper.newInfo(info = "用户名或密码不能为空"))
            }
            val token = Dao.login(userId, password)
            if (token != -1) {
                out.println(ApiWrapper.newApi(status = 200, info = "登录成功", data = Token(token)))
            } else {
                out.println(InfoWrapper.newInfo(info = "登录失败"))
            }
        }

        if (action == "releaseDynamic") {
            val text = request.getParameter("text")
            val topic = request.getParameter("topic")
            val picUrls = request.getParameter("pic_url")?.split(",")
            tokenGlobal?.let {
                val dynamicId: Int = if (picUrls != null) {
                    Dao.releaseDynamic(it.toInt(), text, topic, *picUrls.toTypedArray())
                } else {
                    Dao.releaseDynamic(it.toInt(), text, topic)
                }
                when (dynamicId) {
                    -1 -> {
                        out.println(InfoWrapper.newInfo(info = "发送动态失败"))
                    }
                    -2 -> { // -2
                        out.println(InfoWrapper.newInfo(info = "发送动态成功，储存图片链接失败"))
                    }
                    -3 -> { // -3
                        out.println(InfoWrapper.newInfo(info = "token过期"))
                    }
                    else -> {
                        out.println(ApiWrapper.newApi(status = 200, info = "发送动态成功", data = dynamicId))
                    }
                }

            }

        }

        if (action == "getAllDynamic") {
            val pos: Int
            val size: Int
            val topic: String? = request.getParameter("topic")
            try {
                pos = (request.getParameter("pos") ?: "0").toInt()
                size = (request.getParameter("size") ?: "10").toInt()
            } catch (e: Exception) {
                out.println(InfoWrapper.newInfo(info = "参数格式有误"))
                return
            }
            Dao.getAllDynamic(pos, size, topic)?.let {
                out.println(ApiWrapper.newApi(status = 200, info = "获取成功", data = it))
                return
            }
            out.println(InfoWrapper.newInfo(info = "获取失败"))


        }


        if (action == "deleteDynamic") {
            val dynamicId: Int
            try {
                dynamicId = (request.getParameter("dynamic_id") ?: "0").toInt()
            } catch (e: Exception) {
                out.println(InfoWrapper.newInfo(info = "参数格式有误"))
                return
            }
            if (Dao.deleteDynamic(dynamicId)) {
                out.println(InfoWrapper.newInfo(status = 200, info = "删除成功"))
            } else {
                out.println(InfoWrapper.newInfo(info = "删除失败"))
            }

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

