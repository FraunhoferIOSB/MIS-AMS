package de.fraunhofer.iosb.ilt.ams.model.input;

import java.util.List;

public class ProductApplicationInput {

    private String id;
    private String sourceId;
    private ProductInput product;
    private PropertyInput quantity;
    private List<PropertyInput> properties;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public ProductInput getProduct() {
        return product;
    }

    public void setProduct(ProductInput product) {
        this.product = product;
    }

    public PropertyInput getQuantity() {
        return quantity;
    }

    public void setQuantity(PropertyInput quantity) {
        this.quantity = quantity;
    }

    public List<PropertyInput> getProperties() {
        return properties;
    }

    public void setProperties(List<PropertyInput> properties) {
        this.properties = properties;
    }
}
