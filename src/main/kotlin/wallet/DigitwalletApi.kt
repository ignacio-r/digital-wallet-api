package wallet

import io.javalin.Javalin
import io.javalin.http.BadRequestResponse
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

class DigitwalletApi(private val port: Int) {

    fun checkAllParams(asd: Any): Boolean {
        val props = asd::class.memberProperties as List<KProperty1<Any, *>>
        return props.all { prop -> prop.get(asd).toString().trim() != "" }
    }
    fun init(): Javalin{
        val app = Javalin.create{

        }.exception(Exception::class.java) { e, ctx ->
            e.printStackTrace()
            ctx.status(500)
            ctx.json("Error fatal")}
            .start(port)

        val service = DigitalWalletService()

        app.exception(BadRequestResponse::class.java) { e, ctx ->
            ctx.status(400)
            ctx.result("Bad Request")
        }

        app.post("login") { ctx ->
            //Falta encriptar la password y el return esta medio falopa
            val loginWrapper: LoginWrapper = ctx.bodyValidator<LoginWrapper>()
                .check({ checkAllParams(it)})
                .get()
            val userWrapper = service.login(loginWrapper)
            if (userWrapper != null) {
                ctx.status(200)
                ctx.result("Exito!")
            } else {
                ctx.status(401)
                ctx.result("Usuario o contraseña incorrectos")
            }
        }

        app.post("register") { ctx ->
            var registerWrapper: RegisterWrapper = ctx.body<RegisterWrapper>()
            registerWrapper = ctx.bodyValidator<RegisterWrapper>()
                .check({ checkAllParams(it) })
                .get()
            val nuevoUsuario = service.register(registerWrapper)
            if (nuevoUsuario != null) {
                ctx.status(200)
                ctx.result("Registro exitoso")
            } else {
                ctx.status(500)
                ctx.result("Error de registro. Por favor intente otra vez.")
            }
        }

        app.post("transfer") { ctx ->
            val transferWrapper: TransferWrapper = ctx.body<TransferWrapper>()
            val nuevaTransferencia = service.transfer(transferWrapper)
            if(transferWrapper.amount.toInt() === 0){
                ctx.status(400)
                ctx.result("Las transferencias tienen que tener un monto mayor a cero")
            }
            else if (nuevaTransferencia) {
                ctx.status(200)
                ctx.json(transferWrapper.fromCVU)
            } else {
                ctx.status(400)
                ctx.result("Transferencia fallida, chequear que el CVU destinatario o emisor sean correctos")
            }
        }

        app.post("/cashin") { ctx ->
            val cashInWrapper: CashInWrapper = ctx.body<CashInWrapper>()
            val cashIn = service.cashin(cashInWrapper)

        }

        app.get("/transaccions/:cvu") { ctx ->
            val cvu = ctx.pathParam("cvu")
            val movientos = service.getMovimientos(cvu)
            if (movientos != null) {
                ctx.status(200)
                ctx.json(movientos)
            } else {
                ctx.status(404)
                ctx.json("CVU incorrecto")
            }
        }

        app.delete("/users/:cvu") { ctx ->
            val cvu = ctx.pathParam("cvu")
            val usuarioEliminadoConExito = service.borrarUsuarioPorCVU(cvu)
            if (usuarioEliminadoConExito) {
                ctx.status(200)
                ctx.json("xd")
            } else {
                ctx.status(404)
                ctx.json("CVU incorrecto")
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
                ctx.json("CVU incorrecto")
            }
        }
        return app
    }
}


