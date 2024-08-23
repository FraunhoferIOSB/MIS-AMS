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

import de.fraunhofer.iosb.ilt.ams.dao.SupplyChainDAO;
import de.fraunhofer.iosb.ilt.ams.model.*;
import de.fraunhofer.iosb.ilt.ams.model.filter.SupplyChainFilter;
import de.fraunhofer.iosb.ilt.ams.model.input.SupplyChainElementInput;
import de.fraunhofer.iosb.ilt.ams.model.input.SupplyChainInput;
import de.fraunhofer.iosb.ilt.ams.model.response.SupplyChainElementResponse;
import de.fraunhofer.iosb.ilt.ams.model.response.SupplyChainResponse;
import de.fraunhofer.iosb.ilt.ams.utility.LoggingHelper;
import de.fraunhofer.iosb.ilt.ams.utility.MESSAGE;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SupplyChainRepository {

    @Autowired SupplyChainDAO supplyChainDAO;

    @Autowired ObjectRdf4jRepository repo;

    public List<SupplyChain> getSupplyChains(SupplyChainFilter supplyChainFilter) {
        repo.emptyProcessedIds();
        LoggingHelper.logQuery("getSupplyChains", repo.getGraphNameForQuery().getQueryString());
        List<SupplyChain> supplyChainList =
                supplyChainDAO.list().stream().distinct().collect(Collectors.toList());
        if (supplyChainFilter != null) {
            if (supplyChainFilter.getId() != null) {
                supplyChainList =
                        supplyChainList.stream()
                                .filter(
                                        supplyChain ->
                                                Values.iri(supplyChainFilter.getId())
                                                        .equals(supplyChain.getId()))
                                .collect(Collectors.toList());
            }
            if (supplyChainFilter.getSourceId() != null) {
                supplyChainList =
                        supplyChainList.stream()
                                .filter(
                                        supplyChain ->
                                                supplyChainFilter
                                                        .getSourceId()
                                                        .equals(supplyChain.getSourceId()))
                                .collect(Collectors.toList());
            }
        }
        return supplyChainList;
    }

    public SupplyChainResponse createSupplyChain(SupplyChainInput input) {
        LoggingHelper.logMutation("createSupplyChain", repo.getGraphNameForMutation());
        SupplyChainResponse supplyChainResponse = new SupplyChainResponse();
        try {
            checkSupplyChainInputForErrors(input);
        } catch (IllegalArgumentException illegalArgumentException) {
            supplyChainResponse.setCode(500);
            supplyChainResponse.setSuccess(false);
            supplyChainResponse.setMessage(illegalArgumentException.getMessage());
            return supplyChainResponse;
        }
        supplyChainResponse.setSupplyChain(repo.createSupplyChain(input));
        supplyChainResponse.setSuccess(true);
        supplyChainResponse.setCode(200);
        supplyChainResponse.setMessage("Success");
        return supplyChainResponse;
    }

    public static SupplyChain createAndReturnSupplyChain(SupplyChainInput input)
            throws IllegalArgumentException {
        checkSupplyChainInputForErrors(input);
        SupplyChain supplyChain = new SupplyChain();
        supplyChain.setSourceId(input.getSourceId());
        supplyChain.setDescription(input.getDescription());
        supplyChain.setDescriptionLanguageCode(input.getDescriptionLanguageCode());
        return supplyChain;
    }

    public static void checkSupplyChainInputForErrors(SupplyChainInput input)
            throws IllegalArgumentException {
        if (input.getDescription() != null && input.getDescriptionLanguageCode() == null) {
            throw new IllegalArgumentException(
                    String.format(MESSAGE.LANGUAGE_ERROR, input.getDescription()));
        }
    }

    public SupplyChainResponse updateSupplyChain(String id, SupplyChainInput input) {
        LoggingHelper.logMutation("updateSupplyChain", repo.getGraphNameForMutation());
        SupplyChainResponse supplyChainResponse = new SupplyChainResponse();
        IRI iri = null;
        try {
            iri = Values.iri(id);
        } catch (IllegalArgumentException iae) {
            supplyChainResponse.setMessage(iae.getMessage());
            supplyChainResponse.setSuccess(false);
            supplyChainResponse.setCode(500);
            return supplyChainResponse;
        }
        SupplyChain supplyChain = repo.getSupplyChainById(iri);
        if (supplyChain == null) {
            supplyChainResponse.setCode(500);
            supplyChainResponse.setSuccess(false);
            supplyChainResponse.setMessage(
                    String.format(MESSAGE.ENTITY_NOT_FOUND, iri.stringValue()));
            return supplyChainResponse;
        }
        supplyChainResponse.setSupplyChain(repo.updateSupplyChain(iri, input));
        supplyChainResponse.setMessage(MESSAGE.SUCCESS);
        supplyChainResponse.setSuccess(true);
        supplyChainResponse.setCode(200);
        return supplyChainResponse;
    }

    public SupplyChainResponse deleteSupplyChain(String id) {
        LoggingHelper.logMutation("deleteSupplyChain", repo.getGraphNameForMutation());
        SupplyChainResponse response = new SupplyChainResponse();
        IRI iri = null;
        try {
            iri = Values.iri(id);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(iae.getMessage());
            return response;
        }
        response.setCode(200);
        response.setSuccess(repo.deleteSupplyChain(iri));
        response.setMessage(String.format(MESSAGE.DELETED, iri));
        return response;
    }

    public SupplyChainResponse addSupplyChainElementToSupplyChain(String sceId, String scId) {
        LoggingHelper.logMutation(
                "addSupplyChainElementToSupplyChain", repo.getGraphNameForMutation());
        SupplyChainResponse response = new SupplyChainResponse();
        IRI sceIRI = null;
        IRI scIRI = null;
        try {
            sceIRI = Values.iri(sceId);
            scIRI = Values.iri(scId);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            return response;
        }
        SupplyChainElement supplyChainElement = repo.getSupplyChainElementById(sceIRI);
        SupplyChain supplyChain = repo.getSupplyChainById(scIRI);
        if (supplyChainElement == null || supplyChain == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, supplyChainElement == null ? sceId : scId));
            return response;
        }
        response.setSupplyChain(repo.addSupplyChainElementToSupplyChain(sceIRI, scIRI));
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        response.setCode(200);
        return response;
    }

    public SupplyChainResponse removeSupplyChainElementFromSupplyChain(String sceId, String scId) {
        LoggingHelper.logMutation(
                "removeSupplyChainElementFromSupplyChain", repo.getGraphNameForMutation());
        SupplyChainResponse response = new SupplyChainResponse();
        IRI sceIRI = null;
        IRI scIRI = null;
        try {
            sceIRI = Values.iri(sceId);
            scIRI = Values.iri(scId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        SupplyChainElement supplyChainElement = repo.getSupplyChainElementById(sceIRI);
        SupplyChain supplyChain = repo.getSupplyChainById(scIRI);
        if (supplyChainElement == null || supplyChain == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, supplyChainElement == null ? sceId : scId));
            return response;
        }

        if (supplyChain.getSuppliers().remove(supplyChainElement)) {
            response.setSupplyChain(repo.removeSupplyChainElementFromSupplyChain(sceIRI, scIRI));
            response.setCode(200);
            response.setSuccess(true);
            response.setMessage(MESSAGE.SUCCESS);
        } else {
            response.setCode(500);
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_CONTAINED, scId, sceId));
            response.setSuccess(false);
        }
        return response;
    }

    public SupplyChainElementResponse createSupplyChainElementForSupplyChain(
            SupplyChainElementInput supplyChainElement, String supplyChainId) {
        LoggingHelper.logMutation(
                "createSupplyChainElementForSupplyChain", repo.getGraphNameForMutation());
        SupplyChainElementResponse response = new SupplyChainElementResponse();
        IRI supplyChainIRI = null;
        try {
            supplyChainIRI = Values.iri(supplyChainId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        SupplyChain supplyChain = repo.getSupplyChainById(supplyChainIRI);
        if (supplyChain == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, supplyChainId));
            return response;
        }
        try {
            SupplyChainElementRepository.checkSupplyChainInputForErrors(supplyChainElement);
        } catch (IllegalArgumentException iae) {
            response.setSuccess(false);
            response.setMessage(iae.getMessage());
            response.setCode(500);
            return response;
        }
        response.setSupplyChainElement(
                repo.createSupplyChainElementForSupplyChain(supplyChainElement, supplyChainIRI));
        response.setCode(200);
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        return response;
    }
}
