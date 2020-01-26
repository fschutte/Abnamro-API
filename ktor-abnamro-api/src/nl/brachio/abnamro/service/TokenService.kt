package nl.brachio.abnamro.service

import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class TokenService(private val abnamroRequestService: AbnamroRequestService) {

    // TODO: currently we just return the access token as a string, in a real application we would need
    //  to have the expiration time etc, so that we can cache it..
    suspend fun retrieveAccessToken(code: String): String {
        val tokenResponse = abnamroRequestService.callTokenOauth(code)
        logger.info("tokenResponse = $tokenResponse")
        return tokenResponse.access_token
    }
}