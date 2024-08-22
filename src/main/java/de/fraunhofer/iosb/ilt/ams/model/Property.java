package de.fraunhofer.iosb.ilt.ams.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;

public class Property {
    private IRI id;
    private String sourceId;
    private String label;
    private String labelLanguageCode;
    private String description;
    private String descriptionLanguageCode;
    private Set<SemanticReference> semanticReferences = new HashSet<>();
    private String value;

    public static final Variable PROPERTY_ID = SparqlBuilder.var("property_id");
    public static final Variable PROPERTY_SOURCE_ID = SparqlBuilder.var("property_source_id");
    public static final Variable PROPERTY_LABEL = SparqlBuilder.var("property_label");
    public static final Variable PROPERTY_DESCRIPTION = SparqlBuilder.var("property_description");
    public static final Variable PROPERTY_SEMANTIC_REFERENCES =
            SparqlBuilder.var("property_semantic_references");
    public static final Variable PROPERTY_VALUE = SparqlBuilder.var("property_value");

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

    public Set<SemanticReference> getSemanticReferences() {
        return semanticReferences;
    }

    public boolean addSemanticReference(SemanticReference semanticReference) {
        return this.semanticReferences.add(semanticReference);
    }

    public boolean removeSemanticReference(SemanticReference semanticReference) {
        return this.semanticReferences.remove(semanticReference);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Property property = (Property) o;
        return getId().equals(property.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
