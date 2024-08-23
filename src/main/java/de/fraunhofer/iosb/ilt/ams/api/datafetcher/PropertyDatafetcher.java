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
import de.fraunhofer.iosb.ilt.ams.model.Property;
import de.fraunhofer.iosb.ilt.ams.model.filter.PropertyFilter;
import de.fraunhofer.iosb.ilt.ams.model.input.PropertyInput;
import de.fraunhofer.iosb.ilt.ams.model.input.SemanticReferenceInput;
import de.fraunhofer.iosb.ilt.ams.model.response.PropertyResponse;
import de.fraunhofer.iosb.ilt.ams.model.response.SemanticReferenceResponse;
import de.fraunhofer.iosb.ilt.ams.repository.PropertyRepository;
import de.fraunhofer.iosb.ilt.ams.utility.MESSAGE;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

@DgsComponent
public class PropertyDatafetcher {

    @Autowired PropertyRepository propertyRepository;

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'property')")
    @DgsQuery
    public List<Property> property(@InputArgument PropertyFilter filter) {
        return propertyRepository.getProperties(filter);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'property')")
    @DgsMutation
    public PropertyResponse createProperty(@InputArgument PropertyInput property) {
        if (property.getSemanticReferences() == null
                && (property.getDescription() == null || property.getLabel() == null)) {
            throw new IllegalArgumentException(MESSAGE.NOT_ENOUGH_ARGUMENTS_GENERIC);
        }
        if ((property.getSemanticReferences() != null && property.getSemanticReferences().isEmpty())
                && (property.getDescription().isBlank() || property.getLabel().isBlank())) {
            throw new IllegalArgumentException(MESSAGE.NOT_ENOUGH_ARGUMENTS_GENERIC);
        }
        if (property.getLabel() != null && property.getLabelLanguageCode() == null) {
            throw new IllegalArgumentException(
                    String.format(MESSAGE.LANGUAGE_ERROR, property.getLabel()));
        }
        if (property.getDescription() != null && property.getDescriptionLanguageCode() == null) {
            throw new IllegalArgumentException(
                    String.format(MESSAGE.LANGUAGE_ERROR, property.getDescription()));
        }
        return propertyRepository.createProperty(property);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'property')")
    @DgsMutation
    public PropertyResponse updateProperty(
            @InputArgument String propertyId, @InputArgument PropertyInput property) {
        if (property.getSemanticReferences() == null
                && (property.getDescription() == null || property.getLabel() == null)) {
            throw new IllegalArgumentException(
                    String.format(MESSAGE.NOT_ENOUGH_ARGUMENTS, propertyId));
        }
        if ((property.getSemanticReferences() != null && property.getSemanticReferences().isEmpty())
                && (property.getDescription().isBlank() || property.getLabel().isBlank())) {
            throw new IllegalArgumentException(
                    String.format(MESSAGE.NOT_ENOUGH_ARGUMENTS, propertyId));
        }
        if (property.getLabel() != null && property.getLabelLanguageCode() == null) {
            throw new IllegalArgumentException(
                    String.format(MESSAGE.LANGUAGE_ERROR, property.getLabel()));
        }
        if (property.getDescription() != null && property.getDescriptionLanguageCode() == null) {
            throw new IllegalArgumentException(
                    String.format(MESSAGE.LANGUAGE_ERROR, property.getDescription()));
        }
        return propertyRepository.updateProperty(propertyId, property);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'property')")
    @DgsMutation
    public PropertyResponse deleteProperty(@InputArgument String propertyId) {
        return propertyRepository.deleteProperty(propertyId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'property')")
    @DgsMutation
    public PropertyResponse addSemanticReferenceToProperty(
            @InputArgument String semanticReferenceId, @InputArgument String propertyId) {
        return propertyRepository.addSemanticReferenceToProperty(semanticReferenceId, propertyId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'property')")
    @DgsMutation
    public SemanticReferenceResponse createSemanticReferenceForProperty(
            @InputArgument SemanticReferenceInput semanticReference,
            @InputArgument String propertyId) {
        return propertyRepository.createSemanticReferenceForProperty(semanticReference, propertyId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'property')")
    @DgsMutation
    public PropertyResponse removeSemanticReferenceFromProperty(
            @InputArgument String semanticReferenceId, @InputArgument String propertyId) {
        return propertyRepository.removeSemanticReferenceFromProperty(
                semanticReferenceId, propertyId);
    }
}
