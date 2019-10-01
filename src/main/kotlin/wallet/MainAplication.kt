package wallet

import io.javalin.Javalin
import io.javalin.http.BadRequestResponse

fun main() {
    val app = Javalin.create().start(7000)
    val service = DigitalWalletService()

    app.exception(BadRequestResponse::class.java) { e, ctx ->
        ctx.status(400)
        ctx.result("Bad Request")
    }

    app.post("login") { ctx ->
        //Falta encriptar la password y el return esta medio falopa
        val loginWrapper: LoginWrapper = ctx.body<LoginWrapper>()
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
            .check({
                it.email.trim() != ""
                        && it.firstName.trim() != ""
                        && it.lastName.trim() != ""
                        && it.password.trim() != ""
                        && it.idCard.trim() != ""
            })
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
        if (nuevaTransferencia) {
            ctx.status(200)
            ctx.json(transferWrapper.fromCVU)
        } else {
            ctx.status(400)
            ctx.json("Transferencia fallida")
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
            ctx.json(balanceRecuperado)
        } else {
            ctx.status(404)
            ctx.json("CVU incorrecto")
        }
    }
}
