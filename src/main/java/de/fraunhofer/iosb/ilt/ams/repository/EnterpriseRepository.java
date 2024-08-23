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
package de.fraunhofer.iosb.ilt.ams.repository;

import static de.fraunhofer.iosb.ilt.ams.repository.FactoryRepository.checkFactoryInputForErrors;
import static de.fraunhofer.iosb.ilt.ams.repository.ProductRepository.checkProductForErrors;
import static de.fraunhofer.iosb.ilt.ams.repository.PropertyRepository.checkPropertyInputForErrors;

import de.fraunhofer.iosb.ilt.ams.dao.EnterpriseDAO;
import de.fraunhofer.iosb.ilt.ams.model.*;
import de.fraunhofer.iosb.ilt.ams.model.filter.EnterpriseFilter;
import de.fraunhofer.iosb.ilt.ams.model.input.EnterpriseInput;
import de.fraunhofer.iosb.ilt.ams.model.input.FactoryInput;
import de.fraunhofer.iosb.ilt.ams.model.input.ProductInput;
import de.fraunhofer.iosb.ilt.ams.model.input.PropertyInput;
import de.fraunhofer.iosb.ilt.ams.model.response.EnterpriseResponse;
import de.fraunhofer.iosb.ilt.ams.model.response.FactoryResponse;
import de.fraunhofer.iosb.ilt.ams.model.response.ProductResponse;
import de.fraunhofer.iosb.ilt.ams.model.response.PropertyResponse;
import de.fraunhofer.iosb.ilt.ams.utility.LoggingHelper;
import de.fraunhofer.iosb.ilt.ams.utility.MESSAGE;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class EnterpriseRepository {

    @Autowired EnterpriseDAO enterpriseDAO;

    @Autowired ObjectRdf4jRepository rdf4jRepository;

    public List<Enterprise> getEnterprises(EnterpriseFilter enterpriseFilter) {
        LoggingHelper.logQuery(
                "getEnterprises", rdf4jRepository.getGraphNameForQuery().getQueryString());
        rdf4jRepository.emptyProcessedIds();
        var enterpriseList = enterpriseDAO.list();
        if (enterpriseFilter != null) {
            if (enterpriseFilter.getId() != null) {
                enterpriseList =
                        enterpriseList.stream()
                                .filter(
                                        (enterprise ->
                                                Values.iri(enterpriseFilter.getId())
                                                        .equals(enterprise.getId())))
                                .distinct()
                                .collect(Collectors.toList());
            }
            if (enterpriseFilter.getSourceId() != null) {
                enterpriseList =
                        enterpriseList.stream()
                                .filter(
                                        (enterprise ->
                                                enterpriseFilter
                                                        .getSourceId()
                                                        .equals(enterprise.getSourceId())))
                                .distinct()
                                .collect(Collectors.toList());
            }
        }
        return enterpriseList;
    }

    public EnterpriseResponse createEnterprise(EnterpriseInput enterpriseInput) {
        EnterpriseResponse enterpriseResponse = new EnterpriseResponse();
        try {
            checkEnterpriseInputForErrors(enterpriseInput);
        } catch (IllegalArgumentException iae) {
            enterpriseResponse.setEnterprise(null);
            enterpriseResponse.setCode(500);
            enterpriseResponse.setMessage(iae.getMessage());
            enterpriseResponse.setSuccess(false);
            return enterpriseResponse;
        }
        LoggingHelper.logMutation("createEnterprise", rdf4jRepository.getGraphNameForMutation());
        enterpriseResponse.setEnterprise(rdf4jRepository.createEnterprise(enterpriseInput));
        enterpriseResponse.setSuccess(true);
        enterpriseResponse.setMessage("Success");
        enterpriseResponse.setCode(200);
        return enterpriseResponse;
    }

    public static Enterprise createAndReturnEnterprise(EnterpriseInput enterpriseInput)
            throws IllegalArgumentException {
        checkEnterpriseInputForErrors(enterpriseInput);
        Enterprise enterprise = new Enterprise();
        enterprise.setSourceId(enterpriseInput.getSourceId());

        enterprise.setLabel(enterpriseInput.getLabel());
        enterprise.setLabelLanguageCode(enterpriseInput.getLabelLanguageCode());

        enterprise.setDescription(enterpriseInput.getDescription());
        enterprise.setDescriptionLanguageCode(enterpriseInput.getDescriptionLanguageCode());
        var location = ObjectRdf4jRepository.createLocation(enterpriseInput.getLocation());
        enterprise.setLocation(location);
        return enterprise;
    }

    public static void checkEnterpriseInputForErrors(EnterpriseInput enterpriseInput)
            throws IllegalArgumentException {
        if (enterpriseInput.getLabel() != null && enterpriseInput.getLabelLanguageCode() == null) {
            throw new IllegalArgumentException(
                    String.format(MESSAGE.LANGUAGE_ERROR, enterpriseInput.getLabel()));
        }
        if (enterpriseInput.getDescription() != null
                && enterpriseInput.getDescriptionLanguageCode() == null) {
            throw new IllegalArgumentException(
                    String.format(MESSAGE.LANGUAGE_ERROR, enterpriseInput.getDescription()));
        }

        if (enterpriseInput.getSubsidiaryEnterprises() != null) {
            for (var subsidiary : enterpriseInput.getSubsidiaryEnterprises()) {
                if (subsidiary.getId() != null) {
                    Values.iri(subsidiary.getId());
                }
                checkEnterpriseInputForErrors(subsidiary);
            }
        }
        if (enterpriseInput.getProducts() != null) {
            for (var product : enterpriseInput.getProducts()) {
                if (product.getId() != null) {
                    Values.iri(product.getId());
                }
                checkProductForErrors(product);
            }
        }
        if (enterpriseInput.getProperties() != null) {
            for (var property : enterpriseInput.getProperties()) {
                if (property.getId() != null) {
                    Values.iri(property.getId());
                }
                checkPropertyInputForErrors(property);
            }
        }
        if (enterpriseInput.getFactories() != null) {
            for (var factory : enterpriseInput.getFactories()) {
                if (factory.getId() != null) {
                    Values.iri(factory.getId());
                }
                checkFactoryInputForErrors(factory);
            }
        }
    }

    public EnterpriseResponse updateEnterprise(String id, EnterpriseInput enterpriseInput) {
        EnterpriseResponse enterpriseResponse = new EnterpriseResponse();
        IRI iri = null;
        try {
            iri = Values.iri(id);
        } catch (IllegalArgumentException iae) {
            enterpriseResponse.setMessage(iae.getMessage());
            enterpriseResponse.setSuccess(false);
            enterpriseResponse.setCode(500);
            return enterpriseResponse;
        }
        Enterprise enterprise = rdf4jRepository.getEnterpriseById(iri);
        if (enterprise == null) {
            enterpriseResponse.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, id));
            enterpriseResponse.setSuccess(false);
            enterpriseResponse.setCode(500);
            return enterpriseResponse;
        }
        enterprise.setSourceId(
                enterpriseInput.getSourceId() == null
                        ? enterprise.getSourceId()
                        : enterpriseInput.getSourceId());
        enterprise.setLabel(
                enterpriseInput.getLabel() == null
                        ? enterprise.getLabel()
                        : enterpriseInput.getLabel());
        enterprise.setLabelLanguageCode(
                enterpriseInput.getLabelLanguageCode() == null
                        ? enterprise.getLabelLanguageCode()
                        : enterpriseInput.getLabelLanguageCode());
        enterprise.setDescription(
                enterpriseInput.getDescription() == null
                        ? enterprise.getDescription()
                        : enterpriseInput.getDescription());
        enterprise.setDescriptionLanguageCode(
                enterpriseInput.getDescriptionLanguageCode() == null
                        ? enterprise.getDescriptionLanguageCode()
                        : enterpriseInput.getDescriptionLanguageCode());
        if (enterpriseInput.getLocation() != null) {
            var location = ObjectRdf4jRepository.createLocation(enterpriseInput.getLocation());
            enterprise.setLocation(location);
        }
        try {
            checkEnterpriseForUpdateErrors(enterprise);
        } catch (IllegalArgumentException iae) {
            enterpriseResponse.setMessage(iae.getMessage());
            enterpriseResponse.setSuccess(false);
            enterpriseResponse.setCode(500);
            return enterpriseResponse;
        }
        LoggingHelper.logMutation("updateEnterprise", rdf4jRepository.getGraphNameForMutation());
        enterpriseResponse.setEnterprise(rdf4jRepository.updateEnterprise(iri, enterpriseInput));
        enterpriseResponse.setCode(200);
        enterpriseResponse.setSuccess(true);
        enterpriseResponse.setMessage(MESSAGE.SUCCESS);
        return enterpriseResponse;
    }

    private void checkEnterpriseForUpdateErrors(Enterprise enterprise)
            throws IllegalArgumentException {
        if (enterprise.getLabel() != null && enterprise.getLabelLanguageCode() == null) {
            throw new IllegalArgumentException(
                    String.format(MESSAGE.LANGUAGE_ERROR, enterprise.getLabel()));
        }
        if (enterprise.getDescription() != null
                && enterprise.getDescriptionLanguageCode() == null) {
            throw new IllegalArgumentException(
                    String.format(MESSAGE.LANGUAGE_ERROR, enterprise.getDescription()));
        }
    }

    public EnterpriseResponse deleteEnterprise(String id) {
        EnterpriseResponse response = new EnterpriseResponse();
        IRI iri = null;
        try {
            iri = Values.iri(id);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            return response;
        }
        LoggingHelper.logMutation("deleteEnterprise", rdf4jRepository.getGraphNameForMutation());
        response.setCode(200);
        response.setSuccess(rdf4jRepository.deleteEnterprise(iri));
        response.setMessage(String.format(MESSAGE.DELETED, iri));
        return response;
    }

    public EnterpriseResponse bulkDeleteEnterprise(String id) {
        EnterpriseResponse response = new EnterpriseResponse();
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
        response.setSuccess(rdf4jRepository.bulkDeleteEnterprise(iri));
        response.setMessage(String.format(MESSAGE.DELETED, iri));
        return response;
    }

    public EnterpriseResponse addFactoryToEnterprise(String factoryId, String enterpriseId) {
        LoggingHelper.logMutation(
                "addFactoryToEnterprise", rdf4jRepository.getGraphNameForMutation());
        EnterpriseResponse response = new EnterpriseResponse();
        IRI factoryIRI = null;
        IRI enterpriseIRI = null;
        try {
            factoryIRI = Values.iri(factoryId);
            enterpriseIRI = Values.iri(enterpriseId);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            return response;
        }
        Factory factory = rdf4jRepository.getFactoryByIri(factoryIRI);
        Enterprise enterprise = rdf4jRepository.getEnterpriseById(enterpriseIRI);
        if (factory == null || enterprise == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, factory == null ? factoryId : enterpriseId));
            return response;
        }
        response.setEnterprise(rdf4jRepository.addFactoryToEnterprise(factoryIRI, enterpriseIRI));
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        response.setCode(200);
        return response;
    }

    public FactoryResponse createFactoryForEnterprise(
            FactoryInput factoryInput, String enterpriseId) {
        LoggingHelper.logMutation(
                "createFactoryForEnterprise", rdf4jRepository.getGraphNameForMutation());

        FactoryResponse response = new FactoryResponse();
        IRI enterpriseIRI = null;
        try {
            enterpriseIRI = Values.iri(enterpriseId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Enterprise enterprise = rdf4jRepository.getEnterpriseById(enterpriseIRI);
        if (enterprise == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, enterpriseId));
            return response;
        }
        try {
            checkFactoryInputForErrors(factoryInput);
        } catch (IllegalArgumentException iae) {
            response.setSuccess(false);
            response.setMessage(iae.getMessage());
            response.setCode(500);
            return response;
        }

        response.setFactory(
                rdf4jRepository.createFactoryForEnterprise(factoryInput, enterpriseIRI));
        response.setCode(200);
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        return response;
    }

    public EnterpriseResponse removeFactoryFromEnterprise(String factoryId, String enterpriseId) {
        LoggingHelper.logMutation(
                "removeFactoryFromEnterprise", rdf4jRepository.getGraphNameForMutation());

        EnterpriseResponse response = new EnterpriseResponse();
        IRI factoryIRI = null;
        IRI enterpriseIRI = null;
        try {
            factoryIRI = Values.iri(factoryId);
            enterpriseIRI = Values.iri(enterpriseId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Factory factory = rdf4jRepository.getFactoryByIri(factoryIRI);
        Enterprise enterprise = rdf4jRepository.getEnterpriseById(enterpriseIRI);
        if (factory == null || enterprise == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, factory == null ? factoryId : enterpriseId));
            return response;
        }

        if (enterprise.getFactories().remove(factory)) {
            response.setEnterprise(
                    rdf4jRepository.removeFactoryFromEnterprise(factoryIRI, enterpriseIRI));
            response.setCode(200);
            response.setSuccess(true);
            response.setMessage(MESSAGE.SUCCESS);
        } else {
            response.setCode(500);
            response.setMessage(
                    String.format(MESSAGE.ENTITY_NOT_CONTAINED, enterpriseId, factoryId));
            response.setSuccess(false);
        }
        return response;
    }

    public EnterpriseResponse addPropertyToEnterprise(String propertyId, String enterpriseId) {
        LoggingHelper.logMutation(
                "addPropertyToEnterprise", rdf4jRepository.getGraphNameForMutation());

        EnterpriseResponse response = new EnterpriseResponse();
        IRI propertyIRI = null;
        IRI enterpriseIRI = null;
        try {
            propertyIRI = Values.iri(propertyId);
            enterpriseIRI = Values.iri(enterpriseId);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            return response;
        }
        Property property = rdf4jRepository.getPropertyById(propertyIRI);
        Enterprise enterprise = rdf4jRepository.getEnterpriseById(enterpriseIRI);
        if (property == null || enterprise == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND,
                            property == null ? propertyId : enterpriseId));
            return response;
        }
        response.setEnterprise(rdf4jRepository.addPropertyToEnterprise(propertyIRI, enterpriseIRI));
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        response.setCode(200);
        return response;
    }

    public PropertyResponse createPropertyForEnterprise(
            PropertyInput propertyInput, String enterpriseId) {
        LoggingHelper.logMutation(
                "createPropertyForEnterprise", rdf4jRepository.getGraphNameForMutation());

        PropertyResponse response = new PropertyResponse();
        IRI enterpriseIRI = null;
        try {
            enterpriseIRI = Values.iri(enterpriseId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Enterprise enterprise = rdf4jRepository.getEnterpriseById(enterpriseIRI);
        if (enterprise == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, enterpriseId));
            return response;
        }
        try {
            checkPropertyInputForErrors(propertyInput);
        } catch (IllegalArgumentException iae) {
            response.setSuccess(false);
            response.setMessage(iae.getMessage());
            response.setCode(500);
            return response;
        }

        response.setProperty(
                rdf4jRepository.createPropertyForEnterprise(propertyInput, enterpriseIRI));
        response.setCode(200);
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        return response;
    }

    public EnterpriseResponse removePropertyFromEnterprise(String propertyId, String enterpriseId) {
        LoggingHelper.logMutation(
                "removePropertyFromEnterprise", rdf4jRepository.getGraphNameForMutation());

        EnterpriseResponse response = new EnterpriseResponse();
        IRI propertyIRI = null;
        IRI enterpriseIRI = null;
        try {
            propertyIRI = Values.iri(propertyId);
            enterpriseIRI = Values.iri(enterpriseId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Property property = rdf4jRepository.getPropertyById(propertyIRI);
        Enterprise enterprise = rdf4jRepository.getEnterpriseById(enterpriseIRI);
        if (property == null || enterprise == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND,
                            property == null ? propertyId : enterpriseId));
            return response;
        }

        if (enterprise.getProperties().remove(property)) {
            response.setEnterprise(
                    rdf4jRepository.removePropertyFromEnterprise(propertyIRI, enterpriseIRI));
            response.setCode(200);
            response.setSuccess(true);
            response.setMessage(MESSAGE.SUCCESS);
        } else {
            response.setCode(500);
            response.setMessage(
                    String.format(MESSAGE.ENTITY_NOT_CONTAINED, enterpriseId, propertyId));
            response.setSuccess(false);
        }
        return response;
    }

    public EnterpriseResponse addSubsidiaryEnterpriseToEnterprise(
            String subsidiaryEnterpriseId, String enterpriseId) {
        LoggingHelper.logMutation(
                "addSubsidiaryEnterpriseToEnterprise", rdf4jRepository.getGraphNameForMutation());

        EnterpriseResponse response = new EnterpriseResponse();
        IRI subsidiaryEnterpriseIRI = null;
        IRI enterpriseIRI = null;
        try {
            subsidiaryEnterpriseIRI = Values.iri(subsidiaryEnterpriseId);
            enterpriseIRI = Values.iri(enterpriseId);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            return response;
        }

        Enterprise subsidiaryEnterprise =
                rdf4jRepository.getEnterpriseById(subsidiaryEnterpriseIRI);
        Enterprise enterprise = rdf4jRepository.getEnterpriseById(enterpriseIRI);
        if (subsidiaryEnterprise == null || enterprise == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND,
                            subsidiaryEnterprise == null ? subsidiaryEnterpriseId : enterpriseId));
            return response;
        }
        response.setEnterprise(
                rdf4jRepository.addSubsidiaryEnterpriseToEnterprise(
                        subsidiaryEnterpriseIRI, enterpriseIRI));
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        response.setCode(200);
        return response;
    }

    public EnterpriseResponse createSubsidiaryEnterpriseForEnterprise(
            EnterpriseInput subsidiaryEnterprise, String enterpriseId) {
        LoggingHelper.logMutation(
                "createSubsidiaryEnterpriseForEnterprise",
                rdf4jRepository.getGraphNameForMutation());

        EnterpriseResponse response = new EnterpriseResponse();
        IRI enterpriseIRI = null;
        try {
            enterpriseIRI = Values.iri(enterpriseId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Enterprise enterprise = rdf4jRepository.getEnterpriseById(enterpriseIRI);
        if (enterprise == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, enterpriseId));
            return response;
        }
        try {
            checkEnterpriseInputForErrors(subsidiaryEnterprise);
        } catch (IllegalArgumentException iae) {
            response.setSuccess(false);
            response.setMessage(iae.getMessage());
            response.setCode(500);
            return response;
        }

        response.setEnterprise(
                rdf4jRepository.createSubsidiaryEnterpriseForEnterprise(
                        subsidiaryEnterprise, enterpriseIRI));
        response.setCode(200);
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        return response;
    }

    public EnterpriseResponse removeSubsidiaryEnterpriseFromEnterprise(
            String subsidiaryEnterpriseId, String enterpriseId) {
        LoggingHelper.logMutation(
                "removeSubsidiaryEnterpriseFromEnterprise",
                rdf4jRepository.getGraphNameForMutation());

        EnterpriseResponse response = new EnterpriseResponse();
        IRI subsidiaryEnterpriseIRI = null;
        IRI enterpriseIRI = null;
        try {
            subsidiaryEnterpriseIRI = Values.iri(subsidiaryEnterpriseId);
            enterpriseIRI = Values.iri(enterpriseId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Enterprise subsidiaryEnterprise =
                rdf4jRepository.getEnterpriseById(subsidiaryEnterpriseIRI);
        Enterprise enterprise = rdf4jRepository.getEnterpriseById(enterpriseIRI);
        if (subsidiaryEnterprise == null || enterprise == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND,
                            subsidiaryEnterprise == null ? subsidiaryEnterpriseId : enterpriseId));
            return response;
        }

        if (enterprise.getSubsidiaryEnterprises().remove(subsidiaryEnterprise)) {
            response.setEnterprise(
                    rdf4jRepository.removeSubsidiaryEnterpriseFromEnterprise(
                            subsidiaryEnterpriseIRI, enterpriseIRI));
            response.setCode(200);
            response.setSuccess(true);
            response.setMessage(MESSAGE.SUCCESS);
        } else {
            response.setCode(500);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_CONTAINED, enterpriseId, subsidiaryEnterpriseId));
            response.setSuccess(false);
        }
        return response;
    }

    public EnterpriseResponse addProductToEnterprise(String productId, String enterpriseId) {
        LoggingHelper.logMutation(
                "addProductToEnterprise", rdf4jRepository.getGraphNameForMutation());

        EnterpriseResponse response = new EnterpriseResponse();
        IRI productIRI = null;
        IRI enterpriseIRI = null;
        try {
            productIRI = Values.iri(productId);
            enterpriseIRI = Values.iri(enterpriseId);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            return response;
        }
        Product product = rdf4jRepository.getProductById(productIRI);
        Enterprise enterprise = rdf4jRepository.getEnterpriseById(enterpriseIRI);
        if (product == null || enterprise == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, product == null ? productId : enterpriseId));
            return response;
        }
        response.setEnterprise(rdf4jRepository.addProductToEnterprise(productIRI, enterpriseIRI));
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        response.setCode(200);
        return response;
    }

    public ProductResponse createProductForEnterprise(ProductInput product, String enterpriseId) {
        LoggingHelper.logMutation(
                "createProductForEnterprise", rdf4jRepository.getGraphNameForMutation());

        ProductResponse response = new ProductResponse();
        IRI enterpriseIRI = null;
        try {
            enterpriseIRI = Values.iri(enterpriseId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Enterprise enterprise = rdf4jRepository.getEnterpriseById(enterpriseIRI);
        if (enterprise == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, enterpriseId));
            return response;
        }
        try {
            checkProductForErrors(product);
        } catch (IllegalArgumentException iae) {
            response.setSuccess(false);
            response.setMessage(iae.getMessage());
            response.setCode(500);
            return response;
        }

        response.setProduct(rdf4jRepository.createProductForEnterprise(product, enterpriseIRI));
        response.setCode(200);
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        return response;
    }

    public EnterpriseResponse removeProductFromEnterprise(String productId, String enterpriseId) {
        LoggingHelper.logMutation(
                "removeProductFromEnterprise", rdf4jRepository.getGraphNameForMutation());

        EnterpriseResponse response = new EnterpriseResponse();
        IRI productIRI = null;
        IRI enterpriseIRI = null;
        try {
            productIRI = Values.iri(productId);
            enterpriseIRI = Values.iri(enterpriseId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Product product = rdf4jRepository.getProductById(productIRI);
        Enterprise enterprise = rdf4jRepository.getEnterpriseById(enterpriseIRI);
        if (product == null || enterprise == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, product == null ? productId : enterpriseId));
            return response;
        }

        if (enterprise.getProducts().remove(product)) {
            response.setEnterprise(
                    rdf4jRepository.removeProductFromEnterprise(productIRI, enterpriseIRI));
            response.setCode(200);
            response.setSuccess(true);
            response.setMessage(MESSAGE.SUCCESS);
        } else {
            response.setCode(500);
            response.setMessage(
                    String.format(MESSAGE.ENTITY_NOT_CONTAINED, enterpriseId, productId));
            response.setSuccess(false);
        }
        return response;
    }
}
