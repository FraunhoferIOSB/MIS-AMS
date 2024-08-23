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

import de.fraunhofer.iosb.ilt.ams.dao.SemanticReferenceDAO;
import de.fraunhofer.iosb.ilt.ams.model.ObjectRdf4jRepository;
import de.fraunhofer.iosb.ilt.ams.model.SemanticReference;
import de.fraunhofer.iosb.ilt.ams.model.filter.SemanticReferenceFilter;
import de.fraunhofer.iosb.ilt.ams.model.input.SemanticReferenceInput;
import de.fraunhofer.iosb.ilt.ams.model.response.SemanticReferenceResponse;
import de.fraunhofer.iosb.ilt.ams.utility.LoggingHelper;
import de.fraunhofer.iosb.ilt.ams.utility.MESSAGE;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SemanticReferenceRepository {

    @Autowired SemanticReferenceDAO semanticReferenceDAO;

    @Autowired ObjectRdf4jRepository repo;

    public List<SemanticReference> getSemanticReferences(
            SemanticReferenceFilter semanticReferenceFilter) {
        LoggingHelper.logQuery(
                "getSemanticReferences", repo.getGraphNameForQuery().getQueryString());
        repo.emptyProcessedIds();
        List<SemanticReference> semanticReferences =
                semanticReferenceDAO.list().stream().distinct().collect(Collectors.toList());
        if (semanticReferenceFilter != null) {
            if (semanticReferenceFilter.getId() != null) {
                semanticReferences =
                        semanticReferences.stream()
                                .filter(
                                        sr ->
                                                Values.iri(semanticReferenceFilter.getId())
                                                        .equals(sr.getId()))
                                .collect(Collectors.toList());
            }
            if (semanticReferenceFilter.getSourceUri() != null) {
                semanticReferences =
                        semanticReferences.stream()
                                .filter(
                                        sr ->
                                                URI.create(semanticReferenceFilter.getSourceUri())
                                                        .equals(sr.getSourceUri()))
                                .collect(Collectors.toList());
            }
        }
        return semanticReferences;
    }

    public static SemanticReference createAndReturnSemanticReference(SemanticReferenceInput input)
            throws IllegalArgumentException {
        checkSemanticReferenceInputForErrors(input);
        SemanticReference semanticReference = new SemanticReference();
        if (input.getSourceUri() != null) {
            semanticReference.setSourceUri(URI.create(input.getSourceUri()));
        }
        semanticReference.setLabel(input.getLabel());
        semanticReference.setLabelLanguageCode(input.getLabelLanguageCode());
        semanticReference.setDescription(input.getDescription());
        semanticReference.setDescriptionLanguageCode(input.getDescriptionLanguageCode());
        return semanticReference;
    }

    public static void checkSemanticReferenceInputForErrors(SemanticReferenceInput input) {
        if (input.getLabel() != null
                && !input.getLabel().isBlank()
                && (input.getLabelLanguageCode() == null
                        || input.getLabelLanguageCode().isBlank())) {
            throw new IllegalArgumentException(
                    String.format(MESSAGE.LANGUAGE_ERROR, input.getLabel()));
        }
        if (input.getDescription() != null
                && !input.getDescription().isBlank()
                && (input.getDescriptionLanguageCode() == null
                        || input.getDescriptionLanguageCode().isBlank())) {
            throw new IllegalArgumentException(
                    String.format(MESSAGE.LANGUAGE_ERROR, input.getDescription()));
        }
    }

    public SemanticReferenceResponse createSemanticReference(
            SemanticReferenceInput semanticReferenceInput) {
        LoggingHelper.logMutation("createSemanticReference", repo.getGraphNameForMutation());
        SemanticReferenceResponse response = new SemanticReferenceResponse();
        try {
            checkSemanticReferenceInputForErrors(semanticReferenceInput);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(iae.getMessage());
            return response;
        }
        response.setSemanticReference(repo.createSemanticReference(semanticReferenceInput));
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        response.setCode(200);
        return response;
    }

    public SemanticReferenceResponse updateSemanticReference(
            String id, SemanticReferenceInput semanticReferenceInput) {
        LoggingHelper.logMutation("updateSemanticReference", repo.getGraphNameForMutation());
        SemanticReferenceResponse semanticReferenceResponse = new SemanticReferenceResponse();
        IRI iri = null;
        try {
            iri = Values.iri(id);
        } catch (IllegalArgumentException iae) {
            semanticReferenceResponse.setCode(500);
            semanticReferenceResponse.setSuccess(false);
            semanticReferenceResponse.setMessage(iae.getMessage());
            return semanticReferenceResponse;
        }
        SemanticReference semanticReference = repo.getSemanticReferenceById(iri);
        if (semanticReference == null) {
            semanticReferenceResponse.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, id));
            semanticReferenceResponse.setSuccess(false);
            semanticReferenceResponse.setCode(500);
            return semanticReferenceResponse;
        }
        semanticReferenceResponse.setSemanticReference(
                repo.updateSemanticReference(iri, semanticReferenceInput));
        semanticReferenceResponse.setSuccess(true);
        semanticReferenceResponse.setCode(200);
        semanticReferenceResponse.setMessage(MESSAGE.SUCCESS);
        return semanticReferenceResponse;
    }

    public SemanticReferenceResponse deleteSemanticReference(String id) {
        LoggingHelper.logMutation("deleteSemanticReference", repo.getGraphNameForMutation());
        SemanticReferenceResponse response = new SemanticReferenceResponse();
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
        response.setSuccess(repo.deleteSemanticReference(iri));
        response.setMessage(String.format(MESSAGE.DELETED, iri));
        return response;
    }
}
