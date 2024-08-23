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
package de.fraunhofer.iosb.ilt.ams.model;

import static de.fraunhofer.iosb.ilt.ams.dao.CapabilityDAO.getCapabilitySelectQuery;
import static de.fraunhofer.iosb.ilt.ams.dao.CapabilityDAO.mapCapability;
import static de.fraunhofer.iosb.ilt.ams.dao.EnterpriseDAO.getEnterpriseSelectQuery;
import static de.fraunhofer.iosb.ilt.ams.dao.EnterpriseDAO.mapEnterprise;
import static de.fraunhofer.iosb.ilt.ams.dao.FactoryDAO.*;
import static de.fraunhofer.iosb.ilt.ams.dao.LocationDAO.getLocationSelectQuery;
import static de.fraunhofer.iosb.ilt.ams.dao.LocationDAO.mapLocation;
import static de.fraunhofer.iosb.ilt.ams.dao.ProcessDAO.getProcessSelectQuery;
import static de.fraunhofer.iosb.ilt.ams.dao.ProcessDAO.mapProcess;
import static de.fraunhofer.iosb.ilt.ams.dao.ProductApplicationDAO.getProductApplicationSelectQuery;
import static de.fraunhofer.iosb.ilt.ams.dao.ProductApplicationDAO.mapProductApplication;
import static de.fraunhofer.iosb.ilt.ams.dao.ProductClassDAO.*;
import static de.fraunhofer.iosb.ilt.ams.dao.ProductDAO.*;
import static de.fraunhofer.iosb.ilt.ams.dao.ProductPassportDAO.getProductPassportSelectQuery;
import static de.fraunhofer.iosb.ilt.ams.dao.ProductPassportDAO.mapProductPassport;
import static de.fraunhofer.iosb.ilt.ams.dao.ProductionResourceDAO.*;
import static de.fraunhofer.iosb.ilt.ams.dao.PropertyDAO.getPropertySelectQuery;
import static de.fraunhofer.iosb.ilt.ams.dao.PropertyDAO.mapProperty;
import static de.fraunhofer.iosb.ilt.ams.dao.SemanticReferenceDAO.getSemanticReferenceSelectQuery;
import static de.fraunhofer.iosb.ilt.ams.dao.SemanticReferenceDAO.mapSemanticReference;
import static de.fraunhofer.iosb.ilt.ams.dao.SupplyChainDAO.getSupplyChainSelectQuery;
import static de.fraunhofer.iosb.ilt.ams.dao.SupplyChainDAO.mapSupplyChain;
import static de.fraunhofer.iosb.ilt.ams.dao.SupplyChainElementDAO.*;
import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri;

import de.fraunhofer.iosb.ilt.ams.AMS;
import de.fraunhofer.iosb.ilt.ams.api.datafetcher.DatafetcherSecurity;
import de.fraunhofer.iosb.ilt.ams.dao.FactoryDAO;
import de.fraunhofer.iosb.ilt.ams.model.input.*;
import java.util.*;
import javax.annotation.PostConstruct;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.sparqlbuilder.core.Dataset;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.TriplesTemplate;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.eclipse.rdf4j.spring.util.QueryResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

@org.springframework.stereotype.Repository
public class ObjectRdf4jRepository {

    public static final String DOMAIN_NAME = "https://www.smartfactoryweb.de/graph/";
    public static final String GRAPH_NAME = "<" + DOMAIN_NAME + ">";
    public static long counter = 0L;
    Map<String, String> env = System.getenv();

    @Autowired DatafetcherSecurity security;

    @Autowired RDF4JTemplate rdf4JTemplate;

    @Value("${rdf4j.spring.repository.remote.manager-url}")
    String rdf4jServer;

    @Value("${rdf4j.spring.repository.remote.name}")
    String repositoryID;

    Repository repo;

    Repository repository;

    private Set<IRI> processedIds = new HashSet<>();

    @PostConstruct
    public void init() {
        repo = new HTTPRepository(rdf4jServer, repositoryID);

        repository =
                new SPARQLRepository(
                        String.format("%s/repositories/%s/statements", rdf4jServer, repositoryID));
    }

    public void emptyProcessedIds() {
        this.processedIds = new HashSet<>();
    }

    public void addProcessedId(IRI id) {
        this.processedIds.add(id);
    }

    public Factory getFactoryByIri(IRI iri) {
        // &&7SelectQuery
        processedIds.add(iri);
        try (RepositoryConnection connection = repo.getConnection()) {
            String selectQuery =
                    FactoryDAO.getFactorySelectPattern(iri.stringValue())
                            .from(this.getGraphNameForQuery())
                            .getQueryString();
            TupleQuery tupleQuery = connection.prepareTupleQuery(selectQuery);
            try (TupleQueryResult tupleQueryResult = tupleQuery.evaluate()) {
                Factory factory = null;
                while (tupleQueryResult.hasNext()) {
                    BindingSet bindings = tupleQueryResult.next();
                    if (factory == null) {
                        factory = new Factory();
                        mapFactory(bindings, factory);
                        var location =
                                QueryResultUtils.getIRIMaybe(bindings, Factory.FACTORY_LOCATION);
                        if (location != null) {
                            factory.setLocation(this.getLocationById(location));
                        }
                    }
                    var propertyId =
                            QueryResultUtils.getIRIMaybe(bindings, Factory.FACTORY_PROPERTIES);
                    if (propertyId != null) {
                        factory.addProperty(this.getPropertyById(propertyId));
                    }
                    var machineId =
                            QueryResultUtils.getIRIMaybe(bindings, Factory.FACTORY_MACHINES);
                    if (machineId != null /*&& !processedIds.contains(machineId)*/) {
                        factory.addMachine(getMachineById(machineId));
                    } /*else if (machineId != null) {
                          Machine machine = new Machine();
                          machine.setId(machineId);
                          factory.addMachine(machine);
                      }*/
                    var humanResourceId =
                            QueryResultUtils.getIRIMaybe(bindings, Factory.FACTORY_HUMAN_RESOURCES);
                    if (humanResourceId != null && !processedIds.contains(humanResourceId)) {
                        factory.addHumanResource(this.getHumanResourceById(humanResourceId));
                    } else if (humanResourceId != null) {
                        HumanResource humanResource = new HumanResource();
                        humanResource.setId(humanResourceId);
                        factory.addHumanResource(humanResource);
                    }
                    var enterpriseId =
                            QueryResultUtils.getIRIMaybe(bindings, Factory.FACTORY_ENTERPRISE);
                    if (enterpriseId != null
                            && factory.getEnterprise() == null
                            && !processedIds.contains(enterpriseId)) {
                        factory.setEnterprise(this.getEnterpriseById(enterpriseId));
                    } else if (enterpriseId != null && factory.getEnterprise() == null) {
                        Enterprise enterprise = new Enterprise();
                        enterprise.setId(enterpriseId);
                        factory.setEnterprise(enterprise);
                    }
                    var processId =
                            QueryResultUtils.getIRIMaybe(bindings, Factory.FACTORY_PROCESSES);
                    if (processId != null && !processedIds.contains(processId)) {
                        factory.addProcess(this.getProcessById(processId));
                    } else if (processId != null) {
                        Process process = new Process();
                        process.setId(processId);
                        factory.addProcess(process);
                    }
                    var productId =
                            QueryResultUtils.getIRIMaybe(bindings, Factory.FACTORY_PRODUCTS);
                    if (productId != null && !processedIds.contains(productId)) {
                        factory.addProduct(this.getProductById(productId));
                    } else if (productId != null) {
                        Product product = new Product();
                        product.setId(productId);
                        factory.addProduct(product);
                    }
                    var certificate =
                            QueryResultUtils.getIRIMaybe(bindings, Factory.FACTORY_CERTIFICATES);
                    if (certificate != null) {
                        factory.addCertificate(this.getPropertyById(certificate));
                    }
                }
                return factory;
            }
        }
    }

    public Property getPropertyById(IRI iri) {
        try (RepositoryConnection connection = repo.getConnection()) {
            String selectQuery =
                    getPropertySelectQuery(iri.stringValue())
                            .from(getGraphNameForQuery())
                            .getQueryString();
            TupleQuery tupleQuery = connection.prepareTupleQuery(selectQuery);
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                Property property = null;
                while (result.hasNext()) {
                    var bindings = result.next();
                    if (property == null) {
                        property = new Property();
                        mapProperty(bindings, property);
                    }
                    var semanticReference =
                            QueryResultUtils.getIRIMaybe(
                                    bindings, Property.PROPERTY_SEMANTIC_REFERENCES);
                    if (semanticReference != null) {
                        property.addSemanticReference(
                                this.getSemanticReferenceById(semanticReference));
                    }
                }
                return property;
            }
        }
    }

    public ProductApplication getProductApplicationById(IRI iri) {
        this.addProcessedId(iri);
        try (RepositoryConnection connection = repo.getConnection()) {
            String selectQuery =
                    getProductApplicationSelectQuery(iri.stringValue())
                            .from(getGraphNameForQuery())
                            .getQueryString();
            TupleQuery tupleQuery = connection.prepareTupleQuery(selectQuery);
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                ProductApplication productApplication = null;
                if (!result.hasNext()) {
                    productApplication = new ProductApplication();
                    productApplication.setId(Values.iri("urn:uuid:placeholderuuidXXX" + counter++));
                    if (counter + 1 == Long.MAX_VALUE) {
                        counter = 0;
                    }
                    productApplication.setProduct(this.getProductById(iri));
                    return productApplication;
                }
                while (result.hasNext()) {
                    var querySolution = result.next();
                    if (productApplication == null) {
                        productApplication = new ProductApplication();
                        mapProductApplication(querySolution, productApplication);
                    }
                    var productIRI =
                            QueryResultUtils.getIRIMaybe(
                                    querySolution, ProductApplication.PRODUCT_APP_PRODUCT);
                    if (productIRI != null
                            && productApplication.getProduct() == null
                            && !processedIds.contains(productIRI)) {
                        productApplication.setProduct(this.getProductById(productIRI));
                    } else if (productIRI != null && productApplication.getProduct() == null) {
                        Product product = new Product();
                        product.setId(productIRI);
                        productApplication.setProduct(product);
                    }

                    var quantity =
                            QueryResultUtils.getIRIMaybe(
                                    querySolution, ProductApplication.PRODUCT_APP_QUANTITY);
                    if (quantity != null && productApplication.getQuantity() == null) {
                        productApplication.setQuantity(this.getPropertyById(quantity));
                    }

                    var property =
                            QueryResultUtils.getIRIMaybe(
                                    querySolution, ProductApplication.PRODUCT_APP_PROPERTY);
                    if (property != null) {
                        productApplication.addProperty(this.getPropertyById(property));
                    }
                }
                return productApplication;
            }
        }
    }

    public ProductionResource getProductionResourceById(IRI iri) {
        ProductionResource productionResource = getHumanResourceById(iri);
        if (productionResource == null) {
            productionResource = getMachineById(iri);
        }
        return productionResource;
    }

    private void populateProductionResource(
            BindingSet bindingSet, ProductionResource productionResource) {
        var providedProcess =
                QueryResultUtils.getIRIMaybe(
                        bindingSet, ProductionResource.PRODUCTION_RESOURCE_PROVIDING_PROCESS);
        if (providedProcess != null && !processedIds.contains(providedProcess)) {
            productionResource.addProvidedProcess(this.getProcessById(providedProcess));
        } else if (providedProcess != null) {
            Process process = new Process();
            process.setId(providedProcess);
            productionResource.addProvidedProcess(process);
        }
        var usingProcess =
                QueryResultUtils.getIRIMaybe(
                        bindingSet, ProductionResource.PRODUCTION_RESOURCE_USING_PROCESS);
        if (usingProcess != null && !processedIds.contains(usingProcess)) {
            productionResource.addUsingProcess(this.getProcessById(usingProcess));
        } else if (usingProcess != null) {
            Process using = new Process();
            using.setId(usingProcess);
            productionResource.addUsingProcess(using);
        }
        var capability =
                QueryResultUtils.getIRIMaybe(
                        bindingSet, ProductionResource.PRODUCTION_RESOURCE_PROVIDED_CAPABILITY);
        if (capability != null && !processedIds.contains(capability)) {
            productionResource.addProvidedCapability(this.getCapabilityById(capability));
        } else if (capability != null) {
            Capability capabilityObject = new Capability();
            capabilityObject.setId(capability);
            productionResource.addProvidedCapability(capabilityObject);
        }
    }

    public Machine getMachineById(IRI iri) {
        processedIds.add(iri);
        try (RepositoryConnection connection = repo.getConnection()) {
            String selectQuery =
                    getMachineSelectQuery(iri.stringValue())
                            .from(getGraphNameForQuery())
                            .getQueryString();
            TupleQuery tupleQuery = connection.prepareTupleQuery(selectQuery);
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                Machine machine = null;
                while (result.hasNext()) {
                    var bindingSet = result.next();
                    if (machine == null) {
                        machine = new Machine();
                        mapProductionResource(bindingSet, machine);
                    }
                    populateProductionResource(bindingSet, machine);
                    var machineProperty =
                            QueryResultUtils.getIRIMaybe(bindingSet, Machine.MACHINE_PROPERTY);
                    if (machineProperty != null) {
                        machine.addMachineProperty(this.getPropertyById(machineProperty));
                    }
                }
                return machine;
            }
        }
    }

    public HumanResource getHumanResourceById(IRI iri) {
        processedIds.add(iri);
        try (RepositoryConnection connection = repo.getConnection()) {
            String selectQuery =
                    getHumanResourceSelectQuery(iri.stringValue())
                            .from(getGraphNameForQuery())
                            .getQueryString();
            TupleQuery tupleQuery = connection.prepareTupleQuery(selectQuery);
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                HumanResource humanResource = null;
                while (result.hasNext()) {
                    var bindingSet = result.next();
                    if (humanResource == null) {
                        humanResource = new HumanResource();
                        mapProductionResource(bindingSet, humanResource);
                    }
                    populateProductionResource(bindingSet, humanResource);
                    var certificate =
                            QueryResultUtils.getIRIMaybe(
                                    bindingSet, HumanResource.HUMAN_RESOURCE_CERTIFICATES);
                    if (certificate != null) {
                        humanResource.addCertificate(this.getPropertyById(certificate));
                    }
                    var property =
                            QueryResultUtils.getIRIMaybe(
                                    bindingSet, HumanResource.HUMAN_RESOURCE_PROPERTIES);
                    if (property != null) {
                        humanResource.addProperty(this.getPropertyById(property));
                    }
                }
                return humanResource;
            }
        }
    }

    public Enterprise getEnterpriseById(IRI iri) {
        this.processedIds.add(iri);
        try (RepositoryConnection connection = repo.getConnection()) {
            String selectQuery =
                    getEnterpriseSelectQuery(iri.stringValue())
                            .from(getGraphNameForQuery())
                            .getQueryString();
            TupleQuery tupleQuery = connection.prepareTupleQuery(selectQuery);
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                Enterprise enterprise = null;
                while (result.hasNext()) {
                    var bindingSet = result.next();
                    if (enterprise == null) {
                        enterprise = new Enterprise();
                        mapEnterprise(bindingSet, enterprise);
                        var location =
                                QueryResultUtils.getIRIMaybe(
                                        bindingSet, Enterprise.ENTERPRISE_LOCATION);
                        if (location != null) {
                            enterprise.setLocation(this.getLocationById(location));
                        }
                    }
                    var factory =
                            QueryResultUtils.getIRIMaybe(
                                    bindingSet, Enterprise.ENTERPRISE_FACTORIES);
                    if (factory != null /*&&!processedIds.contains(factory)*/) {
                        enterprise.addFactory(this.getFactoryByIri(factory));
                    } /*else if (factory != null) {
                          Factory factoryObject = new Factory();
                          factoryObject.setId(factory);
                          enterprise.addFactory(factoryObject);
                      }*/
                    var subsidiaryEnterprise =
                            QueryResultUtils.getIRIMaybe(
                                    bindingSet, Enterprise.ENTERPRISE_SUBSIDIARY_ENTERPRISES);
                    if (subsidiaryEnterprise != null) {
                        enterprise.addSubsidiaryEnterprise(
                                this.getEnterpriseById(subsidiaryEnterprise));
                    }
                    var property =
                            QueryResultUtils.getIRIMaybe(
                                    bindingSet, Enterprise.ENTERPRISE_PROPERTIES);
                    if (property != null) {
                        enterprise.addProperty(this.getPropertyById(property));
                    }
                    var productIRI =
                            QueryResultUtils.getIRIMaybe(
                                    bindingSet, Enterprise.ENTERPRISE_PRODUCTS);
                    if (productIRI != null && !processedIds.contains(productIRI)) {
                        enterprise.addProduct(this.getProductById(productIRI));
                    } else if (productIRI != null) {
                        Product product = new Product();
                        product.setId(productIRI);
                        enterprise.addProduct(product);
                    }
                    var process =
                            QueryResultUtils.getIRIMaybe(
                                    bindingSet, Enterprise.ENTERPRISE_PROCESSES);
                    if (process != null && !processedIds.contains(process)) {
                        enterprise.addProcess(this.getProcessById(process));
                    } else if (process != null) {
                        Process processObject = new Process();
                        processObject.setId(process);
                        enterprise.addProcess(processObject);
                    }
                    var productionResource =
                            QueryResultUtils.getIRIMaybe(
                                    bindingSet, Enterprise.ENTERPRISE_PRODUCTION_RESOURCES);
                    if (productionResource != null && !processedIds.contains(productionResource)) {
                        enterprise.addProductionResource(
                                this.getProductionResourceById(productionResource));
                    }
                    var supplyChain =
                            QueryResultUtils.getIRIMaybe(
                                    bindingSet, Enterprise.ENTERPRISE_SUPPLY_CHAINS);
                    if (supplyChain != null) {
                        enterprise.addSupplyChain(this.getSupplyChainById(supplyChain));
                    }
                    var certificate =
                            QueryResultUtils.getIRIMaybe(
                                    bindingSet, Enterprise.ENTERPRISE_CERTIFICATES);
                    if (certificate != null) {
                        enterprise.addCertificate(this.getPropertyById(certificate));
                    }
                }
                return enterprise;
            }
        }
    }

    public Process getProcessById(IRI iri) {
        processedIds.add(iri);
        try (RepositoryConnection connection = repo.getConnection()) {
            String selectQuery =
                    getProcessSelectQuery(iri.stringValue())
                            .from(getGraphNameForQuery())
                            .getQueryString();
            TupleQuery tupleQuery = connection.prepareTupleQuery(selectQuery);
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                Process process = null;
                while (result.hasNext()) {
                    BindingSet bindings = result.next();
                    if (process == null) {
                        process = new Process();
                        mapProcess(bindings, process);
                    }
                    var property = QueryResultUtils.getIRIMaybe(bindings, Process.PROCESS_PROPERTY);
                    if (property != null) {
                        process.addProperty(this.getPropertyById(property));
                    }
                    var parent = QueryResultUtils.getIRIMaybe(bindings, Process.PROCESS_PARENT);
                    if (parent != null && !processedIds.contains(parent)) {
                        process.addParentProcess(this.getProcessById(parent));
                    } else if (parent != null) {
                        Process parentProcess = new Process();
                        parentProcess.setId(parent);
                        process.addParentProcess(process);
                    }
                    var child = QueryResultUtils.getIRIMaybe(bindings, Process.PROCESS_CHILD);
                    if (child != null && !processedIds.contains(child)) {
                        process.addChildProcess(this.getProcessById(child));
                    } else if (child != null) {
                        Process childProcess = new Process();
                        childProcess.setId(child);
                        process.addChildProcess(process);
                    }
                    var realizedCapabilities =
                            QueryResultUtils.getIRIMaybe(bindings, Process.PROCESS_CAPABILITY);
                    if (realizedCapabilities != null
                            && !processedIds.contains(realizedCapabilities)) {
                        processedIds.add(realizedCapabilities);
                        process.addRealizedCapability(this.getCapabilityById(realizedCapabilities));
                    } else if (realizedCapabilities != null) {
                        Capability realizedCapability = new Capability();
                        realizedCapability.setId(realizedCapabilities);
                        process.addRealizedCapability(realizedCapability);
                    }
                    var requiredCapability =
                            QueryResultUtils.getIRIMaybe(bindings, Process.PROCESS_REQ_CAPABILITY);
                    if (requiredCapability != null && !processedIds.contains(requiredCapability)) {
                        processedIds.add(requiredCapability);
                        process.addRequiredCapability(this.getCapabilityById(requiredCapability));
                    } else if (requiredCapability != null) {
                        Capability required = new Capability();
                        required.setId(requiredCapability);
                        process.addRequiredCapability(required);
                    }
                    var preliminaryIRI =
                            QueryResultUtils.getIRIMaybe(
                                    bindings, Process.PROCESS_PRELIMINARY_PRODUCT);
                    if (preliminaryIRI != null && !processedIds.contains(preliminaryIRI)) {
                        ProductApplication productApplication =
                                this.getProductApplicationById(preliminaryIRI);
                        process.addPreliminaryProduct(productApplication);
                        process.getInputProducts().add(productApplication);
                    } else if (preliminaryIRI != null) {
                        ProductApplication productApplication = new ProductApplication();
                        productApplication.setId(preliminaryIRI);
                        process.addPreliminaryProduct(productApplication);
                        process.getInputProducts().add(productApplication);
                    }
                    var rawIRI =
                            QueryResultUtils.getIRIMaybe(bindings, Process.PROCESS_RAW_MATERIAL);
                    if (rawIRI != null && !processedIds.contains(rawIRI)) {
                        ProductApplication productApplication =
                                this.getProductApplicationById(rawIRI);
                        process.addRawMaterial(productApplication);
                        process.getInputProducts().add(productApplication);
                    } else if (rawIRI != null) {
                        ProductApplication raw = new ProductApplication();
                        raw.setId(rawIRI);
                        process.addRawMaterial(raw);
                        process.getInputProducts().add(raw);
                    }
                    var auxiliaryIRI =
                            QueryResultUtils.getIRIMaybe(
                                    bindings, Process.PROCESS_AUXILIARY_MATERIAL);
                    if (auxiliaryIRI != null && !processedIds.contains(auxiliaryIRI)) {
                        ProductApplication auxiliary = this.getProductApplicationById(auxiliaryIRI);
                        process.addAuxiliaryMaterial(auxiliary);
                        process.getInputProducts().add(auxiliary);
                    } else if (auxiliaryIRI != null) {
                        ProductApplication auxiliary = new ProductApplication();
                        auxiliary.setId(auxiliaryIRI);
                        process.addAuxiliaryMaterial(auxiliary);
                        process.getInputProducts().add(auxiliary);
                    }
                    var operating =
                            QueryResultUtils.getIRIMaybe(
                                    bindings, Process.PROCESS_OPERATING_MATERIAL);
                    if (operating != null && !processedIds.contains(operating)) {
                        ProductApplication operatingMaterial =
                                this.getProductApplicationById(operating);
                        process.addOperatingMaterial(operatingMaterial);
                        process.getInputProducts().add(operatingMaterial);
                    } else if (operating != null) {
                        ProductApplication operatingMaterial = new ProductApplication();
                        operatingMaterial.setId(operating);
                        process.addOperatingMaterial(operatingMaterial);
                        process.getInputProducts().add(operatingMaterial);
                    }
                    var end = QueryResultUtils.getIRIMaybe(bindings, Process.PROCESS_END_PRODUCT);
                    if (end != null && !processedIds.contains(end)) {
                        ProductApplication endProduct = this.getProductApplicationById(end);
                        process.addEndProduct(endProduct);
                        process.getOutputProducts().add(endProduct);
                    } else if (end != null) {
                        ProductApplication endProduct = new ProductApplication();
                        endProduct.setId(end);
                        process.addEndProduct(endProduct);
                        process.getOutputProducts().add(endProduct);
                    }
                    var by = QueryResultUtils.getIRIMaybe(bindings, Process.PROCESS_BY_PRODUCT);
                    if (by != null && !processedIds.contains(by)) {
                        ProductApplication byProduct = this.getProductApplicationById(by);
                        process.addByProduct(byProduct);
                        process.getOutputProducts().add(byProduct);
                    } else if (by != null) {
                        ProductApplication byProduct = new ProductApplication();
                        byProduct.setId(by);
                        process.addByProduct(byProduct);
                        process.getOutputProducts().add(byProduct);
                    }
                    var waste =
                            QueryResultUtils.getIRIMaybe(bindings, Process.PROCESS_WASTE_PRODUCT);
                    if (waste != null && !processedIds.contains(waste)) {
                        ProductApplication wasteProduct = this.getProductApplicationById(waste);
                        process.addWasteProduct(wasteProduct);
                        process.getOutputProducts().add(wasteProduct);
                    } else if (waste != null) {
                        ProductApplication wasteProduct = new ProductApplication();
                        wasteProduct.setId(waste);
                        process.addWasteProduct(wasteProduct);
                        process.getOutputProducts().add(wasteProduct);
                    }
                    // These two might be bugged, I can't determine right now if endless loop is
                    // broken here.
                    var usedProductionResource =
                            QueryResultUtils.getIRIMaybe(bindings, Process.PROCESS_USED);
                    if (usedProductionResource != null) {
                        process.addUsedProductionResource(
                                this.getProductionResourceById(usedProductionResource));
                    }
                    var providingProductionResource =
                            QueryResultUtils.getIRIMaybe(bindings, Process.PROCESS_PROVIDING);
                    if (providingProductionResource != null) {
                        process.addProvidingProductionResource(
                                this.getProductionResourceById(providingProductionResource));
                    }
                }
                return process;
            }
        }
    }

    public Product getProductById(IRI iri) {
        this.addProcessedId(iri);
        try (RepositoryConnection connection = repo.getConnection()) {
            String selectQuery =
                    getProductSelectQuery(iri.stringValue())
                            .from(getGraphNameForQuery())
                            .getQueryString();
            TupleQuery tupleQuery = connection.prepareTupleQuery(selectQuery);
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                Product product = null;
                while (result.hasNext()) {
                    BindingSet bindings = result.next();
                    if (product == null) {
                        product = new Product();
                        mapProduct(bindings, product);
                    }
                    var billOfMaterialIRI =
                            QueryResultUtils.getIRIMaybe(bindings, Product.PRODUCT_BOM);
                    if (billOfMaterialIRI != null && !processedIds.contains(billOfMaterialIRI)) {
                        product.addBillOfMaterial(
                                this.getProductApplicationById(billOfMaterialIRI));
                    } else if (billOfMaterialIRI != null) {
                        ProductApplication productApplication = new ProductApplication();
                        productApplication.setId(billOfMaterialIRI);
                        product.addBillOfMaterial(productApplication);
                    }
                    var property =
                            QueryResultUtils.getIRIMaybe(bindings, Product.PRODUCT_PROPERTIES);
                    if (property != null) {
                        product.addProperty(this.getPropertyById(property));
                    }
                    var productClassIRI =
                            QueryResultUtils.getIRIMaybe(bindings, Product.PRODUCT_PRODUCT_CLASSES);
                    if (productClassIRI != null && !processedIds.contains(productClassIRI)) {
                        product.addProductClass(this.getProductClassById(productClassIRI));
                    } else if (productClassIRI != null) {
                        ProductClass productClass = new ProductClass();
                        productClass.setId(productClassIRI);
                        product.addProductClass(productClass);
                    }
                    var semanticReference =
                            QueryResultUtils.getIRIMaybe(
                                    bindings, Product.PRODUCT_SEMANTIC_REFERENCES);
                    if (semanticReference != null) {
                        product.addSemanticReference(
                                this.getSemanticReferenceById(semanticReference));
                    }
                    var factory = QueryResultUtils.getIRIMaybe(bindings, Product.PRODUCT_FACTORIES);
                    if (factory != null && !processedIds.contains(factory)) {
                        product.addFactory(this.getFactoryByIri(factory));
                    } else if (factory != null) {
                        Factory factoryObject = new Factory();
                        factoryObject.setId(factory);
                        product.addFactory(factoryObject);
                    }
                    var enterprise =
                            QueryResultUtils.getIRIMaybe(bindings, Product.PRODUCT_ENTERPRISES);
                    if (enterprise != null && !processedIds.contains(enterprise)) {
                        product.addEnterprise(this.getEnterpriseById(enterprise));
                    } else if (enterprise != null) {
                        Enterprise enterpriseObject = new Enterprise();
                        enterpriseObject.setId(enterprise);
                        product.addEnterprise(enterpriseObject);
                    }
                    var supplyChain =
                            QueryResultUtils.getIRIMaybe(bindings, Product.PRODUCT_SUPPLY_CHAINS);
                    if (supplyChain != null) {
                        product.setSupplyChains(this.getSupplyChainById(supplyChain));
                    }
                    var productPassport =
                            QueryResultUtils.getIRIMaybe(
                                    bindings, Product.PRODUCT_PRODUCT_PASSPORT);
                    if (productPassport != null) {
                        product.setProductPassport(this.getProductPassportById(productPassport));
                    }
                }
                return product;
            }
        }
    }

    public SemanticReference getSemanticReferenceById(IRI iri) {
        try (RepositoryConnection connection = repo.getConnection()) {
            String selectQuery =
                    getSemanticReferenceSelectQuery(iri.stringValue())
                            .from(getGraphNameForQuery())
                            .getQueryString();
            TupleQuery tupleQuery = connection.prepareTupleQuery(selectQuery);
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                boolean created = false;
                SemanticReference semanticReference = null;
                while (result.hasNext()) {
                    BindingSet bindings = result.next();
                    if (!created) {
                        semanticReference = new SemanticReference();
                        mapSemanticReference(bindings, semanticReference);
                        created = true;
                    }
                }
                return semanticReference;
            }
        }
    }

    public ProductClass getProductClassById(IRI iri) {
        this.addProcessedId(iri);
        try (RepositoryConnection connection = repo.getConnection()) {
            String selectQuery =
                    getProductClassSelectQuery(iri.stringValue())
                            .from(getGraphNameForQuery())
                            .getQueryString();
            TupleQuery tupleQuery = connection.prepareTupleQuery(selectQuery);
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                ProductClass productClass = null;
                while (result.hasNext()) {
                    BindingSet bindings = result.next();
                    if (productClass == null) {
                        productClass = new ProductClass();
                        mapProductClass(bindings, productClass);
                    }
                    var semanticReference =
                            QueryResultUtils.getIRIMaybe(
                                    bindings, ProductClass.PRODUCT_CLASS_SEMANTIC_REFERENCES);
                    if (semanticReference != null) {
                        productClass.addSemanticReference(
                                this.getSemanticReferenceById(semanticReference));
                    }
                    var parentClass =
                            QueryResultUtils.getIRIMaybe(
                                    bindings, ProductClass.PRODUCT_CLASS_PARENT_CLASSES);
                    if (parentClass != null && !processedIds.contains(parentClass)) {
                        processedIds.add(parentClass);
                        productClass.addParentClass(this.getProductClassById(parentClass));
                    } else if (parentClass != null) {
                        ProductClass parent = new ProductClass();
                        parent.setId(parentClass);
                        productClass.addParentClass(parent);
                    }

                    var childClass =
                            QueryResultUtils.getIRIMaybe(
                                    bindings, ProductClass.PRODUCT_CLASS_CHILD_CLASSES);
                    if (childClass != null && !processedIds.contains(childClass)) {
                        processedIds.add(childClass);
                        productClass.addChildClass(this.getProductClassById(childClass));
                    } else if (childClass != null) {
                        ProductClass child = new ProductClass();
                        child.setId(childClass);
                        productClass.addChildClass(child);
                    }
                    var productIRI =
                            QueryResultUtils.getIRIMaybe(
                                    bindings, ProductClass.PRODUCT_CLASS_PRODUCTS);
                    if (productIRI != null && !processedIds.contains(productIRI)) {
                        productClass.addProduct(this.getProductById(productIRI));
                    } else if (productIRI != null) {
                        Product product = new Product();
                        product.setId(productIRI);
                        productClass.addProduct(product);
                    }
                }
                return productClass;
            }
        }
    }

    public Capability getCapabilityById(IRI iri) {
        this.processedIds.add(iri);
        try (RepositoryConnection connection = repo.getConnection()) {
            String selectQuery =
                    getCapabilitySelectQuery(iri.stringValue())
                            .from(getGraphNameForQuery())
                            .getQueryString();
            TupleQuery tupleQuery = connection.prepareTupleQuery(selectQuery);
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                Capability capability = null;
                while (result.hasNext()) {
                    BindingSet bindings = result.next();
                    if (capability == null) {
                        capability = new Capability();
                        mapCapability(bindings, capability);
                    }
                    var property =
                            QueryResultUtils.getIRIMaybe(bindings, Capability.CAPABILITY_PROPERTY);
                    if (property != null) {
                        capability.addProperty(this.getPropertyById(property));
                    }
                    var process =
                            QueryResultUtils.getIRIMaybe(bindings, Capability.CAPABILITY_PROCESS);
                    if (process != null && !processedIds.contains(process)) {
                        capability.addProcess(this.getProcessById(process));
                    } else if (process != null) {
                        Process processObject = new Process();
                        processObject.setId(process);
                        capability.addProcess(processObject);
                    }
                    var childCapability =
                            QueryResultUtils.getIRIMaybe(
                                    bindings, Capability.CAPABILITY_CHILD_CAPABILITY);
                    if (childCapability != null && !processedIds.contains(childCapability)) {
                        processedIds.add(childCapability);
                        capability.addChildCapability(this.getCapabilityById(childCapability));
                    } else if (childCapability != null) {
                        Capability child = new Capability();
                        child.setId(childCapability);
                        capability.addChildCapability(child);
                    }

                    var parentCapability =
                            QueryResultUtils.getIRIMaybe(
                                    bindings, Capability.CAPABILITY_PARENT_CAPABILITY);
                    if (parentCapability != null && !processedIds.contains(parentCapability)) {
                        processedIds.add(parentCapability);
                        capability.addParentCapability(this.getCapabilityById(parentCapability));
                    } else if (parentCapability != null) {
                        Capability parent = new Capability();
                        parent.setId(parentCapability);
                        capability.addParentCapability(parent);
                    }
                    var productionResource =
                            QueryResultUtils.getIRIMaybe(
                                    bindings, Capability.CAPABILITY_PRODUCTION_RESOURCE);
                    if (productionResource != null && !processedIds.contains(productionResource)) {
                        capability.addProductionResource(
                                this.getProductionResourceById(productionResource));
                    }
                    var semanticReference =
                            QueryResultUtils.getIRIMaybe(
                                    bindings, Capability.CAPABILITY_SEMANTIC_REFERENCE);
                    if (semanticReference != null) {
                        capability.addSemanticReference(
                                this.getSemanticReferenceById(semanticReference));
                    }
                }
                return capability;
            }
        }
    }

    public Location getLocationById(IRI iri) {
        try (RepositoryConnection connection = repo.getConnection()) {
            String selectQuery =
                    getLocationSelectQuery(iri.stringValue())
                            .from(getGraphNameForQuery())
                            .getQueryString();
            TupleQuery tupleQuery = connection.prepareTupleQuery(selectQuery);
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                Location location = null;
                while (result.hasNext()) {
                    BindingSet bindings = result.next();
                    location = new Location();
                    mapLocation(bindings, location);
                }
                return location;
            }
        }
    }

    public ProductPassport getProductPassportById(IRI iri) {
        try (RepositoryConnection connection = repo.getConnection()) {
            String selectQuery =
                    getProductPassportSelectQuery(iri.stringValue())
                            .from(getGraphNameForQuery())
                            .getQueryString();
            TupleQuery tupleQuery = connection.prepareTupleQuery(selectQuery);
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                ProductPassport productPassport = null;
                while (result.hasNext()) {
                    BindingSet bindings = result.next();
                    if (productPassport == null) {
                        productPassport = new ProductPassport();
                        mapProductPassport(bindings, productPassport);
                    }
                    var property =
                            QueryResultUtils.getIRIMaybe(
                                    bindings, ProductPassport.PRODUCT_PASSPORT_PROPERTIES);
                    if (property != null) {
                        productPassport.addProperty(this.getPropertyById(property));
                    }
                }
                return productPassport;
            }
        }
    }

    public SupplyChain getSupplyChainById(IRI iri) {
        try (RepositoryConnection connection = repo.getConnection()) {
            String selectQuery =
                    getSupplyChainSelectQuery(iri.stringValue())
                            .from(getGraphNameForQuery())
                            .getQueryString();
            TupleQuery tupleQuery = connection.prepareTupleQuery(selectQuery);
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                SupplyChain supplyChain = null;
                while (result.hasNext()) {
                    BindingSet bindings = result.next();
                    if (supplyChain == null) {
                        supplyChain = new SupplyChain();
                        mapSupplyChain(bindings, supplyChain);
                    }
                    var supplier =
                            QueryResultUtils.getIRIMaybe(
                                    bindings, SupplyChain.SUPPLY_CHAIN_SUPPLIER);
                    if (supplier != null) {
                        supplyChain.addSupplier(this.getSupplyChainElementById(supplier));
                    }
                }
                return supplyChain;
            }
        }
    }

    public SupplyChainElement getSupplyChainElementById(IRI iri) {
        try (RepositoryConnection connection = repo.getConnection()) {
            String selectQuery =
                    getSupplyChainElementSelectQuery(iri.stringValue())
                            .from(getGraphNameForQuery())
                            .getQueryString();
            TupleQuery tupleQuery = connection.prepareTupleQuery(selectQuery);
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                SupplyChainElement supplyChainElement = null;
                while (result.hasNext()) {
                    BindingSet bindings = result.next();
                    if (supplyChainElement == null) {
                        supplyChainElement = new SupplyChainElement();
                        mapSupplyChainElement(bindings, supplyChainElement);
                        var factory =
                                QueryResultUtils.getIRIMaybe(
                                        bindings, SupplyChainElement.SCE_FACTORY);
                        if (supplyChainElement.getFactory() == null && factory != null) {
                            supplyChainElement.setFactory(this.getFactoryByIri(factory));
                        }
                        var enterprise =
                                QueryResultUtils.getIRIMaybe(
                                        bindings, SupplyChainElement.SCE_ENTERPRISE);
                        if (supplyChainElement.getEnterprise() == null && enterprise != null) {
                            supplyChainElement.setEnterprise(this.getEnterpriseById(enterprise));
                        }
                    }
                    var productIRI =
                            QueryResultUtils.getIRIMaybe(bindings, SupplyChainElement.SCE_PRODUCT);
                    if (productIRI != null && !processedIds.contains(productIRI)) {
                        supplyChainElement.addProduct(this.getProductApplicationById(productIRI));
                    } else if (productIRI != null) {
                        ProductApplication productApplication = new ProductApplication();
                        productApplication.setId(productIRI);
                        supplyChainElement.addProduct(productApplication);
                    }
                    var supplier =
                            QueryResultUtils.getIRIMaybe(bindings, SupplyChainElement.SCE_SUPPLIER);
                    if (supplier != null) {
                        supplyChainElement.addSupplier(this.getSupplyChainElementById(supplier));
                    }
                }
                return supplyChainElement;
            }
        }
    }

    // CREATE BLOCK

    public Enterprise createEnterprise(EnterpriseInput enterpriseInput) {
        IRI enterpriseIRI;

        if (enterpriseInput.getId() != null) {
            return this.getEnterpriseById(Values.iri(enterpriseInput.getId()));
        } else {
            enterpriseIRI = this.getNewUUID();
        }

        TriplePattern insertPattern = GraphPatterns.tp(enterpriseIRI, RDF.TYPE, AMS.Enterprise);
        if (enterpriseInput.getLabel() != null && !enterpriseInput.getLabel().isBlank()) {
            insertPattern =
                    insertPattern.andHas(
                            iri(RDFS.LABEL),
                            Values.literal(
                                    enterpriseInput.getLabel(),
                                    enterpriseInput.getLabelLanguageCode()));
        }

        if (enterpriseInput.getDescription() != null
                && !enterpriseInput.getDescription().isBlank()) {
            insertPattern =
                    insertPattern.andHas(
                            iri(RDFS.COMMENT),
                            Values.literal(
                                    enterpriseInput.getDescription(),
                                    enterpriseInput.getDescriptionLanguageCode()));
        }

        if (enterpriseInput.getSourceId() != null) {
            insertPattern =
                    insertPattern.andHas(
                            iri(AMS.externalIdentifier), enterpriseInput.getSourceId());
        }

        if (enterpriseInput.getLogo() != null) {
            insertPattern = insertPattern.andHas(iri(AMS.logo), enterpriseInput.getLogo());
        }

        if (enterpriseInput.getFactories() != null) {
            for (FactoryInput factory : enterpriseInput.getFactories()) {
                IRI factoryIRI = this.createFactory(factory).getId();
                this.addFactoryToEnterprise(factoryIRI, enterpriseIRI);
            }
        }

        if (enterpriseInput.getSubsidiaryEnterprises() != null) {
            for (EnterpriseInput enterprise : enterpriseInput.getSubsidiaryEnterprises()) {
                IRI subEnterpriseIRI = this.createEnterprise(enterprise).getId();
                this.addSubsidiaryEnterpriseToEnterprise(subEnterpriseIRI, enterpriseIRI);
            }
        }

        if (enterpriseInput.getProperties() != null) {
            for (PropertyInput property : enterpriseInput.getProperties()) {
                IRI propertyIRI = this.createProperty(property).getId();
                this.addPropertyToEnterprise(propertyIRI, enterpriseIRI);
            }
        }

        if (enterpriseInput.getProducts() != null) {
            for (ProductInput product : enterpriseInput.getProducts()) {
                IRI productIRI = this.createProduct(product).getId();
                this.addProductToEnterprise(productIRI, enterpriseIRI);
            }
        }
        String insertQuery =
                Queries.INSERT_DATA(insertPattern)
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        Location location;
        String locationQuery = null;
        if (enterpriseInput.getLocation() != null) {
            location = this.createAndInsertLocation(enterpriseInput.getLocation());
            locationQuery =
                    Queries.INSERT_DATA(GraphPatterns.tp(enterpriseIRI, AMS.has, location.getId()))
                            .into(this::getGraphNameForMutation)
                            .getQueryString();
        }
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
            if (locationQuery != null) {
                connection.prepareUpdate(locationQuery).execute();
            }
            if (enterpriseInput.getCertificates() != null) {
                for (var cert : enterpriseInput.getCertificates()) {
                    var certificate = this.createProperty(cert);
                    String certificateInsertQuery =
                            Queries.INSERT_DATA(
                                            GraphPatterns.tp(
                                                    enterpriseIRI,
                                                    AMS.certificate,
                                                    certificate.getId()))
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(certificateInsertQuery).execute();
                }
            }
        }
        return this.getEnterpriseById(enterpriseIRI);
    }

    public Enterprise updateEnterprise(IRI enterpriseId, EnterpriseInput enterpriseInput) {
        Enterprise enterprise = this.getEnterpriseById(enterpriseId);
        try (RepositoryConnection connection = repository.getConnection()) {
            if (enterpriseInput.getSourceId() != null) {
                connection.remove(enterpriseId, AMS.externalIdentifier, null);
                TriplePattern insertPattern =
                        GraphPatterns.tp(
                                enterpriseId,
                                AMS.externalIdentifier,
                                Values.literal(enterpriseInput.getSourceId()));
                String insertQuery =
                        Queries.INSERT_DATA(insertPattern)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(insertQuery).execute();
            }
            if (enterpriseInput.getLabel() != null) {
                connection.remove(enterpriseId, RDFS.LABEL, null);
                if (enterpriseInput.getLabelLanguageCode() != null) {
                    TriplePattern insertPattern =
                            GraphPatterns.tp(
                                    enterpriseId,
                                    RDFS.LABEL,
                                    Values.literal(
                                            enterpriseInput.getLabel(),
                                            enterpriseInput.getLabelLanguageCode()));
                    String insertQuery =
                            Queries.INSERT_DATA(insertPattern)
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(insertQuery).execute();
                }
            } else if (enterpriseInput.getLabelLanguageCode() != null) {
                connection.remove(enterpriseId, RDFS.LABEL, null);
                TriplePattern insertPattern =
                        GraphPatterns.tp(
                                enterpriseId,
                                RDFS.LABEL,
                                Values.literal(
                                        enterprise.getLabel(),
                                        enterpriseInput.getLabelLanguageCode()));
                String insertQuery =
                        Queries.INSERT_DATA(insertPattern)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(insertQuery).execute();
            }
            if (enterpriseInput.getDescription() != null) {
                connection.remove(enterpriseId, RDFS.COMMENT, null);
                if (enterpriseInput.getDescriptionLanguageCode() != null) {
                    TriplePattern insertPattern =
                            GraphPatterns.tp(
                                    enterpriseId,
                                    RDFS.COMMENT,
                                    Values.literal(
                                            enterpriseInput.getDescription(),
                                            enterpriseInput.getDescriptionLanguageCode()));
                    String insertQuery =
                            Queries.INSERT_DATA(insertPattern)
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(insertQuery).execute();
                }
            }
            if (enterpriseInput.getDescription() == null
                    && enterpriseInput.getDescriptionLanguageCode() != null) {
                connection.remove(enterpriseId, RDFS.COMMENT, null);
                TriplePattern insertPattern =
                        GraphPatterns.tp(
                                enterpriseId,
                                RDFS.COMMENT,
                                Values.literal(
                                        enterprise.getDescription(),
                                        enterpriseInput.getDescriptionLanguageCode()));
                String insertQuery =
                        Queries.INSERT_DATA(insertPattern)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(insertQuery).execute();
            }

            if (enterpriseInput.getLocation() != null) {
                connection.remove(enterpriseId, AMS.has, enterprise.getLocation().getId());
                var location = this.createAndInsertLocation(enterpriseInput.getLocation());
                String locationQuery =
                        Queries.INSERT_DATA(
                                        GraphPatterns.tp(enterpriseId, AMS.has, location.getId()))
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(locationQuery).execute();
            }

            if (enterpriseInput.getSubsidiaryEnterprises() != null) {
                for (EnterpriseInput subsidiary : enterpriseInput.getSubsidiaryEnterprises()) {
                    if (subsidiary.getId() != null) {
                        IRI subsidiaryIRI = Values.iri(subsidiary.getId());
                        this.updateEnterprise(subsidiaryIRI, subsidiary);
                    } else {
                        var newSubsidiary = this.createEnterprise(subsidiary);
                        this.addSubsidiaryEnterpriseToEnterprise(
                                newSubsidiary.getId(), enterpriseId);
                    }
                }
            }

            if (enterpriseInput.getFactories() != null) {
                for (FactoryInput factory : enterpriseInput.getFactories()) {
                    if (factory.getId() != null) {
                        IRI factoryIRI = Values.iri(factory.getId());
                        this.updateFactory(factoryIRI, factory);
                    } else {
                        var newFactory = this.createFactory(factory);
                        this.addFactoryToEnterprise(newFactory.getId(), enterpriseId);
                    }
                }
            }

            if (enterpriseInput.getProperties() != null) {
                for (PropertyInput property : enterpriseInput.getProperties()) {
                    if (property.getId() != null) {
                        IRI propertyIRI = Values.iri(property.getId());
                        this.updateProperty(propertyIRI, property);
                    } else {
                        var newProperty = this.createProperty(property);
                        this.addPropertyToEnterprise(newProperty.getId(), enterpriseId);
                    }
                }
            }

            if (enterpriseInput.getProducts() != null) {
                for (ProductInput product : enterpriseInput.getProducts()) {
                    if (product.getId() != null) {
                        IRI productIRI = Values.iri(product.getId());
                        this.updateProduct(productIRI, product);
                    } else {
                        var newProduct = this.createProduct(product);
                        this.addProductToEnterprise(newProduct.getId(), enterpriseId);
                    }
                }
            }
        }
        return this.getEnterpriseById(enterpriseId);
    }

    public boolean deleteEnterprise(IRI enterpriseId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.remove(enterpriseId, null, null);
            if (this.getEnterpriseById(enterpriseId) != null) {
                return false;
            }
        }
        return true;
    }

    public boolean bulkDeleteEnterprise(IRI enterpriseId) {
        String query =
                "delete { ?s ?p ?o. ?o ?x ?y}\n"
                        + "where { {\n"
                        + "  <"
                        + enterpriseId
                        + "> <https://www.smartfactoryweb.de/ontology/sfw_capability_model_top_level#has>"
                        + " ?s .\n"
                        + "    ?s ?p ?o .\n"
                        + " ?o ?x ?y.}\n"
                        + "UNION {\n"
                        + "    <"
                        + enterpriseId
                        + "> <https://www.smartfactoryweb.de/ontology/sfw_capability_model_top_level#contains>"
                        + " ?s . \n"
                        + "?s ?p ?o .\n"
                        + " ?o ?x ?y.}\n"
                        + "}";

        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(query).execute();
        }
        return this.deleteEnterprise(enterpriseId);
    }

    public Enterprise addFactoryToEnterprise(IRI factoryId, IRI enterpriseId) {
        String insertQuery =
                Queries.INSERT_DATA(GraphPatterns.tp(enterpriseId, AMS.contains, factoryId))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getEnterpriseById(enterpriseId);
    }

    public Factory createFactoryForEnterprise(FactoryInput factoryInput, IRI enterpriseId) {
        var factory = this.createFactory(factoryInput);
        String insertQuery =
                Queries.INSERT_DATA(
                                GraphPatterns.tp(enterpriseId, iri(AMS.contains), factory.getId()))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getFactoryByIri(factory.getId());
    }

    public Enterprise removeFactoryFromEnterprise(IRI factoryId, IRI enterpriseId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            // TODO: Add context.
            connection.remove(enterpriseId, AMS.contains, factoryId);
        }
        return this.getEnterpriseById(enterpriseId);
    }

    public Enterprise addPropertyToEnterprise(IRI propertyId, IRI enterpriseId) {
        String insertQuery =
                Queries.INSERT_DATA(GraphPatterns.tp(enterpriseId, AMS.has, propertyId))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getEnterpriseById(enterpriseId);
    }

    public Property createPropertyForEnterprise(PropertyInput propertyInput, IRI enterpriseId) {
        var property = this.createProperty(propertyInput);
        String insertQuery =
                Queries.INSERT_DATA(GraphPatterns.tp(enterpriseId, AMS.has, property.getId()))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getPropertyById(property.getId());
    }

    public Enterprise removePropertyFromEnterprise(IRI propertyId, IRI enterpriseId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            // TODO: Add context.
            connection.remove(enterpriseId, AMS.has, propertyId);
        }
        return this.getEnterpriseById(enterpriseId);
    }

    public Enterprise addSubsidiaryEnterpriseToEnterprise(
            IRI subsidiaryEnterpriseId, IRI enterpriseId) {
        String insertQuery =
                Queries.INSERT_DATA(
                                GraphPatterns.tp(
                                        enterpriseId, AMS.contains, subsidiaryEnterpriseId))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getEnterpriseById(enterpriseId);
    }

    public Enterprise createSubsidiaryEnterpriseForEnterprise(
            EnterpriseInput enterpriseInput, IRI enterpriseId) {
        var subsidiaryEnterprise = this.createEnterprise(enterpriseInput);
        String insertQuery =
                Queries.INSERT_DATA(
                                GraphPatterns.tp(
                                        enterpriseId, AMS.contains, subsidiaryEnterprise.getId()))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getEnterpriseById(subsidiaryEnterprise.getId());
    }

    public Enterprise removeSubsidiaryEnterpriseFromEnterprise(
            IRI subsidiaryEnterpriseId, IRI enterpriseId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            // TODO: Add context.
            connection.remove(enterpriseId, AMS.contains, subsidiaryEnterpriseId);
        }
        return this.getEnterpriseById(enterpriseId);
    }

    public Enterprise addProductToEnterprise(IRI productId, IRI enterpriseId) {
        String insertQuery =
                Queries.INSERT_DATA(GraphPatterns.tp(enterpriseId, AMS.contains, productId))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getEnterpriseById(enterpriseId);
    }

    public Product createProductForEnterprise(ProductInput productInput, IRI enterpriseId) {
        var product = this.createProduct(productInput);
        String insertQuery =
                Queries.INSERT_DATA(GraphPatterns.tp(enterpriseId, AMS.contains, product.getId()))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getProductById(product.getId());
    }

    public Enterprise removeProductFromEnterprise(IRI productId, IRI enterpriseId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            // TODO: Add context.
            connection.remove(enterpriseId, AMS.contains, productId);
        }
        return this.getEnterpriseById(enterpriseId);
    }

    public Factory createFactory(FactoryInput factoryInput) {
        if (factoryInput.getId() != null) {
            return this.getFactoryByIri(Values.iri(factoryInput.getId()));
        }
        IRI factoryIRI = this.getNewUUID();

        TriplePattern insertPattern = GraphPatterns.tp(factoryIRI, RDF.TYPE, AMS.PhysicalFactory);
        if (factoryInput.getLabel() != null && !factoryInput.getLabel().isBlank()) {
            insertPattern =
                    insertPattern.andHas(
                            iri(RDFS.LABEL),
                            Values.literal(
                                    factoryInput.getLabel(), factoryInput.getLabelLanguageCode()));
        }

        if (factoryInput.getDescription() != null && !factoryInput.getDescription().isBlank()) {
            insertPattern =
                    insertPattern.andHas(
                            iri(RDFS.COMMENT),
                            Values.literal(
                                    factoryInput.getDescription(),
                                    factoryInput.getDescriptionLanguageCode()));
        }

        if (factoryInput.getSourceId() != null) {
            insertPattern =
                    insertPattern.andHas(iri(AMS.externalIdentifier), factoryInput.getSourceId());
        }

        if (factoryInput.getProperties() != null) {
            for (PropertyInput property : factoryInput.getProperties()) {
                IRI propertyIRI = this.createProperty(property).getId();
                this.addPropertyToFactory(propertyIRI, factoryIRI);
            }
        }

        if (factoryInput.getProducts() != null) {
            for (ProductInput product : factoryInput.getProducts()) {
                IRI productIRI = this.createProduct(product).getId();
                this.addProductToFactory(productIRI, factoryIRI);
            }
        }

        if (factoryInput.getHumanResources() != null) {
            for (HumanResourceInput humanResource : factoryInput.getHumanResources()) {
                IRI humanResourceIRI = this.createHumanResource(humanResource).getId();
                this.addHumanResourceToFactory(humanResourceIRI, factoryIRI);
            }
        }

        if (factoryInput.getProcesses() != null) {
            for (ProcessInput process : factoryInput.getProcesses()) {
                IRI processIRI =
                        this.createProcess(process, new LinkedList<>(), new LinkedList<>()).getId();
                this.addProcessToFactory(processIRI, factoryIRI);
            }
        }

        String insertQuery =
                Queries.INSERT_DATA(insertPattern)
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        Location location;
        String locationQuery = null;
        String machinesQuery = null;
        TriplesTemplate machineInsertQuery = SparqlBuilder.triplesTemplate();
        if (factoryInput.getMachines() != null) {
            for (MachineInput machine : factoryInput.getMachines()) {
                this.createInsertMachineQueryToFactory(machineInsertQuery, machine, factoryIRI);
            }
            machinesQuery =
                    Queries.INSERT_DATA(machineInsertQuery)
                            .into(this::getGraphNameForMutation)
                            .getQueryString();
        }
        if (factoryInput.getLocation() != null) {
            location = this.createAndInsertLocation(factoryInput.getLocation());
            locationQuery =
                    Queries.INSERT_DATA(GraphPatterns.tp(factoryIRI, AMS.has, location.getId()))
                            .into(this::getGraphNameForMutation)
                            .getQueryString();
        }
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
            if (machinesQuery != null) {
                connection.prepareUpdate(machinesQuery).execute();
            }
            if (locationQuery != null) {
                connection.prepareUpdate(locationQuery).execute();
            }
            if (factoryInput.getCertificates() != null) {
                for (var cert : factoryInput.getCertificates()) {
                    var certificate = this.createProperty(cert);
                    String certificateInsertQuery =
                            Queries.INSERT_DATA(
                                            GraphPatterns.tp(
                                                    factoryIRI,
                                                    AMS.certificate,
                                                    certificate.getId()))
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(certificateInsertQuery).execute();
                }
            }
        }
        return this.getFactoryByIri(factoryIRI);
    }

    public Factory updateFactory(IRI factoryId, FactoryInput factoryInput) {
        Factory factory = this.getFactoryByIri(factoryId);
        try (RepositoryConnection connection = repository.getConnection()) {
            if (factoryInput.getSourceId() != null) {
                connection.remove(factoryId, AMS.externalIdentifier, null);
                TriplePattern insert =
                        GraphPatterns.tp(
                                factoryId,
                                AMS.externalIdentifier,
                                Values.literal(factoryInput.getSourceId()));
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }
            if (factoryInput.getLabel() != null) {
                connection.remove(factoryId, RDFS.LABEL, null);
                if (factoryInput.getLabelLanguageCode() != null) {
                    TriplePattern insert =
                            GraphPatterns.tp(
                                    factoryId,
                                    RDFS.LABEL,
                                    Values.literal(
                                            factoryInput.getLabel(),
                                            factoryInput.getLabelLanguageCode()));
                    String queryText =
                            Queries.INSERT_DATA(insert)
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(queryText).execute();
                }
            } else if (factoryInput.getLabelLanguageCode() != null) {
                connection.remove(factoryId, RDFS.LABEL, null);
                TriplePattern insert =
                        GraphPatterns.tp(
                                factoryId,
                                RDFS.LABEL,
                                Values.literal(
                                        factory.getLabel(), factoryInput.getLabelLanguageCode()));
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }
            if (factoryInput.getDescription() != null) {
                connection.remove(factoryId, RDFS.COMMENT, null);
                if (factoryInput.getDescriptionLanguageCode() != null) {
                    TriplePattern insert =
                            GraphPatterns.tp(
                                    factoryId,
                                    RDFS.COMMENT,
                                    Values.literal(
                                            factoryInput.getDescription(),
                                            factoryInput.getDescriptionLanguageCode()));
                    String queryText =
                            Queries.INSERT_DATA(insert)
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(queryText).execute();
                }
            }
            if (factoryInput.getDescription() == null
                    && factoryInput.getDescriptionLanguageCode() != null) {
                connection.remove(factoryId, RDFS.COMMENT, null);
                TriplePattern insert =
                        GraphPatterns.tp(
                                factoryId,
                                RDFS.LABEL,
                                Values.literal(
                                        factory.getDescription(),
                                        factoryInput.getDescriptionLanguageCode()));
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }

            if (factoryInput.getLocation() != null) {
                connection.remove(factoryId, AMS.has, factory.getLocation().getId());
                var location = this.createAndInsertLocation(factoryInput.getLocation());
                String locationQuery =
                        Queries.INSERT_DATA(GraphPatterns.tp(factoryId, AMS.has, location.getId()))
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(locationQuery).execute();
            }
            if (factoryInput.getProcesses() != null) {
                for (ProcessInput process : factoryInput.getProcesses()) {
                    if (process.getId() != null) {
                        IRI processIRI = Values.iri(process.getId());
                        this.updateProcess(processIRI, process, null, null);
                    } else {
                        var newProcess = this.createProcess(process, null, null);
                        this.addProcessToFactory(newProcess.getId(), factoryId);
                    }
                }
            }

            if (factoryInput.getProperties() != null) {
                for (PropertyInput property : factoryInput.getProperties()) {
                    if (property.getId() != null) {
                        IRI propertyIRI = Values.iri(property.getId());
                        this.updateProperty(propertyIRI, property);
                    } else {
                        var newProperty = this.createProperty(property);
                        this.addPropertyToFactory(newProperty.getId(), factoryId);
                    }
                }
            }

            if (factoryInput.getHumanResources() != null) {
                for (HumanResourceInput humanResource : factoryInput.getHumanResources()) {
                    if (humanResource.getId() != null) {
                        IRI hrIRI = Values.iri(humanResource.getId());
                        this.updateHumanResource(hrIRI, humanResource);
                    } else {
                        var newHR = this.createHumanResource(humanResource);
                        this.addHumanResourceToFactory(newHR.getId(), factoryId);
                    }
                }
            }

            if (factoryInput.getMachines() != null) {
                for (MachineInput machine : factoryInput.getMachines()) {
                    if (machine.getId() != null) {
                        IRI machineIRI = Values.iri(machine.getId());
                        this.updateMachine(machineIRI, machine);
                    } else {
                        var newMachine = this.createMachine(machine);
                        this.addMachineToFactory(newMachine.getId(), factoryId);
                    }
                }
            }
        }
        return this.getFactoryByIri(factoryId);
    }

    public boolean deleteFactory(IRI factoryId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.remove(factoryId, null, null);
            if (this.getFactoryByIri(factoryId) != null) {
                return false;
            }
        }
        return true;
    }

    public Factory addPropertyToFactory(IRI propertyId, IRI factoryId) {
        String insertQuery =
                Queries.INSERT_DATA(GraphPatterns.tp(factoryId, AMS.has, propertyId))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getFactoryByIri(factoryId);
    }

    public Property createPropertyForFactory(PropertyInput propertyInput, IRI factoryId) {
        var property = this.createProperty(propertyInput);
        String insertQuery =
                Queries.INSERT_DATA(GraphPatterns.tp(factoryId, iri(AMS.has), property.getId()))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getPropertyById(property.getId());
    }

    public Factory removePropertyFromFactory(IRI propertyId, IRI factoryId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            // TODO: Add context.
            connection.remove(factoryId, AMS.has, propertyId);
        }
        return this.getFactoryByIri(factoryId);
    }

    public Factory addProductToFactory(IRI productId, IRI factoryId) {
        String insertQuery =
                Queries.INSERT_DATA(GraphPatterns.tp(factoryId, AMS.contains, productId))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getFactoryByIri(factoryId);
    }

    public Product createProductForFactory(ProductInput productInput, IRI factoryId) {
        var product = this.createProduct(productInput);
        String insertQuery =
                Queries.INSERT_DATA(GraphPatterns.tp(factoryId, AMS.contains, product.getId()))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getProductById(product.getId());
    }

    public Factory removeProductFromFactory(IRI productId, IRI factoryId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            // TODO: Add context.
            connection.remove(factoryId, AMS.contains, productId);
        }
        return this.getFactoryByIri(factoryId);
    }

    public Factory addMachineToFactory(IRI machineId, IRI factoryId) {
        String insertQuery =
                Queries.INSERT_DATA(GraphPatterns.tp(factoryId, AMS.contains, machineId))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getFactoryByIri(factoryId);
    }

    public Machine createMachineForFactory(MachineInput machineInput, IRI factoryId) {
        var machine = this.createMachine(machineInput);
        String insertQuery =
                Queries.INSERT_DATA(GraphPatterns.tp(factoryId, iri(AMS.contains), machine.getId()))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getMachineById(machine.getId());
    }

    public Factory removeMachineFromFactory(IRI machineId, IRI factoryId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            // TODO: Add context.
            connection.remove(factoryId, AMS.contains, machineId);
        }
        return this.getFactoryByIri(factoryId);
    }

    public Factory addHumanResourceToFactory(IRI humanResourceId, IRI factoryId) {
        String insertQuery =
                Queries.INSERT_DATA(GraphPatterns.tp(factoryId, AMS.contains, humanResourceId))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getFactoryByIri(factoryId);
    }

    public HumanResource createHumanResourceForFactory(
            HumanResourceInput humanResourceInput, IRI factoryId) {
        var humanResource = this.createHumanResource(humanResourceInput);
        String insertQuery =
                Queries.INSERT_DATA(
                                GraphPatterns.tp(
                                        factoryId, iri(AMS.contains), humanResource.getId()))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getHumanResourceById(humanResource.getId());
    }

    public Factory removeHumanResourceFromFactory(IRI humanResourceId, IRI factoryId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            // TODO: Add context.
            connection.remove(factoryId, AMS.contains, humanResourceId);
        }
        return this.getFactoryByIri(factoryId);
    }

    public Factory addProcessToFactory(IRI processId, IRI factoryId) {
        String insertQuery =
                Queries.INSERT_DATA(GraphPatterns.tp(factoryId, AMS.provides, processId))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getFactoryByIri(factoryId);
    }

    public Process createProcessForFactory(ProcessInput processInput, IRI factoryId) {
        var process = this.createAndInsertProcess(processInput);
        String insertQuery =
                Queries.INSERT_DATA(GraphPatterns.tp(factoryId, AMS.provides, process.getId()))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getProcessById(process.getId());
    }

    public Factory removeProcessFromFactory(IRI processId, IRI factoryId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            // TODO: Add context.
            connection.remove(factoryId, AMS.provides, processId);
        }
        return this.getFactoryByIri(factoryId);
    }

    public SupplyChain createSupplyChain(SupplyChainInput supplyChainInput) {
        if (supplyChainInput.getId() != null) {
            return this.getSupplyChainById(Values.iri(supplyChainInput.getId()));
        }
        IRI supplyChainIRI = this.getNewUUID();

        TriplePattern insertPattern = GraphPatterns.tp(supplyChainIRI, RDF.TYPE, AMS.SupplyChain);

        if (supplyChainInput.getDescription() != null
                && !supplyChainInput.getDescription().isBlank()) {
            insertPattern =
                    insertPattern.andHas(
                            iri(RDFS.COMMENT),
                            Values.literal(
                                    supplyChainInput.getDescription(),
                                    supplyChainInput.getDescriptionLanguageCode()));
        }

        if (supplyChainInput.getSourceId() != null) {
            insertPattern =
                    insertPattern.andHas(
                            iri(AMS.externalIdentifier), supplyChainInput.getSourceId());
        }
        if (supplyChainInput.getSuppliers() != null) {
            for (SupplyChainElementInput sce : supplyChainInput.getSuppliers()) {
                IRI sceId =
                        this.createSupplyChainElement(sce, new LinkedList<>(), new LinkedList<>())
                                .getId();
                this.addSupplyChainElementToSupplyChain(sceId, supplyChainIRI);
            }
        }
        String insertQuery =
                Queries.INSERT_DATA(insertPattern)
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getSupplyChainById(supplyChainIRI);
    }

    public SupplyChain updateSupplyChain(IRI supplyChainId, SupplyChainInput supplyChainInput) {
        SupplyChain supplyChain = this.getSupplyChainById(supplyChainId);
        try (RepositoryConnection connection = repository.getConnection()) {
            if (supplyChainInput.getSourceId() != null) {
                connection.remove(supplyChainId, AMS.externalIdentifier, null);
                TriplePattern insert =
                        GraphPatterns.tp(
                                supplyChainId,
                                AMS.externalIdentifier,
                                Values.literal(supplyChainInput.getSourceId()));
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }
            if (supplyChainInput.getDescription() != null) {
                connection.remove(supplyChainId, RDFS.COMMENT, null);
                if (supplyChainInput.getDescriptionLanguageCode() != null) {
                    TriplePattern insert =
                            GraphPatterns.tp(
                                    supplyChainId,
                                    RDFS.COMMENT,
                                    Values.literal(
                                            supplyChainInput.getDescription(),
                                            supplyChainInput.getDescriptionLanguageCode()));
                    String queryText =
                            Queries.INSERT_DATA(insert)
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(queryText).execute();
                }
            } else if (supplyChainInput.getDescriptionLanguageCode() != null) {
                connection.remove(supplyChainId, RDFS.COMMENT, null);
                TriplePattern insert =
                        GraphPatterns.tp(
                                supplyChainId,
                                RDFS.COMMENT,
                                Values.literal(
                                        supplyChain.getDescription(),
                                        supplyChainInput.getDescriptionLanguageCode()));
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }
        }
        return this.getSupplyChainById(supplyChainId);
    }

    public boolean deleteSupplyChain(IRI scId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.remove(scId, null, null);
            if (this.getSupplyChainById(scId) != null) {
                return false;
            }
        }
        return true;
    }

    public SupplyChain addSupplyChainElementToSupplyChain(IRI sceId, IRI scId) {
        String insertQuery =
                Queries.INSERT_DATA(GraphPatterns.tp(scId, AMS.contains, sceId))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getSupplyChainById(scId);
    }

    public SupplyChainElement createSupplyChainElementForSupplyChain(
            SupplyChainElementInput input, IRI scId) {
        var supplyChainElement = this.createAndInsertSupplyChainElement(input);
        String insertQuery =
                Queries.INSERT_DATA(
                                GraphPatterns.tp(scId, AMS.contains, supplyChainElement.getId()))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getSupplyChainElementById(supplyChainElement.getId());
    }

    public SupplyChain removeSupplyChainElementFromSupplyChain(IRI sceId, IRI scId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            // TODO: Add context.
            connection.remove(scId, AMS.contains, sceId);
        }
        return this.getSupplyChainById(scId);
    }

    public SupplyChainElement createAndInsertSupplyChainElement(
            SupplyChainElementInput supplyChainElementInput) {
        IRI supplyChainElementIRI = this.getNewUUID();

        Factory factory = null;
        if (supplyChainElementInput.getFactory() != null) {
            factory = createFactory(supplyChainElementInput.getFactory());
        }
        Enterprise enterprise = null;
        if (supplyChainElementInput.getEnterprise() != null) {
            enterprise = createEnterprise(supplyChainElementInput.getEnterprise());
        }

        TriplePattern insertPattern =
                GraphPatterns.tp(supplyChainElementIRI, RDF.TYPE, AMS.SupplyChainElement);

        if (supplyChainElementInput.getDescription() != null) {
            insertPattern =
                    insertPattern.andHas(
                            iri(RDFS.COMMENT),
                            Values.literal(
                                    supplyChainElementInput.getDescription(),
                                    supplyChainElementInput.getDescriptionLanguageCode()));
        }

        if (supplyChainElementInput.getSourceId() != null) {
            insertPattern =
                    insertPattern.andHas(
                            iri(AMS.externalIdentifier), supplyChainElementInput.getSourceId());
        }
        String insertQuery =
                Queries.INSERT_DATA(insertPattern)
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
            if (enterprise != null) {
                String enterpriseQuery =
                        Queries.INSERT_DATA(
                                        GraphPatterns.tp(
                                                supplyChainElementIRI,
                                                iri(AMS.has),
                                                enterprise.getId()))
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(enterpriseQuery).execute();
            }
            if (factory != null) {
                String factoryQuery =
                        Queries.INSERT_DATA(
                                        GraphPatterns.tp(
                                                supplyChainElementIRI,
                                                iri(AMS.has),
                                                factory.getId()))
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(factoryQuery).execute();
            }
            if (supplyChainElementInput.getProducts() != null) {
                for (var productApp : supplyChainElementInput.getProducts()) {
                    var product = this.createProductApplication(productApp);
                    String productQuery =
                            Queries.INSERT_DATA(
                                            GraphPatterns.tp(
                                                    supplyChainElementIRI,
                                                    iri(AMS.has),
                                                    product.getId()))
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(productQuery).execute();
                }
            }
        }
        return this.getSupplyChainElementById(supplyChainElementIRI);
    }

    public SupplyChainElement createSupplyChainElement(
            SupplyChainElementInput input, List<IRI> parentIds, List<IRI> childIds) {
        if (input.getId() != null) {
            return this.getSupplyChainElementById(Values.iri(input.getId()));
        }
        var sce = this.createAndInsertSupplyChainElement(input);
        if (input.getSuppliers() != null) {
            for (SupplyChainElementInput sceInput : input.getSuppliers()) {
                this.createSupplyChainElement(
                        sceInput, Collections.singletonList(sce.getId()), new LinkedList<>());
            }
        }
        try (RepositoryConnection connection = repository.getConnection()) {
            for (var parent : parentIds) {
                String parentQuery =
                        Queries.INSERT_DATA(
                                        GraphPatterns.tp(parent, AMS.contains, sce.getId()),
                                        GraphPatterns.tp(sce.getId(), AMS.containedIn, parent))
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(parentQuery).execute();
            }

            for (var child : childIds) {
                String childQuery =
                        Queries.INSERT_DATA(
                                        GraphPatterns.tp(sce.getId(), AMS.contains, child),
                                        GraphPatterns.tp(child, AMS.containedIn, sce.getId()))
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(childQuery).execute();
            }
        }
        return this.getSupplyChainElementById(sce.getId());
    }

    public SupplyChainElement updateSupplyChainElement(
            IRI sceId, SupplyChainElementInput input, List<IRI> parentIds, List<IRI> childIds) {
        try (RepositoryConnection connection = repository.getConnection()) {
            SupplyChainElement supplyChainElement = this.getSupplyChainElementById(sceId);
            if (input.getSourceId() != null) {
                connection.remove(sceId, AMS.externalIdentifier, null);
                TriplePattern insert =
                        GraphPatterns.tp(
                                sceId, AMS.externalIdentifier, Values.literal(input.getSourceId()));
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }
            if (input.getDescription() != null) {
                connection.remove(sceId, RDFS.COMMENT, null);
                if (input.getDescriptionLanguageCode() != null) {
                    TriplePattern insert =
                            GraphPatterns.tp(
                                    sceId,
                                    RDFS.COMMENT,
                                    Values.literal(
                                            input.getDescription(),
                                            input.getDescriptionLanguageCode()));
                    String queryText =
                            Queries.INSERT_DATA(insert)
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(queryText).execute();
                }
            } else if (input.getDescriptionLanguageCode() != null) {
                connection.remove(sceId, RDFS.COMMENT, null);
                TriplePattern insert =
                        GraphPatterns.tp(
                                sceId,
                                RDFS.COMMENT,
                                Values.literal(
                                        supplyChainElement.getDescription(),
                                        input.getDescriptionLanguageCode()));
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }
            if (input.getEnterprise() != null) {
                connection.remove(sceId, AMS.has, supplyChainElement.getEnterprise().getId());
                var enterprise = this.createEnterprise(input.getEnterprise());
                TriplePattern insert = GraphPatterns.tp(sceId, AMS.has, enterprise.getId());
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }
            if (input.getFactory() != null) {
                connection.remove(sceId, AMS.has, supplyChainElement.getFactory().getId());
                var factory = this.createFactory(input.getFactory());
                TriplePattern insert = GraphPatterns.tp(sceId, AMS.has, factory.getId());
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }
            if (input.getProducts() != null) {
                for (var product : supplyChainElement.getProducts()) {
                    connection.remove(sceId, AMS.has, product.getId());
                }
                for (var product : input.getProducts()) {
                    var productApplication = this.createProductApplication(product);
                    TriplePattern insert =
                            GraphPatterns.tp(sceId, AMS.has, productApplication.getId());
                    String queryText =
                            Queries.INSERT_DATA(insert)
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(queryText).execute();
                }
            }
            if (parentIds != null) {
                connection.remove(sceId, AMS.containedIn, null);
                for (var parent : parentIds) {
                    String parentQuery =
                            Queries.INSERT_DATA(
                                            GraphPatterns.tp(parent, AMS.contains, sceId),
                                            GraphPatterns.tp(sceId, AMS.containedIn, parent))
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(parentQuery).execute();
                }
            }
            if (childIds != null) {
                connection.remove(sceId, AMS.contains, null);
                for (var child : childIds) {
                    String childQuery =
                            Queries.INSERT_DATA(
                                            GraphPatterns.tp(sceId, AMS.contains, child),
                                            GraphPatterns.tp(child, AMS.containedIn, sceId))
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(childQuery).execute();
                }
            }
        }
        return this.getSupplyChainElementById(sceId);
    }

    public boolean deleteSupplyChainElement(IRI sceId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.remove(sceId, null, null);
            if (this.getSupplyChainElementById(sceId) != null) {
                return false;
            }
        }
        return true;
    }

    public Property createProperty(PropertyInput propertyInput) {
        if (propertyInput.getId() != null) {
            return this.getPropertyById(Values.iri(propertyInput.getId()));
        }
        IRI propertyIRI = this.getNewUUID();

        TriplePattern insertPattern = GraphPatterns.tp(propertyIRI, RDF.TYPE, AMS.Property);
        if (propertyInput.getLabel() != null && !propertyInput.getLabel().isBlank()) {
            insertPattern =
                    insertPattern.andHas(
                            iri(RDFS.LABEL),
                            Values.literal(
                                    propertyInput.getLabel(),
                                    propertyInput.getLabelLanguageCode()));
        }

        if (propertyInput.getDescription() != null && !propertyInput.getDescription().isBlank()) {
            insertPattern =
                    insertPattern.andHas(
                            iri(RDFS.COMMENT),
                            Values.literal(
                                    propertyInput.getDescription(),
                                    propertyInput.getDescriptionLanguageCode()));
        }

        if (propertyInput.getSourceId() != null) {
            insertPattern =
                    insertPattern.andHas(iri(AMS.externalIdentifier), propertyInput.getSourceId());
        }

        if (propertyInput.getValue() != null) {
            insertPattern = insertPattern.andHas(iri(AMS.value), propertyInput.getValue());
        }
        String insertQuery =
                Queries.INSERT_DATA(insertPattern)
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
            if (propertyInput.getSemanticReferences() != null) {
                for (var semRef : propertyInput.getSemanticReferences()) {
                    var added = this.createSemanticReference(semRef);
                    String semRefQuery =
                            Queries.INSERT_DATA(
                                            GraphPatterns.tp(
                                                    propertyIRI,
                                                    iri(AMS.hasSemantic),
                                                    added.getId()))
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(semRefQuery).execute();
                }
            }
        }
        return this.getPropertyById(propertyIRI);
    }

    public Property updateProperty(IRI propertyId, PropertyInput propertyInput) {
        Property property = this.getPropertyById(propertyId);
        try (RepositoryConnection connection = repository.getConnection()) {
            if (propertyInput.getSourceId() != null) {
                connection.remove(propertyId, AMS.externalIdentifier, null);
                TriplePattern insert =
                        GraphPatterns.tp(
                                propertyId,
                                AMS.externalIdentifier,
                                Values.literal(propertyInput.getSourceId()));
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }
            if (propertyInput.getValue() != null) {
                connection.remove(propertyId, AMS.value, null);
                TriplePattern insert =
                        GraphPatterns.tp(
                                propertyId, AMS.value, Values.literal(propertyInput.getValue()));
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }
            if (propertyInput.getLabel() != null) {
                connection.remove(propertyId, RDFS.LABEL, null);
                if (propertyInput.getLabelLanguageCode() != null) {
                    TriplePattern insert =
                            GraphPatterns.tp(
                                    propertyId,
                                    RDFS.LABEL,
                                    Values.literal(
                                            propertyInput.getLabel(),
                                            propertyInput.getLabelLanguageCode()));
                    String queryText =
                            Queries.INSERT_DATA(insert)
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(queryText).execute();
                } else if (propertyInput.getLabelLanguageCode() == null
                        && property.getLabelLanguageCode() != null) {
                    TriplePattern insert =
                            GraphPatterns.tp(
                                    propertyId,
                                    RDFS.LABEL,
                                    Values.literal(
                                            propertyInput.getLabel(),
                                            property.getLabelLanguageCode()));
                    String queryText =
                            Queries.INSERT_DATA(insert)
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(queryText).execute();
                }
            } else if (propertyInput.getLabelLanguageCode() != null) {
                connection.remove(propertyId, RDFS.LABEL, null);
                TriplePattern insert =
                        GraphPatterns.tp(
                                propertyId,
                                RDFS.LABEL,
                                Values.literal(
                                        property.getLabel(), propertyInput.getLabelLanguageCode()));
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }
            if (propertyInput.getDescription() != null) {
                connection.remove(propertyId, RDFS.COMMENT, null);
                if (propertyInput.getDescriptionLanguageCode() != null) {
                    TriplePattern insert =
                            GraphPatterns.tp(
                                    propertyId,
                                    RDFS.COMMENT,
                                    Values.literal(
                                            propertyInput.getDescription(),
                                            propertyInput.getDescriptionLanguageCode()));
                    String queryText =
                            Queries.INSERT_DATA(insert)
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(queryText).execute();
                } else if (propertyInput.getDescriptionLanguageCode() == null
                        && property.getDescriptionLanguageCode() != null) {
                    TriplePattern insert =
                            GraphPatterns.tp(
                                    propertyId,
                                    RDFS.COMMENT,
                                    Values.literal(
                                            propertyInput.getDescription(),
                                            property.getDescriptionLanguageCode()));
                    String queryText =
                            Queries.INSERT_DATA(insert)
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(queryText).execute();
                }
            } else if (propertyInput.getDescriptionLanguageCode() != null) {
                connection.remove(propertyId, RDFS.COMMENT, null);
                TriplePattern insert =
                        GraphPatterns.tp(
                                propertyId,
                                RDFS.COMMENT,
                                Values.literal(
                                        property.getDescription(),
                                        propertyInput.getDescriptionLanguageCode()));
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }

            if (propertyInput.getSemanticReferences() != null) {
                for (var semRef : propertyInput.getSemanticReferences()) {
                    if (semRef.getId() != null) {
                        var semRefIRI = Values.iri(semRef.getId());
                        this.updateSemanticReference(semRefIRI, semRef);
                    } else {
                        var newSemRef = this.createSemanticReference(semRef);
                        this.addSemanticReferenceToProperty(newSemRef.getId(), propertyId);
                    }
                }
            }
        }
        return this.getPropertyById(propertyId);
    }

    public boolean deleteProperty(IRI propertyId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.remove(propertyId, null, null);
            if (this.getPropertyById(propertyId) != null) {
                return false;
            }
        }
        return true;
    }

    public Property addSemanticReferenceToProperty(IRI semRefId, IRI propertyId) {
        String insertQuery =
                Queries.INSERT_DATA(GraphPatterns.tp(propertyId, AMS.hasSemantic, semRefId))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getPropertyById(propertyId);
    }

    public SemanticReference createSemanticReferenceForProperty(
            SemanticReferenceInput semanticReferenceInput, IRI propertyId) {
        var semRef = this.createSemanticReference(semanticReferenceInput);
        String insertQuery =
                Queries.INSERT_DATA(GraphPatterns.tp(propertyId, AMS.hasSemantic, semRef.getId()))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getSemanticReferenceById(semRef.getId());
    }

    public Property removeSemanticReferenceFromProperty(IRI semRefId, IRI propertyId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            // TODO: Add context.
            connection.remove(propertyId, AMS.hasSemantic, semRefId);
        }
        return this.getPropertyById(propertyId);
    }

    public Product createProduct(ProductInput productInput) {
        if (productInput.getId() != null) {
            return this.getProductById(Values.iri(productInput.getId()));
        }
        IRI productInputIRI = this.getNewUUID();
        TriplePattern insertPattern = GraphPatterns.tp(productInputIRI, RDF.TYPE, AMS.Product);
        if (productInput.getLabel() != null && !productInput.getLabel().isBlank()) {
            insertPattern =
                    insertPattern.andHas(
                            iri(RDFS.LABEL),
                            Values.literal(
                                    productInput.getLabel(), productInput.getLabelLanguageCode()));
        }

        if (productInput.getDescription() != null && !productInput.getDescription().isBlank()) {
            insertPattern =
                    insertPattern.andHas(
                            iri(RDFS.COMMENT),
                            Values.literal(
                                    productInput.getDescription(),
                                    productInput.getDescriptionLanguageCode()));
        }

        if (productInput.getSourceId() != null) {
            insertPattern =
                    insertPattern.andHas(iri(AMS.externalIdentifier), productInput.getSourceId());
        }

        if (productInput.getBillOfMaterials() != null) {
            for (ProductApplicationInput bom : productInput.getBillOfMaterials()) {
                IRI bomIRI = this.createProductApplication(bom).getId();
                this.addSubProductToProduct(bomIRI, productInputIRI);
            }
        }

        if (productInput.getSupplyChains() != null) {
            for (SupplyChainInput sc : productInput.getSupplyChains()) {
                IRI scIRI = this.createSupplyChain(sc).getId();
                this.addSupplyChainToProduct(scIRI, productInputIRI);
            }
        }

        if (productInput.getProperties() != null) {
            for (PropertyInput property : productInput.getProperties()) {
                IRI propertyIRI = this.createProperty(property).getId();
                this.addPropertyToProduct(propertyIRI, productInputIRI);
            }
        }

        if (productInput.getSemanticReferences() != null) {
            for (SemanticReferenceInput semRef : productInput.getSemanticReferences()) {
                IRI semRefIRI = this.createSemanticReference(semRef).getId();
                this.addSemanticReferenceToProduct(semRefIRI, productInputIRI);
            }
        }
        String insertSubProductQuery =
                Queries.INSERT_DATA(insertPattern)
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertSubProductQuery).execute();
            if (productInput.getProductPassportInput() != null) {
                var productPassport =
                        this.createProductPassport(productInput.getProductPassportInput());
                String productPassportQuery =
                        Queries.INSERT_DATA(
                                        GraphPatterns.tp(
                                                productInputIRI,
                                                iri(AMS.has),
                                                productPassport.getId()))
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(productPassportQuery).execute();
            }
        }
        return this.getProductById(productInputIRI);
    }

    public Product updateProduct(IRI productId, ProductInput productInput) {
        Product product = this.getProductById(productId);
        try (RepositoryConnection connection = repository.getConnection()) {
            if (productInput.getSourceId() != null) {
                connection.remove(productId, AMS.externalIdentifier, null);
                TriplePattern insert =
                        GraphPatterns.tp(
                                productId,
                                AMS.externalIdentifier,
                                Values.literal(productInput.getSourceId()));
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }
            if (productInput.getLabel() != null) {
                connection.remove(productId, RDFS.LABEL, null);
                if (productInput.getLabelLanguageCode() != null) {
                    TriplePattern insert =
                            GraphPatterns.tp(
                                    productId,
                                    RDFS.LABEL,
                                    Values.literal(
                                            productInput.getLabel(),
                                            productInput.getLabelLanguageCode()));
                    String queryText =
                            Queries.INSERT_DATA(insert)
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(queryText).execute();
                }
            } else if (productInput.getLabelLanguageCode() != null) {
                connection.remove(productId, RDFS.LABEL, null);
                TriplePattern insert =
                        GraphPatterns.tp(
                                productId,
                                RDFS.LABEL,
                                Values.literal(
                                        product.getLabel(), productInput.getLabelLanguageCode()));
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }
            if (productInput.getDescription() != null) {
                connection.remove(productId, RDFS.COMMENT, null);
                if (productInput.getDescriptionLanguageCode() != null) {
                    TriplePattern insert =
                            GraphPatterns.tp(
                                    productId,
                                    RDFS.COMMENT,
                                    Values.literal(
                                            productInput.getDescription(),
                                            productInput.getDescriptionLanguageCode()));
                    String queryText =
                            Queries.INSERT_DATA(insert)
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(queryText).execute();
                }
            } else if (productInput.getDescriptionLanguageCode() != null) {
                connection.remove(productId, RDFS.COMMENT, null);
                TriplePattern insert =
                        GraphPatterns.tp(
                                productId,
                                RDFS.COMMENT,
                                Values.literal(
                                        product.getDescription(),
                                        productInput.getDescriptionLanguageCode()));
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }

            if (productInput.getProductPassportInput() != null) {
                connection.remove(productId, AMS.has, product.getProductPassport().getId());
                var productPassport =
                        this.createProductPassport(productInput.getProductPassportInput());
                TriplePattern insert =
                        GraphPatterns.tp(productId, AMS.has, productPassport.getId());
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }

            if (productInput.getBillOfMaterials() != null) {
                for (var material : productInput.getBillOfMaterials()) {
                    if (material.getId() != null) {
                        this.updateProductApplication(Values.iri(material.getId()), material);
                    } else {
                        var newProductApp = this.createProductApplication(material);
                        TriplePattern insert =
                                GraphPatterns.tp(productId, AMS.contains, newProductApp.getId());
                        String queryText =
                                Queries.INSERT_DATA(insert)
                                        .into(this::getGraphNameForMutation)
                                        .getQueryString();
                        connection.prepareUpdate(queryText).execute();
                    }
                }
            }

            if (productInput.getSemanticReferences() != null) {
                for (var semRef : productInput.getSemanticReferences()) {
                    if (semRef.getId() != null) {
                        var semRefIRI = Values.iri(semRef.getId());
                        this.updateSemanticReference(semRefIRI, semRef);
                    } else {
                        var newSemRef = this.createSemanticReference(semRef);
                        this.addSemanticReferenceToProduct(newSemRef.getId(), productId);
                    }
                }
            }

            if (productInput.getProperties() != null) {
                for (var property : productInput.getProperties()) {
                    if (property.getId() != null) {
                        var propertyIRI = Values.iri(property.getId());
                        this.updateProperty(propertyIRI, property);
                    } else {
                        var newProperty = this.createProperty(property);
                        this.addPropertyToProduct(newProperty.getId(), productId);
                    }
                }
            }

            if (productInput.getSupplyChains() != null) {
                for (var supplyChain : productInput.getSupplyChains()) {
                    if (supplyChain.getId() != null) {
                        var supplyChainIRI = Values.iri(supplyChain.getId());
                        this.updateSupplyChain(supplyChainIRI, supplyChain);
                    } else {
                        var newSc = this.createSupplyChain(supplyChain);
                        this.addSupplyChainToProduct(newSc.getId(), productId);
                    }
                }
            }
        }
        return this.getProductById(productId);
    }

    public boolean deleteProduct(IRI productId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.remove(productId, null, null);
            if (this.getProductById(productId) != null) {
                return false;
            }
        }
        return true;
    }

    public ProductPassport createProductPassport(ProductPassportInput productPassportInput) {
        if (productPassportInput.getId() != null) {
            return this.getProductPassportById(Values.iri(productPassportInput.getId()));
        }
        IRI productPassIRI = this.getNewUUID();

        try (RepositoryConnection connection = repository.getConnection()) {
            String insertQuery =
                    Queries.INSERT_DATA(
                                    GraphPatterns.tp(productPassIRI, RDF.TYPE, AMS.ProductPassport))
                            .into(this::getGraphNameForMutation)
                            .getQueryString();
            connection.prepareUpdate(insertQuery).execute();
            if (productPassportInput.getSourceId() != null) {
                String sourceIdQuery =
                        Queries.INSERT_DATA(
                                        GraphPatterns.tp(
                                                productPassIRI,
                                                AMS.externalIdentifier,
                                                Values.literal(productPassportInput.getSourceId())))
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(sourceIdQuery).execute();
            }
            if (productPassportInput.getIdentifier() != null) {
                String identifierQuery =
                        Queries.INSERT_DATA(
                                        GraphPatterns.tp(
                                                productPassIRI,
                                                AMS.identifier,
                                                Values.literal(
                                                        productPassportInput.getIdentifier())))
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(identifierQuery).execute();
            }
            if (productPassportInput.getProperties() != null) {
                for (var property : productPassportInput.getProperties()) {
                    var addedProperty = this.createProperty(property);
                    String propertyQuery =
                            Queries.INSERT_DATA(
                                            GraphPatterns.tp(
                                                    productPassIRI,
                                                    iri(AMS.has),
                                                    addedProperty.getId()))
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(propertyQuery).execute();
                }
            }
        }
        return this.getProductPassportById(productPassIRI);
    }

    public ProductPassport updateProductPassport(
            IRI productPassportId, ProductPassportInput productPassportInput) {
        try (RepositoryConnection connection = repository.getConnection()) {
            if (productPassportInput.getIdentifier() != null) {
                connection.remove(productPassportId, AMS.identifier, null);
                TriplePattern insert =
                        GraphPatterns.tp(
                                productPassportId,
                                AMS.identifier,
                                Values.literal(productPassportInput.getIdentifier()));
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }
            if (productPassportInput.getSourceId() != null) {
                connection.remove(productPassportId, AMS.externalIdentifier, null);
                TriplePattern insert =
                        GraphPatterns.tp(
                                productPassportId,
                                AMS.externalIdentifier,
                                Values.literal(productPassportInput.getSourceId()));
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }
            if (productPassportInput.getProperties() != null
                    && !productPassportInput.getProperties().isEmpty()) {
                connection.remove(productPassportId, AMS.has, null);
                for (var property : productPassportInput.getProperties()) {
                    IRI iri = this.createProperty(property).getId();
                    TriplePattern insert = GraphPatterns.tp(productPassportId, AMS.has, iri);
                    String queryText =
                            Queries.INSERT_DATA(insert)
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(queryText).execute();
                }
            }
        }
        return this.getProductPassportById(productPassportId);
    }

    public boolean deleteProductPassport(IRI productPassportId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.remove(productPassportId, null, null);
            if (this.getProductPassportById(productPassportId) != null) {
                return false;
            }
        }
        return true;
    }

    public Machine createMachine(MachineInput machineInput) {
        if (machineInput.getId() != null) {
            return this.getMachineById(Values.iri(machineInput.getId()));
        }
        IRI machineIri = this.getNewUUID();
        TriplePattern insertPattern = GraphPatterns.tp(machineIri, RDF.TYPE, AMS.Machine);
        if (machineInput.getLabel() != null && !machineInput.getLabel().isBlank()) {
            insertPattern =
                    insertPattern.andHas(
                            iri(RDFS.LABEL),
                            Values.literal(
                                    machineInput.getLabel(), machineInput.getLabelLanguageCode()));
        }

        if (machineInput.getDescription() != null && !machineInput.getDescription().isBlank()) {
            insertPattern =
                    insertPattern.andHas(
                            iri(RDFS.COMMENT),
                            Values.literal(
                                    machineInput.getDescription(),
                                    machineInput.getDescriptionLanguageCode()));
        }

        if (machineInput.getSourceId() != null) {
            insertPattern =
                    insertPattern.andHas(iri(AMS.externalIdentifier), machineInput.getSourceId());
        }

        if (machineInput.getMachineProperties() != null) {
            for (PropertyInput property : machineInput.getMachineProperties()) {
                IRI propertyIri = this.createProperty(property).getId();
                this.addPropertyToMachine(propertyIri, machineIri);
            }
        }

        if (machineInput.getProvidedCapabilities() != null) {
            for (CapabilityInput capability : machineInput.getProvidedCapabilities()) {
                var childCapabilities = new LinkedList<IRI>();
                var parentCapabilities = new LinkedList<IRI>();
                if (capability.getParentCapabilities() != null) {
                    for (CapabilityInput parentCapability : capability.getParentCapabilities()) {
                        IRI parentCapabilityIRI =
                                this.createCapability(
                                                parentCapability,
                                                new LinkedList<>(),
                                                new LinkedList<>())
                                        .getId();
                        parentCapabilities.add(parentCapabilityIRI);
                    }
                }
                if (capability.getChildCapabilities() != null) {
                    for (CapabilityInput childCapability : capability.getChildCapabilities()) {
                        IRI childCapabilityIRI =
                                this.createCapability(
                                                childCapability,
                                                new LinkedList<>(),
                                                new LinkedList<>())
                                        .getId();
                        childCapabilities.add(childCapabilityIRI);
                    }
                }

                IRI capabilityIRI =
                        this.createCapability(capability, parentCapabilities, childCapabilities)
                                .getId();
                this.addCapabilityToMachine(capabilityIRI, machineIri);
            }
        }

        if (machineInput.getProvidedProcesses() != null) {
            for (ProcessInput process : machineInput.getProvidedProcesses()) {
                IRI processIRI =
                        this.createProcess(process, new LinkedList<>(), new LinkedList<>()).getId();
                this.addProcessToMachine(processIRI, machineIri);
            }
        }
        try (RepositoryConnection connection = repository.getConnection()) {
            String insertQuery =
                    Queries.INSERT_DATA(insertPattern)
                            .into(this::getGraphNameForMutation)
                            .getQueryString();
            connection.prepareUpdate(insertQuery).execute();
            if (machineInput.getUsingProcesses() != null) {
                for (ProcessInput process : machineInput.getUsingProcesses()) {
                    IRI processIRI =
                            this.createProcess(process, new LinkedList<>(), new LinkedList<>())
                                    .getId();
                    String processInsertQuery =
                            Queries.INSERT_DATA(GraphPatterns.tp(machineIri, AMS.uses, processIRI))
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(processInsertQuery).execute();
                }
            }
        }
        return this.getMachineById(machineIri);
    }

    public Machine updateMachine(IRI machineId, MachineInput machineInput) {
        Machine machine = this.getMachineById(machineId);
        try (RepositoryConnection connection = repository.getConnection()) {
            if (machineInput.getSourceId() != null) {
                connection.remove(machineId, AMS.externalIdentifier, null);
                TriplePattern insert =
                        GraphPatterns.tp(
                                machineId,
                                AMS.externalIdentifier,
                                Values.literal(machineInput.getSourceId()));
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }
            if (machineInput.getLabel() != null) {
                connection.remove(machineId, RDFS.LABEL, null);
                if (machineInput.getLabelLanguageCode() != null) {
                    TriplePattern insert =
                            GraphPatterns.tp(
                                    machineId,
                                    RDFS.LABEL,
                                    Values.literal(
                                            machineInput.getLabel(),
                                            machineInput.getLabelLanguageCode()));
                    String queryText =
                            Queries.INSERT_DATA(insert)
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(queryText).execute();
                } else if (machineInput.getLabelLanguageCode() == null
                        && machine.getLabelLanguageCode() != null) {
                    TriplePattern insert =
                            GraphPatterns.tp(
                                    machineId,
                                    RDFS.LABEL,
                                    Values.literal(
                                            machineInput.getLabel(),
                                            machine.getLabelLanguageCode()));
                    String queryText =
                            Queries.INSERT_DATA(insert)
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(queryText).execute();
                }
            } else if (machineInput.getLabelLanguageCode() != null) {
                connection.remove(machineId, RDFS.LABEL, null);
                TriplePattern insert =
                        GraphPatterns.tp(
                                machineId,
                                RDFS.LABEL,
                                Values.literal(
                                        machine.getLabel(), machineInput.getLabelLanguageCode()));
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }
            if (machineInput.getDescription() != null) {
                connection.remove(machineId, RDFS.COMMENT, null);
                if (machineInput.getDescriptionLanguageCode() != null) {
                    TriplePattern insert =
                            GraphPatterns.tp(
                                    machineId,
                                    RDFS.COMMENT,
                                    Values.literal(
                                            machineInput.getDescription(),
                                            machineInput.getDescriptionLanguageCode()));
                    String queryText =
                            Queries.INSERT_DATA(insert)
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(queryText).execute();
                } else if (machineInput.getDescriptionLanguageCode() == null
                        && machine.getDescriptionLanguageCode() != null) {
                    TriplePattern insert =
                            GraphPatterns.tp(
                                    machineId,
                                    RDFS.COMMENT,
                                    Values.literal(
                                            machineInput.getDescription(),
                                            machine.getDescriptionLanguageCode()));
                    String queryText =
                            Queries.INSERT_DATA(insert)
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(queryText).execute();
                }
            } else if (machineInput.getDescriptionLanguageCode() != null) {
                connection.remove(machineId, RDFS.COMMENT, null);
                TriplePattern insert =
                        GraphPatterns.tp(
                                machineId,
                                RDFS.COMMENT,
                                Values.literal(
                                        machine.getDescription(),
                                        machineInput.getDescriptionLanguageCode()));
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }
        }
        if (machineInput.getMachineProperties() != null) {
            for (var machineProperty : machineInput.getMachineProperties()) {
                if (machineProperty.getId() != null) {
                    var machinePropertyId = Values.iri(machineProperty.getId());
                    this.updateProperty(machinePropertyId, machineProperty);
                } else {
                    var propertyId = this.createProperty(machineProperty).getId();
                    this.addPropertyToMachine(propertyId, machineId);
                }
            }
        }
        if (machineInput.getProvidedCapabilities() != null) {
            for (var capability : machineInput.getProvidedCapabilities()) {
                if (capability.getId() != null) {
                    var capabilityId = Values.iri(capability.getId());
                    this.updateCapability(
                            capabilityId, capability, new LinkedList<>(), new LinkedList<>());
                } else {
                    var capabilityId =
                            this.createCapability(
                                            capability, new LinkedList<>(), new LinkedList<>())
                                    .getId();
                    this.addCapabilityToMachine(capabilityId, machineId);
                }
            }
        }
        return this.getMachineById(machineId);
    }

    public boolean deleteMachine(String machineId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.remove(Values.iri(machineId), null, null);
            if (this.getMachineById(Values.iri(machineId)) != null) {
                return false;
            }
        }
        return true;
    }

    public Machine addPropertyToMachine(IRI propertyId, IRI machineId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            String insertQuery =
                    Queries.INSERT_DATA(GraphPatterns.tp(machineId, AMS.has, propertyId))
                            .into(this::getGraphNameForMutation)
                            .getQueryString();
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getMachineById(machineId);
    }

    public Property createPropertyForMachine(PropertyInput propertyInput, IRI machineId) {
        var property = this.createProperty(propertyInput);
        String insertQuery =
                Queries.INSERT_DATA(GraphPatterns.tp(machineId, iri(AMS.has), property.getId()))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getPropertyById(property.getId());
    }

    public Machine removePropertyFromMachine(IRI propertyId, IRI machineId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            // TODO: Add context.
            connection.remove(machineId, AMS.has, propertyId);
        }
        return this.getMachineById(machineId);
    }

    public Machine addProcessToMachine(IRI processId, IRI machineId) {
        String insertQuery =
                Queries.INSERT_DATA(GraphPatterns.tp(machineId, AMS.provides, processId))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getMachineById(machineId);
    }

    public Machine removeProcessFromMachine(IRI processId, IRI machineId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            // TODO: Add context.
            connection.remove(machineId, AMS.provides, processId);
        }
        return this.getMachineById(machineId);
    }

    public Machine addCapabilityToMachine(IRI capabilityId, IRI machineId) {
        String insertQuery =
                Queries.INSERT_DATA(GraphPatterns.tp(machineId, AMS.provides, capabilityId))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getMachineById(machineId);
    }

    public Machine removeCapabilityFromMachine(IRI capabilityId, IRI machineId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            // TODO: Add context.
            connection.remove(machineId, AMS.provides, capabilityId);
        }
        return this.getMachineById(machineId);
    }

    public HumanResource createHumanResource(HumanResourceInput humanResourceInput) {
        if (humanResourceInput.getId() != null) {
            return this.getHumanResourceById(Values.iri(humanResourceInput.getId()));
        }
        IRI hrIRI = this.getNewUUID();

        TriplePattern insertPattern = GraphPatterns.tp(hrIRI, RDF.TYPE, AMS.HumanResource);
        if (humanResourceInput.getLabel() != null && !humanResourceInput.getLabel().isBlank()) {
            insertPattern =
                    insertPattern.andHas(
                            iri(RDFS.LABEL),
                            Values.literal(
                                    humanResourceInput.getLabel(),
                                    humanResourceInput.getLabelLanguageCode()));
        }

        if (humanResourceInput.getDescription() != null
                && !humanResourceInput.getDescription().isBlank()) {
            insertPattern =
                    insertPattern.andHas(
                            iri(RDFS.COMMENT),
                            Values.literal(
                                    humanResourceInput.getDescription(),
                                    humanResourceInput.getDescriptionLanguageCode()));
        }

        if (humanResourceInput.getSourceId() != null) {
            insertPattern =
                    insertPattern.andHas(
                            iri(AMS.externalIdentifier), humanResourceInput.getSourceId());
        }
        try (RepositoryConnection connection = repository.getConnection()) {
            String insertQuery =
                    Queries.INSERT_DATA(insertPattern)
                            .into(this::getGraphNameForMutation)
                            .getQueryString();
            connection.prepareUpdate(insertQuery).execute();
            if (humanResourceInput.getCertificates() != null) {
                for (var cert : humanResourceInput.getCertificates()) {
                    var certificate = this.createProperty(cert);
                    String certificateInsertQuery =
                            Queries.INSERT_DATA(
                                            GraphPatterns.tp(
                                                    hrIRI, AMS.certificate, certificate.getId()))
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(certificateInsertQuery).execute();
                }
            }
            if (humanResourceInput.getUsingProcesses() != null) {
                for (ProcessInput process : humanResourceInput.getUsingProcesses()) {
                    IRI processIRI =
                            this.createProcess(process, new LinkedList<>(), new LinkedList<>())
                                    .getId();
                    this.addUsedProcessToHumanResource(processIRI, hrIRI);
                }
            }

            if (humanResourceInput.getProperties() != null) {
                for (PropertyInput property : humanResourceInput.getProperties()) {
                    IRI propertyIri = this.createProperty(property).getId();
                    String propertyInsertQuery =
                            Queries.INSERT_DATA(GraphPatterns.tp(hrIRI, AMS.has, propertyIri))
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(propertyInsertQuery).execute();
                }
            }
        }

        if (humanResourceInput.getProvidedCapabilities() != null) {
            for (CapabilityInput capability : humanResourceInput.getProvidedCapabilities()) {
                var childCapabilities = new LinkedList<IRI>();
                var parentCapabilities = new LinkedList<IRI>();
                if (capability.getParentCapabilities() != null) {
                    for (CapabilityInput parentCapability : capability.getParentCapabilities()) {
                        IRI parentCapabilityIRI =
                                this.createCapability(
                                                parentCapability,
                                                new LinkedList<>(),
                                                new LinkedList<>())
                                        .getId();
                        parentCapabilities.add(parentCapabilityIRI);
                    }
                }
                if (capability.getChildCapabilities() != null) {
                    for (CapabilityInput childCapability : capability.getChildCapabilities()) {
                        IRI childCapabilityIRI =
                                this.createCapability(
                                                childCapability,
                                                new LinkedList<>(),
                                                new LinkedList<>())
                                        .getId();
                        childCapabilities.add(childCapabilityIRI);
                    }
                }
                IRI capabilityIri =
                        this.createCapability(capability, parentCapabilities, childCapabilities)
                                .getId();
                this.addCapabilityToHumanResource(capabilityIri, hrIRI);
            }
        }

        if (humanResourceInput.getProvidedProcesses() != null) {
            for (ProcessInput process : humanResourceInput.getProvidedProcesses()) {
                IRI processIri =
                        this.createProcess(process, new LinkedList<>(), new LinkedList<>()).getId();
                this.addProvidedProcessToHumanResource(processIri, hrIRI);
            }
        }

        return this.getHumanResourceById(hrIRI);
    }

    public HumanResource updateHumanResource(
            IRI humanResourceId, HumanResourceInput humanResourceInput) {
        HumanResource humanResource = this.getHumanResourceById(humanResourceId);
        try (RepositoryConnection connection = repository.getConnection()) {
            if (humanResourceInput.getSourceId() != null) {
                connection.remove(humanResourceId, AMS.externalIdentifier, null);
                TriplePattern insert =
                        GraphPatterns.tp(
                                humanResourceId,
                                AMS.externalIdentifier,
                                Values.literal(humanResourceInput.getSourceId()));
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }
            if (humanResourceInput.getLabel() != null) {
                connection.remove(humanResourceId, RDFS.LABEL, null);
                if (humanResourceInput.getLabelLanguageCode() != null) {
                    TriplePattern insert =
                            GraphPatterns.tp(
                                    humanResourceId,
                                    RDFS.LABEL,
                                    Values.literal(
                                            humanResourceInput.getLabel(),
                                            humanResourceInput.getLabelLanguageCode()));
                    String queryText =
                            Queries.INSERT_DATA(insert)
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(queryText).execute();
                }
            } else if (humanResourceInput.getLabelLanguageCode() != null) {
                connection.remove(humanResourceId, RDFS.LABEL, null);
                TriplePattern insert =
                        GraphPatterns.tp(
                                humanResourceId,
                                RDFS.LABEL,
                                Values.literal(
                                        humanResource.getLabel(),
                                        humanResourceInput.getLabelLanguageCode()));
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }
            if (humanResourceInput.getDescription() != null) {
                connection.remove(humanResourceId, RDFS.COMMENT, null);
                if (humanResourceInput.getDescriptionLanguageCode() != null) {
                    TriplePattern insert =
                            GraphPatterns.tp(
                                    humanResourceId,
                                    RDFS.COMMENT,
                                    Values.literal(
                                            humanResourceInput.getDescription(),
                                            humanResourceInput.getDescriptionLanguageCode()));
                    String queryText =
                            Queries.INSERT_DATA(insert)
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(queryText).execute();
                }
            } else if (humanResourceInput.getDescriptionLanguageCode() != null) {
                connection.remove(humanResourceId, RDFS.COMMENT, null);
                TriplePattern insert =
                        GraphPatterns.tp(
                                humanResourceId,
                                RDFS.COMMENT,
                                Values.literal(
                                        humanResource.getDescription(),
                                        humanResourceInput.getDescriptionLanguageCode()));
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }

            if (humanResourceInput.getCertificates() != null) {
                connection.remove(humanResourceId, AMS.has, null);
                for (var certificate : humanResourceInput.getCertificates()) {
                    var cert = this.createProperty(certificate);
                    TriplePattern insert = GraphPatterns.tp(humanResourceId, AMS.has, cert.getId());
                    String queryText =
                            Queries.INSERT_DATA(insert)
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(queryText).execute();
                }
            }
        }
        return this.getHumanResourceById(humanResourceId);
    }

    public boolean deleteHumanResource(String humanResourceId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.remove(Values.iri(humanResourceId), null, null);
            if (this.getMachineById(Values.iri(humanResourceId)) != null) {
                return false;
            }
        }
        return true;
    }

    public HumanResource addUsedProcessToHumanResource(IRI processId, IRI humanResourceId) {
        String insertQuery =
                Queries.INSERT_DATA(GraphPatterns.tp(humanResourceId, AMS.usedBy, processId))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getHumanResourceById(humanResourceId);
    }

    public HumanResource addProvidedProcessToHumanResource(IRI processId, IRI humanResourceId) {
        String insertQuery =
                Queries.INSERT_DATA(GraphPatterns.tp(humanResourceId, AMS.provides, processId))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getHumanResourceById(humanResourceId);
    }

    public HumanResource removeProcessFromHumanResource(IRI processId, IRI humanResourceId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            // TODO: Add context.
            connection.remove(humanResourceId, AMS.usedBy, processId);
        }
        return this.getHumanResourceById(humanResourceId);
    }

    public HumanResource addCapabilityToHumanResource(IRI capabilityId, IRI humanResourceId) {
        String insertQuery =
                Queries.INSERT_DATA(GraphPatterns.tp(humanResourceId, AMS.provides, capabilityId))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getHumanResourceById(humanResourceId);
    }

    public HumanResource removeCapabilityFromHumanResource(IRI capabilityId, IRI humanResourceId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            // TODO: Add context.
            connection.remove(humanResourceId, AMS.provides, capabilityId);
        }
        return this.getHumanResourceById(humanResourceId);
    }

    public Product addPropertyToProduct(IRI propertyId, IRI productId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            String insertQuery =
                    Queries.INSERT_DATA(GraphPatterns.tp(productId, AMS.has, propertyId))
                            .into(this::getGraphNameForMutation)
                            .getQueryString();
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getProductById(productId);
    }

    public Property createPropertyForProduct(PropertyInput propertyInput, IRI productId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            IRI propertyIRI = this.getNewUUID();

            String insertQuery =
                    Queries.INSERT_DATA(
                                    GraphPatterns.tp(productId, AMS.has, propertyIRI),
                                    GraphPatterns.tp(propertyIRI, RDF.TYPE, AMS.Property)
                                            .andHas(
                                                    iri(AMS.externalIdentifier),
                                                    propertyInput.getSourceId())
                                            .andHas(
                                                    iri(RDFS.LABEL),
                                                    propertyInput.getLabel() == null
                                                            ? null
                                                            : Values.literal(
                                                                    propertyInput.getLabel(),
                                                                    propertyInput
                                                                            .getLabelLanguageCode()))
                                            .andHas(
                                                    iri(RDFS.COMMENT),
                                                    propertyInput.getDescription() == null
                                                            ? null
                                                            : Values.literal(
                                                                    propertyInput.getDescription(),
                                                                    propertyInput
                                                                            .getDescriptionLanguageCode()))
                                            .andHas(iri(AMS.value), propertyInput.getValue()))
                            .into(this::getGraphNameForMutation)
                            .getQueryString();
            connection.prepareUpdate(insertQuery).execute();
            if (propertyInput.getSemanticReferences() == null) {
                return this.getPropertyById(propertyIRI);
            }
            for (var semRef : propertyInput.getSemanticReferences()) {
                IRI semRefIRI = this.getNewUUID();

                String semRefQuery =
                        Queries.INSERT_DATA(
                                        GraphPatterns.tp(propertyIRI, AMS.hasSemantic, semRefIRI),
                                        GraphPatterns.tp(semRefIRI, RDF.TYPE, AMS.SemanticReference)
                                                .andHas(iri(AMS.identifier), semRef.getSourceUri())
                                                .andHas(
                                                        iri(RDFS.LABEL),
                                                        semRef.getLabel() == null
                                                                ? null
                                                                : Values.literal(
                                                                        semRef.getLabel(),
                                                                        semRef
                                                                                .getLabelLanguageCode()))
                                                .andHas(
                                                        iri(RDFS.COMMENT),
                                                        semRef.getDescription() == null
                                                                ? null
                                                                : Values.literal(
                                                                        semRef.getDescription(),
                                                                        semRef
                                                                                .getDescriptionLanguageCode())))
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(semRefQuery).execute();
            }
            return this.getPropertyById(propertyIRI);
        }
    }

    public Product removePropertyFromProduct(IRI propertyId, IRI productId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            // TODO: Add context.
            connection.remove(productId, AMS.has, propertyId);
        }
        return this.getProductById(productId);
    }

    public Product addProductClassToProduct(IRI productClassId, IRI productId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            String insertQuery =
                    Queries.INSERT_DATA(
                                    GraphPatterns.tp(productId, AMS.specializes, productClassId))
                            .into(this::getGraphNameForMutation)
                            .getQueryString();
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getProductById(productId);
    }

    public ProductClass createProductClassForProduct(
            ProductClassInput productClassInput, IRI productId) {
        var productClass = this.createAndInsertProductClass(productClassInput);
        try (RepositoryConnection connection = repository.getConnection()) {
            String insertQuery =
                    Queries.INSERT_DATA(
                                    GraphPatterns.tp(
                                            productId, AMS.specializes, productClass.getId()))
                            .into(this::getGraphNameForMutation)
                            .getQueryString();
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getProductClassById(productClass.getId());
    }

    public Product removeProductClassFromProduct(IRI productClassId, IRI productId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.remove(productId, AMS.specializes, productClassId);
        }
        return this.getProductById(productId);
    }

    public Product addSubProductToProduct(IRI subProductProductApplicationId, IRI productId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            String insertQuery =
                    Queries.INSERT_DATA(
                                    GraphPatterns.tp(
                                            productId,
                                            AMS.contains,
                                            subProductProductApplicationId))
                            .into(this::getGraphNameForMutation)
                            .getQueryString();
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getProductById(productId);
    }

    public ProductApplication createSubProductForProduct(
            ProductApplicationInput productApplicationInput, IRI productId) {
        var productApplication = this.createProductApplication(productApplicationInput);
        String insertQuery =
                Queries.INSERT_DATA(
                                GraphPatterns.tp(
                                        productId, iri(AMS.contains), productApplication.getId()))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getProductApplicationById(productApplication.getId());
    }

    public Product removeSubProductFromProduct(IRI subProductProductApplicationId, IRI productId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.remove(productId, AMS.contains, subProductProductApplicationId);
        }
        return this.getProductById(productId);
    }

    public Product addSemanticReferenceToProduct(IRI semanticReferenceId, IRI productId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            String insertQuery =
                    Queries.INSERT_DATA(
                                    GraphPatterns.tp(
                                            productId, AMS.hasSemantic, semanticReferenceId))
                            .into(this::getGraphNameForMutation)
                            .getQueryString();
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getProductById(productId);
    }

    public SemanticReference createSemanticReferenceForProduct(
            SemanticReferenceInput input, IRI productId) {
        var semanticReference = this.createSemanticReference(input);
        String insertQuery =
                Queries.INSERT_DATA(
                                GraphPatterns.tp(
                                        productId, iri(AMS.hasSemantic), semanticReference.getId()))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getSemanticReferenceById(semanticReference.getId());
    }

    public Product removeSemanticReferenceFromProduct(IRI semanticReferenceId, IRI productId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.remove(productId, AMS.hasSemantic, semanticReferenceId);
        }
        return this.getProductById(productId);
    }

    public Product addSupplyChainToProduct(IRI supplyChainId, IRI productId) {
        String insertQuery =
                Queries.INSERT_DATA(GraphPatterns.tp(productId, AMS.has, supplyChainId))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getProductById(productId);
    }

    public SupplyChain createSupplyChainForProduct(
            SupplyChainInput supplyChainInput, IRI productId) {
        var supplyChain = this.createSupplyChain(supplyChainInput);
        String insertQuery =
                Queries.INSERT_DATA(GraphPatterns.tp(productId, AMS.has, supplyChain.getId()))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getSupplyChainById(supplyChain.getId());
    }

    public Product removeSupplyChainFromProduct(IRI supplyChainId, IRI productId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.remove(productId, null, supplyChainId);
        }
        return this.getProductById(productId);
    }

    public ProductClass createAndInsertProductClass(ProductClassInput productClassInput) {
        IRI productClassIRI = this.getNewUUID();

        TriplePattern insertPattern = GraphPatterns.tp(productClassIRI, RDF.TYPE, AMS.ProductClass);
        if (productClassInput.getLabel() != null && !productClassInput.getLabel().isBlank()) {
            insertPattern =
                    insertPattern.andHas(
                            iri(RDFS.LABEL),
                            Values.literal(
                                    productClassInput.getLabel(),
                                    productClassInput.getLabelLanguageCode()));
        }

        if (productClassInput.getDescription() != null
                && !productClassInput.getDescription().isBlank()) {
            insertPattern =
                    insertPattern.andHas(
                            iri(RDFS.COMMENT),
                            Values.literal(
                                    productClassInput.getDescription(),
                                    productClassInput.getDescriptionLanguageCode()));
        }

        if (productClassInput.getSourceId() != null) {
            insertPattern =
                    insertPattern.andHas(
                            iri(AMS.externalIdentifier), productClassInput.getSourceId());
        }

        String insertQuery =
                Queries.INSERT_DATA(insertPattern)
                        .into(this::getGraphNameForMutation)
                        .getQueryString();

        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
            if (productClassInput.getSemanticReferences() == null) {
                return this.getProductClassById(productClassIRI);
            }
            for (var semRef : productClassInput.getSemanticReferences()) {
                var semRefCreated = this.createSemanticReference(semRef);

                String semRefQuery =
                        Queries.INSERT_DATA(
                                        GraphPatterns.tp(
                                                productClassIRI,
                                                AMS.hasSemantic,
                                                semRefCreated.getId()))
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(semRefQuery).execute();
            }
            return this.getProductClassById(productClassIRI);
        }
    }

    public ProductClass createProductClass(
            ProductClassInput productClassInput, List<IRI> parentIds, List<IRI> childIds) {
        if (productClassInput.getId() != null) {
            return this.getProductClassById(Values.iri(productClassInput.getId()));
        }
        var productClass = this.createAndInsertProductClass(productClassInput);
        try (RepositoryConnection connection = repository.getConnection()) {
            for (var parent : parentIds) {
                String parentQuery =
                        Queries.INSERT_DATA(
                                        GraphPatterns.tp(
                                                parent, AMS.generalizes, productClass.getId()),
                                        GraphPatterns.tp(
                                                productClass.getId(), AMS.specializes, parent))
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(parentQuery).execute();
            }

            for (var child : childIds) {
                String childQuery =
                        Queries.INSERT_DATA(
                                        GraphPatterns.tp(
                                                productClass.getId(), AMS.generalizes, child),
                                        GraphPatterns.tp(
                                                child, AMS.specializes, productClass.getId()))
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(childQuery).execute();
            }
        }
        return this.getProductClassById(productClass.getId());
    }

    public ProductClass updateProductClass(
            IRI productClassId,
            ProductClassInput productClassInput,
            List<IRI> parentIds,
            List<IRI> childIds) {
        ProductClass productClass = this.getProductClassById(productClassId);
        try (RepositoryConnection connection = repository.getConnection()) {
            if (productClassInput.getSourceId() != null) {
                connection.remove(productClassId, AMS.externalIdentifier, null);
                TriplePattern insert =
                        GraphPatterns.tp(
                                productClassId,
                                AMS.externalIdentifier,
                                Values.literal(productClassInput.getSourceId()));
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }
            if (productClassInput.getLabel() != null) {
                connection.remove(productClassId, RDFS.LABEL, null);
                if (productClassInput.getLabelLanguageCode() != null) {
                    TriplePattern insert =
                            GraphPatterns.tp(
                                    productClassId,
                                    RDFS.LABEL,
                                    Values.literal(
                                            productClassInput.getLabel(),
                                            productClassInput.getLabelLanguageCode()));
                    String queryText =
                            Queries.INSERT_DATA(insert)
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(queryText).execute();
                }
            } else if (productClassInput.getLabelLanguageCode() != null) {
                connection.remove(productClassId, RDFS.LABEL, null);
                TriplePattern insert =
                        GraphPatterns.tp(
                                productClassId,
                                RDFS.LABEL,
                                Values.literal(
                                        productClass.getLabel(),
                                        productClassInput.getLabelLanguageCode()));
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }
            if (productClassInput.getDescription() != null) {
                connection.remove(productClassId, RDFS.COMMENT, null);
                if (productClassInput.getDescriptionLanguageCode() != null) {
                    TriplePattern insert =
                            GraphPatterns.tp(
                                    productClassId,
                                    RDFS.COMMENT,
                                    Values.literal(
                                            productClassInput.getDescription(),
                                            productClassInput.getDescriptionLanguageCode()));
                    String queryText =
                            Queries.INSERT_DATA(insert)
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(queryText).execute();
                }
            } else if (productClassInput.getDescriptionLanguageCode() != null) {
                connection.remove(productClassId, RDFS.COMMENT, null);
                TriplePattern insert =
                        GraphPatterns.tp(
                                productClassId,
                                RDFS.COMMENT,
                                Values.literal(
                                        productClass.getDescription(),
                                        productClassInput.getDescriptionLanguageCode()));
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }

            if (productClassInput.getSemanticReferences() != null) {
                connection.remove(productClassId, AMS.hasSemantic, null);
                for (var semRef : productClassInput.getSemanticReferences()) {
                    var semantic = this.createSemanticReference(semRef);
                    TriplePattern insert =
                            GraphPatterns.tp(productClassId, AMS.hasSemantic, semantic.getId());
                    String queryText =
                            Queries.INSERT_DATA(insert)
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(queryText).execute();
                }
            }
            if (parentIds != null) {
                connection.remove(productClassId, AMS.specializes, null);
                for (var parent : parentIds) {
                    String parentQuery =
                            Queries.INSERT_DATA(
                                            GraphPatterns.tp(
                                                    parent, AMS.generalizes, productClassId),
                                            GraphPatterns.tp(
                                                    productClassId, AMS.specializes, parent))
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(parentQuery).execute();
                }
            }
            if (childIds != null) {
                connection.remove(productClassId, AMS.generalizes, null);
                for (var child : childIds) {
                    String childQuery =
                            Queries.INSERT_DATA(
                                            GraphPatterns.tp(
                                                    productClassId, AMS.generalizes, child),
                                            GraphPatterns.tp(
                                                    child, AMS.specializes, productClassId))
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(childQuery).execute();
                }
            }
        }
        return this.getProductClassById(productClassId);
    }

    public boolean deleteProductClass(IRI productClassId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.remove(productClassId, null, null);
            if (this.getProductClassById(productClassId) != null) {
                return false;
            }
        }
        return true;
    }

    public ProductClass addSemanticReferenceToProductClass(
            IRI semanticReferenceId, IRI productClassId) {
        String insertQuery =
                Queries.INSERT_DATA(
                                GraphPatterns.tp(
                                        productClassId, AMS.hasSemantic, semanticReferenceId))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getProductClassById(productClassId);
    }

    public SemanticReference createSemanticReferenceForProductClass(
            SemanticReferenceInput semanticReferenceInput, IRI productClassId) {
        var semRef = this.createSemanticReference(semanticReferenceInput);
        String insertQuery =
                Queries.INSERT_DATA(
                                GraphPatterns.tp(productClassId, AMS.hasSemantic, semRef.getId()))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getSemanticReferenceById(semRef.getId());
    }

    public ProductClass removeSemanticReferenceFromProductClass(
            IRI semanticReferenceId, IRI productClassId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.remove(productClassId, AMS.hasSemantic, semanticReferenceId);
        }
        return this.getProductClassById(productClassId);
    }

    public Capability createCapability(
            CapabilityInput capabilityInput, List<IRI> parentIds, List<IRI> childIds) {
        if (capabilityInput.getId() != null) {
            return this.getCapabilityById(Values.iri(capabilityInput.getId()));
        }
        IRI capabilityIRI = this.getNewUUID();

        TriplePattern insertPattern = GraphPatterns.tp(capabilityIRI, RDF.TYPE, AMS.Capability);
        if (capabilityInput.getLabel() != null && !capabilityInput.getLabel().isBlank()) {
            insertPattern =
                    insertPattern.andHas(
                            iri(RDFS.LABEL),
                            Values.literal(
                                    capabilityInput.getLabel(),
                                    capabilityInput.getLabelLanguageCode()));
        }

        if (capabilityInput.getDescription() != null
                && !capabilityInput.getDescription().isBlank()) {
            insertPattern =
                    insertPattern.andHas(
                            iri(RDFS.COMMENT),
                            Values.literal(
                                    capabilityInput.getDescription(),
                                    capabilityInput.getDescriptionLanguageCode()));
        }

        if (capabilityInput.getSourceId() != null) {
            insertPattern =
                    insertPattern.andHas(
                            iri(AMS.externalIdentifier), capabilityInput.getSourceId());
        }
        String insertQuery =
                Queries.INSERT_DATA(insertPattern)
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
            for (var parent : parentIds) {
                String parentQuery =
                        Queries.INSERT_DATA(
                                        GraphPatterns.tp(parent, AMS.generalizes, capabilityIRI),
                                        GraphPatterns.tp(capabilityIRI, AMS.specializes, parent))
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(parentQuery).execute();
            }

            for (var child : childIds) {
                String childQuery =
                        Queries.INSERT_DATA(
                                        GraphPatterns.tp(capabilityIRI, AMS.generalizes, child),
                                        GraphPatterns.tp(child, AMS.specializes, capabilityIRI))
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(childQuery).execute();
            }
            if (capabilityInput.getSemanticReferences() != null) {
                for (var semRef : capabilityInput.getSemanticReferences()) {
                    IRI semRefIRI = this.createSemanticReference(semRef).getId();
                    String semRefQuery =
                            Queries.INSERT_DATA(
                                            GraphPatterns.tp(
                                                    capabilityIRI, AMS.hasSemantic, semRefIRI))
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(semRefQuery).execute();
                }
            }

            if (capabilityInput.getProperties() != null) {
                for (PropertyInput property : capabilityInput.getProperties()) {
                    IRI propertyIRI = this.createProperty(property).getId();
                    this.addPropertyToCapability(propertyIRI, capabilityIRI);
                }
            }

            return this.getCapabilityById(capabilityIRI);
        }
    }

    public Capability updateCapability(
            IRI capabilityId,
            CapabilityInput capabilityInput,
            List<IRI> parentIds,
            List<IRI> childIds) {
        Capability capability = this.getCapabilityById(capabilityId);
        try (RepositoryConnection connection = repository.getConnection()) {
            if (capabilityInput.getSourceId() != null) {
                connection.remove(capabilityId, AMS.externalIdentifier, null);
                TriplePattern insert =
                        GraphPatterns.tp(
                                capabilityId,
                                AMS.externalIdentifier,
                                Values.literal(capabilityInput.getSourceId()));
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }
            if (capabilityInput.getLabel() != null) {
                connection.remove(capabilityId, RDFS.LABEL, null);
                if (capabilityInput.getLabelLanguageCode() != null) {
                    TriplePattern insert =
                            GraphPatterns.tp(
                                    capabilityId,
                                    RDFS.LABEL,
                                    Values.literal(
                                            capabilityInput.getLabel(),
                                            capabilityInput.getLabelLanguageCode()));
                    String queryText =
                            Queries.INSERT_DATA(insert)
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(queryText).execute();
                } else if (capabilityInput.getLabelLanguageCode() == null
                        && capability.getLabelLanguageCode() != null) {
                    TriplePattern insert =
                            GraphPatterns.tp(
                                    capabilityId,
                                    RDFS.LABEL,
                                    Values.literal(
                                            capabilityInput.getLabel(),
                                            capability.getLabelLanguageCode()));
                    String queryText =
                            Queries.INSERT_DATA(insert)
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(queryText).execute();
                }
            } else if (capabilityInput.getLabelLanguageCode() != null) {
                connection.remove(capabilityId, RDFS.LABEL, null);
                TriplePattern insert =
                        GraphPatterns.tp(
                                capabilityId,
                                RDFS.LABEL,
                                Values.literal(
                                        capability.getLabel(),
                                        capabilityInput.getLabelLanguageCode()));
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }
            if (capabilityInput.getDescription() != null) {
                connection.remove(capabilityId, RDFS.COMMENT, null);
                if (capabilityInput.getDescriptionLanguageCode() != null) {
                    TriplePattern insert =
                            GraphPatterns.tp(
                                    capabilityId,
                                    RDFS.COMMENT,
                                    Values.literal(
                                            capabilityInput.getDescription(),
                                            capabilityInput.getDescriptionLanguageCode()));
                    String queryText =
                            Queries.INSERT_DATA(insert)
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(queryText).execute();
                } else if (capabilityInput.getDescriptionLanguageCode() == null
                        && capability.getDescriptionLanguageCode() != null) {
                    TriplePattern insert =
                            GraphPatterns.tp(
                                    capabilityId,
                                    RDFS.COMMENT,
                                    Values.literal(
                                            capabilityInput.getDescription(),
                                            capability.getDescriptionLanguageCode()));
                    String queryText =
                            Queries.INSERT_DATA(insert)
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(queryText).execute();
                }
            } else if (capabilityInput.getDescriptionLanguageCode() != null) {
                connection.remove(capabilityId, RDFS.COMMENT, null);
                TriplePattern insert =
                        GraphPatterns.tp(
                                capabilityId,
                                RDFS.COMMENT,
                                Values.literal(
                                        capability.getDescription(),
                                        capabilityInput.getDescriptionLanguageCode()));
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }

            if (capabilityInput.getSemanticReferences() != null) {
                connection.remove(capabilityId, AMS.hasSemantic, null);
                for (var semRef : capabilityInput.getSemanticReferences()) {
                    var semantic = this.createSemanticReference(semRef);
                    TriplePattern insert =
                            GraphPatterns.tp(capabilityId, AMS.hasSemantic, semantic.getId());
                    String queryText =
                            Queries.INSERT_DATA(insert)
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(queryText).execute();
                }
            }

            if (parentIds != null) {
                connection.remove(capabilityId, AMS.specializes, null);
                for (var parent : parentIds) {
                    String parentQuery =
                            Queries.INSERT_DATA(
                                            GraphPatterns.tp(parent, AMS.generalizes, capabilityId),
                                            GraphPatterns.tp(capabilityId, AMS.specializes, parent))
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(parentQuery).execute();
                }
            }
            if (childIds != null) {
                connection.remove(capabilityId, AMS.generalizes, null);
                for (var child : childIds) {
                    String childQuery =
                            Queries.INSERT_DATA(
                                            GraphPatterns.tp(capabilityId, AMS.generalizes, child),
                                            GraphPatterns.tp(child, AMS.specializes, capabilityId))
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(childQuery).execute();
                }
            }
        }
        if (capabilityInput.getProperties() != null) {
            for (var property : capabilityInput.getProperties()) {
                if (property.getId() != null) {
                    var propertyId = Values.iri(property.getId());
                    this.updateProperty(propertyId, property);
                } else {
                    var propertyId = this.createProperty(property).getId();
                    this.addPropertyToCapability(propertyId, capabilityId);
                }
            }
        }
        return this.getCapabilityById(capabilityId);
    }

    public boolean deleteCapability(IRI capabilityId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.remove(capabilityId, null, null);
            if (this.getCapabilityById(capabilityId) != null) {
                return false;
            }
        }
        return true;
    }

    public Capability addPropertyToCapability(IRI propertyId, IRI capabilityId) {
        String insertQuery =
                Queries.INSERT_DATA(GraphPatterns.tp(capabilityId, AMS.has, propertyId))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getCapabilityById(capabilityId);
    }

    public Property createPropertyForCapability(PropertyInput propertyInput, IRI capabilityId) {
        var property = createProperty(propertyInput);
        String insertQuery =
                Queries.INSERT_DATA(GraphPatterns.tp(capabilityId, iri(AMS.has), property.getId()))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getPropertyById(property.getId());
    }

    public Capability removePropertyFromCapability(IRI propertyId, IRI capabilityId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.remove(capabilityId, AMS.has, propertyId);
        }
        return this.getCapabilityById(capabilityId);
    }

    public Capability addProcessToCapability(IRI processId, IRI capabilityId) {
        String insertQuery =
                Queries.INSERT_DATA(GraphPatterns.tp(capabilityId, AMS.realizedBy, processId))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getCapabilityById(capabilityId);
    }

    public Process createProcessForCapability(ProcessInput processInput, IRI capabilityId) {
        var process = this.createAndInsertProcess(processInput);
        String insertQuery =
                Queries.INSERT_DATA(GraphPatterns.tp(capabilityId, AMS.realizedBy, process.getId()))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getProcessById(process.getId());
    }

    public Capability removeProcessFromCapability(IRI processId, IRI capabilityId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.remove(capabilityId, AMS.realizedBy, processId);
        }
        return this.getCapabilityById(capabilityId);
    }

    public Capability addSemanticReferenceToCapability(IRI semanticReferenceId, IRI capabilityId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            String insertQuery =
                    Queries.INSERT_DATA(
                                    GraphPatterns.tp(
                                            capabilityId, AMS.hasSemantic, semanticReferenceId))
                            .into(this::getGraphNameForMutation)
                            .getQueryString();
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getCapabilityById(capabilityId);
    }

    public SemanticReference createSemanticReferenceForCapability(
            SemanticReferenceInput semanticReferenceInput, IRI capabilityId) {
        var semanticReference = this.createSemanticReference(semanticReferenceInput);
        String insertQuery =
                Queries.INSERT_DATA(
                                GraphPatterns.tp(
                                        capabilityId,
                                        iri(AMS.hasSemantic),
                                        semanticReference.getId()))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getSemanticReferenceById(semanticReference.getId());
    }

    public Capability removeSemanticReferenceFromCapability(
            IRI semanticReferenceId, IRI capabilityId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.remove(capabilityId, AMS.hasSemantic, semanticReferenceId);
        }
        return this.getCapabilityById(capabilityId);
    }

    public Capability addProductionResourceToCapability(
            IRI productionResourceId, IRI capabilityId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            String insertQuery =
                    Queries.INSERT_DATA(
                                    GraphPatterns.tp(
                                            capabilityId, AMS.providedBy, productionResourceId))
                            .into(this::getGraphNameForMutation)
                            .getQueryString();
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getCapabilityById(capabilityId);
    }

    public Capability removeProductionResourceFromCapability(
            IRI productionResourceId, IRI capabilityId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.remove(capabilityId, AMS.providedBy, productionResourceId);
        }
        return this.getCapabilityById(capabilityId);
    }

    public Process createAndInsertProcess(ProcessInput processInput) {
        IRI processIRI = this.getNewUUID();

        TriplePattern insertPattern = GraphPatterns.tp(processIRI, RDF.TYPE, AMS.Process);

        if (processInput.getDescription() != null && !processInput.getDescription().isBlank()) {
            insertPattern =
                    insertPattern.andHas(
                            iri(RDFS.COMMENT),
                            Values.literal(
                                    processInput.getDescription(),
                                    processInput.getDescriptionLanguageCode()));
        }

        if (processInput.getSourceId() != null) {
            insertPattern =
                    insertPattern.andHas(iri(AMS.externalIdentifier), processInput.getSourceId());
        }
        String insertQuery =
                Queries.INSERT_DATA(insertPattern)
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getProcessById(processIRI);
    }

    public Process createProcess(
            ProcessInput processInput, List<IRI> parentIds, List<IRI> childIds) {
        if (processInput.getId() != null) {
            return this.getProcessById(Values.iri(processInput.getId()));
        }
        var process = this.createAndInsertProcess(processInput);
        try (RepositoryConnection connection = repository.getConnection()) {
            for (var parent : parentIds) {
                String parentQuery =
                        Queries.INSERT_DATA(
                                        GraphPatterns.tp(parent, AMS.contains, process.getId()),
                                        GraphPatterns.tp(process.getId(), AMS.containedIn, parent))
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(parentQuery).execute();
            }

            for (var child : childIds) {
                String childQuery =
                        Queries.INSERT_DATA(
                                        GraphPatterns.tp(process.getId(), AMS.contains, child),
                                        GraphPatterns.tp(child, AMS.containedIn, process.getId()))
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(childQuery).execute();
            }

            if (processInput.getParentProcesses() != null) {
                for (ProcessInput parentProcess : processInput.getParentProcesses()) {
                    IRI parentIRI =
                            this.createProcess(
                                            parentProcess,
                                            new LinkedList<>(),
                                            Collections.singletonList(process.getId()))
                                    .getId();
                    String query =
                            Queries.INSERT_DATA(
                                            GraphPatterns.tp(
                                                    process.getId(), AMS.containedIn, parentIRI))
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(query).execute();
                }
            }

            if (processInput.getChildProcesses() != null) {
                for (ProcessInput childProcess : processInput.getChildProcesses()) {
                    IRI childIRI =
                            this.createProcess(
                                            childProcess,
                                            Collections.singletonList(process.getId()),
                                            new LinkedList<>())
                                    .getId();
                    String query =
                            Queries.INSERT_DATA(
                                            GraphPatterns.tp(
                                                    process.getId(), AMS.contains, childIRI))
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(query).execute();
                }
            }

            if (processInput.getProvidingMachines() != null) {
                for (MachineInput providingMachine : processInput.getProvidingMachines()) {
                    IRI machineIRI = this.createMachine(providingMachine).getId();
                    String query =
                            Queries.INSERT_DATA(
                                            GraphPatterns.tp(
                                                    process.getId(), AMS.providedBy, machineIRI))
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(query).execute();
                }
            }

            if (processInput.getProvidingHumanResources() != null) {
                for (HumanResourceInput hr : processInput.getProvidingHumanResources()) {
                    IRI hrIRI = this.createHumanResource(hr).getId();
                    String query =
                            Queries.INSERT_DATA(
                                            GraphPatterns.tp(
                                                    process.getId(), AMS.providedBy, hrIRI))
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(query).execute();
                }
            }
        }

        if (processInput.getProperties() != null) {
            for (PropertyInput property : processInput.getProperties()) {
                IRI propertyIRI = this.createProperty(property).getId();
                this.addPropertyToProcess(propertyIRI, process.getId());
            }
        }

        if (processInput.getPreliminaryProducts() != null) {
            for (ProductApplicationInput preliminary : processInput.getPreliminaryProducts()) {
                IRI preliminaryIRI = this.createProductApplication(preliminary).getId();
                this.addInputProductToProcess(
                        preliminaryIRI, InputProductType.PRELIMINARY_PRODUCT, process.getId());
            }
        }

        if (processInput.getRawMaterials() != null) {
            for (ProductApplicationInput raw : processInput.getRawMaterials()) {
                IRI rawIRI = this.createProductApplication(raw).getId();
                this.addInputProductToProcess(
                        rawIRI, InputProductType.RAW_MATERIAL, process.getId());
            }
        }

        if (processInput.getAuxiliaryMaterials() != null) {
            for (ProductApplicationInput auxiliary : processInput.getAuxiliaryMaterials()) {
                IRI auxiliaryIRI = this.createProductApplication(auxiliary).getId();
                this.addInputProductToProcess(
                        auxiliaryIRI, InputProductType.AUXILIARY_MATERIAL, process.getId());
            }
        }

        if (processInput.getOperatingMaterials() != null) {
            for (ProductApplicationInput operating : processInput.getOperatingMaterials()) {
                IRI operatingIRI = this.createProductApplication(operating).getId();
                this.addInputProductToProcess(
                        operatingIRI, InputProductType.OPERATING_MATERIAL, process.getId());
            }
        }

        if (processInput.getEndProducts() != null) {
            for (ProductApplicationInput output : processInput.getEndProducts()) {
                IRI outputIRI = this.createProductApplication(output).getId();
                this.addOutputProductToProcess(
                        outputIRI, OutputProductType.END_PRODUCT, process.getId());
            }
        }

        if (processInput.getByProducts() != null) {
            for (ProductApplicationInput output : processInput.getByProducts()) {
                IRI outputIRI = this.createProductApplication(output).getId();
                this.addOutputProductToProcess(
                        outputIRI, OutputProductType.BY_PRODUCT, process.getId());
            }
        }

        if (processInput.getWasteProducts() != null) {
            for (ProductApplicationInput output : processInput.getWasteProducts()) {
                IRI outputIRI = this.createProductApplication(output).getId();
                this.addOutputProductToProcess(
                        outputIRI, OutputProductType.WASTE_PRODUCT, process.getId());
            }
        }

        if (processInput.getInputProducts() != null) {
            for (ProductApplicationInput input : processInput.getInputProducts()) {
                IRI inputIRI = this.createProductApplication(input).getId();
                this.addInputProductToProcess(inputIRI, InputProductType.DEFAULT, process.getId());
            }
        }

        if (processInput.getOutputProducts() != null) {
            for (ProductApplicationInput output : processInput.getOutputProducts()) {
                IRI outputIRI = this.createProductApplication(output).getId();
                this.addOutputProductToProcess(
                        outputIRI, OutputProductType.DEFAULT, process.getId());
            }
        }

        if (processInput.getUsedMachines() != null) {
            for (MachineInput used : processInput.getUsedMachines()) {
                IRI usedIRI = this.createMachine(used).getId();
                this.addProductionResourceToProcess(usedIRI, process.getId());
            }
        }

        if (processInput.getUsedHumanResources() != null) {
            for (HumanResourceInput used : processInput.getUsedHumanResources()) {
                IRI usedIRI = this.createHumanResource(used).getId();
                this.addProductionResourceToProcess(usedIRI, process.getId());
            }
        }

        if (processInput.getRealizedCapabilities() != null) {
            for (CapabilityInput realized : processInput.getRealizedCapabilities()) {
                var childCapabilities = new LinkedList<IRI>();
                var parentCapabilities = new LinkedList<IRI>();
                if (realized.getParentCapabilities() != null) {
                    for (CapabilityInput parentCapability : realized.getParentCapabilities()) {
                        IRI parentCapabilityIRI =
                                this.createCapability(
                                                parentCapability,
                                                new LinkedList<>(),
                                                new LinkedList<>())
                                        .getId();
                        parentCapabilities.add(parentCapabilityIRI);
                    }
                }
                if (realized.getChildCapabilities() != null) {
                    for (CapabilityInput childCapability : realized.getChildCapabilities()) {
                        IRI childCapabilityIRI =
                                this.createCapability(
                                                childCapability,
                                                new LinkedList<>(),
                                                new LinkedList<>())
                                        .getId();
                        childCapabilities.add(childCapabilityIRI);
                    }
                }
                IRI realizedIRI =
                        this.createCapability(realized, parentCapabilities, childCapabilities)
                                .getId();
                this.addRealizedCapabilityToProcess(realizedIRI, process.getId());
            }
        }

        if (processInput.getRequiredCapabilities() != null) {
            for (CapabilityInput required : processInput.getRequiredCapabilities()) {
                var childCapabilities = new LinkedList<IRI>();
                var parentCapabilities = new LinkedList<IRI>();
                if (required.getParentCapabilities() != null) {
                    for (CapabilityInput parentCapability : required.getParentCapabilities()) {
                        IRI parentCapabilityIRI =
                                this.createCapability(
                                                parentCapability,
                                                new LinkedList<>(),
                                                new LinkedList<>())
                                        .getId();
                        parentCapabilities.add(parentCapabilityIRI);
                    }
                }
                if (required.getChildCapabilities() != null) {
                    for (CapabilityInput childCapability : required.getChildCapabilities()) {
                        IRI childCapabilityIRI =
                                this.createCapability(
                                                childCapability,
                                                new LinkedList<>(),
                                                new LinkedList<>())
                                        .getId();
                        childCapabilities.add(childCapabilityIRI);
                    }
                }
                IRI requiredIRI =
                        this.createCapability(required, parentCapabilities, childCapabilities)
                                .getId();
                this.addRequiredCapabilityToProcess(requiredIRI, process.getId());
            }
        }

        return this.getProcessById(process.getId());
    }

    public Process updateProcess(
            IRI processId, ProcessInput processInput, List<IRI> parentIds, List<IRI> childIds) {
        Process process = this.getProcessById(processId);
        try (RepositoryConnection connection = repository.getConnection()) {
            if (processInput.getSourceId() != null) {
                connection.remove(processId, AMS.externalIdentifier, null);
                TriplePattern insert =
                        GraphPatterns.tp(
                                processId,
                                AMS.externalIdentifier,
                                Values.literal(processInput.getSourceId()));
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }
            if (processInput.getDescription() != null) {
                connection.remove(processId, RDFS.COMMENT, null);
                if (processInput.getDescriptionLanguageCode() != null) {
                    TriplePattern insert =
                            GraphPatterns.tp(
                                    processId,
                                    RDFS.COMMENT,
                                    Values.literal(
                                            processInput.getDescription(),
                                            processInput.getDescriptionLanguageCode()));
                    String queryText =
                            Queries.INSERT_DATA(insert)
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(queryText).execute();
                }
            } else if (processInput.getDescriptionLanguageCode() != null) {
                connection.remove(processId, RDFS.COMMENT, null);
                TriplePattern insert =
                        GraphPatterns.tp(
                                processId,
                                RDFS.COMMENT,
                                Values.literal(
                                        process.getDescription(),
                                        processInput.getDescriptionLanguageCode()));
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }
            if (parentIds != null) {
                connection.remove(processId, AMS.containedIn, null);
                for (var parent : parentIds) {
                    String parentQuery =
                            Queries.INSERT_DATA(
                                            GraphPatterns.tp(parent, AMS.generalizes, processId),
                                            GraphPatterns.tp(processId, AMS.specializes, parent))
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(parentQuery).execute();
                }
            }
            if (childIds != null) {
                connection.remove(processId, AMS.contains, null);
                for (var child : childIds) {
                    String childQuery =
                            Queries.INSERT_DATA(
                                            GraphPatterns.tp(processId, AMS.contains, child),
                                            GraphPatterns.tp(child, AMS.containedIn, processId))
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(childQuery).execute();
                }
            }
        }
        return this.getProcessById(processId);
    }

    public boolean deleteProcess(IRI processId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.remove(processId, null, null);
            if (this.getProcessById(processId) != null) {
                return false;
            }
        }
        return true;
    }

    public Process addRealizedCapabilityToProcess(IRI capabilityId, IRI processId) {
        String insertQuery =
                Queries.INSERT_DATA(GraphPatterns.tp(processId, AMS.realizes, capabilityId))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getProcessById(processId);
    }

    public Process removeRealizedCapabilityFromProcess(IRI capabilityId, IRI processId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.remove(processId, AMS.realizes, capabilityId);
        }
        return this.getProcessById(processId);
    }

    public Process addRequiredCapabilityToProcess(IRI capabilityId, IRI processId) {
        String insertQuery =
                Queries.INSERT_DATA(GraphPatterns.tp(processId, AMS.requires, capabilityId))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getProcessById(processId);
    }

    public Process removeRequiredCapabilityFromProcess(IRI capabilityId, IRI processId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.remove(processId, AMS.requires, capabilityId);
        }
        return this.getProcessById(processId);
    }

    /**
     * Adds a used production resource to the process.
     * Process --uses--> ProductionResource
     * @param prId id of the productionResource
     * @param processId id of the process.
     * @return Process with the newly added production resource.
     */
    public Process addProductionResourceToProcess(IRI prId, IRI processId) {
        String insertQuery =
                Queries.INSERT_DATA(GraphPatterns.tp(processId, AMS.uses, prId))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getProcessById(processId);
    }

    public Process removeProductionResourceFromProcess(IRI prId, IRI processId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.remove(processId, AMS.uses, prId);
        }
        return this.getProcessById(processId);
    }

    public Process addInputProductToProcess(IRI productId, InputProductType type, IRI processId) {
        String insertQuery;
        switch (type) {
            case RAW_MATERIAL:
                insertQuery =
                        Queries.INSERT_DATA(
                                        GraphPatterns.tp(processId, AMS.hasRawMaterial, productId))
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                break;
            case AUXILIARY_MATERIAL:
                insertQuery =
                        Queries.INSERT_DATA(
                                        GraphPatterns.tp(
                                                processId, AMS.hasAuxiliaryMaterial, productId))
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                break;
            case OPERATING_MATERIAL:
                insertQuery =
                        Queries.INSERT_DATA(
                                        GraphPatterns.tp(
                                                processId, AMS.hasOperatingMaterial, productId))
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                break;
            case PRELIMINARY_PRODUCT:
                insertQuery =
                        Queries.INSERT_DATA(
                                        GraphPatterns.tp(
                                                processId, AMS.hasPreliminaryProduct, productId))
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                break;
            default:
                insertQuery =
                        Queries.INSERT_DATA(GraphPatterns.tp(processId, AMS.hasInput, productId))
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
        }
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getProcessById(processId);
    }

    public Process removeInputProductFromProcess(
            IRI productId, InputProductType type, IRI processId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            switch (type) {
                case PRELIMINARY_PRODUCT:
                    connection.remove(processId, AMS.hasPreliminaryProduct, productId);
                    break;
                case AUXILIARY_MATERIAL:
                    connection.remove(processId, AMS.hasAuxiliaryMaterial, productId);
                    break;
                case RAW_MATERIAL:
                    connection.remove(processId, AMS.hasRawMaterial, productId);
                    break;
                case OPERATING_MATERIAL:
                    connection.remove(processId, AMS.hasOperatingMaterial, productId);
                    break;
                default:
                    connection.remove(processId, AMS.hasInput, productId);
            }
        }
        return this.getProcessById(processId);
    }

    public Process addOutputProductToProcess(IRI productId, OutputProductType type, IRI processId) {
        String insertQuery;
        switch (type) {
            case BY_PRODUCT:
                insertQuery =
                        Queries.INSERT_DATA(
                                        GraphPatterns.tp(processId, AMS.hasByProduct, productId))
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                break;
            case END_PRODUCT:
                insertQuery =
                        Queries.INSERT_DATA(
                                        GraphPatterns.tp(processId, AMS.hasEndProduct, productId))
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                break;
            case WASTE_PRODUCT:
                insertQuery =
                        Queries.INSERT_DATA(
                                        GraphPatterns.tp(processId, AMS.hasWasteProduct, productId))
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                break;
            default:
                insertQuery =
                        Queries.INSERT_DATA(GraphPatterns.tp(processId, AMS.hasOutput, productId))
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
        }
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getProcessById(processId);
    }

    public Process removeOutputProductFromProcess(
            IRI productId, OutputProductType type, IRI processId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            switch (type) {
                case WASTE_PRODUCT:
                    connection.remove(processId, AMS.hasWasteProduct, productId);
                    break;
                case END_PRODUCT:
                    connection.remove(processId, AMS.hasEndProduct, productId);
                    break;
                case BY_PRODUCT:
                    connection.remove(processId, AMS.hasByProduct, productId);
                    break;
                default:
                    connection.remove(processId, AMS.hasOutput, productId);
            }
        }
        return this.getProcessById(processId);
    }

    public Process addPropertyToProcess(IRI propertyId, IRI processId) {
        String insertQuery =
                Queries.INSERT_DATA(GraphPatterns.tp(processId, AMS.has, propertyId))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getProcessById(propertyId);
    }

    public Property createPropertyForProcess(PropertyInput propertyInput, IRI processId) {
        var property = createProperty(propertyInput);
        String insertQuery =
                Queries.INSERT_DATA(GraphPatterns.tp(processId, iri(AMS.has), property.getId()))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getPropertyById(property.getId());
    }

    public Process removePropertyFromProcess(IRI propertyId, IRI processId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.remove(processId, AMS.has, propertyId);
        }
        return this.getProcessById(processId);
    }

    public SemanticReference createSemanticReference(
            SemanticReferenceInput semanticReferenceInput) {
        if (semanticReferenceInput.getId() != null) {
            return this.getSemanticReferenceById(Values.iri(semanticReferenceInput.getId()));
        }

        IRI semRefIRI = this.getNewUUID();

        TriplePattern insertPattern = GraphPatterns.tp(semRefIRI, RDF.TYPE, AMS.SemanticReference);
        if (semanticReferenceInput.getLabel() != null
                && !semanticReferenceInput.getLabel().isBlank()) {
            insertPattern =
                    insertPattern.andHas(
                            iri(RDFS.LABEL),
                            Values.literal(
                                    semanticReferenceInput.getLabel(),
                                    semanticReferenceInput.getLabelLanguageCode()));
        }

        if (semanticReferenceInput.getDescription() != null
                && !semanticReferenceInput.getDescription().isBlank()) {
            insertPattern =
                    insertPattern.andHas(
                            iri(RDFS.COMMENT),
                            Values.literal(
                                    semanticReferenceInput.getDescription(),
                                    semanticReferenceInput.getDescriptionLanguageCode()));
        }

        if (semanticReferenceInput.getSourceUri() != null) {
            insertPattern =
                    insertPattern.andHas(
                            iri(AMS.identifier), semanticReferenceInput.getSourceUri());
        }
        String insertQuery =
                Queries.INSERT_DATA(insertPattern)
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getSemanticReferenceById(semRefIRI);
    }

    public SemanticReference updateSemanticReference(
            IRI semRefId, SemanticReferenceInput semanticReferenceInput) {
        SemanticReference semanticReference = this.getSemanticReferenceById(semRefId);
        try (RepositoryConnection connection = repository.getConnection()) {
            if (semanticReferenceInput.getSourceUri() != null) {
                connection.remove(semRefId, AMS.externalIdentifier, null);
                TriplePattern insert =
                        GraphPatterns.tp(
                                semRefId,
                                AMS.externalIdentifier,
                                Values.literal(semanticReferenceInput.getSourceUri()));
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }
            if (semanticReferenceInput.getLabel() != null) {
                connection.remove(semRefId, RDFS.LABEL, null);
                if (semanticReferenceInput.getLabelLanguageCode() != null) {
                    TriplePattern insert =
                            GraphPatterns.tp(
                                    semRefId,
                                    RDFS.LABEL,
                                    Values.literal(
                                            semanticReferenceInput.getLabel(),
                                            semanticReferenceInput.getLabelLanguageCode()));
                    String queryText =
                            Queries.INSERT_DATA(insert)
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(queryText).execute();
                }
            } else if (semanticReferenceInput.getLabelLanguageCode() != null) {
                connection.remove(semRefId, RDFS.LABEL, null);
                TriplePattern insert =
                        GraphPatterns.tp(
                                semRefId,
                                RDFS.LABEL,
                                Values.literal(
                                        semanticReference.getLabel(),
                                        semanticReferenceInput.getLabelLanguageCode()));
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }
            if (semanticReferenceInput.getDescription() != null) {
                connection.remove(semRefId, RDFS.COMMENT, null);
                if (semanticReferenceInput.getDescriptionLanguageCode() != null) {
                    TriplePattern insert =
                            GraphPatterns.tp(
                                    semRefId,
                                    RDFS.COMMENT,
                                    Values.literal(
                                            semanticReferenceInput.getDescription(),
                                            semanticReferenceInput.getDescriptionLanguageCode()));
                    String queryText =
                            Queries.INSERT_DATA(insert)
                                    .into(this::getGraphNameForMutation)
                                    .getQueryString();
                    connection.prepareUpdate(queryText).execute();
                }
            } else if (semanticReferenceInput.getDescriptionLanguageCode() != null) {
                connection.remove(semRefId, RDFS.COMMENT, null);
                TriplePattern insert =
                        GraphPatterns.tp(
                                semRefId,
                                RDFS.COMMENT,
                                Values.literal(
                                        semanticReference.getDescription(),
                                        semanticReferenceInput.getDescriptionLanguageCode()));
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }
        }
        return this.getSemanticReferenceById(semRefId);
    }

    public boolean deleteSemanticReference(IRI semRefId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.remove(semRefId, null, null);
            if (this.getSemanticReferenceById(semRefId) != null) {
                return false;
            }
        }
        return true;
    }

    public ProductApplication createProductApplication(
            ProductApplicationInput productApplicationInput) {
        if (productApplicationInput.getId() != null) {
            return this.getProductApplicationById(Values.iri(productApplicationInput.getId()));
        }
        IRI productApplicationIRI = this.getNewUUID();
        TriplePattern insertPattern =
                GraphPatterns.tp(productApplicationIRI, RDF.TYPE, AMS.ProductApplication);

        if (productApplicationInput.getSourceId() != null) {
            insertPattern =
                    insertPattern.andHas(
                            iri(AMS.externalIdentifier), productApplicationInput.getSourceId());
        }

        String insertQuery =
                Queries.INSERT_DATA(insertPattern)
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
            if (productApplicationInput.getProduct() != null) {
                var product = this.createProduct(productApplicationInput.getProduct());
                String productQuery =
                        Queries.INSERT_DATA(
                                        GraphPatterns.tp(
                                                productApplicationIRI,
                                                iri(AMS.has),
                                                product.getId()))
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(productQuery).execute();
            }
            if (productApplicationInput.getQuantity() != null) {
                var quantity = this.createProperty(productApplicationInput.getQuantity());
                String quantityQuery =
                        Queries.INSERT_DATA(
                                        GraphPatterns.tp(
                                                productApplicationIRI,
                                                iri(AMS.has),
                                                quantity.getId()))
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(quantityQuery).execute();
            }
        }

        if (productApplicationInput.getProperties() != null) {
            for (PropertyInput property : productApplicationInput.getProperties()) {
                IRI propertyIRI = this.createProperty(property).getId();
                this.addPropertyToProductApplication(propertyIRI, productApplicationIRI);
            }
        }
        return this.getProductApplicationById(productApplicationIRI);
    }

    public ProductApplication updateProductApplication(
            IRI productApplicationId, ProductApplicationInput productApplicationInput) {
        ProductApplication productApplication =
                this.getProductApplicationById(productApplicationId);
        try (RepositoryConnection connection = repository.getConnection()) {
            if (productApplicationInput.getSourceId() != null) {
                connection.remove(productApplicationId, AMS.externalIdentifier, null);
                TriplePattern insert =
                        GraphPatterns.tp(
                                productApplicationId,
                                AMS.externalIdentifier,
                                Values.literal(productApplicationInput.getSourceId()));
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }
            if (productApplicationInput.getProduct() != null) {
                connection.remove(
                        productApplicationId, AMS.has, productApplication.getProduct().getId());
                var product = this.createProduct(productApplicationInput.getProduct());
                TriplePattern insert =
                        GraphPatterns.tp(productApplicationId, AMS.has, product.getId());
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }
            if (productApplicationInput.getQuantity() != null) {
                connection.remove(
                        productApplicationId, AMS.has, productApplication.getQuantity().getId());
                var quantity = this.createProperty(productApplicationInput.getQuantity());
                TriplePattern insert =
                        GraphPatterns.tp(productApplicationId, AMS.has, quantity.getId());
                String queryText =
                        Queries.INSERT_DATA(insert)
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(queryText).execute();
            }
        }
        return this.getProductApplicationById(productApplicationId);
    }

    public boolean deleteProductApplication(IRI productApplicationId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.remove(productApplicationId, null, null);
            if (this.getProductApplicationById(productApplicationId) != null) {
                return false;
            }
        }
        return true;
    }

    public ProductApplication addPropertyToProductApplication(
            IRI propertyId, IRI productApplicationId) {
        String insertQuery =
                Queries.INSERT_DATA(GraphPatterns.tp(productApplicationId, AMS.has, propertyId))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getProductApplicationById(productApplicationId);
    }

    public Property createPropertyForProductApplication(
            PropertyInput propertyInput, IRI productApplicationId) {
        var property = this.createProperty(propertyInput);
        String insertQuery =
                Queries.INSERT_DATA(
                                GraphPatterns.tp(productApplicationId, AMS.has, property.getId()))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getPropertyById(property.getId());
    }

    public ProductApplication removePropertyFromProductApplication(
            IRI propertyId, IRI productApplicationId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            // TODO: Add context.
            connection.remove(productApplicationId, AMS.has, propertyId);
        }
        return this.getProductApplicationById(productApplicationId);
    }

    public Location createAndInsertLocation(LocationInput locationInput) {
        IRI locationIRI = this.getNewUUID();

        String insertQuery =
                Queries.INSERT_DATA(
                                GraphPatterns.tp(locationIRI, RDF.TYPE, iri(AMS.Location))
                                        .andHas(iri(AMS.latitude), locationInput.getLatitude())
                                        .andHas(iri(AMS.longitude), locationInput.getLongitude())
                                        .andHas(iri(AMS.street), locationInput.getStreet())
                                        .andHas(
                                                iri(AMS.streetNumber),
                                                locationInput.getStreetNumber())
                                        .andHas(iri(AMS.zipcode), locationInput.getZip())
                                        .andHas(iri(AMS.city), locationInput.getCity())
                                        .andHas(iri(AMS.country), locationInput.getCountry()))
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate(insertQuery).execute();
        }
        return this.getLocationById(locationIRI);
    }

    public Location updateLocation(IRI locationId, LocationInput locationInput) {
        try (RepositoryConnection connection = repository.getConnection()) {
            if (locationInput.getCity() != null) {
                connection.remove(locationId, AMS.city, null);
                String updateQuery =
                        Queries.INSERT_DATA(
                                        GraphPatterns.tp(
                                                locationId,
                                                AMS.city,
                                                Values.literal(locationInput.getCity())))
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(updateQuery).execute();
            }

            if (locationInput.getCountry() != null) {
                connection.remove(locationId, AMS.country, null);
                String updateQuery =
                        Queries.INSERT_DATA(
                                        GraphPatterns.tp(
                                                locationId,
                                                AMS.country,
                                                Values.literal(locationInput.getCountry())))
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(updateQuery).execute();
            }

            if (locationInput.getLatitude() != null) {
                connection.remove(locationId, AMS.latitude, null);
                String updateQuery =
                        Queries.INSERT_DATA(
                                        GraphPatterns.tp(
                                                locationId,
                                                AMS.latitude,
                                                Values.literal(locationInput.getLatitude())))
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(updateQuery).execute();
            }

            if (locationInput.getLongitude() != null) {
                connection.remove(locationId, AMS.longitude, null);
                String updateQuery =
                        Queries.INSERT_DATA(
                                        GraphPatterns.tp(
                                                locationId,
                                                AMS.longitude,
                                                Values.literal(locationInput.getLongitude())))
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(updateQuery).execute();
            }

            if (locationInput.getStreet() != null) {
                connection.remove(locationId, AMS.street, null);
                String updateQuery =
                        Queries.INSERT_DATA(
                                        GraphPatterns.tp(
                                                locationId,
                                                AMS.street,
                                                Values.literal(locationInput.getStreet())))
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(updateQuery).execute();
            }

            if (locationInput.getStreetNumber() != null) {
                connection.remove(locationId, AMS.streetNumber, null);
                String updateQuery =
                        Queries.INSERT_DATA(
                                        GraphPatterns.tp(
                                                locationId,
                                                AMS.streetNumber,
                                                Values.literal(locationInput.getStreetNumber())))
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(updateQuery).execute();
            }

            if (locationInput.getZip() != null) {
                connection.remove(locationId, AMS.zipcode, null);
                String updateQuery =
                        Queries.INSERT_DATA(
                                        GraphPatterns.tp(
                                                locationId,
                                                AMS.zipcode,
                                                Values.literal(locationInput.getZip())))
                                .into(this::getGraphNameForMutation)
                                .getQueryString();
                connection.prepareUpdate(updateQuery).execute();
            }
        }

        return this.getLocationById(locationId);
    }

    public boolean deleteLocation(IRI locationId) {
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.remove(locationId, null, null);
            if (this.getLocationById(locationId) != null) {
                return false;
            }
        }
        return true;
    }

    public static Location createLocation(LocationInput locationInput)
            throws IllegalArgumentException {

        Location location = new Location();

        location.setLatitude(locationInput.getLatitude());
        location.setLongitude(locationInput.getLongitude());
        location.setStreet(locationInput.getStreet());
        location.setStreetNumber(locationInput.getStreetNumber());
        location.setCity(locationInput.getCity());
        location.setCountry(locationInput.getCountry());
        location.setZip(locationInput.getZip());
        return location;
    }

    public String createInsertMachineQuery(MachineInput machineInput) {
        IRI machineIri = this.getNewUUID();
        String returnPattern = null;
        TriplePattern insertPattern = GraphPatterns.tp(machineIri, RDF.TYPE, AMS.Machine);
        TriplesTemplate allTriples = SparqlBuilder.triplesTemplate(insertPattern);

        if (machineInput.getLabel() != null && !machineInput.getLabel().isBlank()) {
            insertPattern =
                    insertPattern.andHas(
                            iri(RDFS.LABEL),
                            Values.literal(
                                    machineInput.getLabel(), machineInput.getLabelLanguageCode()));
        }

        if (machineInput.getDescription() != null && !machineInput.getDescription().isBlank()) {
            insertPattern =
                    insertPattern.andHas(
                            iri(RDFS.COMMENT),
                            Values.literal(
                                    machineInput.getDescription(),
                                    machineInput.getDescriptionLanguageCode()));
        }

        if (machineInput.getSourceId() != null) {
            insertPattern =
                    insertPattern.andHas(iri(AMS.externalIdentifier), machineInput.getSourceId());
        }
        if (machineInput.getMachineProperties() != null) {
            for (PropertyInput propertyInput : machineInput.getMachineProperties()) {
                IRI propertyIri = this.getNewUUID();
                TriplePattern propertyPattern =
                        GraphPatterns.tp(propertyIri, RDF.TYPE, AMS.Property);
                if (propertyInput.getLabel() != null && !propertyInput.getLabel().isBlank()) {
                    propertyPattern =
                            propertyPattern.andHas(
                                    iri(RDFS.LABEL),
                                    Values.literal(
                                            propertyInput.getLabel(),
                                            propertyInput.getLabelLanguageCode()));
                }

                if (propertyInput.getDescription() != null
                        && !propertyInput.getDescription().isBlank()) {
                    propertyPattern =
                            propertyPattern.andHas(
                                    iri(RDFS.COMMENT),
                                    Values.literal(
                                            propertyInput.getDescription(),
                                            propertyInput.getDescriptionLanguageCode()));
                }

                if (propertyInput.getSourceId() != null) {
                    propertyPattern =
                            propertyPattern.andHas(
                                    iri(AMS.externalIdentifier), propertyInput.getSourceId());
                }

                if (propertyInput.getValue() != null) {
                    propertyPattern =
                            propertyPattern.andHas(iri(AMS.value), propertyInput.getValue());
                }
                if (propertyInput.getSemanticReferences() != null) {
                    for (var semanticReferenceInput : propertyInput.getSemanticReferences()) {
                        IRI semRefIRI = this.getNewUUID();

                        TriplePattern semRefPattern =
                                GraphPatterns.tp(semRefIRI, RDF.TYPE, AMS.SemanticReference);
                        if (semanticReferenceInput.getLabel() != null
                                && !semanticReferenceInput.getLabel().isBlank()) {
                            semRefPattern =
                                    semRefPattern.andHas(
                                            iri(RDFS.LABEL),
                                            Values.literal(
                                                    semanticReferenceInput.getLabel(),
                                                    semanticReferenceInput.getLabelLanguageCode()));
                        }

                        if (semanticReferenceInput.getDescription() != null
                                && !semanticReferenceInput.getDescription().isBlank()) {
                            semRefPattern =
                                    semRefPattern.andHas(
                                            iri(RDFS.COMMENT),
                                            Values.literal(
                                                    semanticReferenceInput.getDescription(),
                                                    semanticReferenceInput
                                                            .getDescriptionLanguageCode()));
                        }

                        if (semanticReferenceInput.getSourceUri() != null) {
                            semRefPattern =
                                    semRefPattern.andHas(
                                            iri(AMS.identifier),
                                            semanticReferenceInput.getSourceUri());
                        }
                        propertyPattern.andHas(iri(AMS.SemanticReference), semRefIRI);
                        allTriples = allTriples.and(semRefPattern);
                    }
                }
                insertPattern = insertPattern.andHas(iri(AMS.has), propertyIri);
                allTriples = allTriples.and(propertyPattern);
            }
        }

        if (machineInput.getProvidedCapabilities() != null) {
            for (CapabilityInput capabilityInput : machineInput.getProvidedCapabilities()) {
                this.createInsertCapabilityQuery(
                        insertPattern, allTriples, capabilityInput, AMS.provides);
            }
        }
        if (machineInput.getProvidedProcesses() != null) {
            for (ProcessInput processInput : machineInput.getProvidedProcesses()) {
                this.createInsertProcessQuery(
                        insertPattern, allTriples, processInput, AMS.provides);
            }
        }

        if (machineInput.getUsingProcesses() != null) {
            for (ProcessInput processInput : machineInput.getUsingProcesses()) {
                this.createInsertProcessQuery(insertPattern, allTriples, processInput, AMS.uses);
            }
        }
        returnPattern =
                Queries.INSERT_DATA(allTriples)
                        .into(this::getGraphNameForMutation)
                        .getQueryString();
        return returnPattern;
    }

    public TriplesTemplate createInsertMachineQueryToFactory(
            TriplesTemplate allTriples, MachineInput machineInput, IRI factoryIri) {
        IRI machineIri = this.getNewUUID();
        TriplePattern insertPattern = GraphPatterns.tp(machineIri, RDF.TYPE, AMS.Machine);
        allTriples = allTriples.and(insertPattern);

        if (machineInput.getLabel() != null && !machineInput.getLabel().isBlank()) {
            insertPattern =
                    insertPattern.andHas(
                            iri(RDFS.LABEL),
                            Values.literal(
                                    machineInput.getLabel(), machineInput.getLabelLanguageCode()));
        }

        if (machineInput.getDescription() != null && !machineInput.getDescription().isBlank()) {
            insertPattern =
                    insertPattern.andHas(
                            iri(RDFS.COMMENT),
                            Values.literal(
                                    machineInput.getDescription(),
                                    machineInput.getDescriptionLanguageCode()));
        }

        if (machineInput.getSourceId() != null) {
            insertPattern =
                    insertPattern.andHas(iri(AMS.externalIdentifier), machineInput.getSourceId());
        }
        if (machineInput.getProvidedCapabilities() != null) {
            for (CapabilityInput capabilityInput : machineInput.getProvidedCapabilities()) {
                this.createInsertCapabilityQuery(
                        insertPattern, allTriples, capabilityInput, AMS.provides);
            }
        }
        if (machineInput.getMachineProperties() != null) {
            for (PropertyInput propertyInput : machineInput.getMachineProperties()) {
                this.createInsertPropertyQuery(insertPattern, allTriples, propertyInput);
            }
        }
        if (machineInput.getProvidedProcesses() != null) {
            for (ProcessInput processInput : machineInput.getProvidedProcesses()) {
                this.createInsertProcessQuery(
                        insertPattern, allTriples, processInput, AMS.provides);
            }
        }

        if (machineInput.getUsingProcesses() != null) {
            for (ProcessInput processInput : machineInput.getUsingProcesses()) {
                this.createInsertProcessQuery(insertPattern, allTriples, processInput, AMS.uses);
            }
        }
        TriplePattern factory = GraphPatterns.tp(factoryIri, AMS.contains, machineIri);
        allTriples = allTriples.and(factory);
        return allTriples;
    }

    public void createInsertMachineQueryTo(
            TriplePattern insertPattern,
            TriplesTemplate allTriples,
            MachineInput machineInput,
            IRI relationType) {
        IRI machineIri = this.getNewUUID();
        TriplePattern machinePattern = GraphPatterns.tp(machineIri, RDF.TYPE, AMS.Machine);

        if (machineInput.getLabel() != null && !machineInput.getLabel().isBlank()) {
            machinePattern =
                    machinePattern.andHas(
                            iri(RDFS.LABEL),
                            Values.literal(
                                    machineInput.getLabel(), machineInput.getLabelLanguageCode()));
        }

        if (machineInput.getDescription() != null && !machineInput.getDescription().isBlank()) {
            machinePattern =
                    machinePattern.andHas(
                            iri(RDFS.COMMENT),
                            Values.literal(
                                    machineInput.getDescription(),
                                    machineInput.getDescriptionLanguageCode()));
        }

        if (machineInput.getSourceId() != null) {
            machinePattern =
                    machinePattern.andHas(iri(AMS.externalIdentifier), machineInput.getSourceId());
        }
        if (machineInput.getProvidedCapabilities() != null) {
            for (CapabilityInput capabilityInput : machineInput.getProvidedCapabilities()) {
                this.createInsertCapabilityQuery(
                        machinePattern, allTriples, capabilityInput, AMS.provides);
            }
        }
        if (machineInput.getMachineProperties() != null) {
            for (PropertyInput propertyInput : machineInput.getMachineProperties()) {
                this.createInsertPropertyQuery(machinePattern, allTriples, propertyInput);
            }
        }

        if (machineInput.getProvidedProcesses() != null) {
            for (ProcessInput processInput : machineInput.getProvidedProcesses()) {
                this.createInsertProcessQuery(
                        machinePattern, allTriples, processInput, AMS.provides);
            }
        }

        if (machineInput.getUsingProcesses() != null) {
            for (ProcessInput processInput : machineInput.getUsingProcesses()) {
                this.createInsertProcessQuery(machinePattern, allTriples, processInput, AMS.uses);
            }
        }
        insertPattern = insertPattern.andHas(iri(relationType), machineIri);
        allTriples = allTriples.and(machinePattern);
    }

    public void createInsertPropertyQuery(
            TriplePattern insertPattern, TriplesTemplate allTriples, PropertyInput propertyInput) {
        IRI propertyIri = this.getNewUUID();
        TriplePattern propertyPattern = GraphPatterns.tp(propertyIri, RDF.TYPE, AMS.Property);
        if (propertyInput.getLabel() != null && !propertyInput.getLabel().isBlank()) {
            propertyPattern =
                    propertyPattern.andHas(
                            iri(RDFS.LABEL),
                            Values.literal(
                                    propertyInput.getLabel(),
                                    propertyInput.getLabelLanguageCode()));
        }

        if (propertyInput.getDescription() != null && !propertyInput.getDescription().isBlank()) {
            propertyPattern =
                    propertyPattern.andHas(
                            iri(RDFS.COMMENT),
                            Values.literal(
                                    propertyInput.getDescription(),
                                    propertyInput.getDescriptionLanguageCode()));
        }

        if (propertyInput.getSourceId() != null) {
            propertyPattern =
                    propertyPattern.andHas(
                            iri(AMS.externalIdentifier), propertyInput.getSourceId());
        }

        if (propertyInput.getValue() != null) {
            propertyPattern = propertyPattern.andHas(iri(AMS.value), propertyInput.getValue());
        }
        if (propertyInput.getSemanticReferences() != null) {
            for (var semanticReferenceInput : propertyInput.getSemanticReferences()) {
                this.createInsertSemanticReferenceQuery(
                        propertyPattern, allTriples, semanticReferenceInput);
            }
        }
        insertPattern = insertPattern.andHas(iri(AMS.has), propertyIri);
        allTriples = allTriples.and(propertyPattern);
    }

    public void createInsertSemanticReferenceQuery(
            TriplePattern insertPattern,
            TriplesTemplate allTriples,
            SemanticReferenceInput semanticReferenceInput) {
        IRI semRefIRI = this.getNewUUID();

        TriplePattern semRefPattern = GraphPatterns.tp(semRefIRI, RDF.TYPE, AMS.SemanticReference);
        if (semanticReferenceInput.getLabel() != null
                && !semanticReferenceInput.getLabel().isBlank()) {
            semRefPattern =
                    semRefPattern.andHas(
                            iri(RDFS.LABEL),
                            Values.literal(
                                    semanticReferenceInput.getLabel(),
                                    semanticReferenceInput.getLabelLanguageCode()));
        }

        if (semanticReferenceInput.getDescription() != null
                && !semanticReferenceInput.getDescription().isBlank()) {
            semRefPattern =
                    semRefPattern.andHas(
                            iri(RDFS.COMMENT),
                            Values.literal(
                                    semanticReferenceInput.getDescription(),
                                    semanticReferenceInput.getDescriptionLanguageCode()));
        }

        if (semanticReferenceInput.getSourceUri() != null) {
            semRefPattern =
                    semRefPattern.andHas(
                            iri(AMS.identifier), semanticReferenceInput.getSourceUri());
        }
        allTriples = allTriples.and(semRefPattern);
        insertPattern.andHas(iri(AMS.hasSemantic), semRefIRI);
    }

    public void createInsertCapabilityQuery(
            TriplePattern insertPattern,
            TriplesTemplate allTriples,
            CapabilityInput capabilityInput,
            IRI relationType) {
        IRI capabilityIRI = null;
        if (capabilityInput.getId() != null) {
            capabilityIRI = Values.iri(capabilityInput.getId());
        }
        capabilityIRI = this.getNewUUID();

        TriplePattern capabilityPattern = GraphPatterns.tp(capabilityIRI, RDF.TYPE, AMS.Capability);
        if (capabilityInput.getLabel() != null && !capabilityInput.getLabel().isBlank()) {
            capabilityPattern =
                    capabilityPattern.andHas(
                            iri(RDFS.LABEL),
                            Values.literal(
                                    capabilityInput.getLabel(),
                                    capabilityInput.getLabelLanguageCode()));
        }

        if (capabilityInput.getDescription() != null
                && !capabilityInput.getDescription().isBlank()) {
            capabilityPattern =
                    capabilityPattern.andHas(
                            iri(RDFS.COMMENT),
                            Values.literal(
                                    capabilityInput.getDescription(),
                                    capabilityInput.getDescriptionLanguageCode()));
        }

        if (capabilityInput.getSourceId() != null) {
            capabilityPattern =
                    capabilityPattern.andHas(
                            iri(AMS.externalIdentifier), capabilityInput.getSourceId());
        }
        if (capabilityInput.getParentCapabilities() != null) {
            for (CapabilityInput parentCapability : capabilityInput.getParentCapabilities()) {
                if (parentCapability.getId() == null) {
                    parentCapability.setId(this.getNewUUID().stringValue());
                    this.createInsertCapabilityQuery(
                            insertPattern, allTriples, parentCapability, null);
                }
                capabilityPattern =
                        capabilityPattern.andHas(
                                iri(AMS.specializes), Values.iri(parentCapability.getId()));
            }
        }
        if (capabilityInput.getChildCapabilities() != null) {
            for (CapabilityInput childCapability : capabilityInput.getChildCapabilities()) {
                if (childCapability.getId() == null) {
                    childCapability.setId(this.getNewUUID().stringValue());
                    this.createInsertCapabilityQuery(
                            insertPattern, allTriples, childCapability, null);
                }
                capabilityPattern =
                        capabilityPattern.andHas(
                                iri(AMS.generalizes), Values.iri(childCapability.getId()));
            }
        }
        if (capabilityInput.getSemanticReferences() != null) {
            for (var semanticReferenceInput : capabilityInput.getSemanticReferences()) {
                IRI semRefIRI = this.getNewUUID();

                TriplePattern semRefPattern =
                        GraphPatterns.tp(semRefIRI, RDF.TYPE, AMS.SemanticReference);
                if (semanticReferenceInput.getLabel() != null
                        && !semanticReferenceInput.getLabel().isBlank()) {
                    semRefPattern =
                            semRefPattern.andHas(
                                    iri(RDFS.LABEL),
                                    Values.literal(
                                            semanticReferenceInput.getLabel(),
                                            semanticReferenceInput.getLabelLanguageCode()));
                }

                if (semanticReferenceInput.getDescription() != null
                        && !semanticReferenceInput.getDescription().isBlank()) {
                    semRefPattern =
                            semRefPattern.andHas(
                                    iri(RDFS.COMMENT),
                                    Values.literal(
                                            semanticReferenceInput.getDescription(),
                                            semanticReferenceInput.getDescriptionLanguageCode()));
                }

                if (semanticReferenceInput.getSourceUri() != null) {
                    semRefPattern =
                            semRefPattern.andHas(
                                    iri(AMS.identifier), semanticReferenceInput.getSourceUri());
                }
                capabilityPattern.andHas(iri(AMS.SemanticReference), semRefIRI);
                allTriples = allTriples.and(semRefPattern);
            }
        }
        if (capabilityInput.getProperties() != null) {
            for (PropertyInput propertyInput : capabilityInput.getProperties()) {
                this.createInsertPropertyQuery(capabilityPattern, allTriples, propertyInput);
            }
        }
        if (relationType != null) {
            insertPattern = insertPattern.andHas(iri(relationType), capabilityIRI);
        }
        allTriples = allTriples.and(capabilityPattern);
    }

    public void createInsertProcessQuery(
            TriplePattern insertPattern,
            TriplesTemplate allTriples,
            ProcessInput processInput,
            IRI relationType) {
        IRI processIRI = this.getNewUUID();

        TriplePattern processPattern = GraphPatterns.tp(processIRI, RDF.TYPE, AMS.Process);

        if (processInput.getDescription() != null && !processInput.getDescription().isBlank()) {
            processPattern =
                    processPattern.andHas(
                            iri(RDFS.COMMENT),
                            Values.literal(
                                    processInput.getDescription(),
                                    processInput.getDescriptionLanguageCode()));
        }

        if (processInput.getSourceId() != null) {
            processPattern =
                    processPattern.andHas(iri(AMS.externalIdentifier), processInput.getSourceId());
        }

        if (processInput.getParentProcesses() != null) {
            for (ProcessInput parentProcess : processInput.getParentProcesses()) {
                if (parentProcess.getId() == null) {
                    parentProcess.setId(this.getNewUUID().stringValue());
                    this.createInsertProcessQuery(insertPattern, allTriples, parentProcess, null);
                }
                processPattern =
                        processPattern.andHas(
                                iri(AMS.containedIn), Values.iri(parentProcess.getId()));
            }
        }

        if (processInput.getChildProcesses() != null) {
            for (ProcessInput childProcess : processInput.getChildProcesses()) {
                if (childProcess.getId() == null) {
                    childProcess.setId(this.getNewUUID().stringValue());
                    this.createInsertProcessQuery(insertPattern, allTriples, childProcess, null);
                }
                processPattern =
                        processPattern.andHas(iri(AMS.contains), Values.iri(childProcess.getId()));
            }
        }
        if (processInput.getProperties() != null) {
            for (PropertyInput propertyInput : processInput.getProperties()) {
                this.createInsertPropertyQuery(processPattern, allTriples, propertyInput);
            }
        }
        if (processInput.getRequiredCapabilities() != null) {
            for (CapabilityInput capabilityInput : processInput.getRequiredCapabilities()) {
                this.createInsertCapabilityQuery(
                        processPattern, allTriples, capabilityInput, AMS.requires);
            }
        }
        if (processInput.getRealizedCapabilities() != null) {
            for (CapabilityInput capabilityInput : processInput.getRealizedCapabilities()) {
                this.createInsertCapabilityQuery(
                        processPattern, allTriples, capabilityInput, AMS.realizes);
            }
        }

        if (processInput.getPreliminaryProducts() != null) {
            for (ProductApplicationInput preliminary : processInput.getPreliminaryProducts()) {
                this.createInsertProductApplicationQuery(
                        processPattern, allTriples, preliminary, AMS.hasPreliminaryProduct);
            }
        }

        if (processInput.getRawMaterials() != null) {
            for (ProductApplicationInput raw : processInput.getRawMaterials()) {
                this.createInsertProductApplicationQuery(
                        processPattern, allTriples, raw, AMS.hasRawMaterial);
            }
        }

        if (processInput.getAuxiliaryMaterials() != null) {
            for (ProductApplicationInput auxiliary : processInput.getAuxiliaryMaterials()) {
                this.createInsertProductApplicationQuery(
                        processPattern, allTriples, auxiliary, AMS.hasAuxiliaryMaterial);
            }
        }

        if (processInput.getOperatingMaterials() != null) {
            for (ProductApplicationInput operating : processInput.getOperatingMaterials()) {
                this.createInsertProductApplicationQuery(
                        processPattern, allTriples, operating, AMS.hasOperatingMaterial);
            }
        }

        if (processInput.getEndProducts() != null) {
            for (ProductApplicationInput output : processInput.getEndProducts()) {
                this.createInsertProductApplicationQuery(
                        processPattern, allTriples, output, AMS.hasEndProduct);
            }
        }

        if (processInput.getByProducts() != null) {
            for (ProductApplicationInput output : processInput.getByProducts()) {
                this.createInsertProductApplicationQuery(
                        processPattern, allTriples, output, AMS.hasByProduct);
            }
        }

        if (processInput.getWasteProducts() != null) {
            for (ProductApplicationInput output : processInput.getWasteProducts()) {
                this.createInsertProductApplicationQuery(
                        processPattern, allTriples, output, AMS.hasWasteProduct);
            }
        }

        if (processInput.getInputProducts() != null) {
            for (ProductApplicationInput input : processInput.getInputProducts()) {
                this.createInsertProductApplicationQuery(
                        processPattern, allTriples, input, AMS.hasInput);
            }
        }

        if (processInput.getOutputProducts() != null) {
            for (ProductApplicationInput output : processInput.getOutputProducts()) {
                this.createInsertProductApplicationQuery(
                        processPattern, allTriples, output, AMS.hasOutput);
            }
        }

        if (processInput.getProvidingMachines() != null) {
            for (MachineInput machine : processInput.getProvidingMachines()) {
                this.createInsertMachineQueryTo(
                        processPattern, allTriples, machine, AMS.providedBy);
            }
        }

        if (processInput.getUsedMachines() != null) {
            for (MachineInput machine : processInput.getProvidingMachines()) {
                this.createInsertMachineQueryTo(processPattern, allTriples, machine, AMS.uses);
            }
        }

        // if (processInput.getProvidingHumanResources() != null) {
        //     for (HumanResourceInput hr : processInput.getProvidingHumanResources()) {
        //         // this.createInsertMachineQueryTo(processPattern, allTriples, machine,
        //         // AMS.providedBy);
        //     }
        // }

        // if (processInput.getUsedHumanResources() != null) {
        //     for (HumanResourceInput hr : processInput.getUsedHumanResources()) {
        //         // this.createInsertMachineQueryTo(processPattern, allTriples, machine, AMS.uses);
        //     }
        // }
        insertPattern = insertPattern.andHas(iri(relationType), processIRI);
        allTriples = allTriples.and(processPattern);
    }

    public void createInsertProductApplicationQuery(
            TriplePattern insertPattern,
            TriplesTemplate allTriples,
            ProductApplicationInput productApplicationInput,
            IRI relationType) {
        IRI productApplicationIRI = this.getNewUUID();
        TriplePattern productApplicationPattern =
                GraphPatterns.tp(productApplicationIRI, RDF.TYPE, AMS.ProductApplication);

        if (productApplicationInput.getSourceId() != null) {
            productApplicationPattern =
                    productApplicationPattern.andHas(
                            iri(AMS.externalIdentifier), productApplicationInput.getSourceId());
        }

        if (productApplicationInput.getProperties() != null) {
            for (PropertyInput propertyInput : productApplicationInput.getProperties()) {
                this.createInsertPropertyQuery(
                        productApplicationPattern, allTriples, propertyInput);
            }
        }

        if (productApplicationInput.getQuantity() != null) {
            this.createInsertPropertyQuery(
                    productApplicationPattern, allTriples, productApplicationInput.getQuantity());
        }

        if (productApplicationInput.getProduct() != null) {
            this.createInsertProductQuery(
                    productApplicationPattern, allTriples, productApplicationInput.getProduct());
        }
        insertPattern = insertPattern.andHas(iri(relationType), productApplicationIRI);
        allTriples = allTriples.and(productApplicationPattern);
    }

    public void createInsertProductQuery(
            TriplePattern insertPattern, TriplesTemplate allTriples, ProductInput productInput) {
        IRI productInputIRI = this.getNewUUID();
        TriplePattern productPattern = GraphPatterns.tp(productInputIRI, RDF.TYPE, AMS.Product);
        if (productInput.getLabel() != null && !productInput.getLabel().isBlank()) {
            productPattern =
                    productPattern.andHas(
                            iri(RDFS.LABEL),
                            Values.literal(
                                    productInput.getLabel(), productInput.getLabelLanguageCode()));
        }

        if (productInput.getDescription() != null && !productInput.getDescription().isBlank()) {
            productPattern =
                    productPattern.andHas(
                            iri(RDFS.COMMENT),
                            Values.literal(
                                    productInput.getDescription(),
                                    productInput.getDescriptionLanguageCode()));
        }

        if (productInput.getSourceId() != null) {
            productPattern =
                    productPattern.andHas(iri(AMS.externalIdentifier), productInput.getSourceId());
        }

        if (productInput.getBillOfMaterials() != null) {
            for (ProductApplicationInput bom : productInput.getBillOfMaterials()) {
                this.createInsertProductApplicationQuery(
                        productPattern, allTriples, bom, AMS.contains);
            }
        }

        if (productInput.getProperties() != null) {
            for (PropertyInput property : productInput.getProperties()) {
                this.createInsertPropertyQuery(productPattern, allTriples, property);
            }
        }

        if (productInput.getSemanticReferences() != null) {
            for (SemanticReferenceInput semanticReferenceInput :
                    productInput.getSemanticReferences()) {
                this.createInsertSemanticReferenceQuery(
                        productPattern, allTriples, semanticReferenceInput);
            }
        }

        if (productInput.getSupplyChains() != null) {
            for (SupplyChainInput supplyChainInput : productInput.getSupplyChains()) {
                this.createInsertSupplyChainQuery(productPattern, allTriples, supplyChainInput);
            }
        }

        if (productInput.getProductPassportInput() != null) {
            this.createInsertProductPassportQuery(
                    productPattern, allTriples, productInput.getProductPassportInput());
        }
        insertPattern = insertPattern.andHas(iri(AMS.has), productInputIRI);
        allTriples = allTriples.and(productPattern);
    }

    public void createInsertProductPassportQuery(
            TriplePattern insertPattern,
            TriplesTemplate allTriples,
            ProductPassportInput productPassportInput) {
        IRI productPassportId = this.getNewUUID();
        TriplePattern productPassportPattern =
                GraphPatterns.tp(productPassportId, RDF.TYPE, AMS.ProductPassport);

        if (productPassportInput.getSourceId() != null) {
            productPassportPattern =
                    productPassportPattern.andHas(
                            iri(AMS.externalIdentifier), productPassportInput.getSourceId());
        }
        if (productPassportInput.getIdentifier() != null) {
            productPassportPattern =
                    productPassportPattern.andHas(
                            iri(AMS.identifier), productPassportInput.getIdentifier());
        }
        if (productPassportInput.getProperties() != null) {
            for (PropertyInput propertyInput : productPassportInput.getProperties()) {
                this.createInsertPropertyQuery(productPassportPattern, allTriples, propertyInput);
            }
        }

        insertPattern = insertPattern.andHas(iri(AMS.has), productPassportId);
        allTriples = allTriples.and(productPassportPattern);
    }

    public void createInsertSupplyChainQuery(
            TriplePattern insertPattern,
            TriplesTemplate allTriples,
            SupplyChainInput supplyChainInput) {
        IRI supplyChainIRI = this.getNewUUID();
        TriplePattern supplyChainPattern =
                GraphPatterns.tp(supplyChainIRI, RDF.TYPE, AMS.SupplyChain);

        if (supplyChainInput.getDescription() != null
                && !supplyChainInput.getDescription().isBlank()) {
            supplyChainPattern =
                    supplyChainPattern.andHas(
                            iri(RDFS.COMMENT),
                            Values.literal(
                                    supplyChainInput.getDescription(),
                                    supplyChainInput.getDescriptionLanguageCode()));
        }

        if (supplyChainInput.getSourceId() != null) {
            supplyChainPattern =
                    supplyChainPattern.andHas(
                            iri(AMS.externalIdentifier), supplyChainInput.getSourceId());
        }

        if (supplyChainInput.getSuppliers() != null) {
            // TODO
        }
        insertPattern = insertPattern.andHas(iri(AMS.has), supplyChainIRI);
        allTriples = allTriples.and(supplyChainPattern);
    }

    public RDF4JTemplate getRdf4JTemplate() {
        return rdf4JTemplate;
    }

    private IRI getNewUUID() {
        IRI uuid =
                rdf4JTemplate
                        .tupleQuery("SELECT (UUID() as ?id) WHERE {}")
                        .evaluateAndConvert()
                        .toSingleton((b) -> QueryResultUtils.getIRI(b, "id"));
        // This value is assigned to 9 because the uuids are in form urn:uuid:xxxxxxxxxx
        int beginningIndex = 9;
        String trimmedUuid = uuid.stringValue().substring(beginningIndex);

        // Hashtag is for separating uuid from graph name.
        String finalUuid = DOMAIN_NAME + security.getGroupName() + "#" + trimmedUuid;

        return Values.iri(finalUuid);
    }

    public void deleteAll() {
        try (RepositoryConnection connection = repository.getConnection()) {
            connection.prepareUpdate("CLEAR GRAPH " + getGraphNameForMutation()).execute();
        }
    }

    // Add / to the end if needed.
    public Dataset getGraphNameForQuery() {
        if (security.isCanRead()) {
            return new Dataset().from(Values.iri(DOMAIN_NAME));
        }
        return new Dataset().from(Values.iri(DOMAIN_NAME + security.getGroupName()));
    }

    public String getGraphNameForMutation() {
        return "<" + DOMAIN_NAME + security.getGroupName() + ">";
    }
}
