package wallet

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import io.javalin.Javalin
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class DigitalWalletApiTest {
    private lateinit var api: Javalin

    @BeforeAll
    fun setUp() {
        api = DigitalWalletController(8000).init()
        FuelManager.instance.basePath = "http://localhost:${api.port()}/"
    }
    @AfterAll
    fun tearDown(){
        api.stop()
    }

    @Test
    @Order(1)
    fun se_retorna_400_al_intentar_loguearse_con_datos_vacios(){
        val json = """{"email":"", "password":"password"}""".trimMargin()
        val(_, response, _) = Fuel.post("login").body(json).response()

        assertEquals(400, response.statusCode)
        assertEquals("Bad Request", response.responseMessage)

    }

    @Test
    @Order(2)
    fun se_retorna_400_al_intentar_loguearse_con_datos_erroneos(){
        val json = """{"email":"email", "password":"password"}""".trimMargin()
        val(_, response, _) = Fuel.post("login").body(json).response()

        assertEquals(401, response.statusCode)
        assertEquals("Unauthorized", response.responseMessage)

    }

    @Test
    @Order(3)
    fun se_retorna_200_al_loguerase_exitosamente(){
        val json = """{"email":"a@gmail.com", "password":"a"}""".trimMargin()
        val(_, response, _) = Fuel.post("login").body(json).response()

        assertEquals(200, response.statusCode)
        assertEquals("OK", response.responseMessage)
    }

    @Test
    @Order(4)
    fun se_retorna_400_al_intentar_registrarse_con_datos_incompletos(){
        val json = """{
                        "email":"",
                        "firstName":" ",
                        "idCard":"", 
                        "lastName":"", 
                        "password":"polo",
                   }""".trimMargin()
        val(_, response, _) = Fuel.post("register").body(json).response()

        assertEquals(400, response.statusCode)
        assertEquals("Bad Request", response.responseMessage)

    }

    @Test
    @Order(5)
    fun se_retorna_200_al_registrarse_exitosamente(){
      val json = """{
                "email":"polo@gmail.com",
                "firstName":"facundo",
                "idCard":"12345667", 
                "lastName":"Polo", 
                "password":"polo"
	
            } """.trimMargin()
        val(_, response, _) = Fuel.post("register").body(json).response()
        assertEquals(200, response.statusCode)
        assertEquals("OK", response.responseMessage)

    }
    @Test
    @Order(6)
    fun se_retorna_400_al_intentar_realizar_una_transferencia_desde_un_cvu_que_no_pertenece_al_usuario(){
        val json = """{
                "fromCVU":"01",
                "toCVU":"519264035",
                "amount":"6"
            } """.trimMargin()
        val(_, response, _) = Fuel.post("transfer").body(json).response()

        assertEquals(400, response.statusCode)
        assertEquals("Transferencia fallida, chequear que el CVU destinatario o emisor sean correctos", String(response.data))

    }
    @Test
    @Order(8)
    fun se_retorna_400_si_un_cvu_de_una_transferencia_no_pertenece_a_ningun_usuario_del_sistema(){
        val json = """{
                "fromCVU":"060065243",
                "toCVU":"a",
                "amount":"6"
            } """.trimMargin()
        val(_, response, _) = Fuel.post("transfer").body(json).response()

        assertEquals(400, response.statusCode)
        assertEquals("Transferencia fallida, chequear que el CVU destinatario o emisor sean correctos", String(response.data))
    }

    @Test
    @Order(9)
    fun se_retorna_400_si_el_monto_de_la_transferencia_es_0(){
        val json = """{
                "fromCVU":"060065243",
                "toCVU":"519264035",
                "amount":"0"
            } """.trimMargin()
        val(_, response, _) = Fuel.post("transfer").body(json).response()

        assertEquals(400, response.statusCode)
        assertEquals("Las transferencias tienen que tener un monto mayor a cero", String(response.data))
    }

    @Test
    @Order(10)
    fun se_retorna_400_si_el_monto_de_la_transferencia_es_menor_a_0(){
        val json = """{
                "fromCVU":"060065243",
                "toCVU":"519264035",
                "amount":"-1"
            } """.trimMargin()
        val(_, response, _) = Fuel.post("transfer").body(json).response()

        assertEquals(400, response.statusCode)
        assertEquals("Las transferencias tienen que tener un monto mayor a cero", String(response.data))
    }

//    @Test
//    @Order(10)
//    fun se_retorna_200_si_la_transferencia_es_exitosa(){
//        val json = """{
//                "fromCVU":"060065243",
//                "toCVU":"519264035",
//                "amount":"1"
//            } """.trimMargin()
//        val(_, response, _) = Fuel.post("transfer").body(json).response()
//
//        assertEquals(200, response.statusCode)
//    }
    @Test
    @Order(11)
    fun cashIn(){
        //algo de cashIn
    }
    @Test
    @Order(12)
    fun transactions() {
    }

    @Test
    @Order(13)
    fun delete() {
    }

    @Test
    @Order(14)
    fun sePideElBalanceDeUnaCuentaPorSuCVU() {
        val (_, response, _) = Fuel.get("account/060065243").response()
        assertEquals("amount: 0.0", String(response.data).toString())
    }
}
