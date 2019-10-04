package wallet

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.google.gson.JsonObject
import io.javalin.Javalin
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class DigitalWalletApiTest {
    private lateinit var api: Javalin
    private lateinit var jsonFactory: JsonFactory

    @BeforeAll
    fun setUp() {
        api = DigitalWalletController(7000).init()
        FuelManager.instance.basePath = "http://localhost:${api.port()}/"
        jsonFactory = JsonFactory()
    }

    @AfterAll
    fun tearDown() {
        api.stop()
    }

    @Test
    @Order(1)
    fun se_retorna_400_al_intentar_loguearse_con_datos_vacios() {
        val json_obj: JsonObject = jsonFactory.emailPasswordJson("", "password")

        val (_, response, _) = Fuel.post("login").body(json_obj.toString()).response()

        assertEquals(400, response.statusCode)
        assertEquals("Bad Request", response.responseMessage)
    }

    @Test
    @Order(2)
    fun se_retorna_400_al_intentar_loguearse_con_datos_erroneos() {
        val json_obj: JsonObject = jsonFactory.emailPasswordJson("email", "password")
        val (_, response, _) = Fuel.post("login").body(json_obj.toString()).response()

        assertEquals(401, response.statusCode)
        assertEquals("Unauthorized", response.responseMessage)
    }

    @Test
    @Order(3)
    fun se_retorna_200_al_loguerase_exitosamente() {
        val json_obj: JsonObject = jsonFactory.emailPasswordJson("a@gmail.com", "a")
        val (_, response, _) = Fuel.post("login").body(json_obj.toString()).response()

        assertEquals(200, response.statusCode)
        assertEquals("OK", response.responseMessage)
    }

    @Test
    @Order(4)
    fun se_retorna_400_al_intentar_registrarse_con_datos_incompletos() {

        val email = ""
        val firstName = " "
        val idCard = ""
        val lastName = ""
        val password = "polo"
        val json_obj: JsonObject = jsonFactory.registerUserJson(email, firstName, idCard, lastName, password)

        val (_, response, _) = Fuel.post("register").body(json_obj.toString()).response()

        assertEquals(400, response.statusCode)
        assertEquals("Bad Request", response.responseMessage)
    }

    @Test
    @Order(5)
    fun se_retorna_200_al_registrarse_exitosamente() {
        val json_obj: JsonObject = jsonFactory.registerUserJson("polo@gmail.com", "facundo", "12345667", "Polo", "polo")

        val (_, response, _) = Fuel.post("register").body(json_obj.toString()).response()
        assertEquals(200, response.statusCode)
        assertEquals("OK", response.responseMessage)
    }

    @Test
    @Order(6)
    fun se_retorna_400_si_se_intenta_realizar_un_cash_in_con_campos_vacios() {
        val json_obj: JsonObject =
            jsonFactory.cashInJson("", "500.25", "1234 1234 1234 1234", "Facundo ", "07/2019", "123")

        val (_, response, _) = Fuel.post("cashin").body(json_obj.toString()).response()

        assertEquals(400, response.statusCode)
        assertEquals("Bad Request", String(response.data))
    }

    @Test
    @Order(7)
    fun se_retorna_400_si_se_intenta_realizar_un_cash_in_a_un_cvu_que_no_existe() {
        val json_obj: JsonObject = jsonFactory.cashInJson(
            "100000000", "500.25", "1234 1234 1234 1234", "Facundo ",
            "07/2019", "123"
        )

        val (_, response, _) = Fuel.post("cashin").body(json_obj.toString()).response()

        assertEquals(400, response.statusCode)
        assertEquals("Cash In fallido. Chequee que el CVU sea correcto", String(response.data))
    }

    @Test
    @Order(8)
    fun se_retorna_400_si_se_intenta_realizar_un_cash_in_con_un_monto_igual_a_0() {
        val json_obj: JsonObject = jsonFactory.cashInJson(
            "100000000", "0", "1234 1234 1234 1234",
            "Facundo ", "07/2019", "123"
        )

        val (_, response, _) = Fuel.post("cashin").body(json_obj.toString()).response()

        assertEquals(400, response.statusCode)
        assertEquals("Cash In fallido. Chequee que el monto sea mayor a 0", String(response.data))
    }

    @Test
    @Order(9)
    fun se_retorna_400_si_se_intenta_realizar_un_cash_in_con_un_monto_negativo() {
        val json_obj: JsonObject = jsonFactory.cashInJson(
            "100000000", "-10", "1234 1234 1234 1234",
            "Facundo ", "07/2019", "123"
        )

        val (_, response, _) = Fuel.post("cashin").body(json_obj.toString()).response()

        assertEquals(400, response.statusCode)
        assertEquals("Cash In fallido. Chequee que el monto sea mayor a 0", String(response.data))
    }

    @Test
    @Order(10)
    fun se_retorna_200_para_un_cash_in_exitoso() {
        val json_obj: JsonObject = jsonFactory.cashInJson(
            "060065243", "10", "1234 1234 1234 1234",
            "Facundo ", "07/2019", "123"
        )

        val (_, response, _) = Fuel.post("cashin").body(json_obj.toString()).response()

        assertEquals(200, response.statusCode)
        assertEquals("Cash In exitoso", String(response.data))
    }

    @Test
    @Order(11)
    fun se_retorna_400_al_intentar_realizar_una_transferencia_desde_un_cvu_que_no_pertenece_al_usuario() {
        val json_obj: JsonObject = jsonFactory.transferJson("01", "519264035", "6")

        val (_, response, _) = Fuel.post("transfer").body(json_obj.toString()).response()

        assertEquals(400, response.statusCode)
        assertEquals(
            "Transferencia fallida, chequear que el CVU destinatario o emisor sean correctos",
            String(response.data)
        )
    }

    @Test
    @Order(12)
    fun se_retorna_400_si_se_intenta_realizar_una_transferencia_con_campos_vacios() {
        val json_obj: JsonObject = jsonFactory.transferJson(
            "",
            "",
            "6"
        )

        val (_, response, _) = Fuel.post("transfer").body(json_obj.toString()).response()

        assertEquals(400, response.statusCode)
        assertEquals("Bad Request", String(response.data))
    }

    @Test
    @Order(13)
    fun se_retorna_400_si_un_cvu_de_una_transferencia_no_pertenece_a_ningun_usuario_del_sistema() {
        val json_obj: JsonObject = jsonFactory.transferJson(
            "060065243",
            "a",
            "6"
        )

        val (_, response, _) = Fuel.post("transfer").body(json_obj.toString()).response()

        assertEquals(400, response.statusCode)
        assertEquals(
            "Transferencia fallida, chequear que el CVU destinatario o emisor sean correctos",
            String(response.data)
        )
    }

    @Test
    @Order(14)
    fun se_retorna_400_si_el_monto_de_la_transferencia_es_0() {
        val json_obj: JsonObject = jsonFactory.transferJson(
            "060065243",
            "519264035",
            "0"
        )

        val (_, response, _) = Fuel.post("transfer").body(json_obj.toString()).response()

        assertEquals(400, response.statusCode)
        assertEquals("Las transferencias tienen que tener un monto mayor a cero", String(response.data))
    }

    @Test
    @Order(15)
    fun se_retorna_400_si_el_monto_de_la_transferencia_es_menor_a_0() {
        val json_obj: JsonObject = jsonFactory.transferJson(
            "060065243",
            "519264035",
            "-1"
        )

        val (_, response, _) = Fuel.post("transfer").body(json_obj.toString()).response()

        assertEquals(400, response.statusCode)
        assertEquals("Las transferencias tienen que tener un monto mayor a cero", String(response.data))
    }

    @Test
    @Order(16)
    fun se_retorna_200_si_la_transferencia_es_exitosa() {
        val cashin_json_obj: JsonObject = jsonFactory.cashInJson(
            "060065243", "10",
            "1234 1234 1234 1234", "Facundo ", "07/2019", "123"
        )
        val (_, _, _) = Fuel.post("cashin").body(cashin_json_obj.toString()).response()

        val transfer_json_obj: JsonObject = jsonFactory.transferJson("060065243", "519264035", "10")

        val (_, response, _) = Fuel.post("transfer").body(transfer_json_obj.toString()).response()

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

