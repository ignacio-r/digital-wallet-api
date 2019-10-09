package wallet

import io.javalin.Javalin
import io.javalin.http.BadRequestResponse
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

class DigitalWalletController(private val port: Int) {

    fun checkAllParams(asd: Any): Boolean {
        val props = asd::class.memberProperties as List<KProperty1<Any, *>>
        return props.all { prop -> prop.get(asd).toString().trim() != "" }
    }

    fun init(): Javalin {
        val app = Javalin.create {
        }
            .exception(Exception::class.java) { e, ctx ->
                e.printStackTrace()
                ctx.status(500)
                ctx.json("Error fatal")
            }
            .exception(BadRequestResponse::class.java) { _, ctx ->
                ctx.status(400)
                ctx.result("Bad Request")
            }
            .start(port)

        val service = DigitalWalletService()

        app.post("login") { ctx ->
            //Falta encriptar la password
            val loginWrapper: LoginWrapper = ctx.bodyValidator<LoginWrapper>()
                .check({ checkAllParams(it)})
                .get()
            try {
                service.login(loginWrapper)
                ctx.status(200)
                ctx.result("Exito!")
            } catch (e: LoginException) {
                ctx.status(401)
                ctx.result("Usuario o contraseña incorrectos")
            }
        }

        app.post("register") { ctx ->
            var registerWrapper: RegisterWrapper
            registerWrapper = ctx.bodyValidator<RegisterWrapper>()
                .check({ checkAllParams(it) })
                .get()
            try {
                service.register(registerWrapper)
                ctx.status(200)
                ctx.result("Registro exitoso")
            } catch (e: Error) {
                ctx.status(500)
                ctx.result("Error de registro. Por favor intente otra vez.")
            }
        }

        app.post("transfer") { ctx ->
            val transferWrapper: TransferWrapper = ctx.bodyValidator<TransferWrapper>()
                .check({ checkAllParams(it) })
                .get()
            if (transferWrapper.amount.toInt() <= 0) {
                ctx.status(400)
                ctx.result("Las transferencias tienen que tener un monto mayor a cero")
                return@post
            }
            try {
                service.transfer(transferWrapper)
                ctx.status(200)
                ctx.json(transferWrapper.fromCVU)
            } catch (e: NoSuchElementException) {
                ctx.status(400)
                ctx.result("Transferencia fallida, chequear que el CVU destinatario o emisor sean correctos")
            }
        }

        app.post("cashin") { ctx ->
            val cashInWrapper: CashInWrapper = ctx.bodyValidator<CashInWrapper>()
                .check({ checkAllParams(it) })
                .get()
            if (cashInWrapper.amount.toDouble() <= 0) {
                ctx.status(400)
                ctx.result("Cash In fallido. Chequee que el monto sea mayor a 0")
                return@post
            }
            try {
                service.cashin(cashInWrapper)
                ctx.status(200)
                ctx.result("Cash In exitoso")
            } catch (e: NoSuchElementException) {
                ctx.status(400)
                ctx.result("Cash In fallido. Chequee que el CVU sea correcto")
            }
        }

        app.get("/transactions/:cvu") { ctx ->
            val cvu = ctx.pathParam("cvu")
            try {
                val movimientos = service.getMovimientos(cvu)
                ctx.status(200)
                ctx.json(movimientos)

            } catch (error: Error) {
                ctx.status(404)
                ctx.result("CVU incorrecto")
            }
        }

        app.delete("/users/:cvu") { ctx ->
            val cvu = ctx.pathParam("cvu")

            try{
                service.borrarUsuarioPorCVU(cvu)
                ctx.status(200)
                ctx.json("Borrado exitoso")
            } catch (error: Exception) {
                ctx.status(404)
                ctx.result("CVU incorrecto o con saldo mayor a cero")
            }
        }

        app.get("/account/:cvu") { ctx ->
            val cvu = ctx.pathParam("cvu")
            val balanceRecuperado = service.balancePorCVU(cvu)
            if (balanceRecuperado != null) {
                ctx.status(200)
                ctx.result("amount: ${balanceRecuperado}")
            } else {
                ctx.status(404)
                ctx.result("CVU incorrecto")
            }
        }
        return app
    }
}


