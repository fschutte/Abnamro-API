package nl.brachio.abnamro

import com.typesafe.config.ConfigFactory
import io.ktor.config.HoconApplicationConfig
import io.ktor.util.KtorExperimentalAPI

@UseExperimental(KtorExperimentalAPI::class)
class Config() {
    private val config = HoconApplicationConfig(ConfigFactory.load())

    val keystorePath = config.property("abnamro.keystore.path").getString()
    val keystorePassword = config.property("abnamro.keystore.storepassword").getString()
    val keyPassword = config.property("abnamro.keystore.keypassword").getString()
//    val truststorePath = config.property("abnamro.truststore.path").getString()
//    val truststorePassword = config.property("abnamro.truststore.storepassword").getString()

    val apiKey = config.property("abnamro.api.apiKey").getString()
    val consentPage = config.property("abnamro.api.consentPage").getString()
    val oauthTokenUrl = config.property("abnamro.api.oauthTokenUrl").getString()
    val apiBaseUrl = config.property("abnamro.api.apiBaseUrl").getString()

}