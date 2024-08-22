package de.fraunhofer.iosb.ilt.ams.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;

public class ProductApplication {

    public static final Variable PRODUCT_APP_ID = SparqlBuilder.var("product_app_id");
    public static final Variable PRODUCT_APP_SOURCE_ID = SparqlBuilder.var("product_app_source_id");
    public static final Variable PRODUCT_APP_PRODUCT = SparqlBuilder.var("product_app_product");
    public static final Variable PRODUCT_APP_QUANTITY = SparqlBuilder.var("product_app_quantity");
    public static final Variable PRODUCT_APP_PROPERTY = SparqlBuilder.var("product_app_property");

    private IRI id;
    private String sourceId;
    private Product product;
    private Property quantity;
    private Set<Property> properties = new HashSet<>();

    public IRI getId() {
        return id;
    }

    public void setId(IRI id) {
        this.id = id;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Property getQuantity() {
        return quantity;
    }

    public void setQuantity(Property quantity) {
        this.quantity = quantity;
    }

    public Set<Property> getProperties() {
        return properties;
    }

    public boolean addProperty(Property property) {
        return this.properties.add(property);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductApplication that = (ProductApplication) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
