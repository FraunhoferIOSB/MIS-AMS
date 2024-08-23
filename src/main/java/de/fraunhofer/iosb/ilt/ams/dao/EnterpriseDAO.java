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
package de.fraunhofer.iosb.ilt.ams.dao;

import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri;

import de.fraunhofer.iosb.ilt.ams.AMS;
import de.fraunhofer.iosb.ilt.ams.model.*;
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
public class EnterpriseDAO extends RDF4JCRUDDao<Enterprise, Enterprise, IRI> {
    public static final GraphPatternNotTriples labelPattern =
            GraphPatterns.optional(
                    Enterprise.ENTERPRISE_ID.has(iri(RDFS.LABEL), Enterprise.ENTERPRISE_LABEL));
    public static final GraphPatternNotTriples descriptionPattern =
            GraphPatterns.optional(
                    Enterprise.ENTERPRISE_ID.has(
                            iri(RDFS.COMMENT), Enterprise.ENTERPRISE_DESCRIPTION));
    public static final GraphPatternNotTriples sourceIdPattern =
            GraphPatterns.optional(
                    Enterprise.ENTERPRISE_ID.has(
                            iri(AMS.externalIdentifier), Enterprise.ENTERPRISE_SOURCE_ID));
    public static final GraphPatternNotTriples locationPattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Enterprise.ENTERPRISE_ID.has(
                                    iri(AMS.has), Enterprise.ENTERPRISE_LOCATION),
                            Enterprise.ENTERPRISE_LOCATION.isA(AMS.Location)));
    public static final GraphPatternNotTriples enterprisePropertyPattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Enterprise.ENTERPRISE_ID.has(
                                    iri(AMS.has), Enterprise.ENTERPRISE_PROPERTIES),
                            Enterprise.ENTERPRISE_PROPERTIES.isA(AMS.Property)));
    public static final GraphPatternNotTriples enterpriseFactoryPattern =
            GraphPatterns.optional(
                    GraphPatterns.union(
                            GraphPatterns.and(
                                    Enterprise.ENTERPRISE_ID.has(
                                            iri(AMS.contains), Enterprise.ENTERPRISE_FACTORIES),
                                    Enterprise.ENTERPRISE_FACTORIES.isA(AMS.VirtualFactory)),
                            GraphPatterns.and(
                                    Enterprise.ENTERPRISE_ID.has(
                                            iri(AMS.contains), Enterprise.ENTERPRISE_FACTORIES),
                                    Enterprise.ENTERPRISE_FACTORIES.isA(AMS.Factory)),
                            GraphPatterns.and(
                                    Enterprise.ENTERPRISE_ID.has(
                                            iri(AMS.contains), Enterprise.ENTERPRISE_FACTORIES),
                                    Enterprise.ENTERPRISE_FACTORIES.isA(AMS.PhysicalFactory))));
    public static final GraphPatternNotTriples enterpriseProductionResourcePattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Enterprise.ENTERPRISE_ID.has(
                                    iri(AMS.contains), Enterprise.ENTERPRISE_PRODUCTION_RESOURCES),
                            Enterprise.ENTERPRISE_PRODUCTION_RESOURCES.isA(
                                    AMS.ProductionResource)));
    public static final GraphPatternNotTriples enterpriseSubsidiaryEnterprisePattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Enterprise.ENTERPRISE_ID.has(
                                    iri(AMS.contains),
                                    Enterprise.ENTERPRISE_SUBSIDIARY_ENTERPRISES),
                            Enterprise.ENTERPRISE_SUBSIDIARY_ENTERPRISES.isA(AMS.Enterprise)));
    public static final GraphPatternNotTriples enterpriseProductPattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Enterprise.ENTERPRISE_ID.has(
                                    iri(AMS.contains), Enterprise.ENTERPRISE_PRODUCTS),
                            Enterprise.ENTERPRISE_PRODUCTS.isA(AMS.Product)));
    public static final GraphPatternNotTriples enterpriseProcessPattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Enterprise.ENTERPRISE_ID.has(
                                    iri(AMS.contains), Enterprise.ENTERPRISE_PROCESSES),
                            Enterprise.ENTERPRISE_PROCESSES.isA(AMS.Process)));

    public static final GraphPatternNotTriples enterpriseCertificatePattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Enterprise.ENTERPRISE_ID.has(
                                    iri(AMS.certificate), Enterprise.ENTERPRISE_CERTIFICATES),
                            Enterprise.ENTERPRISE_CERTIFICATES.isA(AMS.Property)));

    public static final GraphPatternNotTriples logoPattern =
            GraphPatterns.optional(
                    Enterprise.ENTERPRISE_ID.has(iri(AMS.logo), Enterprise.ENTERPRISE_LOGO));

    private List<Enterprise> enterpriseList = new LinkedList<>();

    @Autowired ObjectRdf4jRepository objectRepo;

    public EnterpriseDAO(RDF4JTemplate rdf4JTemplate) {
        super(rdf4JTemplate);
    }

    @Override
    protected void populateIdBindings(MutableBindings bindingsBuilder, IRI iri) {
        bindingsBuilder.add(Enterprise.ENTERPRISE_ID, iri);
    }

    @Override
    protected NamedSparqlSupplierPreparer prepareNamedSparqlSuppliers(
            NamedSparqlSupplierPreparer preparer) {
        return null;
    }

    @Override
    protected Enterprise mapSolution(BindingSet querySolution) {
        objectRepo.emptyProcessedIds();
        Enterprise enterprise = null;
        for (Enterprise e : enterpriseList) {
            if (e.getId()
                    .equals(QueryResultUtils.getIRI(querySolution, Enterprise.ENTERPRISE_ID))) {
                enterprise = e;
                break;
            }
        }
        if (enterprise == null) {
            enterprise = new Enterprise();
            mapEnterprise(querySolution, enterprise);
            var location =
                    QueryResultUtils.getIRIMaybe(querySolution, Enterprise.ENTERPRISE_LOCATION);
            if (location != null) {
                enterprise.setLocation(objectRepo.getLocationById(location));
            }
            enterpriseList.add(enterprise);
        }
        var factory = QueryResultUtils.getIRIMaybe(querySolution, Enterprise.ENTERPRISE_FACTORIES);
        if (factory != null) {
            enterprise.addFactory(objectRepo.getFactoryByIri(factory));
        }
        var subsidiaryEnterprise =
                QueryResultUtils.getIRIMaybe(
                        querySolution, Enterprise.ENTERPRISE_SUBSIDIARY_ENTERPRISES);
        if (subsidiaryEnterprise != null) {
            enterprise.addSubsidiaryEnterprise(objectRepo.getEnterpriseById(subsidiaryEnterprise));
        }
        var property =
                QueryResultUtils.getIRIMaybe(querySolution, Enterprise.ENTERPRISE_PROPERTIES);
        if (property != null) {
            enterprise.addProperty(objectRepo.getPropertyById(property));
        }
        var product = QueryResultUtils.getIRIMaybe(querySolution, Enterprise.ENTERPRISE_PRODUCTS);
        if (product != null) {
            enterprise.addProduct(objectRepo.getProductById(product));
        }
        var process = QueryResultUtils.getIRIMaybe(querySolution, Enterprise.ENTERPRISE_PROCESSES);
        if (process != null) {
            enterprise.addProcess(objectRepo.getProcessById(process));
        }
        var productionResource =
                QueryResultUtils.getIRIMaybe(
                        querySolution, Enterprise.ENTERPRISE_PRODUCTION_RESOURCES);
        if (productionResource != null) {
            enterprise.addProductionResource(
                    objectRepo.getProductionResourceById(productionResource));
        }
        var supplyChain =
                QueryResultUtils.getIRIMaybe(querySolution, Enterprise.ENTERPRISE_SUPPLY_CHAINS);
        if (supplyChain != null) {
            enterprise.addSupplyChain(objectRepo.getSupplyChainById(supplyChain));
        }

        var certificate =
                QueryResultUtils.getIRIMaybe(querySolution, Enterprise.ENTERPRISE_CERTIFICATES);
        if (certificate != null) {
            enterprise.addCertificate(objectRepo.getPropertyById(certificate));
        }
        return enterprise;
    }

    @Override
    protected String getReadQuery() {
        enterpriseList = new LinkedList<>();
        return getEnterpriseSelectQuery(null)
                .from(objectRepo.getGraphNameForQuery())
                .getQueryString();
    }

    public static SelectQuery getEnterpriseSelectQuery(String iri) {
        var selectQuery =
                Queries.SELECT(
                                Enterprise.ENTERPRISE_ID,
                                Enterprise.ENTERPRISE_SOURCE_ID,
                                Enterprise.ENTERPRISE_LABEL,
                                Enterprise.ENTERPRISE_LABEL_LANGUAGE_CODE,
                                Enterprise.ENTERPRISE_DESCRIPTION,
                                Enterprise.ENTERPRISE_DESCRIPTION_LANGUAGE_CODE,
                                Enterprise.ENTERPRISE_LOCATION,
                                Enterprise.ENTERPRISE_FACTORIES,
                                Enterprise.ENTERPRISE_SUBSIDIARY_ENTERPRISES,
                                Enterprise.ENTERPRISE_PROPERTIES,
                                Enterprise.ENTERPRISE_PRODUCTS,
                                Enterprise.ENTERPRISE_PROCESSES,
                                Enterprise.ENTERPRISE_PRODUCTION_RESOURCES,
                                Enterprise.ENTERPRISE_SUPPLY_CHAINS,
                                Enterprise.ENTERPRISE_LOGO,
                                Enterprise.ENTERPRISE_CERTIFICATES)
                        .where(
                                Enterprise.ENTERPRISE_ID
                                        .isA(iri(AMS.Enterprise))
                                        .and(locationPattern)
                                        .and(descriptionPattern)
                                        .and(labelPattern)
                                        .and(enterprisePropertyPattern)
                                        .and(sourceIdPattern)
                                        .and(enterpriseFactoryPattern)
                                        .and(enterpriseProductionResourcePattern)
                                        .and(enterpriseSubsidiaryEnterprisePattern)
                                        .and(enterpriseProductPattern)
                                        .and(enterpriseProcessPattern)
                                        .and(logoPattern)
                                        .and(enterpriseCertificatePattern));
        if (iri != null) {
            return selectQuery
                    .groupBy(
                            Enterprise.ENTERPRISE_ID,
                            Enterprise.ENTERPRISE_SOURCE_ID,
                            Enterprise.ENTERPRISE_LABEL,
                            Enterprise.ENTERPRISE_LABEL_LANGUAGE_CODE,
                            Enterprise.ENTERPRISE_DESCRIPTION,
                            Enterprise.ENTERPRISE_DESCRIPTION_LANGUAGE_CODE,
                            Enterprise.ENTERPRISE_LOCATION,
                            Enterprise.ENTERPRISE_FACTORIES,
                            Enterprise.ENTERPRISE_SUBSIDIARY_ENTERPRISES,
                            Enterprise.ENTERPRISE_PROPERTIES,
                            Enterprise.ENTERPRISE_PRODUCTS,
                            Enterprise.ENTERPRISE_PROCESSES,
                            Enterprise.ENTERPRISE_PRODUCTION_RESOURCES,
                            Enterprise.ENTERPRISE_SUPPLY_CHAINS,
                            Enterprise.ENTERPRISE_LOGO,
                            Enterprise.ENTERPRISE_CERTIFICATES)
                    .having(Expressions.equals(Enterprise.ENTERPRISE_ID, iri(iri)));
        }
        return selectQuery;
    }

    public static void mapEnterprise(BindingSet querySolution, Enterprise enterprise) {
        enterprise.setId(QueryResultUtils.getIRI(querySolution, Enterprise.ENTERPRISE_ID));
        enterprise.setSourceId(
                QueryResultUtils.getStringMaybe(querySolution, Enterprise.ENTERPRISE_SOURCE_ID));
        var label = QueryResultUtils.getValueMaybe(querySolution, Enterprise.ENTERPRISE_LABEL);
        if (label != null) {
            enterprise.setLabel(label.stringValue());
            if (((Literal) label).getLanguage().isPresent()) {
                enterprise.setLabelLanguageCode(((Literal) label).getLanguage().get());
            }
        }
        var description =
                QueryResultUtils.getValueMaybe(querySolution, Enterprise.ENTERPRISE_DESCRIPTION);
        if (description != null) {
            enterprise.setDescription(description.stringValue());
            if (((Literal) description).getLanguage().isPresent()) {
                enterprise.setDescriptionLanguageCode(((Literal) description).getLanguage().get());
            }
        }
        var logo = QueryResultUtils.getValueMaybe(querySolution, Enterprise.ENTERPRISE_LOGO);
        if (logo != null) {
            enterprise.setLogo(logo.stringValue());
        }
    }

    @Override
    protected NamedSparqlSupplier getUpdateSparql(Enterprise enterprise) {
        return super.getUpdateSparql(enterprise);
    }

    @Override
    protected void deleteForUpdate(IRI iri) {
        super.deleteForUpdate(iri);
    }

    @Override
    protected NamedSparqlSupplier getInsertSparql(Enterprise enterprise) {
        return NamedSparqlSupplier.of(
                "insert",
                () ->
                        Queries.INSERT(
                                        Enterprise.ENTERPRISE_ID
                                                .isA(iri(AMS.Enterprise))
                                                .andHas(
                                                        iri(AMS.externalIdentifier),
                                                        Enterprise.ENTERPRISE_SOURCE_ID)
                                                .andHas(
                                                        iri(RDFS.LABEL),
                                                        Enterprise.ENTERPRISE_LABEL)
                                                .andHas(
                                                        iri(RDFS.COMMENT),
                                                        Enterprise.ENTERPRISE_DESCRIPTION)
                                                .andHas(
                                                        iri(AMS.has),
                                                        Enterprise.ENTERPRISE_LOCATION),
                                        Enterprise.ENTERPRISE_LOCATION
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
    protected void populateBindingsForUpdate(
            MutableBindings bindingsBuilder, Enterprise enterprise) {
        populateEnterpriseBindingsForUpdate(bindingsBuilder, enterprise);
    }

    public static void populateEnterpriseBindingsForUpdate(
            MutableBindings bindingsBuilder, Enterprise enterprise) {
        bindingsBuilder.addMaybe(Enterprise.ENTERPRISE_SOURCE_ID, enterprise.getSourceId());
        bindingsBuilder.addMaybe(Enterprise.ENTERPRISE_LABEL, enterprise.getLabel());
        bindingsBuilder.addMaybe(Enterprise.ENTERPRISE_DESCRIPTION, enterprise.getDescription());
        if (enterprise.getLocation() != null) {
            bindingsBuilder.add(Enterprise.ENTERPRISE_LOCATION, enterprise.getLocation().getId());
            bindingsBuilder.addMaybe(Location.LOCATION_ID, enterprise.getLocation().getId());
            bindingsBuilder.addMaybe(
                    Location.LOCATION_LATITUDE, enterprise.getLocation().getLatitude());
            bindingsBuilder.addMaybe(
                    Location.LOCATION_LONGITUDE, enterprise.getLocation().getLongitude());
            bindingsBuilder.addMaybe(
                    Location.LOCATION_STREET, enterprise.getLocation().getStreet());
            bindingsBuilder.addMaybe(
                    Location.LOCATION_STREET_NUMBER, enterprise.getLocation().getStreetNumber());
            bindingsBuilder.addMaybe(Location.LOCATION_ZIP, enterprise.getLocation().getZip());
            bindingsBuilder.addMaybe(Location.LOCATION_CITY, enterprise.getLocation().getCity());
            bindingsBuilder.addMaybe(
                    Location.LOCATION_COUNTRY, enterprise.getLocation().getCountry());
        }
        enterprise
                .getFactories()
                .forEach(
                        factory ->
                                bindingsBuilder.add(
                                        Enterprise.ENTERPRISE_FACTORIES, factory.getId()));
        enterprise
                .getSubsidiaryEnterprises()
                .forEach(
                        sub ->
                                bindingsBuilder.add(
                                        Enterprise.ENTERPRISE_SUBSIDIARY_ENTERPRISES, sub.getId()));
        enterprise
                .getProperties()
                .forEach(
                        property ->
                                bindingsBuilder.add(
                                        Enterprise.ENTERPRISE_PROPERTIES, property.getId()));
        enterprise
                .getProducts()
                .forEach(
                        product ->
                                bindingsBuilder.add(
                                        Enterprise.ENTERPRISE_PRODUCTS, product.getId()));
        enterprise
                .getProcesses()
                .forEach(
                        process ->
                                bindingsBuilder.add(
                                        Enterprise.ENTERPRISE_PROCESSES, process.getId()));
        enterprise
                .getProductionResources()
                .forEach(
                        pr ->
                                bindingsBuilder.add(
                                        Enterprise.ENTERPRISE_PRODUCTION_RESOURCES, pr.getId()));
        enterprise
                .getSupplyChains()
                .forEach(
                        sc -> bindingsBuilder.add(Enterprise.ENTERPRISE_SUPPLY_CHAINS, sc.getId()));
    }

    @Override
    protected IRI getInputId(Enterprise enterprise) {
        if (enterprise.getId() == null) {
            return getRdf4JTemplate().getNewUUID();
            // TODO duplicate uuid?
        }
        return enterprise.getId();
    }

    @Override
    protected RDF4JTemplate getRdf4JTemplate() {
        return super.getRdf4JTemplate();
    }
}
