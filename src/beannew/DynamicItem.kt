package beannew

import com.google.gson.annotations.SerializedName

data class DynamicItem(
        @SerializedName("dynamic_id")
        val dynamicId: Int = 0,
        @SerializedName("user_id")
        val userId: String = "",
        @SerializedName("submit_time")
        val submitTime: Long = 0L,
        @SerializedName("text")
        val text: String = "",

        @SerializedName("nickname")
        val nickname: String = "",
        @SerializedName("avatar_url")
        val avatarUrl: String = ""

)
