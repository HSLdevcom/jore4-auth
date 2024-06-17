package fi.hsl.jore4.auth.userInfo

import fi.hsl.jore4.auth.apipublic.v1.model.UserInfoApiDTO

/**
 * Censor the given user info object by masking sensitive fields in the {@code toString()} method.
 *
 * Objects of this class are safe to be used in e.g. logging.
 *
 * Since the user info object is (and in the foreseeable future will be) the only
 * entity containing sensitive data, we won't bother integrating a more sophisticated
 * logging system, which could mask sensitive data in a better way.
 */
class SafeUserInfoApiDTO : UserInfoApiDTO() {
    override fun toString() =
        UserInfoApiDTO()
            .also {
                it.id = this.id
                it.givenName = "XXX"
                it.familyName = "XXX"
                it.fullName = "XXX"
                it.permissions = this.permissions
            }
            .toString()
}
