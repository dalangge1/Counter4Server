package beannew

import com.google.gson.annotations.SerializedName

data class CommentItem(
        @SerializedName("id")
        val id: Int = 0,
        @SerializedName("reply_id")
        val replyId: Int = 0,
        @SerializedName("user_id")
        val userId: String = "",
        @SerializedName("submit_time")
        val submitTime: Long = 0L,
        @SerializedName("text")
        val text: String = "",
        @SerializedName("which")
        val which: Int = 0,
        @SerializedName("reply_user_nickname")
        var replyUserNickname: String = "",

        @SerializedName("nickname")
        var nickname: String = "",
        @SerializedName("avatar_url")
        var avatarUrl: String = "",
        @SerializedName("reply_list") // 只有一级评论有，二级评论没有这个，默认空
        var replyList: List<CommentItem> = listOf(),

        @SerializedName("praise")
        var praise: List<User> = listOf(),
        @SerializedName("reply_user_id")
        var replyUserId: String = ""
)
