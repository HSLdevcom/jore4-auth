package fi.hsl.jore4.auth

import fi.hsl.jore4.auth.apipublic.v1.OIDCCodeExchangeApiController.Companion.EXCHANGE_ENDPOINT_PATH_SUFFIX

/**
 * Constant definitions used in the integration tests.
 *
 * Note that all constants are defined here statically, even though some of them are duplicated from the integration
 * test maven properties (which are passed as spring properties). This is because static definitions allow us to catch
 * errors related to reading the properties at run time. If we'd read the integration test definitions at run time,
 * any errors related to reading the properties would appear in the integration tests too. (Comparing a value computed
 * by the application to the expected value would pass, even though both values might be wrong.)
 */
object Constants {
    const val SELF_BASE_URL_PUBLIC = "https://myhost.mydomain:1234"
    const val LOGIN_PAGE_URL = "https://mylogin.myhost.mydomain:5678"
    const val LOGOUT_PAGE_URL = "https://mylogout.myhost.mydomain:9012"

    const val API_PATH_PREFIX = "/internal-api"
    const val API_PATH_PREFIX_PUBLIC = "/external-api/auth"

    const val CODE_EXCHANGE_CALLBACK_URI = "$SELF_BASE_URL_PUBLIC$API_PATH_PREFIX_PUBLIC$EXCHANGE_ENDPOINT_PATH_SUFFIX"

    const val OIDC_CLIENT_ID = "123456789clientId"
    const val OIDC_CLIENT_SECRET = "clientSecret012345"

    const val OIDC_STATE = "01010state23344332"
    const val OIDC_AUTHORIZATION_CODE = "2345678901authCode"

    const val OIDC_REFRESH_TOKEN = "sadkjfahsdkjfahsldkjawpowqeirw"

    const val OIDC_PROVIDER_SCHEME = "http"
    const val OIDC_PROVIDER_HOST = "localhost"
    const val OIDC_PROVIDER_PORT = 9977

    const val OIDC_PROVIDER_BASE_URL = "$OIDC_PROVIDER_SCHEME://$OIDC_PROVIDER_HOST:$OIDC_PROVIDER_PORT"

    const val OIDC_PROVIDER_AUTHORIZATION_ENDPOINT_PATH = "/test-openid/auth"
    const val OIDC_PROVIDER_TOKEN_ENDPOINT_PATH = "/test-openid/token"
    const val OIDC_PROVIDER_USERINFO_ENDPOINT_PATH = "/test-openid/userinfo"
    const val OIDC_PROVIDER_JWKS_URI_PATH = "/test-openid/jwks.json"
    const val OIDC_PROVIDER_END_SESSION_ENDPOINT_PATH = "/test-openid/logout"

    const val OIDC_PROVIDER_DISCOVERY_CONTENT = """
        {
          "issuer": "$OIDC_PROVIDER_BASE_URL",
          "authorization_endpoint": "$OIDC_PROVIDER_BASE_URL$OIDC_PROVIDER_AUTHORIZATION_ENDPOINT_PATH",
          "token_endpoint": "$OIDC_PROVIDER_BASE_URL$OIDC_PROVIDER_TOKEN_ENDPOINT_PATH",
          "userinfo_endpoint": "$OIDC_PROVIDER_BASE_URL$OIDC_PROVIDER_USERINFO_ENDPOINT_PATH",
          "jwks_uri": "$OIDC_PROVIDER_BASE_URL$OIDC_PROVIDER_JWKS_URI_PATH",
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
          "end_session_endpoint": "$OIDC_PROVIDER_BASE_URL$OIDC_PROVIDER_END_SESSION_ENDPOINT_PATH",
          "frontchannel_logout_supported": true,
          "frontchannel_logout_session_supported": false,
          "backchannel_logout_supported": true,
          "backchannel_logout_session_supported": false
        }
    """

    const val OIDC_PROVIDER_TOKEN_RESPONSE_TEMPLATE = """
        {
            "access_token": "%s",
            "refresh_token": "%s",
            "token_type": "Bearer"
        }
    """

    const val OIDC_PROVIDER_SIGNING_KEY_TYPE = "RSA"
    const val OIDC_PROVIDER_SIGNING_KEY_ID = "3653cf03-bd5c-46a6-be19-1dbc8f2f1eb7"
    const val OIDC_PROVIDER_SIGNING_KEY_EXP = "AQAB"
    const val OIDC_PROVIDER_SIGNING_KEY_MOD = "vzoth4D1m7y-Vp5ISoROb8lCVibEBb7gLWJuRgBaSMSMepmMNLPa3b4fzjoxZqQoi7kQ4khKbVt37jWgWjF99YDGxSlDYbudIg0AmpOBYmaORmY4KviuCJ7ASGew--_WtU1IwSHQC5PR-rEocom5MfMwhjQ-LMHaGIrxQIYrsefMXmcS8dxdCK_BXnB2AHWC7QV31CZsLRsCQ9nV4u0HqeQ2g3-5cyKUKptWScx6SKPurGLTO9Kx8V80t-PdxjNJ9WPEjm7W2MEsKLO6CKYb1R57amhrGRvNvKQ4jQMJ5CBGF0Qhgg9OjG45FvIfhm-koH39ZLT8bQMlobMdPUjMzQ"
    const val OIDC_PROVIDER_SIGNING_KEY_USE = "sig"

    const val OIDC_PROVIDER_SIGNING_KEY = """
        {
          "p": "7UxbBFTt4lHCteu8kRPLxrPbwnkKJbv_W-BJbwp9_RMYexUvZtOCfJ9cszjlOnmfH-s6cSkvGbx3spHfOMJLvT2TPJmI0CqgWWTqyrpt8JxmLnyq9-kn6vEyaYyX384I2CUrp3SNNI1kvyVTwNGb-1jdeU5SMYX9pxCHDxvq86k",
          "kty": "$OIDC_PROVIDER_SIGNING_KEY_TYPE",
          "q": "zkxPbFmTdy6rr7W__kZl0-ZoYbyzrLIPn4T0RAaqUh-4cByynLqv_GzBAp0SkPyQfejpOIMwpsJ5H5aMDnmFEBPLFuX0f94j_9u9Bu_ompMI6qdRKE_DGEYQ0H6ZfaPzSbywRpAnMDCy1ROe5AzzaOJTGZbdf1Ex3_LlVQ_YRoU",
          "d": "nZxV6llgFfx65OmQq9pgy8IV6uwIpLOuEboPKaMgxtk479Oeu9GBk0Ig9NkB3pdu07q1L8xli7zYHH4WrEAekTqSme5omyUHXglZdg4dlfzxRxT5NnHA_1nZIdRVC1GipcwNKyGzV9d-OiHKNJ1PBtxqIjLtC_a6y3-5WHU9jmMRb-kJ1Hovbx5vfk2IzYEq-wpc4TNVhSFkiBYonLyHTZ2ZdquGQCumLntcxBuH--3itFla5-DVIHvawGWjrmt-T7IsfSSU-Nho_NjTrOfnUKg9V83p6N5ODaDkJButBmbDcUFxLuC_1FGVkWQAtF0gtl4YaQJ0Sgkhs1KXNtnh",
          "e": "$OIDC_PROVIDER_SIGNING_KEY_EXP",
          "use": "$OIDC_PROVIDER_SIGNING_KEY_USE",
          "kid": "$OIDC_PROVIDER_SIGNING_KEY_ID",
          "qi": "QBcwrz4l5HfxOqclyMd0M7k_br71bx2bHp-J56vou3qE8BXTq4DToHAIsvPkAHglfBzMEl7BuBiQNqZMnQE-72mktdMjyKjbN1gGf4MbldQZAGKDyVWF0ABDLY5HvDsq3nFYMFsFOujzbXiJ0BN07DuO9ic2x1EzbZ5RPAV-6b0",
          "dp": "SnvsGcj6vk5Bms666qLXFLDB6yIJEF0ZQ2J8SlF1yGklBwVYUqNTUwDGnS7frOIeE2vHdVujACYLQE7PTUaXdXgNbjkXG5g1qrmlhSrNdXfyDuds1tIliHZbB4xs44ANgKLyN-a6p-M121XBG0Xv-w4ivn_aBtdzDex_KDvwHLk",
          "dq": "xgtmF8sqkTjhow4lIXLtoG5J-MIR7xEFhIs7f-6yJaLECgTSxzlFZ1v0MpEd2MhN4kHtWs4-r2h0pQl-rQql3hhSWEor3XEhOCf4IKfBslrDLZmwhLWFwlDJJY7TxVTWYwxcYXFT3aRymbF7UMzKA_957U_ylZ4Z9jcVSAKtCo0",
          "n": "$OIDC_PROVIDER_SIGNING_KEY_MOD"
        }
    """

    const val OIDC_PROVIDER_JWKS_RESPONSE = """
        {
            "keys": [
                {
                    "kty": "$OIDC_PROVIDER_SIGNING_KEY_TYPE",
                    "e": "$OIDC_PROVIDER_SIGNING_KEY_EXP",
                    "use": "$OIDC_PROVIDER_SIGNING_KEY_USE",
                    "kid": "$OIDC_PROVIDER_SIGNING_KEY_ID",
                    "n": "$OIDC_PROVIDER_SIGNING_KEY_MOD"
                },
                {
                    "kty": "EC",
                    "use": "sig",
                    "crv": "P-256",
                    "kid": "some ES256 key",
                    "x": "ligCm7MacrziXcZ_mmNXwm2UDVE6SjbsBe6oo3jK1gY",
                    "y": "HblO0SHtRE6nFl3SUswgfZ1vsfww7oItsvRC4bAv1kU"
                }
            ]
        }
    """

    const val RO_USER_EXT_PERMISSION_ID = "0123456789ABCDE"
    const val RO_USER_EXT_PERMISSION_EXT_ID = "admin"
    const val RO_USER_SUB = "60643ec68823e65ee44a9050"
    const val RO_USER_FIRST_NAME = "Rita"
    const val RO_USER_LAST_NAME = "Readonly"
    const val RO_USER_FULL_NAME = "$RO_USER_FIRST_NAME Roswita $RO_USER_LAST_NAME"

    const val RW_USER_EXT_PERMISSION_1_ID = "123456789ABCDEFGH"
    const val RW_USER_EXT_PERMISSION_1_EXT_ID = "admin"
    const val RW_USER_SUB = "13643ec68823e65ee4479432"
    const val RW_USER_FIRST_NAME = "Alicia"
    const val RW_USER_LAST_NAME = "Admin"
    const val RW_USER_FULL_NAME = "$RW_USER_FIRST_NAME $RW_USER_LAST_NAME"

    const val OIDC_PROVIDER_USERINFO_RESPONSE_TEMPLATE = """
        {
            "https://oneportal.trivore.com/claims/namespace": "jore4",
            "sub": "%s",
            "updated_at": 1623308975,
            "https://oneportal.trivore.com/claims/active_external_permissions": {
                "active": [
                    %s
                ]
            },
            "name": "%s",
            "preferred_username": "user.person@domain.xyz",
            "given_name": "%s",
            "family_name": "%s",
            "https://oneportal.trivore.com/claims/tags": []
        }
    """

    const val OIDC_PROVIDER_USERINFO_RESPONSE_WO_SUB_CLAIM = """
        {
            "https://oneportal.trivore.com/claims/namespace": "jore4",
            "updated_at": 1623308975,
            "name": "Someone Clever",
            "preferred_username": "user.person@domain.xyz",
            "given_name": "Someone",
            "family_name": "Clever",
            "https://oneportal.trivore.com/claims/tags": [],
            "https://oneportal.trivore.com/claims/active_external_permissions": {
                "active": [
                    {
                        "permissionId": "019283038374abcdef",
                        "permissionGroupId": "abcdefghijklmnopqrstuvwx",
                        "invalidPermissionExternalId": "permission1",
                        "grantedById": "1234567890abcdefghijklmn",
                        "grantedByType": "User",
                        "grantedTime": "2021-06-12T07:21:14.089Z",
                        "grantedToType": "CustomRole",
                        "grantedToId": "234567890abcdefghijklmno"
                    }
                ]
            }
        }
    """

    const val OIDC_PROVIDER_USERINFO_RESPONSE_WO_EXT_PERMISSIONS = """
        {
            "https://oneportal.trivore.com/claims/namespace": "jore4",
            "sub": "60643ec68823e65ee44a9050",
            "updated_at": 1623308975,
            "name": "Someone Clever",
            "preferred_username": "user.person@domain.xyz",
            "given_name": "Someone",
            "family_name": "Clever",
            "https://oneportal.trivore.com/claims/tags": []
        }
    """

    const val OIDC_PROVIDER_USERINFO_RESPONSE_WO_ACTIVE_EXT_PERMISSIONS = """
        {
            "https://oneportal.trivore.com/claims/namespace": "jore4",
            "sub": "60643ec68823e65ee44a9050",
            "updated_at": 1623308975,
            "name": "Someone Clever",
            "preferred_username": "user.person@domain.xyz",
            "given_name": "Someone",
            "family_name": "Clever",
            "https://oneportal.trivore.com/claims/tags": [],
            "https://oneportal.trivore.com/claims/active_external_permissions": {
                "notactive": [
                    {
                        "permissionId": "019283038374abcdef",
                        "permissionGroupId": "abcdefghijklmnopqrstuvwx",
                        "permissionExternalId": "permission1",
                        "grantedById": "1234567890abcdefghijklmn",
                        "grantedByType": "User",
                        "grantedTime": "2021-06-12T07:21:14.089Z",
                        "grantedToType": "CustomRole",
                        "grantedToId": "234567890abcdefghijklmno"
                    }
                ]
            }
        }
    """

    const val OIDC_PROVIDER_USERINFO_RESPONSE_WITH_INVALID_ACTIVE_EXT_PERMISSION = """
        {
            "https://oneportal.trivore.com/claims/namespace": "jore4",
            "sub": "60643ec68823e65ee44a9050",
            "updated_at": 1623308975,
            "name": "Someone Clever",
            "preferred_username": "user.person@domain.xyz",
            "given_name": "Someone",
            "family_name": "Clever",
            "https://oneportal.trivore.com/claims/tags": [],
            "https://oneportal.trivore.com/claims/active_external_permissions": {
                "active": [
                    {
                        "permissionId": "019283038374abcdef",
                        "permissionGroupId": "abcdefghijklmnopqrstuvwx",
                        "invalidPermissionExternalId": "permission1",
                        "grantedById": "1234567890abcdefghijklmn",
                        "grantedByType": "User",
                        "grantedTime": "2021-06-12T07:21:14.089Z",
                        "grantedToType": "CustomRole",
                        "grantedToId": "234567890abcdefghijklmno"
                    }
                ]
            }
        }
    """

    const val OIDC_PROVIDER_USERINFO_EXT_PERMISSION_TEMPLATE = """
        {
            "permissionId": "%s",
            "permissionGroupId": "abcdefghijklmnopqrstuvwx",
            "permissionExternalId": "%s",
            "grantedById": "1234567890abcdefghijklmn",
            "grantedByType": "User",
            "grantedTime": "2021-06-12T07:21:14.089Z",
            "grantedToType": "CustomRole",
            "grantedToId": "234567890abcdefghijklmno"
        }
    """
}
