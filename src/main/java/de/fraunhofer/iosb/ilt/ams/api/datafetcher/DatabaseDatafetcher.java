package de.fraunhofer.iosb.ilt.ams.api.datafetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import de.fraunhofer.iosb.ilt.ams.model.ObjectRdf4jRepository;
import de.fraunhofer.iosb.ilt.ams.model.response.CapabilityResponse;
import de.fraunhofer.iosb.ilt.ams.utility.MESSAGE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

@DgsComponent
public class DatabaseDatafetcher {

    @Autowired ObjectRdf4jRepository rdf4jRepository;

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'deleteAll')")
    @DgsMutation
    public CapabilityResponse deleteAll() {
        this.rdf4jRepository.deleteAll();
        CapabilityResponse response = new CapabilityResponse();
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        response.setCode(200);
        return response;
    }
}
