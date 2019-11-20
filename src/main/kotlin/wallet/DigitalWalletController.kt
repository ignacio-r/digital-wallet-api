package wallet

import com.github.salomonbrys.kotson.toMap
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.javalin.Javalin
import java.util.*


class DigitalWalletController(private val port: Int) {

    private fun checkParamIsEmpty(any: String): Boolean {
        return (JsonParser().parse(any) as JsonObject)
            .toMap().values.stream().anyMatch { e -> e.asString == "" }
    }

    fun init(): Javalin {
        val app = Javalin.create {
            it.enableCorsForAllOrigins()
        }
            .exception(Exception::class.java) { e, ctx ->
                e.printStackTrace()
                ctx.status(500)
                ctx.json(APIResponse("Error fatal"))
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
                ctx.json(
                    APIResponseWithCVU(
                        "Login exitoso para cuenta ${login.account!!.cvu}",
                        "${login.account!!.cvu}"
                    )
                )
            } catch (e: LoginException) {
                ctx.status(401)
                ctx.json(APIResponse("Usuario o contraseña incorrectos"))
            }
        }

        app.post("register") { ctx ->
            val registerWrapper: RegisterWrapper = ctx.bodyValidator<RegisterWrapper>()
                .check({ it.email.contains("@") && it.email.contains(".com") }, "Email invalido")
                .check({ it.password.trim().isNotEmpty() }, "Contraseña corta")
                .check({ it.idCard.trim().isNotEmpty() }, "ID Card invalido")
                .check({ it.firstName.trim().isNotEmpty() }, "Primer nombre invalido")
                .check({ it.lastName.trim().isNotEmpty() }, "Apellido invalido")
                .get()
            try {
                service.register(registerWrapper)
                ctx.status(200)
                ctx.json(APIResponse("Registro exitoso"))
            } catch (e: IllegalArgumentException) {
                ctx.status(500)
                ctx.json(APIResponse(e.message!!))
            }
        }

        app.post("transfer") { ctx ->
            if (checkParamIsEmpty(ctx.body())) {
                ctx.status(400)
                ctx.json(APIResponse("Cash out fallido. Campos vacios"))
                return@post
            }
            val transferWrapper: TransferWrapper = ctx.bodyValidator<TransferWrapper>().get()
            if (transferWrapper.amount.toInt() <= 0) {
                ctx.status(400)
                ctx.json(APIResponse("Las transferencias tienen que tener un monto mayor a cero"))
                return@post
            }
            try {
                service.transfer(transferWrapper)
                ctx.status(200)
                ctx.json(APIResponseWithCVU("Success", "${transferWrapper.fromCVU}"))
            } catch (e: NoSuchElementException) {
                ctx.status(400)
                ctx.json(APIResponse("Transferencia fallida, chequear que el CVU destinatario o emisor sean correctos"))
            } catch (e: IllegalArgumentException) {
                ctx.status(400)
                ctx.json(APIResponse(e.message!!))
            } catch (e: BlockedAccountException) {
                ctx.status(400)
                ctx.json(APIResponse(e.message!!))
            } catch (e: NoMoneyException) {
                ctx.status(400)
                ctx.json(APIResponse(e.message!!))
            }
        }

        app.post("cashin") { ctx ->
            if (checkParamIsEmpty(ctx.body())) {
                ctx.status(400)
                ctx.json(APIResponse("Cash In fallido. Campos vacios"))
                return@post
            }
            val cashInWrapper: CashInWrapper = ctx.bodyValidator<CashInWrapper>().get()
            if (cashInWrapper.amount.toDouble() <= 0) {
                ctx.status(400)
                ctx.json(APIResponse("Cash In fallido. Chequee que el monto sea mayor a 0"))
                return@post
            }
            try {
                service.cashin(cashInWrapper)
                ctx.status(200)
                ctx.json(APIResponse("Cash In exitoso"))
            } catch (e: NoSuchElementException) {
                ctx.status(400)
                ctx.json(APIResponse("Cash In fallido. CVU incorrecto"))
            } catch (e: BlockedAccountException) {
                ctx.status(400)
                ctx.json(APIResponse(e.message!!))
            }
        }

        app.get("/transactions/:cvu") { ctx ->
            val cvu = ctx.pathParam("cvu")
            try {
                val movimientos = service.getMovimientos(cvu)
                ctx.status(200)
                ctx.json(movimientos.toMutableList())
            } catch (error: NoSuchElementException) {
                ctx.status(404)
                ctx.json(APIResponse("La cuenta con CVU ${cvu} no existe"))
            }
        }

        app.delete("/users/:cvu") { ctx ->
            val cvu = ctx.pathParam("cvu")
            try {
                service.borrarUsuarioPorCVU(cvu)
                ctx.status(200)
                ctx.json(APIResponse("Borrado exitoso"))
            } catch (error: IllegalArgumentException) {
                ctx.status(404)
                ctx.json(APIResponse("No puede eliminar cuenta $cvu con fondos"))
            } catch (error: NoSuchElementException) {
                ctx.status(404)
                ctx.json(APIResponse("La cuenta con CVU ${cvu} no existe"))
            }
        }

        app.get("/account/:cvu") { ctx ->
            val cvu = ctx.pathParam("cvu")
            try {
                val balanceRecuperado = service.balancePorCVU(cvu)
                ctx.status(200)
                ctx.json(Balance(balanceRecuperado!!))
            } catch (error: NoSuchElementException) {
                ctx.status(404)
                ctx.json(APIResponse("La cuenta con CVU ${cvu} no existe"))
            }
        }

        app.put("/users/firstname") { ctx ->
            try {
                val json = ctx.body<Map<String, String>>()
                val cvu = json["cvu"]
                val firstname = json["firstname"]
                service.modificarNombreDeUsuario(cvu, firstname)
                ctx.status(200)
                ctx.json(APIResponse(message = "Modificacion exitosa"))
            } catch (error: NoSuchElementException) {
                ctx.status(404)
                ctx.json(APIResponse(message = "No se pudo modificar el nombre"))
            } catch (e: EmptyValueException) {
                ctx.status(404)
                ctx.json(APIResponse(e.message))
            }

        }

        app.put("/users/lastname") { ctx ->
            try {
                val json = ctx.body<Map<String, String>>()
                val cvu = json["cvu"]
                val lastname = json["lastname"]
                service.modificarApellidoDeUsuario(cvu, lastname)
                ctx.status(200)
                ctx.json(APIResponse(message = "Modificacion exitosa"))
            } catch (error: NoSuchElementException) {
                ctx.status(404)
                ctx.json(APIResponse(message = "No se pudo modificar el nombre"))
            } catch (e: EmptyValueException) {
                ctx.status(404)
                ctx.json(APIResponse(e.message))
            }
        }

        app.get("/users/:cvu") { ctx ->
            val cvu = ctx.pathParam("cvu")
            try {
                val user = service.digitalWallet.accountByCVU(cvu).user
                ctx.status(200)
                ctx.json(
                    UserWrapper(
                        idCard = user.idCard,
                        email = user.email,
                        firstName = user.firstName,
                        lastName = user.lastName,
                        isAdmin = user.isAdmin
                    )
                )
            } catch (error: NoSuchElementException) {
                ctx.status(404)
                ctx.json(APIResponse(message = "CVU incorrecto"))
            }
        }


        return app
    }

}


