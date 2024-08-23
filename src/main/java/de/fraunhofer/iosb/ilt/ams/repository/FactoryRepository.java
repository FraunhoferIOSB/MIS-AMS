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

import static de.fraunhofer.iosb.ilt.ams.model.ObjectRdf4jRepository.createLocation;
import static de.fraunhofer.iosb.ilt.ams.repository.ProcessRepository.checkProcessInputForErrors;
import static de.fraunhofer.iosb.ilt.ams.repository.ProductRepository.checkProductForErrors;
import static de.fraunhofer.iosb.ilt.ams.repository.ProductionResourceRepository.checkHumanResourceInputForErrors;
import static de.fraunhofer.iosb.ilt.ams.repository.ProductionResourceRepository.checkMachineInputForErrors;

import de.fraunhofer.iosb.ilt.ams.dao.FactoryDAO;
import de.fraunhofer.iosb.ilt.ams.model.*;
import de.fraunhofer.iosb.ilt.ams.model.Process;
import de.fraunhofer.iosb.ilt.ams.model.filter.FactoryFilter;
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
public class FactoryRepository {

    @Autowired FactoryDAO factoryDAO;

    @Autowired ObjectRdf4jRepository repo;

    public List<Factory> getFactories(FactoryFilter factoryFilter) {
        LoggingHelper.logQuery("getFactories", repo.getGraphNameForQuery().getQueryString());
        repo.emptyProcessedIds();
        var factoryList = factoryDAO.list();
        if (factoryFilter != null) {
            if (factoryFilter.getId() != null) {
                factoryList =
                        factoryList.stream()
                                .filter(
                                        factory ->
                                                Values.iri(factoryFilter.getId())
                                                        .equals(factory.getId()))
                                .distinct()
                                .collect(Collectors.toList());
            }
            if (factoryFilter.getSourceId() != null) {
                factoryList =
                        factoryList.stream()
                                .filter(
                                        factory ->
                                                factoryFilter
                                                        .getSourceId()
                                                        .equals(factory.getSourceId()))
                                .distinct()
                                .collect(Collectors.toList());
            }
            return factoryList;
        } else {
            return factoryList.stream().distinct().collect(Collectors.toList());
        }
    }

    public FactoryResponse createFactory(FactoryInput factoryInput) {
        LoggingHelper.logMutation("createFactory", repo.getGraphNameForMutation());
        checkFactoryInputForErrors(factoryInput);
        FactoryResponse factoryResponse = new FactoryResponse();
        try {
            checkFactoryInputForErrors(factoryInput);
        } catch (IllegalArgumentException iae) {
            factoryResponse.setFactory(null);
            factoryResponse.setCode(500);
            factoryResponse.setMessage(iae.getMessage());
            factoryResponse.setSuccess(false);
            return factoryResponse;
        }
        factoryResponse.setFactory(repo.createFactory(factoryInput));
        factoryResponse.setSuccess(true);
        factoryResponse.setMessage(MESSAGE.SUCCESS);
        factoryResponse.setCode(200);
        return factoryResponse;
    }

    public static Factory createAndReturnFactory(FactoryInput factoryInput) {
        Factory factory = new Factory();
        factory.setSourceId(factoryInput.getSourceId());

        factory.setLabel(factoryInput.getLabel());
        factory.setLabelLanguageCode(factory.getLabelLanguageCode());

        factory.setDescription(factoryInput.getDescription());
        factory.setDescriptionLanguageCode(factoryInput.getDescriptionLanguageCode());
        var location = createLocation(factoryInput.getLocation());
        factory.setLocation(location);
        return factory;
    }

    public static void checkFactoryInputForErrors(FactoryInput factoryInput)
            throws IllegalArgumentException {
        if (factoryInput.getLabel() != null && factoryInput.getLabelLanguageCode() == null) {
            throw new IllegalArgumentException(
                    String.format(MESSAGE.LANGUAGE_ERROR, factoryInput.getLabel()));
        }
        if (factoryInput.getDescription() != null
                && factoryInput.getDescriptionLanguageCode() == null) {
            throw new IllegalArgumentException(
                    String.format(MESSAGE.LANGUAGE_ERROR, factoryInput.getDescription()));
        }
    }

    public FactoryResponse updateFactory(String factoryId, FactoryInput factoryInput) {
        LoggingHelper.logMutation("updateFactory", repo.getGraphNameForMutation());
        FactoryResponse factoryResponse = new FactoryResponse();
        IRI iri = null;
        try {
            iri = Values.iri(factoryId);
        } catch (IllegalArgumentException iae) {
            factoryResponse.setMessage(iae.getMessage());
            factoryResponse.setSuccess(false);
            factoryResponse.setCode(500);
            return factoryResponse;
        }
        Factory factory = repo.getFactoryByIri(iri);
        if (factory == null) {
            factoryResponse.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, iri));
            factoryResponse.setSuccess(false);
            factoryResponse.setCode(500);
            return factoryResponse;
        }
        factoryResponse.setFactory(repo.updateFactory(iri, factoryInput));
        factoryResponse.setCode(200);
        factoryResponse.setMessage(MESSAGE.SUCCESS);
        factoryResponse.setSuccess(true);
        return factoryResponse;
    }

    public FactoryResponse deleteFactory(String id) {
        LoggingHelper.logMutation("deleteFactory", repo.getGraphNameForMutation());
        FactoryResponse response = new FactoryResponse();
        IRI iri = null;
        try {
            iri = Values.iri(id);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            return response;
        }
        response.setSuccess(repo.deleteFactory(iri));
        response.setCode(200);
        response.setMessage(String.format(MESSAGE.DELETED, iri));
        return response;
    }

    public FactoryResponse addPropertyToFactory(String propertyId, String factoryId) {
        LoggingHelper.logMutation("addPropertyToFactory", repo.getGraphNameForMutation());
        FactoryResponse response = new FactoryResponse();
        IRI propertyIRI = null;
        IRI factoryIRI = null;
        try {
            propertyIRI = Values.iri(propertyId);
            factoryIRI = Values.iri(factoryId);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            return response;
        }
        Property property = repo.getPropertyById(propertyIRI);
        Factory factory = repo.getFactoryByIri(factoryIRI);
        if (property == null || factory == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, property == null ? propertyId : factoryId));
            return response;
        }
        response.setFactory(repo.addPropertyToFactory(propertyIRI, factoryIRI));
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        response.setCode(200);
        return response;
    }

    public PropertyResponse createPropertyForFactory(
            PropertyInput propertyInput, String factoryId) {
        LoggingHelper.logMutation("createPropertyForFactory", repo.getGraphNameForMutation());
        PropertyResponse response = new PropertyResponse();
        IRI factoryIRI = null;
        try {
            factoryIRI = Values.iri(factoryId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Factory factory = repo.getFactoryByIri(factoryIRI);
        if (factory == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, factoryId));
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

        response.setProperty(repo.createPropertyForFactory(propertyInput, factoryIRI));
        response.setCode(200);
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        return response;
    }

    public FactoryResponse removePropertyFromFactory(String propertyId, String factoryId) {
        LoggingHelper.logMutation("removePropertyFromFactory", repo.getGraphNameForMutation());
        FactoryResponse response = new FactoryResponse();
        IRI propertyIRI = null;
        IRI factoryIRI = null;
        try {
            propertyIRI = Values.iri(propertyId);
            factoryIRI = Values.iri(factoryId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Property property = repo.getPropertyById(propertyIRI);
        Factory factory = repo.getFactoryByIri(factoryIRI);
        if (property == null || factory == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, property == null ? propertyId : factoryId));
            return response;
        }

        if (factory.getProperties().remove(property)) {
            response.setFactory(repo.removePropertyFromFactory(propertyIRI, factoryIRI));
            response.setCode(200);
            response.setSuccess(true);
            response.setMessage(MESSAGE.SUCCESS);
        } else {
            response.setCode(500);
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_CONTAINED, factoryId, propertyId));
            response.setSuccess(false);
        }
        return response;
    }

    public FactoryResponse addProductToFactory(String productId, String factoryId) {
        LoggingHelper.logMutation("addProductToFactory", repo.getGraphNameForMutation());
        FactoryResponse response = new FactoryResponse();
        IRI productIRI = null;
        IRI factoryIRI = null;
        try {
            productIRI = Values.iri(productId);
            factoryIRI = Values.iri(factoryId);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            return response;
        }
        Product product = repo.getProductById(productIRI);
        Factory factory = repo.getFactoryByIri(factoryIRI);
        if (product == null || factory == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, product == null ? productId : factoryId));
            return response;
        }
        response.setFactory(repo.addProductToFactory(productIRI, factoryIRI));
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        response.setCode(200);
        return response;
    }

    public ProductResponse createProductForFactory(ProductInput product, String factoryId) {
        LoggingHelper.logMutation("createProductForFactory", repo.getGraphNameForMutation());
        ProductResponse response = new ProductResponse();
        IRI factoryIRI = null;
        try {
            factoryIRI = Values.iri(factoryId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Factory factory = repo.getFactoryByIri(factoryIRI);
        if (factory == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, factoryId));
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

        response.setProduct(repo.createProductForFactory(product, factoryIRI));
        response.setCode(200);
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        return response;
    }

    public FactoryResponse removeProductFromFactory(String productId, String factoryId) {
        LoggingHelper.logMutation("removeProductFromFactory", repo.getGraphNameForMutation());
        FactoryResponse response = new FactoryResponse();
        IRI productIRI = null;
        IRI factoryIRI = null;
        try {
            productIRI = Values.iri(productId);
            factoryIRI = Values.iri(factoryId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Product product = repo.getProductById(productIRI);
        Factory factory = repo.getFactoryByIri(factoryIRI);
        if (product == null || factory == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, product == null ? productId : factoryId));
            return response;
        }

        if (factory.getProducts().remove(product)) {
            response.setFactory(repo.removeProductFromFactory(productIRI, factoryIRI));
            response.setCode(200);
            response.setSuccess(true);
            response.setMessage(MESSAGE.SUCCESS);
        } else {
            response.setCode(500);
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_CONTAINED, factoryId, productId));
            response.setSuccess(false);
        }
        return response;
    }

    public FactoryResponse addMachineToFactory(String machineId, String factoryId) {
        LoggingHelper.logMutation("addMachineToFactory", repo.getGraphNameForMutation());
        FactoryResponse response = new FactoryResponse();
        IRI machineIRI = null;
        IRI factoryIRI = null;
        try {
            machineIRI = Values.iri(machineId);
            factoryIRI = Values.iri(factoryId);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            return response;
        }
        Machine machine = repo.getMachineById(machineIRI);
        Factory factory = repo.getFactoryByIri(factoryIRI);
        if (machine == null || factory == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, machine == null ? machineId : factoryId));
            return response;
        }
        response.setFactory(repo.addMachineToFactory(machineIRI, factoryIRI));
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        response.setCode(200);
        return response;
    }

    public MachineResponse createMachineForFactory(MachineInput machineInput, String factoryId) {
        LoggingHelper.logMutation("createMachineForFactory", repo.getGraphNameForMutation());
        MachineResponse response = new MachineResponse();
        IRI factoryIRI = null;
        try {
            factoryIRI = Values.iri(factoryId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Factory factory = repo.getFactoryByIri(factoryIRI);
        if (factory == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, factoryId));
            return response;
        }
        try {
            checkMachineInputForErrors(machineInput);
        } catch (IllegalArgumentException iae) {
            response.setSuccess(false);
            response.setMessage(iae.getMessage());
            response.setCode(500);
            return response;
        }

        response.setMachine(repo.createMachineForFactory(machineInput, factoryIRI));
        response.setCode(200);
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        return response;
    }

    public FactoryResponse removeMachineFromFactory(String machineId, String factoryId) {
        LoggingHelper.logMutation("removeMachineFromFactory", repo.getGraphNameForMutation());
        FactoryResponse response = new FactoryResponse();
        IRI machineIRI = null;
        IRI factoryIRI = null;
        try {
            machineIRI = Values.iri(machineId);
            factoryIRI = Values.iri(factoryId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Machine machine = repo.getMachineById(machineIRI);
        Factory factory = repo.getFactoryByIri(factoryIRI);
        if (machine == null || factory == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, machine == null ? machineId : factoryId));
            return response;
        }

        if (factory.getMachines().remove(machine)) {
            response.setFactory(repo.removeMachineFromFactory(machineIRI, factoryIRI));
            response.setCode(200);
            response.setSuccess(true);
            response.setMessage(MESSAGE.SUCCESS);
        } else {
            response.setCode(500);
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_CONTAINED, factoryId, machineId));
            response.setSuccess(false);
        }
        return response;
    }

    public FactoryResponse addHumanResourceToFactory(String humanResourceId, String factoryId) {
        LoggingHelper.logMutation("addHumanResourceToFactory", repo.getGraphNameForMutation());
        FactoryResponse response = new FactoryResponse();
        IRI humanResourceIRI = null;
        IRI factoryIRI = null;
        try {
            humanResourceIRI = Values.iri(humanResourceId);
            factoryIRI = Values.iri(factoryId);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            return response;
        }
        HumanResource humanResource = repo.getHumanResourceById(humanResourceIRI);
        Factory factory = repo.getFactoryByIri(factoryIRI);
        if (humanResource == null || factory == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND,
                            humanResource == null ? humanResourceId : factoryId));
            return response;
        }
        response.setFactory(repo.addHumanResourceToFactory(humanResourceIRI, factoryIRI));
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        response.setCode(200);
        return response;
    }

    public HumanResourceResponse createHumanResourceForFactory(
            HumanResourceInput humanResource, String factoryId) {
        LoggingHelper.logMutation("createHumanResourceForFactory", repo.getGraphNameForMutation());
        HumanResourceResponse response = new HumanResourceResponse();
        IRI factoryIRI = null;
        try {
            factoryIRI = Values.iri(factoryId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Factory factory = repo.getFactoryByIri(factoryIRI);
        if (factory == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, factoryId));
            return response;
        }
        try {
            checkHumanResourceInputForErrors(humanResource);
        } catch (IllegalArgumentException iae) {
            response.setSuccess(false);
            response.setMessage(iae.getMessage());
            response.setCode(500);
            return response;
        }

        response.setHumanResource(repo.createHumanResourceForFactory(humanResource, factoryIRI));
        response.setCode(200);
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        return response;
    }

    public FactoryResponse removeHumanResourceFromFactory(
            String humanResourceId, String factoryId) {
        LoggingHelper.logMutation("removeHumanResourceFromFactory", repo.getGraphNameForMutation());
        FactoryResponse response = new FactoryResponse();
        IRI humanResourceIRI = null;
        IRI factoryIRI = null;
        try {
            humanResourceIRI = Values.iri(humanResourceId);
            factoryIRI = Values.iri(factoryId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        HumanResource humanResource = repo.getHumanResourceById(humanResourceIRI);
        Factory factory = repo.getFactoryByIri(factoryIRI);
        if (humanResource == null || factory == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND,
                            humanResource == null ? humanResourceId : factoryId));
            return response;
        }

        if (factory.getHumanResources().remove(humanResource)) {
            response.setFactory(repo.removeHumanResourceFromFactory(humanResourceIRI, factoryIRI));
            response.setCode(200);
            response.setSuccess(true);
            response.setMessage(MESSAGE.SUCCESS);
        } else {
            response.setCode(500);
            response.setMessage(
                    String.format(MESSAGE.ENTITY_NOT_CONTAINED, factoryId, humanResourceId));
            response.setSuccess(false);
        }
        return response;
    }

    public FactoryResponse addProcessToFactory(String processId, String factoryId) {
        LoggingHelper.logMutation("addProcessToFactory", repo.getGraphNameForMutation());
        FactoryResponse response = new FactoryResponse();
        IRI processIRI = null;
        IRI factoryIRI = null;
        try {
            processIRI = Values.iri(processId);
            factoryIRI = Values.iri(factoryId);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            return response;
        }
        Process process = repo.getProcessById(processIRI);
        Factory factory = repo.getFactoryByIri(factoryIRI);
        if (process == null || factory == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, process == null ? processId : factoryId));
            return response;
        }
        response.setFactory(repo.addProcessToFactory(processIRI, factoryIRI));
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        response.setCode(200);
        return response;
    }

    public ProcessResponse createProcessForFactory(ProcessInput processInput, String factoryId) {
        LoggingHelper.logMutation("createProcessForFactory", repo.getGraphNameForMutation());
        ProcessResponse response = new ProcessResponse();
        IRI factoryIRI = null;
        try {
            factoryIRI = Values.iri(factoryId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Factory factory = repo.getFactoryByIri(factoryIRI);
        if (factory == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, factoryId));
            return response;
        }
        try {
            checkProcessInputForErrors(processInput);
        } catch (IllegalArgumentException iae) {
            response.setSuccess(false);
            response.setMessage(iae.getMessage());
            response.setCode(500);
            return response;
        }
        response.setProcess(repo.createProcessForFactory(processInput, factoryIRI));
        response.setCode(200);
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        return response;
    }

    public FactoryResponse removeProcessFromFactory(String processId, String factoryId) {
        LoggingHelper.logMutation("removeProcessFromFactory", repo.getGraphNameForMutation());
        FactoryResponse response = new FactoryResponse();
        IRI processIRI = null;
        IRI factoryIRI = null;
        try {
            processIRI = Values.iri(processId);
            factoryIRI = Values.iri(factoryId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Process process = repo.getProcessById(processIRI);
        Factory factory = repo.getFactoryByIri(factoryIRI);
        if (process == null || factory == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, process == null ? processId : factoryId));
            return response;
        }

        if (factory.getProcesses().remove(process)) {
            response.setFactory(repo.removeProcessFromFactory(processIRI, factoryIRI));
            response.setCode(200);
            response.setSuccess(true);
            response.setMessage(MESSAGE.SUCCESS);
        } else {
            response.setCode(500);
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_CONTAINED, factoryId, processId));
            response.setSuccess(false);
        }
        return response;
    }
}
