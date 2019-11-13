package wallet

data class UserWrapper(
    var idCard: String,
    var firstName: String,
    var lastName: String,
    var email: String,
    var isAdmin: Boolean
)