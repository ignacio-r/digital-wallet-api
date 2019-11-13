package wallet

import java.time.LocalDateTime

class TransactionWrapper(
    val amount: Double,
    val dateTime: LocalDateTime,
    val description: String = "",
    val fullDescription: String = "",
    val isCashOut: Boolean
)