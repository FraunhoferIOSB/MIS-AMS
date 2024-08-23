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
import de.fraunhofer.iosb.ilt.ams.utility.MESSAGE;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.rdf4j.model.IRI;
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
public class ProductApplicationDAO
        extends RDF4JCRUDDao<ProductApplication, ProductApplication, IRI> {

    public static final GraphPatternNotTriples sourceIdPattern =
            GraphPatterns.optional(
                    ProductApplication.PRODUCT_APP_ID.has(
                            iri(AMS.externalIdentifier), ProductApplication.PRODUCT_APP_SOURCE_ID));
    public static final GraphPatternNotTriples productPattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            ProductApplication.PRODUCT_APP_ID.has(
                                    iri(AMS.has), ProductApplication.PRODUCT_APP_PRODUCT),
                            ProductApplication.PRODUCT_APP_PRODUCT.isA(AMS.Product)));
    public static final GraphPatternNotTriples quantityPattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            ProductApplication.PRODUCT_APP_ID.has(
                                    iri(AMS.has), ProductApplication.PRODUCT_APP_QUANTITY),
                            ProductApplication.PRODUCT_APP_QUANTITY.isA(AMS.Property)));
    public static final GraphPatternNotTriples propertyPattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            ProductApplication.PRODUCT_APP_ID.has(
                                    iri(AMS.has), ProductApplication.PRODUCT_APP_PROPERTY),
                            ProductApplication.PRODUCT_APP_PROPERTY.isA(AMS.Property)));

    @Autowired ObjectRdf4jRepository repo;
    List<ProductApplication> productApplications;

    public ProductApplicationDAO(RDF4JTemplate rdf4JTemplate) {
        super(rdf4JTemplate);
    }

    @Override
    protected void populateIdBindings(MutableBindings bindingsBuilder, IRI iri) {
        bindingsBuilder.add(ProductApplication.PRODUCT_APP_ID, iri);
    }

    @Override
    protected NamedSparqlSupplierPreparer prepareNamedSparqlSuppliers(
            NamedSparqlSupplierPreparer preparer) {
        return null;
    }

    @Override
    protected String getReadQuery() {
        productApplications = new LinkedList<>();
        return getProductApplicationSelectQuery(null)
                .from(repo.getGraphNameForQuery())
                .getQueryString();
    }

    @Override
    protected ProductApplication mapSolution(BindingSet querySolution) {
        ProductApplication productApplication = null;
        for (ProductApplication pa : productApplications) {
            if (pa.getId()
                    .equals(
                            QueryResultUtils.getIRI(
                                    querySolution, ProductApplication.PRODUCT_APP_ID))) {
                productApplication = pa;
                break;
            }
        }
        if (productApplication == null) {
            productApplication = new ProductApplication();
            mapProductApplication(querySolution, productApplication);
            productApplications.add(productApplication);
        }
        var product =
                QueryResultUtils.getIRIMaybe(querySolution, ProductApplication.PRODUCT_APP_PRODUCT);
        if (product != null) {
            productApplication.setProduct(repo.getProductById(product));
        }

        var quantity =
                QueryResultUtils.getIRIMaybe(
                        querySolution, ProductApplication.PRODUCT_APP_QUANTITY);
        if (quantity != null) {
            productApplication.setQuantity(repo.getPropertyById(quantity));
        }

        var property =
                QueryResultUtils.getIRIMaybe(
                        querySolution, ProductApplication.PRODUCT_APP_PROPERTY);
        if (property != null) {
            productApplication.addProperty(repo.getPropertyById(property));
        }
        return productApplication;
    }

    public static void mapProductApplication(
            BindingSet querySolution, ProductApplication productApplication) {
        productApplication.setId(
                QueryResultUtils.getIRI(querySolution, ProductApplication.PRODUCT_APP_ID));
        productApplication.setSourceId(
                QueryResultUtils.getStringMaybe(
                        querySolution, ProductApplication.PRODUCT_APP_SOURCE_ID));
    }

    public static SelectQuery getProductApplicationSelectQuery(String iri) {
        SelectQuery selectQuery =
                Queries.SELECT(
                                ProductApplication.PRODUCT_APP_ID,
                                ProductApplication.PRODUCT_APP_SOURCE_ID,
                                ProductApplication.PRODUCT_APP_PRODUCT,
                                ProductApplication.PRODUCT_APP_QUANTITY,
                                ProductApplication.PRODUCT_APP_PROPERTY)
                        .where(
                                ProductApplication.PRODUCT_APP_ID
                                        .isA(AMS.ProductApplication)
                                        .and(sourceIdPattern)
                                        .and(productPattern)
                                        .and(quantityPattern)
                                        .and(propertyPattern));
        if (iri != null) {
            return selectQuery
                    .groupBy(
                            ProductApplication.PRODUCT_APP_ID,
                            ProductApplication.PRODUCT_APP_SOURCE_ID,
                            ProductApplication.PRODUCT_APP_PRODUCT,
                            ProductApplication.PRODUCT_APP_QUANTITY,
                            ProductApplication.PRODUCT_APP_PROPERTY)
                    .having(Expressions.equals(ProductApplication.PRODUCT_APP_ID, iri(iri)));
        }
        return selectQuery;
    }

    @Override
    protected IRI getInputId(ProductApplication productApplication) {
        if (productApplication.getId() == null) {
            return getRdf4JTemplate().getNewUUID();
        }
        return productApplication.getId();
    }

    @Override
    protected NamedSparqlSupplier getInsertSparql(ProductApplication productApplication) {
        return NamedSparqlSupplier.of(
                KEY_PREFIX_INSERT,
                () ->
                        Queries.INSERT(
                                        ProductApplication.PRODUCT_APP_ID
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
    protected void populateBindingsForUpdate(
            MutableBindings bindingsBuilder, ProductApplication productApplication) {
        populateProductApplicationBindingsForUpdate(bindingsBuilder, productApplication);
    }

    public static void populateProductApplicationBindingsForUpdate(
            MutableBindings bindingsBuilder, ProductApplication productApplication) {
        bindingsBuilder.addMaybe(
                ProductApplication.PRODUCT_APP_SOURCE_ID, productApplication.getSourceId());
        if (productApplication.getProduct() != null) {
            bindingsBuilder.add(
                    ProductApplication.PRODUCT_APP_PRODUCT,
                    productApplication.getProduct().getId());
            bindingsBuilder.addMaybe(Product.PRODUCT_ID, productApplication.getProduct().getId());
            bindingsBuilder.addMaybe(
                    Product.PRODUCT_SOURCE_ID, productApplication.getProduct().getSourceId());
            var label =
                    Values.literal(
                            productApplication.getProduct().getLabel(),
                            productApplication.getProduct().getLabelLanguageCode());
            bindingsBuilder.addMaybe(Product.PRODUCT_LABEL, label);
            var description =
                    Values.literal(
                            productApplication.getProduct().getDescription(),
                            productApplication.getProduct().getDescriptionLanguageCode());
            bindingsBuilder.addMaybe(Product.PRODUCT_DESCRIPTION, description);
            if (productApplication.getProduct().getProductPassport() != null) {
                bindingsBuilder.addMaybe(
                        Product.PRODUCT_PRODUCT_PASSPORT,
                        productApplication.getProduct().getProductPassport().getId());
                productApplication
                        .getProduct()
                        .getProductPassport()
                        .getProperties()
                        .forEach(
                                property -> {
                                    if (property.getSemanticReferences().isEmpty()
                                            && (property.getLabel().isEmpty()
                                                    || property.getDescription().isEmpty())) {
                                        throw new IllegalArgumentException(
                                                String.format(
                                                        MESSAGE.NOT_ENOUGH_ARGUMENTS,
                                                        property.getId()));
                                    }
                                    bindingsBuilder.addMaybe(
                                            ProductPassport.PRODUCT_PASSPORT_PROPERTIES,
                                            property.getId());
                                    bindingsBuilder.addMaybe(
                                            Property.PROPERTY_SOURCE_ID, property.getSourceId());
                                    var propertyLabel =
                                            Values.literal(
                                                    property.getLabel(),
                                                    property.getLabelLanguageCode());
                                    bindingsBuilder.addMaybe(
                                            Property.PROPERTY_LABEL, propertyLabel);
                                    var propertyDescription =
                                            Values.literal(
                                                    property.getDescription(),
                                                    property.getDescriptionLanguageCode());
                                    bindingsBuilder.addMaybe(
                                            Property.PROPERTY_DESCRIPTION, propertyDescription);
                                    bindingsBuilder.addMaybe(
                                            Property.PROPERTY_VALUE, property.getValue());
                                    property.getSemanticReferences()
                                            .forEach(
                                                    semRef -> {
                                                        bindingsBuilder.add(
                                                                Property
                                                                        .PROPERTY_SEMANTIC_REFERENCES,
                                                                semRef.getId());
                                                        bindingsBuilder.addMaybe(
                                                                SemanticReference
                                                                        .SEMANTIC_REFERENCE_SOURCE_URI,
                                                                semRef.getSourceUri().toString());
                                                        var semRefLabel =
                                                                Values.literal(
                                                                        semRef.getLabel(),
                                                                        semRef
                                                                                .getLabelLanguageCode());
                                                        bindingsBuilder.addMaybe(
                                                                SemanticReference
                                                                        .SEMANTIC_REFERENCE_LABEL,
                                                                semRefLabel);
                                                        var semRefDescription =
                                                                Values.literal(
                                                                        semRef.getDescription(),
                                                                        semRef
                                                                                .getDescriptionLanguageCode());
                                                        bindingsBuilder.addMaybe(
                                                                SemanticReference
                                                                        .SEMANTIC_REFERENCE_DESCRIPTION,
                                                                semRefDescription);
                                                    });
                                });
            }
        }
        if (productApplication.getQuantity() != null) {
            bindingsBuilder.add(
                    ProductApplication.PRODUCT_APP_QUANTITY,
                    productApplication.getQuantity().getId());
            bindingsBuilder.addMaybe(
                    Property.PROPERTY_SOURCE_ID, productApplication.getQuantity().getSourceId());
            var propertyLabel =
                    Values.literal(
                            productApplication.getQuantity().getLabel(),
                            productApplication.getQuantity().getLabelLanguageCode());
            bindingsBuilder.addMaybe(Property.PROPERTY_LABEL, propertyLabel);
            var propertyDescription =
                    Values.literal(
                            productApplication.getQuantity().getDescription(),
                            productApplication.getQuantity().getDescriptionLanguageCode());
            bindingsBuilder.addMaybe(Property.PROPERTY_DESCRIPTION, propertyDescription);
            bindingsBuilder.addMaybe(
                    Property.PROPERTY_VALUE, productApplication.getQuantity().getValue());
            productApplication
                    .getQuantity()
                    .getSemanticReferences()
                    .forEach(
                            semRef -> {
                                bindingsBuilder.add(
                                        Property.PROPERTY_SEMANTIC_REFERENCES, semRef.getId());
                                bindingsBuilder.addMaybe(
                                        SemanticReference.SEMANTIC_REFERENCE_SOURCE_URI,
                                        semRef.getSourceUri().toString());
                                var semRefLabel =
                                        Values.literal(
                                                semRef.getLabel(), semRef.getLabelLanguageCode());
                                bindingsBuilder.addMaybe(
                                        SemanticReference.SEMANTIC_REFERENCE_LABEL, semRefLabel);
                                var semRefDescription =
                                        Values.literal(
                                                semRef.getDescription(),
                                                semRef.getDescriptionLanguageCode());
                                bindingsBuilder.addMaybe(
                                        SemanticReference.SEMANTIC_REFERENCE_DESCRIPTION,
                                        semRefDescription);
                            });
        }
    }
}
