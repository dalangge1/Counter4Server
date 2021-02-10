package bean

import page.Counter4ServletKt
import java.util.ArrayList

class TalkItem(var time: Long, var accountNum: String, var picUrl: String, var text: String) {
    var replies: ArrayList<TalkItem>
    var likeAccounts: ArrayList<Account>

    init {
        replies = ArrayList()
        likeAccounts = ArrayList()
    }
}