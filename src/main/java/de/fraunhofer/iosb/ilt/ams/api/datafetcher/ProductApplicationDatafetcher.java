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
import de.fraunhofer.iosb.ilt.ams.model.ProductApplication;
import de.fraunhofer.iosb.ilt.ams.model.filter.ProductApplicationFilter;
import de.fraunhofer.iosb.ilt.ams.model.input.ProductApplicationInput;
import de.fraunhofer.iosb.ilt.ams.model.input.PropertyInput;
import de.fraunhofer.iosb.ilt.ams.model.response.ProductApplicationResponse;
import de.fraunhofer.iosb.ilt.ams.model.response.PropertyResponse;
import de.fraunhofer.iosb.ilt.ams.repository.ProductApplicationRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

@DgsComponent
public class ProductApplicationDatafetcher {

    @Autowired ProductApplicationRepository repository;

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'productApplication')")
    @DgsQuery
    public List<ProductApplication> productApplication(
            @InputArgument ProductApplicationFilter filter) {
        return repository.getProductApplications(filter);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'productApplication')")
    @DgsMutation
    public ProductApplicationResponse createProductApplication(
            @InputArgument ProductApplicationInput productApplication) {
        return this.repository.createProductApplication(productApplication);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'productApplication')")
    @DgsMutation
    public ProductApplicationResponse updateProductApplication(
            @InputArgument String productApplicationId,
            @InputArgument ProductApplicationInput productApplication) {
        return this.repository.updateProductApplication(productApplicationId, productApplication);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'productApplication')")
    @DgsMutation
    public ProductApplicationResponse deleteProductApplication(
            @InputArgument String productApplicationId) {
        return this.repository.deleteProductApplication(productApplicationId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'productApplication')")
    @DgsMutation
    public ProductApplicationResponse addPropertyToProductApplication(
            @InputArgument String propertyId, @InputArgument String productApplicationId) {
        return this.repository.addPropertyToProductApplication(propertyId, productApplicationId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'productApplication')")
    @DgsMutation
    public PropertyResponse createPropertyForProductApplication(
            @InputArgument PropertyInput property, @InputArgument String productApplicationId) {
        return this.repository.createPropertyForProductApplication(property, productApplicationId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'productApplication')")
    @DgsMutation
    public ProductApplicationResponse removePropertyFromProductApplication(
            @InputArgument String propertyId, @InputArgument String productApplicationId) {
        return this.repository.removePropertyFromProductApplication(
                propertyId, productApplicationId);
    }
}
