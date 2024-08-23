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
import de.fraunhofer.iosb.ilt.ams.model.InputProductType;
import de.fraunhofer.iosb.ilt.ams.model.OutputProductType;
import de.fraunhofer.iosb.ilt.ams.model.Process;
import de.fraunhofer.iosb.ilt.ams.model.filter.ProcessFilter;
import de.fraunhofer.iosb.ilt.ams.model.input.ProcessInput;
import de.fraunhofer.iosb.ilt.ams.model.input.PropertyInput;
import de.fraunhofer.iosb.ilt.ams.model.response.ProcessResponse;
import de.fraunhofer.iosb.ilt.ams.model.response.PropertyResponse;
import de.fraunhofer.iosb.ilt.ams.repository.ProcessRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

@DgsComponent
public class ProcessDatafetcher {

    @Autowired ProcessRepository processRepository;

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'process')")
    @DgsQuery
    public List<Process> process(@InputArgument ProcessFilter filter) {
        return processRepository.getProcesses(filter);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'process')")
    @DgsMutation
    public ProcessResponse createProcess(
            @InputArgument ProcessInput process,
            @InputArgument List<String> parentProcessIds,
            @InputArgument List<String> childProcessIds) {
        return this.processRepository.createProcess(process, parentProcessIds, childProcessIds);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'process')")
    @DgsMutation
    public ProcessResponse updateProcess(
            @InputArgument String processId,
            @InputArgument ProcessInput process,
            @InputArgument List<String> parentProcessIds,
            @InputArgument List<String> childProcessIds) {
        return this.processRepository.updateProcess(
                processId, process, parentProcessIds, childProcessIds);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'process')")
    @DgsMutation
    public ProcessResponse deleteProcess(
            @InputArgument String processId, @InputArgument Boolean deleteChildren) {
        return this.processRepository.deleteProcess(processId, deleteChildren);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'process')")
    @DgsMutation
    public ProcessResponse addRealizedCapabilityToProcess(
            @InputArgument String capabilityId, @InputArgument String processId) {
        return this.processRepository.addRealizedCapabilityToProcess(capabilityId, processId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'process')")
    @DgsMutation
    public ProcessResponse removeRealizedCapabilityFromProcess(
            @InputArgument String capabilityId, @InputArgument String processId) {
        return this.processRepository.removeRealizedCapabilityFromProcess(capabilityId, processId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'process')")
    @DgsMutation
    public ProcessResponse addRequiredCapabilityToProcess(
            @InputArgument String capabilityId, @InputArgument String processId) {
        return this.processRepository.addRequiredCapabilityToProcess(capabilityId, processId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'process')")
    @DgsMutation
    public ProcessResponse removeRequiredCapabilityFromProcess(
            @InputArgument String capabilityId, @InputArgument String processId) {
        return this.processRepository.removeRequiredCapabilityFromProcess(capabilityId, processId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'process')")
    @DgsMutation
    public ProcessResponse addProductionResourceToProcess(
            @InputArgument String productionResourceId, @InputArgument String processId) {
        return this.processRepository.addProductionResourceToProcess(
                productionResourceId, processId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'process')")
    @DgsMutation
    public ProcessResponse removeProductionResourceFromProcess(
            @InputArgument String productionResourceId, @InputArgument String processId) {
        return this.processRepository.removeProductionResourceFromProcess(
                productionResourceId, processId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'process')")
    @DgsMutation
    public ProcessResponse addInputProductToProcess(
            @InputArgument String productId,
            @InputArgument InputProductType type,
            @InputArgument String processId) {
        return this.processRepository.addInputProductToProcess(productId, type, processId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'process')")
    @DgsMutation
    public ProcessResponse removeInputProductFromProcess(
            @InputArgument String productId,
            @InputArgument InputProductType type,
            @InputArgument String processId) {
        return this.processRepository.removeInputProductFromProcess(productId, type, processId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'process')")
    @DgsMutation
    public ProcessResponse addOutputProductToProcess(
            @InputArgument String productId,
            @InputArgument OutputProductType type,
            @InputArgument String processId) {
        return this.processRepository.addOutputProductToProcess(productId, type, processId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'process')")
    @DgsMutation
    public ProcessResponse removeOutputProductFromProcess(
            @InputArgument String productId,
            @InputArgument OutputProductType type,
            @InputArgument String processId) {
        return this.processRepository.removeOutputProductFromProcess(productId, type, processId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'process')")
    @DgsMutation
    public ProcessResponse addPropertyToProcess(
            @InputArgument String propertyId, @InputArgument String processId) {
        return this.processRepository.addPropertyToProcess(propertyId, processId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'process')")
    @DgsMutation
    public PropertyResponse createPropertyForProcess(
            @InputArgument PropertyInput property, @InputArgument String processId) {
        return this.processRepository.createPropertyForProcess(property, processId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'process')")
    @DgsMutation
    public ProcessResponse removePropertyFromProcess(
            @InputArgument String propertyId, @InputArgument String processId) {
        return this.processRepository.removePropertyFromProcess(propertyId, processId);
    }
}
