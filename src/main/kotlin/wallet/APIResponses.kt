package wallet

data class APIResponse(val message: String)

data class APIResponseWithCVU(val message: String, val cvu: String)

class EmptyValueException : RuntimeException() {
    override val message: String = "Cannot be empty fields"
}