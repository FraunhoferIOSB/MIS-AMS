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
import de.fraunhofer.iosb.ilt.ams.model.SupplyChain;
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
public class SupplyChainDAO extends RDF4JCRUDDao<SupplyChain, SupplyChain, IRI> {

    public static final GraphPatternNotTriples sourceId =
            GraphPatterns.optional(
                    SupplyChain.SUPPLY_CHAIN_ID.has(
                            AMS.externalIdentifier, SupplyChain.SUPPLY_CHAIN_SOURCE_ID));
    public static final GraphPatternNotTriples description =
            GraphPatterns.optional(
                    SupplyChain.SUPPLY_CHAIN_ID.has(
                            RDFS.COMMENT, SupplyChain.SUPPLY_CHAIN_DESCRIPTION));
    public static final GraphPatternNotTriples supplier =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            SupplyChain.SUPPLY_CHAIN_ID.has(
                                    iri(AMS.contains), SupplyChain.SUPPLY_CHAIN_SUPPLIER),
                            SupplyChain.SUPPLY_CHAIN_SUPPLIER.isA(AMS.SupplyChainElement)));

    @Autowired ObjectRdf4jRepository repo;

    private List<SupplyChain> supplyChainList;

    public SupplyChainDAO(RDF4JTemplate rdf4JTemplate) {
        super(rdf4JTemplate);
    }

    @Override
    protected void populateIdBindings(MutableBindings bindingsBuilder, IRI iri) {
        bindingsBuilder.add(SupplyChain.SUPPLY_CHAIN_ID, iri);
    }

    @Override
    protected NamedSparqlSupplierPreparer prepareNamedSparqlSuppliers(
            NamedSparqlSupplierPreparer preparer) {
        return null;
    }

    @Override
    protected SupplyChain mapSolution(BindingSet querySolution) {
        SupplyChain supplyChain = null;
        for (SupplyChain sc : supplyChainList) {
            if (sc.getId()
                    .equals(QueryResultUtils.getIRI(querySolution, SupplyChain.SUPPLY_CHAIN_ID))) {
                supplyChain = sc;
                break;
            }
        }
        if (supplyChain == null) {
            supplyChain = new SupplyChain();
            mapSupplyChain(querySolution, supplyChain);
            supplyChainList.add(supplyChain);
        }
        var supplierQuery =
                QueryResultUtils.getIRIMaybe(querySolution, SupplyChain.SUPPLY_CHAIN_SUPPLIER);
        if (supplierQuery != null) {
            supplyChain.addSupplier(repo.getSupplyChainElementById(supplierQuery));
        }
        return supplyChain;
    }

    @Override
    protected String getReadQuery() {
        supplyChainList = new LinkedList<>();
        return getSupplyChainSelectQuery(null).from(repo.getGraphNameForQuery()).getQueryString();
    }

    public static void mapSupplyChain(BindingSet querySolution, SupplyChain supplyChain) {
        supplyChain.setId(QueryResultUtils.getIRI(querySolution, SupplyChain.SUPPLY_CHAIN_ID));
        supplyChain.setSourceId(
                QueryResultUtils.getStringMaybe(querySolution, SupplyChain.SUPPLY_CHAIN_SOURCE_ID));
        var description =
                QueryResultUtils.getValueMaybe(querySolution, SupplyChain.SUPPLY_CHAIN_DESCRIPTION);
        if (description != null) {
            supplyChain.setDescription(description.stringValue());
            if (description.isLiteral() && ((Literal) description).getLanguage().isPresent()) {
                supplyChain.setDescriptionLanguageCode(((Literal) description).getLanguage().get());
            }
        }
    }

    public static SelectQuery getSupplyChainSelectQuery(String iri) {
        SelectQuery selectQuery =
                Queries.SELECT(
                                SupplyChain.SUPPLY_CHAIN_ID,
                                SupplyChain.SUPPLY_CHAIN_SOURCE_ID,
                                SupplyChain.SUPPLY_CHAIN_DESCRIPTION,
                                SupplyChain.SUPPLY_CHAIN_SUPPLIER)
                        .where(
                                SupplyChain.SUPPLY_CHAIN_ID
                                        .isA(iri(AMS.SupplyChain))
                                        .and(sourceId)
                                        .and(description)
                                        .and(supplier));

        if (iri != null) {
            selectQuery
                    .groupBy(
                            SupplyChain.SUPPLY_CHAIN_ID,
                            SupplyChain.SUPPLY_CHAIN_SOURCE_ID,
                            SupplyChain.SUPPLY_CHAIN_DESCRIPTION,
                            SupplyChain.SUPPLY_CHAIN_SUPPLIER)
                    .having(Expressions.equals(SupplyChain.SUPPLY_CHAIN_ID, iri(iri)));
        }
        return selectQuery;
    }

    @Override
    protected void populateBindingsForUpdate(
            MutableBindings bindingsBuilder, SupplyChain supplyChain) {
        bindingsBuilder.addMaybe(SupplyChain.SUPPLY_CHAIN_SOURCE_ID, supplyChain.getSourceId());
        if (supplyChain.getDescription() != null) {
            var descriptionVal =
                    Values.literal(
                            supplyChain.getDescription(), supplyChain.getDescriptionLanguageCode());
            bindingsBuilder.addMaybe(SupplyChain.SUPPLY_CHAIN_DESCRIPTION, descriptionVal);
        }
        supplyChain
                .getSuppliers()
                .forEach(
                        sce ->
                                bindingsBuilder.addMaybe(
                                        SupplyChain.SUPPLY_CHAIN_SUPPLIER, sce.getId()));
    }

    @Override
    protected NamedSparqlSupplier getInsertSparql(SupplyChain supplyChain) {
        return NamedSparqlSupplier.of(
                KEY_PREFIX_INSERT,
                () ->
                        Queries.INSERT(
                                        SupplyChain.SUPPLY_CHAIN_ID
                                                .isA(iri(AMS.SupplyChain))
                                                .andHas(
                                                        iri(AMS.externalIdentifier),
                                                        SupplyChain.SUPPLY_CHAIN_SOURCE_ID)
                                                .andHas(
                                                        iri(RDFS.COMMENT),
                                                        SupplyChain.SUPPLY_CHAIN_DESCRIPTION))
                                .getQueryString());
    }

    @Override
    protected IRI getInputId(SupplyChain supplyChain) {
        if (supplyChain.getId() == null) {
            return getRdf4JTemplate().getNewUUID();
        }
        return supplyChain.getId();
    }
}
