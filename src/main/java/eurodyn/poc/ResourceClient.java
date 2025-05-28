package eurodyn.poc;

import io.quarkus.oidc.token.propagation.common.AccessToken;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/resource/")
@AccessToken
@RegisterRestClient(configKey = "ResourceClient")
public interface ResourceClient {
    @GET
    @Path("value")
    Long findValue();
}
