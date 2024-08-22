package de.fraunhofer.iosb.ilt.ams.dao;

import static de.fraunhofer.iosb.ilt.ams.dao.ProductClassDAO.populateProductClassBindingsForUpdate;
import static de.fraunhofer.iosb.ilt.ams.dao.ProductPassportDAO.populateProductPassportBindingsForUpdate;
import static de.fraunhofer.iosb.ilt.ams.dao.PropertyDAO.populatePropertyBindingsForUpdate;
import static de.fraunhofer.iosb.ilt.ams.dao.SemanticReferenceDAO.populateSemanticReferenceForUpdate;
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
public class ProductDAO extends RDF4JCRUDDao<Product, Product, IRI> {

    public static final String PRODUCT = "product";

    public static final GraphPatternNotTriples labelPattern =
            GraphPatterns.optional(Product.PRODUCT_ID.has(iri(RDFS.LABEL), Product.PRODUCT_LABEL));
    public static final GraphPatternNotTriples sourceIdPattern =
            GraphPatterns.optional(
                    Product.PRODUCT_ID.has(iri(AMS.externalIdentifier), Product.PRODUCT_SOURCE_ID));
    public static final GraphPatternNotTriples descriptionPattern =
            GraphPatterns.optional(
                    Product.PRODUCT_ID.has(iri(RDFS.COMMENT), Product.PRODUCT_DESCRIPTION));

    public static final GraphPatternNotTriples propertyPattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Product.PRODUCT_ID.has(iri(AMS.has), Product.PRODUCT_PROPERTIES),
                            Product.PRODUCT_PROPERTIES.isA(AMS.Property)));
    public static final GraphPatternNotTriples factoryPattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Product.PRODUCT_ID.has(iri(AMS.containedIn), Product.PRODUCT_FACTORIES),
                            GraphPatterns.union(
                                    Product.PRODUCT_FACTORIES.isA(AMS.PhysicalFactory),
                                    Product.PRODUCT_FACTORIES.isA(AMS.VirtualFactory))));
    public static final GraphPatternNotTriples enterprisePattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Product.PRODUCT_ID.has(iri(AMS.contains), Product.PRODUCT_ENTERPRISES),
                            Product.PRODUCT_ENTERPRISES.isA(AMS.Enterprise)));
    public static final GraphPatternNotTriples productClassesPattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Product.PRODUCT_ID.has(
                                    iri(AMS.specializes), Product.PRODUCT_PRODUCT_CLASSES),
                            Product.PRODUCT_PRODUCT_CLASSES.isA(AMS.ProductClass)));
    public static final GraphPatternNotTriples billOfMaterialsPattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Product.PRODUCT_ID.has(iri(AMS.contains), Product.PRODUCT_BOM),
                            Product.PRODUCT_BOM.isA(AMS.ProductApplication)));
    public static final GraphPatternNotTriples supplyChainsPattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Product.PRODUCT_ID.has(iri(AMS.has), Product.PRODUCT_SUPPLY_CHAINS),
                            Product.PRODUCT_SUPPLY_CHAINS.isA(AMS.SupplyChain)));
    public static final GraphPatternNotTriples semanticReferencePattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Product.PRODUCT_ID.has(
                                    iri(AMS.hasSemantic), Product.PRODUCT_SEMANTIC_REFERENCES),
                            Product.PRODUCT_SEMANTIC_REFERENCES.isA(iri(AMS.SemanticReference))));
    public static final GraphPatternNotTriples productPassportPattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            Product.PRODUCT_ID.has(iri(AMS.has), Product.PRODUCT_PRODUCT_PASSPORT),
                            Product.PRODUCT_PRODUCT_PASSPORT.isA(AMS.ProductPassport)));

    @Autowired ObjectRdf4jRepository objectRdf4jRepository;

    private List<Product> productList;

    private boolean shouldDelete = false;

    public void setShouldDelete(boolean shouldDelete) {
        this.shouldDelete = shouldDelete;
    }

    public ProductDAO(RDF4JTemplate rdf4JTemplate) {
        super(rdf4JTemplate);
    }

    @Override
    protected void populateIdBindings(MutableBindings bindingsBuilder, IRI iri) {
        bindingsBuilder.add(Product.PRODUCT_ID, iri);
    }

    @Override
    protected NamedSparqlSupplierPreparer prepareNamedSparqlSuppliers(
            NamedSparqlSupplierPreparer preparer) {
        return preparer.forKey(PRODUCT)
                .supplySparql(
                        Queries.SELECT(
                                        Product.PRODUCT_ID,
                                        Product.PRODUCT_SOURCE_ID,
                                        Product.PRODUCT_LABEL,
                                        Product.PRODUCT_DESCRIPTION,
                                        Product.PRODUCT_BOM,
                                        Product.PRODUCT_PROPERTIES,
                                        Product.PRODUCT_PRODUCT_CLASSES,
                                        Product.PRODUCT_SEMANTIC_REFERENCES,
                                        Product.PRODUCT_FACTORIES,
                                        Product.PRODUCT_ENTERPRISES,
                                        Product.PRODUCT_SUPPLY_CHAINS,
                                        Product.PRODUCT_PRODUCT_PASSPORT)
                                .where(
                                        Product.PRODUCT_ID
                                                .isA(iri(AMS.Product))
                                                .and(labelPattern)
                                                .and(sourceIdPattern)
                                                .and(descriptionPattern)
                                                .and(propertyPattern)
                                                .and(factoryPattern)
                                                .and(enterprisePattern)
                                                .and(productClassesPattern)
                                                .and(billOfMaterialsPattern)
                                                .and(supplyChainsPattern)
                                                .and(semanticReferencePattern)
                                                .and(productPassportPattern))
                                .getQueryString());
    }

    @Override
    protected String getReadQuery() {
        productList = new LinkedList<>();
        return getProductSelectQuery(null)
                .from(objectRdf4jRepository.getGraphNameForQuery())
                .getQueryString();
    }

    @Override
    protected Product mapSolution(BindingSet querySolution) {
        objectRdf4jRepository.emptyProcessedIds();
        Product product = null;

        for (Product p : productList) {
            if (p.getId().equals(QueryResultUtils.getIRI(querySolution, Product.PRODUCT_ID))) {
                product = p;
                break;
            }
        }
        if (product == null) {
            product = new Product();
            mapProduct(querySolution, product);
            var productPassportId =
                    QueryResultUtils.getIRIMaybe(querySolution, Product.PRODUCT_PRODUCT_PASSPORT);
            if (productPassportId != null) {
                product.setProductPassport(
                        objectRdf4jRepository.getProductPassportById(productPassportId));
            }
            productList.add(product);
        }
        var billOfMaterial = QueryResultUtils.getIRIMaybe(querySolution, Product.PRODUCT_BOM);
        if (billOfMaterial != null) {
            product.addBillOfMaterial(
                    objectRdf4jRepository.getProductApplicationById(billOfMaterial));
        }
        var property = QueryResultUtils.getIRIMaybe(querySolution, Product.PRODUCT_PROPERTIES);
        if (property != null) {
            product.addProperty(objectRdf4jRepository.getPropertyById(property));
        }
        var productClass =
                QueryResultUtils.getIRIMaybe(querySolution, Product.PRODUCT_PRODUCT_CLASSES);
        if (productClass != null) {
            product.addProductClass(objectRdf4jRepository.getProductClassById(productClass));
        }
        var semanticReference =
                QueryResultUtils.getIRIMaybe(querySolution, Product.PRODUCT_SEMANTIC_REFERENCES);
        if (semanticReference != null) {
            product.addSemanticReference(
                    objectRdf4jRepository.getSemanticReferenceById(semanticReference));
        }
        var factory = QueryResultUtils.getIRIMaybe(querySolution, Product.PRODUCT_FACTORIES);
        if (factory != null) {
            product.addFactory(objectRdf4jRepository.getFactoryByIri(factory));
        }
        var enterprise = QueryResultUtils.getIRIMaybe(querySolution, Product.PRODUCT_ENTERPRISES);
        if (enterprise != null) {
            product.addEnterprise(objectRdf4jRepository.getEnterpriseById(enterprise));
        }
        var supplyChain =
                QueryResultUtils.getIRIMaybe(querySolution, Product.PRODUCT_SUPPLY_CHAINS);
        if (supplyChain != null) {
            product.setSupplyChains(objectRdf4jRepository.getSupplyChainById(supplyChain));
        }
        return product;
    }

    @Override
    protected NamedSparqlSupplier getInsertSparql(Product product) {
        if (product.getId() != null) {
            return getUpdateSparql(product);
        }
        return NamedSparqlSupplier.of(
                KEY_PREFIX_INSERT,
                () ->
                        Queries.INSERT(
                                        Product.PRODUCT_ID
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
                                                        ProductPassport.PRODUCT_PASSPORT_ID),
                                        ProductPassport.PRODUCT_PASSPORT_ID
                                                .isA(iri(AMS.ProductPassport))
                                                .andHas(iri(AMS.has), Property.PROPERTY_ID),
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
    protected NamedSparqlSupplier getUpdateSparql(Product product) {
        return NamedSparqlSupplier.of(
                KEY_PREFIX_UPDATE,
                () ->
                        Queries.INSERT(
                                        Product.PRODUCT_ID
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
                                                        ProductPassport.PRODUCT_PASSPORT_ID)
                                                .andHas(iri(AMS.has), Property.PROPERTY_ID)
                                                .andHas(
                                                        iri(AMS.specializes),
                                                        ProductClass.PRODUCT_CLASS_ID)
                                                .andHas(
                                                        iri(AMS.contains),
                                                        ProductApplication.PRODUCT_APP_ID)
                                                .andHas(
                                                        iri(AMS.hasSemantic),
                                                        SemanticReference.SEMANTIC_REFERENCE_ID),
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
                                                        SemanticReference.SEMANTIC_REFERENCE_LABEL),
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
                                                        SemanticReference.SEMANTIC_REFERENCE_LABEL),
                                        ProductPassport.PRODUCT_PASSPORT_ID
                                                .isA(iri(AMS.ProductPassport))
                                                .andHas(iri(AMS.has), Property.PROPERTY_ID),
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
                                                        SemanticReference.SEMANTIC_REFERENCE_LABEL),
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

    public static void mapProduct(BindingSet querySolution, Product product) {
        product.setId(QueryResultUtils.getIRI(querySolution, Product.PRODUCT_ID));
        product.setSourceId(
                QueryResultUtils.getStringMaybe(querySolution, Product.PRODUCT_SOURCE_ID));
        var label = querySolution.getValue(Product.PRODUCT_LABEL.getVarName());
        if (label != null) {
            product.setLabel(label.stringValue());
            if (label.isLiteral() && ((Literal) label).getLanguage().isPresent()) {
                product.setLabelLanguageCode(((Literal) label).getLanguage().get());
            }
        }
        var description = querySolution.getValue(Product.PRODUCT_DESCRIPTION.getVarName());
        if (description != null) {
            product.setDescription(description.stringValue());
            if (description.isLiteral() && ((Literal) description).getLanguage().isPresent()) {
                product.setDescriptionLanguageCode(((Literal) description).getLanguage().get());
            }
        }
    }

    public static SelectQuery getProductSelectQuery(String iri) {
        var selectQuery =
                Queries.SELECT(
                                Product.PRODUCT_ID,
                                Product.PRODUCT_SOURCE_ID,
                                Product.PRODUCT_LABEL,
                                Product.PRODUCT_DESCRIPTION,
                                Product.PRODUCT_BOM,
                                Product.PRODUCT_PROPERTIES,
                                Product.PRODUCT_PRODUCT_CLASSES,
                                Product.PRODUCT_SEMANTIC_REFERENCES,
                                Product.PRODUCT_FACTORIES,
                                Product.PRODUCT_ENTERPRISES,
                                Product.PRODUCT_SUPPLY_CHAINS,
                                Product.PRODUCT_PRODUCT_PASSPORT)
                        .where(
                                Product.PRODUCT_ID
                                        .isA(iri(AMS.Product))
                                        .and(labelPattern)
                                        .and(sourceIdPattern)
                                        .and(descriptionPattern)
                                        .and(propertyPattern)
                                        .and(factoryPattern)
                                        .and(enterprisePattern)
                                        .and(productClassesPattern)
                                        .and(billOfMaterialsPattern)
                                        .and(supplyChainsPattern)
                                        .and(semanticReferencePattern)
                                        .and(productPassportPattern));
        if (iri != null) {
            return selectQuery
                    .groupBy(
                            Product.PRODUCT_ID,
                            Product.PRODUCT_SOURCE_ID,
                            Product.PRODUCT_LABEL,
                            Product.PRODUCT_DESCRIPTION,
                            Product.PRODUCT_BOM,
                            Product.PRODUCT_PROPERTIES,
                            Product.PRODUCT_PRODUCT_CLASSES,
                            Product.PRODUCT_SEMANTIC_REFERENCES,
                            Product.PRODUCT_FACTORIES,
                            Product.PRODUCT_ENTERPRISES,
                            Product.PRODUCT_SUPPLY_CHAINS,
                            Product.PRODUCT_PRODUCT_PASSPORT)
                    .having(Expressions.equals(Product.PRODUCT_ID, iri(iri)));
        }
        return selectQuery;
    }

    @Override
    protected void populateBindingsForUpdate(MutableBindings bindingsBuilder, Product product) {
        populateProductBindingsForUpdate(bindingsBuilder, product);
    }

    public static void populateProductBindingsForUpdate(
            MutableBindings bindingsBuilder, Product product) {
        bindingsBuilder.addMaybe(Product.PRODUCT_SOURCE_ID, product.getSourceId());
        if (product.getLabel() != null && product.getLabelLanguageCode() != null) {
            var label = Values.literal(product.getLabel(), product.getLabelLanguageCode());
            bindingsBuilder.add(Product.PRODUCT_LABEL, label);
        }
        if (product.getDescription() != null && product.getDescriptionLanguageCode() != null) {
            var description =
                    Values.literal(product.getDescription(), product.getDescriptionLanguageCode());
            bindingsBuilder.add(Product.PRODUCT_DESCRIPTION, description);
        }
        if (product.getProductPassport() != null) {
            populateProductPassportBindingsForUpdate(
                    bindingsBuilder, product.getProductPassport(), false);
        }
        for (var property : product.getProperties()) {
            populatePropertyBindingsForUpdate(bindingsBuilder, property, false);
        }
        for (var productClass : product.getProductClasses()) {
            populateProductClassBindingsForUpdate(bindingsBuilder, productClass, false);
        }
        for (var semRef : product.getSemanticReferences()) {
            populateSemanticReferenceForUpdate(bindingsBuilder, semRef, false);
        }
    }

    @Override
    protected IRI getInputId(Product product) {
        if (product.getId() == null) {
            return getRdf4JTemplate().getNewUUID();
        }
        return product.getId();
    }

    @Override
    public IRI saveAndReturnId(Product product, IRI iri) {
        if (shouldDelete) {
            return super.saveAndReturnId(product, iri);
        }
        final IRI finalId = getInputId(product);
        final NamedSparqlSupplier cs = getInsertSparql(product);
        String key = KEY_PREFIX_INSERT + cs.getName();
        getRdf4JTemplate()
                .update(this.getClass(), key, cs.getSparqlSupplier())
                .withBindings(bindingsBuilder -> populateIdBindings(bindingsBuilder, finalId))
                .withBindings(
                        bindingsBuilder -> populateBindingsForUpdate(bindingsBuilder, product))
                .execute(bindings -> postProcessUpdate(product, bindings));
        return finalId;
    }
}
