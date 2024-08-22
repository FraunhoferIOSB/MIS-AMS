package de.fraunhofer.iosb.ilt.ams.repository;

import static de.fraunhofer.iosb.ilt.ams.repository.SemanticReferenceRepository.checkSemanticReferenceInputForErrors;
import static de.fraunhofer.iosb.ilt.ams.repository.SemanticReferenceRepository.createAndReturnSemanticReference;

import de.fraunhofer.iosb.ilt.ams.dao.PropertyDAO;
import de.fraunhofer.iosb.ilt.ams.model.ObjectRdf4jRepository;
import de.fraunhofer.iosb.ilt.ams.model.Property;
import de.fraunhofer.iosb.ilt.ams.model.SemanticReference;
import de.fraunhofer.iosb.ilt.ams.model.filter.PropertyFilter;
import de.fraunhofer.iosb.ilt.ams.model.input.PropertyInput;
import de.fraunhofer.iosb.ilt.ams.model.input.SemanticReferenceInput;
import de.fraunhofer.iosb.ilt.ams.model.response.PropertyResponse;
import de.fraunhofer.iosb.ilt.ams.model.response.SemanticReferenceResponse;
import de.fraunhofer.iosb.ilt.ams.utility.LoggingHelper;
import de.fraunhofer.iosb.ilt.ams.utility.MESSAGE;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class PropertyRepository {

    @Autowired PropertyDAO propertyDAO;
    @Autowired ObjectRdf4jRepository repo;

    public List<Property> getProperties(PropertyFilter propertyFilter) {
        LoggingHelper.logQuery("createProduct", repo.getGraphNameForQuery().getQueryString());
        repo.emptyProcessedIds();
        List<Property> propertyList =
                propertyDAO.list().stream().distinct().collect(Collectors.toList());
        if (propertyFilter != null && propertyFilter.getId() != null) {
            propertyList =
                    propertyList.stream()
                            .filter(
                                    (property ->
                                            Values.iri(propertyFilter.getId())
                                                    .equals(property.getId())))
                            .distinct()
                            .collect(Collectors.toList());
        }
        if (propertyFilter != null && propertyFilter.getSourceId() != null) {
            propertyList =
                    propertyList.stream()
                            .filter(
                                    property ->
                                            propertyFilter
                                                    .getSourceId()
                                                    .equals(property.getSourceId()))
                            .distinct()
                            .collect(Collectors.toList());
        }
        if (propertyFilter != null && propertyFilter.getSemanticReferenceId() != null) {
            propertyList =
                    propertyList.stream()
                            .filter(
                                    property ->
                                            property.getSemanticReferences().stream()
                                                    .anyMatch(
                                                            semanticReference ->
                                                                    Values.iri(
                                                                                    propertyFilter
                                                                                            .getSemanticReferenceId())
                                                                            .equals(
                                                                                    semanticReference
                                                                                            .getId())))
                            .collect(Collectors.toList());
        }
        return propertyList;
    }

    public static Property createAndReturnProperty(PropertyInput input, boolean isCreate)
            throws IllegalArgumentException {

        checkPropertyInputForErrors(input);
        Property property = new Property();
        if (isCreate) {
            property.setSourceId(input.getSourceId());
            property.setLabel(input.getLabel());
            property.setLabelLanguageCode(input.getLabelLanguageCode());
            property.setDescription(input.getDescription());
            property.setDescriptionLanguageCode(input.getDescriptionLanguageCode());
            if (input.getSemanticReferences() != null) {
                for (var semRef : input.getSemanticReferences()) {
                    property.addSemanticReference(createAndReturnSemanticReference(semRef));
                }
            }
            property.setValue(input.getValue());
        }
        return property;
    }

    public static void checkPropertyInputForErrors(PropertyInput input)
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

    public PropertyResponse createProperty(PropertyInput propertyInput) {
        LoggingHelper.logMutation("createProperty", repo.getGraphNameForMutation());
        PropertyResponse response = new PropertyResponse();
        try {
            checkPropertyInputForErrors(propertyInput);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            return response;
        }
        response.setProperty(repo.createProperty(propertyInput));
        response.setSuccess(true);
        response.setCode(200);
        response.setMessage(MESSAGE.SUCCESS);
        return response;
    }

    public PropertyResponse updateProperty(String id, PropertyInput propertyInput) {
        LoggingHelper.logMutation("updateProperty", repo.getGraphNameForMutation());
        PropertyResponse response = new PropertyResponse();
        IRI iri = null;
        try {
            iri = Values.iri(id);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(iae.getMessage());
            return response;
        }
        Property property = repo.getPropertyById(iri);
        if (property == null) {
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, id));
            response.setSuccess(false);
            response.setCode(500);
            return response;
        }
        response.setProperty(repo.updateProperty(iri, propertyInput));
        response.setCode(200);
        response.setSuccess(true);
        response.setMessage(MESSAGE.SUCCESS);
        return response;
    }

    public PropertyResponse deleteProperty(String id) {
        LoggingHelper.logMutation("deleteProperty", repo.getGraphNameForMutation());
        PropertyResponse response = new PropertyResponse();
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
        response.setSuccess(repo.deleteProperty(iri));
        response.setMessage(String.format(MESSAGE.DELETED, iri));
        return response;
    }

    public PropertyResponse addSemanticReferenceToProperty(
            String semanticReferenceId, String propertyId) {
        LoggingHelper.logMutation("addSemanticReferenceToProperty", repo.getGraphNameForMutation());
        PropertyResponse response = new PropertyResponse();
        IRI propertyIRI = null;
        IRI semanticIRI = null;
        try {
            propertyIRI = Values.iri(propertyId);
            semanticIRI = Values.iri(semanticReferenceId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Property property = repo.getPropertyById(propertyIRI);
        SemanticReference semanticReference = repo.getSemanticReferenceById(semanticIRI);
        if (property == null || semanticReference == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND,
                            property == null ? propertyId : semanticReferenceId));
            return response;
        }
        response.setProperty(repo.addSemanticReferenceToProperty(semanticIRI, propertyIRI));
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        response.setCode(200);
        return response;
    }

    public SemanticReferenceResponse createSemanticReferenceForProperty(
            SemanticReferenceInput semanticReferenceInput, String propertyId) {
        LoggingHelper.logMutation(
                "createSemanticReferenceForProperty", repo.getGraphNameForMutation());
        SemanticReferenceResponse response = new SemanticReferenceResponse();
        IRI propertyIRI = null;
        try {
            propertyIRI = Values.iri(propertyId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Property property = repo.getPropertyById(propertyIRI);
        if (property == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, propertyId));
            return response;
        }
        try {
            checkSemanticReferenceInputForErrors(semanticReferenceInput);
        } catch (IllegalArgumentException iae) {
            response.setSuccess(false);
            response.setMessage(iae.getMessage());
            response.setCode(500);
            return response;
        }
        response.setSemanticReference(
                repo.createSemanticReferenceForProperty(semanticReferenceInput, propertyIRI));
        response.setCode(200);
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        return response;
    }

    public PropertyResponse removeSemanticReferenceFromProperty(
            String semanticReferenceId, String propertyId) {
        LoggingHelper.logMutation(
                "removeSemanticReferenceFromProperty", repo.getGraphNameForMutation());
        PropertyResponse response = new PropertyResponse();
        IRI propertyIRI = null;
        IRI semanticIRI = null;
        try {
            propertyIRI = Values.iri(propertyId);
            semanticIRI = Values.iri(semanticReferenceId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        Property property = repo.getPropertyById(propertyIRI);
        SemanticReference semanticReference = repo.getSemanticReferenceById(semanticIRI);
        if (property == null || semanticReference == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND,
                            property == null ? propertyId : semanticReferenceId));
            return response;
        }

        if (property.removeSemanticReference(semanticReference)) {
            response.setProperty(
                    repo.removeSemanticReferenceFromProperty(semanticIRI, propertyIRI));
            response.setCode(200);
            response.setSuccess(true);
            response.setMessage(MESSAGE.SUCCESS);
        } else {
            response.setCode(500);
            response.setMessage(
                    String.format(MESSAGE.ENTITY_NOT_CONTAINED, propertyId, semanticReferenceId));
            response.setSuccess(false);
        }
        return response;
    }
}
