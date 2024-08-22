package de.fraunhofer.iosb.ilt.ams.api.datafetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import de.fraunhofer.iosb.ilt.ams.model.HumanResource;
import de.fraunhofer.iosb.ilt.ams.model.filter.HumanResourceFilter;
import de.fraunhofer.iosb.ilt.ams.model.input.HumanResourceInput;
import de.fraunhofer.iosb.ilt.ams.model.response.HumanResourceResponse;
import de.fraunhofer.iosb.ilt.ams.repository.ProductionResourceRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

@DgsComponent
public class HumanResourceDatafetcher {

    @Autowired ProductionResourceRepository productionResourceRepository;

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'humanResource')")
    @DgsQuery
    public List<HumanResource> humanResource(@InputArgument HumanResourceFilter filter) {
        return productionResourceRepository.getHumanResources(filter);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'humanResource')")
    @DgsMutation
    public HumanResourceResponse createHumanResource(
            @InputArgument HumanResourceInput humanResource) {
        return productionResourceRepository.createHumanResource(humanResource);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'humanResource')")
    @DgsMutation
    public HumanResourceResponse updateHumanResource(
            @InputArgument String humanResourceId,
            @InputArgument HumanResourceInput humanResource) {
        return productionResourceRepository.updateHumanResource(humanResourceId, humanResource);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'humanResource')")
    @DgsMutation
    public HumanResourceResponse deleteHumanResource(@InputArgument String humanResourceId) {
        return productionResourceRepository.deleteHumanResource(humanResourceId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'humanResource')")
    @DgsMutation
    public HumanResourceResponse addProcessToHumanResource(
            @InputArgument String processId, @InputArgument String humanResourceId) {
        return this.productionResourceRepository.addProcessToHumanResource(
                processId, humanResourceId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'humanResource')")
    @DgsMutation
    public HumanResourceResponse removeProcessFromHumanResource(
            @InputArgument String processId, @InputArgument String humanResourceId) {
        return this.productionResourceRepository.removeProcessFromHumanResource(
                processId, humanResourceId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'humanResource')")
    @DgsMutation
    public HumanResourceResponse addCapabilityToHumanResource(
            @InputArgument String capabilityId, @InputArgument String humanResourceId) {
        return this.productionResourceRepository.addCapabilityToHumanResource(
                capabilityId, humanResourceId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'humanResource')")
    @DgsMutation
    public HumanResourceResponse removeCapabilityFromHumanResource(
            @InputArgument String capabilityId, @InputArgument String humanResourceId) {
        return this.productionResourceRepository.removeCapabilityFromHumanResource(
                capabilityId, humanResourceId);
    }
}
