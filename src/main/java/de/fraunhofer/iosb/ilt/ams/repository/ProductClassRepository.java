package de.fraunhofer.iosb.ilt.ams.repository;

import static de.fraunhofer.iosb.ilt.ams.repository.SemanticReferenceRepository.createAndReturnSemanticReference;

import de.fraunhofer.iosb.ilt.ams.dao.ProductClassDAO;
import de.fraunhofer.iosb.ilt.ams.model.ObjectRdf4jRepository;
import de.fraunhofer.iosb.ilt.ams.model.ProductClass;
import de.fraunhofer.iosb.ilt.ams.model.SemanticReference;
import de.fraunhofer.iosb.ilt.ams.model.filter.ProductClassFilter;
import de.fraunhofer.iosb.ilt.ams.model.input.ProductClassInput;
import de.fraunhofer.iosb.ilt.ams.model.input.SemanticReferenceInput;
import de.fraunhofer.iosb.ilt.ams.model.response.ProductClassResponse;
import de.fraunhofer.iosb.ilt.ams.model.response.SemanticReferenceResponse;
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
public class ProductClassRepository {

    @Autowired ProductClassDAO productClassDAO;

    @Autowired ObjectRdf4jRepository repo;

    public List<ProductClass> getProductClasses(ProductClassFilter filter) {
        LoggingHelper.logQuery("getProductClasses", repo.getGraphNameForQuery().getQueryString());
        repo.emptyProcessedIds();
        List<ProductClass> productClasses =
                productClassDAO.list().stream().distinct().collect(Collectors.toList());
        if (filter != null) {
            if (filter.getId() != null) {
                productClasses =
                        productClasses.stream()
                                .filter(
                                        productClass ->
                                                productClass
                                                        .getId()
                                                        .equals(Values.iri(filter.getId())))
                                .collect(Collectors.toList());
            }
            if (filter.getSourceId() != null) {
                productClasses =
                        productClasses.stream()
                                .filter(
                                        productClass ->
                                                filter.getSourceId()
                                                        .equals(productClass.getSourceId()))
                                .collect(Collectors.toList());
            }
            if (filter.getSemanticReferenceId() != null) {
                productClasses =
                        productClasses.stream()
                                .filter(
                                        pc ->
                                                pc.getSemanticReferences().stream()
                                                        .anyMatch(
                                                                semanticReference ->
                                                                        Values.iri(
                                                                                        filter
                                                                                                .getSemanticReferenceId())
                                                                                .equals(
                                                                                        semanticReference
                                                                                                .getId())))
                                .collect(Collectors.toList());
            }
        }
        return productClasses;
    }

    public ProductClassResponse createProductClass(
            ProductClassInput productClassInput,
            List<String> parentProductClassIds,
            List<String> childProductClassIds) {
        LoggingHelper.logMutation("createProductClass", repo.getGraphNameForMutation());
        ProductClassResponse response = new ProductClassResponse();
        ProductClass productClass = null;
        try {
            productClass = createAndReturnProductClass(productClassInput);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getLocalizedMessage());
            response.setSuccess(false);
            return response;
        }
        List<IRI> parentIRIs = new LinkedList<>();
        List<IRI> childIRIs = new LinkedList<>();
        if (parentProductClassIds != null) {
            for (var parent : parentProductClassIds) {
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

        if (childProductClassIds != null) {
            for (var child : childProductClassIds) {
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
        if (productClass.getSemanticReferences() != null) {
            for (var semRef : productClass.getSemanticReferences()) {
                if (semRef.getId() == null) {
                    semRef.setId(repo.getRdf4JTemplate().getNewUUID());
                }
            }
        }
        response.setProductClass(repo.createProductClass(productClassInput, parentIRIs, childIRIs));
        response.setMessage(MESSAGE.SUCCESS);
        response.setCode(200);
        response.setSuccess(true);
        return response;
    }

    public ProductClassResponse updateProductClass(
            String id,
            ProductClassInput productClassInput,
            List<String> parentProductClassIds,
            List<String> childProductClassIds) {
        LoggingHelper.logMutation("updateProductClass", repo.getGraphNameForMutation());
        ProductClassResponse response = new ProductClassResponse();
        IRI iri = null;
        try {
            iri = Values.iri(id);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            response.setCode(500);
            return response;
        }
        ProductClass productClass = repo.getProductClassById(iri);
        if (productClass == null) {
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, id));
            response.setSuccess(false);
            response.setCode(500);
            return response;
        }

        List<IRI> parentIRIs = new LinkedList<>();
        List<IRI> childIRIs = new LinkedList<>();
        if (parentProductClassIds != null) {
            for (var parent : parentProductClassIds) {
                try {
                    parentIRIs.add(Values.iri(parent));
                    if (repo.getProductClassById(Values.iri(parent)) == null) {
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

        if (childProductClassIds != null) {
            for (var child : childProductClassIds) {
                try {
                    childIRIs.add(Values.iri(child));
                    if (repo.getProductClassById(Values.iri(child)) == null) {
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
        response.setProductClass(
                repo.updateProductClass(iri, productClassInput, parentIRIs, childIRIs));
        response.setMessage(MESSAGE.SUCCESS);
        response.setCode(200);
        response.setSuccess(true);
        return response;
    }

    public ProductClassResponse deleteProductClass(String productClassId, boolean deleteChildren) {
        LoggingHelper.logMutation("deleteProductClass", repo.getGraphNameForMutation());
        ProductClassResponse response = new ProductClassResponse();
        IRI iri = null;
        try {
            iri = Values.iri(productClassId);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            return response;
        }
        if (deleteChildren) {
            for (var child : repo.getProductClassById(iri).getChildClasses()) {
                repo.deleteProductClass(child.getId());
            }
        }

        response.setCode(200);
        response.setSuccess(repo.deleteProductClass(iri));
        response.setMessage(String.format(MESSAGE.DELETED, iri));
        return response;
    }

    public ProductClassResponse addSemanticReferenceToProductClass(
            String semRefId, String productClassId) {
        LoggingHelper.logMutation("addSemRefToProductClass", repo.getGraphNameForMutation());
        ProductClassResponse response = new ProductClassResponse();
        IRI semRefIRI = null;
        IRI productClassIRI = null;
        try {
            semRefIRI = Values.iri(semRefId);
            productClassIRI = Values.iri(productClassId);
        } catch (IllegalArgumentException iae) {
            response.setCode(500);
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            return response;
        }
        SemanticReference semRef = repo.getSemanticReferenceById(semRefIRI);
        ProductClass productClass = repo.getProductClassById(productClassIRI);
        if (semRef == null || productClass == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND, semRef == null ? semRefId : productClassId));
            return response;
        }
        response.setProductClass(
                repo.addSemanticReferenceToProductClass(semRefIRI, productClassIRI));
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        response.setCode(200);
        return response;
    }

    public SemanticReferenceResponse createSemanticReferenceForProductClass(
            SemanticReferenceInput semanticReferenceInput, String productClassId) {
        LoggingHelper.logMutation("createSemRefForProductClass", repo.getGraphNameForMutation());
        SemanticReferenceResponse response = new SemanticReferenceResponse();
        IRI productClassIRI = null;
        try {
            productClassIRI = Values.iri(productClassId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        ProductClass productClass = repo.getProductClassById(productClassIRI);
        if (productClass == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, productClassId));
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
        response.setSemanticReference(
                repo.createSemanticReferenceForProductClass(
                        semanticReferenceInput, productClassIRI));
        response.setCode(200);
        response.setMessage(MESSAGE.SUCCESS);
        response.setSuccess(true);
        return response;
    }

    public ProductClassResponse removeSemanticReferenceFromProductClass(
            String semRefId, String productClassId) {
        LoggingHelper.logMutation("removeSemRefFromProductClass", repo.getGraphNameForMutation());
        ProductClassResponse response = new ProductClassResponse();
        IRI semanticReferenceIRI = null;
        IRI productClassIRI = null;
        try {
            semanticReferenceIRI = Values.iri(semRefId);
            productClassIRI = Values.iri(productClassId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setCode(500);
            response.setSuccess(false);
            return response;
        }
        SemanticReference semanticReference = repo.getSemanticReferenceById(semanticReferenceIRI);
        ProductClass productClass = repo.getProductClassById(productClassIRI);
        if (semanticReference == null || productClass == null) {
            response.setCode(500);
            response.setSuccess(false);
            response.setMessage(
                    String.format(
                            MESSAGE.ENTITY_NOT_FOUND,
                            semanticReference == null ? semRefId : productClassId));
            return response;
        }

        if (productClass.getSemanticReferences().remove(semanticReference)) {
            response.setProductClass(
                    repo.removeSemanticReferenceFromProductClass(
                            semanticReferenceIRI, productClassIRI));
            response.setCode(200);
            response.setSuccess(true);
            response.setMessage(MESSAGE.SUCCESS);
        } else {
            response.setCode(500);
            response.setMessage(
                    String.format(MESSAGE.ENTITY_NOT_CONTAINED, productClassId, semRefId));
            response.setSuccess(false);
        }
        return response;
    }

    public static ProductClass createAndReturnProductClass(ProductClassInput input)
            throws IllegalArgumentException {
        checkProductClassInputForErrors(input);
        ProductClass productClass = new ProductClass();
        productClass.setSourceId(input.getSourceId());
        productClass.setLabel(input.getLabel());
        productClass.setLabelLanguageCode(input.getLabelLanguageCode());
        productClass.setDescription(input.getDescription());
        productClass.setDescriptionLanguageCode(input.getDescriptionLanguageCode());
        if (input.getSemanticReferences() != null) {
            for (var semRef : input.getSemanticReferences()) {
                productClass.addSemanticReference(createAndReturnSemanticReference(semRef));
            }
        }
        return productClass;
    }

    public static void checkProductClassInputForErrors(ProductClassInput input)
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
}
