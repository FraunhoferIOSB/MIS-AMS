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
package de.fraunhofer.iosb.ilt.ams.model.input;

import java.util.List;

public class SupplyChainElementInput {
    private String id;
    private String sourceId;
    private String description;
    private String descriptionLanguageCode;
    private List<ProductApplicationInput> products;
    private EnterpriseInput enterprise;
    private FactoryInput factory;
    private List<SupplyChainElementInput> suppliers;

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public List<ProductApplicationInput> getProducts() {
        return products;
    }

    public void setProducts(List<ProductApplicationInput> products) {
        this.products = products;
    }

    public EnterpriseInput getEnterprise() {
        return enterprise;
    }

    public void setEnterprise(EnterpriseInput enterprise) {
        this.enterprise = enterprise;
    }

    public FactoryInput getFactory() {
        return factory;
    }

    public void setFactory(FactoryInput factory) {
        this.factory = factory;
    }

    public List<SupplyChainElementInput> getSuppliers() {
        return suppliers;
    }

    public void setSuppliers(List<SupplyChainElementInput> suppliers) {
        this.suppliers = suppliers;
    }
}
