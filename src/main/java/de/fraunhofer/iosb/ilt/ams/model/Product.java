package de.fraunhofer.iosb.ilt.ams.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;

public class Product {

    public static final Variable PRODUCT_ID = SparqlBuilder.var("product_id");
    public static final Variable PRODUCT_SOURCE_ID = SparqlBuilder.var("product_source_id");
    public static final Variable PRODUCT_LABEL = SparqlBuilder.var("product_label");
    public static final Variable PRODUCT_DESCRIPTION = SparqlBuilder.var("product_description");
    public static final Variable PRODUCT_BOM = SparqlBuilder.var("product_bom");
    public static final Variable PRODUCT_PROPERTIES = SparqlBuilder.var("product_properties");
    public static final Variable PRODUCT_PRODUCT_CLASSES =
            SparqlBuilder.var("product_product_classes");
    public static final Variable PRODUCT_SEMANTIC_REFERENCES =
            SparqlBuilder.var("product_semantic_references");
    public static final Variable PRODUCT_FACTORIES = SparqlBuilder.var("product_factories");
    public static final Variable PRODUCT_ENTERPRISES = SparqlBuilder.var("product_enterprises");
    public static final Variable PRODUCT_SUPPLY_CHAINS = SparqlBuilder.var("product_supply_chains");
    public static final Variable PRODUCT_PRODUCT_PASSPORT =
            SparqlBuilder.var("product_product_passport");

    private IRI id;
    private String sourceId;
    private String label;
    private String labelLanguageCode;
    private String description;
    private String descriptionLanguageCode;

    private Set<ProductApplication> billOfMaterials = new HashSet<>();

    private Set<Property> properties = new HashSet<>();

    private Set<ProductClass> productClasses = new HashSet<>();

    private Set<SemanticReference> semanticReferences = new HashSet<>();

    private Set<Factory> factories = new HashSet<>();

    private Set<Enterprise> enterprises = new HashSet<>();

    private Set<SupplyChain> supplyChains = new HashSet<>();

    private ProductPassport productPassport;

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

    public Set<ProductApplication> getBillOfMaterials() {
        return billOfMaterials;
    }

    public boolean addBillOfMaterial(ProductApplication billOfMaterial) {
        return this.billOfMaterials.add(billOfMaterial);
    }

    public Set<Property> getProperties() {
        return properties;
    }

    public boolean addProperty(Property property) {
        return this.properties.add(property);
    }

    public boolean removeProperty(Property property) {
        return this.properties.remove(property);
    }

    public Set<ProductClass> getProductClasses() {
        return productClasses;
    }

    public boolean addProductClass(ProductClass productClass) {
        return this.productClasses.add(productClass);
    }

    public Set<SemanticReference> getSemanticReferences() {
        return semanticReferences;
    }

    public boolean addSemanticReference(SemanticReference semanticReference) {
        return this.semanticReferences.add(semanticReference);
    }

    public Set<Factory> getFactories() {
        return factories;
    }

    public boolean addFactory(Factory factory) {
        return this.factories.add(factory);
    }

    public Set<Enterprise> getEnterprises() {
        return enterprises;
    }

    public boolean addEnterprise(Enterprise enterprise) {
        return this.enterprises.add(enterprise);
    }

    public Set<SupplyChain> getSupplyChains() {
        return supplyChains;
    }

    public boolean setSupplyChains(SupplyChain supplyChain) {
        return this.supplyChains.add(supplyChain);
    }

    public void setProductPassport(ProductPassport productPassport) {
        this.productPassport = productPassport;
    }

    public ProductPassport getProductPassport() {
        return productPassport;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return getId().equals(product.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
