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

import static de.fraunhofer.iosb.ilt.ams.repository.ProcessRepository.checkProcessInputForErrors;
import static de.fraunhofer.iosb.ilt.ams.repository.SemanticReferenceRepository.createAndReturnSemanticReference;

import de.fraunhofer.iosb.ilt.ams.dao.CapabilityDAO;
import de.fraunhofer.iosb.ilt.ams.model.*;
import de.fraunhofer.iosb.ilt.ams.model.Process;
import de.fraunhofer.iosb.ilt.ams.model.filter.CapabilityFilter;
import de.fraunhofer.iosb.ilt.ams.model.input.CapabilityInput;
import de.fraunhofer.iosb.ilt.ams.model.input.ProcessInput;
import de.fraunhofer.iosb.ilt.ams.model.input.PropertyInput;
import de.fraunhofer.iosb.ilt.ams.model.input.SemanticReferenceInput;
import de.fraunhofer.iosb.ilt.ams.model.response.CapabilityResponse;
import de.fraunhofer.iosb.ilt.ams.model.response.ProcessResponse;
import de.fraunhofer.iosb.ilt.ams.model.response.PropertyResponse;
import de.fraunhofer.iosb.ilt.ams.model.response.SemanticReferenceResponse;
import de.fraunhofer.iosb.ilt.ams.utility.LoggingHelper;
import de.fraunhofer.iosb.ilt.ams.utility.MESSAGE;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CapabilityRepository {

    @Autowired CapabilityDAO capabilityDAO;

    @Autowired ObjectRdf4jRepository repo;

    private static final Logger logger = LoggerFactory.getLogger("query");
    private static final Logger mutationLogger = LoggerFactory.getLogger("mutation");

    public List<Capability> getCapabilities(CapabilityFilter capabilityFilter) {
        repo.emptyProcessedIds();
        List<Capability> capabilities =
                capabilityDAO.list().stream().distinct().collect(Collectors.toList());
        if (capabilityFilter != null && capabilityFilter.getId() != null) {
            capabilities =
                    capabilities.stream()
                            .filter(
                                    capability ->
                                            Values.iri(capabilityFilter.getId())
                                                    .equals(capability.getId()))
                            .collect(Collectors.toList());
        }
        if (capabilityFilter != null && capabilityFilter.getSourceId() != null) {
            capabilities =
                    capabilities.stream()
                            .filter(
                                    capability ->
                                            capabilityFilter
                                                    .getSourceId()
                                                    .equals(capability.getSourceId()))
                            .collect(Collectors.toList());
        }
        if (capabilityFilter != null && capabilityFilter.getSemanticReferenceId() != null) {
            capabilities =
                    capabilities.stream()
                            .filter(
                                    capability ->
                                            capability.getSemanticReferences().stream()
                                                    .anyMatch(
                                                            semanticReference ->
                                                                    Values.iri(
                                                                                    capabilityFilter
                                                                                            .getSemanticReferenceId())
                                                                            .equals(
                                                                                    semanticReference
                                                                                            .getId())))
                            .collect(Collectors.toList());
        }
        logger.trace(
                String.format(
                        MESSAGE.LOG_MESSAGE,
                        repo.getGraphNameForQuery().getQueryString().substring(4),
                        "getCapabilities"));
        return capabilities.stream().distinct().collect(Collectors.toList());
    }

    public CapabilityResponse createCapability(
            CapabilityInput capabilityInput,
            List<String> parentCapabilityIds,
            List<String> childCapabilityIds) {
        CapabilityResponse response = new CapabilityResponse();
        try {
            checkCapabilityInputForErrors(capabilityInput);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getLocalizedMessage());
            response.setSuccess(false);
            return response;
        }
        List<IRI> parentIRIs = new LinkedList<>();
        List<IRI> childIRIs = new LinkedList<>();

        if (parentCapabilityIds != null) {
            for (var parent : parentCapabilityIds) {
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

        if (childCapabilityIds != null) {
            for (var child : childCapabilityIds) {
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
        this.logMutation("createCapability");
        response.setCapability(repo.createCapability(capabilityInput, parentIRIs, childIRIs));
        response.setMessage(MESSAGE.SUCCESS);
        response.setCode(200);
        response.setSuccess(true);
        return response;
    }

    public CapabilityResponse updateCapability(
            String capabilityId,
            CapabilityInput capabilityInput,
            List<String> parentCapabilityIds,
            List<String> childCapabilityIds) {
        CapabilityResponse response = new CapabilityResponse();
        IRI iri = null;
        try {
            iri = Values.iri(capabilityId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            response.setCode(500);
            return response;
        }
        Capability capability = repo.getCapabilityById(iri);
        if (capability == null) {
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, capabilityId));
            response.setSuccess(false);
            response.setCode(500);
            return response;
        }
        List<IRI> parentIRIs = new LinkedList<>();
        List<IRI> childIRIs = new LinkedList<>();
        if (parentCapabilityIds != null) {
            for (var parent : parentCapabilityIds) {
                try {
                    parentIRIs.add(Values.iri(parent));
                } catch (IllegalArgumentException | NullPointerException e) {
                    response.setCode(500);
                    response.setMessage(e.getLocalizedMessage());
                    response.setSuccess(false);
                    return response;
                }
            }
        }

        if (childCapabilityIds != null) {
            for (var child : childCapabilityIds) {
                try {
                    childIRIs.add(Values.iri(child));
                } catch (IllegalArgumentException | NullPointerException e) {
                    response.setCode(500);
                    response.setMessage(e.getLocalizedMessage());
                    response.setSuccess(false);
                    return response;
                }
            }
        }
        this.logMutation("updateCapability");
        response.setCapability(repo.updateCapability(iri, capabilityInput, parentIRIs, childIRIs));
        response.setCode(200);
        response.setSuccess(true);
        response.setMessage(MESSAGE.SUCCESS);
        return response;
    }

    public CapabilityResponse deleteCapability(String capabilityId, boolean deleteChildren) {
        CapabilityResponse response = new CapabilityResponse();
        IRI iri = null;
        try {
            iri = Values.iri(capabilityId);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            return response;
        }
        if (deleteChildren) {
            for (var child : repo.getCapabilityById(iri).getChildCapabilities()) {
                repo.deleteCapability(child.getId());
            }
        }
        this.logMutation("deleteCapability");
        response.setSuccess(repo.deleteCapability(iri));
        response.setCode(200);
        response.setSuccess(true);
        response.setMessage(String.format(MESSAGE.DELETED, iri));
        return response;
    }

    public static Capability createAndReturnCapability(CapabilityInput input)
            throws IllegalArgumentException {
        checkCapabilityInputForErrors(input);
        Capability capability = new Capability();
        capability.setSourceId(input.getSourceId());
        capability.setLabel(input.getLabel());
        capability.setLabelLanguageCode(input.getLabelLanguageCode());
        capability.setDescription(input.getDescription());
        capability.setDescriptionLanguageCode(input.getDescriptionLanguageCode());
        if (input.getSemanticReferences() != null) {
            for (var semRef : input.getSemanticReferences()) {
                capability.addSemanticReference(createAndReturnSemanticReference(semRef));
            }
        }
        return capability;
    }

    public static void checkCapabilityInputForErrors(CapabilityInput input)
            throws IllegalArgumentException {
        if (input.getSemanticReferences() == null
                && (input.getDescription() == null || input.getLabel() == null)) {
            throw new IllegalArgumentException(MESSAGE.NOT_ENOUGH_ARGUMENTS_GENERIC);
        }
        if ((input.getSemanticReferences() != null && input.getSemanticReferences().isEmpty())
                && (input.getDescription().isBlank() || input.getLabel().isBlank())) {
            throw new IllegalArgumentException(MESSAGE.NOT_ENOUGH_ARGUMENTS_GENERIC);
        }
        if (input.getLabel() != null && input.getLabelLanguageCode() == null) {
            throw new IllegalArgumentException(
                    String.format(MESSAGE.LANGUAGE_ERROR, input.getLabel()));
        }
        if (input.getDescription() != null && input.getDescriptionLanguageCode() == null) {
            throw new IllegalArgumentException(
                    String.format(MESSAGE.LANGUAGE_ERROR, input.getDescription()));
        }
    }

    public CapabilityResponse addPropertyToCapability(String propertyId, String capabilityId) {
        CapabilityResponse response = new CapabilityResponse();
        IRI propertyIRI = null;
        IRI capabilityIRI = null;
        try {
            propertyIRI = Values.iri(propertyId);
            capabilityIRI = Values.iri(capabilityId);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            return response;
        }
        Property property = repo.getPropertyById(propertyIRI);
        Capability capability = repo.getCapabilityById(capabilityIRI);
        if (property == null || capability == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND,
                            property == null ? propertyId : capabilityId));
            return response;
        }
        this.logMutation("addPropertyToCapability");
        response.setCapability(repo.addPropertyToCapability(propertyIRI, capabilityIRI));
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        response.setCode(200);
        return response;
    }

    public PropertyResponse createPropertyForCapability(
            PropertyInput propertyInput, String capabilityId) {
        PropertyResponse response = new PropertyResponse();
        IRI capabilityIRI = null;
        try {
            capabilityIRI = Values.iri(capabilityId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Capability capability = repo.getCapabilityById(capabilityIRI);
        if (capability == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, capabilityId));
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
        this.logMutation("createPropertyForCapability");

        response.setProperty(repo.createPropertyForCapability(propertyInput, capabilityIRI));
        response.setCode(200);
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        return response;
    }

    public CapabilityResponse removePropertyFromCapability(String propertyId, String capabilityId) {
        CapabilityResponse response = new CapabilityResponse();
        IRI propertyIRI = null;
        IRI capabilityIRI = null;
        try {
            propertyIRI = Values.iri(propertyId);
            capabilityIRI = Values.iri(capabilityId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Property property = repo.getPropertyById(propertyIRI);
        Capability capability = repo.getCapabilityById(capabilityIRI);
        if (property == null || capability == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND,
                            property == null ? propertyId : capabilityId));
            return response;
        }

        if (capability.getProperties().remove(property)) {
            response.setCapability(repo.removePropertyFromCapability(propertyIRI, capabilityIRI));
            response.setCode(200);
            response.setSuccess(true);
            response.setMessage(MESSAGE.SUCCESS);
        } else {
            response.setCode(500);
            response.setMessage(
                    String.format(MESSAGE.ENTITY_NOT_CONTAINED, capabilityId, propertyId));
            response.setSuccess(false);
        }
        this.logMutation("removePropertyFromCapability");

        return response;
    }

    public CapabilityResponse addProcessToCapability(String processId, String capabilityId) {
        CapabilityResponse response = new CapabilityResponse();
        IRI processIRI = null;
        IRI capabilityIRI = null;
        try {
            processIRI = Values.iri(processId);
            capabilityIRI = Values.iri(capabilityId);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            return response;
        }
        Process process = repo.getProcessById(processIRI);
        Capability capability = repo.getCapabilityById(capabilityIRI);
        if (process == null || capability == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, process == null ? processId : capabilityId));
            return response;
        }
        this.logMutation("addProcessToCapability");

        response.setCapability(repo.addProcessToCapability(processIRI, capabilityIRI));
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        response.setCode(200);
        return response;
    }

    public ProcessResponse createProcessForCapability(
            ProcessInput processInput, String capabilityId) {
        ProcessResponse response = new ProcessResponse();
        IRI capabilityIRI = null;
        try {
            capabilityIRI = Values.iri(capabilityId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Capability capability = repo.getCapabilityById(capabilityIRI);
        if (capability == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, capabilityId));
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
        this.logMutation("createProcessForCapability");

        response.setProcess(repo.createProcessForCapability(processInput, capabilityIRI));
        response.setCode(200);
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        return response;
    }

    public CapabilityResponse removeProcessFromCapability(String processId, String capabilityId) {
        CapabilityResponse response = new CapabilityResponse();
        IRI processIRI = null;
        IRI capabilityIRI = null;
        try {
            processIRI = Values.iri(processId);
            capabilityIRI = Values.iri(capabilityId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Process process = repo.getProcessById(processIRI);
        Capability capability = repo.getCapabilityById(capabilityIRI);
        if (process == null || capability == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, process == null ? processId : capabilityId));
            return response;
        }

        if (capability.getProcesses().remove(process)) {
            response.setCapability(repo.removeProcessFromCapability(processIRI, capabilityIRI));
            response.setCode(200);
            response.setSuccess(true);
            response.setMessage(MESSAGE.SUCCESS);
        } else {
            response.setCode(500);
            response.setMessage(
                    String.format(MESSAGE.ENTITY_NOT_CONTAINED, capabilityId, processId));
            response.setSuccess(false);
        }
        this.logMutation("removeProcessFromCapability");

        return response;
    }

    public CapabilityResponse addSemanticReferenceToCapability(
            String semanticReferenceId, String capabilityId) {
        CapabilityResponse response = new CapabilityResponse();
        IRI semRefIRI = null;
        IRI capabilityIRI = null;
        try {
            semRefIRI = Values.iri(semanticReferenceId);
            capabilityIRI = Values.iri(capabilityId);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            return response;
        }
        SemanticReference semRef = repo.getSemanticReferenceById(semRefIRI);
        Capability capability = repo.getCapabilityById(capabilityIRI);
        if (semRef == null || capability == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND,
                            semRef == null ? semanticReferenceId : capabilityId));
            return response;
        }
        this.logMutation("addSemanticReferenceToCapability");

        response.setCapability(repo.addSemanticReferenceToCapability(semRefIRI, capabilityIRI));
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        response.setCode(200);
        return response;
    }

    public SemanticReferenceResponse createSemanticReferenceForCapability(
            SemanticReferenceInput semanticReferenceInput, String capabilityId) {
        SemanticReferenceResponse response = new SemanticReferenceResponse();
        IRI capabilityIRI = null;
        try {
            capabilityIRI = Values.iri(capabilityId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Capability capability = repo.getCapabilityById(capabilityIRI);
        if (capability == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, capabilityId));
            return response;
        }
        try {
            SemanticReferenceRepository.checkSemanticReferenceInputForErrors(
                    semanticReferenceInput);
        } catch (IllegalArgumentException iae) {
            response.setSuccess(false);
            response.setMessage(iae.getMessage());
            response.setCode(500);
            return response;
        }
        this.logMutation("createSemanticReferenceForCapability");

        response.setSemanticReference(
                repo.createSemanticReferenceForCapability(semanticReferenceInput, capabilityIRI));
        response.setCode(200);
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        return response;
    }

    public CapabilityResponse removeSemanticReferenceFromCapability(
            String semanticReferenceId, String capabilityId) {
        CapabilityResponse response = new CapabilityResponse();
        IRI semanticReferenceIRI = null;
        IRI capabilityIRI = null;
        try {
            semanticReferenceIRI = Values.iri(semanticReferenceId);
            capabilityIRI = Values.iri(capabilityId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        SemanticReference semanticReference = repo.getSemanticReferenceById(semanticReferenceIRI);
        Capability capability = repo.getCapabilityById(capabilityIRI);
        if (semanticReference == null || capability == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND,
                            semanticReference == null ? semanticReferenceId : capabilityId));
            return response;
        }

        if (capability.getSemanticReferences().remove(semanticReference)) {
            response.setCapability(
                    repo.removeSemanticReferenceFromCapability(
                            semanticReferenceIRI, capabilityIRI));
            response.setCode(200);
            response.setSuccess(true);
            response.setMessage(MESSAGE.SUCCESS);
        } else {
            response.setCode(500);
            response.setMessage(
                    String.format(MESSAGE.ENTITY_NOT_CONTAINED, capabilityId, semanticReferenceId));
            response.setSuccess(false);
        }
        this.logMutation("removeSemanticReferenceFromCapability");

        return response;
    }

    public CapabilityResponse addProductionResourceToCapability(String prId, String capabilityId) {
        CapabilityResponse response = new CapabilityResponse();
        IRI prIRI = null;
        IRI capabilityIRI = null;
        try {
            prIRI = Values.iri(prId);
            capabilityIRI = Values.iri(capabilityId);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            return response;
        }
        ProductionResource productionResource = repo.getProductionResourceById(prIRI);
        Capability capability = repo.getCapabilityById(capabilityIRI);
        if (productionResource == null || capability == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND,
                            productionResource == null ? prId : capabilityId));
            return response;
        }
        this.logMutation("addProductionResourceToCapability");

        response.setCapability(repo.addProductionResourceToCapability(prIRI, capabilityIRI));
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        response.setCode(200);
        return response;
    }

    public CapabilityResponse removeProductionResourceFromCapability(
            String prId, String capabilityId) {
        CapabilityResponse response = new CapabilityResponse();
        IRI prIRI = null;
        IRI capabilityIRI = null;
        try {
            prIRI = Values.iri(prId);
            capabilityIRI = Values.iri(capabilityId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        ProductionResource productionResource = repo.getProductionResourceById(prIRI);
        Capability capability = repo.getCapabilityById(capabilityIRI);
        if (productionResource == null || capability == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND,
                            productionResource == null ? prId : capabilityId));
            return response;
        }

        if (capability.getProductionResources().remove(productionResource)) {
            response.setCapability(
                    repo.removeProductionResourceFromCapability(prIRI, capabilityIRI));
            response.setCode(200);
            response.setSuccess(true);
            response.setMessage(MESSAGE.SUCCESS);
        } else {
            response.setCode(500);
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_CONTAINED, capabilityId, prId));
            response.setSuccess(false);
        }
        LoggingHelper.logMutation(
                "removeProductionResourceFromCapability", repo.getGraphNameForMutation());

        return response;
    }

    public void logMutation(String methodName) {
        mutationLogger.trace(
                String.format(MESSAGE.LOG_MESSAGE, repo.getGraphNameForMutation(), methodName));
    }
}
