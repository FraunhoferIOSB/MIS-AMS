/*
 * Copyright (c) 2024 Fraunhofer IOSB, eine rechtlich nicht selbstaendige
 * Einrichtung der Fraunhofer-Gesellschaft zur Foerderung der angewandten
 * Forschung e.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
