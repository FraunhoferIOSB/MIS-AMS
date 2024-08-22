package de.fraunhofer.iosb.ilt.ams.repository;

import static de.fraunhofer.iosb.ilt.ams.repository.EnterpriseRepository.checkEnterpriseInputForErrors;
import static de.fraunhofer.iosb.ilt.ams.repository.EnterpriseRepository.createAndReturnEnterprise;
import static de.fraunhofer.iosb.ilt.ams.repository.FactoryRepository.checkFactoryInputForErrors;
import static de.fraunhofer.iosb.ilt.ams.repository.FactoryRepository.createAndReturnFactory;
import static de.fraunhofer.iosb.ilt.ams.repository.ProductApplicationRepository.checkProductApplicationForErrors;
import static de.fraunhofer.iosb.ilt.ams.repository.ProductApplicationRepository.createAndReturnProductApplication;

import de.fraunhofer.iosb.ilt.ams.dao.SupplyChainElementDAO;
import de.fraunhofer.iosb.ilt.ams.model.ObjectRdf4jRepository;
import de.fraunhofer.iosb.ilt.ams.model.SupplyChainElement;
import de.fraunhofer.iosb.ilt.ams.model.filter.SupplyChainElementFilter;
import de.fraunhofer.iosb.ilt.ams.model.input.SupplyChainElementInput;
import de.fraunhofer.iosb.ilt.ams.model.response.SupplyChainElementResponse;
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
public class SupplyChainElementRepository {

    @Autowired SupplyChainElementDAO supplyChainElementDAO;

    @Autowired ObjectRdf4jRepository repo;

    public List<SupplyChainElement> getSupplyChainElements(
            SupplyChainElementFilter supplyChainElementFilter) {
        LoggingHelper.logQuery(
                "getSupplyChainElements", repo.getGraphNameForQuery().getQueryString());
        repo.emptyProcessedIds();
        List<SupplyChainElement> supplyChainElementList =
                supplyChainElementDAO.list().stream().distinct().collect(Collectors.toList());
        if (supplyChainElementFilter != null) {
            if (supplyChainElementFilter.getId() != null) {
                supplyChainElementList =
                        supplyChainElementList.stream()
                                .filter(
                                        supplyChainElement ->
                                                Values.iri(supplyChainElementFilter.getId())
                                                        .equals(supplyChainElement.getId()))
                                .collect(Collectors.toList());
            }
            if (supplyChainElementFilter.getSourceId() != null) {
                supplyChainElementList =
                        supplyChainElementList.stream()
                                .filter(
                                        sce ->
                                                supplyChainElementFilter
                                                        .getSourceId()
                                                        .equals(sce.getSourceId()))
                                .collect(Collectors.toList());
            }
        }
        return supplyChainElementList;
    }

    public SupplyChainElementResponse createSupplyChainElement(
            SupplyChainElementInput input, List<String> parentIds, List<String> childIds) {
        LoggingHelper.logMutation("createSupplyChainElement", repo.getGraphNameForMutation());
        SupplyChainElementResponse response = new SupplyChainElementResponse();
        try {
            checkSupplyChainInputForErrors(input);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getLocalizedMessage());
            response.setSuccess(false);
            return response;
        }
        List<IRI> parentIRIs = new LinkedList<>();
        List<IRI> childIRIs = new LinkedList<>();
        if (parentIds != null) {
            for (var parent : parentIds) {
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

        if (childIds != null) {
            for (var child : childIds) {
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

        response.setSupplyChainElement(repo.createSupplyChainElement(input, parentIRIs, childIRIs));
        response.setSuccess(true);
        response.setCode(200);
        response.setMessage("Success");
        return response;
    }

    public static SupplyChainElement createAndReturnSupplyChainElement(
            SupplyChainElementInput input) {
        SupplyChainElement supplyChainElement = new SupplyChainElement();
        supplyChainElement.setSourceId(input.getSourceId());

        supplyChainElement.setDescription(input.getDescription());
        supplyChainElement.setDescriptionLanguageCode(input.getDescriptionLanguageCode());
        supplyChainElement.setFactory(createAndReturnFactory(input.getFactory()));
        supplyChainElement.setEnterprise(createAndReturnEnterprise(input.getEnterprise()));
        for (var productApplication : input.getProducts()) {
            supplyChainElement.addProduct(createAndReturnProductApplication(productApplication));
        }
        return supplyChainElement;
    }

    public static void checkSupplyChainInputForErrors(SupplyChainElementInput input)
            throws IllegalArgumentException {
        if (input.getDescription() != null && input.getDescriptionLanguageCode() == null) {
            throw new IllegalArgumentException(
                    String.format(MESSAGE.LANGUAGE_ERROR, input.getDescription()));
        }

        if (input.getProducts() != null) {
            for (var product : input.getProducts()) {
                checkProductApplicationForErrors(product);
            }
        }

        if (input.getFactory() != null) {
            checkFactoryInputForErrors(input.getFactory());
        }

        if (input.getEnterprise() != null) {
            checkEnterpriseInputForErrors(input.getEnterprise());
        }
    }

    public SupplyChainElementResponse updateSupplyChainElement(
            String id,
            SupplyChainElementInput supplyChainElementInput,
            List<String> parentIds,
            List<String> childIds) {
        LoggingHelper.logMutation("updateSupplyChainElement", repo.getGraphNameForMutation());
        SupplyChainElementResponse response = new SupplyChainElementResponse();
        IRI iri = null;
        try {
            iri = Values.iri(id);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            response.setCode(500);
            return response;
        }
        SupplyChainElement supplyChainElement = repo.getSupplyChainElementById(iri);
        if (supplyChainElement == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, iri));
            return response;
        }

        try {
            checkSupplyChainInputForErrors(supplyChainElementInput);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getLocalizedMessage());
            response.setSuccess(false);
            return response;
        }

        List<IRI> parentIRIs = new LinkedList<>();
        List<IRI> childIRIs = new LinkedList<>();
        if (parentIds != null) {
            for (var parent : parentIds) {
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

        if (childIds != null) {
            for (var child : childIds) {
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

        response.setSupplyChainElement(
                repo.updateSupplyChainElement(iri, supplyChainElementInput, parentIRIs, childIRIs));
        response.setSuccess(true);
        response.setMessage(MESSAGE.SUCCESS);
        response.setCode(200);
        return response;
    }

    public SupplyChainElementResponse deleteSupplyChainElement(String id) {
        LoggingHelper.logMutation("deleteSupplyChainElement", repo.getGraphNameForMutation());
        SupplyChainElementResponse response = new SupplyChainElementResponse();
        IRI iri = null;
        try {
            iri = Values.iri(id);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            response.setCode(500);
            return response;
        }
        response.setCode(200);
        response.setSuccess(repo.deleteSupplyChainElement(iri));
        response.setMessage(String.format(MESSAGE.DELETED, iri));
        return response;
    }
}
