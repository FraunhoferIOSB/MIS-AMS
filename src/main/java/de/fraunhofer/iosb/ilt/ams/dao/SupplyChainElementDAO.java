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

import static de.fraunhofer.iosb.ilt.ams.dao.EnterpriseDAO.populateEnterpriseBindingsForUpdate;
import static de.fraunhofer.iosb.ilt.ams.dao.FactoryDAO.populateFactoryBindingsForUpdate;
import static de.fraunhofer.iosb.ilt.ams.dao.ProductApplicationDAO.populateProductApplicationBindingsForUpdate;
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
public class SupplyChainElementDAO
        extends RDF4JCRUDDao<SupplyChainElement, SupplyChainElement, IRI> {

    public static final GraphPatternNotTriples sourceId =
            GraphPatterns.optional(
                    SupplyChainElement.SCE_ID.has(
                            AMS.externalIdentifier, SupplyChainElement.SCE_SOURCE_ID));
    public static final GraphPatternNotTriples description =
            GraphPatterns.optional(
                    SupplyChainElement.SCE_ID.has(
                            RDFS.COMMENT, SupplyChainElement.SCE_DESCRIPTION));

    public static final GraphPatternNotTriples factory =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            SupplyChainElement.SCE_ID.has(
                                    iri(AMS.has), SupplyChainElement.SCE_FACTORY),
                            SupplyChainElement.SCE_FACTORY.isA(iri(AMS.Factory))));
    public static final GraphPatternNotTriples product =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            SupplyChainElement.SCE_ID.has(
                                    iri(AMS.has), SupplyChainElement.SCE_PRODUCT),
                            GraphPatterns.union(
                                    SupplyChainElement.SCE_PRODUCT.isA(AMS.Product),
                                    SupplyChainElement.SCE_PRODUCT.isA(AMS.ProductApplication))));
    public static final GraphPatternNotTriples enterprise =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            SupplyChainElement.SCE_ID.has(
                                    iri(AMS.has), SupplyChainElement.SCE_ENTERPRISE),
                            SupplyChainElement.SCE_ENTERPRISE.isA(AMS.Enterprise)));
    public static final GraphPatternNotTriples supplier =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            SupplyChainElement.SCE_ID.has(
                                    iri(AMS.contains), SupplyChainElement.SCE_SUPPLIER),
                            SupplyChainElement.SCE_SUPPLIER.isA(AMS.SupplyChainElement)));

    @Autowired ObjectRdf4jRepository repo;
    private List<SupplyChainElement> supplyChainElementList;

    public SupplyChainElementDAO(RDF4JTemplate rdf4JTemplate) {
        super(rdf4JTemplate);
    }

    @Override
    protected void populateIdBindings(MutableBindings bindingsBuilder, IRI iri) {
        bindingsBuilder.add(SupplyChainElement.SCE_ID, iri);
    }

    @Override
    protected NamedSparqlSupplierPreparer prepareNamedSparqlSuppliers(
            NamedSparqlSupplierPreparer preparer) {
        return null;
    }

    @Override
    protected String getReadQuery() {
        supplyChainElementList = new LinkedList<>();
        return getSupplyChainElementSelectQuery(null)
                .from(repo.getGraphNameForQuery())
                .getQueryString();
    }

    @Override
    protected SupplyChainElement mapSolution(BindingSet querySolution) {
        SupplyChainElement supplyChainElement = null;

        for (SupplyChainElement sce : supplyChainElementList) {
            if (sce.getId()
                    .equals(QueryResultUtils.getIRI(querySolution, SupplyChainElement.SCE_ID))) {
                supplyChainElement = sce;
                break;
            }
        }
        if (supplyChainElement == null) {
            supplyChainElement = new SupplyChainElement();
            mapSupplyChainElement(querySolution, supplyChainElement);
            var factoryQuery =
                    QueryResultUtils.getIRIMaybe(querySolution, SupplyChainElement.SCE_FACTORY);
            if (supplyChainElement.getFactory() == null && factoryQuery != null) {
                supplyChainElement.setFactory(repo.getFactoryByIri(factoryQuery));
            }
            var enterpriseQuery =
                    QueryResultUtils.getIRIMaybe(querySolution, SupplyChainElement.SCE_ENTERPRISE);
            if (supplyChainElement.getEnterprise() == null && enterpriseQuery != null) {
                supplyChainElement.setEnterprise(repo.getEnterpriseById(enterpriseQuery));
            }
            supplyChainElementList.add(supplyChainElement);
        }
        var productQuery =
                QueryResultUtils.getIRIMaybe(querySolution, SupplyChainElement.SCE_PRODUCT);
        if (productQuery != null) {
            supplyChainElement.addProduct(repo.getProductApplicationById(productQuery));
        }
        var supplierQuery =
                QueryResultUtils.getIRIMaybe(querySolution, SupplyChainElement.SCE_SUPPLIER);
        if (supplierQuery != null) {
            supplyChainElement.addSupplier(repo.getSupplyChainElementById(supplierQuery));
        }
        return supplyChainElement;
    }

    public static void mapSupplyChainElement(
            BindingSet querySolution, SupplyChainElement supplyChainElement) {
        supplyChainElement.setId(QueryResultUtils.getIRI(querySolution, SupplyChainElement.SCE_ID));
        supplyChainElement.setSourceId(
                QueryResultUtils.getStringMaybe(querySolution, SupplyChainElement.SCE_SOURCE_ID));
        var description =
                QueryResultUtils.getValueMaybe(querySolution, SupplyChainElement.SCE_DESCRIPTION);
        if (description != null) {
            supplyChainElement.setDescription(description.stringValue());
            if (description.isLiteral() && ((Literal) description).getLanguage().isPresent()) {
                supplyChainElement.setDescriptionLanguageCode(
                        ((Literal) description).getLanguage().get());
            }
        }
    }

    public static SelectQuery getSupplyChainElementSelectQuery(String iri) {
        var selectQuery =
                Queries.SELECT(
                                SupplyChainElement.SCE_ID,
                                SupplyChainElement.SCE_SOURCE_ID,
                                SupplyChainElement.SCE_DESCRIPTION,
                                SupplyChainElement.SCE_PRODUCT,
                                SupplyChainElement.SCE_ENTERPRISE,
                                SupplyChainElement.SCE_FACTORY,
                                SupplyChainElement.SCE_SUPPLIER)
                        .where(
                                SupplyChainElement.SCE_ID
                                        .isA(iri(AMS.SupplyChainElement))
                                        .and(sourceId)
                                        .and(description)
                                        .and(product)
                                        .and(enterprise)
                                        .and(factory)
                                        .and(supplier));
        if (iri != null) {
            return selectQuery
                    .groupBy(
                            SupplyChainElement.SCE_ID,
                            SupplyChainElement.SCE_SOURCE_ID,
                            SupplyChainElement.SCE_DESCRIPTION,
                            SupplyChainElement.SCE_PRODUCT,
                            SupplyChainElement.SCE_ENTERPRISE,
                            SupplyChainElement.SCE_FACTORY,
                            SupplyChainElement.SCE_SUPPLIER)
                    .having(Expressions.equals(SupplyChainElement.SCE_ID, iri(iri)));
        }
        return selectQuery;
    }

    @Override
    protected NamedSparqlSupplier getInsertSparql(SupplyChainElement supplyChainElement) {
        return NamedSparqlSupplier.of(
                KEY_PREFIX_INSERT,
                () ->
                        Queries.INSERT(
                                        SupplyChainElement.SCE_ID
                                                .isA(iri(AMS.SupplyChainElement))
                                                .andHas(
                                                        iri(AMS.externalIdentifier),
                                                        SupplyChainElement.SCE_SOURCE_ID)
                                                .andHas(
                                                        iri(RDFS.COMMENT),
                                                        SupplyChainElement.SCE_DESCRIPTION)
                                                .andHas(
                                                        iri(AMS.has),
                                                        SupplyChainElement.SCE_FACTORY)
                                                .andHas(
                                                        iri(AMS.has),
                                                        SupplyChainElement.SCE_ENTERPRISE)
                                                .andHas(
                                                        iri(AMS.has),
                                                        SupplyChainElement.SCE_PRODUCT),
                                        SupplyChainElement.SCE_FACTORY
                                                .isA(AMS.Factory)
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
                                                        Location.LOCATION_COUNTRY),
                                        SupplyChainElement.SCE_ENTERPRISE
                                                .isA(AMS.Enterprise)
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
                                                        Location.LOCATION_COUNTRY),
                                        SupplyChainElement.SCE_PRODUCT
                                                .isA(iri(AMS.ProductApplication))
                                                .andHas(
                                                        iri(AMS.externalIdentifier),
                                                        ProductApplication.PRODUCT_APP_SOURCE_ID)
                                                .andHas(
                                                        iri(AMS.has),
                                                        ProductApplication.PRODUCT_APP_PRODUCT)
                                                .andHas(
                                                        iri(AMS.has),
                                                        ProductApplication.PRODUCT_APP_QUANTITY),
                                        ProductApplication.PRODUCT_APP_PRODUCT
                                                .isA(iri(AMS.Product))
                                                .andHas(
                                                        iri(AMS.externalIdentifier),
                                                        Product.PRODUCT_SOURCE_ID)
                                                .andHas(iri(RDFS.LABEL), Product.PRODUCT_LABEL)
                                                .andHas(
                                                        iri(RDFS.COMMENT),
                                                        Product.PRODUCT_DESCRIPTION)
                                                .andHas(
                                                        iri(AMS.has),
                                                        Product.PRODUCT_PRODUCT_PASSPORT),
                                        Product.PRODUCT_PRODUCT_PASSPORT
                                                .isA(iri(AMS.ProductPassport))
                                                .andHas(
                                                        iri(AMS.has),
                                                        ProductPassport
                                                                .PRODUCT_PASSPORT_PROPERTIES),
                                        ProductPassport.PRODUCT_PASSPORT_PROPERTIES
                                                .isA(iri(AMS.Property))
                                                .andHas(
                                                        iri(AMS.externalIdentifier),
                                                        Property.PROPERTY_SOURCE_ID)
                                                .andHas(iri(RDFS.LABEL), Property.PROPERTY_LABEL)
                                                .andHas(
                                                        iri(RDFS.COMMENT),
                                                        Property.PROPERTY_DESCRIPTION)
                                                .andHas(
                                                        iri(AMS.hasSemantic),
                                                        Property.PROPERTY_SEMANTIC_REFERENCES)
                                                .andHas(iri(AMS.value), Property.PROPERTY_VALUE),
                                        ProductApplication.PRODUCT_APP_QUANTITY
                                                .isA(iri(AMS.Property))
                                                .andHas(
                                                        iri(AMS.externalIdentifier),
                                                        Property.PROPERTY_SOURCE_ID)
                                                .andHas(iri(RDFS.LABEL), Property.PROPERTY_LABEL)
                                                .andHas(
                                                        iri(RDFS.COMMENT),
                                                        Property.PROPERTY_DESCRIPTION)
                                                .andHas(
                                                        iri(AMS.hasSemantic),
                                                        Property.PROPERTY_SEMANTIC_REFERENCES)
                                                .andHas(iri(AMS.value), Property.PROPERTY_VALUE),
                                        Property.PROPERTY_SEMANTIC_REFERENCES
                                                .isA(iri(AMS.SemanticReference))
                                                .andHas(
                                                        iri(AMS.identifier),
                                                        SemanticReference
                                                                .SEMANTIC_REFERENCE_SOURCE_URI)
                                                .andHas(
                                                        iri(RDFS.LABEL),
                                                        SemanticReference.SEMANTIC_REFERENCE_LABEL)
                                                .andHas(
                                                        iri(RDFS.COMMENT),
                                                        SemanticReference
                                                                .SEMANTIC_REFERENCE_DESCRIPTION))
                                .getQueryString());
    }

    @Override
    protected IRI getInputId(SupplyChainElement supplyChainElement) {
        if (supplyChainElement.getId() == null) {
            return getRdf4JTemplate().getNewUUID();
        }
        return supplyChainElement.getId();
    }

    @Override
    protected void populateBindingsForUpdate(
            MutableBindings bindingsBuilder, SupplyChainElement supplyChainElement) {
        bindingsBuilder.addMaybe(
                SupplyChainElement.SCE_SOURCE_ID, supplyChainElement.getSourceId());
        if (supplyChainElement.getDescription() != null
                && supplyChainElement.getDescriptionLanguageCode() != null) {
            var descriptionVal =
                    Values.literal(
                            supplyChainElement.getDescription(),
                            supplyChainElement.getDescriptionLanguageCode());
            bindingsBuilder.add(SupplyChainElement.SCE_DESCRIPTION, descriptionVal);
        }
        if (supplyChainElement.getEnterprise() != null) {
            bindingsBuilder.add(
                    SupplyChainElement.SCE_ENTERPRISE, supplyChainElement.getEnterprise().getId());
            populateEnterpriseBindingsForUpdate(
                    bindingsBuilder, supplyChainElement.getEnterprise());
        }
        if (supplyChainElement.getFactory() != null) {
            bindingsBuilder.add(
                    SupplyChainElement.SCE_FACTORY, supplyChainElement.getFactory().getId());
            populateFactoryBindingsForUpdate(bindingsBuilder, supplyChainElement.getFactory());
        }
        for (var o : supplyChainElement.getProducts()) {
            populateProductApplicationBindingsForUpdate(bindingsBuilder, o);
        }
    }
}
