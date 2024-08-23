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

public class MachineInput {

    private String id;
    private String sourceId;
    private String label;
    private String labelLanguageCode;
    private String description;
    private String descriptionLanguageCode;

    private List<ProcessInput> providedProcesses;
    private List<ProcessInput> usingProcesses;
    private List<CapabilityInput> providedCapabilities;
    private List<PropertyInput> machineProperties;

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

    public List<ProcessInput> getProvidedProcesses() {
        return providedProcesses;
    }

    public void setProvidedProcesses(List<ProcessInput> providedProcesses) {
        this.providedProcesses = providedProcesses;
    }

    public List<ProcessInput> getUsingProcesses() {
        return usingProcesses;
    }

    public void setUsingProcesses(List<ProcessInput> usingProcesses) {
        this.usingProcesses = usingProcesses;
    }

    public List<CapabilityInput> getProvidedCapabilities() {
        return providedCapabilities;
    }

    public void setProvidedCapabilities(List<CapabilityInput> providedCapabilities) {
        this.providedCapabilities = providedCapabilities;
    }

    public List<PropertyInput> getMachineProperties() {
        return machineProperties;
    }

    public void setMachineProperties(List<PropertyInput> machineProperties) {
        this.machineProperties = machineProperties;
    }
}
