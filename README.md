# Token Propagation Issue with WebSocket Reactive in Quarkus Langchain4j

This project demonstrates a potential issue with OIDC token propagation when using WebSocket Reactive endpoints in
Quarkus Langchain4j. The goal is to reproduce and investigate the problem, which occurs when attempting to send and
receive responses from the chatbot via a WebSocket Reactive endpoint.

## Project Overview

### Project Structure
```
token-propagation-issue/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── eurodyn/poc/
│   │   │       ├── ChatbotAgentService.java         # Defines chatbot behavior using Langchain4j
│   │   │       ├── DemoTools.java                   # Provides tools for chatbot interactions
│   │   │       ├── Resource.java                    # REST resource with role-protected endpoint
│   │   │       ├── ResourceClient.java              # REST client interface with OIDC token propagation
│   │   │       ├── WebSocketNonReactiveEndpoint.java    # WebSocket endpoint for non-reactive communication
│   │   │       ├── WebSocketReactiveUniEndpoint.java    # WebSocket endpoint using Uni for reactive communication
│   │   │       └── WebSocketReactiveMultiEndpoint.java  # WebSocket endpoint using Multi for streaming responses
│   │   └── resources/
│   │       ├── application.properties               # Quarkus configuration file (includes Dev Services settings)
│   │       └── keycloak-realm.json                  # Keycloak realm with users, roles, and client config
│
├── pom.xml                                          # Maven project descriptor with Quarkus dependencies and plugins
```

### WebSocket examples
The project includes three WebSocket examples:

1. **WebSocket Non-Reactive** (Working) - `/websocket-non-reactive/to/rest`
2. **WebSocket Reactive with Uni** (Working) - `/websocket-reactive-uni/to/rest`
3. **WebSocket Reactive with Multi** (Not Working) - `/websocket-reactive-multi/to/rest`

The issue arises in the third example, where the OIDC token appears to be null or nonexistent when reaching the REST
endpoint. This prevents the proper propagation of the token and results in a failure.

## Prerequisites

- Java 21
- Maven
- Quarkus 3.23.0
- Keycloak (configured with the provided `keycloak-realm.json`)

## Running the Application in Dev Mode

1. Set the OpenAI API key as an environment variable:
   ```shell
   export QUARKUS_LANGCHAIN4J_OPENAI_API_KEY="change-me-to-your-openai-api-key"
   ```

2. Run the application in development mode:
   ```shell
   ./mvnw quarkus:dev
   ```
## WebSocket Examples

### WebSocket Testing Tool
To test the WebSocket endpoints, a websocket's client such as [websocat](https://github.com/vi/websocat) is necessary.

The endpoint is secured with the same OIDC token and the request must have the `admin` role to access it.

### WebSocket Non-Reactive (Working)
This example successfully propagates the OIDC token to the REST endpoint
```shell script
# Retrieve an access token for the admin user.
ACCESS_TOKEN=$(curl -s -X POST "http://localhost:8180/realms/poc-realm/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=poc-client-id" \
  -d "client_secret=poc-secret" \
  -d "grant_type=password" \
  -d "username=poc-admin" \
  -d "password=poc-admin123" | jq -r '.access_token')  

websocat ws://localhost:8081//websocket-non-reactive/to/rest -H "Authorization: Bearer $ACCESS_TOKEN"
```

Expected Output:
```
Connection opened in WebSocketNonReactiveEndpoint_Subclass
hello
Would you like to find the value in the Rest endpoint?
yes
The value found in the Rest endpoint is 5.

```

### WebSocket Reactive with Uni (Working)
This example also successfully propagates the OIDC token to the REST endpoint.
```shell script
# Retrieve an access token for the admin user.
ACCESS_TOKEN=$(curl -s -X POST "http://localhost:8180/realms/poc-realm/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=poc-client-id" \
  -d "client_secret=poc-secret" \
  -d "grant_type=password" \
  -d "username=poc-admin" \
  -d "password=poc-admin123" | jq -r '.access_token')  

websocat ws://localhost:8081//websocket-reactive-uni/to/rest -H "Authorization: Bearer $ACCESS_TOKEN"
```

Expected Output:
```
Connection opened in WebSocketReactiveUniEndpoint_Subclass
hello
Would you like to find the value in the Rest endpoint?
yes
The value found in the Rest endpoint is 5.
```

### WebSocket Reactive with Multi (Not Working)

This example fails to propagate the OIDC token to the REST endpoint. The token appears to be null or nonexistent.
```shell script
# Retrieve an access token for the admin user.
ACCESS_TOKEN=$(curl -s -X POST "http://localhost:8180/realms/poc-realm/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=poc-client-id" \
  -d "client_secret=poc-secret" \
  -d "grant_type=password" \
  -d "username=poc-admin" \
  -d "password=poc-admin123" | jq -r '.access_token')  

websocat ws://localhost:8081//websocket-reactive-multi/to/rest -H "Authorization: Bearer $ACCESS_TOKEN"
```

Expected Output:
```
Connection opened in WebSocketReactiveMultiEndpoint_Subclass
hello
Would
 you
 like
 to
 find
 the
 value
 in
 the
 Rest
 endpoint
?
yes
The
 value
 found
 in
 the
 Rest
 endpoint
 is
 null
.

```

## Keycloak Configuration

The Keycloak realm is configured in src/main/resources/keycloak-realm.json. It includes:
- A realm named `poc-realm`
- Client `poc-client-id` with secret `poc-secret`
- User `poc-admin` with password `poc-admin123`

## Application Properties
The application properties are defined in `src/main/resources/application.properties`. Key configurations include:
- HTTP port: `8081`
- Keycloak dev services port: 8180
- OpenAI API key: `QUARKUS_LANGCHAIN4J_OPENAI_API_KEY`
