package beannew

import com.google.gson.annotations.SerializedName

data class DynamicItem(
        @SerializedName("dynamic_id")
        val dynamicId: String = "",
        @SerializedName("user_id")
        val userId: String = "",
        @SerializedName("submit_time")
        val submitTime: String = "",
        @SerializedName("text")
        val text: String = "",

        @SerializedName("nickname")
        val nickname: String = "",
        @SerializedName("avatar_url")
        val avatarUrl: String = ""

)
