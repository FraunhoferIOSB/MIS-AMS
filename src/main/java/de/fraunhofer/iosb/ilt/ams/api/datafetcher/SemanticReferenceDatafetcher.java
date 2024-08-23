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
import de.fraunhofer.iosb.ilt.ams.model.SemanticReference;
import de.fraunhofer.iosb.ilt.ams.model.filter.SemanticReferenceFilter;
import de.fraunhofer.iosb.ilt.ams.model.input.SemanticReferenceInput;
import de.fraunhofer.iosb.ilt.ams.model.response.SemanticReferenceResponse;
import de.fraunhofer.iosb.ilt.ams.repository.SemanticReferenceRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

@DgsComponent
public class SemanticReferenceDatafetcher {

    @Autowired SemanticReferenceRepository semanticReferenceRepository;

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'semanticReference')")
    @DgsQuery
    public List<SemanticReference> semanticReference(
            @InputArgument SemanticReferenceFilter filter) {
        return semanticReferenceRepository.getSemanticReferences(filter);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'semanticReference')")
    @DgsQuery
    public List<SemanticReference> getAllSemanticReferences(
            @InputArgument SemanticReferenceFilter filter) {
        return semanticReferenceRepository.getSemanticReferences(filter);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'semanticReference')")
    @DgsMutation
    public SemanticReferenceResponse createSemanticReference(
            @InputArgument SemanticReferenceInput semanticReference) {
        return semanticReferenceRepository.createSemanticReference(semanticReference);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'semanticReference')")
    @DgsMutation
    public SemanticReferenceResponse updateSemanticReference(
            @InputArgument String semanticReferenceId,
            @InputArgument SemanticReferenceInput semanticReference) {
        return semanticReferenceRepository.updateSemanticReference(
                semanticReferenceId, semanticReference);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'semanticReference')")
    @DgsMutation
    public SemanticReferenceResponse deleteSemanticReference(
            @InputArgument String semanticReferenceId) {
        return semanticReferenceRepository.deleteSemanticReference(semanticReferenceId);
    }
}
