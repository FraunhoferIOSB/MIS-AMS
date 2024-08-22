package de.fraunhofer.iosb.ilt.ams.repository;

import static de.fraunhofer.iosb.ilt.ams.repository.ProductApplicationRepository.checkProductApplicationForErrors;
import static de.fraunhofer.iosb.ilt.ams.repository.ProductClassRepository.checkProductClassInputForErrors;
import static de.fraunhofer.iosb.ilt.ams.repository.PropertyRepository.createAndReturnProperty;
import static de.fraunhofer.iosb.ilt.ams.repository.SupplyChainRepository.checkSupplyChainInputForErrors;

import de.fraunhofer.iosb.ilt.ams.dao.ProductDAO;
import de.fraunhofer.iosb.ilt.ams.model.*;
import de.fraunhofer.iosb.ilt.ams.model.filter.ProductFilter;
import de.fraunhofer.iosb.ilt.ams.model.input.*;
import de.fraunhofer.iosb.ilt.ams.model.response.*;
import de.fraunhofer.iosb.ilt.ams.utility.LoggingHelper;
import de.fraunhofer.iosb.ilt.ams.utility.MESSAGE;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepository {

    @Autowired ProductDAO productDAO;

    @Autowired ObjectRdf4jRepository repository;

    public List<Product> getProducts(ProductFilter productFilter) {
        LoggingHelper.logQuery("getProducts", repository.getGraphNameForQuery().getQueryString());
        repository.emptyProcessedIds();
        List<Product> products = productDAO.list().stream().distinct().collect(Collectors.toList());
        if (productFilter != null && productFilter.getId() != null) {
            products =
                    products.stream()
                            .filter(
                                    product ->
                                            Values.iri(productFilter.getId())
                                                    .equals(product.getId()))
                            .distinct()
                            .collect(Collectors.toList());
        }
        if (productFilter != null && productFilter.getSourceId() != null) {
            products =
                    products.stream()
                            .filter(
                                    product ->
                                            productFilter
                                                    .getSourceId()
                                                    .equals(product.getSourceId()))
                            .distinct()
                            .collect(Collectors.toList());
        }
        return products;
    }

    public static Product createAndReturnProduct(ProductInput input, boolean isCreate)
            throws IllegalArgumentException {
        checkProductForErrors(input);
        Product product = new Product();
        if (isCreate) {
            product.setSourceId(input.getSourceId());
            product.setLabel(input.getLabel());
            product.setLabelLanguageCode(input.getLabelLanguageCode());
            product.setDescription(input.getDescription());
            product.setDescriptionLanguageCode(input.getDescriptionLanguageCode());
            if (input.getProductPassportInput() != null) {
                product.setProductPassport(
                        createAndReturnProductPassport(input.getProductPassportInput()));
            }
        }
        return product;
    }

    public static void checkProductForErrors(ProductInput input) throws IllegalArgumentException {
        if (input.getLabel() != null && input.getLabelLanguageCode() == null) {
            throw new IllegalArgumentException(
                    String.format(MESSAGE.LANGUAGE_ERROR, input.getLabel()));
        }
        if (input.getDescription() != null && input.getDescriptionLanguageCode() == null) {
            throw new IllegalArgumentException(
                    String.format(MESSAGE.LANGUAGE_ERROR, input.getDescription()));
        }
    }

    public static ProductPassport createAndReturnProductPassport(ProductPassportInput input) {
        ProductPassport productPassport = new ProductPassport();
        for (var property : input.getProperties()) {
            productPassport.addProperty(createAndReturnProperty(property, true));
        }
        return productPassport;
    }

    public ProductResponse createProduct(ProductInput productInput) {
        LoggingHelper.logMutation("createProduct", repository.getGraphNameForMutation());
        ProductResponse productResponse = new ProductResponse();
        try {
            checkProductForErrors(productInput);
        } catch (IllegalArgumentException iae) {
            productResponse.setCode(500);
            productResponse.setMessage(iae.getLocalizedMessage());
            productResponse.setSuccess(false);
            return productResponse;
        }
        productResponse.setProduct(repository.createProduct(productInput));
        productResponse.setSuccess(true);
        productResponse.setCode(200);
        productResponse.setMessage(MESSAGE.SUCCESS);
        return productResponse;
    }

    public ProductResponse updateProduct(String id, ProductInput productInput) {
        LoggingHelper.logMutation("updateProduct", repository.getGraphNameForMutation());
        ProductResponse productResponse = new ProductResponse();
        IRI iri = null;
        try {
            iri = Values.iri(id);
        } catch (IllegalArgumentException iae) {
            productResponse.setCode(500);
            productResponse.setSuccess(false);
            productResponse.setMessage(iae.getMessage());
            return productResponse;
        }
        Product product = repository.getProductById(iri);
        if (product == null) {
            productResponse.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, id));
            productResponse.setSuccess(false);
            productResponse.setCode(500);
            return productResponse;
        }
        productResponse.setProduct(repository.updateProduct(iri, productInput));
        productResponse.setCode(200);
        productResponse.setMessage(MESSAGE.SUCCESS);
        productResponse.setSuccess(true);
        return productResponse;
    }

    public ProductResponse deleteProduct(String id) {
        LoggingHelper.logMutation("deleteProduct", repository.getGraphNameForMutation());
        ProductResponse response = new ProductResponse();
        IRI iri = null;
        try {
            iri = Values.iri(id);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            return response;
        }
        response.setCode(200);
        response.setSuccess(repository.deleteProduct(iri));
        response.setMessage(String.format(MESSAGE.DELETED, iri));
        return response;
    }

    public ProductResponse addPropertyToProduct(String propertyId, String productId) {
        LoggingHelper.logMutation("addPropertyToProduct", repository.getGraphNameForMutation());
        ProductResponse response = new ProductResponse();
        IRI propertyIRI = null;
        IRI productIRI = null;
        try {
            propertyIRI = Values.iri(propertyId);
            productIRI = Values.iri(productId);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            return response;
        }
        Property property = repository.getPropertyById(propertyIRI);
        Product product = repository.getProductById(productIRI);
        if (property == null || product == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, property == null ? propertyId : productId));
            return response;
        }
        response.setProduct(repository.addPropertyToProduct(propertyIRI, productIRI));
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        response.setCode(200);
        return response;
    }

    public PropertyResponse createPropertyForProduct(
            PropertyInput propertyInput, String productId) {
        LoggingHelper.logMutation("createPropertyForProduct", repository.getGraphNameForMutation());
        PropertyResponse response = new PropertyResponse();
        IRI productIRI = null;
        try {
            productIRI = Values.iri(productId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Product product = repository.getProductById(productIRI);
        if (product == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, productId));
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

        response.setProperty(repository.createPropertyForProduct(propertyInput, productIRI));
        response.setCode(200);
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        return response;
    }

    public ProductResponse removePropertyFromProduct(String propertyId, String productId) {
        LoggingHelper.logMutation(
                "removePropertyFromProduct", repository.getGraphNameForMutation());
        ProductResponse response = new ProductResponse();
        IRI propertyIRI = null;
        IRI productIRI = null;
        try {
            propertyIRI = Values.iri(propertyId);
            productIRI = Values.iri(productId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Property property = repository.getPropertyById(propertyIRI);
        Product product = repository.getProductById(productIRI);
        if (property == null || product == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, property == null ? propertyId : productId));
            return response;
        }

        if (product.removeProperty(property)) {
            response.setProduct(repository.removePropertyFromProduct(propertyIRI, productIRI));
            response.setCode(200);
            response.setSuccess(true);
            response.setMessage(MESSAGE.SUCCESS);
        } else {
            response.setCode(500);
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_CONTAINED, productId, propertyId));
            response.setSuccess(false);
        }
        return response;
    }

    public ProductResponse addProductClassToProduct(String productClassId, String productId) {
        LoggingHelper.logMutation("addProductClassToProduct", repository.getGraphNameForMutation());
        ProductResponse response = new ProductResponse();
        IRI productClassIRI = null;
        IRI productIRI = null;
        try {
            productClassIRI = Values.iri(productClassId);
            productIRI = Values.iri(productId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        ProductClass productClass = repository.getProductClassById(productClassIRI);
        Product product = repository.getProductById(productIRI);
        if (productClass == null || product == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND,
                            productClass == null ? productClassIRI : productId));
            return response;
        }
        response.setProduct(repository.addProductClassToProduct(productClassIRI, productIRI));
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        response.setCode(200);
        return response;
    }

    public ProductClassResponse createProductClassForProduct(
            ProductClassInput productClassInput, String productId) {
        LoggingHelper.logMutation(
                "createProductClassForProduct", repository.getGraphNameForMutation());
        ProductClassResponse response = new ProductClassResponse();
        IRI productIRI = null;
        try {
            productIRI = Values.iri(productId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getLocalizedMessage());
            response.setSuccess(false);
            response.setCode(500);
            return response;
        }
        Product product = repository.getProductById(productIRI);
        if (product == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, productId));
            return response;
        }

        try {
            checkProductClassInputForErrors(productClassInput);
        } catch (IllegalArgumentException iae) {
            response.setSuccess(false);
            response.setMessage(iae.getMessage());
            response.setCode(500);
            return response;
        }
        response.setProductClass(
                repository.createProductClassForProduct(productClassInput, productIRI));
        response.setSuccess(true);
        response.setMessage(MESSAGE.SUCCESS);
        response.setCode(200);
        return response;
    }

    public ProductResponse removeProductClassFromProduct(String productClassId, String productId) {
        LoggingHelper.logMutation(
                "removeProductClassFromProduct", repository.getGraphNameForMutation());
        ProductResponse response = new ProductResponse();
        IRI productClassIRI = null;
        IRI productIRI = null;
        try {
            productClassIRI = Values.iri(productClassId);
            productIRI = Values.iri(productId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        ProductClass productClass = repository.getProductClassById(productClassIRI);
        Product product = repository.getProductById(productIRI);
        if (productClass == null || product == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND,
                            product == null ? productId : productClassId));
            return response;
        }

        if (product.getProductClasses().remove(productClass)) {
            response.setProduct(
                    repository.removeProductClassFromProduct(productClassIRI, productIRI));
            response.setCode(200);
            response.setSuccess(true);
            response.setMessage(MESSAGE.SUCCESS);
        } else {
            response.setCode(500);
            response.setMessage(
                    String.format(MESSAGE.ENTITY_NOT_CONTAINED, productId, productClassId));
            response.setSuccess(false);
        }
        return response;
    }

    public ProductResponse addSubProductToProduct(
            String subProductProductApplicationId, String productId) {
        LoggingHelper.logMutation("addSubProductToProduct", repository.getGraphNameForMutation());
        ProductResponse response = new ProductResponse();
        IRI productIRI = null;
        IRI subProductIRI = null;
        try {
            productIRI = Values.iri(productId);
            subProductIRI = Values.iri(subProductProductApplicationId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Product product = repository.getProductById(productIRI);
        ProductApplication subProduct = repository.getProductApplicationById(subProductIRI);
        if (product == null || subProduct == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND,
                            product == null ? productId : subProductProductApplicationId));
            return response;
        }
        response.setProduct(repository.addSubProductToProduct(subProductIRI, productIRI));
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        response.setCode(200);
        return response;
    }

    public ProductApplicationResponse createSubProductForProduct(
            ProductApplicationInput productApplication, String productId) {
        LoggingHelper.logMutation(
                "createSubProductForProduct", repository.getGraphNameForMutation());
        ProductApplicationResponse response = new ProductApplicationResponse();
        IRI productIRI = null;
        try {
            productIRI = Values.iri(productId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Product product = repository.getProductById(productIRI);
        if (product == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, productId));
            return response;
        }

        try {
            checkProductApplicationForErrors(productApplication);
        } catch (IllegalArgumentException iae) {
            response.setSuccess(false);
            response.setMessage(iae.getMessage());
            response.setCode(500);
            return response;
        }
        response.setProductApplication(
                repository.createSubProductForProduct(productApplication, productIRI));
        response.setCode(200);
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        return response;
    }

    public ProductResponse removeSubProductFromProduct(
            String subProductProductApplicationId, String productId) {
        LoggingHelper.logMutation(
                "removeSubProductFromProduct", repository.getGraphNameForMutation());
        ProductResponse response = new ProductResponse();
        IRI productIRI = null;
        IRI subProductIRI = null;
        try {
            productIRI = Values.iri(productId);
            subProductIRI = Values.iri(subProductProductApplicationId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Product product = repository.getProductById(productIRI);
        ProductApplication subProduct = repository.getProductApplicationById(subProductIRI);
        if (product == null || subProduct == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND,
                            product == null ? productId : subProductProductApplicationId));
            return response;
        }

        if (product.getBillOfMaterials().remove(subProduct)) {
            response.setProduct(repository.removeSubProductFromProduct(subProductIRI, productIRI));
            response.setCode(200);
            response.setSuccess(true);
            response.setMessage(MESSAGE.SUCCESS);
        } else {
            response.setCode(500);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_CONTAINED,
                            productId,
                            subProductProductApplicationId));
            response.setSuccess(false);
        }
        return response;
    }

    public ProductResponse addSemanticReferenceToProduct(
            String semanticReferenceId, String productId) {
        LoggingHelper.logMutation(
                "addSemanticReferenceToProduct", repository.getGraphNameForMutation());
        ProductResponse response = new ProductResponse();
        IRI productIRI = null;
        IRI semanticIRI = null;
        try {
            productIRI = Values.iri(productId);
            semanticIRI = Values.iri(semanticReferenceId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Product product = repository.getProductById(productIRI);
        SemanticReference semanticReference = repository.getSemanticReferenceById(semanticIRI);
        if (product == null || semanticReference == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND,
                            product == null ? productId : semanticReferenceId));
            return response;
        }
        response.setProduct(repository.addSemanticReferenceToProduct(semanticIRI, productIRI));
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        response.setCode(200);
        return response;
    }

    public SemanticReferenceResponse createSemanticReferenceForProduct(
            SemanticReferenceInput semanticReference, String productId) {
        LoggingHelper.logMutation(
                "createSemanticReferenceForProduct", repository.getGraphNameForMutation());
        SemanticReferenceResponse response = new SemanticReferenceResponse();
        IRI productIRI = null;
        try {
            productIRI = Values.iri(productId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Product product = repository.getProductById(productIRI);
        if (product == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, productId));
            return response;
        }
        try {
            SemanticReferenceRepository.checkSemanticReferenceInputForErrors(semanticReference);
        } catch (IllegalArgumentException iae) {
            response.setSuccess(false);
            response.setMessage(iae.getMessage());
            response.setCode(500);
            return response;
        }
        response.setSemanticReference(
                repository.createSemanticReferenceForProduct(semanticReference, productIRI));
        response.setCode(200);
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        return response;
    }

    public ProductResponse removeSemanticReferenceFromProduct(
            String semanticReferenceId, String productId) {
        LoggingHelper.logMutation(
                "removeSemanticReferenceFromProduct", repository.getGraphNameForMutation());
        ProductResponse response = new ProductResponse();
        IRI productIRI = null;
        IRI semanticIRI = null;
        try {
            productIRI = Values.iri(productId);
            semanticIRI = Values.iri(semanticReferenceId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Product product = repository.getProductById(productIRI);
        SemanticReference semanticReference = repository.getSemanticReferenceById(semanticIRI);
        if (product == null || semanticReference == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND,
                            product == null ? productId : semanticReferenceId));
            return response;
        }

        if (product.getSemanticReferences().remove(semanticReference)) {
            response.setProduct(
                    repository.removeSemanticReferenceFromProduct(semanticIRI, productIRI));
            response.setCode(200);
            response.setSuccess(true);
            response.setMessage(MESSAGE.SUCCESS);
        } else {
            response.setCode(500);
            response.setMessage(
                    String.format(MESSAGE.ENTITY_NOT_CONTAINED, productId, semanticReferenceId));
            response.setSuccess(false);
        }
        return response;
    }

    public ProductResponse addSupplyChainToProduct(String supplyChainId, String productId) {
        LoggingHelper.logMutation("addSupplyChainToProduct", repository.getGraphNameForMutation());
        ProductResponse response = new ProductResponse();
        IRI productIRI = null;
        IRI supplyChainIRI = null;
        try {
            productIRI = Values.iri(productId);
            supplyChainIRI = Values.iri(supplyChainId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Product product = repository.getProductById(productIRI);
        SupplyChain supplyChain = repository.getSupplyChainById(supplyChainIRI);
        if (product == null || supplyChain == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, product == null ? productId : supplyChainId));
            return response;
        }
        response.setProduct(repository.addSupplyChainToProduct(supplyChainIRI, productIRI));
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        response.setCode(200);
        return response;
    }

    public SupplyChainResponse createSupplyChainForProduct(
            SupplyChainInput supplyChainInput, String productId) {
        LoggingHelper.logMutation(
                "createSupplyChainForProduct", repository.getGraphNameForMutation());
        SupplyChainResponse response = new SupplyChainResponse();
        IRI productIRI = null;
        try {
            productIRI = Values.iri(productId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Product product = repository.getProductById(productIRI);
        if (product == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, productId));
            return response;
        }
        try {
            checkSupplyChainInputForErrors(supplyChainInput);
        } catch (IllegalArgumentException iae) {
            response.setSuccess(false);
            response.setMessage(iae.getMessage());
            response.setCode(500);
            return response;
        }
        response.setSupplyChain(
                repository.createSupplyChainForProduct(supplyChainInput, productIRI));
        response.setCode(200);
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        return response;
    }

    public ProductResponse removeSupplyChainFromProduct(String supplyChainId, String productId) {
        LoggingHelper.logMutation(
                "removeSupplyChainFromProduct", repository.getGraphNameForMutation());
        ProductResponse response = new ProductResponse();
        IRI productIRI = null;
        IRI supplyChainIRI = null;
        try {
            productIRI = Values.iri(productId);
            supplyChainIRI = Values.iri(supplyChainId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Product product = repository.getProductById(productIRI);
        SupplyChain supplyChain = repository.getSupplyChainById(supplyChainIRI);
        if (product == null || supplyChain == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, product == null ? productId : supplyChainId));
            return response;
        }

        if (product.getSupplyChains().remove(supplyChain)) {
            response.setProduct(
                    repository.removeSupplyChainFromProduct(supplyChainIRI, productIRI));
            response.setCode(200);
            response.setSuccess(true);
            response.setMessage(MESSAGE.SUCCESS);
        } else {
            response.setCode(500);
            response.setMessage(
                    String.format(MESSAGE.ENTITY_NOT_CONTAINED, productId, supplyChainId));
            response.setSuccess(false);
        }
        return response;
    }
}
