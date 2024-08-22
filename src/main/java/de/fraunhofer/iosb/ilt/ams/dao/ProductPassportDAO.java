package de.fraunhofer.iosb.ilt.ams.dao;

import static de.fraunhofer.iosb.ilt.ams.dao.PropertyDAO.populatePropertyBindingsForUpdate;
import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri;

import de.fraunhofer.iosb.ilt.ams.AMS;
import de.fraunhofer.iosb.ilt.ams.model.*;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.rdf4j.model.IRI;
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
public class ProductPassportDAO extends RDF4JCRUDDao<ProductPassport, ProductPassport, IRI> {

    public static final GraphPatternNotTriples propertyPattern =
            GraphPatterns.optional(
                    GraphPatterns.and(
                            ProductPassport.PRODUCT_PASSPORT_ID.has(
                                    iri(AMS.has), ProductPassport.PRODUCT_PASSPORT_PROPERTIES),
                            ProductPassport.PRODUCT_PASSPORT_PROPERTIES.isA(AMS.Property)));

    public static final GraphPatternNotTriples sourceIdPattern =
            GraphPatterns.optional(
                    ProductPassport.PRODUCT_PASSPORT_ID.has(
                            iri(AMS.externalIdentifier),
                            ProductPassport.PRODUCT_PASSPORT_SOURCE_ID));
    public static final GraphPatternNotTriples identifierPattern =
            GraphPatterns.optional(
                    ProductPassport.PRODUCT_PASSPORT_ID.has(
                            iri(AMS.identifier), ProductPassport.PRODUCT_PASSPORT_IDENTIFIER));

    @Autowired ObjectRdf4jRepository repo;
    List<ProductPassport> productPassports;

    public ProductPassportDAO(RDF4JTemplate rdf4JTemplate) {
        super(rdf4JTemplate);
    }

    @Override
    protected void populateIdBindings(MutableBindings bindingsBuilder, IRI iri) {
        bindingsBuilder.add(ProductPassport.PRODUCT_PASSPORT_ID, iri);
    }

    @Override
    protected NamedSparqlSupplierPreparer prepareNamedSparqlSuppliers(
            NamedSparqlSupplierPreparer preparer) {
        return null;
    }

    @Override
    protected String getReadQuery() {
        productPassports = new LinkedList<>();
        return getProductPassportSelectQuery(null)
                .from(repo.getGraphNameForQuery())
                .getQueryString();
    }

    @Override
    protected ProductPassport mapSolution(BindingSet querySolution) {
        ProductPassport productPassport = null;

        for (ProductPassport pp : productPassports) {
            if (pp.getId()
                    .equals(
                            QueryResultUtils.getIRI(
                                    querySolution, ProductPassport.PRODUCT_PASSPORT_ID))) {
                productPassport = pp;
                break;
            }
        }
        if (productPassport == null) {
            productPassport = new ProductPassport();
            mapProductPassport(querySolution, productPassport);
            productPassports.add(productPassport);
        }
        var propertyId =
                QueryResultUtils.getIRIMaybe(
                        querySolution, ProductPassport.PRODUCT_PASSPORT_PROPERTIES);
        if (propertyId != null) {
            productPassport.addProperty(repo.getPropertyById(propertyId));
        }
        return productPassport;
    }

    public static void mapProductPassport(
            BindingSet querySolution, ProductPassport productPassport) {
        productPassport.setId(
                QueryResultUtils.getIRI(querySolution, ProductPassport.PRODUCT_PASSPORT_ID));
        productPassport.setSourceId(
                QueryResultUtils.getStringMaybe(
                        querySolution, ProductPassport.PRODUCT_PASSPORT_SOURCE_ID));
        productPassport.setIdentifier(
                QueryResultUtils.getStringMaybe(
                        querySolution, ProductPassport.PRODUCT_PASSPORT_IDENTIFIER));
    }

    public static SelectQuery getProductPassportSelectQuery(String iri) {
        SelectQuery selectQuery =
                Queries.SELECT(
                                ProductPassport.PRODUCT_PASSPORT_ID,
                                ProductPassport.PRODUCT_PASSPORT_SOURCE_ID,
                                ProductPassport.PRODUCT_PASSPORT_IDENTIFIER,
                                ProductPassport.PRODUCT_PASSPORT_PROPERTIES)
                        .where(
                                ProductPassport.PRODUCT_PASSPORT_ID
                                        .isA(iri(AMS.ProductPassport))
                                        .and(sourceIdPattern)
                                        .and(identifierPattern)
                                        .and(propertyPattern));
        if (iri != null) {
            return selectQuery
                    .groupBy(
                            ProductPassport.PRODUCT_PASSPORT_ID,
                            ProductPassport.PRODUCT_PASSPORT_SOURCE_ID,
                            ProductPassport.PRODUCT_PASSPORT_IDENTIFIER,
                            ProductPassport.PRODUCT_PASSPORT_PROPERTIES)
                    .having(Expressions.equals(ProductPassport.PRODUCT_PASSPORT_ID, iri(iri)));
        }
        return selectQuery;
    }

    @Override
    protected NamedSparqlSupplier getInsertSparql(ProductPassport productPassport) {
        return NamedSparqlSupplier.of(
                KEY_PREFIX_INSERT,
                () ->
                        Queries.INSERT(
                                        ProductPassport.PRODUCT_PASSPORT_ID
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
                                                .andHas(iri(AMS.value), Property.PROPERTY_VALUE)
                                                .andHas(
                                                        iri(AMS.hasSemantic),
                                                        Property.PROPERTY_SEMANTIC_REFERENCES),
                                        Property.PROPERTY_SEMANTIC_REFERENCES
                                                .isA(iri(AMS.SemanticReference))
                                                .andHas(
                                                        iri(AMS.uri),
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
            MutableBindings bindingsBuilder, ProductPassport productPassport) {
        populateProductPassportBindingsForUpdate(bindingsBuilder, productPassport, true);
    }

    public static void populateProductPassportBindingsForUpdate(
            MutableBindings bindingsBuilder, ProductPassport productPassport, boolean isUpdate) {
        if (!isUpdate) {
            bindingsBuilder.add(ProductPassport.PRODUCT_PASSPORT_ID, productPassport.getId());
        }
        for (var property : productPassport.getProperties()) {
            populatePropertyBindingsForUpdate(bindingsBuilder, property, false);
        }
    }

    @Override
    protected IRI getInputId(ProductPassport productPassport) {
        if (productPassport.getId() == null) {
            return getRdf4JTemplate().getNewUUID();
        }
        return productPassport.getId();
    }
}
