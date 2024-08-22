package de.fraunhofer.iosb.ilt.ams.dao;

import static de.fraunhofer.iosb.ilt.ams.dao.SemanticReferenceDAO.populateSemanticReferenceForUpdate;
import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri;

import de.fraunhofer.iosb.ilt.ams.AMS;
import de.fraunhofer.iosb.ilt.ams.model.ObjectRdf4jRepository;
import de.fraunhofer.iosb.ilt.ams.model.Property;
import de.fraunhofer.iosb.ilt.ams.model.SemanticReference;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatternNotTriples;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.spring.dao.RDF4JCRUDDao;
import org.eclipse.rdf4j.spring.dao.support.bindingsBuilder.MutableBindings;
import org.eclipse.rdf4j.spring.dao.support.sparql.NamedSparqlSupplier;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.eclipse.rdf4j.spring.util.QueryResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PropertyDAO extends RDF4JCRUDDao<Property, Property, IRI> {

    public static final GraphPatternNotTriples labelPattern =
            GraphPatterns.optional(
                    Property.PROPERTY_ID.has(iri(RDFS.LABEL), Property.PROPERTY_LABEL));
    public static final GraphPatternNotTriples descriptionPattern =
            GraphPatterns.optional(
                    Property.PROPERTY_ID.has(iri(RDFS.COMMENT), Property.PROPERTY_DESCRIPTION));
    public static final GraphPatternNotTriples sourceIdPattern =
            GraphPatterns.optional(
                    Property.PROPERTY_ID.has(
                            iri(AMS.externalIdentifier), Property.PROPERTY_SOURCE_ID));
    public static final GraphPatternNotTriples semanticReferencePattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Property.PROPERTY_ID.has(
                                    iri(AMS.hasSemantic), Property.PROPERTY_SEMANTIC_REFERENCES),
                            Property.PROPERTY_SEMANTIC_REFERENCES.isA(iri(AMS.SemanticReference))));
    public static final GraphPatternNotTriples valuePattern =
            GraphPatterns.optional(
                    Property.PROPERTY_ID.has(iri(AMS.value), Property.PROPERTY_VALUE));

    public PropertyDAO(RDF4JTemplate rdf4JTemplate) {
        super(rdf4JTemplate);
    }

    @Autowired ObjectRdf4jRepository repo;

    private List<Property> propertyList;

    @Override
    protected void populateIdBindings(MutableBindings bindingsBuilder, IRI iri) {
        bindingsBuilder.add(Property.PROPERTY_ID, iri);
    }

    @Override
    protected NamedSparqlSupplierPreparer prepareNamedSparqlSuppliers(
            NamedSparqlSupplierPreparer preparer) {
        return null;
    }

    @Override
    protected String getReadQuery() {
        propertyList = new LinkedList<>();
        return getPropertySelectQuery(null).from(repo.getGraphNameForQuery()).getQueryString();
    }

    @Override
    protected NamedSparqlSupplier getInsertSparql(Property property) {
        return NamedSparqlSupplier.of(
                KEY_PREFIX_INSERT,
                () ->
                        Queries.INSERT(
                                        Property.PROPERTY_ID
                                                .isA(iri(AMS.Property))
                                                .andHas(
                                                        iri(AMS.externalIdentifier),
                                                        Property.PROPERTY_SOURCE_ID)
                                                .andHas(iri(RDFS.LABEL), Property.PROPERTY_LABEL)
                                                .andHas(
                                                        iri(RDFS.COMMENT),
                                                        Property.PROPERTY_DESCRIPTION)
                                                .andHas(iri(AMS.value), Property.PROPERTY_VALUE)
                                                .andHas(
                                                        iri(AMS.hasSemantic),
                                                        SemanticReference.SEMANTIC_REFERENCE_ID),
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

    @Override
    protected Property mapSolution(BindingSet querySolution) {
        Property property = null;
        for (Property p : propertyList) {
            if (p.getId().equals(QueryResultUtils.getIRI(querySolution, Property.PROPERTY_ID))) {
                property = p;
                break;
            }
        }
        if (property == null) {
            property = new Property();
            mapProperty(querySolution, property);
            propertyList.add(property);
        }
        var semanticReferenceID =
                QueryResultUtils.getIRIMaybe(querySolution, Property.PROPERTY_SEMANTIC_REFERENCES);
        if (semanticReferenceID != null) {
            property.addSemanticReference(repo.getSemanticReferenceById(semanticReferenceID));
        }
        return property;
    }

    public static void mapProperty(BindingSet querySolution, Property property) {
        property.setId(QueryResultUtils.getIRI(querySolution, Property.PROPERTY_ID));
        property.setSourceId(
                QueryResultUtils.getStringMaybe(querySolution, Property.PROPERTY_SOURCE_ID));
        var label = QueryResultUtils.getValueMaybe(querySolution, Property.PROPERTY_LABEL);
        if (label != null) {
            property.setLabel(label.stringValue());
            if (label.isLiteral() && ((Literal) label).getLanguage().isPresent()) {
                property.setLabelLanguageCode(((Literal) label).getLanguage().get());
            }
        }
        var description =
                QueryResultUtils.getValueMaybe(querySolution, Property.PROPERTY_DESCRIPTION);
        if (description != null) {
            property.setDescription(description.stringValue());
            if (description.isLiteral() && ((Literal) description).getLanguage().isPresent()) {
                property.setDescriptionLanguageCode(((Literal) description).getLanguage().get());
            }
        }
        var value = QueryResultUtils.getValueMaybe(querySolution, Property.PROPERTY_VALUE);
        if (value != null) {
            property.setValue(value.stringValue());
        }
    }

    public static SelectQuery getPropertySelectQuery(String iri) {
        var selectQuery =
                Queries.SELECT(
                                Property.PROPERTY_ID,
                                Property.PROPERTY_SOURCE_ID,
                                Property.PROPERTY_LABEL,
                                Property.PROPERTY_DESCRIPTION,
                                Property.PROPERTY_SEMANTIC_REFERENCES,
                                Property.PROPERTY_VALUE)
                        .where(
                                Property.PROPERTY_ID
                                        .isA(iri(AMS.Property))
                                        .and(labelPattern)
                                        .and(sourceIdPattern)
                                        .and(semanticReferencePattern)
                                        .and(descriptionPattern)
                                        .and(valuePattern));
        if (iri != null) {
            return selectQuery
                    .groupBy(
                            Property.PROPERTY_ID,
                            Property.PROPERTY_SOURCE_ID,
                            Property.PROPERTY_LABEL,
                            Property.PROPERTY_DESCRIPTION,
                            Property.PROPERTY_SEMANTIC_REFERENCES,
                            Property.PROPERTY_VALUE)
                    .having(Expressions.equals(Property.PROPERTY_ID, iri(iri)));
        }
        return selectQuery;
    }

    @Override
    protected void populateBindingsForUpdate(MutableBindings bindingsBuilder, Property property) {
        populatePropertyBindingsForUpdate(bindingsBuilder, property, true);
    }

    public static void populatePropertyBindingsForUpdate(
            MutableBindings bindingsBuilder, Property property, boolean isUpdate) {
        if (!isUpdate) {
            bindingsBuilder.add(Property.PROPERTY_ID, property.getId());
        }
        bindingsBuilder.addMaybe(Property.PROPERTY_SOURCE_ID, property.getSourceId());
        if (property.getLabel() != null && property.getLabelLanguageCode() != null) {
            var label = Values.literal(property.getLabel(), property.getLabelLanguageCode());
            bindingsBuilder.add(Property.PROPERTY_LABEL, label);
        }
        if (property.getDescription() != null && property.getDescriptionLanguageCode() != null) {
            var description =
                    Values.literal(
                            property.getDescription(), property.getDescriptionLanguageCode());
            bindingsBuilder.add(Property.PROPERTY_DESCRIPTION, description);
        }
        bindingsBuilder.addMaybe(Property.PROPERTY_VALUE, property.getValue());
        for (var semRef : property.getSemanticReferences()) {
            populateSemanticReferenceForUpdate(bindingsBuilder, semRef, false);
        }
    }

    @Override
    protected IRI getInputId(Property property) {
        if (property.getId() == null) {
            return this.getRdf4JTemplate().getNewUUID();
        }

        return property.getId();
    }
}
