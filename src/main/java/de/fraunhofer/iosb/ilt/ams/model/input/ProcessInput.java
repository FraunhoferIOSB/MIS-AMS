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

public class ProcessInput {

    private String id;
    private String sourceId;
    private String description;
    private String descriptionLanguageCode;
    private List<PropertyInput> properties;

    private List<ProcessInput> parentProcesses;
    private List<ProcessInput> childProcesses;

    private List<CapabilityInput> realizedCapabilities;
    private List<CapabilityInput> requiredCapabilities;

    private List<ProductApplicationInput> preliminaryProducts;
    private List<ProductApplicationInput> rawMaterials;
    private List<ProductApplicationInput> auxiliaryMaterials;
    private List<ProductApplicationInput> operatingMaterials;
    private List<ProductApplicationInput> endProducts;
    private List<ProductApplicationInput> byProducts;
    private List<ProductApplicationInput> wasteProducts;
    private List<ProductApplicationInput> inputProducts;
    private List<ProductApplicationInput> outputProducts;

    private List<MachineInput> usedMachines;
    private List<HumanResourceInput> usedHumanResources;
    private List<MachineInput> providingMachines;
    private List<HumanResourceInput> providingHumanResources;

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

    public List<PropertyInput> getProperties() {
        return properties;
    }

    public void setProperties(List<PropertyInput> properties) {
        this.properties = properties;
    }

    public List<ProcessInput> getParentProcesses() {
        return parentProcesses;
    }

    public void setParentProcesses(List<ProcessInput> parentProcesses) {
        this.parentProcesses = parentProcesses;
    }

    public List<ProcessInput> getChildProcesses() {
        return childProcesses;
    }

    public void setChildProcesses(List<ProcessInput> childProcesses) {
        this.childProcesses = childProcesses;
    }

    public List<CapabilityInput> getRealizedCapabilities() {
        return realizedCapabilities;
    }

    public void setRealizedCapabilities(List<CapabilityInput> realizedCapabilities) {
        this.realizedCapabilities = realizedCapabilities;
    }

    public List<CapabilityInput> getRequiredCapabilities() {
        return requiredCapabilities;
    }

    public void setRequiredCapabilities(List<CapabilityInput> requiredCapabilities) {
        this.requiredCapabilities = requiredCapabilities;
    }

    public List<ProductApplicationInput> getPreliminaryProducts() {
        return preliminaryProducts;
    }

    public void setPreliminaryProducts(List<ProductApplicationInput> preliminaryProducts) {
        this.preliminaryProducts = preliminaryProducts;
    }

    public List<ProductApplicationInput> getRawMaterials() {
        return rawMaterials;
    }

    public void setRawMaterials(List<ProductApplicationInput> rawMaterials) {
        this.rawMaterials = rawMaterials;
    }

    public List<ProductApplicationInput> getAuxiliaryMaterials() {
        return auxiliaryMaterials;
    }

    public void setAuxiliaryMaterials(List<ProductApplicationInput> auxiliaryMaterials) {
        this.auxiliaryMaterials = auxiliaryMaterials;
    }

    public List<ProductApplicationInput> getOperatingMaterials() {
        return operatingMaterials;
    }

    public void setOperatingMaterials(List<ProductApplicationInput> operatingMaterials) {
        this.operatingMaterials = operatingMaterials;
    }

    public List<ProductApplicationInput> getEndProducts() {
        return endProducts;
    }

    public void setEndProducts(List<ProductApplicationInput> endProducts) {
        this.endProducts = endProducts;
    }

    public List<ProductApplicationInput> getByProducts() {
        return byProducts;
    }

    public void setByProducts(List<ProductApplicationInput> byProducts) {
        this.byProducts = byProducts;
    }

    public List<ProductApplicationInput> getWasteProducts() {
        return wasteProducts;
    }

    public void setWasteProducts(List<ProductApplicationInput> wasteProducts) {
        this.wasteProducts = wasteProducts;
    }

    public List<ProductApplicationInput> getInputProducts() {
        return inputProducts;
    }

    public void setInputProducts(List<ProductApplicationInput> inputProducts) {
        this.inputProducts = inputProducts;
    }

    public List<ProductApplicationInput> getOutputProducts() {
        return outputProducts;
    }

    public void setOutputProducts(List<ProductApplicationInput> outputProducts) {
        this.outputProducts = outputProducts;
    }

    public List<MachineInput> getUsedMachines() {
        return usedMachines;
    }

    public void setUsedMachines(List<MachineInput> usedMachines) {
        this.usedMachines = usedMachines;
    }

    public List<HumanResourceInput> getUsedHumanResources() {
        return usedHumanResources;
    }

    public void setUsedHumanResources(List<HumanResourceInput> usedHumanResources) {
        this.usedHumanResources = usedHumanResources;
    }

    public List<MachineInput> getProvidingMachines() {
        return providingMachines;
    }

    public void setProvidingMachines(List<MachineInput> providingMachines) {
        this.providingMachines = providingMachines;
    }

    public List<HumanResourceInput> getProvidingHumanResources() {
        return providingHumanResources;
    }

    public void setProvidingHumanResources(List<HumanResourceInput> providingHumanResources) {
        this.providingHumanResources = providingHumanResources;
    }
}
