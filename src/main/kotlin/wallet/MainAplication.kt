package wallet

import io.javalin.Javalin

fun main() {
    val app = Javalin.create().start(7000)
    val controler = Controler()

    app.post("api/login") { ctx ->
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
        app.get("/") { ctx -> ctx.result("Hello World")
        }

}
