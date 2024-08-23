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
import de.fraunhofer.iosb.ilt.ams.model.SupplyChainElement;
import de.fraunhofer.iosb.ilt.ams.model.filter.SupplyChainElementFilter;
import de.fraunhofer.iosb.ilt.ams.model.input.SupplyChainElementInput;
import de.fraunhofer.iosb.ilt.ams.model.response.SupplyChainElementResponse;
import de.fraunhofer.iosb.ilt.ams.repository.SupplyChainElementRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

@DgsComponent
public class SupplyChainElementDatafetcher {

    @Autowired SupplyChainElementRepository supplyChainElementRepository;

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'supplyChainElement')")
    @DgsQuery
    public List<SupplyChainElement> supplyChainElement(
            @InputArgument SupplyChainElementFilter filter) {
        return supplyChainElementRepository.getSupplyChainElements(filter);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'supplyChainElement')")
    @DgsMutation
    public SupplyChainElementResponse createSupplyChainElement(
            @InputArgument SupplyChainElementInput supplyChainElement,
            @InputArgument List<String> parentSupplyChainElementIds,
            @InputArgument List<String> childSupplyChainElementIds) {
        return supplyChainElementRepository.createSupplyChainElement(
                supplyChainElement, parentSupplyChainElementIds, childSupplyChainElementIds);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'supplyChainElement')")
    @DgsMutation
    public SupplyChainElementResponse updateSupplyChainElement(
            @InputArgument String id,
            @InputArgument SupplyChainElementInput supplyChainElement,
            @InputArgument List<String> parentSupplyChainElementIds,
            @InputArgument List<String> childSupplyChainElementIds) {
        return supplyChainElementRepository.updateSupplyChainElement(
                id, supplyChainElement, parentSupplyChainElementIds, childSupplyChainElementIds);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'supplyChainElement')")
    @DgsMutation
    public SupplyChainElementResponse deleteSupplyChainElement(@InputArgument String id) {
        return supplyChainElementRepository.deleteSupplyChainElement(id);
    }
}
