package wallet

class TransactionWrapper(
    val amount: Double,
    val dateTime: String,
    val description: String = "",
    val fullDescription: String = "",
    val isCashOut: Boolean
)