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
    fun se_retorna_400_si_se_intenta_realizar_un_cash_in_con_campos_vacios(){
        val json = """{
            "fromCVU": "" ,
            "amount" : 500.25,
            "cardNumber":"1234 1234 1234 1234",
            "fullName":"Facundo ",
            "endDate":"07/2019",
            "securityCode": "123"
                } """.trimMargin()
        val(_, response, _) = Fuel.post("cashin").body(json).response()

        assertEquals(400, response.statusCode)
        assertEquals("Bad Request", String(response.data))
    }

    @Test
    @Order(7)
    fun se_retorna_400_si_se_intenta_realizar_un_cash_in_a_un_cvu_que_no_existe(){
        val json = """{
            "fromCVU": "100000000",
            "amount" : 500.25,
            "cardNumber":"1234 1234 1234 1234",
            "fullName":"Facundo ",
            "endDate":"07/2019",
            "securityCode": "123"
                } """.trimMargin()
        val(_, response, _) = Fuel.post("cashin").body(json).response()

        assertEquals(400, response.statusCode)
        assertEquals("Cash In fallido. Chequee que el CVU sea correcto", String(response.data))
    }

    @Test
    @Order(8)
    fun se_retorna_400_si_se_intenta_realizar_un_cash_in_con_un_monto_igual_a_0(){
        val json = """{
            "fromCVU": "100000000",
            "amount" : 0,
            "cardNumber":"1234 1234 1234 1234",
            "fullName":"Facundo ",
            "endDate":"07/2019",
            "securityCode": "123"
                } """.trimMargin()
        val(_, response, _) = Fuel.post("cashin").body(json).response()

        assertEquals(400, response.statusCode)
        assertEquals("Cash In fallido. Chequee que el monto sea mayor a 0", String(response.data))
    }

    @Test
    @Order(9)
    fun se_retorna_400_si_se_intenta_realizar_un_cash_in_con_un_monto_negativo(){
        val json = """{
            "fromCVU": "100000000",
            "amount" : -10,
            "cardNumber":"1234 1234 1234 1234",
            "fullName":"Facundo ",
            "endDate":"07/2019",
            "securityCode": "123"
                } """.trimMargin()
        val(_, response, _) = Fuel.post("cashin").body(json).response()

        assertEquals(400, response.statusCode)
        assertEquals("Cash In fallido. Chequee que el monto sea mayor a 0", String(response.data))
    }

    @Test
    @Order(10)
    fun se_retorna_200_para_un_cash_in_exitoso(){
        val json = """{
            "fromCVU": "060065243",
            "amount" : 10,
            "cardNumber":"1234 1234 1234 1234",
            "fullName":"Facundo ",
            "endDate":"07/2019",
            "securityCode": "123"
                } """.trimMargin()
        val(_, response, _) = Fuel.post("cashin").body(json).response()

        assertEquals(200, response.statusCode)
        assertEquals("Cash In exitoso", String(response.data))
    }

    @Test
    @Order(11)
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
    @Order(12)
    fun se_retorna_400_si_se_intenta_realizar_una_transferencia_con_campos_vacios(){
        val json = """{
                "fromCVU":"",
                "toCVU":"",
                "amount":"6"
            } """.trimMargin()
        val(_, response, _) = Fuel.post("transfer").body(json).response()

        assertEquals(400, response.statusCode)
        assertEquals("Bad Request", String(response.data))
    }

    @Test
    @Order(13)
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
    @Order(14)
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
    @Order(15)
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

    @Test
    @Order(16)
    fun se_retorna_200_si_la_transferencia_es_exitosa(){
        val cashinJson = """{
            "fromCVU": "060065243",
            "amount" : 10,
            "cardNumber":"1234 1234 1234 1234",
            "fullName":"Facundo ",
            "endDate":"07/2019",
            "securityCode": "123"
                } """.trimMargin()
        val(_, _, _) = Fuel.post("cashin").body(cashinJson).response()

        val transferJson = """{
                "fromCVU":"060065243",
                "toCVU":"519264035",
                "amount":"10"
            } """.trimMargin()
        val(_, response, _) = Fuel.post("transfer").body(transferJson).response()

        assertEquals(200, response.statusCode)
    }

    @Test
    @Order(17)
    fun transactions() {
        val (_, response, _) = Fuel.get("transaction/060065243").response()
        assertEquals(200, response.statusCode)
    }


    @Test
    @Order(18)
    fun delete() {
    }

    @Test
    @Order(19)
    fun sePideElBalanceDeUnaCuentaPorSuCVU() {
        val (_, response, _) = Fuel.get("account/060065243").response()
        assertEquals("amount: 0.0", String(response.data).toString())
    }

    @Test
    @Order(19)
    fun deleteExitosoAUser() {
        val (_, response, _) = Fuel.delete("users/060065243").response()
        assertEquals(200, response.statusCode)
    }
}
