package de.fraunhofer.iosb.ilt.ams.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;

public class SupplyChain {

    public static Variable SUPPLY_CHAIN_ID = SparqlBuilder.var("supply_chain_id");
    public static Variable SUPPLY_CHAIN_SOURCE_ID = SparqlBuilder.var("supply_chain_source_id");
    public static Variable SUPPLY_CHAIN_DESCRIPTION = SparqlBuilder.var("supply_chain_description");
    public static Variable SUPPLY_CHAIN_SUPPLIER = SparqlBuilder.var("supply_chain_supplier");

    private IRI id;
    private String sourceId;
    private String description;
    private String descriptionLanguageCode;
    private Set<SupplyChainElement> suppliers = new HashSet<>();

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

    public Set<SupplyChainElement> getSuppliers() {
        return suppliers;
    }

    public boolean addSupplier(SupplyChainElement supplier) {
        return this.suppliers.add(supplier);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SupplyChain that = (SupplyChain) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
