package de.fraunhofer.iosb.ilt.ams.model.filter;

import java.util.List;

public class HumanResourceFilter {
    private String id;
    private String sourceId;
    private String humanResourceId;
    private List<String> certificateIds;
    private List<PropertyFilter> propertyFilters;

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

    public String getHumanResourceId() {
        return humanResourceId;
    }

    public void setHumanResourceId(String humanResourceId) {
        this.humanResourceId = humanResourceId;
    }

    public List<String> getCertificateIds() {
        return certificateIds;
    }

    public void setCertificateIds(List<String> certificateIds) {
        this.certificateIds = certificateIds;
    }

    public List<PropertyFilter> getPropertyFilters() {
        return propertyFilters;
    }

    public void setPropertyFilters(List<PropertyFilter> propertyFilters) {
        this.propertyFilters = propertyFilters;
    }
}
