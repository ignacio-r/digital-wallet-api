package wallet

import io.javalin.Javalin

fun main() {
    val app = Javalin.create().start(7000)
    val controler = Controler()

    app.post("api/login") { ctx ->
        //Falta encriptar la password y el return esta medio falopa
        val loginWrapper: LoginWrapper = ctx.body<LoginWrapper>()
        val userWrapper = controler.login(loginWrapper)
         if (userWrapper != null) {
            ctx.status(200)
            ctx.json(userWrapper.email)
         } else {
            ctx.status(404)
            ctx.json("usuario/password incorrectos")
       }
    }
    app.post("/register"){ctx ->
        val registerWrapper: RegisterWrapper = ctx.body<RegisterWrapper>()
        val nuevoUsuario = controler.register(registerWrapper)
        if(nuevoUsuario != null){
            ctx.status(200)
            ctx.json(nuevoUsuario.fullName())
            // ctx.json(nuevoUsuario)
        } else {
            ctx.status(400)
            ctx.json("No deje campos en blanco al momento de agregar un usuario")
        }

    }
    app.post("/transfer"){ctx ->
        val transferWrapper: TransferWrapper = ctx.body<TransferWrapper>()
        val nuevaTransferencia = controler.transfer(transferWrapper)
        if(nuevaTransferencia){
            ctx.status(200)
            ctx.json(transferWrapper.fromCVU)
        }else{
            ctx.status(400)
            ctx.json("Transferencia fallida")
        }
    }
    app.post("/cashin"){ctx->
        val cashInWrapper: CashInWrapper = ctx.body<CashInWrapper>()
        val cashIn = controler.cashin(cashInWrapper)

    }

    app.get("/transaccions/:cvu"){ctx ->
        val cvu = ctx.pathParam("cvu")
        val movientos = controler.getMovimientos(cvu)
        if(movientos != null){
            ctx.status(200)
            ctx.json(movientos)
        }else{
            ctx.status(404)
            ctx.json("CVU incorrecto")
        }
    }

    app.delete("/users/:cvu"){ctx ->
        val cvu = ctx.pathParam("cvu")
        val usuarioEliminadoConExito = controler.borrarUsuarioPorCVU(cvu)
        if(usuarioEliminadoConExito){
            ctx.status(200)
            ctx.json("xd")
        }else{
            ctx.status(404)
            ctx.json("CVU incorrecto")
        }
    }

    app.get("/account/:cvu"){ctx ->
        val cvu = ctx.pathParam("cvu")
        val balanceRecuperado = controler.balancePorCVU(cvu)
        if(balanceRecuperado != null){
            ctx.status(200)
            ctx.json(balanceRecuperado)
        }else{
            ctx.status(404)
            ctx.json("CVU incorrecto")
        }
    }
        app.get("/") { ctx -> ctx.result("Hello World")
        }

}
