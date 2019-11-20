package wallet

data class APIResponse(val message: String)

data class APIResponseWithCVU(val message: String, val cvu: String)
