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

public class ProductInput {

    private String id;
    private String sourceId;
    private String label;
    private String labelLanguageCode;
    private String description;
    private String descriptionLanguageCode;
    private List<ProductApplicationInput> billOfMaterials;
    private ProductPassportInput productPassport;
    private List<SupplyChainInput> supplyChains;
    private List<PropertyInput> properties;
    private List<SemanticReferenceInput> semanticReferences;

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

    public ProductPassportInput getProductPassportInput() {
        return productPassport;
    }

    public void setProductPassportInput(ProductPassportInput productPassportInput) {
        this.productPassport = productPassportInput;
    }

    public List<ProductApplicationInput> getBillOfMaterials() {
        return billOfMaterials;
    }

    public void setBillOfMaterials(List<ProductApplicationInput> billOfMaterials) {
        this.billOfMaterials = billOfMaterials;
    }

    public List<SupplyChainInput> getSupplyChains() {
        return supplyChains;
    }

    public void setSupplyChains(List<SupplyChainInput> supplyChains) {
        this.supplyChains = supplyChains;
    }

    public List<PropertyInput> getProperties() {
        return properties;
    }

    public void setProperties(List<PropertyInput> properties) {
        this.properties = properties;
    }

    public List<SemanticReferenceInput> getSemanticReferences() {
        return semanticReferences;
    }

    public void setSemanticReferences(List<SemanticReferenceInput> semanticReferences) {
        this.semanticReferences = semanticReferences;
    }
}
