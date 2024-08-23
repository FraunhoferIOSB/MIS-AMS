/*
 * Copyright (c) 2024 Fraunhofer IOSB, eine rechtlich nicht selbstaendige
 * Einrichtung der Fraunhofer-Gesellschaft zur Foerderung der angewandten
 * Forschung e.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.iosb.ilt.ams.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;

public class SupplyChainElement {
    public static Variable SCE_ID = SparqlBuilder.var("sce_id");
    public static Variable SCE_SOURCE_ID = SparqlBuilder.var("sce_source_id");
    public static Variable SCE_DESCRIPTION = SparqlBuilder.var("sce_description");
    public static Variable SCE_PRODUCT = SparqlBuilder.var("sce_product");
    public static Variable SCE_ENTERPRISE = SparqlBuilder.var("sce_enterprise");
    public static Variable SCE_FACTORY = SparqlBuilder.var("sce_factory");
    public static Variable SCE_SUPPLIER = SparqlBuilder.var("sce_supplier");

    private IRI id;
    private String sourceId;
    private String description;
    private String descriptionLanguageCode;
    private Set<ProductApplication> products = new HashSet<>();
    private Enterprise enterprise;
    private Factory factory;
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

    public Set<ProductApplication> getProducts() {
        return products;
    }

    public boolean addProduct(ProductApplication product) {
        return this.products.add(product);
    }

    public Enterprise getEnterprise() {
        return enterprise;
    }

    public void setEnterprise(Enterprise enterprise) {
        this.enterprise = enterprise;
    }

    public Factory getFactory() {
        return factory;
    }

    public void setFactory(Factory factory) {
        this.factory = factory;
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
        SupplyChainElement that = (SupplyChainElement) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
