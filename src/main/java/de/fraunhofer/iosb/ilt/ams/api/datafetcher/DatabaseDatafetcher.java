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
