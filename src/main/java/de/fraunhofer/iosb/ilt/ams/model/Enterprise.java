package de.fraunhofer.iosb.ilt.ams.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;

public class Enterprise {

    private IRI id;
    private String sourceId;
    private String label;
    private String labelLanguageCode;
    private String description;
    private String descriptionLanguageCode;
    private Location location;
    private Set<Factory> factories = new HashSet<>();
    private Set<Enterprise> subsidiaryEnterprises = new HashSet<>();
    private Set<Property> properties = new HashSet<>();
    private Set<Product> products = new HashSet<>();
    private Set<Process> processes = new HashSet<>();
    private Set<ProductionResource> productionResources = new HashSet<>();
    private Set<SupplyChain> supplyChains = new HashSet<>();
    private Set<Property> certificates = new HashSet<>();
    private String logo;

    public static final Variable ENTERPRISE_ID = SparqlBuilder.var("enterprise_id");
    public static final Variable ENTERPRISE_SOURCE_ID = SparqlBuilder.var("enterprise_source_id");
    public static final Variable ENTERPRISE_LABEL = SparqlBuilder.var("enterprise_label");
    public static final Variable ENTERPRISE_LABEL_LANGUAGE_CODE =
            SparqlBuilder.var("enterprise_label_language_code");
    public static final Variable ENTERPRISE_DESCRIPTION =
            SparqlBuilder.var("enterprise_description");
    public static final Variable ENTERPRISE_DESCRIPTION_LANGUAGE_CODE =
            SparqlBuilder.var("enterprise_description_language_code");
    public static final Variable ENTERPRISE_LOCATION = SparqlBuilder.var("enterprise_location");
    public static final Variable ENTERPRISE_FACTORIES = SparqlBuilder.var("enterprise_factories");
    public static final Variable ENTERPRISE_SUBSIDIARY_ENTERPRISES =
            SparqlBuilder.var("enterprise_subsidiary_enterprises");
    public static final Variable ENTERPRISE_PROPERTIES = SparqlBuilder.var("enterprise_properties");
    public static final Variable ENTERPRISE_PRODUCTS = SparqlBuilder.var("enterprise_products");
    public static final Variable ENTERPRISE_PROCESSES = SparqlBuilder.var("enterprise_processes");
    public static final Variable ENTERPRISE_PRODUCTION_RESOURCES =
            SparqlBuilder.var("enterprise_production_resources");
    public static final Variable ENTERPRISE_SUPPLY_CHAINS =
            SparqlBuilder.var("enterprise_supply_chains");
    public static final Variable ENTERPRISE_CERTIFICATES =
            SparqlBuilder.var("enterprise_certificates");
    public static final Variable ENTERPRISE_LOGO = SparqlBuilder.var("enterprise_logo");

    public String getLabelLanguageCode() {
        return labelLanguageCode;
    }

    public void setLabelLanguageCode(String labelLanguageCode) {
        this.labelLanguageCode = labelLanguageCode;
    }

    public Set<ProductionResource> getProductionResources() {
        return productionResources;
    }

    public boolean addProductionResource(ProductionResource productionResource) {
        return this.productionResources.add(productionResource);
    }

    public Set<SupplyChain> getSupplyChains() {
        return supplyChains;
    }

    public boolean addSupplyChain(SupplyChain supplyChain) {
        return this.supplyChains.add(supplyChain);
    }

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

    public Set<Factory> getFactories() {
        return factories;
    }

    public boolean addFactory(Factory factory) {
        return this.factories.add(factory);
    }

    public Set<Enterprise> getSubsidiaryEnterprises() {
        return subsidiaryEnterprises;
    }

    public boolean addSubsidiaryEnterprise(Enterprise enterprise) {
        return this.subsidiaryEnterprises.add(enterprise);
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

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Enterprise that = (Enterprise) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
