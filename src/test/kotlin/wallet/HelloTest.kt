package wallet

import io.javalin.Javalin
import io.kotlintest.specs.AbstractAnnotationSpec
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class DigitalWalletApiTest {
    private lateinit var api: Javalin

    @AbstractAnnotationSpec.BeforeAll
    fun setUp() {
        api = DigitwalletApi(8000).init()
        // Inject the base path to no have repeat the whole URL
        FuelManager.instance.basePath = "http://localhost:${api.port()}/"
    }
}