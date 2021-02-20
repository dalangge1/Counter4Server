package page

import beannew.ApiWrapper
import beannew.InfoWrapper
import beannew.Token
import model.MySqlDao
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
        val tokenGlobal = (request as HttpServletRequest).getHeader("token")
        request.characterEncoding = "UTF-8"
        response.characterEncoding = "UTF-8"
        response.contentType = "text/html;charset=UTF-8"
        val out = response.writer
        val action = request.getParameter("action")?: ""

        if (action == "") {
            out.println("无法匹配请求")
        }

        if (action == "register") {
            val userId = request.getParameter("user_id")
            val password = request.getParameter("password")
            if (userId.isNullOrBlank() || password.isNullOrBlank()) {
                out.println(InfoWrapper.newInfo(info = "用户名或密码不能为空"))
            }
            val isSuccess = MySqlDao.register(userId, password)
            if (isSuccess) {
                out.println(InfoWrapper.newInfo(info = "注册成功"))
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
            val token = MySqlDao.login(userId, password)
            if (token != -1) {
                out.println(ApiWrapper.newApi(info = "登录成功", data = Token(token)))
            } else {
                out.println(InfoWrapper.newInfo(info = "登录失败"))
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

