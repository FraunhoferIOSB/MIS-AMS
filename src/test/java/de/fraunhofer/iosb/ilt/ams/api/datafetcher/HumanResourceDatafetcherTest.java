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
class HumanResourceDatafetcherTest {
    private static final String HEADER_NAME = "Authorization";

    @Value("${test.token}")
    String token;

    @Value("${test.graph}")
    String graph;

    private static String HUMAN_RESOURCE_ID = "";
    private static String dummyCapabilityId = "";

    private static String dummyProcessId = "";

    @Autowired private MockMvc mockMvc;

    private ObjectMapper mapper;

    @BeforeEach
    public void setUp() {
        mapper = new ObjectMapper();
    }

    @Test
    @Order(1)
    public void humanResource() throws Exception {
        String requestText =
                "{\"query\":\"query {\\r"
                        + "\\n"
                        + "  humanResource {\\r"
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
    public void createHumanResource() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r"
                        + "\\n"
                        + "  createHumanResource(humanResource: \\r"
                        + "\\n"
                        + "    {\\r"
                        + "\\n"
                        + "      sourceId : \\\"HumanResource HR001\\\",\\r"
                        + "\\n"
                        + "      label: \\\"Drilling\\\",\\r"
                        + "\\n"
                        + "      labelLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "      description: \\\"This is a test human resource\\\",\\r"
                        + "\\n"
                        + "      descriptionLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "      certificates: [\\r"
                        + "\\n"
                        + "            {\\r"
                        + "\\n"
                        + "                sourceId : \\\"Drilling Certification\\\",\\r"
                        + "\\n"
                        + "                label: \\\"Drilling Certification\\\",\\r"
                        + "\\n"
                        + "                labelLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "                description: \\\"This is a test certification\\\",\\r"
                        + "\\n"
                        + "                descriptionLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "                value: \\\"passed\\\",\\r"
                        + "\\n"
                        + "                semanticReferences: [\\r"
                        + "\\n"
                        + "                    {\\r"
                        + "\\n"
                        + "                        sourceUri:"
                        + " \\\"sem_ref_drilling_certification\\\",\\r"
                        + "\\n"
                        + "                        label: \\\"Drilling Certification\\\",\\r"
                        + "\\n"
                        + "                        labelLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "                        description: \\\"This is a test semantic"
                        + " reference\\\",\\r"
                        + "\\n"
                        + "                        descriptionLanguageCode: \\\"en\\\"\\r"
                        + "\\n"
                        + "                    }\\r"
                        + "\\n"
                        + "                ]\\r"
                        + "\\n"
                        + "            }\\r"
                        + "\\n"
                        + "        ]\\r"
                        + "\\n"
                        + "    }) {code\\r"
                        + "\\n"
                        + "  message humanResource{id sourceId label description certificates{ id"
                        + " sourceId label description}}}\\r"
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
        var id =
                bodyJson.get("data")
                        .get("createHumanResource")
                        .get("humanResource")
                        .get("id")
                        .textValue();
        HUMAN_RESOURCE_ID = id;
        var sourceId =
                bodyJson.get("data")
                        .get("createHumanResource")
                        .get("humanResource")
                        .get("sourceId")
                        .textValue();
        var description =
                bodyJson.get("data")
                        .get("createHumanResource")
                        .get("humanResource")
                        .get("description")
                        .textValue();
        var label =
                bodyJson.get("data")
                        .get("createHumanResource")
                        .get("humanResource")
                        .get("label")
                        .textValue();

        var certificates =
                bodyJson.get("data")
                        .get("createHumanResource")
                        .get("humanResource")
                        .get("certificates");

        var addedCertificate = certificates.get(0);

        var sourceIdCertificate = addedCertificate.get("sourceId").textValue();
        var labelCertificate = addedCertificate.get("label").textValue();
        var descriptionCertificate = addedCertificate.get("description").textValue();

        assertEquals(sourceId, "HumanResource HR001");
        assertEquals(label, "Drilling");
        assertEquals(description, "This is a test human resource");

        assertEquals("Drilling Certification", sourceIdCertificate);
        assertEquals("Drilling Certification", labelCertificate);
        assertEquals("This is a test certification", descriptionCertificate);

        var checkRequest =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"query\":\"query {\\r"
                                                        + "\\n"
                                                        + "  humanResource {\\r"
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
        var idArray = checkBodyJson.get("data").get("humanResource");
        if (idArray.isArray()) {
            var iterator = idArray.elements();
            while (iterator.hasNext()) {
                var humanResource = iterator.next();
                if (humanResource.get("id").textValue().equals(id)) {
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
    }

    @Test
    @Order(2)
    public void addCapabilityToHumanResource() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r\\n  addCapabilityToHumanResource(\\r\\n      "
                        + "capabilityId : \\\""
                        + dummyCapabilityId
                        + "\\\",\\r\\n      "
                        + "humanResourceId: \\\""
                        + HUMAN_RESOURCE_ID
                        + "\\\"\\r"
                        + "\\n"
                        + "  ) {code\\r"
                        + "\\n"
                        + "  message humanResource { providedCapabilities { id } }}\\r"
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
        var process = bodyJson.get("data").get("addCapabilityToHumanResource").get("humanResource");
        var capabilities = (ArrayNode) process.get("providedCapabilities");
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
    public void removeCapabilityFromHumanResource() throws Exception {
        String requestText =
                "{\"query\":\"mutation "
                        + "{\\r\\n removeCapabilityFromHumanResource(\\r\\n      "
                        + "capabilityId : \\\""
                        + dummyCapabilityId
                        + "\\\",\\r\\n      "
                        + "humanResourceId: \\\""
                        + HUMAN_RESOURCE_ID
                        + "\\\"\\r"
                        + "\\n"
                        + ") {code\\r"
                        + "\\n"
                        + "\\tmessage humanResource { providedCapabilities { id } }\\r"
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
        var process =
                bodyJson.get("data").get("removeCapabilityFromHumanResource").get("humanResource");
        var providedCapabilities = (ArrayNode) process.get("providedCapabilities");
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
    }

    @Test
    @Order(2)
    public void addProcessToHumanResource() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r\\n  addProcessToHumanResource(\\r\\n      "
                        + "processId : \\\""
                        + dummyProcessId
                        + "\\\",\\r\\n      "
                        + "humanResourceId: \\\""
                        + HUMAN_RESOURCE_ID
                        + "\\\"\\r"
                        + "\\n"
                        + "  ) {code\\r"
                        + "\\n"
                        + "  message humanResource { usingProcesses {id} }}\\r"
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
        var process = bodyJson.get("data").get("addProcessToHumanResource").get("humanResource");
        var processes = (ArrayNode) process.get("usingProcesses");
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
    public void removeProcessFromHumanResource() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r\\n removeProcessFromHumanResource(\\r\\n      "
                        + "processId : \\\""
                        + dummyProcessId
                        + "\\\",\\r\\n      "
                        + "humanResourceId: \\\""
                        + HUMAN_RESOURCE_ID
                        + "\\\"\\r"
                        + "\\n"
                        + ") {code\\r"
                        + "\\n"
                        + "\\tmessage humanResource { usingProcesses { id }}\\r"
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
        var humanResource =
                bodyJson.get("data").get("removeProcessFromHumanResource").get("humanResource");
        var usingProcesses = (ArrayNode) humanResource.get("usingProcesses");
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
    @Order(Integer.MAX_VALUE)
    public void tearDown() throws Exception {
        var request =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"query\":\"mutation {\\r"
                                                    + "\\n"
                                                    + "  deleteHumanResource(humanResourceId: \\r"
                                                    + "\\n"
                                                    + "    \\\""
                                                        + HUMAN_RESOURCE_ID
                                                        + "\\\") {code\\r"
                                                        + "\\n"
                                                        + "  message}\\r"
                                                        + "\\n"
                                                        + "}\",\"variables\":{}}")
                                        .header(HEADER_NAME, "Bearer " + token))
                        .andReturn();
        var body = request.getResponse().getContentAsString();
        var bodyJson = mapper.readTree(body);
        var message = bodyJson.get("data").get("deleteHumanResource").get("message");
        assertThat(message.textValue()).isEqualTo("Success");
    }
}
