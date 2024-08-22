package de.fraunhofer.iosb.ilt.ams.dao;

import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri;

import de.fraunhofer.iosb.ilt.ams.AMS;
import de.fraunhofer.iosb.ilt.ams.model.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatternNotTriples;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.spring.dao.RDF4JCRUDDao;
import org.eclipse.rdf4j.spring.dao.support.bindingsBuilder.MutableBindings;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.eclipse.rdf4j.spring.util.QueryResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductionResourceDAO
        extends RDF4JCRUDDao<ProductionResource, ProductionResource, IRI> {
    public static final String MACHINE = "machine";
    public static final String HUMAN_RESOURCE = "human_resource";
    public static final GraphPatternNotTriples productionResourceSourceIdPattern =
            GraphPatterns.optional(
                    ProductionResource.PRODUCTION_RESOURCE_ID.has(
                            iri(AMS.externalIdentifier),
                            ProductionResource.PRODUCTION_RESOURCE_SOURCE_ID));
    public static final GraphPatternNotTriples productionResourceDescriptionPattern =
            GraphPatterns.optional(
                    ProductionResource.PRODUCTION_RESOURCE_ID.has(
                            iri(RDFS.COMMENT), ProductionResource.PRODUCTION_RESOURCE_DESCRIPTION));

    public static final GraphPatternNotTriples productionResourceProvidingPattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            ProductionResource.PRODUCTION_RESOURCE_ID.has(
                                    iri(AMS.provides),
                                    ProductionResource.PRODUCTION_RESOURCE_PROVIDING_PROCESS),
                            ProductionResource.PRODUCTION_RESOURCE_PROVIDING_PROCESS.isA(
                                    AMS.Process)));
    public static final GraphPatternNotTriples productionResourceUsingPattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            ProductionResource.PRODUCTION_RESOURCE_ID.has(
                                    iri(AMS.usedBy),
                                    ProductionResource.PRODUCTION_RESOURCE_USING_PROCESS),
                            ProductionResource.PRODUCTION_RESOURCE_USING_PROCESS.isA(AMS.Process)));
    public static final GraphPatternNotTriples productionResourceProvidedPattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            ProductionResource.PRODUCTION_RESOURCE_ID.has(
                                    iri(AMS.provides),
                                    ProductionResource.PRODUCTION_RESOURCE_PROVIDED_CAPABILITY),
                            ProductionResource.PRODUCTION_RESOURCE_PROVIDED_CAPABILITY.isA(
                                    AMS.Capability)));
    public static final GraphPatternNotTriples machinePropertyPattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            ProductionResource.PRODUCTION_RESOURCE_ID.has(
                                    iri(AMS.has), Machine.MACHINE_PROPERTY),
                            Machine.MACHINE_PROPERTY.isA(AMS.Property)));
    public static final GraphPatternNotTriples humanResourceCertificatePattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            ProductionResource.PRODUCTION_RESOURCE_ID.has(
                                    iri(AMS.certificate),
                                    HumanResource.HUMAN_RESOURCE_CERTIFICATES),
                            HumanResource.HUMAN_RESOURCE_CERTIFICATES.isA(AMS.Property)));
    public static final GraphPatternNotTriples humanResourcePropertyPattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            ProductionResource.PRODUCTION_RESOURCE_ID.has(
                                    iri(AMS.has), HumanResource.HUMAN_RESOURCE_PROPERTIES),
                            HumanResource.HUMAN_RESOURCE_PROPERTIES.isA(AMS.Property)));

    @Autowired ObjectRdf4jRepository repo;
    List<ProductionResource> productionResources;

    public ProductionResourceDAO(RDF4JTemplate rdf4JTemplate) {
        super(rdf4JTemplate);
    }

    @Override
    protected void populateIdBindings(MutableBindings bindingsBuilder, IRI iri) {
        bindingsBuilder.add(ProductionResource.PRODUCTION_RESOURCE_ID, iri);
    }

    @Override
    protected NamedSparqlSupplierPreparer prepareNamedSparqlSuppliers(
            NamedSparqlSupplierPreparer preparer) {
        return preparer.forKey(MACHINE)
                .supplySparql(
                        Queries.SELECT(Machine.MACHINE_ID)
                                .where(Machine.MACHINE_ID.isA(AMS.Machine))
                                .getQueryString())
                .forKey(HUMAN_RESOURCE)
                .supplySparql(
                        Queries.SELECT(HumanResource.HUMAN_RESOURCE_ID)
                                .where(HumanResource.HUMAN_RESOURCE_ID.isA(AMS.HumanResource))
                                .getQueryString());
    }

    public static SelectQuery getProductionResourceSelectQuery(String iri) {
        SelectQuery selectQuery =
                Queries.SELECT(
                                ProductionResource.PRODUCTION_RESOURCE_ID,
                                ProductionResource.PRODUCTION_RESOURCE_SOURCE_ID,
                                ProductionResource.PRODUCTION_RESOURCE_LABEL,
                                ProductionResource.PRODUCTION_RESOURCE_DESCRIPTION,
                                ProductionResource.PRODUCTION_RESOURCE_PROVIDING_PROCESS,
                                ProductionResource.PRODUCTION_RESOURCE_PROVIDING_PROCESS,
                                ProductionResource.PRODUCTION_RESOURCE_USING_PROCESS,
                                ProductionResource.PRODUCTION_RESOURCE_PROVIDED_CAPABILITY,
                                Machine.MACHINE_PROPERTY,
                                HumanResource.HUMAN_RESOURCE_CERTIFICATES,
                                HumanResource.HUMAN_RESOURCE_PROPERTIES)
                        .where(
                                ProductionResource.PRODUCTION_RESOURCE_ID
                                        .isA(AMS.HumanResource)
                                        .andHas(
                                                RDFS.LABEL,
                                                ProductionResource.PRODUCTION_RESOURCE_LABEL)
                                        .union(
                                                ProductionResource.PRODUCTION_RESOURCE_ID
                                                        .isA(AMS.Machine)
                                                        .andHas(
                                                                RDFS.LABEL,
                                                                ProductionResource
                                                                        .PRODUCTION_RESOURCE_LABEL))
                                        .and(productionResourceDescriptionPattern)
                                        .and(productionResourceSourceIdPattern)
                                        .and(productionResourceProvidingPattern)
                                        .and(productionResourceUsingPattern)
                                        .and(productionResourceProvidedPattern)
                                        .and(humanResourceCertificatePattern)
                                        .and(humanResourcePropertyPattern));
        if (iri != null) {
            return selectQuery
                    .groupBy(
                            ProductionResource.PRODUCTION_RESOURCE_ID,
                            ProductionResource.PRODUCTION_RESOURCE_SOURCE_ID,
                            ProductionResource.PRODUCTION_RESOURCE_LABEL,
                            ProductionResource.PRODUCTION_RESOURCE_DESCRIPTION,
                            ProductionResource.PRODUCTION_RESOURCE_PROVIDING_PROCESS,
                            ProductionResource.PRODUCTION_RESOURCE_PROVIDING_PROCESS,
                            ProductionResource.PRODUCTION_RESOURCE_USING_PROCESS,
                            ProductionResource.PRODUCTION_RESOURCE_PROVIDED_CAPABILITY,
                            Machine.MACHINE_PROPERTY,
                            HumanResource.HUMAN_RESOURCE_CERTIFICATES,
                            HumanResource.HUMAN_RESOURCE_PROPERTIES)
                    .having(
                            Expressions.equals(
                                    ProductionResource.PRODUCTION_RESOURCE_ID, iri(iri)));
        }
        return selectQuery;
    }

    public static SelectQuery getMachineSelectQuery(String iri) {
        return getProductionResourceSelectQuery(iri)
                .where(
                        ProductionResource.PRODUCTION_RESOURCE_ID
                                .isA(AMS.Machine)
                                .and(machinePropertyPattern));
    }

    public static SelectQuery getHumanResourceSelectQuery(String iri) {
        return getProductionResourceSelectQuery(iri)
                .where(
                        ProductionResource.PRODUCTION_RESOURCE_ID
                                .isA(AMS.HumanResource)
                                .and(humanResourceCertificatePattern)
                                .and(humanResourcePropertyPattern));
    }

    @Override
    protected String getReadQuery() {
        productionResources = new LinkedList<>();
        return getProductionResourceSelectQuery(null)
                .from(repo.getGraphNameForQuery())
                .getQueryString();
    }

    public List<Machine> getMachines() {
        repo.emptyProcessedIds();
        return getNamedTupleQuery(MACHINE)
                .evaluateAndConvert()
                .toStream()
                .map(bindings -> QueryResultUtils.getIRI(bindings, Machine.MACHINE_ID))
                .map(repo::getMachineById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<HumanResource> getHumanResources() {
        repo.emptyProcessedIds();
        return getNamedTupleQuery(HUMAN_RESOURCE)
                .evaluateAndConvert()
                .toStream()
                .map(bindings -> QueryResultUtils.getIRI(bindings, HumanResource.HUMAN_RESOURCE_ID))
                .map(repo::getHumanResourceById)
                .collect(Collectors.toList());
    }

    public static void mapProductionResource(
            BindingSet querySolution, ProductionResource productionResource) {
        productionResource.setId(
                QueryResultUtils.getIRI(querySolution, ProductionResource.PRODUCTION_RESOURCE_ID));
        productionResource.setSourceId(
                QueryResultUtils.getStringMaybe(
                        querySolution, ProductionResource.PRODUCTION_RESOURCE_SOURCE_ID));

        var label =
                QueryResultUtils.getValueMaybe(
                        querySolution, ProductionResource.PRODUCTION_RESOURCE_LABEL);
        if (label != null) {
            productionResource.setLabel(label.stringValue());
            if (label.isLiteral() && ((Literal) label).getLanguage().isPresent()) {
                productionResource.setLabelLanguageCode(((Literal) label).getLanguage().get());
            }
        }

        var description =
                QueryResultUtils.getValueMaybe(
                        querySolution, ProductionResource.PRODUCTION_RESOURCE_DESCRIPTION);
        if (description != null) {
            productionResource.setDescription(description.stringValue());
            if (description.isLiteral() && ((Literal) description).getLanguage().isPresent()) {
                productionResource.setDescriptionLanguageCode(
                        ((Literal) description).getLanguage().get());
            }
        }
    }
}
