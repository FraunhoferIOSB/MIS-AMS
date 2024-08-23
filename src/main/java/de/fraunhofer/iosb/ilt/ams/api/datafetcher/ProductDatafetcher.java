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
import de.fraunhofer.iosb.ilt.ams.model.Product;
import de.fraunhofer.iosb.ilt.ams.model.filter.ProductFilter;
import de.fraunhofer.iosb.ilt.ams.model.input.*;
import de.fraunhofer.iosb.ilt.ams.model.response.*;
import de.fraunhofer.iosb.ilt.ams.repository.ProductRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

@DgsComponent
public class ProductDatafetcher {

    @Autowired ProductRepository productRepository;

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'product')")
    @DgsQuery
    public List<Product> product(@InputArgument ProductFilter filter) {
        return productRepository.getProducts(filter);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'product')")
    @DgsMutation
    public ProductResponse createProduct(@InputArgument ProductInput product) {
        return this.productRepository.createProduct(product);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'product')")
    @DgsMutation
    public ProductResponse updateProduct(
            @InputArgument String productId, @InputArgument ProductInput product) {
        return this.productRepository.updateProduct(productId, product);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'product')")
    @DgsMutation
    public ProductResponse deleteProduct(@InputArgument String productId) {
        return this.productRepository.deleteProduct(productId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'product')")
    @DgsMutation
    public ProductResponse addPropertyToProduct(
            @InputArgument String propertyId, @InputArgument String productId) {
        return this.productRepository.addPropertyToProduct(propertyId, productId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'product')")
    @DgsMutation
    public PropertyResponse createPropertyForProduct(
            @InputArgument PropertyInput property, @InputArgument String productId) {
        return this.productRepository.createPropertyForProduct(property, productId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'product')")
    @DgsMutation
    public ProductResponse removePropertyFromProduct(
            @InputArgument String propertyId, @InputArgument String productId) {
        return this.productRepository.removePropertyFromProduct(propertyId, productId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'product')")
    @DgsMutation
    public ProductResponse addProductClassToProduct(
            @InputArgument String productClassId, @InputArgument String productId) {
        return this.productRepository.addProductClassToProduct(productClassId, productId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'product')")
    @DgsMutation
    public ProductClassResponse createProductClassForProduct(
            @InputArgument ProductClassInput productClass, @InputArgument String productId) {
        return this.productRepository.createProductClassForProduct(productClass, productId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'product')")
    @DgsMutation
    public ProductResponse removeProductClassFromProduct(
            @InputArgument String productClassId, @InputArgument String productId) {
        return this.productRepository.removeProductClassFromProduct(productClassId, productId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'product')")
    @DgsMutation
    public ProductResponse addSubProductToProduct(
            @InputArgument String subProductProductApplicationId, @InputArgument String productId) {
        return this.productRepository.addSubProductToProduct(
                subProductProductApplicationId, productId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'product')")
    @DgsMutation
    public ProductApplicationResponse createSubProductForProduct(
            @InputArgument ProductApplicationInput subProductProductApplication,
            @InputArgument String productId) {
        return this.productRepository.createSubProductForProduct(
                subProductProductApplication, productId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'product')")
    @DgsMutation
    public ProductResponse removeSubProductFromProduct(
            @InputArgument String subProductProductApplicationId, @InputArgument String productId) {
        return this.productRepository.removeSubProductFromProduct(
                subProductProductApplicationId, productId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'product')")
    @DgsMutation
    public ProductResponse addSemanticReferenceToProduct(
            @InputArgument String semanticReferenceId, @InputArgument String productId) {
        return this.productRepository.addSemanticReferenceToProduct(semanticReferenceId, productId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'product')")
    @DgsMutation
    public SemanticReferenceResponse createSemanticReferenceForProduct(
            @InputArgument SemanticReferenceInput semanticReference,
            @InputArgument String productId) {
        return this.productRepository.createSemanticReferenceForProduct(
                semanticReference, productId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'product')")
    @DgsMutation
    public ProductResponse removeSemanticReferenceFromProduct(
            @InputArgument String semanticReferenceId, @InputArgument String productId) {
        return this.productRepository.removeSemanticReferenceFromProduct(
                semanticReferenceId, productId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'product')")
    @DgsMutation
    public ProductResponse addSupplyChainToProduct(
            @InputArgument String supplyChainId, @InputArgument String productId) {
        return this.productRepository.addSupplyChainToProduct(supplyChainId, productId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'product')")
    @DgsMutation
    public SupplyChainResponse createSupplyChainForProduct(
            @InputArgument SupplyChainInput supplyChain, @InputArgument String productId) {
        return this.productRepository.createSupplyChainForProduct(supplyChain, productId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'product')")
    @DgsMutation
    public ProductResponse removeSupplyChainFromProduct(
            @InputArgument String supplyChainId, @InputArgument String productId) {
        return this.productRepository.removeSupplyChainFromProduct(supplyChainId, productId);
    }
}
