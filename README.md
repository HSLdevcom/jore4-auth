# jore4-auth

Jore4 authentication and authorization backend. The backend uses a configured OpenID Connect provider to authenticate
and authorize the user via the OIDC Authorization Code Flow.

For more information on OpenID Connect, please see
[the OIDC specs](https://openid.net/specs/openid-connect-core-1_0.html), especially the
[section on the Authorization Code Flow](https://openid.net/specs/openid-connect-core-1_0.html#CodeFlowAuth).

## How to run locally

1. Install preliminaries
    - OpenJDK 11
1. Make a copy of the maven `dev`-profile for your user:
    ```
    cp profiles/dev/config.properties profiles/dev/config.<my-username>.properties
    ```
1. Adjust the properties in `profiles/dev/config.<my-username>.properties` to your needs. By default, the backend
   will listen at port 3001 and expect its API path (`/api`) to be visible to the world at `/api/auth`.
1. Build and run:
    ```
    mvn clean spring-boot:run -Pdev
    ```

## Building for deployment

Note that the above mentioned `dev` profile is only meant for use in your local development environment. To create a
build to be used for deployment, compile and create a package using the `prod` profile:
```
mvn clean package -Pprod
```

## Functionality

When started (and on application context refresh events), the backend automatically reads the OIDC provider
configuration from the configured URL's discovery endpoint.

To authenticate the user at the configured OIDC provider, redirect the user to the endpoint
`/api/public/v1/login` . After successful authentication, the access and refresh tokens obtained will be
stored in the user's session, which is at this point kept in-memory.
![login flow](https://github.com/HSLdevcom/jore4/blob/main/wiki/images/auth-login.png?raw=true)
*The login flow, also see the
[diagram source](https://github.com/HSLdevcom/jore4/blob/main/wiki/images/auth-login.puml)*

The user can then retrieve her user-info data from the OIDC provider by using the endpoint
`/api/public/v1/userInfo` . This will perform a user-info request with the user's access token stored in the session.
![user info flow](https://github.com/HSLdevcom/jore4/blob/main/wiki/images/auth-userinfo.png?raw=true)
*The user info flow, also see the
[diagram source](https://github.com/HSLdevcom/jore4/blob/main/wiki/images/auth-userinfo.puml)*

When the access token expires, the access and refresh tokens are refreshed transparently.

In the future, this backend will provide functionality to authorize the user at a Hasura instance via a web
hook.

To log the user out, redirect her to the endpoint `/api/public/v1/logout`. This will invalidate the user's session with
the Jore4 auth backend and redirect her to the "end session" endpoint of the OIDC provider.
![logout flow](https://github.com/HSLdevcom/jore4/blob/main/wiki/images/auth-logout.png?raw=true)
*The logout flow, also see the
[diagram source](https://github.com/HSLdevcom/jore4/blob/main/wiki/images/auth-logout.puml)*

## Implementation details

The backend is implemented as a spring boot application.

The public endpoint interfaces are generated from the OpenAPI specifications in the directory `src/main/openapi`. The
specifications are provided for download from the running application under the `/api-specs/openapi` path, the root
document being `/api-specs/openapi/api.yaml`.
