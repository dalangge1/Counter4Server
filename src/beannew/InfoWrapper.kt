package beannew

import com.google.gson.Gson
import java.io.Serializable
import javax.servlet.http.HttpServletResponse

open class InfoWrapper : Serializable {
    var status: Int = 0
    var info: String = ""

    companion object {
        var response: HttpServletResponse? = null
        fun setResponseObject (response: HttpServletResponse) {
            this.response = response
        }
        fun newInfo(status: Int = 400, info: String = ""): String {
            return InfoWrapper().apply{
                this.status = status
                this@Companion.response?.status = status
                this.info = info
            }.parseToString()
        }
    }
}

val InfoWrapper.isSuccessful get() = (status == 200 || status == 10000)

fun InfoWrapper.parseToString(): String {
    return Gson().toJson(this)
}

