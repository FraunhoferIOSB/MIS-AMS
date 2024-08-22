package de.fraunhofer.iosb.ilt.ams.repository;

import de.fraunhofer.iosb.ilt.ams.dao.ProductPassportDAO;
import de.fraunhofer.iosb.ilt.ams.model.ObjectRdf4jRepository;
import de.fraunhofer.iosb.ilt.ams.model.ProductPassport;
import de.fraunhofer.iosb.ilt.ams.model.filter.ProductPassportFilter;
import de.fraunhofer.iosb.ilt.ams.model.input.ProductPassportInput;
import de.fraunhofer.iosb.ilt.ams.model.response.ProductPassportResponse;
import de.fraunhofer.iosb.ilt.ams.utility.MESSAGE;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ProductPassportRepository {

    @Autowired ProductPassportDAO productPassportDAO;

    @Autowired ObjectRdf4jRepository repo;

    public List<ProductPassport> getProductPassports(ProductPassportFilter filter) {
        repo.emptyProcessedIds();
        List<ProductPassport> productPassports =
                productPassportDAO.list().stream().distinct().collect(Collectors.toList());
        if (filter != null) {
            if (filter.getId() != null) {
                productPassports =
                        productPassports.stream()
                                .filter(
                                        productPassport ->
                                                Values.iri(filter.getId())
                                                        .equals(productPassport.getId()))
                                .collect(Collectors.toList());
            }
            if (filter.getSourceId() != null) {
                productPassports =
                        productPassports.stream()
                                .filter(
                                        productPassport ->
                                                filter.getSourceId()
                                                        .equals(productPassport.getSourceId()))
                                .collect(Collectors.toList());
            }
            if (filter.getIdentifier() != null) {
                productPassports =
                        productPassports.stream()
                                .filter(
                                        productPassport ->
                                                filter.getIdentifier()
                                                        .equals(productPassport.getIdentifier()))
                                .collect(Collectors.toList());
            }
        }
        return productPassports;
    }

    public ProductPassportResponse createProductPassport(
            ProductPassportInput productPassportInput) {
        ProductPassportResponse productPassportResponse = new ProductPassportResponse();

        productPassportResponse.setProductPassport(
                repo.createProductPassport(productPassportInput));
        productPassportResponse.setCode(200);
        productPassportResponse.setMessage(MESSAGE.SUCCESS);
        productPassportResponse.setSuccess(true);
        return productPassportResponse;
    }

    public ProductPassportResponse updateProductPassport(
            String id, ProductPassportInput productPassportInput) {
        ProductPassportResponse response = new ProductPassportResponse();
        IRI iri = null;
        try {
            iri = Values.iri(id);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            response.setCode(500);
            return response;
        }
        ProductPassport productPassport = repo.getProductPassportById(iri);
        if (productPassport == null) {
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, iri));
            response.setSuccess(false);
            response.setCode(500);
            return response;
        }
        response.setProductPassport(repo.updateProductPassport(iri, productPassportInput));
        response.setCode(200);
        response.setSuccess(true);
        response.setMessage(MESSAGE.SUCCESS);
        return response;
    }

    public ProductPassportResponse deleteProductPassport(String productPassportId) {
        ProductPassportResponse response = new ProductPassportResponse();
        IRI iri = null;
        try {
            iri = Values.iri(productPassportId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            response.setCode(500);
            return response;
        }
        response.setSuccess(repo.deleteProductPassport(iri));
        response.setCode(200);
        response.setMessage(String.format(MESSAGE.DELETED, iri));
        return response;
    }
}
