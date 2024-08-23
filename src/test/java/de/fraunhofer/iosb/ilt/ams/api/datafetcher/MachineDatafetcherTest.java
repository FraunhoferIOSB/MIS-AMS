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
package de.fraunhofer.iosb.ilt.ams.api.datafetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.fraunhofer.iosb.ilt.ams.TestConfig;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@ContextConfiguration(classes = {TestConfig.class})
@ExtendWith({MockitoExtension.class, SpringExtension.class})
@TestPropertySource("classpath:application-test.properties")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MachineDatafetcherTest {
    private static final String HEADER_NAME = "Authorization";

    @Value("${test.token}")
    String token;

    @Value("${test.graph}")
    String graph;

    private static String MACHINE_ID = "";
    private static String dummyCapabilityId = "";
    private static String dummyProcessId = "";
    private static String bulkImportMachineId = "";
    private static String bulkImportMachineCapabilityId = "";
    private static String bulkImportMachinePropertyId = "";
    private static String bulkImportMachineCapabilityPropertyId = "";

    @Autowired private MockMvc mockMvc;

    private ObjectMapper mapper;

    @BeforeEach
    public void setUp() {
        mapper = new ObjectMapper();
    }

    @Test
    public void machine() throws Exception {
        String requestText =
                "{\"query\":\"query {\\r"
                        + "\\n"
                        + "  machine {\\r"
                        + "\\n"
                        + "    id\\r"
                        + "\\n"
                        + "  }\\r"
                        + "\\n"
                        + "}\",\"variables\":{}}";

        mockMvc.perform(
                        post("/graphql")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestText)
                                .header(HEADER_NAME, "Bearer " + token))
                .andDo(print())
                .andExpect(content().string(containsString("\"id\":\"" + graph)));
    }

    @Test
    @Order(0)
    public void createMachine() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r"
                        + "\\n"
                        + "  createMachine(machine: \\r"
                        + "\\n"
                        + "    {\\r"
                        + "\\n"
                        + "      sourceId : \\\"Heating_Machine\\\",\\r"
                        + "\\n"
                        + "      label: \\\"Heating Machine\\\",\\r"
                        + "\\n"
                        + "      labelLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "      description: \\\"This is a test machine\\\",\\r"
                        + "\\n"
                        + "      descriptionLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "    }) {code\\r"
                        + "\\n"
                        + "  message machine {id sourceId label description}}\\r"
                        + "\\n"
                        + "}\",\"variables\":{}}";

        var request =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestText)
                                        .header(HEADER_NAME, "Bearer " + token))
                        .andReturn();

        var body = request.getResponse().getContentAsString();
        var bodyJson = mapper.readTree(body);
        var id = bodyJson.get("data").get("createMachine").get("machine").get("id").textValue();
        MACHINE_ID = id;
        var sourceId =
                bodyJson.get("data")
                        .get("createMachine")
                        .get("machine")
                        .get("sourceId")
                        .textValue();
        var description =
                bodyJson.get("data")
                        .get("createMachine")
                        .get("machine")
                        .get("description")
                        .textValue();
        var label =
                bodyJson.get("data").get("createMachine").get("machine").get("label").textValue();

        assertEquals(sourceId, "Heating_Machine");
        assertEquals(label, "Heating Machine");
        assertEquals(description, "This is a test machine");

        var checkRequest =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"query\":\"query {\\r"
                                                        + "\\n"
                                                        + "  machine {\\r"
                                                        + "\\n"
                                                        + "    id\\r"
                                                        + "\\n"
                                                        + "  }\\r"
                                                        + "\\n"
                                                        + "}\",\"variables\":{}}")
                                        .header(HEADER_NAME, "Bearer " + token))
                        .andDo(print())
                        .andReturn();

        var checkBodyJson = mapper.readTree(checkRequest.getResponse().getContentAsString());
        System.out.println(checkBodyJson.toPrettyString());
        boolean isThere = false;
        var idArray = checkBodyJson.get("data").get("machine");
        if (idArray.isArray()) {
            var iterator = idArray.elements();
            while (iterator.hasNext()) {
                var machine = iterator.next();
                if (machine.get("id").textValue().equals(id)) {
                    isThere = true;
                }
            }
            assertTrue(isThere);
        }

        System.out.println(id);
        System.out.println(bodyJson.toPrettyString());
    }

    @Test
    @Order(1)
    public void createPropertyForMachine() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r"
                        + "\\n"
                        + " createPropertyForMachine(property: {\\r"
                        + "\\n"
                        + "        sourceId : \\\"Maximum_Temperature\\\",\\r"
                        + "\\n"
                        + "        label: \\\"Maximum Temperature\\\",\\r"
                        + "\\n"
                        + "         labelLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "        description: \\\"This is a test property\\\",\\r"
                        + "\\n"
                        + "        descriptionLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "        value : \\\"1000\\\",\\r"
                        + "\\n"
                        + "        semanticReferences: [{\\r"
                        + "\\n"
                        + "            sourceUri: \\\"sem_ref_maximum_temperature\\\",\\r"
                        + "\\n"
                        + "            label: \\\"Maximum Temperature\\\",\\r"
                        + "\\n"
                        + "            labelLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "            description: \\\"This is a test semantic reference\\\",\\r"
                        + "\\n"
                        + "            descriptionLanguageCode: \\\"en\\\"\\r"
                        + "\\n"
                        + "            }]\\r"
                        + "\\n"
                        + "}, machineId: \\\""
                        + MACHINE_ID
                        + "\\\"\\r"
                        + "\\n"
                        + ") {code\\r"
                        + "\\n"
                        + "\\tmessage property {id sourceId label description value}\\r"
                        + "\\n"
                        + "}\\r"
                        + "\\n"
                        + "}\",\"variables\":{}}";
        var createRequest =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestText)
                                        .header(HEADER_NAME, "Bearer " + token))
                        .andReturn();
        var createResponse = mapper.readTree(createRequest.getResponse().getContentAsString());
        assertEquals(
                createResponse.get("data").get("createPropertyForMachine").get("code").asInt(),
                200);
        assertEquals(
                createResponse
                        .get("data")
                        .get("createPropertyForMachine")
                        .get("message")
                        .textValue(),
                "Success");
        var propertyId =
                createResponse
                        .get("data")
                        .get("createPropertyForMachine")
                        .get("property")
                        .get("id")
                        .textValue();
        var label =
                createResponse
                        .get("data")
                        .get("createPropertyForMachine")
                        .get("property")
                        .get("label")
                        .textValue();
        var description =
                createResponse
                        .get("data")
                        .get("createPropertyForMachine")
                        .get("property")
                        .get("description")
                        .textValue();
        var sourceId =
                createResponse
                        .get("data")
                        .get("createPropertyForMachine")
                        .get("property")
                        .get("sourceId")
                        .textValue();
        var value =
                createResponse
                        .get("data")
                        .get("createPropertyForMachine")
                        .get("property")
                        .get("value")
                        .textValue();

        assertEquals("Maximum Temperature", label);
        assertEquals("This is a test property", description);
        assertEquals("Maximum_Temperature", sourceId);
        assertEquals("1000", value);

        var controlResponseData = this.getPropertyDataForMachine(MACHINE_ID);
        var machineArray = (ArrayNode) controlResponseData.get("machine");
        assertFalse(machineArray.isEmpty());
        var propertiesArray = (ArrayNode) machineArray.get(0).get("machineProperties");
        assertFalse(propertiesArray.isEmpty());
        boolean isExisting = false;
        for (com.fasterxml.jackson.databind.JsonNode property : propertiesArray) {
            if (property.get("id").textValue().equals(propertyId)) {
                isExisting = true;
            }
        }
        assertTrue(isExisting);
        deleteProperty(propertyId);
        isExisting = false;
        var secondControlResponseData = this.getPropertyDataForMachine(MACHINE_ID);
        var secondMachineArray = (ArrayNode) secondControlResponseData.get("machine");
        var secondPropertiesArray = (ArrayNode) secondMachineArray.get(0).get("machineProperties");
        for (com.fasterxml.jackson.databind.JsonNode property : secondPropertiesArray) {
            if (property.get("id").textValue().equals(propertyId)) {
                isExisting = true;
            }
        }
        assertFalse(isExisting);
    }

    public JsonNode getPropertyDataForMachine(String machineId) throws Exception {
        String controlRequestText =
                "{\"query\":\"query {\\r\\n  machine("
                        + "filter: {id: \\\""
                        + machineId
                        + "\\\"}){\\r"
                        + "\\n"
                        + "    id \\r"
                        + "\\n"
                        + "    machineProperties{\\r"
                        + "\\n"
                        + "        id\\r"
                        + "\\n"
                        + "        label\\r"
                        + "\\n"
                        + "    }\\r"
                        + "\\n"
                        + "  }\\r"
                        + "\\n"
                        + "}\",\"variables\":{}}";

        var controlRequest =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(controlRequestText)
                                        .header(HEADER_NAME, "Bearer " + token))
                        .andReturn();

        return mapper.readTree(controlRequest.getResponse().getContentAsString()).get("data");
    }

    public void deleteProperty(String id) throws Exception {
        String deleteRequestText =
                "{\"query\":\"mutation {\\r\\n  deleteProperty(propertyId:\\r\\n  "
                        + "\\\""
                        + id
                        + "\\\"\\r"
                        + "\\n"
                        + "  ) {\\r"
                        + "\\n"
                        + "      code\\r"
                        + "\\n"
                        + "      message\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "}\",\"variables\":{}}";

        var deleteRequest =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(deleteRequestText)
                                        .header(HEADER_NAME, "Bearer " + token))
                        .andReturn();
    }

    @Test
    @Order(1)
    public void createDummyCapability() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r"
                        + "\\n"
                        + "  createCapability(capability: \\r"
                        + "\\n"
                        + "    {\\r"
                        + "\\n"
                        + "        sourceId : \\\"Heating\\\",\\r"
                        + "\\n"
                        + "        label: \\\"Heating\\\",\\r"
                        + "\\n"
                        + "        labelLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "        description: \\\"This is a test capability\\\",\\r"
                        + "\\n"
                        + "\\t    descriptionLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "        semanticReferences: {\\r"
                        + "\\n"
                        + "            sourceUri: \\\"sem_ref_working_conditions\\\",\\r"
                        + "\\n"
                        + "            label: \\\"Working Conditions\\\",\\r"
                        + "\\n"
                        + "            labelLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "            description: \\\"This is a test semantic reference\\\",\\r"
                        + "\\n"
                        + "            descriptionLanguageCode: \\\"en\\\"\\r"
                        + "\\n"
                        + "        }\\r"
                        + "\\n"
                        + "    }) {code\\r"
                        + "\\n"
                        + "  message capability {id sourceId label description}}\\r"
                        + "\\n"
                        + "}\",\"variables\":{}}";

        var request =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestText)
                                        .header(HEADER_NAME, "Bearer " + token))
                        .andReturn();

        var body = request.getResponse().getContentAsString();
        var bodyJson = mapper.readTree(body);
        dummyCapabilityId =
                bodyJson.get("data")
                        .get("createCapability")
                        .get("capability")
                        .get("id")
                        .textValue();
        System.out.println(dummyCapabilityId);
        assertThat(dummyCapabilityId).contains("https://www.smartfactoryweb.de/graph/SfwAms");
    }

    @Test
    @Order(2)
    public void addCapabilityToMachine() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r\\n  addCapabilityToMachine(\\r\\n      "
                        + "capabilityId : \\\""
                        + dummyCapabilityId
                        + "\\\",\\r\\n      "
                        + "machineId: \\\""
                        + MACHINE_ID
                        + "\\\"\\r"
                        + "\\n"
                        + "  ) {code\\r"
                        + "\\n"
                        + "  message machine { providedCapabilities { id } }}\\r"
                        + "\\n"
                        + "}\",\"variables\":{}}";

        var request =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestText)
                                        .header(HEADER_NAME, "Bearer " + token))
                        .andReturn();

        var body = request.getResponse().getContentAsString();
        var bodyJson = mapper.readTree(body);
        var machine = bodyJson.get("data").get("addCapabilityToMachine").get("machine");
        var capabilities = (ArrayNode) machine.get("providedCapabilities");
        boolean isThere = false;
        for (var capability : capabilities) {
            if (capability.get("id").textValue().equals(dummyCapabilityId)) {
                isThere = true;
            }
        }
        assertTrue(isThere);
    }

    @Test
    @Order(3)
    public void removeCapabilityFromMachine() throws Exception {
        String requestText =
                "{\"query\":\"mutation "
                        + "{\\r\\n removeCapabilityFromMachine(\\r\\n      "
                        + "capabilityId : \\\""
                        + dummyCapabilityId
                        + "\\\",\\r\\n      "
                        + "machineId: \\\""
                        + MACHINE_ID
                        + "\\\"\\r"
                        + "\\n"
                        + ") {code\\r"
                        + "\\n"
                        + "\\tmessage machine { providedCapabilities { id } }\\r"
                        + "\\n"
                        + "}\\r"
                        + "\\n"
                        + "}\",\"variables\":{}}";

        var request =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestText)
                                        .header(HEADER_NAME, "Bearer " + token))
                        .andReturn();

        var body = request.getResponse().getContentAsString();
        var bodyJson = mapper.readTree(body);
        var machine = bodyJson.get("data").get("removeCapabilityFromMachine").get("machine");
        var providedCapabilities = (ArrayNode) machine.get("providedCapabilities");
        boolean isThere = false;
        for (var capability : providedCapabilities) {
            if (capability.get("id").textValue().equals(dummyCapabilityId)) {
                isThere = true;
            }
        }
        assertFalse(isThere);
    }

    @Test
    @Order(4)
    public void deleteDummyCapability() throws Exception {
        var request =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"query\":\"mutation {\\r"
                                                        + "\\n"
                                                        + "  deleteCapability(capabilityId: \\r"
                                                        + "\\n"
                                                        + "    \\\""
                                                        + dummyCapabilityId
                                                        + "\\\", deleteChildren: true) {code\\r"
                                                        + "\\n"
                                                        + "  message}\\r"
                                                        + "\\n"
                                                        + "}\",\"variables\":{}}")
                                        .header(HEADER_NAME, "Bearer " + token))
                        .andReturn();
        var body = request.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        var bodyJson = objectMapper.readTree(body);
        var message = bodyJson.get("data").get("deleteCapability").get("message");
        assertThat(message.textValue()).isEqualTo("Deleted " + dummyCapabilityId);
    }

    @Test
    @Order(1)
    public void createDummyProcess() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r"
                        + "\\n"
                        + " createProcess(process: {\\r"
                        + "\\n"
                        + "    sourceId: \\\"Assembling Process AP002\\\",\\r"
                        + "\\n"
                        + "    description: \\\"This is a test process\\\",\\r"
                        + "\\n"
                        + "    descriptionLanguageCode: \\\"en\\\"\\r"
                        + "\\n"
                        + "}) {\\r"
                        + "\\n"
                        + "  process {\\r"
                        + "\\n"
                        + "    id\\r"
                        + "\\n"
                        + "  }\\r"
                        + "\\n"
                        + "}\\r"
                        + "\\n"
                        + "}\\r"
                        + "\\n"
                        + "\",\"variables\":{}}";

        var request =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestText)
                                        .header(HEADER_NAME, "Bearer " + token))
                        .andReturn();

        var body = request.getResponse().getContentAsString();
        var bodyJson = mapper.readTree(body);
        dummyProcessId =
                bodyJson.get("data").get("createProcess").get("process").get("id").textValue();
        System.out.println(dummyProcessId);
        assertThat(dummyProcessId).contains("https://www.smartfactoryweb.de/graph/SfwAms");
    }

    @Test
    @Order(2)
    public void addProcessToMachine() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r\\n  addProcessToMachine(\\r\\n      "
                        + "processId : \\\""
                        + dummyProcessId
                        + "\\\",\\r\\n      "
                        + "machineId: \\\""
                        + MACHINE_ID
                        + "\\\"\\r"
                        + "\\n"
                        + "  ) {code\\r"
                        + "\\n"
                        + "  message machine { providedProcesses {id} }}\\r"
                        + "\\n"
                        + "}\",\"variables\":{}}";

        var request =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestText)
                                        .header(HEADER_NAME, "Bearer " + token))
                        .andReturn();

        var body = request.getResponse().getContentAsString();
        var bodyJson = mapper.readTree(body);
        var machine = bodyJson.get("data").get("addProcessToMachine").get("machine");
        var processes = (ArrayNode) machine.get("providedProcesses");
        boolean isThere = false;
        for (var capability : processes) {
            if (capability.get("id").textValue().equals(dummyProcessId)) {
                isThere = true;
            }
        }
        assertTrue(isThere);
    }

    @Test
    @Order(3)
    public void removeProcessFromMachine() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r\\n removeProcessFromMachine(\\r\\n      "
                        + "processId : \\\""
                        + dummyProcessId
                        + "\\\",\\r\\n      "
                        + "machineId: \\\""
                        + MACHINE_ID
                        + "\\\"\\r"
                        + "\\n"
                        + ") {code\\r"
                        + "\\n"
                        + "\\tmessage machine { providedProcesses { id }}\\r"
                        + "\\n"
                        + "}\\r"
                        + "\\n"
                        + "}\",\"variables\":{}}";

        var request =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestText)
                                        .header(HEADER_NAME, "Bearer " + token))
                        .andReturn();

        var body = request.getResponse().getContentAsString();
        var bodyJson = mapper.readTree(body);
        var machine = bodyJson.get("data").get("removeProcessFromMachine").get("machine");
        var usingProcesses = (ArrayNode) machine.get("providedProcesses");
        boolean isThere = false;
        for (var process : usingProcesses) {
            if (process.get("id").textValue().equals(dummyProcessId)) {
                isThere = true;
            }
        }
        assertFalse(isThere);
    }

    @Test
    @Order(4)
    public void deleteDummyProcess() throws Exception {
        var request =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"query\":\"mutation {\\r"
                                                        + "\\n"
                                                        + "    deleteProcess(\\r"
                                                        + "\\n"
                                                        + "    processId: \\\""
                                                        + dummyProcessId
                                                        + "\\\", deleteChildren: true\\r"
                                                        + "\\n"
                                                        + "    ) {\\r"
                                                        + "\\n"
                                                        + "    message\\r"
                                                        + "\\n"
                                                        + "  }\\r"
                                                        + "\\n"
                                                        + "}\",\"variables\":{}}")
                                        .header(HEADER_NAME, "Bearer " + token))
                        .andReturn();
        var body = request.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        var bodyJson = objectMapper.readTree(body);
        var message = bodyJson.get("data").get("deleteProcess").get("message");
        assertThat(message.textValue()).isEqualTo("Deleted " + dummyProcessId);
    }

    @Test
    @Order(1)
    public void createMachineBulk() throws Exception {
        String requestText =
                "{\"query\":\"mutation { createMachine(machine: { sourceId : \\\"DMP 70\\\", label:"
                    + " \\\"DMP 70\\\", labelLanguageCode: \\\"en\\\", description: \\\"CNC milling"
                    + " machine\\\", descriptionLanguageCode: \\\"en\\\", providedCapabilities: [ {"
                    + " sourceId : \\\"Milling\\\", label: \\\"Milling\\\", labelLanguageCode:"
                    + " \\\"en\\\", description: \\\"Milling\\\", descriptionLanguageCode:"
                    + " \\\"en\\\", properties: [ { sourceId: \\\"Material\\\", label:"
                    + " \\\"Material\\\", labelLanguageCode: \\\"en\\\", description:"
                    + " \\\"Material\\\", descriptionLanguageCode: \\\"en\\\", value:"
                    + " \\\"Aluminium, Stahl, Titan, Plastik, CFK\\\" } ] } ], machineProperties: ["
                    + " { sourceId: \\\"Maximum workpiece diameter\\\", label: \\\"Maximum"
                    + " workpiece diameter\\\", labelLanguageCode: \\\"en\\\", description:"
                    + " \\\"Maximum workpiece diameters\\\", descriptionLanguageCode: \\\"en\\\","
                    + " value: \\\"290 mm\\\" }, { sourceId: \\\"Maximum workpiece length\\\","
                    + " label: \\\"Maximum workpiece length\\\", labelLanguageCode: \\\"en\\\","
                    + " description: \\\"Maximum workpiece length\\\", descriptionLanguageCode:"
                    + " \\\"en\\\", value: \\\"840 mm\\\" } ] })  { code machine {id sourceId label"
                    + " description} } }\",\"variables\":{}}";

        var request =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestText)
                                        .header(HEADER_NAME, "Bearer " + token))
                        .andReturn();

        var body = request.getResponse().getContentAsString();
        var bodyJson = mapper.readTree(body);
        var id = bodyJson.get("data").get("createMachine").get("machine").get("id").textValue();
        bulkImportMachineId = id;
        var sourceId =
                bodyJson.get("data")
                        .get("createMachine")
                        .get("machine")
                        .get("sourceId")
                        .textValue();
        var label =
                bodyJson.get("data").get("createMachine").get("machine").get("label").textValue();
        var description =
                bodyJson.get("data")
                        .get("createMachine")
                        .get("machine")
                        .get("description")
                        .textValue();

        assertEquals(sourceId, "DMP 70");
        assertEquals(label, "DMP 70");
        assertEquals(description, "CNC milling machine");

        String checkText =
                "{\"query\":\"query { machine { id sourceId label labelLanguageCode description"
                    + " descriptionLanguageCode  providedCapabilities { id sourceId label"
                    + " labelLanguageCode description descriptionLanguageCode properties{ id"
                    + " sourceId label labelLanguageCode description descriptionLanguageCode value"
                    + " semanticReferences { id sourceUri label labelLanguageCode description"
                    + " descriptionLanguageCode } } } machineProperties { id sourceId label"
                    + " labelLanguageCode description descriptionLanguageCode value"
                    + " semanticReferences { id sourceUri label labelLanguageCode description"
                    + " descriptionLanguageCode } } } }\",\"variables\":{}}";
        var checkRequest =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(checkText)
                                        .header(HEADER_NAME, "Bearer " + token))
                        .andDo(print())
                        .andReturn();

        var checkBodyJson = mapper.readTree(checkRequest.getResponse().getContentAsString());
        System.out.println(checkBodyJson.toPrettyString());
        boolean isThere = false;
        var idArray = checkBodyJson.get("data").get("machine");
        if (idArray.isArray()) {
            var iterator = idArray.elements();
            while (iterator.hasNext()) {
                var machine = iterator.next();
                if (machine.get("id").textValue().equals(id)) {
                    isThere = true;
                    var providedCapability = machine.get("providedCapabilities").get(0);
                    var machineProperty = machine.get("machineProperties").get(1);
                    if (!machineProperty
                            .get("label")
                            .textValue()
                            .equals("Maximum workpiece diameter")) {
                        machineProperty = machine.get("machineProperties").get(0);
                    }
                    bulkImportMachineCapabilityId = providedCapability.get("id").textValue();
                    bulkImportMachinePropertyId = machineProperty.get("id").textValue();
                    var capabilityProperty = providedCapability.get("properties").get(0);
                    bulkImportMachineCapabilityPropertyId =
                            capabilityProperty.get("id").textValue();
                    assertEquals("Milling", providedCapability.get("label").textValue());
                    assertEquals("Milling", providedCapability.get("description").textValue());
                    assertEquals("Milling", providedCapability.get("sourceId").textValue());
                    assertEquals("Material", capabilityProperty.get("label").textValue());
                    assertEquals("Material", capabilityProperty.get("description").textValue());
                    assertEquals("Material", capabilityProperty.get("sourceId").textValue());
                    assertEquals(
                            "Maximum workpiece diameter", machineProperty.get("label").textValue());
                    assertEquals(
                            "Maximum workpiece diameter",
                            machineProperty.get("sourceId").textValue());
                    assertEquals(
                            "Maximum workpiece diameters",
                            machineProperty.get("description").textValue());
                }
            }
            assertTrue(isThere);
        }
    }

    @Test
    @Order(2)
    public void updateMachineBulk() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r\\n  updateMachine(machineId:\\\""
                        + bulkImportMachineId
                        + "\\\", machine: \\r"
                        + "\\n"
                        + "    { sourceId : \\\"DMP 70\\\", label: \\\"DMP 70\\\","
                        + " labelLanguageCode: \\\"en\\\", description: \\\"CNC milling"
                        + " machine\\\", descriptionLanguageCode: \\\"en\\\", providedCapabilities:"
                        + " [ {id: \\\""
                        + bulkImportMachineCapabilityId
                        + "\\\",sourceId : \\\"Milling_update\\\", label:"
                        + " \\\"Milling_update_test\\\", labelLanguageCode: \\\"en\\\","
                        + " description: \\\"Milling updated\\\", descriptionLanguageCode:"
                        + " \\\"en\\\", properties: [ { id:\\\""
                        + bulkImportMachineCapabilityPropertyId
                        + "\\\", sourceId: \\\"Material updated\\\", label: \\\"Material"
                        + " updated\\\", labelLanguageCode: \\\"en\\\", description: \\\"Material"
                        + " updated\\\", descriptionLanguageCode: \\\"en\\\", value: \\\"Aluminium,"
                        + " Stahl, Titan, Plastik, CFK\\\" } ] } ], machineProperties: [ { id:\\\""
                        + bulkImportMachinePropertyId
                        + "\\\", sourceId: \\\"Maximum workpiece diameter update test\\\", label:"
                        + " \\\"Maximum workpiece diameter updated works\\\", labelLanguageCode:"
                        + " \\\"de\\\", description: \\\"Maximum workpiece diameters update"
                        + " description\\\", descriptionLanguageCode: \\\"en\\\", value: \\\"390"
                        + " mm\\\" } ] }\\r"
                        + "\\n"
                        + "  ) {code message}\\r"
                        + "\\n"
                        + "}\",\"variables\":{}}";

        var request =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestText)
                                        .header(HEADER_NAME, "Bearer " + token))
                        .andReturn();

        var body = request.getResponse().getContentAsString();
        var bodyJson = mapper.readTree(body);
        var code = bodyJson.get("data").get("updateMachine").get("code");
        assertEquals(200, code.asInt());

        String checkText =
                "{\"query\":\"query { machine { id sourceId label labelLanguageCode description"
                    + " descriptionLanguageCode  providedCapabilities { id sourceId label"
                    + " labelLanguageCode description descriptionLanguageCode properties{ id"
                    + " sourceId label labelLanguageCode description descriptionLanguageCode value"
                    + " semanticReferences { id sourceUri label labelLanguageCode description"
                    + " descriptionLanguageCode } } } machineProperties { id sourceId label"
                    + " labelLanguageCode description descriptionLanguageCode value"
                    + " semanticReferences { id sourceUri label labelLanguageCode description"
                    + " descriptionLanguageCode } } } }\",\"variables\":{}}";
        var checkRequest =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(checkText)
                                        .header(HEADER_NAME, "Bearer " + token))
                        .andDo(print())
                        .andReturn();

        var checkBodyJson = mapper.readTree(checkRequest.getResponse().getContentAsString());
        System.out.println(checkBodyJson.toPrettyString());
        boolean isThere = false;
        var idArray = checkBodyJson.get("data").get("machine");

        if (idArray.isArray()) {
            var iterator = idArray.elements();
            while (iterator.hasNext()) {
                var machine = iterator.next();
                if (machine.get("id").textValue().equals(bulkImportMachineId)) {
                    isThere = true;
                    var providedCapability = machine.get("providedCapabilities").get(0);
                    var machineProperty = machine.get("machineProperties").get(1);
                    if (!machineProperty
                            .get("label")
                            .textValue()
                            .equals("Maximum workpiece diameter updated works")) {
                        machineProperty = machine.get("machineProperties").get(0);
                    }
                    var capabilityProperty = providedCapability.get("properties").get(0);
                    assertEquals(
                            "Milling_update_test", providedCapability.get("label").textValue());
                    assertEquals(
                            "Milling updated", providedCapability.get("description").textValue());
                    assertEquals("Milling_update", providedCapability.get("sourceId").textValue());
                    assertEquals("Material updated", capabilityProperty.get("label").textValue());
                    assertEquals(
                            "Material updated", capabilityProperty.get("description").textValue());
                    assertEquals(
                            "Material updated", capabilityProperty.get("sourceId").textValue());
                    assertEquals(
                            "Maximum workpiece diameter updated works",
                            machineProperty.get("label").textValue());
                    assertEquals(
                            "Maximum workpiece diameters update description",
                            machineProperty.get("description").textValue());
                    assertEquals(
                            "Maximum workpiece diameter update test",
                            machineProperty.get("sourceId").textValue());
                }
            }
            assertTrue(isThere);
        }
    }

    @Test
    @Order(3)
    public void deleteMachine() throws Exception {
        var request =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"query\":\"mutation {\\r"
                                                        + "\\n"
                                                        + "  deleteMachine(machineId: \\r"
                                                        + "\\n"
                                                        + "    \\\""
                                                        + bulkImportMachineId
                                                        + "\\\") {code\\r"
                                                        + "\\n"
                                                        + "  message}\\r"
                                                        + "\\n"
                                                        + "}\",\"variables\":{}}")
                                        .header(HEADER_NAME, "Bearer " + token))
                        .andReturn();
        var body = request.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        var bodyJson = objectMapper.readTree(body);
        var code = bodyJson.get("data").get("deleteMachine").get("code");
        assertThat(code.asInt()).isEqualTo(200);
        System.out.println(code.textValue());
    }
}
