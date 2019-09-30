package wallet

import io.javalin.Javalin

fun main(args: Array<String>) {
    val app = Javalin.create().start(7000)
    app.get("/") { ctx -> ctx.result("Hello World") }
}
// You can wrap the main function
// in a Kotlin object

