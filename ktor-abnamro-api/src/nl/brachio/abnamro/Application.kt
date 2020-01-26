package nl.brachio.abnamro

import freemarker.cache.ClassTemplateLoader
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging
import io.ktor.features.BadRequestException
import io.ktor.features.StatusPages
import io.ktor.freemarker.FreeMarker
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.http.takeFrom
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.sessions.*
import io.ktor.util.KtorExperimentalAPI
import mu.KotlinLogging
import nl.brachio.abnamro.service.AbnamroRequestService
import nl.brachio.abnamro.service.AccountInformationService
import nl.brachio.abnamro.service.ConsentService
import nl.brachio.abnamro.service.TokenService
import org.apache.http.ssl.SSLContexts
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import org.kodein.di.newInstance
import java.io.File
import java.util.*
import kotlin.collections.set


/**
 * In this example I use Kotlin with:
 * Freemarker
 * Static Content
 * Sessions
 * HttpClient Engine
 * Apache HttpClient Engine
 * Json Serialization
 * Logging
 * Ktor 1.2
 * Maven
 * Netty
 * SDK 12
 *
 *
 */


private val logger = KotlinLogging.logger {}

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@KtorExperimentalAPI
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }

    install(StatusPages) {
        exception<NoSessionException> { _ ->
            call.respond(HttpStatusCode.Forbidden, "No session found")
        }
        exception<Throwable> { cause ->
            call.respond(HttpStatusCode.InternalServerError, "Oops, we got an error: ${cause.message}" )
        }
    }

    install(Sessions) {
        cookie<MySession>("MY_SESSION", SessionStorageMemory()) {
            cookie.extensions["SameSite"] = "lax"
        }
    }

    val kodein = Kodein {
        val config = instance(Config())
        bind<Config>() with config
        bind<HttpClient>() with instance(createHttpClient(config.instance))
        bind<AbnamroRequestService>() with singleton { AbnamroRequestService(instance(), instance()) }
    }

    val tokenService by kodein.newInstance { TokenService(instance()) }
    val consentService by kodein.newInstance { ConsentService(instance()) }
    val accountInformationService by kodein.newInstance { AccountInformationService(instance()) }
    val config by kodein.instance<Config>()

    routing {

        get("/") {
            call.respond(FreeMarkerContent("index.ftl", null))
        }

        // Static feature. Try to access `/static/ktor_logo.svg`
        static("/static") {
            resources("static")
        }

        get("/startconsent") {

            // create a random string and put this in session; when coming back we should check the value
            val state = UUID.randomUUID().toString()
            val session = call.sessions.get<MySession>() ?: MySession(state)
            call.sessions.set(session.copy(state = state))

            val abnamroConsentPage = URLBuilder().apply {
                takeFrom(config.consentPage)
                parameters.apply {
                    append("scope", "psd2:account:balance:read psd2:account:transaction:read psd2:account:details:read")
                    append("client_id", "TPP_test")
                    append("response_type", "code")
                    append("flow", "code")
                    append("redirect_uri", "https://localhost/auth")  // enig toegestane redirect (http ipv https mag niet en ook andere port kan niet)
                    append("bank", "NLAA01")
                    append("state", state)  // state moet iets zijn wat we in de sessie hebben gezet
                }
            }.buildString()
            call.respondRedirect(abnamroConsentPage)
        }

        // this is called once the user has given consent; it should contain a state and a code
        get("/auth") {

            // TODO: currently only dealing with happy path; in case of error
            //   we can expect parameters state, error, and error_description (but not code)
            val state = call.parameters["state"] ?: throw BadRequestException("parameter 'state' missing")
            val code = call.parameters["code"] ?: throw BadRequestException("parameter 'code' missing")
            val session = call.sessions.get<MySession>() ?: throw NoSessionException("No session")
            if (state != session.state) throw BadRequestException("state is incorrect")

            val accessToken = tokenService.retrieveAccessToken(code)
            val iban = consentService.retrieveConsentInfo(accessToken)

            logger.debug { "Access token = $accessToken and user gave consent for $iban" }

            val account = accountInformationService.getAccount(accessToken, iban)
            val transactionList = accountInformationService.getTransactions(accessToken, iban)

            call.respond(FreeMarkerContent("accountinfo.ftl",
                mapOf(
                    "account" to account,
                    "transactionList" to transactionList
                    )
            ))
        }
    }
}


// create an HttpClient with appropriate configuration
private fun createHttpClient(config: Config): HttpClient = HttpClient(Apache) {
    engine {
        sslContext = SSLContexts.custom()
            .loadKeyMaterial(
                File(config.keystorePath),
                config.keystorePassword.toCharArray(),
                config.keyPassword.toCharArray()
            )
            // we current do not need to add trustmanagers because Abnamro apparently is now using a Digicert certificate
            // and that is trusted by default
            //  .loadTrustMaterial(...)
            .build()
    }
    install(JsonFeature) {
        serializer = GsonSerializer() {
            setLenient()
        }
    }
    install(Logging) {
        // nice feature: set LogLevel to INFO, HEADERS or BODY for useful tracing
        level = LogLevel.BODY
    }
}




data class MySession(val state: String)

class NoSessionException(message: String) : RuntimeException(message)


