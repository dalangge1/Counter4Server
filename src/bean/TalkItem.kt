package bean

import java.util.*

class TalkItem(
        var time: Long,
        var accountNum: String,
        var picUrl: String,
        var text: String
        ) {
    var replies: ArrayList<TalkItem> = ArrayList()
    var likeAccounts: ArrayList<Account> = ArrayList()

}