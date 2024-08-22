package de.fraunhofer.iosb.ilt.ams.api.datafetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import de.fraunhofer.iosb.ilt.ams.model.Capability;
import de.fraunhofer.iosb.ilt.ams.model.filter.CapabilityFilter;
import de.fraunhofer.iosb.ilt.ams.model.input.CapabilityInput;
import de.fraunhofer.iosb.ilt.ams.model.input.ProcessInput;
import de.fraunhofer.iosb.ilt.ams.model.input.PropertyInput;
import de.fraunhofer.iosb.ilt.ams.model.input.SemanticReferenceInput;
import de.fraunhofer.iosb.ilt.ams.model.response.CapabilityResponse;
import de.fraunhofer.iosb.ilt.ams.model.response.ProcessResponse;
import de.fraunhofer.iosb.ilt.ams.model.response.PropertyResponse;
import de.fraunhofer.iosb.ilt.ams.model.response.SemanticReferenceResponse;
import de.fraunhofer.iosb.ilt.ams.repository.CapabilityRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

@DgsComponent
public class CapabilityDatafetcher {

    @Autowired CapabilityRepository capabilityRepository;

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'capability')")
    @DgsQuery
    public List<Capability> capability(@InputArgument CapabilityFilter filter) {
        return capabilityRepository.getCapabilities(filter);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'capability')")
    @DgsQuery
    public List<Capability> getAllCapabilities(@InputArgument CapabilityFilter filter) {
        return capabilityRepository.getCapabilities(filter);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'capability')")
    @DgsMutation
    public CapabilityResponse createCapability(
            @InputArgument CapabilityInput capability,
            @InputArgument List<String> parentCapabilityIds,
            @InputArgument List<String> childCapabilityIds) {
        return capabilityRepository.createCapability(
                capability, parentCapabilityIds, childCapabilityIds);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'capability')")
    @DgsMutation
    public CapabilityResponse updateCapability(
            @InputArgument String capabilityId,
            @InputArgument CapabilityInput capability,
            @InputArgument List<String> parentCapabilityIds,
            @InputArgument List<String> childCapabilityIds) {
        return capabilityRepository.updateCapability(
                capabilityId, capability, parentCapabilityIds, childCapabilityIds);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'capability')")
    @DgsMutation
    public CapabilityResponse deleteCapability(
            @InputArgument String capabilityId, @InputArgument Boolean deleteChildren) {
        return capabilityRepository.deleteCapability(capabilityId, deleteChildren);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'capability')")
    @DgsMutation
    public CapabilityResponse addPropertyToCapability(
            @InputArgument String propertyId, @InputArgument String capabilityId) {
        return capabilityRepository.addPropertyToCapability(propertyId, capabilityId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'capability')")
    @DgsMutation
    public PropertyResponse createPropertyForCapability(
            @InputArgument PropertyInput property, @InputArgument String capabilityId) {
        return capabilityRepository.createPropertyForCapability(property, capabilityId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'capability')")
    @DgsMutation
    public CapabilityResponse removePropertyFromCapability(
            @InputArgument String propertyId, @InputArgument String capabilityId) {
        return capabilityRepository.removePropertyFromCapability(propertyId, capabilityId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'capability')")
    @DgsMutation
    public CapabilityResponse addProcessToCapability(
            @InputArgument String processId, @InputArgument String capabilityId) {
        return capabilityRepository.addProcessToCapability(processId, capabilityId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'capability')")
    @DgsMutation
    public ProcessResponse createProcessForCapability(
            @InputArgument ProcessInput process, @InputArgument String capabilityId) {
        return capabilityRepository.createProcessForCapability(process, capabilityId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'capability')")
    @DgsMutation
    public CapabilityResponse removeProcessFromCapability(
            @InputArgument String processId, @InputArgument String capabilityId) {
        return capabilityRepository.removeProcessFromCapability(processId, capabilityId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'capability')")
    @DgsMutation
    public CapabilityResponse addSemanticReferenceToCapability(
            @InputArgument String semanticReferenceId, @InputArgument String capabilityId) {
        return capabilityRepository.addSemanticReferenceToCapability(
                semanticReferenceId, capabilityId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'capability')")
    @DgsMutation
    public SemanticReferenceResponse createSemanticReferenceForCapability(
            @InputArgument SemanticReferenceInput semanticReference,
            @InputArgument String capabilityId) {
        return capabilityRepository.createSemanticReferenceForCapability(
                semanticReference, capabilityId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'capability')")
    @DgsMutation
    public CapabilityResponse removeSemanticReferenceFromCapability(
            @InputArgument String semanticReferenceId, @InputArgument String capabilityId) {
        return capabilityRepository.removeSemanticReferenceFromCapability(
                semanticReferenceId, capabilityId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'capability')")
    @DgsMutation
    public CapabilityResponse addProductionResourceToCapability(
            @InputArgument String productionResourceId, @InputArgument String capabilityId) {
        return capabilityRepository.addProductionResourceToCapability(
                productionResourceId, capabilityId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'capability')")
    @DgsMutation
    public CapabilityResponse removeProductionResourceFromCapability(
            @InputArgument String productionResourceId, @InputArgument String capabilityId) {
        return capabilityRepository.removeProductionResourceFromCapability(
                productionResourceId, capabilityId);
    }
}
