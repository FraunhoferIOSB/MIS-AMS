package de.fraunhofer.iosb.ilt.ams.model.input;

import java.util.List;

public class EnterpriseInput {
    private String id;
    private String sourceId;
    private String label;
    private String labelLanguageCode;
    private String description;
    private String descriptionLanguageCode;
    private LocationInput location;
    private List<FactoryInput> factories;
    private List<EnterpriseInput> subsidiaryEnterprises;
    private List<PropertyInput> properties;
    private List<ProductInput> products;
    private List<PropertyInput> certificates;
    private String logo;

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
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

    public LocationInput getLocation() {
        return location;
    }

    public void setLocation(LocationInput location) {
        this.location = location;
    }

    public String getLabelLanguageCode() {
        return labelLanguageCode;
    }

    public void setLabelLanguageCode(String labelLanguageCode) {
        this.labelLanguageCode = labelLanguageCode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<FactoryInput> getFactories() {
        return factories;
    }

    public void setFactories(List<FactoryInput> factories) {
        this.factories = factories;
    }

    public List<EnterpriseInput> getSubsidiaryEnterprises() {
        return subsidiaryEnterprises;
    }

    public void setSubsidiaryEnterprises(List<EnterpriseInput> subsidiaryEnterprises) {
        this.subsidiaryEnterprises = subsidiaryEnterprises;
    }

    public List<PropertyInput> getProperties() {
        return properties;
    }

    public void setProperties(List<PropertyInput> properties) {
        this.properties = properties;
    }

    public List<ProductInput> getProducts() {
        return products;
    }

    public void setProducts(List<ProductInput> products) {
        this.products = products;
    }

    public List<PropertyInput> getCertificates() {
        return certificates;
    }

    public void setCertificates(List<PropertyInput> certificates) {
        this.certificates = certificates;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }
}
