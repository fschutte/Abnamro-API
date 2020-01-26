package nl.brachio.abnamro.model

import java.util.concurrent.atomic.AtomicInteger

/**
 * This class groups a specific iban with the access token that is necessary to retrieve info about the iban.
 * In the abnamro model you can apparently only give consent for one account at a time, contrary to APIs of other banks.
 */
data class Registration (
    val id: Int = idGenerator.incrementAndGet(),
    val accessToken: String,
    val iban: String

    // TODO: add expiry, refreshToken, scopes, ..
)  {

    companion object {
        private val idGenerator = AtomicInteger()
    }

}
