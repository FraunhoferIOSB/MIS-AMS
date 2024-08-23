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
class ProcessDatafetcherTest {
    private static final String HEADER_NAME = "Authorization";

    @Value("${test.token}")
    String token;

    @Value("${test.graph}")
    String graph;

    private static String PROCESS_ID = "";
    private static String dummyCapabilityId = "";
    private static String dummyProductId = "";

    @Autowired private MockMvc mockMvc;

    private ObjectMapper mapper;

    @Test
    @Order(0)
    void createRootProcess() throws Exception {
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
        var bodyJson = new ObjectMapper().readTree(body);
        PROCESS_ID = bodyJson.get("data").get("createProcess").get("process").get("id").textValue();
    }

    @BeforeEach
    public void setUp() {
        mapper = new ObjectMapper();
    }

    @Test
    @Order(Integer.MAX_VALUE)
    void removeRootProcess() throws Exception {
        var request =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"query\":\"mutation {\\r"
                                                        + "\\n"
                                                        + "  deleteProcess(processId: \\r"
                                                        + "\\n"
                                                        + "    \\\""
                                                        + PROCESS_ID
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
        var message = bodyJson.get("data").get("deleteProcess").get("message");
        assertThat(message.textValue()).isEqualTo("Deleted " + PROCESS_ID);
        System.out.println(message.textValue());
    }

    @Test
    public void process() throws Exception {
        String requestText =
                "{\"query\":\"query {\\r"
                        + "\\n"
                        + "  process {\\r"
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
    public void addRealizedCapabilityToProcess() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r\\n "
                        + " addRealizedCapabilityToProcess(capabilityId: \\\""
                        + dummyCapabilityId
                        + "\\\", "
                        + "processId: \\\""
                        + PROCESS_ID
                        + "\\\") {\\r"
                        + "\\n"
                        + "    process {\\r"
                        + "\\n"
                        + "      realizedCapabilities {\\r"
                        + "\\n"
                        + "        id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "    }\\r"
                        + "\\n"
                        + "  }\\r"
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
        var process = bodyJson.get("data").get("addRealizedCapabilityToProcess").get("process");
        var realizedCapabilities = (ArrayNode) process.get("realizedCapabilities");
        boolean isThere = false;
        for (var capability : realizedCapabilities) {
            if (capability.get("id").textValue().equals(dummyCapabilityId)) {
                isThere = true;
            }
        }
        assertTrue(isThere);
    }

    @Test
    @Order(3)
    public void removeRealizedCapabilityFromProcess() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r\\n "
                        + " removeRealizedCapabilityFromProcess(capabilityId: \\\""
                        + dummyCapabilityId
                        + "\\\", "
                        + "processId: \\\""
                        + PROCESS_ID
                        + "\\\") {\\r"
                        + "\\n"
                        + "    process {\\r"
                        + "\\n"
                        + "      realizedCapabilities {\\r"
                        + "\\n"
                        + "        id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "    }\\r"
                        + "\\n"
                        + "  }\\r"
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
                bodyJson.get("data").get("removeRealizedCapabilityFromProcess").get("process");
        var realizedCapabilities = (ArrayNode) process.get("realizedCapabilities");
        boolean isThere = false;
        for (var capability : realizedCapabilities) {
            if (capability.get("id").textValue().equals(dummyCapabilityId)) {
                isThere = true;
            }
        }
        assertFalse(isThere);
    }

    @Test
    @Order(2)
    public void addRequiredCapabilityToProcess() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r\\n "
                        + " addRequiredCapabilityToProcess(capabilityId: \\\""
                        + dummyCapabilityId
                        + "\\\", "
                        + "processId: \\\""
                        + PROCESS_ID
                        + "\\\") {\\r"
                        + "\\n"
                        + "    process {\\r"
                        + "\\n"
                        + "      requiredCapabilities {\\r"
                        + "\\n"
                        + "        id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "    }\\r"
                        + "\\n"
                        + "  }\\r"
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
        var process = bodyJson.get("data").get("addRequiredCapabilityToProcess").get("process");
        var requiredCapabilities = (ArrayNode) process.get("requiredCapabilities");
        boolean isThere = false;
        for (var capability : requiredCapabilities) {
            if (capability.get("id").textValue().equals(dummyCapabilityId)) {
                isThere = true;
            }
        }
        assertTrue(isThere);
    }

    @Test
    @Order(3)
    public void removeRequiredCapabilityFromProcess() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r\\n "
                        + " removeRequiredCapabilityFromProcess(capabilityId: \\\""
                        + dummyCapabilityId
                        + "\\\", "
                        + "processId: \\\""
                        + PROCESS_ID
                        + "\\\") {\\r"
                        + "\\n"
                        + "    process {\\r"
                        + "\\n"
                        + "      requiredCapabilities {\\r"
                        + "\\n"
                        + "        id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "    }\\r"
                        + "\\n"
                        + "  }\\r"
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
                bodyJson.get("data").get("removeRequiredCapabilityFromProcess").get("process");
        var requiredCapabilities = (ArrayNode) process.get("requiredCapabilities");
        boolean isThere = false;
        for (var capability : requiredCapabilities) {
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
    public void createDummyProduct() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r"
                        + "\\n"
                        + "  createProduct(product: \\r"
                        + "\\n"
                        + "    {\\r"
                        + "\\n"
                        + "        sourceId : \\\"Battery\\\",\\r"
                        + "\\n"
                        + "        label: \\\"Battery\\\",\\r"
                        + "\\n"
                        + "        labelLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "        description: \\\"This is a test product\\\",\\r"
                        + "\\n"
                        + "        descriptionLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "        productPassport: {\\r"
                        + "\\n"
                        + "            identifier : \\\"BatteryPassportA0001\\\",\\r"
                        + "\\n"
                        + "        properties: [\\r"
                        + "\\n"
                        + "            {\\r"
                        + "\\n"
                        + "                sourceId: \\\"Conflict_Minerals\\\",\\r"
                        + "\\n"
                        + "                label: \\\"Conflict Minerals\\\",\\r"
                        + "\\n"
                        + "                labelLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "                description: \\\"This is a test property\\\",\\r"
                        + "\\n"
                        + "                descriptionLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "                value: \\\"No\\\"\\r"
                        + "\\n"
                        + "            }\\r"
                        + "\\n"
                        + "        ]\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "    }) {code message product {id sourceId label labelLanguageCode"
                        + " description descriptionLanguageCode productPassport{identifier}}}\\r"
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
        dummyProductId =
                bodyJson.get("data").get("createProduct").get("product").get("id").textValue();
        System.out.println(dummyProductId);
    }

    @Test
    @Order(2)
    public void addRawMaterialToProcess() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r\\n  addInputProductToProcess(productId: \\\""
                        + dummyProductId
                        + "\\\",type: RAW_MATERIAL, "
                        + "processId: \\\""
                        + PROCESS_ID
                        + "\\\") {\\r"
                        + "\\n"
                        + "    process {\\r"
                        + "\\n"
                        + "      id\\r"
                        + "\\n"
                        + "      rawMaterials {\\r"
                        + "\\n"
                        + "          id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "      operatingMaterials {\\r"
                        + "\\n"
                        + "          id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "      preliminaryProducts {\\r"
                        + "\\n"
                        + "          id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "      inputProducts {\\r"
                        + "\\n"
                        + "        id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "    }\\r"
                        + "\\n"
                        + "  }\\r"
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
        var process = bodyJson.get("data").get("addInputProductToProcess").get("process");
        var rawMaterials = (ArrayNode) process.get("rawMaterials");
        boolean isThere = false;
        for (var inputProduct : rawMaterials) {
            if (inputProduct.get("id").textValue().equals(dummyProductId)) {
                isThere = true;
            }
        }
        assertTrue(isThere);
    }

    @Test
    @Order(2)
    public void addOperatingMaterialToProcess() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r\\n  addInputProductToProcess(productId: \\\""
                        + dummyProductId
                        + "\\\",type: OPERATING_MATERIAL, "
                        + "processId: \\\""
                        + PROCESS_ID
                        + "\\\") {\\r"
                        + "\\n"
                        + "    process {\\r"
                        + "\\n"
                        + "      id\\r"
                        + "\\n"
                        + "      rawMaterials {\\r"
                        + "\\n"
                        + "          id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "      operatingMaterials {\\r"
                        + "\\n"
                        + "          id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "      preliminaryProducts {\\r"
                        + "\\n"
                        + "          id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "      inputProducts {\\r"
                        + "\\n"
                        + "        id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "    }\\r"
                        + "\\n"
                        + "  }\\r"
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
        var process = bodyJson.get("data").get("addInputProductToProcess").get("process");
        var operatingMaterials = (ArrayNode) process.get("operatingMaterials");
        boolean isThere = false;
        for (var inputProduct : operatingMaterials) {
            if (inputProduct.get("id").textValue().equals(dummyProductId)) {
                isThere = true;
            }
        }
        assertTrue(isThere);
    }

    @Test
    @Order(2)
    public void addPreliminaryProductToProcess() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r\\n  addInputProductToProcess(productId: \\\""
                        + dummyProductId
                        + "\\\",type: PRELIMINARY_PRODUCT, "
                        + "processId: \\\""
                        + PROCESS_ID
                        + "\\\") {\\r"
                        + "\\n"
                        + "    process {\\r"
                        + "\\n"
                        + "      id\\r"
                        + "\\n"
                        + "      rawMaterials {\\r"
                        + "\\n"
                        + "          id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "      operatingMaterials {\\r"
                        + "\\n"
                        + "          id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "      preliminaryProducts {\\r"
                        + "\\n"
                        + "          id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "      inputProducts {\\r"
                        + "\\n"
                        + "        id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "    }\\r"
                        + "\\n"
                        + "  }\\r"
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
        var process = bodyJson.get("data").get("addInputProductToProcess").get("process");
        var preliminaryProducts = (ArrayNode) process.get("preliminaryProducts");
        boolean isThere = false;
        for (var inputProduct : preliminaryProducts) {
            if (inputProduct.get("id").textValue().equals(dummyProductId)) {
                isThere = true;
            }
        }
        assertTrue(isThere);
    }

    @Test
    @Order(3)
    public void removeRawMaterialFromProcess() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r\\n    "
                        + "removeInputProductFromProcess(productId: \\\""
                        + dummyProductId
                        + "\\\",type: RAW_MATERIAL,"
                        + " processId: \\\""
                        + PROCESS_ID
                        + "\\\") {\\r"
                        + "\\n"
                        + "    process {\\r"
                        + "\\n"
                        + "      id\\r"
                        + "\\n"
                        + "      rawMaterials {\\r"
                        + "\\n"
                        + "          id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "      operatingMaterials {\\r"
                        + "\\n"
                        + "          id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "      preliminaryProducts {\\r"
                        + "\\n"
                        + "          id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "      inputProducts {\\r"
                        + "\\n"
                        + "        id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "    }\\r"
                        + "\\n"
                        + "    message\\r"
                        + "\\n"
                        + "  }\\r"
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
        var process = bodyJson.get("data").get("removeInputProductFromProcess").get("process");
        var rawMaterials = (ArrayNode) process.get("rawMaterials");
        boolean isThere = false;
        for (var product : rawMaterials) {
            if (product.get("id").textValue().equals(dummyProductId)) {
                isThere = true;
            }
        }
        assertFalse(isThere);
    }

    @Test
    @Order(3)
    public void removeOperatingMaterialFromProcess() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r\\n    "
                        + "removeInputProductFromProcess(productId: \\\""
                        + dummyProductId
                        + "\\\",type: OPERATING_MATERIAL,"
                        + " processId: \\\""
                        + PROCESS_ID
                        + "\\\") {\\r"
                        + "\\n"
                        + "    process {\\r"
                        + "\\n"
                        + "      id\\r"
                        + "\\n"
                        + "      rawMaterials {\\r"
                        + "\\n"
                        + "          id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "      operatingMaterials {\\r"
                        + "\\n"
                        + "          id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "      preliminaryProducts {\\r"
                        + "\\n"
                        + "          id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "      inputProducts {\\r"
                        + "\\n"
                        + "        id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "    }\\r"
                        + "\\n"
                        + "    message\\r"
                        + "\\n"
                        + "  }\\r"
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
        var process = bodyJson.get("data").get("removeInputProductFromProcess").get("process");
        var operatingMaterials = (ArrayNode) process.get("operatingMaterials");
        boolean isThere = false;
        for (var product : operatingMaterials) {
            if (product.get("id").textValue().equals(dummyProductId)) {
                isThere = true;
            }
        }
        assertFalse(isThere);
    }

    @Test
    @Order(3)
    public void removePreliminaryProductFromProcess() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r\\n    "
                        + "removeInputProductFromProcess(productId: \\\""
                        + dummyProductId
                        + "\\\",type: PRELIMINARY_PRODUCT,"
                        + " processId: \\\""
                        + PROCESS_ID
                        + "\\\") {\\r"
                        + "\\n"
                        + "    process {\\r"
                        + "\\n"
                        + "      id\\r"
                        + "\\n"
                        + "      rawMaterials {\\r"
                        + "\\n"
                        + "          id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "      operatingMaterials {\\r"
                        + "\\n"
                        + "          id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "      preliminaryProducts {\\r"
                        + "\\n"
                        + "          id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "      inputProducts {\\r"
                        + "\\n"
                        + "        id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "    }\\r"
                        + "\\n"
                        + "    message\\r"
                        + "\\n"
                        + "  }\\r"
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
        var process = bodyJson.get("data").get("removeInputProductFromProcess").get("process");
        var preliminaryProducts = (ArrayNode) process.get("preliminaryProducts");
        boolean isThere = false;
        for (var product : preliminaryProducts) {
            if (product.get("id").textValue().equals(dummyProductId)) {
                isThere = true;
            }
        }
        assertFalse(isThere);
    }

    @Test
    @Order(2)
    public void addEndProductToProcess() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r\\n  addOutputProductToProcess(productId: \\\""
                        + dummyProductId
                        + "\\\",type: END_PRODUCT, "
                        + "processId: \\\""
                        + PROCESS_ID
                        + "\\\") {\\r"
                        + "\\n"
                        + "    process {\\r"
                        + "\\n"
                        + "      id\\r"
                        + "\\n"
                        + "      endProducts {\\r"
                        + "\\n"
                        + "          id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "      byProducts {\\r"
                        + "\\n"
                        + "          id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "      wasteProducts {\\r"
                        + "\\n"
                        + "          id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "      outputProducts {\\r"
                        + "\\n"
                        + "        id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "    }\\r"
                        + "\\n"
                        + "  }\\r"
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
        var process = bodyJson.get("data").get("addOutputProductToProcess").get("process");
        var endProducts = (ArrayNode) process.get("endProducts");
        boolean isThere = false;
        for (var product : endProducts) {
            if (product.get("id").textValue().equals(dummyProductId)) {
                isThere = true;
            }
        }
        assertTrue(isThere);
    }

    @Test
    @Order(2)
    public void addByProductToProcess() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r\\n  addOutputProductToProcess(productId: \\\""
                        + dummyProductId
                        + "\\\",type: BY_PRODUCT, "
                        + "processId: \\\""
                        + PROCESS_ID
                        + "\\\") {\\r"
                        + "\\n"
                        + "    process {\\r"
                        + "\\n"
                        + "      id\\r"
                        + "\\n"
                        + "      endProducts {\\r"
                        + "\\n"
                        + "          id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "      byProducts {\\r"
                        + "\\n"
                        + "          id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "      wasteProducts {\\r"
                        + "\\n"
                        + "          id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "      outputProducts {\\r"
                        + "\\n"
                        + "        id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "    }\\r"
                        + "\\n"
                        + "  }\\r"
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
        var process = bodyJson.get("data").get("addOutputProductToProcess").get("process");
        var byProducts = (ArrayNode) process.get("byProducts");
        boolean isThere = false;
        for (var product : byProducts) {
            if (product.get("id").textValue().equals(dummyProductId)) {
                isThere = true;
            }
        }
        assertTrue(isThere);
    }

    @Test
    @Order(2)
    public void addWasteProductToProcess() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r\\n  addOutputProductToProcess(productId: \\\""
                        + dummyProductId
                        + "\\\",type: END_PRODUCT, "
                        + "processId: \\\""
                        + PROCESS_ID
                        + "\\\") {\\r"
                        + "\\n"
                        + "    process {\\r"
                        + "\\n"
                        + "      id\\r"
                        + "\\n"
                        + "      endProducts {\\r"
                        + "\\n"
                        + "          id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "      byProducts {\\r"
                        + "\\n"
                        + "          id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "      wasteProducts {\\r"
                        + "\\n"
                        + "          id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "      outputProducts {\\r"
                        + "\\n"
                        + "        id\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "    }\\r"
                        + "\\n"
                        + "  }\\r"
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
        var process = bodyJson.get("data").get("addOutputProductToProcess").get("process");
        var endProducts = (ArrayNode) process.get("endProducts");
        boolean isThere = false;
        for (var product : endProducts) {
            if (product.get("id").textValue().equals(dummyProductId)) {
                isThere = true;
            }
        }
        assertTrue(isThere);
    }

    @Test
    @Order(4)
    public void deleteDummyProduct() throws Exception {
        var request =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"query\":\"mutation {\\r"
                                                        + "\\n"
                                                        + "  deleteProduct(productId: \\r"
                                                        + "\\n"
                                                        + "    \\\""
                                                        + dummyProductId
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
        var message = bodyJson.get("data").get("deleteProduct").get("message");
        assertThat(message.textValue()).isEqualTo("Deleted " + dummyProductId);
    }
}
