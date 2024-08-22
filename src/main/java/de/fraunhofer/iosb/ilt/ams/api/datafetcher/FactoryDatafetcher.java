package de.fraunhofer.iosb.ilt.ams.api.datafetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import de.fraunhofer.iosb.ilt.ams.model.Factory;
import de.fraunhofer.iosb.ilt.ams.model.filter.FactoryFilter;
import de.fraunhofer.iosb.ilt.ams.model.input.*;
import de.fraunhofer.iosb.ilt.ams.model.response.*;
import de.fraunhofer.iosb.ilt.ams.repository.FactoryRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

@DgsComponent
public class FactoryDatafetcher {

    @Autowired FactoryRepository factoryRepository;

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'factory')")
    @DgsQuery
    public List<Factory> factory(@InputArgument FactoryFilter filter) {
        return factoryRepository.getFactories(filter);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'factory')")
    @DgsMutation
    public FactoryResponse createFactory(@InputArgument FactoryInput factory) {
        return factoryRepository.createFactory(factory);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'factory')")
    @DgsMutation
    public FactoryResponse updateFactory(
            @InputArgument String id, @InputArgument FactoryInput factory) {
        return factoryRepository.updateFactory(id, factory);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'factory')")
    @DgsMutation
    public FactoryResponse deleteFactory(@InputArgument String id) {
        return factoryRepository.deleteFactory(id);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'factory')")
    @DgsMutation
    public FactoryResponse addPropertyToFactory(
            @InputArgument String propertyId, @InputArgument String factoryId) {
        return factoryRepository.addPropertyToFactory(propertyId, factoryId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'factory')")
    @DgsMutation
    public PropertyResponse createPropertyForFactory(
            @InputArgument PropertyInput property, @InputArgument String factoryId) {
        return factoryRepository.createPropertyForFactory(property, factoryId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'factory')")
    @DgsMutation
    public FactoryResponse removePropertyFromFactory(
            @InputArgument String propertyId, @InputArgument String factoryId) {
        return factoryRepository.removePropertyFromFactory(propertyId, factoryId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'factory')")
    @DgsMutation
    public FactoryResponse addProductToFactory(
            @InputArgument String productId, @InputArgument String factoryId) {
        return factoryRepository.addProductToFactory(productId, factoryId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'factory')")
    @DgsMutation
    public ProductResponse createProductForFactory(
            @InputArgument ProductInput product, @InputArgument String factoryId) {
        return factoryRepository.createProductForFactory(product, factoryId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'factory')")
    @DgsMutation
    public FactoryResponse removeProductFromFactory(
            @InputArgument String productId, @InputArgument String factoryId) {
        return factoryRepository.removeProductFromFactory(productId, factoryId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'factory')")
    @DgsMutation
    public FactoryResponse addMachineToFactory(
            @InputArgument String machineId, @InputArgument String factoryId) {
        return factoryRepository.addMachineToFactory(machineId, factoryId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'factory')")
    @DgsMutation
    public MachineResponse createMachineForFactory(
            @InputArgument MachineInput machine, @InputArgument String factoryId) {
        return factoryRepository.createMachineForFactory(machine, factoryId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'factory')")
    @DgsMutation
    public FactoryResponse removeMachineFromFactory(
            @InputArgument String machineId, @InputArgument String factoryId) {
        return factoryRepository.removeMachineFromFactory(machineId, factoryId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'factory')")
    @DgsMutation
    public FactoryResponse addHumanResourceToFactory(
            @InputArgument String humanResourceId, @InputArgument String factoryId) {
        return factoryRepository.addHumanResourceToFactory(humanResourceId, factoryId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'factory')")
    @DgsMutation
    public HumanResourceResponse createHumanResourceForFactory(
            @InputArgument HumanResourceInput humanResource, @InputArgument String factoryId) {
        return factoryRepository.createHumanResourceForFactory(humanResource, factoryId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'factory')")
    @DgsMutation
    public FactoryResponse removeHumanResourceFromFactory(
            @InputArgument String humanResourceId, @InputArgument String factoryId) {
        return factoryRepository.removeHumanResourceFromFactory(humanResourceId, factoryId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'factory')")
    @DgsMutation
    public FactoryResponse addProcessToFactory(
            @InputArgument String processId, @InputArgument String factoryId) {
        return factoryRepository.addProcessToFactory(processId, factoryId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'factory')")
    @DgsMutation
    public ProcessResponse createProcessForFactory(
            @InputArgument ProcessInput process, @InputArgument String factoryId) {
        return factoryRepository.createProcessForFactory(process, factoryId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'factory')")
    @DgsMutation
    public FactoryResponse removeProcessFromFactory(
            @InputArgument String processId, @InputArgument String factoryId) {
        return factoryRepository.removeProcessFromFactory(processId, factoryId);
    }
}
