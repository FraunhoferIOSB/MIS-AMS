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

import de.fraunhofer.iosb.ilt.ams.dao.ProcessDAO;
import de.fraunhofer.iosb.ilt.ams.model.*;
import de.fraunhofer.iosb.ilt.ams.model.Process;
import de.fraunhofer.iosb.ilt.ams.model.filter.ProcessFilter;
import de.fraunhofer.iosb.ilt.ams.model.input.ProcessInput;
import de.fraunhofer.iosb.ilt.ams.model.input.PropertyInput;
import de.fraunhofer.iosb.ilt.ams.model.response.ProcessResponse;
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
public class ProcessRepository {

    @Autowired ProcessDAO processDAO;

    @Autowired ObjectRdf4jRepository repo;

    public List<Process> getProcesses(ProcessFilter filter) {
        LoggingHelper.logQuery("getProcesses", repo.getGraphNameForQuery().getQueryString());
        repo.emptyProcessedIds();
        List<Process> processes =
                processDAO.list().stream().distinct().collect(Collectors.toList());
        if (filter != null) {
            if (filter.getId() != null) {
                processes =
                        processes.stream()
                                .filter(
                                        process ->
                                                Values.iri(filter.getId()).equals(process.getId()))
                                .collect(Collectors.toList());
            }
            if (filter.getSourceId() != null) {
                processes =
                        processes.stream()
                                .filter(
                                        process ->
                                                filter.getSourceId().equals(process.getSourceId()))
                                .collect(Collectors.toList());
            }
        }
        return processes;
    }

    public static void checkProcessInputForErrors(ProcessInput input)
            throws IllegalArgumentException {
        if (input.getDescription() != null && input.getDescriptionLanguageCode() == null) {
            throw new IllegalArgumentException(
                    String.format(MESSAGE.LANGUAGE_ERROR, input.getDescription()));
        }
    }

    public ProcessResponse createProcess(
            ProcessInput processInput,
            List<String> parentProcessIds,
            List<String> childProcessIds) {
        LoggingHelper.logMutation("createProcess", repo.getGraphNameForMutation());
        ProcessResponse response = new ProcessResponse();

        try {
            checkProcessInputForErrors(processInput);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getLocalizedMessage());
            response.setSuccess(false);
            return response;
        }
        List<IRI> parentIRIs = new LinkedList<>();
        List<IRI> childIRIs = new LinkedList<>();
        if (parentProcessIds != null) {
            for (var parent : parentProcessIds) {
                try {
                    parentIRIs.add(Values.iri(parent));
                    if (this.repo.getCapabilityById(Values.iri(parent)) == null) {
                        response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, parent));
                        response.setSuccess(false);
                        response.setCode(500);
                        return response;
                    }
                } catch (IllegalArgumentException | NullPointerException e) {
                    response.setCode(500);
                    response.setMessage(e.getLocalizedMessage());
                    response.setSuccess(false);
                    return response;
                }
            }
        }

        if (childProcessIds != null) {
            for (var child : childProcessIds) {
                try {
                    childIRIs.add(Values.iri(child));
                    if (this.repo.getCapabilityById(Values.iri(child)) == null) {
                        response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, child));
                        response.setSuccess(false);
                        response.setCode(500);
                        return response;
                    }
                } catch (IllegalArgumentException | NullPointerException e) {
                    response.setCode(500);
                    response.setMessage(e.getLocalizedMessage());
                    response.setSuccess(false);
                    return response;
                }
            }
        }

        response.setProcess(repo.createProcess(processInput, parentIRIs, childIRIs));
        response.setMessage(MESSAGE.SUCCESS);
        response.setCode(200);
        response.setSuccess(true);
        return response;
    }

    public ProcessResponse updateProcess(
            String processId,
            ProcessInput processInput,
            List<String> parentProcessIds,
            List<String> childProcessIds) {
        LoggingHelper.logMutation("updateProcess", repo.getGraphNameForMutation());
        ProcessResponse response = new ProcessResponse();
        IRI iri = null;
        try {
            iri = Values.iri(processId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            response.setCode(500);
            return response;
        }
        Process process = repo.getProcessById(iri);
        if (process == null) {
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, processId));
            response.setSuccess(false);
            response.setCode(500);
            return response;
        }
        List<IRI> parentIRIs = new LinkedList<>();
        List<IRI> childIRIs = new LinkedList<>();
        if (parentProcessIds != null) {
            for (var parent : parentProcessIds) {
                try {
                    parentIRIs.add(Values.iri(parent));
                    if (repo.getProcessById(Values.iri(parent)) == null) {
                        response.setCode(500);
                        response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, parent));
                        response.setSuccess(false);
                        return response;
                    }
                } catch (IllegalArgumentException | NullPointerException e) {
                    response.setCode(500);
                    response.setMessage(e.getLocalizedMessage());
                    response.setSuccess(false);
                    return response;
                }
            }
        }

        if (childProcessIds != null) {
            for (var child : childProcessIds) {
                try {
                    childIRIs.add(Values.iri(child));
                    if (repo.getProcessById(Values.iri(child)) == null) {
                        response.setCode(500);
                        response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, child));
                        response.setSuccess(false);
                        return response;
                    }
                } catch (IllegalArgumentException | NullPointerException e) {
                    response.setCode(500);
                    response.setMessage(e.getLocalizedMessage());
                    response.setSuccess(false);
                    return response;
                }
            }
        }

        response.setProcess(repo.updateProcess(iri, processInput, parentIRIs, childIRIs));
        response.setSuccess(true);
        response.setMessage(MESSAGE.SUCCESS);
        response.setCode(200);
        return response;
    }

    public ProcessResponse deleteProcess(String processId, boolean deleteChildren) {
        LoggingHelper.logMutation("deleteProcess", repo.getGraphNameForMutation());
        ProcessResponse response = new ProcessResponse();
        IRI iri = null;
        try {
            iri = Values.iri(processId);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            return response;
        }
        if (deleteChildren) {
            for (var child : repo.getProcessById(iri).getChildProcesses()) {
                repo.deleteProcess(child.getId());
            }
        }
        response.setCode(200);
        response.setSuccess(repo.deleteProcess(iri));
        response.setMessage(String.format(MESSAGE.DELETED, iri));
        return response;
    }

    public ProcessResponse addRealizedCapabilityToProcess(String capabilityId, String processId) {
        LoggingHelper.logMutation("addRealizedCapabilityToProcess", repo.getGraphNameForMutation());
        ProcessResponse response = new ProcessResponse();
        IRI capabilityIRI = null;
        IRI processIRI = null;
        try {
            capabilityIRI = Values.iri(capabilityId);
            processIRI = Values.iri(processId);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            return response;
        }
        Capability capability = repo.getCapabilityById(capabilityIRI);
        Process process = repo.getProcessById(processIRI);
        if (process == null || capability == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, process == null ? processId : capabilityId));
            return response;
        }
        response.setProcess(repo.addRealizedCapabilityToProcess(capabilityIRI, processIRI));
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        response.setCode(200);
        return response;
    }

    public ProcessResponse removeRealizedCapabilityFromProcess(
            String capabilityId, String processId) {
        LoggingHelper.logMutation(
                "removeRealizedCapabilityFromProcess", repo.getGraphNameForMutation());
        ProcessResponse response = new ProcessResponse();
        IRI capabilityIRI = null;
        IRI processIRI = null;
        try {
            capabilityIRI = Values.iri(capabilityId);
            processIRI = Values.iri(processId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Capability capability = repo.getCapabilityById(capabilityIRI);
        Process process = repo.getProcessById(processIRI);
        if (process == null || capability == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, process == null ? processId : capabilityId));
            return response;
        }

        if (process.getRealizedCapabilities().remove(capability)) {
            response.setProcess(
                    repo.removeRealizedCapabilityFromProcess(capabilityIRI, processIRI));
            response.setCode(200);
            response.setSuccess(true);
            response.setMessage(MESSAGE.SUCCESS);
        } else {
            response.setCode(500);
            response.setMessage(
                    String.format(MESSAGE.ENTITY_NOT_CONTAINED, processId, capabilityId));
            response.setSuccess(false);
        }
        return response;
    }

    public ProcessResponse addRequiredCapabilityToProcess(String capabilityId, String processId) {
        LoggingHelper.logMutation("addRequiredCapabilityToProcess", repo.getGraphNameForMutation());
        ProcessResponse response = new ProcessResponse();
        IRI capabilityIRI = null;
        IRI processIRI = null;
        try {
            capabilityIRI = Values.iri(capabilityId);
            processIRI = Values.iri(processId);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            return response;
        }
        Capability capability = repo.getCapabilityById(capabilityIRI);
        Process process = repo.getProcessById(processIRI);
        if (process == null || capability == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, process == null ? processId : capabilityId));
            return response;
        }
        response.setProcess(repo.addRequiredCapabilityToProcess(capabilityIRI, processIRI));
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        response.setCode(200);
        return response;
    }

    public ProcessResponse removeRequiredCapabilityFromProcess(
            String capabilityId, String processId) {
        LoggingHelper.logMutation(
                "removeRequiredCapabilityFromProcess", repo.getGraphNameForMutation());
        ProcessResponse response = new ProcessResponse();
        IRI capabilityIRI = null;
        IRI processIRI = null;
        try {
            capabilityIRI = Values.iri(capabilityId);
            processIRI = Values.iri(processId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Capability capability = repo.getCapabilityById(capabilityIRI);
        Process process = repo.getProcessById(processIRI);
        if (process == null || capability == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, process == null ? processId : capabilityId));
            return response;
        }

        if (process.getRequiredCapabilities().remove(capability)) {
            response.setProcess(
                    repo.removeRequiredCapabilityFromProcess(capabilityIRI, processIRI));
            response.setCode(200);
            response.setSuccess(true);
            response.setMessage(MESSAGE.SUCCESS);
        } else {
            response.setCode(500);
            response.setMessage(
                    String.format(MESSAGE.ENTITY_NOT_CONTAINED, processId, capabilityId));
            response.setSuccess(false);
        }
        return response;
    }

    public ProcessResponse addProductionResourceToProcess(String prId, String processId) {
        LoggingHelper.logMutation("addProductionResourceToProcess", repo.getGraphNameForMutation());
        ProcessResponse response = new ProcessResponse();
        IRI prIRI = null;
        IRI processIRI = null;
        try {
            prIRI = Values.iri(prId);
            processIRI = Values.iri(processId);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            return response;
        }
        ProductionResource productionResource = repo.getProductionResourceById(prIRI);
        Process process = repo.getProcessById(processIRI);
        if (process == null || productionResource == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(MESSAGE.ENTITY_NOT_FOUND, process == null ? processId : prId));
            return response;
        }
        response.setProcess(repo.addProductionResourceToProcess(prIRI, processIRI));
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        response.setCode(200);
        return response;
    }

    public ProcessResponse removeProductionResourceFromProcess(String prId, String processId) {
        LoggingHelper.logMutation(
                "removeProductionResourceFromProcess", repo.getGraphNameForMutation());
        ProcessResponse response = new ProcessResponse();
        IRI prIRI = null;
        IRI processIRI = null;
        try {
            prIRI = Values.iri(prId);
            processIRI = Values.iri(processId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        ProductionResource productionResource = repo.getProductionResourceById(prIRI);
        Process process = repo.getProcessById(processIRI);
        if (process == null || productionResource == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(MESSAGE.ENTITY_NOT_FOUND, process == null ? processId : prId));
            return response;
        }

        if (process.getUsedProductionResources().remove(productionResource)) {
            response.setProcess(repo.removeProductionResourceFromProcess(prIRI, processIRI));
            response.setCode(200);
            response.setSuccess(true);
            response.setMessage(MESSAGE.SUCCESS);
        } else {
            response.setCode(500);
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_CONTAINED, processId, prId));
            response.setSuccess(false);
        }
        return response;
    }

    public ProcessResponse addInputProductToProcess(
            String productId, InputProductType type, String processId) {
        LoggingHelper.logMutation("addInputProductToProcess", repo.getGraphNameForMutation());
        ProcessResponse response = new ProcessResponse();
        IRI productIRI = null;
        IRI processIRI = null;
        try {
            productIRI = Values.iri(productId);
            processIRI = Values.iri(processId);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            return response;
        }
        Product product = repo.getProductById(productIRI);
        Process process = repo.getProcessById(processIRI);
        if (process == null || product == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, process == null ? processId : productId));
            return response;
        }
        response.setProcess(repo.addInputProductToProcess(productIRI, type, processIRI));
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        response.setCode(200);
        return response;
    }

    public ProcessResponse removeInputProductFromProcess(
            String productId, InputProductType type, String processId) {
        LoggingHelper.logMutation("removeInputProductFromProcess", repo.getGraphNameForMutation());
        ProcessResponse response = new ProcessResponse();
        IRI productIRI = null;
        IRI processIRI = null;
        try {
            productIRI = Values.iri(productId);
            processIRI = Values.iri(processId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Product product = repo.getProductById(productIRI);
        Process process = repo.getProcessById(processIRI);
        if (process == null || product == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, process == null ? processId : productId));
            return response;
        }
        boolean removable = false;
        List<IRI>[] productListArray = this.getInputProductIdsOfProcess(process);
        switch (type) {
            case OPERATING_MATERIAL:
                removable = productListArray[0].contains(product.getId());
                break;
            case RAW_MATERIAL:
                removable = productListArray[1].contains(product.getId());
                break;
            case AUXILIARY_MATERIAL:
                removable = productListArray[2].contains(product.getId());
                break;
            case PRELIMINARY_PRODUCT:
                removable = productListArray[3].contains(product.getId());
                break;
            default:
                removable =
                        process.getInputProducts().stream()
                                .map(ProductApplication::getProduct)
                                .anyMatch(product::equals);
                break;
        }
        if (!removable) {
            response.setCode(500);
            response.setMessage(
                    String.format(MESSAGE.ENTITY_NOT_CONTAINED, processIRI, productIRI));
            response.setSuccess(false);
            return response;
        }
        response.setProcess(repo.removeInputProductFromProcess(productIRI, type, processIRI));
        response.setCode(200);
        response.setSuccess(true);
        response.setMessage(MESSAGE.SUCCESS);
        return response;
    }

    private List<IRI>[] getInputProductIdsOfProcess(Process process) {
        repo.emptyProcessedIds();
        List<IRI> operatingMaterialProductApplicationList =
                process.getOperatingMaterials().stream()
                        .map(ProductApplication::getId)
                        .collect(Collectors.toList());
        List<IRI> operatingMaterialList = new LinkedList<>();
        for (IRI productApplication : operatingMaterialProductApplicationList) {
            var productApplicationObject = repo.getProductApplicationById(productApplication);
            operatingMaterialList.add(productApplicationObject.getProduct().getId());
        }

        List<IRI> rawMaterialProductApplicationList =
                process.getRawMaterials().stream()
                        .map(ProductApplication::getId)
                        .collect(Collectors.toList());
        List<IRI> rawMaterial = new LinkedList<>();
        for (IRI productApplicationIRI : rawMaterialProductApplicationList) {
            rawMaterial.add(
                    repo.getProductApplicationById(productApplicationIRI).getProduct().getId());
        }

        List<IRI> auxiliaryMaterialProductApplicationList =
                process.getAuxiliaryMaterials().stream()
                        .map(ProductApplication::getId)
                        .collect(Collectors.toList());
        List<IRI> auxiliaryMaterial = new LinkedList<>();
        for (IRI productApplication : auxiliaryMaterialProductApplicationList) {
            var productApplicationObject = repo.getProductApplicationById(productApplication);
            auxiliaryMaterial.add(productApplicationObject.getProduct().getId());
        }

        List<IRI> preliminaryProductApplicationList =
                process.getPreliminaryProducts().stream()
                        .map(ProductApplication::getId)
                        .collect(Collectors.toList());
        List<IRI> preliminary = new LinkedList<>();
        for (IRI productApplicationIRI : preliminaryProductApplicationList) {
            preliminary.add(
                    repo.getProductApplicationById(productApplicationIRI).getProduct().getId());
        }

        return new List[] {operatingMaterialList, rawMaterial, auxiliaryMaterial, preliminary};
    }

    public ProcessResponse addOutputProductToProcess(
            String productId, OutputProductType type, String processId) {
        LoggingHelper.logMutation("addOutputProductToProcess", repo.getGraphNameForMutation());
        ProcessResponse response = new ProcessResponse();
        IRI productIRI = null;
        IRI processIRI = null;
        try {
            productIRI = Values.iri(productId);
            processIRI = Values.iri(processId);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            return response;
        }
        Product product = repo.getProductById(productIRI);
        Process process = repo.getProcessById(processIRI);
        if (process == null || product == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, process == null ? processId : productId));
            return response;
        }
        response.setProcess(repo.addOutputProductToProcess(productIRI, type, processIRI));
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        response.setCode(200);
        return response;
    }

    public ProcessResponse removeOutputProductFromProcess(
            String productId, OutputProductType type, String processId) {
        LoggingHelper.logMutation("removeOutputProductFromProcess", repo.getGraphNameForMutation());
        ProcessResponse response = new ProcessResponse();
        IRI productIRI = null;
        IRI processIRI = null;
        try {
            productIRI = Values.iri(productId);
            processIRI = Values.iri(processId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Product product = repo.getProductById(productIRI);
        Process process = repo.getProcessById(processIRI);
        if (process == null || product == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, process == null ? processId : productId));
            return response;
        }

        boolean removable = false;
        switch (type) {
            case END_PRODUCT:
                removable = this.getOutputProductIdsOfProcess(process)[0].contains(product.getId());
                break;
            case WASTE_PRODUCT:
                removable = this.getOutputProductIdsOfProcess(process)[1].contains(product.getId());
                break;
            case BY_PRODUCT:
                removable = this.getOutputProductIdsOfProcess(process)[2].contains(product.getId());
                break;
            default:
                removable =
                        process.getOutputProducts().stream()
                                .map(ProductApplication::getProduct)
                                .anyMatch(product::equals);
        }
        if (!removable) {
            response.setCode(500);
            response.setMessage(
                    String.format(MESSAGE.ENTITY_NOT_CONTAINED, processIRI, productIRI));
            response.setSuccess(false);
            return response;
        }
        response.setProcess(repo.removeOutputProductFromProcess(productIRI, type, processIRI));
        response.setCode(200);
        response.setSuccess(true);
        response.setMessage(MESSAGE.SUCCESS);
        return response;
    }

    private List<IRI>[] getOutputProductIdsOfProcess(Process process) {
        repo.emptyProcessedIds();
        List<IRI> endProductApplicationList =
                process.getEndProducts().stream()
                        .map(ProductApplication::getId)
                        .collect(Collectors.toList());
        List<IRI> endProductList = new LinkedList<>();
        for (IRI productApplication : endProductApplicationList) {
            var productApplicationObject = repo.getProductApplicationById(productApplication);
            endProductList.add(productApplicationObject.getProduct().getId());
        }

        List<IRI> wasteProductApplicationList =
                process.getWasteProducts().stream()
                        .map(ProductApplication::getId)
                        .collect(Collectors.toList());
        List<IRI> wasteProduct = new LinkedList<>();
        for (IRI productApplicationIRI : wasteProductApplicationList) {
            wasteProduct.add(
                    repo.getProductApplicationById(productApplicationIRI).getProduct().getId());
        }

        List<IRI> byProductApplicationList =
                process.getByProducts().stream()
                        .map(ProductApplication::getId)
                        .collect(Collectors.toList());
        List<IRI> byProduct = new LinkedList<>();
        for (IRI productApplication : byProductApplicationList) {
            var productApplicationObject = repo.getProductApplicationById(productApplication);
            byProduct.add(productApplicationObject.getProduct().getId());
        }

        return new List[] {endProductList, wasteProduct, byProduct};
    }

    public ProcessResponse addPropertyToProcess(String propertyId, String processId) {
        LoggingHelper.logMutation("addPropertyToProcess", repo.getGraphNameForMutation());
        ProcessResponse response = new ProcessResponse();
        IRI propertyIRI = null;
        IRI processIRI = null;
        try {
            propertyIRI = Values.iri(propertyId);
            processIRI = Values.iri(processId);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            return response;
        }
        Property property = repo.getPropertyById(propertyIRI);
        Process process = repo.getProcessById(processIRI);
        if (process == null || property == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, process == null ? processId : propertyId));
            return response;
        }
        response.setProcess(repo.addPropertyToProcess(propertyIRI, processIRI));
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        response.setCode(200);
        return response;
    }

    public PropertyResponse createPropertyForProcess(
            PropertyInput propertyInput, String processId) {
        LoggingHelper.logMutation("createPropertyForProcess", repo.getGraphNameForMutation());
        PropertyResponse response = new PropertyResponse();
        IRI processIRI = null;
        try {
            processIRI = Values.iri(processId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Process process = repo.getProcessById(processIRI);
        if (process == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, processId));
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
        response.setProperty(repo.createPropertyForProcess(propertyInput, processIRI));
        response.setCode(200);
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        return response;
    }

    public ProcessResponse removePropertyFromProcess(String propertyId, String processId) {
        LoggingHelper.logMutation("removePropertyFromProcess", repo.getGraphNameForMutation());
        ProcessResponse response = new ProcessResponse();
        IRI propertyIRI = null;
        IRI processIRI = null;
        try {
            propertyIRI = Values.iri(propertyId);
            processIRI = Values.iri(processId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Property property = repo.getPropertyById(propertyIRI);
        Process process = repo.getProcessById(processIRI);
        if (process == null || property == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, process == null ? processId : propertyId));
            return response;
        }

        if (process.getProperties().remove(property)) {
            response.setProcess(repo.removePropertyFromProcess(propertyIRI, processIRI));
            response.setCode(200);
            response.setSuccess(true);
            response.setMessage(MESSAGE.SUCCESS);
        } else {
            response.setCode(500);
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_CONTAINED, processId, propertyId));
            response.setSuccess(false);
        }
        return response;
    }
}
