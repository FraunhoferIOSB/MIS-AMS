package de.fraunhofer.iosb.ilt.ams.api.datafetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import de.fraunhofer.iosb.ilt.ams.model.SupplyChain;
import de.fraunhofer.iosb.ilt.ams.model.filter.SupplyChainFilter;
import de.fraunhofer.iosb.ilt.ams.model.input.SupplyChainElementInput;
import de.fraunhofer.iosb.ilt.ams.model.input.SupplyChainInput;
import de.fraunhofer.iosb.ilt.ams.model.response.SupplyChainElementResponse;
import de.fraunhofer.iosb.ilt.ams.model.response.SupplyChainResponse;
import de.fraunhofer.iosb.ilt.ams.repository.SupplyChainRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

@DgsComponent
public class SupplyChainDatafetcher {
    @Autowired SupplyChainRepository supplyChainRepository;

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'supplyChain')")
    @DgsQuery
    public List<SupplyChain> supplyChain(@InputArgument SupplyChainFilter filter) {
        return supplyChainRepository.getSupplyChains(filter);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'supplyChain')")
    @DgsMutation
    public SupplyChainResponse createSupplyChain(@InputArgument SupplyChainInput supplyChain) {
        return supplyChainRepository.createSupplyChain(supplyChain);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'supplyChain')")
    @DgsMutation
    public SupplyChainResponse updateSupplyChain(
            @InputArgument String id, @InputArgument SupplyChainInput supplyChain) {
        return supplyChainRepository.updateSupplyChain(id, supplyChain);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'supplyChain')")
    @DgsMutation
    public SupplyChainResponse deleteSupplyChain(@InputArgument String id) {
        return supplyChainRepository.deleteSupplyChain(id);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'supplyChain')")
    @DgsMutation
    public SupplyChainResponse addSupplyChainElementToSupplyChain(
            @InputArgument String supplyChainElementId, @InputArgument String supplyChainId) {
        return supplyChainRepository.addSupplyChainElementToSupplyChain(
                supplyChainElementId, supplyChainId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'supplyChain')")
    @DgsMutation
    public SupplyChainElementResponse createSupplyChainElementForSupplyChain(
            @InputArgument SupplyChainElementInput supplyChainElement,
            @InputArgument String supplyChainId) {
        return supplyChainRepository.createSupplyChainElementForSupplyChain(
                supplyChainElement, supplyChainId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'supplyChain')")
    @DgsMutation
    public SupplyChainResponse removeSupplyChainElementFromSupplyChain(
            @InputArgument String supplyChainElementId, @InputArgument String supplyChainId) {
        return supplyChainRepository.removeSupplyChainElementFromSupplyChain(
                supplyChainElementId, supplyChainId);
    }
}
