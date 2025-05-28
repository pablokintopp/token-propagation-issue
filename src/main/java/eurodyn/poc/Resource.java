package eurodyn.poc;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.jboss.logging.Logger;

@Path("resource")
public class Resource {

    private static final Logger LOG = Logger.getLogger(Resource.class);

    @Inject
    SecurityIdentity identity;

    @GET
    @Path("value")
    @RolesAllowed({"user", "admin"})
    public Long findValue() {
        LOG.infof("Request to %s from user '%s' with roles '%s'",
                this.getClass().getSimpleName(),
                identity.getPrincipal(),
                identity.getRoles().toString());

        return 5L;
    }

}
