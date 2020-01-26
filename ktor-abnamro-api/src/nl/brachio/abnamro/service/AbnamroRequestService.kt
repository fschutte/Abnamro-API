package nl.brachio.abnamro.service

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.content.TextContent
import io.ktor.http.takeFrom
import nl.brachio.abnamro.Config

/**
 * Generic http service, and also a method for retrieving the token.
 */
class AbnamroRequestService(val config: Config, val client: HttpClient) {


    // generic get service
    suspend inline fun <reified T>doGetCall(accessToken: String, path: String): T =
        this.client.get {
            url(config.apiBaseUrl + path)
            header("Accept", "application/json")
            header("API-Key", config.apiKey)
            header("Authorization", "Bearer $accessToken")
        }

    suspend fun callTokenOauth(code: String): AbnamroTokenResponse {

        val abnamroTokenPage = URLBuilder().apply {
            takeFrom(config.oauthTokenUrl)
        }
        val requestBody = "grant_type=authorization_code&client_id=TPP_test&code=$code&redirect_uri=https://localhost/auth"
        return client.post<AbnamroTokenResponse> {
            url.takeFrom(abnamroTokenPage)
            body = TextContent(requestBody, contentType = ContentType.Application.FormUrlEncoded)
            header("Cache-Control", "no-cache")
        }
    }


}

data class AbnamroTokenResponse(
    val access_token: String,
    val refresh_token: String,
    val scope: String,
    val token_type: String,
    val expires_in: String
)