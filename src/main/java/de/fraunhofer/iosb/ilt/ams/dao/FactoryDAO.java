package de.fraunhofer.iosb.ilt.ams.dao;

import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri;

import de.fraunhofer.iosb.ilt.ams.AMS;
import de.fraunhofer.iosb.ilt.ams.model.Enterprise;
import de.fraunhofer.iosb.ilt.ams.model.Factory;
import de.fraunhofer.iosb.ilt.ams.model.Location;
import de.fraunhofer.iosb.ilt.ams.model.ObjectRdf4jRepository;
import java.util.LinkedList;
import java.util.List;
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
import org.eclipse.rdf4j.spring.dao.support.sparql.NamedSparqlSupplier;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.eclipse.rdf4j.spring.util.QueryResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FactoryDAO extends RDF4JCRUDDao<Factory, Factory, IRI> {

    public static final GraphPatternNotTriples labelPattern =
            GraphPatterns.optional(Factory.FACTORY_ID.has(iri(RDFS.LABEL), Factory.FACTORY_LABEL));
    public static final GraphPatternNotTriples sourceIdPattern =
            GraphPatterns.optional(
                    Factory.FACTORY_ID.has(iri(AMS.externalIdentifier), Factory.FACTORY_SOURCE_ID));
    public static final GraphPatternNotTriples locationPattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Factory.FACTORY_ID.has(iri(AMS.has), Factory.FACTORY_LOCATION),
                            Factory.FACTORY_LOCATION.isA(AMS.Location)));
    public static final GraphPatternNotTriples factoryPropertyPattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Factory.FACTORY_ID.has(iri(AMS.has), Factory.FACTORY_PROPERTIES),
                            Factory.FACTORY_PROPERTIES.isA(AMS.Property)));
    public static final GraphPatternNotTriples descriptionPattern =
            GraphPatterns.optional(
                    Factory.FACTORY_ID.has(iri(RDFS.COMMENT), Factory.FACTORY_DESCRIPTION));
    public static final GraphPatternNotTriples factoryEnterprisePattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            GraphPatterns.union(
                                    Factory.FACTORY_ID.has(
                                            iri(AMS.has), Factory.FACTORY_ENTERPRISE),
                                    Factory.FACTORY_ID.has(
                                            iri(AMS.containedIn), Factory.FACTORY_ENTERPRISE)),
                            Factory.FACTORY_ENTERPRISE.isA(AMS.Enterprise)));
    public static final GraphPatternNotTriples factoryMachinePattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Factory.FACTORY_ID.has(iri(AMS.contains), Factory.FACTORY_MACHINES),
                            Factory.FACTORY_MACHINES.isA(AMS.Machine)));
    public static final GraphPatternNotTriples factoryHumanResourcePattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Factory.FACTORY_ID.has(
                                    iri(AMS.contains), Factory.FACTORY_HUMAN_RESOURCES),
                            Factory.FACTORY_HUMAN_RESOURCES.isA(AMS.HumanResource)));
    public static final GraphPatternNotTriples enterpriseSubsidiaryEnterprisePattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Enterprise.ENTERPRISE_ID.has(
                                    iri(AMS.contains),
                                    Enterprise.ENTERPRISE_SUBSIDIARY_ENTERPRISES),
                            Enterprise.ENTERPRISE_SUBSIDIARY_ENTERPRISES.isA(AMS.Enterprise)));
    public static final GraphPatternNotTriples factoryProductPattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Factory.FACTORY_ID.has(iri(AMS.contains), Factory.FACTORY_PRODUCTS),
                            Factory.FACTORY_PRODUCTS.isA(AMS.Product)));
    public static final GraphPatternNotTriples factoryProcessPattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Factory.FACTORY_ID.has(iri(AMS.provides), Factory.FACTORY_PROCESSES),
                            Factory.FACTORY_PROCESSES.isA(AMS.Process)));
    public static final GraphPatternNotTriples factoryCertificatePattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Factory.FACTORY_ID.has(
                                    iri(AMS.certificate), Factory.FACTORY_CERTIFICATES),
                            Factory.FACTORY_CERTIFICATES.isA(AMS.Property)));

    private List<Factory> factoryList;

    @Autowired ObjectRdf4jRepository repo;

    public FactoryDAO(RDF4JTemplate rdf4JTemplate) {
        super(rdf4JTemplate);
    }

    @Override
    protected void populateIdBindings(MutableBindings bindingsBuilder, IRI iri) {
        bindingsBuilder.add(Factory.FACTORY_ID, iri);
    }

    @Override
    protected NamedSparqlSupplierPreparer prepareNamedSparqlSuppliers(
            NamedSparqlSupplierPreparer preparer) {
        return null;
    }

    @Override
    protected Factory mapSolution(BindingSet querySolution) {
        repo.emptyProcessedIds();
        Factory factory = null;

        for (Factory f : factoryList) {
            if (f.getId().equals(QueryResultUtils.getIRI(querySolution, Factory.FACTORY_ID))) {
                factory = f;
                break;
            }
        }
        if (factory == null) {
            factory = new Factory();
            mapFactory(querySolution, factory);
            var location = QueryResultUtils.getIRIMaybe(querySolution, Factory.FACTORY_LOCATION);
            if (location != null) {
                factory.setLocation(repo.getLocationById(location));
            }
            factoryList.add(factory);
        }
        var propertyId = QueryResultUtils.getIRIMaybe(querySolution, Factory.FACTORY_PROPERTIES);
        if (propertyId != null) {
            factory.addProperty(repo.getPropertyById(propertyId));
        }
        var machineId = QueryResultUtils.getIRIMaybe(querySolution, Factory.FACTORY_MACHINES);
        if (machineId != null) {
            factory.addMachine(repo.getMachineById(machineId));
        }
        var humanResourceId =
                QueryResultUtils.getIRIMaybe(querySolution, Factory.FACTORY_HUMAN_RESOURCES);
        if (humanResourceId != null) {
            factory.addHumanResource(repo.getHumanResourceById(humanResourceId));
        }
        var enterpriseId = QueryResultUtils.getIRIMaybe(querySolution, Factory.FACTORY_ENTERPRISE);
        if (enterpriseId != null && factory.getEnterprise() == null) {
            factory.setEnterprise(repo.getEnterpriseById(enterpriseId));
        }
        var processId = QueryResultUtils.getIRIMaybe(querySolution, Factory.FACTORY_PROCESSES);
        if (processId != null) {
            factory.addProcess(repo.getProcessById(processId));
        }
        var productId = QueryResultUtils.getIRIMaybe(querySolution, Factory.FACTORY_PRODUCTS);
        if (productId != null) {
            factory.addProduct(repo.getProductById(productId));
        }
        var certificateId =
                QueryResultUtils.getIRIMaybe(querySolution, Factory.FACTORY_CERTIFICATES);
        if (certificateId != null) {
            factory.addCertificate(repo.getPropertyById(certificateId));
        }
        return factory;
    }

    @Override
    protected NamedSparqlSupplier getInsertSparql(Factory factory) {
        return NamedSparqlSupplier.of(
                "insert",
                () ->
                        Queries.INSERT(
                                        Factory.FACTORY_ID
                                                .isA(iri(AMS.Factory))
                                                .andHas(
                                                        iri(AMS.externalIdentifier),
                                                        Factory.FACTORY_SOURCE_ID)
                                                .andHas(iri(RDFS.LABEL), Factory.FACTORY_LABEL)
                                                .andHas(
                                                        iri(RDFS.COMMENT),
                                                        Factory.FACTORY_DESCRIPTION)
                                                .andHas(iri(AMS.has), Factory.FACTORY_LOCATION),
                                        Factory.FACTORY_LOCATION
                                                .isA(iri(AMS.Location))
                                                .andHas(
                                                        iri(AMS.latitude),
                                                        Location.LOCATION_LATITUDE)
                                                .andHas(
                                                        iri(AMS.longitude),
                                                        Location.LOCATION_LONGITUDE)
                                                .andHas(iri(AMS.street), Location.LOCATION_STREET)
                                                .andHas(
                                                        iri(AMS.streetNumber),
                                                        Location.LOCATION_STREET_NUMBER)
                                                .andHas(iri(AMS.zipcode), Location.LOCATION_ZIP)
                                                .andHas(iri(AMS.city), Location.LOCATION_CITY)
                                                .andHas(
                                                        iri(AMS.country),
                                                        Location.LOCATION_COUNTRY))
                                .getQueryString());
    }

    @Override
    protected NamedSparqlSupplier getUpdateSparql(Factory factory) {
        return super.getUpdateSparql(factory);
    }

    @Override
    protected String getReadQuery() {
        factoryList = new LinkedList<>();
        return getFactorySelectPattern(null).from(repo.getGraphNameForQuery()).getQueryString();
    }

    public static void mapFactory(BindingSet querySolution, Factory factory) {
        factory.setId(QueryResultUtils.getIRI(querySolution, Factory.FACTORY_ID));
        factory.setSourceId(
                QueryResultUtils.getStringMaybe(querySolution, Factory.FACTORY_SOURCE_ID));
        var label = QueryResultUtils.getValueMaybe(querySolution, Factory.FACTORY_LABEL);
        if (label != null) {
            factory.setLabel(label.stringValue());
            if (((Literal) label).getLanguage().isPresent()) {
                factory.setLabelLanguageCode(((Literal) label).getLanguage().get());
            }
        }
        var description =
                QueryResultUtils.getValueMaybe(querySolution, Factory.FACTORY_DESCRIPTION);
        if (description != null) {
            factory.setDescription(description.stringValue());
            if (((Literal) description).getLanguage().isPresent()) {
                factory.setDescriptionLanguageCode(((Literal) description).getLanguage().get());
            }
        }
    }

    public static SelectQuery getFactorySelectPattern(String iri) {
        SelectQuery selectQuery =
                Queries.SELECT(
                                Factory.FACTORY_ID,
                                Factory.FACTORY_SOURCE_ID,
                                Factory.FACTORY_LABEL,
                                Factory.FACTORY_LABEL_LANGUAGE_CODE,
                                Factory.FACTORY_DESCRIPTION,
                                Factory.FACTORY_DESCRIPTION_LANGUAGE_CODE,
                                Factory.FACTORY_LOCATION,
                                Factory.FACTORY_PROPERTIES,
                                Factory.FACTORY_PRODUCTS,
                                Factory.FACTORY_MACHINES,
                                Factory.FACTORY_HUMAN_RESOURCES,
                                Factory.FACTORY_ENTERPRISE,
                                Factory.FACTORY_PROCESSES,
                                Factory.FACTORY_CERTIFICATES)
                        .where(
                                Factory.FACTORY_ID
                                        .isA(iri(AMS.PhysicalFactory))
                                        .and(locationPattern)
                                        .and(labelPattern)
                                        .and(descriptionPattern)
                                        .and(sourceIdPattern)
                                        .and(factoryMachinePattern)
                                        .and(factoryHumanResourcePattern)
                                        .and(factoryEnterprisePattern)
                                        .and(factoryProcessPattern)
                                        .and(factoryProductPattern)
                                        .and(factoryPropertyPattern)
                                        .and(factoryCertificatePattern));
        if (iri != null) {
            return selectQuery
                    .groupBy(
                            Factory.FACTORY_ID,
                            Factory.FACTORY_SOURCE_ID,
                            Factory.FACTORY_LABEL,
                            Factory.FACTORY_LABEL_LANGUAGE_CODE,
                            Factory.FACTORY_DESCRIPTION,
                            Factory.FACTORY_DESCRIPTION_LANGUAGE_CODE,
                            Factory.FACTORY_LOCATION,
                            Factory.FACTORY_PROPERTIES,
                            Factory.FACTORY_PRODUCTS,
                            Factory.FACTORY_MACHINES,
                            Factory.FACTORY_HUMAN_RESOURCES,
                            Factory.FACTORY_ENTERPRISE,
                            Factory.FACTORY_PROCESSES,
                            Factory.FACTORY_CERTIFICATES)
                    .having(Expressions.equals(Factory.FACTORY_ID, iri(iri)));
        }
        return selectQuery;
    }

    @Override
    protected void populateBindingsForUpdate(MutableBindings bindingsBuilder, Factory factory) {
        populateFactoryBindingsForUpdate(bindingsBuilder, factory);
    }

    public static void populateFactoryBindingsForUpdate(
            MutableBindings bindingsBuilder, Factory factory) {
        bindingsBuilder.addMaybe(Factory.FACTORY_SOURCE_ID, factory.getSourceId());
        bindingsBuilder.addMaybe(Factory.FACTORY_LABEL, factory.getLabel());
        bindingsBuilder.addMaybe(Factory.FACTORY_DESCRIPTION, factory.getDescription());
        if (factory.getLocation() != null) {
            bindingsBuilder.add(Factory.FACTORY_LOCATION, factory.getLocation().getId());
            bindingsBuilder.addMaybe(Location.LOCATION_ID, factory.getLocation().getId());
            bindingsBuilder.addMaybe(
                    Location.LOCATION_LATITUDE, factory.getLocation().getLatitude());
            bindingsBuilder.addMaybe(
                    Location.LOCATION_LONGITUDE, factory.getLocation().getLongitude());
            bindingsBuilder.addMaybe(Location.LOCATION_STREET, factory.getLocation().getStreet());
            bindingsBuilder.addMaybe(
                    Location.LOCATION_STREET_NUMBER, factory.getLocation().getStreetNumber());
            bindingsBuilder.addMaybe(Location.LOCATION_ZIP, factory.getLocation().getZip());
            bindingsBuilder.addMaybe(Location.LOCATION_CITY, factory.getLocation().getCity());
            bindingsBuilder.addMaybe(Location.LOCATION_COUNTRY, factory.getLocation().getCountry());
        }

        if (factory.getEnterprise() != null) {
            bindingsBuilder.add(Factory.FACTORY_ENTERPRISE, factory.getEnterprise().getId());
        }

        // populateEnterpriseBindingsForUpdate(bindingsBuilder, factory.getEnterprise());

        factory.getMachines()
                .forEach(machine -> bindingsBuilder.add(Factory.FACTORY_MACHINES, machine.getId()));
        factory.getHumanResources()
                .forEach(hr -> bindingsBuilder.add(Factory.FACTORY_HUMAN_RESOURCES, hr.getId()));
        factory.getProperties()
                .forEach(
                        property ->
                                bindingsBuilder.add(Factory.FACTORY_PROPERTIES, property.getId()));
        factory.getProducts()
                .forEach(product -> bindingsBuilder.add(Factory.FACTORY_PRODUCTS, product.getId()));
        factory.getProcesses()
                .forEach(
                        process -> bindingsBuilder.add(Factory.FACTORY_PROCESSES, process.getId()));
    }

    @Override
    protected IRI getInputId(Factory factory) {
        if (factory.getId() == null) {
            return getRdf4JTemplate().getNewUUID();
        }
        return factory.getId();
    }
}
