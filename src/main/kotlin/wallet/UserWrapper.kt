package wallet

class UserWrapper(val user: User) {
    var email: String = user.email
    var password: String = user.password
}
