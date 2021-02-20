package bean

data class Account(
        var accountNum: String,
        var usrName: String?,
        var password: String,
        var sex: String?,
        var text: String?,
        var token: Int,
        var picUrl: String?
        ) {
    var likes = 0
    var dataItems: ArrayList<DataItem> = ArrayList()

}