package fi.hsl.jore4.auth.oidc

/**
 * Definition of the keys used to store data in in the user session.
 */
class SessionKeys {
    companion object {
        const val OIDC_STATE_KEY = "oidc_state"

        const val USER_TOKEN_SET_KEY = "user_token_set"
    }
}
