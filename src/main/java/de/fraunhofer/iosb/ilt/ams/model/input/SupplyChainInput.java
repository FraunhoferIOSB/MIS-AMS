package de.fraunhofer.iosb.ilt.ams.model.input;

import java.util.List;

public class SupplyChainInput {

    private String id;
    private String sourceId;
    private String description;
    private String descriptionLanguageCode;
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

    public List<SupplyChainElementInput> getSuppliers() {
        return suppliers;
    }

    public void setSuppliers(List<SupplyChainElementInput> suppliers) {
        this.suppliers = suppliers;
    }
}
