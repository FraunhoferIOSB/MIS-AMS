package de.fraunhofer.iosb.ilt.ams.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;

public class Factory {
    private IRI id;
    private String sourceId;
    private String label;
    private String labelLanguageCode;
    private String description;
    private String descriptionLanguageCode;
    private Location location;
    private Set<Property> properties = new HashSet<>();
    private Set<Product> products = new HashSet<>();
    private Set<Machine> machines = new HashSet<>();
    private Set<HumanResource> humanResources = new HashSet<>();
    private Enterprise enterprise;
    private Set<Process> processes = new HashSet<>();
    private Set<Property> certificates = new HashSet<>();

    public static final Variable FACTORY_ID = SparqlBuilder.var("factory_id");
    public static final Variable FACTORY_SOURCE_ID = SparqlBuilder.var("factory_source_id");
    public static final Variable FACTORY_LABEL = SparqlBuilder.var("factory_label");
    public static final Variable FACTORY_LABEL_LANGUAGE_CODE =
            SparqlBuilder.var("factory_label_language_code");
    public static final Variable FACTORY_DESCRIPTION = SparqlBuilder.var("factory_description");
    public static final Variable FACTORY_DESCRIPTION_LANGUAGE_CODE =
            SparqlBuilder.var("factory_description_language_code");
    public static final Variable FACTORY_LOCATION = SparqlBuilder.var("factory_location");
    public static final Variable FACTORY_PROPERTIES = SparqlBuilder.var("factory_properties");
    public static final Variable FACTORY_PRODUCTS = SparqlBuilder.var("factory_products");
    public static final Variable FACTORY_MACHINES = SparqlBuilder.var("factory_machines");
    public static final Variable FACTORY_HUMAN_RESOURCES =
            SparqlBuilder.var("factory_human_resources");
    public static final Variable FACTORY_ENTERPRISE = SparqlBuilder.var("factory_enterprise");
    public static final Variable FACTORY_PROCESSES = SparqlBuilder.var("factory_processes");
    public static final Variable FACTORY_CERTIFICATES = SparqlBuilder.var("factory_certificates");

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

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Set<Property> getProperties() {
        return properties;
    }

    public boolean addProperty(Property property) {
        return this.properties.add(property);
    }

    public Set<Product> getProducts() {
        return products;
    }

    public boolean addProduct(Product product) {
        return this.products.add(product);
    }

    public Set<Machine> getMachines() {
        return machines;
    }

    public boolean addMachine(Machine machine) {

        return this.machines.add(machine);
    }

    public Set<HumanResource> getHumanResources() {
        return humanResources;
    }

    public boolean addHumanResource(HumanResource humanResource) {
        return this.humanResources.add(humanResource);
    }

    public Enterprise getEnterprise() {
        return enterprise;
    }

    public void setEnterprise(Enterprise enterprise) {
        this.enterprise = enterprise;
    }

    public Set<Process> getProcesses() {
        return processes;
    }

    public boolean addProcess(Process process) {
        return this.processes.add(process);
    }

    public Set<Property> getCertificates() {
        return certificates;
    }

    public boolean addCertificate(Property certificate) {
        return this.certificates.add(certificate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Factory factory = (Factory) o;
        return Objects.equals(getId(), factory.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
