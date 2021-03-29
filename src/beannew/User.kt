package beannew

import com.google.gson.annotations.SerializedName

data class User(
        @SerializedName("user_id")
        val userId: String = "",
        @SerializedName("nickname")
        val nickname: String = "",
        @SerializedName("password")
        var password: String = "",
        @SerializedName("register_date")
        val registerDate: Long = 0L,
        @SerializedName("sex")
        val sex: String = "",
        @SerializedName("text")
        val text: String = "",
        @SerializedName("avatar_url")
        val avatarUrl: String = ""
)
