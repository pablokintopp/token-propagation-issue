package poc;


import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.websockets.next.OnError;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import io.smallrye.mutiny.Multi;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@WebSocket(path = "/websocket-reactive-multi/to/rest")
public class WebSocketReactiveMultiEndpoint {

    private static final Logger LOG = Logger.getLogger(WebSocketReactiveMultiEndpoint.class);
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
    Multi<String> message(String message) {
        LOG.infof("User %s with roles %s sent message %s to %s",
                currentIdentity.getPrincipal().getName(),
                currentIdentity.getRoles(),
                message,
                this.getClass().getSimpleName());

        return chatbotAgentService.chatMessage("Test", message);
    }

    @OnError
    void error(Exception e) {
        e.printStackTrace();
    }
}
