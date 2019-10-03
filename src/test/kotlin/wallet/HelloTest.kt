package wallet

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import io.javalin.Javalin
import junit.framework.Assert.assertEquals
import org.junit.jupiter.api.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class DigitalWalletApiTest {
    private lateinit var api: Javalin

    @BeforeAll
    fun setUp() {
        api = DigitwalletApi(8000).init()
        FuelManager.instance.basePath = "http://localhost:${api.port()}/"
    }
    @AfterAll
    fun tearDown(){
        api.stop()
    }

    @Test
    @Order(1)
    fun unPostNoPuedeTenerElEmailOPasswordSinCompletar(){
        val json = """{"email":"", "password":"password"}""".trimMargin()
        val(_, response, _) = Fuel.post("login").body(json).response()

        assertEquals(400, response.statusCode)
        assertEquals("Bad Request", response.responseMessage)

    }

    @Test
    @Order(2)
    fun unPostConEmailYPasswordNoRegistradosNoIngresaALaAplicacion(){
        val json = """{"email":"email", "password":"password"}""".trimMargin()
        val(_, response, _) = Fuel.post("login").body(json).response()

        assertEquals(401, response.statusCode)
        assertEquals("Unauthorized", response.responseMessage)

    }

    @Test
    @Order(3)
    fun postExitoso(){
        val json = """{"email":"a@gmail.com", "password":"a"}""".trimMargin()
        val(_, response, _) = Fuel.post("login").body(json).response()

        assertEquals(200, response.statusCode)
        assertEquals("OK", response.responseMessage)
    }

    @Test
    @Order(4)
    fun siNoEstanCompletosTodosLosCamposDelRegisterNoSeComletaElRegister(){
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
    fun siEstanTodosLosCamposSeRealizaElRegiser(){
      val json = """{
                "email":"polo@gmail.com",
                "firstName":"facundo",
                "idCard":"12345667", 
                "lastName":"Polo", 
                "password":"polo"
	
            } """.trimMargin()
        val(_, response, result) = Fuel.post("register").body(json).response()
        assertEquals(200, response.statusCode)
        assertEquals("OK", response.responseMessage)

    }
    @Test
    @Order(6)
    fun unaTransferenciaNoPuedeSerRealizadaDesdeUnCVUQueNoPertenezcaAUnUsuario(){
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
    fun unCVUDestinatarioTambienTieneQuePertenecerAUnUsuarioDelSistema(){
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
    fun transferExitoso(){
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
    fun cashIn(){
        //algo de cashIn
    }
    @Test
    @Order(11)
    fun transactions() {
    }

    @Test
    @Order(12)
    fun delete() {
    }

    @Test
    @Order(13)
    fun sePideElBalanceDeUnaCuentaPorSuCVU() {
        val (_, response, _) = Fuel.get("account/060065243").response()
        assertEquals("amount: 0.0", String(response.data).toString())
    }
}
