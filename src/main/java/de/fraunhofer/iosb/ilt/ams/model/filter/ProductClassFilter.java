package de.fraunhofer.iosb.ilt.ams.model.filter;

public class ProductClassFilter {
    private String id;
    private String sourceId;
    private String semanticReferenceId;

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

    public String getSemanticReferenceId() {
        return semanticReferenceId;
    }

    public void setSemanticReferenceId(String semanticReferenceId) {
        this.semanticReferenceId = semanticReferenceId;
    }
}