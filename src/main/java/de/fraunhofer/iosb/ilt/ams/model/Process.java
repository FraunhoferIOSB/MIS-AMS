package de.fraunhofer.iosb.ilt.ams.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;

public class Process {

    public static Variable PROCESS_ID = SparqlBuilder.var("process_id");
    public static Variable PROCESS_SOURCE_ID = SparqlBuilder.var("process_source_id");
    public static Variable PROCESS_DESCRIPTION = SparqlBuilder.var("process_description");
    public static Variable PROCESS_PROPERTY = SparqlBuilder.var("process_property");
    public static Variable PROCESS_PARENT = SparqlBuilder.var("process_parent");
    public static Variable PROCESS_CHILD = SparqlBuilder.var("process_child");
    public static Variable PROCESS_CAPABILITY = SparqlBuilder.var("process_capability");
    public static Variable PROCESS_REQ_CAPABILITY =
            SparqlBuilder.var("process_required_capability");
    public static Variable PROCESS_PRELIMINARY_PRODUCT =
            SparqlBuilder.var("process_preliminary_product");
    public static Variable PROCESS_RAW_MATERIAL = SparqlBuilder.var("process_raw_material");
    public static Variable PROCESS_AUXILIARY_MATERIAL =
            SparqlBuilder.var("process_auxiliary_material");
    public static Variable PROCESS_OPERATING_MATERIAL =
            SparqlBuilder.var("process_operating_material");
    public static Variable PROCESS_END_PRODUCT = SparqlBuilder.var("process_end_product");
    public static Variable PROCESS_BY_PRODUCT = SparqlBuilder.var("process_by_product");
    public static Variable PROCESS_WASTE_PRODUCT = SparqlBuilder.var("process_waste_product");
    public static Variable PROCESS_USED = SparqlBuilder.var("process_used");
    public static Variable PROCESS_PROVIDING = SparqlBuilder.var("process_providing");

    private IRI id;
    private String sourceId;
    private String description;
    private String descriptionLanguageCode;

    private Set<Property> properties = new HashSet<>();

    private Set<Process> parentProcesses = new HashSet<>();
    private Set<Process> childProcesses = new HashSet<>();

    private Set<Capability> realizedCapabilities = new HashSet<>();
    private Set<Capability> requiredCapabilities = new HashSet<>();

    private Set<ProductApplication> preliminaryProducts = new HashSet<>();
    private Set<ProductApplication> rawMaterials = new HashSet<>();
    private Set<ProductApplication> auxiliaryMaterials = new HashSet<>();
    private Set<ProductApplication> operatingMaterials = new HashSet<>();

    private Set<ProductApplication> inputProducts = new HashSet<>();
    private Set<ProductApplication> endProducts = new HashSet<>();
    private Set<ProductApplication> byProducts = new HashSet<>();
    private Set<ProductApplication> wasteProducts = new HashSet<>();

    private Set<ProductApplication> outputProducts = new HashSet<>();

    private Set<ProductionResource> usedProductionResources = new HashSet<>();
    private Set<ProductionResource> providingProductionResources = new HashSet<>();

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

    public Set<Property> getProperties() {
        return properties;
    }

    public boolean addProperty(Property property) {
        return this.properties.add(property);
    }

    public Set<Process> getParentProcesses() {
        return parentProcesses;
    }

    public boolean addParentProcess(Process parentProcess) {
        return this.parentProcesses.add(parentProcess);
    }

    public Set<Process> getChildProcesses() {
        return childProcesses;
    }

    public boolean addChildProcess(Process childProcess) {
        return this.childProcesses.add(childProcess);
    }

    public Set<Capability> getRealizedCapabilities() {
        return realizedCapabilities;
    }

    public boolean addRealizedCapability(Capability realizedCapability) {
        return this.realizedCapabilities.add(realizedCapability);
    }

    public Set<ProductApplication> getPreliminaryProducts() {
        return preliminaryProducts;
    }

    public boolean addPreliminaryProduct(ProductApplication preliminaryProduct) {
        return this.preliminaryProducts.add(preliminaryProduct);
    }

    public Set<ProductApplication> getRawMaterials() {
        return rawMaterials;
    }

    public boolean addRawMaterial(ProductApplication rawMaterial) {
        return this.rawMaterials.add(rawMaterial);
    }

    public Set<ProductApplication> getAuxiliaryMaterials() {
        return auxiliaryMaterials;
    }

    public boolean addAuxiliaryMaterial(ProductApplication auxiliaryMaterial) {
        return this.auxiliaryMaterials.add(auxiliaryMaterial);
    }

    public Set<ProductApplication> getOperatingMaterials() {
        return operatingMaterials;
    }

    public boolean addOperatingMaterial(ProductApplication operatingMaterial) {
        return this.operatingMaterials.add(operatingMaterial);
    }

    public Set<ProductApplication> getInputProducts() {
        return inputProducts;
    }

    public Set<ProductApplication> getEndProducts() {
        return endProducts;
    }

    public boolean addEndProduct(ProductApplication endProduct) {
        return this.endProducts.add(endProduct);
    }

    public Set<ProductApplication> getByProducts() {
        return byProducts;
    }

    public boolean addByProduct(ProductApplication byProduct) {
        return this.byProducts.add(byProduct);
    }

    public Set<ProductApplication> getWasteProducts() {
        return wasteProducts;
    }

    public boolean addWasteProduct(ProductApplication wasteProduct) {
        return this.wasteProducts.add(wasteProduct);
    }

    public Set<ProductApplication> getOutputProducts() {
        return outputProducts;
    }

    public Set<ProductionResource> getUsedProductionResources() {
        return usedProductionResources;
    }

    public boolean addUsedProductionResource(ProductionResource usedProductionResource) {
        return this.usedProductionResources.add(usedProductionResource);
    }

    public Set<ProductionResource> getProvidingProductionResources() {
        return providingProductionResources;
    }

    public boolean addProvidingProductionResource(ProductionResource providingProductionResource) {
        return this.providingProductionResources.add(providingProductionResource);
    }

    public void appendInputProduct(Set<ProductApplication> toAppend) {
        this.inputProducts.addAll(toAppend);
    }

    public void appendOutputProduct(Set<ProductApplication> toAppend) {
        this.outputProducts.addAll(toAppend);
    }

    public Set<Capability> getRequiredCapabilities() {
        return requiredCapabilities;
    }

    public boolean addRequiredCapability(Capability capability) {
        return this.requiredCapabilities.add(capability);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Process process = (Process) o;
        return Objects.equals(getId(), process.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
