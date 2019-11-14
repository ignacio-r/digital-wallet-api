package wallet

import com.github.salomonbrys.kotson.jsonObject
import io.javalin.Javalin
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
                ctx.json(MessageObject("Error fatal"))
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
                ctx.json(MessageObjectWithCVU("Login exitoso para cuenta ${login.account!!.cvu}", "${login.account!!.cvu}"))
            } catch (e: LoginException) {
                ctx.status(401)
                ctx.json(MessageObject("Usuario o contraseña incorrectos"))
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
                ctx.json(MessageObject("Registro exitoso"))
            } catch (e: IllegalArgumentException) {
                ctx.status(500)
                ctx.json(MessageObject(e.message!!))
            }
        }

        app.post("transfer") { ctx ->
            val transferWrapper: TransferWrapper = ctx.bodyValidator<TransferWrapper>()
                .check({ checkAllParams(it) })
                .get()
            if (transferWrapper.amount.toInt() <= 0) {
                ctx.status(400)
                ctx.json(MessageObject("Las transferencias tienen que tener un monto mayor a cero"))
                return@post
            }
            try {
                service.transfer(transferWrapper)
                ctx.status(200)
                ctx.json(MessageObjectWithCVU("Success",  "${transferWrapper.fromCVU}"))
            } catch (e: NoSuchElementException) {
                ctx.status(400)
                ctx.json(MessageObject("Transferencia fallida, chequear que el CVU destinatario o emisor sean correctos"))
            } catch (e: IllegalArgumentException) {
                ctx.status(400)
                ctx.json(MessageObject(e.message!!))
            } catch (e: BlockedAccountException) {
                ctx.status(400)
                ctx.json(MessageObject(e.message!!))
            } catch (e: NoMoneyException) {
                ctx.status(400)
                ctx.json(MessageObject(e.message!!))
            }
        }

        app.post("cashin") { ctx ->
            val cashInWrapper: CashInWrapper = ctx.bodyValidator<CashInWrapper>()
                .check({ checkAllParams(it) })
                .get()
            if (cashInWrapper.amount.toDouble() <= 0) {
                ctx.status(400)
                ctx.json(MessageObject("Cash In fallido. Chequee que el monto sea mayor a 0"))
                return@post
            }
            try {
                service.cashin(cashInWrapper)
                ctx.status(200)
                ctx.json(MessageObject("Cash In exitoso"))
            } catch (e: NoSuchElementException) {
                ctx.status(400)
                ctx.json(MessageObject("Cash In fallido. CVU incorrecto"))
            } catch (e: BlockedAccountException) {
                ctx.status(400)
                ctx.json(MessageObject(e.message!!))
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
                ctx.json(MessageObject("La cuenta con CVU ${cvu} no existe"))
            }
        }

        app.delete("/users/:cvu") { ctx ->
            val cvu = ctx.pathParam("cvu")
            try{
                service.borrarUsuarioPorCVU(cvu)
                ctx.status(200)
                ctx.json(MessageObject("Borrado exitoso"))
            } catch (error: IllegalArgumentException) {
                ctx.status(404)
                ctx.json(MessageObject("No puede eliminar cuenta $cvu con fondos"))
            } catch (error: NoSuchElementException) {
                ctx.status(404)
                ctx.json(MessageObject("La cuenta con CVU ${cvu} no existe"))
            }
        }

        app.get("/account/:cvu") { ctx ->
            val cvu = ctx.pathParam("cvu")
            try {
                val balanceRecuperado = service.balancePorCVU(cvu)
                ctx.status(200)
                ctx.json(MessageObject(" ${balanceRecuperado!!}"))
            } catch (error: NoSuchElementException){
                ctx.status(404)
                ctx.json(MessageObject("La cuenta con CVU ${cvu} no existe"))
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


