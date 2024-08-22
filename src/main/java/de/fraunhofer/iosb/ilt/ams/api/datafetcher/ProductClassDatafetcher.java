package de.fraunhofer.iosb.ilt.ams.api.datafetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import de.fraunhofer.iosb.ilt.ams.model.ProductClass;
import de.fraunhofer.iosb.ilt.ams.model.filter.ProductClassFilter;
import de.fraunhofer.iosb.ilt.ams.model.input.ProductClassInput;
import de.fraunhofer.iosb.ilt.ams.model.input.SemanticReferenceInput;
import de.fraunhofer.iosb.ilt.ams.model.response.ProductClassResponse;
import de.fraunhofer.iosb.ilt.ams.model.response.SemanticReferenceResponse;
import de.fraunhofer.iosb.ilt.ams.repository.ProductClassRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

@DgsComponent
public class ProductClassDatafetcher {

    @Autowired ProductClassRepository productClassRepository;

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'productClass')")
    @DgsQuery
    public List<ProductClass> productClass(@InputArgument ProductClassFilter filter) {
        return productClassRepository.getProductClasses(filter);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'productClass')")
    @DgsQuery
    public List<ProductClass> getAllProductClasses(@InputArgument ProductClassFilter filter) {
        return productClassRepository.getProductClasses(filter);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'productClass')")
    @DgsMutation
    public ProductClassResponse createProductClass(
            @InputArgument ProductClassInput productClass,
            @InputArgument List<String> parentProductClassIds,
            @InputArgument List<String> childProductClassIds) {
        return productClassRepository.createProductClass(
                productClass, parentProductClassIds, childProductClassIds);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'productClass')")
    @DgsMutation
    public ProductClassResponse updateProductClass(
            @InputArgument String productClassId,
            @InputArgument ProductClassInput productClass,
            @InputArgument List<String> parentProductClassIds,
            @InputArgument List<String> childProductClassIds) {
        return productClassRepository.updateProductClass(
                productClassId, productClass, parentProductClassIds, childProductClassIds);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'productClass')")
    @DgsMutation
    public ProductClassResponse deleteProductClass(
            @InputArgument String productClassId, @InputArgument Boolean deleteChildren) {
        return productClassRepository.deleteProductClass(productClassId, deleteChildren);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'productClass')")
    @DgsMutation
    public ProductClassResponse addSemanticReferenceToProductClass(
            @InputArgument String semanticReferenceId, @InputArgument String productClassId) {
        return productClassRepository.addSemanticReferenceToProductClass(
                semanticReferenceId, productClassId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'productClass')")
    @DgsMutation
    public ProductClassResponse removeSemanticReferenceFromProductClass(
            @InputArgument String semanticReferenceId, @InputArgument String productClassId) {
        return productClassRepository.removeSemanticReferenceFromProductClass(
                semanticReferenceId, productClassId);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'productClass')")
    @DgsMutation
    public SemanticReferenceResponse createSemanticReferenceForProductClass(
            @InputArgument SemanticReferenceInput semanticReference,
            @InputArgument String productClassId) {
        return productClassRepository.createSemanticReferenceForProductClass(
                semanticReference, productClassId);
    }
}
