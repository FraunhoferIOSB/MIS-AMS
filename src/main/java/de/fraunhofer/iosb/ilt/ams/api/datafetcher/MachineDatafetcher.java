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
import de.fraunhofer.iosb.ilt.ams.model.Machine;
import de.fraunhofer.iosb.ilt.ams.model.filter.MachineFilter;
import de.fraunhofer.iosb.ilt.ams.model.input.MachineInput;
import de.fraunhofer.iosb.ilt.ams.model.input.PropertyInput;
import de.fraunhofer.iosb.ilt.ams.model.response.MachineResponse;
import de.fraunhofer.iosb.ilt.ams.model.response.PropertyResponse;
import de.fraunhofer.iosb.ilt.ams.repository.ProductionResourceRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

@DgsComponent
public class MachineDatafetcher {

    @Autowired ProductionResourceRepository productionResourceRepository;

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'machine')")
    @DgsQuery
    public List<Machine> machine(@InputArgument MachineFilter filter) {
        return productionResourceRepository.getMachines(filter);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'machine')")
    @DgsMutation
    public MachineResponse createMachine(@InputArgument MachineInput machine) {
        return productionResourceRepository.createMachine(machine);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'machine')")
    @DgsMutation
    public MachineResponse updateMachine(
            @InputArgument String machineId, @InputArgument MachineInput machine) {
        return productionResourceRepository.updateMachine(machineId, machine);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'machine')")
    @DgsMutation
    public MachineResponse deleteMachine(@InputArgument String machineId) {
        return productionResourceRepository.deleteMachine(machineId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'machine')")
    @DgsMutation
    public MachineResponse addPropertyToMachine(
            @InputArgument String propertyId, @InputArgument String machineId) {
        return productionResourceRepository.addPropertyToMachine(propertyId, machineId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'machine')")
    @DgsMutation
    public PropertyResponse createPropertyForMachine(
            @InputArgument PropertyInput property, @InputArgument String machineId) {
        return productionResourceRepository.createPropertyForMachine(property, machineId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'machine')")
    @DgsMutation
    public MachineResponse removePropertyFromMachine(
            @InputArgument String propertyId, @InputArgument String machineId) {
        return productionResourceRepository.removePropertyFromMachine(propertyId, machineId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'machine')")
    @DgsMutation
    public MachineResponse addProcessToMachine(
            @InputArgument String processId, @InputArgument String machineId) {
        return productionResourceRepository.addProcessToMachine(processId, machineId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'machine')")
    @DgsMutation
    public MachineResponse removeProcessFromMachine(
            @InputArgument String processId, @InputArgument String machineId) {
        return productionResourceRepository.removeProcessFromMachine(processId, machineId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'machine')")
    @DgsMutation
    public MachineResponse addCapabilityToMachine(
            @InputArgument String capabilityId, @InputArgument String machineId) {
        return productionResourceRepository.addCapabilityToMachine(capabilityId, machineId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'machine')")
    @DgsMutation
    public MachineResponse removeCapabilityFromMachine(
            @InputArgument String capabilityId, @InputArgument String machineId) {
        return productionResourceRepository.removeCapabilityFromMachine(capabilityId, machineId);
    }
}
