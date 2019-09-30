package wallet

import data.DigitalWalletData

class Controler {
    val digitalWallet = DigitalWalletData.build()

    fun login(loginWrapper: LoginWrapper): UserWrapper? {
        print(digitalWallet.users)
        try {
            var userModelo = digitalWallet.login(loginWrapper.email, loginWrapper.password)
            return   UserWrapper(userModelo)
        }catch (error: LoginException){
            print(error)
        }
        return null
    }

}
