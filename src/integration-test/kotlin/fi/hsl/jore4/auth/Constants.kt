package fi.hsl.jore4.auth

import fi.hsl.jore4.auth.apipublic.v1.OIDCCodeExchangeApiController.Companion.EXCHANGE_ENDPOINT_PATH_SUFFIX

object Constants {
    const val SELF_BASE_URL_PUBLIC = "https://myhost.mydomain:1234"

    const val API_PATH_PREFIX = "/internal-api"
    const val API_PATH_PREFIX_PUBLIC = "/external-api/auth"

    const val CODE_EXCHANGE_CALLBACK_URI = "$SELF_BASE_URL_PUBLIC$API_PATH_PREFIX_PUBLIC$EXCHANGE_ENDPOINT_PATH_SUFFIX"

    const val OIDC_CLIENT_ID = "123456789clientId"
    const val OIDC_CLIENT_SECRET = "clientSecret012345"

    const val OIDC_AUTHORIZATION_CODE = "2345678901authCode"

    const val OIDC_PROVIDER_SCHEME = "http"
    const val OIDC_PROVIDER_HOST = "localhost"
    const val OIDC_PROVIDER_PORT = 9977

    const val OIDC_PROVIDER_AUTHORIZATION_ENDPOINT_PATH = "/test-openid/auth"
    const val OIDC_PROVIDER_TOKEN_ENDPOINT_PATH = "/test-openid/token"
    const val OIDC_PROVIDER_USERINFO_ENDPOINT_PATH = "/test-openid/userinfo"
    const val OIDC_PROVIDER_JWKS_URI_PATH = "/test-openid/jwks.json"
    const val OIDC_PROVIDER_END_SESSION_ENDPOINT_PATH = "/test-openid/logout"

    const val OIDC_PROVIDER_DISCOVERY_CONTENT = """
            {
              "issuer": "$OIDC_PROVIDER_SCHEME://$OIDC_PROVIDER_HOST:$OIDC_PROVIDER_PORT",
              "authorization_endpoint": "$OIDC_PROVIDER_SCHEME://$OIDC_PROVIDER_HOST:$OIDC_PROVIDER_PORT$OIDC_PROVIDER_AUTHORIZATION_ENDPOINT_PATH",
              "token_endpoint": "$OIDC_PROVIDER_SCHEME://$OIDC_PROVIDER_HOST:$OIDC_PROVIDER_PORT$OIDC_PROVIDER_TOKEN_ENDPOINT_PATH",
              "userinfo_endpoint": "$OIDC_PROVIDER_SCHEME://$OIDC_PROVIDER_HOST:$OIDC_PROVIDER_PORT$OIDC_PROVIDER_USERINFO_ENDPOINT_PATH",
              "jwks_uri": "$OIDC_PROVIDER_SCHEME://$OIDC_PROVIDER_HOST:$OIDC_PROVIDER_PORT$OIDC_PROVIDER_JWKS_URI_PATH",
              "scopes_supported": [
                "address",
                "email",
                "externalpermissions.grant",
                "externalpermissions.query",
                "offline_access",
                "openid",
                "phone",
                "profile"
              ],
              "response_types_supported": [
                "code",
                "code id_token",
                "code id_token token",
                "code token",
                "id_token",
                "id_token token",
                "token id_token"
              ],
              "response_modes_supported": [
                "query",
                "fragment"
              ],
              "grant_types_supported": [
                "authorization_code",
                "refresh_token",
                "implicit",
                "password",
                "client_credentials"
              ],
              "subject_types_supported": [
                "public"
              ],
              "id_token_signing_alg_values_supported": [
                "RS256",
                "RS384",
                "RS512",
                "ES256",
                "ES384",
                "ES512",
                "PS256",
                "PS384",
                "PS512"
              ],
              "token_endpoint_auth_methods_supported": [
                "client_secret_basic",
                "client_secret_post"
              ],
              "claim_types_supported": [
                "normal"
              ],
              "claims_supported": [
                "address",
                "birthdate",
                "commerce/product-supplier-sales-history-access",
                "email",
                "email_verified",
                "etb/claims/benefits",
                "etb/claims/roles",
                "family_name",
                "gender",
                "given_name",
                "https://oneportal.trivore.com/claims/active_external_permissions",
                "locale",
                "middle_name",
                "name",
                "nickname",
                "phone_number",
                "phone_number_verified",
                "picture",
                "preferred_username",
                "profile",
                "sub",
                "updated_at",
                "website",
                "zoneinfo"
              ],
              "service_documentation": "https://redirect.trivore.com/TIS-OpenId-Connect-Doc",
              "ui_locales_supported": [
                "en",
                "fi",
                "sv"
              ],
              "claims_parameter_supported": true,
              "request_parameter_supported": false,
              "require_request_uri_registration": false,
              "end_session_endpoint": "$OIDC_PROVIDER_SCHEME://$OIDC_PROVIDER_HOST:$OIDC_PROVIDER_PORT$OIDC_PROVIDER_END_SESSION_ENDPOINT_PATH",
              "frontchannel_logout_supported": true,
              "frontchannel_logout_session_supported": false,
              "backchannel_logout_supported": true,
              "backchannel_logout_session_supported": false
            }
        """
}
