package wallet
import data.DigitalWalletData
import java.time.YearMonth
import java.time.format.DateTimeFormatter


class DigitalWalletService {
    val digitalWallet = DigitalWalletData.build()

    fun login(loginWrapper: LoginWrapper): User {
        return digitalWallet.login(loginWrapper.email, loginWrapper.password)
    }

    fun register(registerWrapper: RegisterWrapper) {
        val cvu = DigitalWallet.generateNewCVU()
        val user = User(
            registerWrapper.idCard,
            registerWrapper.firstName,
            registerWrapper.lastName,
            registerWrapper.email,
            registerWrapper.password,
            false
        )
        val account = Account(user, cvu)
        digitalWallet.register(user)
        digitalWallet.assignAccount(user, account)
        digitalWallet.addGift(DigitalWallet.createGift(account, 200.0))
    }

    fun transfer(transferWrapper: TransferWrapper) {
        digitalWallet.transfer(transferWrapper.fromCVU, transferWrapper.toCVU, transferWrapper.amount.toDouble())
    }

    fun cashin(cashInWrapper: CashInWrapper) {
        val parsedDate = YearMonth.parse(cashInWrapper.endDate, DateTimeFormatter.ofPattern("MM/yyyy")).atDay(1)

        val card: Card
        if (cashInWrapper.debitCard === "true") {
            card = DebitCard(cashInWrapper.cardNumber, cashInWrapper.fullName, parsedDate, cashInWrapper.securityCode)
        } else {
            card = CreditCard(cashInWrapper.cardNumber, cashInWrapper.fullName, parsedDate, cashInWrapper.securityCode)
        }
        digitalWallet.transferMoneyFromCard(cashInWrapper.fromCVU, card, cashInWrapper.amount.toDouble())
    }

    fun getMovimientos(cvu: String): MutableList<TransactionWrapper> {

        val account: Account = digitalWallet.accountByCVU(cvu)
        return (account.transactions.map { transactional ->
            TransactionWrapper(
                amount = transactional.amount,
                description = transactional.description(),
                fullDescription = transactional.fullDescription(),
                isCashOut = transactional.isCashOut(),
                dateTime = transactional.dateTime.toLocalDate().toString()
            )
        }.toMutableList()) //account.transactions[0].dateTime
    }

    fun borrarUsuarioPorCVU(cvu: String) {
        val account: Account = digitalWallet.accountByCVU(cvu)
        digitalWallet.deleteUser(account.user)
    }

    fun balancePorCVU(cvu: String): Double? {
        val account: Account = digitalWallet.accountByCVU(cvu)
        return account.balance
    }

    fun modificarNombreDeUsuario(cvu: String?, firstname: String?) {
        val string = firstname.toString()
        assertNotEmpty(string)
        digitalWallet.accountByCVU(cvu!!).user.firstName = firstname!!
    }

    private fun assertNotEmpty(string: String) {
        if (string.isEmpty()) {
            throw EmptyValueException()
        }
    }

    fun modificarApellidoDeUsuario(cvu: String?, lastname: String?) {
        val string = lastname.toString()
        assertNotEmpty(string)
        digitalWallet.accountByCVU(cvu!!).user.lastName = lastname!!
    }

}


