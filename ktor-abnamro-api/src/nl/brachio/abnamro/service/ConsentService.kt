package nl.brachio.abnamro.service

import mu.KotlinLogging


private val logger = KotlinLogging.logger {}

class ConsentService(private val abnamroRequestService: AbnamroRequestService) {

    // TODO: we simply return the iban for which the consent was given; in a real application this could be extended
    suspend fun retrieveConsentInfo(accessToken: String): String {
        val consentInfoResponse = abnamroRequestService.doGetCall<ConsentInfoResponse>(accessToken, "/v1/consentinfo")
        logger.info("consentInfoResponse = $consentInfoResponse")
        return consentInfoResponse.iban
    }
}


data class ConsentInfoResponse(
    val iban: String,
    val transactionId: String,
    val scopes: String,
    val valid: Number
)
