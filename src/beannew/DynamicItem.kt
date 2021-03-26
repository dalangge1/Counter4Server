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
        var nickname: String = "",
        @SerializedName("avatar_url")
        var avatarUrl: String = "",

        @SerializedName("pic_url")
        var picUrl: List<String> = listOf(),

        @SerializedName("comment_list")
        var commentList: List<CommentItem> = listOf(),

        @SerializedName("praise")
        var praise: List<User> = listOf()

)
