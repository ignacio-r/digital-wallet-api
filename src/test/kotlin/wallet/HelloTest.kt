package wallet

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.jackson.responseObject
import io.javalin.Javalin
import io.kotlintest.specs.AbstractAnnotationSpec
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
    fun unPostoNoPuedeTenerElEmailOPasswordSinCompletar(){
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
        val(_, response, _) = Fuel.post("register").body(json).response()

        assertEquals(200, response.statusCode)
        assertEquals("OK", response.responseMessage)
    }
    @Test
    @Order(6)
    fun unaTransferenciaNoPuedeSerRealizadaDesdeUnCVUQueNoPertenezcaAUnUsuario(){
        val json = """{
                "email":"polo@gmail.com",
                "firstName":"facundo",
                "idCard":"12345667", 
                "lastName":"Polo", 
                "password":"polo"
	
            } """.trimMargin()

    }
}
