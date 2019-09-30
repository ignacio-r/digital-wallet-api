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
        app.get("/") { ctx -> ctx.result("Hello World")
        }

}
