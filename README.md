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
│   │   │   └── poc/
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

**Interaction example and output** :
```
Connection opened in WebSocketNonReactiveEndpoint_Subclass
What is my user id?
Your user id is 5.
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

**Interaction example and output** :
```
Connection opened in WebSocketReactiveUniEndpoint_Subclass
What is my user id?
Your user id is 5.
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

**Interaction output** :
```
Connection opened in WebSocketReactiveMultiEndpoint_Subclass
What is my user id?
I
'm
 sorry
,
 but
 the
 user
 ID
 could
 not
 be
 retrieved
 at
 this
 time
.

```
**Logs during interaction** :
```
INFO  [poc.DemoTools] (vert.x-worker-thread-1) Request to findValue from user 'io.quarkus.security.runtime.AnonymousIdentityProvider$1@57ad230f' with roles '[]'
org.jboss.resteasy.reactive.ClientWebApplicationException: Received: 'Unauthorized, status code 401' when invoking REST Client method: 'poc.ResourceClient#findValue'
        at org.jboss.resteasy.reactive.client.impl.RestClientRequestContext.unwrapException(RestClientRequestContext.java:205)
        at org.jboss.resteasy.reactive.common.core.AbstractResteasyReactiveContext.handleException(AbstractResteasyReactiveContext.java:329)
        at org.jboss.resteasy.reactive.common.core.AbstractResteasyReactiveContext.run(AbstractResteasyReactiveContext.java:175)
        at io.smallrye.context.impl.wrappers.SlowContextualRunnable.run(SlowContextualRunnable.java:19)
        at org.jboss.resteasy.reactive.client.handlers.ClientSwitchToRequestContextRestHandler$1$1.handle(ClientSwitchToRequestContextRestHandler.java:38)
        at org.jboss.resteasy.reactive.client.handlers.ClientSwitchToRequestContextRestHandler$1$1.handle(ClientSwitchToRequestContextRestHandler.java:35)
        at io.vertx.core.impl.ContextInternal.dispatch(ContextInternal.java:270)
        at io.vertx.core.impl.ContextInternal.dispatch(ContextInternal.java:252)
        at io.vertx.core.impl.ContextInternal.lambda$runOnContext$0(ContextInternal.java:50)
        at io.netty.util.concurrent.AbstractEventExecutor.runTask(AbstractEventExecutor.java:173)
        at io.netty.util.concurrent.AbstractEventExecutor.safeExecute(AbstractEventExecutor.java:166)
        at io.netty.util.concurrent.SingleThreadEventExecutor.runAllTasks(SingleThreadEventExecutor.java:472)
        at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:569)
        at io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:998)
        at io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74)
        at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)
        at java.base/java.lang.Thread.run(Thread.java:1583)
Caused by: jakarta.ws.rs.WebApplicationException: Unauthorized, status code 401
        at io.quarkus.rest.client.reactive.runtime.DefaultMicroprofileRestClientExceptionMapper.toThrowable(DefaultMicroprofileRestClientExceptionMapper.java:19)
        at io.quarkus.rest.client.reactive.runtime.MicroProfileRestClientResponseFilter.filter(MicroProfileRestClientResponseFilter.java:54)
        at org.jboss.resteasy.reactive.client.handlers.ClientResponseFilterRestHandler.handle(ClientResponseFilterRestHandler.java:21)
        at org.jboss.resteasy.reactive.client.handlers.ClientResponseFilterRestHandler.handle(ClientResponseFilterRestHandler.java:10)
        at org.jboss.resteasy.reactive.common.core.AbstractResteasyReactiveContext.invokeHandler(AbstractResteasyReactiveContext.java:231)
        at org.jboss.resteasy.reactive.common.core.AbstractResteasyReactiveContext.run(AbstractResteasyReactiveContext.java:147)
        ... 14 more
io.vertx.core.http.HttpClosedException: Connection was closed

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

