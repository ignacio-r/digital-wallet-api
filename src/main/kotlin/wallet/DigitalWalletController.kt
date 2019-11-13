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
            it.enableCorsForAllOrigins()
        }
            .exception(Exception::class.java) { e, ctx ->
                e.printStackTrace()
                ctx.status(500)
                ctx.json("{\"message\": \"Error fatal\"}")
            }
            .start(port)

        val service = DigitalWalletService()

        app.post("login") { ctx ->
            val loginWrapper: LoginWrapper = ctx.bodyValidator<LoginWrapper>()
                .check({ it.email.trim().length > 0 }, "Email Invalido")
                .check({ it.password.trim().length > 0 }, "Contraseña invalida")
                .get()
            try {
                val login = service.login(loginWrapper)
                ctx.status(200)
                ctx.json("{\"message\": \"Login exitoso para cuenta ${login.account!!.cvu}\", \"cvu\": \"${login.account!!.cvu}\"} ")
            } catch (e: LoginException) {
                ctx.status(401)
                ctx.json("{\"message\": \"Usuario o contraseña incorrectos\"}")
            }
        }

        app.post("register") { ctx ->
            val registerWrapper: RegisterWrapper
            registerWrapper = ctx.bodyValidator<RegisterWrapper>()
                .check({ it.email.contains("@") && it.email.contains(".com") }, "Email invalido")
                .check({ it.password.trim().length > 0 }, "Contraseña corta")
                .check({ it.idCard.trim().length > 0 }, "ID Card invalido")
                .check({ it.firstName.trim().length > 0 }, "Primer nombre invalido")
                .check({ it.lastName.trim().length > 0 }, "Apellido invalido")
                .get()
            try {
                service.register(registerWrapper)
                ctx.status(200)
                ctx.json("{\"message\": \"Registro exitoso\"}")
            } catch (e: IllegalArgumentException) {
                ctx.status(500)
                ctx.json("{\"message\": ${e.message!!}}")
            }
        }

        app.post("transfer") { ctx ->
            val transferWrapper: TransferWrapper = ctx.bodyValidator<TransferWrapper>()
                .check({ checkAllParams(it) })
                .get()
            if (transferWrapper.amount.toInt() <= 0) {
                ctx.status(400)
                ctx.json("{\"message\": \"Las transferencias tienen que tener un monto mayor a cero\"}")
                return@post
            }
            try {
                service.transfer(transferWrapper)
                ctx.status(200)
                ctx.json("{\"message\": \"Success\", \"cvu\": ${transferWrapper.fromCVU}}")
            } catch (e: NoSuchElementException) {
                ctx.status(400)
                ctx.json("{\"message\": \"Transferencia fallida, chequear que el CVU destinatario o emisor sean correctos\"}")
            } catch (e: IllegalArgumentException) {
                ctx.status(400)
                ctx.json("{\"message\": ${e.message!!}}")
            } catch (e: BlockedAccountException) {
                ctx.status(400)
                ctx.json("{\"message\": ${e.message!!}}")
            } catch (e: NoMoneyException) {
                ctx.status(400)
                ctx.json("{\"message\": ${e.message!!}}")
            }
        }

        app.post("cashin") { ctx ->
            val cashInWrapper: CashInWrapper = ctx.bodyValidator<CashInWrapper>()
                .check({ checkAllParams(it) })
                .get()
            if (cashInWrapper.amount.toDouble() <= 0) {
                ctx.status(400)
                ctx.json("{\"message\": \"Cash In fallido. Chequee que el monto sea mayor a 0\"}")
                return@post
            }
            try {
                service.cashin(cashInWrapper)
                ctx.status(200)
                ctx.json("{\"message\": \"Cash In exitoso\"}")
            } catch (e: NoSuchElementException) {
                ctx.status(400)
                ctx.json("{\"message\": \"Cash In fallido. CVU incorrecto\"}")
            } catch (e: BlockedAccountException) {
                ctx.status(400)
                ctx.json("{\"message\": ${e.message!!}}")
            }
        }

        app.get("/transactions/:cvu") { ctx ->
            val cvu = ctx.pathParam("cvu")
            try {
                val movimientos = service.getMovimientos(cvu)
                ctx.status(200)
                ctx.json("{\"message\": \"Success\", \"movimientos\": ${movimientos}}")

            } catch (error: NoSuchElementException) {
                ctx.status(404)
                ctx.json("{\"message\": \"La cuenta con CVU ${cvu} no existe\"}")
            }
        }

        app.delete("/users/:cvu") { ctx ->
            val cvu = ctx.pathParam("cvu")
            try{
                service.borrarUsuarioPorCVU(cvu)
                ctx.status(200)
                ctx.json("{\"message\": \"Borrado exitoso\"}")
            } catch (error: IllegalArgumentException) {
                ctx.status(404)
                ctx.json("{\"message\": \"No puede eliminar cuenta $cvu con fondos\"}")
            } catch (error: NoSuchElementException) {
                ctx.status(404)
                ctx.json("{\"message\": \"La cuenta con CVU ${cvu} no existe\"}")
            }
        }

        app.get("/account/:cvu") { ctx ->
            val cvu = ctx.pathParam("cvu")
            try {
                val balanceRecuperado = service.balancePorCVU(cvu)
                ctx.status(200)
                ctx.json("{\"message\": \"Success\", \"balance\": ${balanceRecuperado!!}}")
            } catch (error: NoSuchElementException){
                ctx.status(404)
                ctx.json("{\"message\": \"La cuenta con CVU ${cvu} no existe\"}")
            }
        }
        return app
    }
}


