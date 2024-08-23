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
import de.fraunhofer.iosb.ilt.ams.model.Enterprise;
import de.fraunhofer.iosb.ilt.ams.model.filter.EnterpriseFilter;
import de.fraunhofer.iosb.ilt.ams.model.input.EnterpriseInput;
import de.fraunhofer.iosb.ilt.ams.model.input.FactoryInput;
import de.fraunhofer.iosb.ilt.ams.model.input.ProductInput;
import de.fraunhofer.iosb.ilt.ams.model.input.PropertyInput;
import de.fraunhofer.iosb.ilt.ams.model.response.*;
import de.fraunhofer.iosb.ilt.ams.repository.EnterpriseRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

@DgsComponent
public class EnterpriseDatafetcher {

    @Autowired EnterpriseRepository enterpriseRepository;

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'enterprise')")
    @DgsQuery
    public List<Enterprise> enterprise(@InputArgument EnterpriseFilter filter) {
        return enterpriseRepository.getEnterprises(filter);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'enterprise')")
    @DgsMutation
    public EnterpriseResponse createEnterprise(@InputArgument EnterpriseInput enterprise) {
        return enterpriseRepository.createEnterprise(enterprise);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'enterprise')")
    @DgsMutation
    public EnterpriseResponse updateEnterprise(
            @InputArgument String id, @InputArgument EnterpriseInput enterprise) {
        return enterpriseRepository.updateEnterprise(id, enterprise);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'enterprise')")
    @DgsMutation
    public EnterpriseResponse deleteEnterprise(@InputArgument String id) {
        return enterpriseRepository.deleteEnterprise(id);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'enterprise')")
    @DgsMutation
    public EnterpriseResponse bulkDeleteEnterprise(@InputArgument String id) {
        return enterpriseRepository.bulkDeleteEnterprise(id);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'enterprise')")
    @DgsMutation
    public EnterpriseResponse addFactoryToEnterprise(
            @InputArgument String factoryId, @InputArgument String enterpriseId) {
        return enterpriseRepository.addFactoryToEnterprise(factoryId, enterpriseId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'enterprise')")
    @DgsMutation
    public FactoryResponse createFactoryForEnterprise(
            @InputArgument FactoryInput factory, @InputArgument String enterpriseId) {
        return enterpriseRepository.createFactoryForEnterprise(factory, enterpriseId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'enterprise')")
    @DgsMutation
    public EnterpriseResponse removeFactoryFromEnterprise(
            @InputArgument String factoryId, @InputArgument String enterpriseId) {
        return enterpriseRepository.removeFactoryFromEnterprise(factoryId, enterpriseId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'enterprise')")
    @DgsMutation
    public EnterpriseResponse addPropertyToEnterprise(
            @InputArgument String propertyId, @InputArgument String enterpriseId) {
        return enterpriseRepository.addPropertyToEnterprise(propertyId, enterpriseId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'enterprise')")
    @DgsMutation
    public PropertyResponse createPropertyForEnterprise(
            @InputArgument PropertyInput property, @InputArgument String enterpriseId) {
        return enterpriseRepository.createPropertyForEnterprise(property, enterpriseId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'enterprise')")
    @DgsMutation
    public EnterpriseResponse removePropertyFromEnterprise(
            @InputArgument String propertyId, @InputArgument String enterpriseId) {
        return enterpriseRepository.removePropertyFromEnterprise(propertyId, enterpriseId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'enterprise')")
    @DgsMutation
    public EnterpriseResponse addSubsidiaryEnterprise(
            @InputArgument String subsidiaryId, @InputArgument String enterpriseId) {
        return enterpriseRepository.addSubsidiaryEnterpriseToEnterprise(subsidiaryId, enterpriseId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'enterprise')")
    @DgsMutation
    public EnterpriseResponse createSubsidiaryForEnterprise(
            @InputArgument EnterpriseInput subsidiary, @InputArgument String enterpriseId) {
        return enterpriseRepository.createSubsidiaryEnterpriseForEnterprise(
                subsidiary, enterpriseId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'enterprise')")
    @DgsMutation
    public EnterpriseResponse removeSubsidiaryFromEnterprise(
            @InputArgument String subsidiaryId, @InputArgument String enterpriseId) {
        return enterpriseRepository.removeSubsidiaryEnterpriseFromEnterprise(
                subsidiaryId, enterpriseId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'enterprise')")
    @DgsMutation
    public EnterpriseResponse addProductToEnterprise(
            @InputArgument String productId, @InputArgument String enterpriseId) {
        return enterpriseRepository.addProductToEnterprise(productId, enterpriseId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'enterprise')")
    @DgsMutation
    public ProductResponse createProductForEnterprise(
            @InputArgument ProductInput product, @InputArgument String enterpriseId) {
        return enterpriseRepository.createProductForEnterprise(product, enterpriseId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'enterprise')")
    @DgsMutation
    public EnterpriseResponse removeProductFromEnterprise(
            @InputArgument String productId, @InputArgument String enterpriseId) {
        return enterpriseRepository.removeProductFromEnterprise(productId, enterpriseId);
    }
}
