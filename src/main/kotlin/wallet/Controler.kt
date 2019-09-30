package wallet

import data.DigitalWalletData

class Controler {
    val digitalWallet = DigitalWalletData.build()

    fun login(loginWrapper: LoginWrapper): UserWrapper? {
        try {
            var userModelo = digitalWallet.login(loginWrapper.email, loginWrapper.password)
            return   UserWrapper(userModelo)
        }catch (error: LoginException){
            print(error)
        }
        return null
    }

    fun register(registerWrapper: RegisterWrapper): User? {
        try{
            val cvu = DigitalWallet.generateNewCVU()
            val user = User(registerWrapper.idCard, registerWrapper.firstName, registerWrapper.lastName, registerWrapper.email, registerWrapper.password, false)
            val account = Account(user, cvu)
            digitalWallet.register(user)
            digitalWallet.assignAccount(user, account)
            digitalWallet.addGift(DigitalWallet.createGift(account, 200.0))

            return user

        }catch(error: Error){
            print(error)
        }
        return null
    }

    fun transfer(transferWrapper: TransferWrapper): Boolean {
        try{
            digitalWallet.transfer(transferWrapper.fromCVU, transferWrapper.toCVU, transferWrapper.amount.toDouble())
            return true
        }catch (error: Error){
            print(error)
            return false
        }
    }

}
