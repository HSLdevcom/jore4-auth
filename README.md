# jore4-auth
Jore4 authentication and authorization backend


## How to run locally

1. Make a copy of the maven `dev`-profile for your user:
    ```
    cp profiles/dev/config.properties profiles/dev/config.<my-username>.properties
    ```


2. Adjust the properties in `profiles/dev/config.<my-username>.properties` to your needs. By default, the backend
   will listen at port 3001 and expect its API path (`/api`) to be visible to the world at `/api/auth`.


3. Build and run:
    ```
    mvn clean spring-boot:run -Pdev
    ```


## Functionality

To authenticate the user at the configured OIDC provider, redirect the user to the endpoint
`/api/public/v1.0/login` . After successful authentication, the access and refresh tokens obtained will be
stored in the user's session.

The user can then retrieve her user-info data from the OIDC provider by using the endpoint
`/api/public/v1.0/userInfo` .

In the future, this backend will provide functionality to authorize the user at a Hasura instance via a web
hook.

To log the user out, redirect her to the endpoint `/api/public/v1.0/login`.


## Implementation

The backend is implemented as a spring boot application.

The public endpoint interfaces are generated from the OpenAPI  specifications in the directory `src/main/openapi`. The
specifications are provided for download from the running application under the `/api-specs/openapi` path, the root
document being `/api-specs/openapi/api.yaml`.
