package de.fraunhofer.iosb.ilt.ams.repository;

import de.fraunhofer.iosb.ilt.ams.dao.ProductionResourceDAO;
import de.fraunhofer.iosb.ilt.ams.model.*;
import de.fraunhofer.iosb.ilt.ams.model.Process;
import de.fraunhofer.iosb.ilt.ams.model.filter.HumanResourceFilter;
import de.fraunhofer.iosb.ilt.ams.model.filter.MachineFilter;
import de.fraunhofer.iosb.ilt.ams.model.input.HumanResourceInput;
import de.fraunhofer.iosb.ilt.ams.model.input.MachineInput;
import de.fraunhofer.iosb.ilt.ams.model.input.PropertyInput;
import de.fraunhofer.iosb.ilt.ams.model.response.HumanResourceResponse;
import de.fraunhofer.iosb.ilt.ams.model.response.MachineResponse;
import de.fraunhofer.iosb.ilt.ams.model.response.PropertyResponse;
import de.fraunhofer.iosb.ilt.ams.utility.LoggingHelper;
import de.fraunhofer.iosb.ilt.ams.utility.MESSAGE;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ProductionResourceRepository {

    @Autowired ProductionResourceDAO productionResourceDAO;

    @Autowired ObjectRdf4jRepository repo;

    public List<Machine> getMachines(MachineFilter machineFilter) {
        LoggingHelper.logQuery("getMachines", repo.getGraphNameForQuery().getQueryString());
        List<Machine> machines =
                productionResourceDAO.getMachines().stream()
                        .distinct()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
        if (machineFilter != null) {
            if (machineFilter.getId() != null) {
                machines =
                        machines.stream()
                                .filter(
                                        machine ->
                                                Values.iri(machineFilter.getId())
                                                        .equals(machine.getId()))
                                .collect(Collectors.toList());
            }
            if (machineFilter.getSourceId() != null) {
                machines =
                        machines.stream()
                                .filter(
                                        machine ->
                                                machineFilter
                                                        .getSourceId()
                                                        .equals(machine.getSourceId()))
                                .collect(Collectors.toList());
            }
        }
        return machines;
    }

    public List<HumanResource> getHumanResources(HumanResourceFilter humanResourceFilter) {
        LoggingHelper.logQuery("getHumanResources", repo.getGraphNameForQuery().getQueryString());
        repo.emptyProcessedIds();
        List<HumanResource> humanResources =
                productionResourceDAO.getHumanResources().stream()
                        .distinct()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
        if (humanResourceFilter != null) {
            if (humanResourceFilter.getId() != null) {
                humanResources =
                        humanResources.stream()
                                .filter(
                                        humanResource ->
                                                Values.iri(humanResourceFilter.getId())
                                                        .equals(humanResource.getId()))
                                .collect(Collectors.toList());
            }
            if (humanResourceFilter.getSourceId() != null) {
                humanResources =
                        humanResources.stream()
                                .filter(
                                        humanResource ->
                                                humanResourceFilter
                                                        .getSourceId()
                                                        .equals(humanResource.getSourceId()))
                                .collect(Collectors.toList());
            }

            if (humanResourceFilter.getCertificateIds() != null) {
                for (var certificate : humanResourceFilter.getCertificateIds()) {
                    humanResources =
                            humanResources.stream()
                                    .filter(
                                            humanResource ->
                                                    humanResource.getCertificates().stream()
                                                            .map(Property::getId)
                                                            .distinct()
                                                            .collect(Collectors.toList())
                                                            .contains(Values.iri(certificate)))
                                    .collect(Collectors.toList());
                }
            }
            if (humanResourceFilter.getPropertyFilters() != null) {
                for (var property : humanResourceFilter.getPropertyFilters()) {
                    if (property.getId() != null) {
                        humanResources =
                                humanResources.stream()
                                        .filter(
                                                humanResource ->
                                                        humanResource.getProperties().stream()
                                                                .map(Property::getId)
                                                                .collect(Collectors.toList())
                                                                .contains(
                                                                        Values.iri(
                                                                                property.getId())))
                                        .collect(Collectors.toList());
                    }
                    if (property.getSourceId() != null) {
                        humanResources =
                                humanResources.stream()
                                        .filter(
                                                humanResource ->
                                                        humanResource.getProperties().stream()
                                                                .map(Property::getSourceId)
                                                                .collect(Collectors.toList())
                                                                .contains(property.getSourceId()))
                                        .collect(Collectors.toList());
                    }

                    if (property.getSemanticReferenceId() != null) {
                        humanResources =
                                humanResources.stream()
                                        .filter(
                                                hr ->
                                                        hr.getProperties().stream()
                                                                .map(
                                                                        Property
                                                                                ::getSemanticReferences)
                                                                .findAny()
                                                                .orElseThrow()
                                                                .stream()
                                                                .map(SemanticReference::getId)
                                                                .collect(Collectors.toList())
                                                                .contains(
                                                                        Values.iri(
                                                                                property
                                                                                        .getSemanticReferenceId())))
                                        .collect(Collectors.toList());
                    }
                }
            }
        }
        return humanResources;
    }

    public MachineResponse createMachine(MachineInput machineInput) {
        LoggingHelper.logMutation("createMachine", repo.getGraphNameForMutation());
        MachineResponse response = new MachineResponse();

        response.setMachine(repo.createMachine(machineInput));
        response.setSuccess(true);
        response.setCode(200);
        response.setMessage(MESSAGE.SUCCESS);
        return response;
    }

    public MachineResponse updateMachine(String id, MachineInput machineInput) {
        LoggingHelper.logMutation("updateMachine", repo.getGraphNameForMutation());
        MachineResponse response = new MachineResponse();
        IRI iri = null;
        try {
            iri = Values.iri(id);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            response.setCode(500);
            return response;
        }
        Machine machine = repo.getMachineById(iri);
        if (machine == null) {
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, iri));
            response.setSuccess(false);
            response.setCode(500);
            return response;
        }
        response.setMachine(repo.updateMachine(iri, machineInput));
        response.setCode(200);
        response.setSuccess(true);
        response.setMessage(MESSAGE.SUCCESS);
        return response;
    }

    public MachineResponse deleteMachine(String machineId) {
        LoggingHelper.logMutation("deleteMachine", repo.getGraphNameForMutation());
        MachineResponse response = new MachineResponse();
        response.setSuccess(repo.deleteMachine(machineId));
        response.setCode(200);
        response.setMessage(MESSAGE.SUCCESS);
        return response;
    }

    public HumanResourceResponse createHumanResource(HumanResourceInput humanResourceInput) {
        LoggingHelper.logMutation("createHumanResource", repo.getGraphNameForMutation());
        HumanResourceResponse response = new HumanResourceResponse();

        response.setHumanResource(repo.createHumanResource(humanResourceInput));
        response.setSuccess(true);
        response.setCode(200);
        response.setMessage(MESSAGE.SUCCESS);
        return response;
    }

    public HumanResourceResponse updateHumanResource(
            String id, HumanResourceInput humanResourceInput) {
        LoggingHelper.logMutation("updateHumanResource", repo.getGraphNameForMutation());
        HumanResourceResponse response = new HumanResourceResponse();
        IRI iri = null;
        try {
            iri = Values.iri(id);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            response.setCode(500);
            return response;
        }
        HumanResource humanResource = repo.getHumanResourceById(iri);
        if (humanResource == null) {
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, iri));
            response.setSuccess(false);
            response.setCode(500);
            return response;
        }
        response.setHumanResource(repo.updateHumanResource(iri, humanResourceInput));
        response.setCode(200);
        response.setSuccess(true);
        response.setMessage(MESSAGE.SUCCESS);
        return response;
    }

    public HumanResourceResponse deleteHumanResource(String humanResourceId) {
        LoggingHelper.logMutation("deleteHumanResource", repo.getGraphNameForMutation());
        HumanResourceResponse response = new HumanResourceResponse();
        response.setSuccess(repo.deleteHumanResource(humanResourceId));
        response.setCode(200);
        response.setMessage(MESSAGE.SUCCESS);
        return response;
    }

    public static void checkMachineInputForErrors(MachineInput input)
            throws IllegalArgumentException {
        if (input.getLabel() != null && input.getLabelLanguageCode() == null) {
            throw new IllegalArgumentException(
                    String.format(MESSAGE.LANGUAGE_ERROR, input.getLabel()));
        }
        if (input.getDescription() != null && input.getDescriptionLanguageCode() == null) {
            throw new IllegalArgumentException(
                    String.format(MESSAGE.LANGUAGE_ERROR, input.getDescription()));
        }
    }

    public static void checkHumanResourceInputForErrors(HumanResourceInput input)
            throws IllegalArgumentException {
        if (input.getLabel() != null && input.getLabelLanguageCode() == null) {
            throw new IllegalArgumentException(
                    String.format(MESSAGE.LANGUAGE_ERROR, input.getLabel()));
        }
        if (input.getDescription() != null && input.getDescriptionLanguageCode() == null) {
            throw new IllegalArgumentException(
                    String.format(MESSAGE.LANGUAGE_ERROR, input.getDescription()));
        }
    }

    public MachineResponse addPropertyToMachine(String propertyId, String machineId) {
        LoggingHelper.logMutation("addPropertyToMachine", repo.getGraphNameForMutation());
        MachineResponse response = new MachineResponse();
        IRI propertyIRI = null;
        IRI machineIRI = null;
        try {
            propertyIRI = Values.iri(propertyId);
            machineIRI = Values.iri(machineId);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            return response;
        }
        Property property = repo.getPropertyById(propertyIRI);
        Machine machine = repo.getMachineById(machineIRI);
        if (property == null || machine == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, property == null ? propertyId : machineId));
            return response;
        }
        response.setMachine(repo.addPropertyToMachine(propertyIRI, machineIRI));
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        response.setCode(200);
        return response;
    }

    public PropertyResponse createPropertyForMachine(
            PropertyInput propertyInput, String machineId) {
        LoggingHelper.logMutation("createPropertyForMachine", repo.getGraphNameForMutation());
        PropertyResponse response = new PropertyResponse();
        IRI machineIRI = null;
        try {
            machineIRI = Values.iri(machineId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Machine machine = repo.getMachineById(machineIRI);
        if (machine == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, machineId));
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

        response.setProperty(repo.createPropertyForMachine(propertyInput, machineIRI));
        response.setCode(200);
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        return response;
    }

    public MachineResponse removePropertyFromMachine(String propertyId, String machineId) {
        LoggingHelper.logMutation("removePropertyFromMachine", repo.getGraphNameForMutation());
        MachineResponse response = new MachineResponse();
        IRI propertyIRI = null;
        IRI machineIRI = null;
        try {
            propertyIRI = Values.iri(propertyId);
            machineIRI = Values.iri(machineId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Property property = repo.getPropertyById(propertyIRI);
        Machine machine = repo.getMachineById(machineIRI);
        if (property == null || machine == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, property == null ? propertyId : machineId));
            return response;
        }

        if (machine.getMachineProperties().remove(property)) {
            response.setMachine(repo.removePropertyFromMachine(propertyIRI, machineIRI));
            response.setCode(200);
            response.setSuccess(true);
            response.setMessage(MESSAGE.SUCCESS);
        } else {
            response.setCode(500);
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_CONTAINED, machineId, propertyId));
            response.setSuccess(false);
        }
        return response;
    }

    public MachineResponse addProcessToMachine(String processId, String machineId) {
        LoggingHelper.logMutation("addProcessToMachine", repo.getGraphNameForMutation());
        MachineResponse response = new MachineResponse();
        IRI processIRI = null;
        IRI machineIRI = null;
        try {
            processIRI = Values.iri(processId);
            machineIRI = Values.iri(machineId);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            return response;
        }
        Process process = repo.getProcessById(processIRI);
        Machine machine = repo.getMachineById(machineIRI);
        if (process == null || machine == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, process == null ? processId : machineId));
            return response;
        }
        response.setMachine(repo.addProcessToMachine(processIRI, machineIRI));
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        response.setCode(200);
        return response;
    }

    public MachineResponse removeProcessFromMachine(String processId, String machineId) {
        LoggingHelper.logMutation("removeProcessFromMachine", repo.getGraphNameForMutation());
        MachineResponse response = new MachineResponse();
        IRI processIRI = null;
        IRI machineIRI = null;
        try {
            processIRI = Values.iri(processId);
            machineIRI = Values.iri(machineId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Process process = repo.getProcessById(processIRI);
        Machine machine = repo.getMachineById(machineIRI);
        if (process == null || machine == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, process == null ? processId : machineId));
            return response;
        }

        if (machine.getProvidedProcesses().remove(process)) {
            response.setMachine(repo.removeProcessFromMachine(processIRI, machineIRI));
            response.setCode(200);
            response.setSuccess(true);
            response.setMessage(MESSAGE.SUCCESS);
        } else {
            response.setCode(500);
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_CONTAINED, machineId, processId));
            response.setSuccess(false);
        }
        return response;
    }

    public MachineResponse addCapabilityToMachine(String capabilityId, String machineId) {
        LoggingHelper.logMutation("addCapabilityToMachine", repo.getGraphNameForMutation());
        MachineResponse response = new MachineResponse();
        IRI capabilityIRI = null;
        IRI machineIRI = null;
        try {
            capabilityIRI = Values.iri(capabilityId);
            machineIRI = Values.iri(machineId);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            return response;
        }
        Capability capability = repo.getCapabilityById(capabilityIRI);
        Machine machine = repo.getMachineById(machineIRI);
        if (capability == null || machine == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND,
                            capability == null ? capabilityId : machineId));
            return response;
        }
        response.setMachine(repo.addCapabilityToMachine(capabilityIRI, machineIRI));
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        response.setCode(200);
        return response;
    }

    public MachineResponse removeCapabilityFromMachine(String capabilityId, String machineId) {
        LoggingHelper.logMutation("removeCapabilityFromMachine", repo.getGraphNameForMutation());
        MachineResponse response = new MachineResponse();
        IRI capabilityIRI = null;
        IRI machineIRI = null;
        try {
            capabilityIRI = Values.iri(capabilityId);
            machineIRI = Values.iri(machineId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Capability capability = repo.getCapabilityById(capabilityIRI);
        Machine machine = repo.getMachineById(machineIRI);
        if (capability == null || machine == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND,
                            capability == null ? capabilityId : machineId));
            return response;
        }

        if (machine.getProvidedCapabilities().remove(capability)) {
            response.setMachine(repo.removeCapabilityFromMachine(capabilityIRI, machineIRI));
            response.setCode(200);
            response.setSuccess(true);
            response.setMessage(MESSAGE.SUCCESS);
        } else {
            response.setCode(500);
            response.setMessage(
                    String.format(MESSAGE.ENTITY_NOT_CONTAINED, machineId, capabilityId));
            response.setSuccess(false);
        }
        return response;
    }

    public HumanResourceResponse addProcessToHumanResource(
            String processId, String humanResourceId) {
        LoggingHelper.logMutation("addProcessToHumanResource", repo.getGraphNameForMutation());
        HumanResourceResponse response = new HumanResourceResponse();
        IRI processIRI = null;
        IRI humanResourceIRI = null;
        try {
            processIRI = Values.iri(processId);
            humanResourceIRI = Values.iri(humanResourceId);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            return response;
        }
        Process process = repo.getProcessById(processIRI);
        HumanResource humanResource = repo.getHumanResourceById(humanResourceIRI);
        if (process == null || humanResource == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND,
                            process == null ? processId : humanResourceId));
            return response;
        }
        response.setHumanResource(repo.addUsedProcessToHumanResource(processIRI, humanResourceIRI));
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        response.setCode(200);
        return response;
    }

    public HumanResourceResponse removeProcessFromHumanResource(
            String processId, String humanResourceId) {
        LoggingHelper.logMutation("removeProcessFromHumanResource", repo.getGraphNameForMutation());
        HumanResourceResponse response = new HumanResourceResponse();
        IRI processIRI = null;
        IRI humanResourceIRI = null;
        try {
            processIRI = Values.iri(processId);
            humanResourceIRI = Values.iri(humanResourceId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Process process = repo.getProcessById(processIRI);
        HumanResource humanResource = repo.getHumanResourceById(humanResourceIRI);
        if (process == null || humanResource == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND,
                            process == null ? processId : humanResourceId));
            return response;
        }

        if (humanResource.getUsingProcesses().remove(process)) {
            response.setHumanResource(
                    repo.removeProcessFromHumanResource(processIRI, humanResourceIRI));
            response.setCode(200);
            response.setSuccess(true);
            response.setMessage(MESSAGE.SUCCESS);
        } else {
            response.setCode(500);
            response.setMessage(
                    String.format(MESSAGE.ENTITY_NOT_CONTAINED, humanResourceId, processId));
            response.setSuccess(false);
        }
        return response;
    }

    public HumanResourceResponse addCapabilityToHumanResource(String capabilityId, String hrId) {
        LoggingHelper.logMutation("addCapabilityToHumanResource", repo.getGraphNameForMutation());
        HumanResourceResponse response = new HumanResourceResponse();
        IRI capabilityIRI = null;
        IRI hrIRI = null;
        try {
            capabilityIRI = Values.iri(capabilityId);
            hrIRI = Values.iri(hrId);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            return response;
        }
        Capability capability = repo.getCapabilityById(capabilityIRI);
        HumanResource humanResource = repo.getHumanResourceById(hrIRI);
        if (capability == null || humanResource == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, capability == null ? capabilityId : hrId));
            return response;
        }
        response.setHumanResource(repo.addCapabilityToHumanResource(capabilityIRI, hrIRI));
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        response.setCode(200);
        return response;
    }

    public HumanResourceResponse removeCapabilityFromHumanResource(
            String capabilityId, String hrId) {
        LoggingHelper.logMutation(
                "removeCapabilityFromHumanResource", repo.getGraphNameForMutation());
        HumanResourceResponse response = new HumanResourceResponse();
        IRI capabilityIRI = null;
        IRI hrIRI = null;
        try {
            capabilityIRI = Values.iri(capabilityId);
            hrIRI = Values.iri(hrId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Capability capability = repo.getCapabilityById(capabilityIRI);
        HumanResource humanResource = repo.getHumanResourceById(hrIRI);
        if (capability == null || humanResource == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, capability == null ? capabilityId : hrId));
            return response;
        }

        if (humanResource.getProvidedCapabilities().remove(capability)) {
            response.setHumanResource(repo.removeCapabilityFromHumanResource(capabilityIRI, hrIRI));
            response.setCode(200);
            response.setSuccess(true);
            response.setMessage(MESSAGE.SUCCESS);
        } else {
            response.setCode(500);
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_CONTAINED, hrId, capabilityId));
            response.setSuccess(false);
        }
        return response;
    }
}
