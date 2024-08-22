package de.fraunhofer.iosb.ilt.ams.model.input;

import java.util.List;

public class SupplyChainElementInput {
    private String id;
    private String sourceId;
    private String description;
    private String descriptionLanguageCode;
    private List<ProductApplicationInput> products;
    private EnterpriseInput enterprise;
    private FactoryInput factory;
    private List<SupplyChainElementInput> suppliers;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionLanguageCode() {
        return descriptionLanguageCode;
    }

    public void setDescriptionLanguageCode(String descriptionLanguageCode) {
        this.descriptionLanguageCode = descriptionLanguageCode;
    }

    public List<ProductApplicationInput> getProducts() {
        return products;
    }

    public void setProducts(List<ProductApplicationInput> products) {
        this.products = products;
    }

    public EnterpriseInput getEnterprise() {
        return enterprise;
    }

    public void setEnterprise(EnterpriseInput enterprise) {
        this.enterprise = enterprise;
    }

    public FactoryInput getFactory() {
        return factory;
    }

    public void setFactory(FactoryInput factory) {
        this.factory = factory;
    }

    public List<SupplyChainElementInput> getSuppliers() {
        return suppliers;
    }

    public void setSuppliers(List<SupplyChainElementInput> suppliers) {
        this.suppliers = suppliers;
    }
}
