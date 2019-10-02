package wallet

import io.javalin.Javalin
import io.javalin.http.BadRequestResponse
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties


fun main() {
    DigitwalletApi(7000).init()

}

