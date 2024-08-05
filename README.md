# jore4-auth

Jore4 authentication and authorization backend. The backend uses a configured OpenID Connect provider to authenticate
and authorize the user via the OIDC Authorization Code Flow.

For more information on OpenID Connect, please see
[the OIDC specs](https://openid.net/specs/openid-connect-core-1_0.html), especially the
[section on the Authorization Code Flow](https://openid.net/specs/openid-connect-core-1_0.html#CodeFlowAuth).

## How to run locally

1. Install preliminaries
    - OpenJDK 17
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

## How to run with docker compose

```
development.sh start
```

Starts the auth service and jore4 postgres database in docker containers

## Building for deployment

Note that the above mentioned `dev` profile is only meant for use in your local development environment. To create a
build to be used for deployment, compile and create a package using the `prod` profile:
```
mvn clean package spring-boot:repackage -Pprod
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

## Docker reference

The application uses spring boot which allows overwriting configuration properties as described
[here](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config.typesafe-configuration-properties.relaxed-binding.environment-variables).
The docker container is also able to
[read secrets](https://github.com/HSLdevcom/jore4-tools#read-secretssh) and expose
them as environment variables.

The following configuration properties are to be defined for each environment:

| Config property            | Environment variable       | Secret name                | Example                 | Description                                           |
| ----------------------     | -------------------------- | -------------------------- | ----------------------- | ----------------------------------------------------- |
| -                          | SECRET_STORE_BASE_PATH     | -                          | /run/secrets            | Directory containing the docker secrets               |
| self.public.base.url       | SELF_PUBLIC_BASE_URL       | self-public-base-url       | https://jore.hsl.fi     | Jore4 auth base URL as the world sees it              |
| loginpage.url              | LOGINPAGE_URL              | loginpage-url              | https://jore.hsl.fi     | The full URL to which to return after login           |
| logoutpage.url             | LOGOUTPAGE_URL             | logoutpage-url             | https://jore.hsl.fi     | The full URL to which to return after logout          |
| oidc.provider.base.url     | OIDC_PROVIDER_BASE_URL     | oidc-provider-base-url     | https://id.hsl.fi       | The base URL of the OIDC provider                     |
| oidc.client.id             | OIDC_CLIENT_ID             | oidc-client-id             | ***                     | The client id from the OIDC provider                  |
| oidc.client.secret         | OIDC_CLIENT_SECRET         | oidc-client-secret         | ***                     | The client secret from the OIDC provider              |
| api.path.prefix            | API_PATH_PREFIX            | api-path-prefix            | /api                    | Base URL of the API within the container              |
| api.path.prefix.public     | API_PATH_PREFIX_PUBLIC     | api-path-prefix-public     | /api/auth               | Exposed base URL for API (e.g. from browser)          |
| db.hostname                | DB_HOSTNAME                | db-hostname                | postgres-host.com       | Persistent session database host name                 |
| db.port                    | DB_PORT                    | -                          | 5432                    | Persistent session database host port (default 5432)  |
| db.name                    | DB_NAME                    | db-name                    | jore4                   | Persistent session database name                      |
| db.username                | DB_USERNAME                | db-username                | auth_user               | Persistent session database user name                 |
| db.password                | DB_PASSWORD                | db-password                | ***                     | Persistent session database user password             |
| db.session.schema          | DB_SESSION_SCHEMA          | -                          | auth_session            | Persistent session database schema to use             |

More properties can be found from `/profiles/prod/config.properties`

## Tests

The tests for the auth backend can be run using the command
```
mvn clean verify -Pall-tests
```

Alternatively, you can run only the integration tests using the command
```
mvn clean verify -Pintegration-test
```

Note that currently there are no unit tests yet.

Beware that the source needs to be compiled again every time the profile is switched, i.e. between running the tests
and the application.
