package de.fraunhofer.iosb.ilt.ams.repository;

import static de.fraunhofer.iosb.ilt.ams.repository.ProductRepository.checkProductForErrors;
import static de.fraunhofer.iosb.ilt.ams.repository.ProductRepository.createAndReturnProduct;
import static de.fraunhofer.iosb.ilt.ams.repository.PropertyRepository.checkPropertyInputForErrors;
import static de.fraunhofer.iosb.ilt.ams.repository.PropertyRepository.createAndReturnProperty;

import de.fraunhofer.iosb.ilt.ams.dao.ProductApplicationDAO;
import de.fraunhofer.iosb.ilt.ams.model.ObjectRdf4jRepository;
import de.fraunhofer.iosb.ilt.ams.model.ProductApplication;
import de.fraunhofer.iosb.ilt.ams.model.Property;
import de.fraunhofer.iosb.ilt.ams.model.filter.ProductApplicationFilter;
import de.fraunhofer.iosb.ilt.ams.model.input.ProductApplicationInput;
import de.fraunhofer.iosb.ilt.ams.model.input.PropertyInput;
import de.fraunhofer.iosb.ilt.ams.model.response.ProductApplicationResponse;
import de.fraunhofer.iosb.ilt.ams.model.response.PropertyResponse;
import de.fraunhofer.iosb.ilt.ams.utility.LoggingHelper;
import de.fraunhofer.iosb.ilt.ams.utility.MESSAGE;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ProductApplicationRepository {

    @Autowired ProductApplicationDAO productApplicationDAO;

    @Autowired ObjectRdf4jRepository repo;

    public List<ProductApplication> getProductApplications(ProductApplicationFilter filter) {
        LoggingHelper.logQuery(
                "getProductApplications", repo.getGraphNameForQuery().getQueryString());
        repo.emptyProcessedIds();
        List<ProductApplication> productApplications =
                productApplicationDAO.list().stream().distinct().collect(Collectors.toList());
        if (filter != null) {
            if (filter.getId() != null) {
                productApplications =
                        productApplications.stream()
                                .filter(pa -> Values.iri(filter.getId()).equals(pa.getId()))
                                .collect(Collectors.toList());
            }
            if (filter.getSourceId() != null) {
                productApplications =
                        productApplications.stream()
                                .filter(pa -> filter.getSourceId().equals(pa.getSourceId()))
                                .collect(Collectors.toList());
            }
            if (filter.getProductId() != null) {
                productApplications =
                        productApplications.stream()
                                .filter(
                                        pa ->
                                                Values.iri(filter.getProductId())
                                                        .equals(
                                                                pa.getProduct() == null
                                                                        ? null
                                                                        : pa.getProduct().getId()))
                                .collect(Collectors.toList());
            }
            if (filter.getProcessId() != null) {
                var process = repo.getProcessById(Values.iri(filter.getProcessId()));
                List<ProductApplication> productApplicationList =
                        new LinkedList<>(process.getAuxiliaryMaterials());
                productApplicationList.addAll(process.getRawMaterials());
                productApplicationList.addAll(process.getPreliminaryProducts());
                productApplicationList.addAll(process.getOperatingMaterials());
                productApplicationList.addAll(process.getByProducts());
                productApplicationList.addAll(process.getEndProducts());
                productApplicationList.addAll(process.getWasteProducts());
                return productApplicationList;
            }
            if (filter.getParentProductId() != null) {
                var parentProduct = repo.getProductById(Values.iri(filter.getParentProductId()));
                return new LinkedList<>(parentProduct.getBillOfMaterials());
            }
        }
        return productApplications;
    }

    public static ProductApplication createAndReturnProductApplication(
            ProductApplicationInput input) {
        checkProductApplicationForErrors(input);
        ProductApplication productApplication = new ProductApplication();
        productApplication.setSourceId(input.getSourceId());
        if (input.getProduct() != null) {
            productApplication.setProduct(createAndReturnProduct(input.getProduct(), true));
        }
        if (input.getQuantity() != null) {
            productApplication.setQuantity(createAndReturnProperty(input.getQuantity(), true));
        }
        return productApplication;
    }

    public static void checkProductApplicationForErrors(
            ProductApplicationInput productApplicationInput) throws IllegalArgumentException {
        if (productApplicationInput.getQuantity() != null) {
            checkPropertyInputForErrors(productApplicationInput.getQuantity());
        }
        if (productApplicationInput.getProduct() != null) {
            checkProductForErrors(productApplicationInput.getProduct());
        }
    }

    public ProductApplicationResponse createProductApplication(
            ProductApplicationInput productApplicationInput) {
        LoggingHelper.logMutation("createProductApplication", repo.getGraphNameForMutation());
        ProductApplicationResponse response = new ProductApplicationResponse();
        try {
            checkProductApplicationForErrors(productApplicationInput);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(iae.getMessage());
            return response;
        }
        response.setProductApplication(repo.createProductApplication(productApplicationInput));
        response.setMessage(MESSAGE.SUCCESS);
        response.setCode(200);
        response.setSuccess(true);
        return response;
    }

    public ProductApplicationResponse updateProductApplication(
            String id, ProductApplicationInput productApplicationInput) {
        LoggingHelper.logMutation("updateProductApplication", repo.getGraphNameForMutation());
        ProductApplicationResponse response = new ProductApplicationResponse();
        IRI iri = null;
        try {
            iri = Values.iri(id);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(iae.getMessage());
            return response;
        }
        ProductApplication productApplication = repo.getProductApplicationById(iri);
        if (productApplication == null) {
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, id));
            response.setSuccess(false);
            response.setCode(500);
            return response;
        }
        response.setProductApplication(repo.updateProductApplication(iri, productApplicationInput));
        response.setSuccess(true);
        response.setMessage(MESSAGE.SUCCESS);
        response.setCode(200);
        return response;
    }

    public ProductApplicationResponse deleteProductApplication(String productApplicationId) {
        LoggingHelper.logMutation("deleteProductApplication", repo.getGraphNameForMutation());
        ProductApplicationResponse response = new ProductApplicationResponse();
        IRI iri = null;
        try {
            iri = Values.iri(productApplicationId);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            return response;
        }
        response.setCode(200);
        response.setSuccess(repo.deleteProductApplication(iri));
        response.setMessage(String.format(MESSAGE.DELETED, iri));
        return response;
    }

    public ProductApplicationResponse addPropertyToProductApplication(
            String propertyId, String productApplicationId) {
        LoggingHelper.logMutation(
                "addPropertyToProductApplication", repo.getGraphNameForMutation());
        ProductApplicationResponse response = new ProductApplicationResponse();
        IRI propertyIRI = null;
        IRI productApplicationIRI = null;
        try {
            propertyIRI = Values.iri(propertyId);
            productApplicationIRI = Values.iri(productApplicationId);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            return response;
        }
        Property property = repo.getPropertyById(propertyIRI);
        ProductApplication productApplication =
                repo.getProductApplicationById(productApplicationIRI);
        if (property == null || productApplication == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND,
                            property == null ? propertyId : productApplicationId));
            return response;
        }
        response.setProductApplication(
                repo.addPropertyToProductApplication(propertyIRI, productApplicationIRI));
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        response.setCode(200);
        return response;
    }

    public PropertyResponse createPropertyForProductApplication(
            PropertyInput propertyInput, String productApplicationId) {
        LoggingHelper.logMutation(
                "createPropertyForProductApplication", repo.getGraphNameForMutation());
        PropertyResponse response = new PropertyResponse();
        IRI productApplicationIRI = null;
        try {
            productApplicationIRI = Values.iri(productApplicationId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        ProductApplication productApplication =
                repo.getProductApplicationById(productApplicationIRI);
        if (productApplication == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, productApplicationId));
            return response;
        }
        try {
            PropertyRepository.checkPropertyInputForErrors(propertyInput);
        } catch (IllegalArgumentException iae) {
            response.setSuccess(false);
            response.setMessage(iae.getMessage());
            response.setCode(500);
            return response;
        }

        response.setProperty(
                repo.createPropertyForProductApplication(propertyInput, productApplicationIRI));
        response.setCode(200);
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        return response;
    }

    public ProductApplicationResponse removePropertyFromProductApplication(
            String propertyId, String productApplicationId) {
        LoggingHelper.logMutation(
                "removePropertyFromProductApplication", repo.getGraphNameForMutation());
        ProductApplicationResponse response = new ProductApplicationResponse();
        IRI propertyIRI = null;
        IRI productApplicationIRI = null;
        try {
            propertyIRI = Values.iri(propertyId);
            productApplicationIRI = Values.iri(productApplicationId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Property property = repo.getPropertyById(propertyIRI);
        ProductApplication productApplication =
                repo.getProductApplicationById(productApplicationIRI);
        if (property == null || productApplication == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND,
                            property == null ? propertyId : productApplicationId));
            return response;
        }

        if (productApplication.getProperties().remove(property)) {
            response.setProductApplication(
                    repo.removePropertyFromProductApplication(propertyIRI, productApplicationIRI));
            response.setCode(200);
            response.setSuccess(true);
            response.setMessage(MESSAGE.SUCCESS);
        } else {
            response.setCode(500);
            response.setMessage(
                    String.format(MESSAGE.ENTITY_NOT_CONTAINED, productApplicationId, propertyId));
            response.setSuccess(false);
        }
        return response;
    }
}
