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
import de.fraunhofer.iosb.ilt.ams.model.ObjectRdf4jRepository;
import de.fraunhofer.iosb.ilt.ams.model.Process;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
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
public class ProcessDAO extends RDF4JCRUDDao<Process, Process, IRI> {
    public static final GraphPatternNotTriples sourceId =
            GraphPatterns.optional(
                    Process.PROCESS_ID.has(AMS.externalIdentifier, Process.PROCESS_SOURCE_ID));
    public static final GraphPatternNotTriples description =
            GraphPatterns.optional(
                    Process.PROCESS_ID.has(RDFS.COMMENT, Process.PROCESS_DESCRIPTION));

    public static final GraphPatternNotTriples propertyPattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Process.PROCESS_ID.has(iri(AMS.has), Process.PROCESS_PROPERTY),
                            Process.PROCESS_PROPERTY.isA(AMS.Property)));

    public static final GraphPatternNotTriples parentPattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Process.PROCESS_ID.has(iri(AMS.containedIn), Process.PROCESS_PARENT),
                            Process.PROCESS_PARENT.isA(AMS.Process)));

    public static final GraphPatternNotTriples childPattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Process.PROCESS_ID.has(iri(AMS.contains), Process.PROCESS_CHILD),
                            Process.PROCESS_CHILD.isA(AMS.Process)));

    public static final GraphPatternNotTriples realizedCapabilities =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Process.PROCESS_ID.has(iri(AMS.realizes), Process.PROCESS_CAPABILITY),
                            Process.PROCESS_CAPABILITY.isA(AMS.Capability)));

    public static final GraphPatternNotTriples requiredCapabilities =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Process.PROCESS_ID.has(
                                    iri(AMS.requires), Process.PROCESS_REQ_CAPABILITY),
                            Process.PROCESS_REQ_CAPABILITY.isA(AMS.Capability)));

    public static final GraphPatternNotTriples preliminaryProduct =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Process.PROCESS_ID.has(
                                    iri(AMS.hasPreliminaryProduct),
                                    Process.PROCESS_PRELIMINARY_PRODUCT),
                            GraphPatterns.union(
                                    Process.PROCESS_PRELIMINARY_PRODUCT.isA(AMS.Product),
                                    Process.PROCESS_PRELIMINARY_PRODUCT.isA(
                                            AMS.ProductApplication))));
    public static final GraphPatternNotTriples rawMaterial =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Process.PROCESS_ID.has(
                                    iri(AMS.hasRawMaterial), Process.PROCESS_RAW_MATERIAL),
                            GraphPatterns.union(
                                    Process.PROCESS_RAW_MATERIAL.isA(AMS.Product),
                                    Process.PROCESS_RAW_MATERIAL.isA(AMS.ProductApplication))));
    public static final GraphPatternNotTriples auxiliaryMaterial =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Process.PROCESS_ID.has(
                                    iri(AMS.hasAuxiliaryMaterial),
                                    Process.PROCESS_AUXILIARY_MATERIAL),
                            GraphPatterns.union(
                                    Process.PROCESS_AUXILIARY_MATERIAL.isA(AMS.Product),
                                    Process.PROCESS_AUXILIARY_MATERIAL.isA(
                                            AMS.ProductApplication))));

    public static final GraphPatternNotTriples operatingMaterial =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Process.PROCESS_ID.has(
                                    iri(AMS.hasOperatingMaterial),
                                    Process.PROCESS_OPERATING_MATERIAL),
                            GraphPatterns.union(
                                    Process.PROCESS_OPERATING_MATERIAL.isA(AMS.Product),
                                    Process.PROCESS_OPERATING_MATERIAL.isA(
                                            AMS.ProductApplication))));
    public static final GraphPatternNotTriples endProduct =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Process.PROCESS_ID.has(
                                    iri(AMS.hasEndProduct), Process.PROCESS_END_PRODUCT),
                            GraphPatterns.union(
                                    Process.PROCESS_END_PRODUCT.isA(AMS.Product),
                                    Process.PROCESS_END_PRODUCT.isA(AMS.ProductApplication))));
    public static final GraphPatternNotTriples byProduct =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Process.PROCESS_ID.has(
                                    iri(AMS.hasByProduct), Process.PROCESS_BY_PRODUCT),
                            GraphPatterns.union(
                                    Process.PROCESS_BY_PRODUCT.isA(AMS.Product),
                                    Process.PROCESS_BY_PRODUCT.isA(AMS.ProductApplication))));
    public static final GraphPatternNotTriples wasteProduct =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Process.PROCESS_ID.has(
                                    iri(AMS.hasWasteProduct), Process.PROCESS_WASTE_PRODUCT),
                            GraphPatterns.union(
                                    Process.PROCESS_WASTE_PRODUCT.isA(AMS.Product),
                                    Process.PROCESS_WASTE_PRODUCT.isA(AMS.ProductApplication))));

    public static final GraphPatternNotTriples usedProductionResources =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Process.PROCESS_ID.has(iri(AMS.uses), Process.PROCESS_USED),
                            GraphPatterns.union(
                                    Process.PROCESS_USED.isA(AMS.HumanResource),
                                    Process.PROCESS_USED.isA(AMS.Machine))));
    public static final GraphPatternNotTriples providingProductionResources =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Process.PROCESS_ID.has(iri(AMS.providedBy), Process.PROCESS_PROVIDING),
                            GraphPatterns.union(
                                    Process.PROCESS_PROVIDING.isA(AMS.HumanResource),
                                    Process.PROCESS_PROVIDING.isA(AMS.Machine))));

    @Autowired ObjectRdf4jRepository repo;
    List<Process> processList;

    public ProcessDAO(RDF4JTemplate rdf4JTemplate) {
        super(rdf4JTemplate);
    }

    @Override
    protected void populateIdBindings(MutableBindings bindingsBuilder, IRI iri) {
        bindingsBuilder.add(Process.PROCESS_ID, iri);
    }

    @Override
    protected NamedSparqlSupplierPreparer prepareNamedSparqlSuppliers(
            NamedSparqlSupplierPreparer preparer) {
        return null;
    }

    @Override
    protected Process mapSolution(BindingSet querySolution) {
        Process process = null;

        for (Process p : processList) {
            if (p.getId().equals(QueryResultUtils.getIRI(querySolution, Process.PROCESS_ID))) {
                process = p;
                break;
            }
        }
        if (process == null) {
            process = new Process();
            mapProcess(querySolution, process);
            processList.add(process);
        }
        var property = QueryResultUtils.getIRIMaybe(querySolution, Process.PROCESS_PROPERTY);
        if (property != null) {
            process.addProperty(repo.getPropertyById(property));
        }
        var parent = QueryResultUtils.getIRIMaybe(querySolution, Process.PROCESS_PARENT);
        if (parent != null) {
            process.addParentProcess(repo.getProcessById(parent));
        }
        var child = QueryResultUtils.getIRIMaybe(querySolution, Process.PROCESS_CHILD);
        if (child != null) {
            process.addChildProcess(repo.getProcessById(child));
        }
        var realizedCapabilities =
                QueryResultUtils.getIRIMaybe(querySolution, Process.PROCESS_CAPABILITY);
        if (realizedCapabilities != null) {
            process.addRealizedCapability(repo.getCapabilityById(realizedCapabilities));
        }
        var requiredCapability =
                QueryResultUtils.getIRIMaybe(querySolution, Process.PROCESS_REQ_CAPABILITY);
        if (requiredCapability != null) {
            process.addRequiredCapability(repo.getCapabilityById(requiredCapability));
        }
        var preliminary =
                QueryResultUtils.getIRIMaybe(querySolution, Process.PROCESS_PRELIMINARY_PRODUCT);
        if (preliminary != null) {
            process.addPreliminaryProduct(repo.getProductApplicationById(preliminary));
        }
        var raw = QueryResultUtils.getIRIMaybe(querySolution, Process.PROCESS_RAW_MATERIAL);
        if (raw != null) {
            process.addRawMaterial(repo.getProductApplicationById(raw));
        }
        var auxiliary =
                QueryResultUtils.getIRIMaybe(querySolution, Process.PROCESS_AUXILIARY_MATERIAL);
        if (auxiliary != null) {
            process.addAuxiliaryMaterial(repo.getProductApplicationById(auxiliary));
        }
        var operating =
                QueryResultUtils.getIRIMaybe(querySolution, Process.PROCESS_OPERATING_MATERIAL);
        if (operating != null) {
            process.addOperatingMaterial(repo.getProductApplicationById(operating));
        }
        var end = QueryResultUtils.getIRIMaybe(querySolution, Process.PROCESS_END_PRODUCT);
        if (end != null) {
            process.addEndProduct(repo.getProductApplicationById(end));
        }
        var by = QueryResultUtils.getIRIMaybe(querySolution, Process.PROCESS_BY_PRODUCT);
        if (by != null) {
            process.addByProduct(repo.getProductApplicationById(by));
        }
        var waste = QueryResultUtils.getIRIMaybe(querySolution, Process.PROCESS_WASTE_PRODUCT);
        if (waste != null) {
            process.addWasteProduct(repo.getProductApplicationById(waste));
        }
        var usedProductionResource =
                QueryResultUtils.getIRIMaybe(querySolution, Process.PROCESS_USED);
        if (usedProductionResource != null) {
            process.addUsedProductionResource(
                    repo.getProductionResourceById(usedProductionResource));
        }
        var providingProductionResource =
                QueryResultUtils.getIRIMaybe(querySolution, Process.PROCESS_PROVIDING);
        if (providingProductionResource != null) {
            process.addProvidingProductionResource(
                    repo.getProductionResourceById(providingProductionResource));
        }
        process.appendInputProduct(process.getPreliminaryProducts());
        process.appendInputProduct(process.getAuxiliaryMaterials());
        process.appendInputProduct(process.getOperatingMaterials());
        process.appendInputProduct(process.getRawMaterials());

        process.appendOutputProduct(process.getByProducts());
        process.appendOutputProduct(process.getEndProducts());
        process.appendOutputProduct(process.getWasteProducts());
        return process;
    }

    @Override
    protected String getReadQuery() {
        processList = new LinkedList<>();
        return getProcessSelectQuery(null).from(repo.getGraphNameForQuery()).getQueryString();
    }

    public static void mapProcess(BindingSet querySolution, Process process) {
        process.setId(QueryResultUtils.getIRI(querySolution, Process.PROCESS_ID));
        process.setSourceId(
                QueryResultUtils.getStringMaybe(querySolution, Process.PROCESS_SOURCE_ID));
        var description =
                QueryResultUtils.getValueMaybe(querySolution, Process.PROCESS_DESCRIPTION);
        if (description != null) {
            process.setDescription(description.stringValue());
            if (description.isLiteral() && ((Literal) description).getLanguage().isPresent()) {
                process.setDescriptionLanguageCode(((Literal) description).getLanguage().get());
            }
        }
    }

    public static SelectQuery getProcessSelectQuery(String iri) {
        SelectQuery selectQuery =
                Queries.SELECT(
                                Process.PROCESS_ID,
                                Process.PROCESS_SOURCE_ID,
                                Process.PROCESS_DESCRIPTION,
                                Process.PROCESS_PROPERTY,
                                Process.PROCESS_PARENT,
                                Process.PROCESS_CHILD,
                                Process.PROCESS_CAPABILITY,
                                Process.PROCESS_REQ_CAPABILITY,
                                Process.PROCESS_PRELIMINARY_PRODUCT,
                                Process.PROCESS_RAW_MATERIAL,
                                Process.PROCESS_AUXILIARY_MATERIAL,
                                Process.PROCESS_OPERATING_MATERIAL,
                                Process.PROCESS_END_PRODUCT,
                                Process.PROCESS_BY_PRODUCT,
                                Process.PROCESS_WASTE_PRODUCT,
                                Process.PROCESS_USED,
                                Process.PROCESS_PROVIDING)
                        .where(
                                Process.PROCESS_ID
                                        .isA(iri(AMS.Process))
                                        .and(sourceId)
                                        .and(description)
                                        .and(propertyPattern)
                                        .and(parentPattern)
                                        .and(childPattern)
                                        .and(realizedCapabilities)
                                        .and(requiredCapabilities)
                                        .and(preliminaryProduct)
                                        .and(rawMaterial)
                                        .and(auxiliaryMaterial)
                                        .and(operatingMaterial)
                                        .and(endProduct)
                                        .and(byProduct)
                                        .and(wasteProduct)
                                        .and(usedProductionResources)
                                        .and(providingProductionResources));
        if (iri != null) {
            return selectQuery
                    .groupBy(
                            Process.PROCESS_ID,
                            Process.PROCESS_SOURCE_ID,
                            Process.PROCESS_DESCRIPTION,
                            Process.PROCESS_PROPERTY,
                            Process.PROCESS_PARENT,
                            Process.PROCESS_CHILD,
                            Process.PROCESS_CAPABILITY,
                            Process.PROCESS_REQ_CAPABILITY,
                            Process.PROCESS_PRELIMINARY_PRODUCT,
                            Process.PROCESS_RAW_MATERIAL,
                            Process.PROCESS_AUXILIARY_MATERIAL,
                            Process.PROCESS_OPERATING_MATERIAL,
                            Process.PROCESS_END_PRODUCT,
                            Process.PROCESS_BY_PRODUCT,
                            Process.PROCESS_WASTE_PRODUCT,
                            Process.PROCESS_USED,
                            Process.PROCESS_PROVIDING)
                    .having(Expressions.equals(Process.PROCESS_ID, iri(iri)));
        }
        return selectQuery;
    }

    @Override
    protected NamedSparqlSupplier getInsertSparql(Process process) {
        TriplePattern childPatternProcess =
                GraphPatterns.tp(Process.PROCESS_ID, iri(AMS.contains), Process.PROCESS_CHILD);
        for (int i = 1; i < process.getChildProcesses().size(); i++) {
            childPatternProcess =
                    childPatternProcess.andHas(
                            iri(AMS.contains),
                            SparqlBuilder.var(Process.PROCESS_CHILD.getVarName() + i));
        }

        TriplePattern parentPatternProcess =
                GraphPatterns.tp(Process.PROCESS_ID, iri(AMS.containedIn), Process.PROCESS_PARENT);
        for (int i = 1; i < process.getParentProcesses().size(); i++) {
            parentPatternProcess =
                    parentPatternProcess.andHas(
                            iri(AMS.containedIn),
                            SparqlBuilder.var(Process.PROCESS_PARENT.getVarName() + i));
        }
        TriplePattern finalChildPattern = childPatternProcess;
        TriplePattern finalParentPattern = parentPatternProcess;
        return NamedSparqlSupplier.of(
                KEY_PREFIX_INSERT,
                () ->
                        Queries.INSERT(
                                        Process.PROCESS_ID
                                                .isA(iri(AMS.Process))
                                                .andHas(
                                                        iri(AMS.externalIdentifier),
                                                        Process.PROCESS_SOURCE_ID)
                                                .andHas(
                                                        iri(RDFS.COMMENT),
                                                        Process.PROCESS_DESCRIPTION),
                                        finalChildPattern,
                                        finalParentPattern)
                                .getQueryString());
    }

    @Override
    protected IRI getInputId(Process process) {
        if (process.getId() == null) {
            return getRdf4JTemplate().getNewUUID();
        }
        return process.getId();
    }
}
