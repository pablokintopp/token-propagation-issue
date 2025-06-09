package poc;


import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.websockets.next.OnError;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@WebSocket(path = "/websocket-reactive-uni/to/rest")
public class WebSocketReactiveUniEndpoint {

    private static final Logger LOG = Logger.getLogger(WebSocketReactiveUniEndpoint.class);
    @Inject
    SecurityIdentity currentIdentity;

    @Inject
    ChatbotAgentService chatbotAgentService;


    @OnOpen
    String open() {
        LOG.infof("User %s with roles %s connected to %s",
                currentIdentity.getPrincipal().getName(),
                currentIdentity.getRoles(),
                this.getClass().getSimpleName());
        return "Connection opened in " + this.getClass().getSimpleName();
    }

    @RolesAllowed("admin")
    @OnTextMessage
    Uni<String> message(String message) {
        LOG.infof("User %s with roles %s sent message %s to %s",
                currentIdentity.getPrincipal().getName(),
                currentIdentity.getRoles(),
                message,
                this.getClass().getSimpleName());

        return chatbotAgentService.chatMessage("Test", message).collect().asList()
                .onItem().transform(list -> String.join("", list));
    }

    @OnError
    void error(Exception e) {
        e.printStackTrace();
    }
}
