package de.fraunhofer.iosb.ilt.ams.model;

import java.net.URI;
import java.util.Objects;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;

public class SemanticReference {
    private IRI id;
    private URI sourceUri;
    private String label;
    private String labelLanguageCode;
    private String description;
    private String descriptionLanguageCode;

    public static final Variable SEMANTIC_REFERENCE_ID = SparqlBuilder.var("semantic_reference_id");
    public static final Variable SEMANTIC_REFERENCE_SOURCE_URI =
            SparqlBuilder.var("semantic_reference_source_id");
    public static final Variable SEMANTIC_REFERENCE_LABEL =
            SparqlBuilder.var("semantic_reference_label");
    public static final Variable SEMANTIC_REFERENCE_DESCRIPTION =
            SparqlBuilder.var("semantic_reference_description");

    public IRI getId() {
        return id;
    }

    public void setId(IRI id) {
        this.id = id;
    }

    public URI getSourceUri() {
        return sourceUri;
    }

    public void setSourceUri(URI sourceUri) {
        this.sourceUri = sourceUri;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SemanticReference that = (SemanticReference) o;
        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
