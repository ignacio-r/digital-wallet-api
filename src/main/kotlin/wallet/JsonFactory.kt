package wallet

import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonObject

class JsonFactory {

    fun emailPasswordJson(email: String, password: String): JsonObject {
        return jsonObject(
            "email" to email,
            "password" to password
        )
    }

    fun cashInJson(
        fromCVU: String, amount: String, cardNumber: String, fullname: String,
        endDate: String, securityCode: String
    ): JsonObject {
        return jsonObject(
            "fromCVU" to fromCVU,
            "amount" to amount,
            "cardNumber" to cardNumber,
            "fullName" to fullname,
            "endDate" to endDate,
            "securityCode" to securityCode
        )
    }

    fun transferJson(fromCVU: String, toCVU: String, amount: String): JsonObject {
        return jsonObject(
            "fromCVU" to fromCVU,
            "toCVU" to toCVU,
            "amount" to amount
        )
    }

    fun registerUserJson(
        email: String,
        firstName: String,
        idCard: String,
        lastName: String,
        password: String
    ): JsonObject {
        return jsonObject(
            "email" to email,
            "firstName" to firstName,
            "idCard" to idCard,
            "lastName" to lastName,
            "password" to password
        )
    }
}