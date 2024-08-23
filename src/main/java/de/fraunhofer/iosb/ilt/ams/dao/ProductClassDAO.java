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

import static de.fraunhofer.iosb.ilt.ams.dao.ProductDAO.populateProductBindingsForUpdate;
import static de.fraunhofer.iosb.ilt.ams.dao.SemanticReferenceDAO.populateSemanticReferenceForUpdate;
import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri;

import de.fraunhofer.iosb.ilt.ams.AMS;
import de.fraunhofer.iosb.ilt.ams.model.ObjectRdf4jRepository;
import de.fraunhofer.iosb.ilt.ams.model.ProductClass;
import de.fraunhofer.iosb.ilt.ams.model.SemanticReference;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
public class ProductClassDAO extends RDF4JCRUDDao<ProductClass, ProductClass, IRI> {
    public static final String PRODUCT_CLASS = "product-class";

    public static final GraphPatternNotTriples labelPattern =
            GraphPatterns.optional(
                    ProductClass.PRODUCT_CLASS_ID.has(
                            RDFS.LABEL, ProductClass.PRODUCT_CLASS_LABEL));
    public static final GraphPatternNotTriples sourceIdPattern =
            GraphPatterns.optional(
                    ProductClass.PRODUCT_CLASS_ID.has(
                            iri(AMS.externalIdentifier), ProductClass.PRODUCT_CLASS_SOURCE_ID));
    public static final GraphPatternNotTriples descriptionPattern =
            GraphPatterns.optional(
                    ProductClass.PRODUCT_CLASS_ID.has(
                            RDFS.COMMENT, ProductClass.PRODUCT_CLASS_DESCRIPTION));

    public static final GraphPatternNotTriples parentClassesPattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            ProductClass.PRODUCT_CLASS_ID.has(
                                    iri(AMS.specializes),
                                    ProductClass.PRODUCT_CLASS_PARENT_CLASSES),
                            ProductClass.PRODUCT_CLASS_PARENT_CLASSES.isA(AMS.ProductClass)));
    public static final GraphPatternNotTriples childClassesPattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            ProductClass.PRODUCT_CLASS_ID.has(
                                    iri(AMS.generalizes), ProductClass.PRODUCT_CLASS_CHILD_CLASSES),
                            ProductClass.PRODUCT_CLASS_CHILD_CLASSES.isA(AMS.ProductClass)));
    public static final GraphPatternNotTriples productsPattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            ProductClass.PRODUCT_CLASS_ID.has(
                                    iri(AMS.generalizes), ProductClass.PRODUCT_CLASS_PRODUCTS),
                            ProductClass.PRODUCT_CLASS_PRODUCTS.isA(AMS.Product)));
    public static final GraphPatternNotTriples semanticReferencePattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            ProductClass.PRODUCT_CLASS_ID.has(
                                    iri(AMS.hasSemantic),
                                    ProductClass.PRODUCT_CLASS_SEMANTIC_REFERENCES),
                            ProductClass.PRODUCT_CLASS_SEMANTIC_REFERENCES.isA(
                                    iri(AMS.SemanticReference))));

    List<ProductClass> productClassList;

    @Autowired ObjectRdf4jRepository objectRdf4jRepository;

    public ProductClassDAO(RDF4JTemplate rdf4JTemplate) {
        super(rdf4JTemplate);
    }

    @Override
    protected void populateIdBindings(MutableBindings bindingsBuilder, IRI iri) {
        bindingsBuilder.add(ProductClass.PRODUCT_CLASS_ID, iri);
    }

    @Override
    protected NamedSparqlSupplierPreparer prepareNamedSparqlSuppliers(
            NamedSparqlSupplierPreparer preparer) {
        return preparer.forKey(PRODUCT_CLASS)
                .supplySparql(
                        Queries.SELECT(
                                        ProductClass.PRODUCT_CLASS_ID,
                                        ProductClass.PRODUCT_CLASS_SOURCE_ID,
                                        ProductClass.PRODUCT_CLASS_LABEL,
                                        ProductClass.PRODUCT_CLASS_DESCRIPTION,
                                        ProductClass.PRODUCT_CLASS_SEMANTIC_REFERENCES,
                                        ProductClass.PRODUCT_CLASS_PARENT_CLASSES,
                                        ProductClass.PRODUCT_CLASS_CHILD_CLASSES,
                                        ProductClass.PRODUCT_CLASS_PRODUCTS)
                                .where(
                                        ProductClass.PRODUCT_CLASS_ID
                                                .isA(iri(AMS.ProductClass))
                                                .and(labelPattern)
                                                .and(sourceIdPattern)
                                                .and(descriptionPattern)
                                                .and(childClassesPattern)
                                                .and(parentClassesPattern)
                                                .and(semanticReferencePattern)
                                                .and(productsPattern))
                                .getQueryString());
    }

    @Override
    protected ProductClass mapSolution(BindingSet querySolution) {
        ProductClass productClass = null;

        for (ProductClass pc : productClassList) {
            if (pc.getId()
                    .equals(
                            QueryResultUtils.getIRI(
                                    querySolution, ProductClass.PRODUCT_CLASS_ID))) {
                productClass = pc;
                break;
            }
        }
        if (productClass == null) {
            productClass = new ProductClass();
            mapProductClass(querySolution, productClass);
            productClassList.add(productClass);
        }
        var semanticReference =
                QueryResultUtils.getIRIMaybe(
                        querySolution, ProductClass.PRODUCT_CLASS_SEMANTIC_REFERENCES);
        if (semanticReference != null) {
            productClass.addSemanticReference(
                    objectRdf4jRepository.getSemanticReferenceById(semanticReference));
        }
        var parentClass =
                QueryResultUtils.getIRIMaybe(
                        querySolution, ProductClass.PRODUCT_CLASS_PARENT_CLASSES);
        if (parentClass != null) {
            productClass.addParentClass(objectRdf4jRepository.getProductClassById(parentClass));
        }

        var childClass =
                QueryResultUtils.getIRIMaybe(
                        querySolution, ProductClass.PRODUCT_CLASS_CHILD_CLASSES);
        if (childClass != null) {
            productClass.addChildClass(objectRdf4jRepository.getProductClassById(childClass));
        }
        var product =
                QueryResultUtils.getIRIMaybe(querySolution, ProductClass.PRODUCT_CLASS_PRODUCTS);
        if (product != null) {
            productClass.addProduct(objectRdf4jRepository.getProductById(product));
        }

        return productClass;
    }

    @Override
    protected String getReadQuery() {
        productClassList = new LinkedList<>();
        return getProductClassSelectQuery(null)
                .from(objectRdf4jRepository.getGraphNameForQuery())
                .getQueryString();
    }

    public static void mapProductClass(BindingSet bindings, ProductClass productClass) {
        productClass.setId(QueryResultUtils.getIRI(bindings, ProductClass.PRODUCT_CLASS_ID));
        productClass.setSourceId(
                QueryResultUtils.getStringMaybe(bindings, ProductClass.PRODUCT_CLASS_SOURCE_ID));
        var label = bindings.getValue(ProductClass.PRODUCT_CLASS_LABEL.getVarName());
        if (label != null) {
            productClass.setLabel(label.stringValue());
            if (label.isLiteral() && ((Literal) label).getLanguage().isPresent()) {
                productClass.setLabelLanguageCode(((Literal) label).getLanguage().get());
            }
        }
        var description = bindings.getValue(ProductClass.PRODUCT_CLASS_DESCRIPTION.getVarName());
        if (description != null) {
            productClass.setDescription(description.stringValue());
            if (description.isLiteral() && ((Literal) description).getLanguage().isPresent()) {
                productClass.setDescriptionLanguageCode(
                        ((Literal) description).getLanguage().get());
            }
        }
    }

    public ProductClass getFromId(IRI iri) {

        Set<ProductClass> productClassSet =
                getNamedTupleQuery(PRODUCT_CLASS)
                        .evaluateAndConvert()
                        .toStream()
                        .filter(
                                bs ->
                                        QueryResultUtils.getIRI(bs, ProductClass.PRODUCT_CLASS_ID)
                                                .equals(iri))
                        .map(this::mapSolution)
                        .collect(Collectors.toSet());
        if (!productClassSet.isEmpty()) {
            return productClassSet.stream().findFirst().get();
        }
        return null;
    }

    public static SelectQuery getProductClassSelectQuery(String iri) {
        SelectQuery selectQuery =
                Queries.SELECT(
                                ProductClass.PRODUCT_CLASS_ID,
                                ProductClass.PRODUCT_CLASS_SOURCE_ID,
                                ProductClass.PRODUCT_CLASS_LABEL,
                                ProductClass.PRODUCT_CLASS_DESCRIPTION,
                                ProductClass.PRODUCT_CLASS_SEMANTIC_REFERENCES,
                                ProductClass.PRODUCT_CLASS_PARENT_CLASSES,
                                ProductClass.PRODUCT_CLASS_CHILD_CLASSES,
                                ProductClass.PRODUCT_CLASS_PRODUCTS)
                        .where(
                                ProductClass.PRODUCT_CLASS_ID
                                        .isA(iri(AMS.ProductClass))
                                        .and(labelPattern)
                                        .and(sourceIdPattern)
                                        .and(descriptionPattern)
                                        .and(childClassesPattern)
                                        .and(parentClassesPattern)
                                        .and(semanticReferencePattern)
                                        .and(productsPattern));
        if (iri != null) {
            return selectQuery
                    .groupBy(
                            ProductClass.PRODUCT_CLASS_ID,
                            ProductClass.PRODUCT_CLASS_SOURCE_ID,
                            ProductClass.PRODUCT_CLASS_LABEL,
                            ProductClass.PRODUCT_CLASS_DESCRIPTION,
                            ProductClass.PRODUCT_CLASS_SEMANTIC_REFERENCES,
                            ProductClass.PRODUCT_CLASS_PARENT_CLASSES,
                            ProductClass.PRODUCT_CLASS_CHILD_CLASSES,
                            ProductClass.PRODUCT_CLASS_PRODUCTS)
                    .having(Expressions.equals(ProductClass.PRODUCT_CLASS_ID, iri(iri)));
        }
        return selectQuery;
    }

    @Override
    protected NamedSparqlSupplier getInsertSparql(ProductClass productClass) {
        TriplePattern childPattern =
                GraphPatterns.tp(
                        ProductClass.PRODUCT_CLASS_ID,
                        iri(AMS.generalizes),
                        ProductClass.PRODUCT_CLASS_CHILD_CLASSES);
        for (int i = 1; i < productClass.getChildClasses().size(); i++) {
            childPattern =
                    childPattern.andHas(
                            iri(AMS.generalizes),
                            SparqlBuilder.var(
                                    ProductClass.PRODUCT_CLASS_CHILD_CLASSES.getVarName() + i));
        }

        TriplePattern parentPattern =
                GraphPatterns.tp(
                        ProductClass.PRODUCT_CLASS_ID,
                        iri(AMS.specializes),
                        ProductClass.PRODUCT_CLASS_PARENT_CLASSES);
        for (int i = 1; i < productClass.getParentClasses().size(); i++) {
            parentPattern =
                    parentPattern.andHas(
                            iri(AMS.specializes),
                            SparqlBuilder.var(
                                    ProductClass.PRODUCT_CLASS_PARENT_CLASSES.getVarName() + i));
        }
        TriplePattern finalChildPattern = childPattern;
        TriplePattern finalParentPattern = parentPattern;
        return NamedSparqlSupplier.of(
                KEY_PREFIX_INSERT,
                () ->
                        Queries.INSERT(
                                        ProductClass.PRODUCT_CLASS_ID
                                                .isA(iri(AMS.ProductClass))
                                                .andHas(
                                                        iri(AMS.externalIdentifier),
                                                        ProductClass.PRODUCT_CLASS_SOURCE_ID)
                                                .andHas(
                                                        iri(RDFS.LABEL),
                                                        ProductClass.PRODUCT_CLASS_LABEL)
                                                .andHas(
                                                        iri(RDFS.COMMENT),
                                                        ProductClass.PRODUCT_CLASS_DESCRIPTION)
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

    @Override
    protected void populateBindingsForUpdate(
            MutableBindings bindingsBuilder, ProductClass productClass) {
        populateProductClassBindingsForUpdate(bindingsBuilder, productClass, true);
    }

    public static void populateProductClassBindingsForUpdate(
            MutableBindings bindingsBuilder, ProductClass productClass, boolean isUpdate) {
        if (!isUpdate) {
            bindingsBuilder.add(ProductClass.PRODUCT_CLASS_ID, productClass.getId());
        }
        bindingsBuilder.addMaybe(ProductClass.PRODUCT_CLASS_SOURCE_ID, productClass.getSourceId());
        if (productClass.getLabel() != null) {
            var label =
                    Values.literal(productClass.getLabel(), productClass.getLabelLanguageCode());
            bindingsBuilder.add(ProductClass.PRODUCT_CLASS_LABEL, label);
        }
        if (productClass.getDescription() != null) {
            var description =
                    Values.literal(
                            productClass.getDescription(),
                            productClass.getDescriptionLanguageCode());
            bindingsBuilder.add(ProductClass.PRODUCT_CLASS_DESCRIPTION, description);
        }
        for (var semRef : productClass.getSemanticReferences()) {

            populateSemanticReferenceForUpdate(bindingsBuilder, semRef, false);
        }
        var iterator = productClass.getChildClasses().iterator();
        for (int i = 0; i < productClass.getChildClasses().size(); i++) {
            if (i == 0) {
                bindingsBuilder.add(
                        ProductClass.PRODUCT_CLASS_CHILD_CLASSES, iterator.next().getId());
            } else {
                bindingsBuilder.add(
                        SparqlBuilder.var(
                                ProductClass.PRODUCT_CLASS_CHILD_CLASSES.getVarName() + i),
                        iterator.next().getId());
            }
        }
        var parentIterator = productClass.getParentClasses().iterator();
        for (int i = 0; i < productClass.getParentClasses().size(); i++) {
            if (i == 0) {
                bindingsBuilder.add(
                        ProductClass.PRODUCT_CLASS_PARENT_CLASSES, parentIterator.next().getId());
            } else {
                bindingsBuilder.add(
                        SparqlBuilder.var(
                                ProductClass.PRODUCT_CLASS_PARENT_CLASSES.getVarName() + i),
                        parentIterator.next().getId());
            }
        }
        for (var product : productClass.getProducts()) {
            populateProductBindingsForUpdate(bindingsBuilder, product);
        }
    }

    @Override
    protected IRI getInputId(ProductClass productClass) {
        if (productClass.getId() == null) {
            return this.getRdf4JTemplate().getNewUUID();
        }
        return productClass.getId();
    }
}
