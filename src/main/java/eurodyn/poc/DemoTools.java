package eurodyn.poc;

import dev.langchain4j.agent.tool.Tool;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;


@ApplicationScoped
public class DemoTools {

    private static final Logger LOG = Logger.getLogger(DemoTools.class);

    @Inject
    @RestClient
    ResourceClient resourceClient;

    @Inject
    SecurityIdentity identity;


    @Tool("find the value in the Rest endpoint")
    public Long findValue(String system) {
        try {
            LOG.infof("Request to findValue from user '%s' with roles '%s'",
                    identity.getPrincipal(),
                    identity.getRoles().toString());

            Long value = resourceClient.findValue();

            LOG.infof("Value found: %s", value);
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
