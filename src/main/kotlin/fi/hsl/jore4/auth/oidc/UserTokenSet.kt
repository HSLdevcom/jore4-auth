package fi.hsl.jore4.auth.oidc

import com.nimbusds.oauth2.sdk.RefreshTokenGrant
import com.nimbusds.oauth2.sdk.TokenRequest
import com.nimbusds.oauth2.sdk.TokenResponse
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic
import com.nimbusds.oauth2.sdk.auth.Secret
import com.nimbusds.oauth2.sdk.id.ClientID
import com.nimbusds.oauth2.sdk.token.AccessToken
import com.nimbusds.oauth2.sdk.token.RefreshToken
import org.slf4j.LoggerFactory
import java.io.Serializable
import java.net.URI
import javax.naming.AuthenticationException

/**
 * Holds the user's access and refresh tokens and refreshes them.
 */
class UserTokenSet(
    accessToken: AccessToken,
    refreshToken: RefreshToken
) : Serializable {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(UserTokenSet::class.java)
    }

    var accessToken: AccessToken = accessToken
        private set
    var refreshToken: RefreshToken = refreshToken
        private set

    /**
     * Refresh the access and refresh tokens.
     */
    fun refresh(tokenEndpointURI: URI, clientID: ClientID, clientSecret: Secret): UserTokenSet {
        LOGGER.debug("Refreshing tokens...")

        // create the token request based on the refresh token
        val request = TokenRequest(
            tokenEndpointURI,
            ClientSecretBasic(clientID, clientSecret),
            RefreshTokenGrant(refreshToken)
        )

        val response = TokenResponse.parse(request.toHTTPRequest().send())

        if (!response.indicatesSuccess()) {
            throw AuthenticationException("Token refresh failed")
        }

        val successResponse = response.toSuccessResponse()

        LOGGER.debug("Tokens refreshed.")

        return UserTokenSet(successResponse.tokens.accessToken, successResponse.tokens.refreshToken)
    }
}
