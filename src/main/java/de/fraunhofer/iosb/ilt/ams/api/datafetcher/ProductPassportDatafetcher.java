package de.fraunhofer.iosb.ilt.ams.api.datafetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import de.fraunhofer.iosb.ilt.ams.model.ProductPassport;
import de.fraunhofer.iosb.ilt.ams.model.filter.ProductPassportFilter;
import de.fraunhofer.iosb.ilt.ams.model.input.ProductPassportInput;
import de.fraunhofer.iosb.ilt.ams.model.response.ProductPassportResponse;
import de.fraunhofer.iosb.ilt.ams.repository.ProductPassportRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

@DgsComponent
public class ProductPassportDatafetcher {

    @Autowired ProductPassportRepository productPassportRepository;

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'productPassport')")
    @DgsQuery
    public List<ProductPassport> productPassport(@InputArgument ProductPassportFilter filter) {
        return productPassportRepository.getProductPassports(filter);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'productPassport')")
    @DgsMutation
    public ProductPassportResponse createProductPassport(
            @InputArgument ProductPassportInput productPassport) {
        return productPassportRepository.createProductPassport(productPassport);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'productPassport')")
    @DgsMutation
    public ProductPassportResponse updateProductPassport(
            @InputArgument String productPassportId,
            @InputArgument ProductPassportInput productPassport) {
        return productPassportRepository.updateProductPassport(productPassportId, productPassport);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'productPassport')")
    @DgsMutation
    public ProductPassportResponse deleteProductPassport(@InputArgument String productPassportId) {
        return productPassportRepository.deleteProductPassport(productPassportId);
    }
}
