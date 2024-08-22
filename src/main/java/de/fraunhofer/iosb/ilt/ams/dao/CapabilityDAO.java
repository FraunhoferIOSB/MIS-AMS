package de.fraunhofer.iosb.ilt.ams.dao;

import static de.fraunhofer.iosb.ilt.ams.dao.SemanticReferenceDAO.populateSemanticReferenceForUpdate;
import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri;

import de.fraunhofer.iosb.ilt.ams.AMS;
import de.fraunhofer.iosb.ilt.ams.model.*;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatternNotTriples;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.spring.dao.RDF4JCRUDDao;
import org.eclipse.rdf4j.spring.dao.support.bindingsBuilder.MutableBindings;
import org.eclipse.rdf4j.spring.dao.support.sparql.NamedSparqlSupplier;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.eclipse.rdf4j.spring.util.QueryResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CapabilityDAO extends RDF4JCRUDDao<Capability, Capability, IRI> {
    public static final GraphPatternNotTriples capabilitySourceIdPattern =
            GraphPatterns.optional(
                    Capability.CAPABILITY_ID.has(
                            iri(AMS.externalIdentifier), Capability.CAPABILITY_SOURCE_ID));
    public static final GraphPatternNotTriples capabilityDescriptionPattern =
            GraphPatterns.optional(
                    Capability.CAPABILITY_ID.has(
                            iri(RDFS.COMMENT), Capability.CAPABILITY_DESCRIPTION));
    public static final GraphPatternNotTriples capabilityLabelPattern =
            GraphPatterns.optional(
                    Capability.CAPABILITY_ID.has(iri(RDFS.LABEL), Capability.CAPABILITY_LABEL));

    public static final GraphPatternNotTriples capabilityPropertyPattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Capability.CAPABILITY_ID.has(
                                    iri(AMS.has), Capability.CAPABILITY_PROPERTY),
                            Capability.CAPABILITY_PROPERTY.isA(AMS.Property)));
    public static final GraphPatternNotTriples capabilityProcessPattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Capability.CAPABILITY_ID.has(
                                    iri(AMS.realizedBy), Capability.CAPABILITY_PROCESS),
                            Capability.CAPABILITY_PROCESS.isA(AMS.Process)));
    public static final GraphPatternNotTriples capabilityChildPattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Capability.CAPABILITY_ID.has(
                                    iri(AMS.generalizes), Capability.CAPABILITY_CHILD_CAPABILITY),
                            Capability.CAPABILITY_CHILD_CAPABILITY.isA(AMS.Capability)));
    public static final GraphPatternNotTriples capabilityParentPattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Capability.CAPABILITY_ID.has(
                                    iri(AMS.specializes), Capability.CAPABILITY_PARENT_CAPABILITY),
                            Capability.CAPABILITY_PARENT_CAPABILITY.isA(AMS.Capability)));
    public static final GraphPatternNotTriples capabilityProductionResourcePattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Capability.CAPABILITY_ID.has(
                                    iri(AMS.providedBy), Capability.CAPABILITY_PRODUCTION_RESOURCE),
                            GraphPatterns.union(
                                    Capability.CAPABILITY_PRODUCTION_RESOURCE.isA(AMS.Machine),
                                    Capability.CAPABILITY_PRODUCTION_RESOURCE.isA(
                                            AMS.HumanResource),
                                    Capability.CAPABILITY_PRODUCTION_RESOURCE.isA(
                                            AMS.ProductionResource))));
    public static final GraphPatternNotTriples capabilitySemanticReferencePattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Capability.CAPABILITY_ID.has(
                                    iri(AMS.hasSemantic), Capability.CAPABILITY_SEMANTIC_REFERENCE),
                            Capability.CAPABILITY_SEMANTIC_REFERENCE.isA(AMS.SemanticReference)));

    @Autowired ObjectRdf4jRepository repo;

    private List<Capability> capabilityList;

    public CapabilityDAO(RDF4JTemplate rdf4JTemplate) {
        super(rdf4JTemplate);
    }

    @Override
    protected void populateIdBindings(MutableBindings bindingsBuilder, IRI iri) {
        bindingsBuilder.add(Capability.CAPABILITY_ID, iri);
    }

    @Override
    protected NamedSparqlSupplierPreparer prepareNamedSparqlSuppliers(
            NamedSparqlSupplierPreparer preparer) {
        return null;
    }

    @Override
    protected String getReadQuery() {
        capabilityList = new LinkedList<>();
        return getCapabilitySelectQuery(null).from(repo.getGraphNameForQuery()).getQueryString();
    }

    @Override
    protected Capability mapSolution(BindingSet querySolution) {
        Capability capability = null;

        for (Capability c : capabilityList) {
            if (c.getId()
                    .equals(QueryResultUtils.getIRI(querySolution, Capability.CAPABILITY_ID))) {
                capability = c;
                break;
            }
        }
        if (capability == null) {
            capability = new Capability();
            mapCapability(querySolution, capability);
            capabilityList.add(capability);
        }
        var property = QueryResultUtils.getIRIMaybe(querySolution, Capability.CAPABILITY_PROPERTY);
        if (property != null) {
            capability.addProperty(repo.getPropertyById(property));
        }
        var process = QueryResultUtils.getIRIMaybe(querySolution, Capability.CAPABILITY_PROCESS);
        if (process != null) {
            capability.addProcess(repo.getProcessById(process));
        }
        var childCapability =
                QueryResultUtils.getIRIMaybe(querySolution, Capability.CAPABILITY_CHILD_CAPABILITY);
        if (childCapability != null) {
            capability.addChildCapability(repo.getCapabilityById(childCapability));
        }

        var parentCapability =
                QueryResultUtils.getIRIMaybe(
                        querySolution, Capability.CAPABILITY_PARENT_CAPABILITY);
        if (parentCapability != null) {
            capability.addParentCapability(repo.getCapabilityById(parentCapability));
        }
        var productionResource =
                QueryResultUtils.getIRIMaybe(
                        querySolution, Capability.CAPABILITY_PRODUCTION_RESOURCE);
        if (productionResource != null) {
            capability.addProductionResource(repo.getProductionResourceById(productionResource));
        }
        var semanticReference =
                QueryResultUtils.getIRIMaybe(
                        querySolution, Capability.CAPABILITY_SEMANTIC_REFERENCE);
        if (semanticReference != null) {
            capability.addSemanticReference(repo.getSemanticReferenceById(semanticReference));
        }

        return capability;
    }

    public static SelectQuery getCapabilitySelectQuery(String iri) {
        var selectQuery =
                Queries.SELECT(
                                Capability.CAPABILITY_ID,
                                Capability.CAPABILITY_SOURCE_ID,
                                Capability.CAPABILITY_LABEL,
                                Capability.CAPABILITY_DESCRIPTION,
                                Capability.CAPABILITY_PROPERTY,
                                Capability.CAPABILITY_PROCESS,
                                Capability.CAPABILITY_CHILD_CAPABILITY,
                                Capability.CAPABILITY_PARENT_CAPABILITY,
                                Capability.CAPABILITY_PRODUCTION_RESOURCE,
                                Capability.CAPABILITY_SEMANTIC_REFERENCE)
                        .where(
                                Capability.CAPABILITY_ID
                                        .isA(iri(AMS.Capability))
                                        .and(capabilityLabelPattern)
                                        .and(capabilitySourceIdPattern)
                                        .and(capabilityDescriptionPattern)
                                        .and(capabilityPropertyPattern)
                                        .and(capabilityProcessPattern)
                                        .and(capabilityChildPattern)
                                        .and(capabilityParentPattern)
                                        .and(capabilityProductionResourcePattern)
                                        .and(capabilitySemanticReferencePattern));
        if (iri != null) {
            return selectQuery
                    .groupBy(
                            Capability.CAPABILITY_ID,
                            Capability.CAPABILITY_SOURCE_ID,
                            Capability.CAPABILITY_LABEL,
                            Capability.CAPABILITY_DESCRIPTION,
                            Capability.CAPABILITY_PROPERTY,
                            Capability.CAPABILITY_PROCESS,
                            Capability.CAPABILITY_CHILD_CAPABILITY,
                            Capability.CAPABILITY_PARENT_CAPABILITY,
                            Capability.CAPABILITY_PRODUCTION_RESOURCE,
                            Capability.CAPABILITY_SEMANTIC_REFERENCE)
                    .having(Expressions.equals(Capability.CAPABILITY_ID, iri(iri)));
        }
        return selectQuery;
    }

    public static void mapCapability(BindingSet querySolution, Capability capability) {
        capability.setId(QueryResultUtils.getIRI(querySolution, Capability.CAPABILITY_ID));
        capability.setSourceId(
                QueryResultUtils.getStringMaybe(querySolution, Capability.CAPABILITY_SOURCE_ID));
        var label = QueryResultUtils.getValueMaybe(querySolution, Capability.CAPABILITY_LABEL);
        if (label != null) {
            capability.setLabel(label.stringValue());
            if (label.isLiteral() && ((Literal) label).getLanguage().isPresent()) {
                capability.setLabelLanguageCode(((Literal) label).getLanguage().get());
            }
        }
        var description =
                QueryResultUtils.getValueMaybe(querySolution, Capability.CAPABILITY_DESCRIPTION);
        if (description != null) {
            capability.setDescription(description.stringValue());
            if (description.isLiteral() && ((Literal) description).getLanguage().isPresent()) {
                capability.setDescriptionLanguageCode(((Literal) description).getLanguage().get());
            }
        }
    }

    @Override
    protected NamedSparqlSupplier getInsertSparql(Capability capability) {
        TriplePattern childPattern =
                GraphPatterns.tp(
                        Capability.CAPABILITY_ID,
                        iri(AMS.generalizes),
                        Capability.CAPABILITY_CHILD_CAPABILITY);
        for (int i = 1; i < capability.getChildCapabilities().size(); i++) {
            childPattern =
                    childPattern.andHas(
                            iri(AMS.generalizes),
                            SparqlBuilder.var(
                                    Capability.CAPABILITY_CHILD_CAPABILITY.getVarName() + i));
        }

        TriplePattern parentPattern =
                GraphPatterns.tp(
                        Capability.CAPABILITY_ID,
                        iri(AMS.specializes),
                        Capability.CAPABILITY_PARENT_CAPABILITY);
        for (int i = 1; i < capability.getParentCapabilities().size(); i++) {
            parentPattern =
                    parentPattern.andHas(
                            iri(AMS.specializes),
                            SparqlBuilder.var(
                                    Capability.CAPABILITY_PARENT_CAPABILITY.getVarName() + i));
        }
        TriplePattern finalChildPattern = childPattern;
        TriplePattern finalParentPattern = parentPattern;
        return NamedSparqlSupplier.of(
                KEY_PREFIX_INSERT,
                () ->
                        Queries.INSERT(
                                        Capability.CAPABILITY_ID
                                                .isA(iri(AMS.Capability))
                                                .andHas(
                                                        iri(AMS.externalIdentifier),
                                                        Capability.CAPABILITY_SOURCE_ID)
                                                .andHas(
                                                        iri(RDFS.LABEL),
                                                        Capability.CAPABILITY_LABEL)
                                                .andHas(
                                                        iri(RDFS.COMMENT),
                                                        Capability.CAPABILITY_DESCRIPTION)
                                                .andHas(
                                                        iri(AMS.hasSemantic),
                                                        SemanticReference.SEMANTIC_REFERENCE_ID),
                                        finalChildPattern,
                                        finalParentPattern,
                                        SemanticReference.SEMANTIC_REFERENCE_ID
                                                .isA(iri(AMS.SemanticReference))
                                                .andHas(
                                                        iri(AMS.identifier),
                                                        SemanticReference
                                                                .SEMANTIC_REFERENCE_SOURCE_URI)
                                                .andHas(
                                                        iri(RDFS.COMMENT),
                                                        SemanticReference
                                                                .SEMANTIC_REFERENCE_DESCRIPTION)
                                                .andHas(
                                                        iri(RDFS.LABEL),
                                                        SemanticReference.SEMANTIC_REFERENCE_LABEL))
                                .getQueryString());
    }

    public static void populateCapabilityBindingsForUpdate(
            MutableBindings bindingsBuilder, Capability capability) {
        bindingsBuilder.addMaybe(Capability.CAPABILITY_SOURCE_ID, capability.getSourceId());
        if (capability.getLabel() != null) {
            var label = Values.literal(capability.getLabel(), capability.getLabelLanguageCode());
            bindingsBuilder.add(Capability.CAPABILITY_LABEL, label);
        }
        if (capability.getDescription() != null) {
            var description =
                    Values.literal(
                            capability.getDescription(), capability.getDescriptionLanguageCode());
            bindingsBuilder.add(Capability.CAPABILITY_DESCRIPTION, description);
        }
        for (var semRef : capability.getSemanticReferences()) {
            populateSemanticReferenceForUpdate(bindingsBuilder, semRef, false);
        }
        var iterator = capability.getChildCapabilities().iterator();
        for (int i = 0; i < capability.getChildCapabilities().size(); i++) {
            if (i == 0) {
                bindingsBuilder.add(
                        Capability.CAPABILITY_CHILD_CAPABILITY, iterator.next().getId());
            } else {
                bindingsBuilder.add(
                        SparqlBuilder.var(Capability.CAPABILITY_CHILD_CAPABILITY.getVarName() + i),
                        iterator.next().getId());
            }
        }
        var parentIterator = capability.getParentCapabilities().iterator();
        for (int i = 0; i < capability.getParentCapabilities().size(); i++) {
            if (i == 0) {
                bindingsBuilder.add(
                        Capability.CAPABILITY_PARENT_CAPABILITY, parentIterator.next().getId());
            } else {
                bindingsBuilder.add(
                        SparqlBuilder.var(Capability.CAPABILITY_PARENT_CAPABILITY.getVarName() + i),
                        parentIterator.next().getId());
            }
        }
    }

    @Override
    protected void populateBindingsForUpdate(
            MutableBindings bindingsBuilder, Capability capability) {
        populateCapabilityBindingsForUpdate(bindingsBuilder, capability);
    }

    @Override
    protected IRI getInputId(Capability capability) {
        if (capability.getId() == null) {
            this.getRdf4JTemplate()
                    .applyToConnection(
                            co -> {
                                IRI iri;
                                do {
                                    iri = this.getRdf4JTemplate().getNewUUID();
                                } while (co.hasStatement(iri, null, null, true));
                                return iri;
                            });
        }
        return capability.getId();
    }
}
