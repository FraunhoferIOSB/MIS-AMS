package de.fraunhofer.iosb.ilt.ams.model;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;

public class HumanResource extends ProductionResource {
    public static final Variable HUMAN_RESOURCE_ID = SparqlBuilder.var("human_resource_id");
    public static final Variable HUMAN_RESOURCE_CERTIFICATES =
            SparqlBuilder.var("human_resource_certificates");
    public static final Variable HUMAN_RESOURCE_PROPERTIES =
            SparqlBuilder.var("human_resource_properties");

    private Set<Property> certificates = new HashSet<>();
    private Set<Property> properties = new HashSet<>();

    public Set<Property> getCertificates() {
        return certificates;
    }

    public boolean addCertificate(Property certificate) {
        return this.certificates.add(certificate);
    }

    public Set<Property> getProperties() {
        return properties;
    }

    public boolean addProperty(Property property) {
        return this.properties.add(property);
    }
}
