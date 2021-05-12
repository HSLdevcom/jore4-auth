package fi.hsl.jore4.auth.authentication

import org.springframework.util.StringUtils
import java.net.URLEncoder

class OIDCUtil {
    companion object {
        fun buildRedirectUri(baseUrl: String, clientRedirectUrl: String?): String = baseUrl +
                if (StringUtils.isEmpty(clientRedirectUrl)) ""
                else "?clientRedirectUrl=${URLEncoder.encode(clientRedirectUrl, "UTF-8")}"
    }
}