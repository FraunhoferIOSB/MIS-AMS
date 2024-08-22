package de.fraunhofer.iosb.ilt.ams.model.input;

import java.util.List;

public class CapabilityInput {
    private String id;
    private String sourceId;
    private String label;
    private String labelLanguageCode;
    private String description;
    private String descriptionLanguageCode;
    private List<SemanticReferenceInput> semanticReferences;
    private List<PropertyInput> properties;
    private List<CapabilityInput> childCapabilities;
    private List<CapabilityInput> parentCapabilities;

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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabelLanguageCode() {
        return labelLanguageCode;
    }

    public void setLabelLanguageCode(String labelLanguageCode) {
        this.labelLanguageCode = labelLanguageCode;
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

    public List<SemanticReferenceInput> getSemanticReferences() {
        return semanticReferences;
    }

    public void setSemanticReferences(List<SemanticReferenceInput> semanticReferences) {
        this.semanticReferences = semanticReferences;
    }

    public List<PropertyInput> getProperties() {
        return properties;
    }

    public void setProperties(List<PropertyInput> properties) {
        this.properties = properties;
    }

    public List<CapabilityInput> getChildCapabilities() {
        return childCapabilities;
    }

    public List<CapabilityInput> getParentCapabilities() {
        return parentCapabilities;
    }
}
