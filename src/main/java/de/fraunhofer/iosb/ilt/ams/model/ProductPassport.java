package de.fraunhofer.iosb.ilt.ams.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;

public class ProductPassport {

    public static final Variable PRODUCT_PASSPORT_ID = SparqlBuilder.var("product_passport_id");
    public static final Variable PRODUCT_PASSPORT_SOURCE_ID =
            SparqlBuilder.var("product_passport_source_id");
    public static final Variable PRODUCT_PASSPORT_IDENTIFIER =
            SparqlBuilder.var("product_passport_identifier");
    public static final Variable PRODUCT_PASSPORT_PROPERTIES =
            SparqlBuilder.var("product_passport_properties");

    private IRI id;
    private String sourceId;
    private String identifier;
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

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
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
        ProductPassport that = (ProductPassport) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
