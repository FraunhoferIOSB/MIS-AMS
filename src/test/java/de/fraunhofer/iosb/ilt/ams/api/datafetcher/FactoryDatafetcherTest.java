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
import de.fraunhofer.iosb.ilt.ams.dao.FactoryDAO;
import de.fraunhofer.iosb.ilt.ams.model.ObjectRdf4jRepository;
import de.fraunhofer.iosb.ilt.ams.repository.FactoryRepository;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
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
class FactoryDatafetcherTest {

    private static final String HEADER_NAME = "Authorization";

    @Value("${test.token}")
    String token;

    @Value("${test.graph}")
    String graph;

    private static String FACTORY_ID = "";

    @Autowired FactoryDAO factoryDAO;

    @Mock DatafetcherSecurity security;

    @Mock private ObjectRdf4jRepository repository;

    @Mock FactoryRepository repositoryUnderTest;

    @Autowired private MockMvc mockMvc;

    private List<String> createdIds;
    private ObjectMapper mapper;

    @Test
    @Order(0)
    void createRootFactory() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r"
                        + "\\n"
                        + "  createFactory(factory: \\r"
                        + "\\n"
                        + "    {\\r"
                        + "\\n"
                        + "      sourceId : \\\"Battery Factory Karlsruhe\\\",\\r"
                        + "\\n"
                        + "      label: \\\"Battery Factory Karlsruhe\\\",\\r"
                        + "\\n"
                        + "      labelLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "      description: \\\"This is a test factory\\\",\\r"
                        + "\\n"
                        + "      descriptionLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "      location: {\\r"
                        + "\\n"
                        + "        street: \\\"Fraunhoferstr.\\\",\\r"
                        + "\\n"
                        + "        streetNumber: \\\"1\\\",\\r"
                        + "\\n"
                        + "        zip: \\\"76131\\\",\\r"
                        + "\\n"
                        + "        city:\\\"Karlsruhe\\\",\\r"
                        + "\\n"
                        + "        country:\\\"Germany\\\"\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "    }) {code\\r"
                        + "\\n"
                        + "  message\\r"
                        + "\\n"
                        + "  factory {\\r"
                        + "\\n"
                        + "      id\\r"
                        + "\\n"
                        + "  }}\\r"
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
        var bodyJson = new ObjectMapper().readTree(body);
        FACTORY_ID = bodyJson.get("data").get("createFactory").get("factory").get("id").textValue();
    }

    @BeforeEach
    public void setUp() {
        createdIds = new LinkedList<>();
        mapper = new ObjectMapper();
    }

    @Test
    @Order(Integer.MAX_VALUE)
    void removeRootFactory() throws Exception {
        var request =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"query\":\"mutation {\\r"
                                                        + "\\n"
                                                        + "  deleteFactory(id: \\r"
                                                        + "\\n"
                                                        + "    \\\""
                                                        + FACTORY_ID
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
        var message = bodyJson.get("data").get("deleteFactory").get("message");
        assertThat(message.textValue()).isEqualTo("Deleted " + FACTORY_ID);
        System.out.println(message.textValue());
    }

    @Test
    @Order(1)
    public void createFactory() throws Exception {
        var request =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"query\":\"mutation {\\r"
                                                    + "\\n"
                                                    + "  createFactory(factory: \\r"
                                                    + "\\n"
                                                    + "    {\\r"
                                                    + "\\n"
                                                    + "      sourceId : \\\"Battery Factory"
                                                    + " Karlsruhe\\\",\\r"
                                                    + "\\n"
                                                    + "      label: \\\"Battery Factory"
                                                    + " Karlsruhe\\\",\\r"
                                                    + "\\n"
                                                    + "      labelLanguageCode: \\\"en\\\",\\r"
                                                    + "\\n"
                                                    + "      description: \\\"This is a test"
                                                    + " factory\\\",\\r"
                                                    + "\\n"
                                                    + "      descriptionLanguageCode:"
                                                    + " \\\"en\\\",\\r"
                                                    + "\\n"
                                                    + "      location: {\\r"
                                                    + "\\n"
                                                    + "        street: \\\"Fraunhoferstr.\\\",\\r"
                                                    + "\\n"
                                                    + "        streetNumber: \\\"1\\\",\\r"
                                                    + "\\n"
                                                    + "        zip: \\\"76131\\\",\\r"
                                                    + "\\n"
                                                    + "        city:\\\"Karlsruhe\\\",\\r"
                                                    + "\\n"
                                                    + "        country:\\\"Germany\\\"\\r"
                                                    + "\\n"
                                                    + "      }\\r"
                                                    + "\\n"
                                                    + "    }) {code\\r"
                                                    + "\\n"
                                                    + "  message\\r"
                                                    + "\\n"
                                                    + "  factory {\\r"
                                                    + "\\n"
                                                    + "      id\\r"
                                                    + "\\n"
                                                    + "  }}\\r"
                                                    + "\\n"
                                                    + "}\",\"variables\":{}}")
                                        .header(HEADER_NAME, "Bearer " + token))
                        .andReturn();

        var body = request.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        var bodyJson = objectMapper.readTree(body);
        var id = bodyJson.get("data").get("createFactory").get("factory").get("id").textValue();

        var checkRequest =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"query\":\"query {\\r"
                                                        + "\\n"
                                                        + "  factory {\\r"
                                                        + "\\n"
                                                        + "    id\\r"
                                                        + "\\n"
                                                        + "  }\\r"
                                                        + "\\n"
                                                        + "}\",\"variables\":{}}")
                                        .header(HEADER_NAME, "Bearer " + token))
                        .andDo(print())
                        .andReturn();

        var checkBodyJson = objectMapper.readTree(checkRequest.getResponse().getContentAsString());
        System.out.println(checkBodyJson.toPrettyString());
        boolean isThere = false;
        var idArray = checkBodyJson.get("data").get("factory");
        if (idArray.isArray()) {
            var iterator = idArray.elements();
            while (iterator.hasNext()) {
                var factory = iterator.next();
                if (factory.get("id").textValue().equals(id)) {
                    isThere = true;
                }
            }
            assertTrue(isThere);
        }

        System.out.println(id);
        createdIds.add(id);
        System.out.println(bodyJson.toPrettyString());
    }

    @Test
    public void factory() throws Exception {
        mockMvc.perform(
                        post("/graphql")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"query\":\"query {\\r"
                                                + "\\n"
                                                + "  factory {\\r"
                                                + "\\n"
                                                + "    id\\r"
                                                + "\\n"
                                                + "  }\\r"
                                                + "\\n"
                                                + "}\",\"variables\":{}}")
                                .header(HEADER_NAME, "Bearer " + token))
                .andDo(print())
                .andExpect(content().string(containsString("\"id\":\"" + graph)));
    }

    @Test
    @Order(1)
    public void createPropertyForFactory() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r"
                        + "\\n"
                        + " createPropertyForFactory(property: {\\r"
                        + "\\n"
                        + "    sourceId: \\\"CO2_Emissions\\\",\\r"
                        + "\\n"
                        + "    label: \\\"CO2 Emissions\\\",\\r"
                        + "\\n"
                        + "    labelLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "    description: \\\"This is a test property\\\",\\r"
                        + "\\n"
                        + "    descriptionLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "    value: \\\"CO2 neutral production\\\",\\r"
                        + "\\n"
                        + "    semanticReferences: [\\r"
                        + "\\n"
                        + "        {\\r"
                        + "\\n"
                        + "        sourceUri: \\\"sem_ref_CO2_emissions\\\",\\r"
                        + "\\n"
                        + "        label: \\\"CO2 Emissions\\\",\\r"
                        + "\\n"
                        + "        labelLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "        description: \\\"This is a test semantic reference\\\",\\r"
                        + "\\n"
                        + "        descriptionLanguageCode: \\\"en\\\"\\r"
                        + "\\n"
                        + "        }\\r"
                        + "\\n"
                        + "    ]\\r"
                        + "\\n"
                        + "}, factoryId: \\\""
                        + FACTORY_ID
                        + "\\\"\\r"
                        + "\\n"
                        + ") {code\\r"
                        + "\\n"
                        + "\\tmessage\\r"
                        + "\\n"
                        + "\\tproperty\\r"
                        + "\\n"
                        + " {id\\r"
                        + "\\n"
                        + "}}\\r"
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
                createResponse.get("data").get("createPropertyForFactory").get("code").asInt(),
                200);
        assertEquals(
                createResponse
                        .get("data")
                        .get("createPropertyForFactory")
                        .get("message")
                        .textValue(),
                "Success");
        var propertyId =
                createResponse
                        .get("data")
                        .get("createPropertyForFactory")
                        .get("property")
                        .get("id")
                        .textValue();

        var controlResponseData = this.getPropertyDataForFactory(FACTORY_ID);
        var factoryArray = (ArrayNode) controlResponseData.get("factory");
        assertFalse(factoryArray.isEmpty());
        var propertiesArray = (ArrayNode) factoryArray.get(0).get("properties");
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
        var secondControlResponseData = this.getPropertyDataForFactory(FACTORY_ID);
        var secondFactoryArray = (ArrayNode) secondControlResponseData.get("factory");
        var secondPropertiesArray = (ArrayNode) secondFactoryArray.get(0).get("properties");
        for (com.fasterxml.jackson.databind.JsonNode property : secondPropertiesArray) {
            if (property.get("id").textValue().equals(propertyId)) {
                isExisting = true;
            }
        }
        assertFalse(isExisting);
    }

    public JsonNode getPropertyDataForFactory(String factoryId) throws Exception {
        String controlRequestText =
                "{\"query\":\"query {\\r\\n  factory("
                        + "filter: {id: \\\""
                        + factoryId
                        + "\\\"}){\\r"
                        + "\\n"
                        + "    id \\r"
                        + "\\n"
                        + "    properties{\\r"
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
    public void createProductForFactory() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r"
                        + "\\n"
                        + " createProductForFactory(product: {\\r"
                        + "\\n"
                        + "    sourceId : \\\"Battery\\\",\\r"
                        + "\\n"
                        + "    label: \\\"Battery\\\",\\r"
                        + "\\n"
                        + "    labelLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "    description: \\\"This is a test product\\\",\\r"
                        + "\\n"
                        + "    descriptionLanguageCode: \\\"en\\\"\\r"
                        + "\\n"
                        + "}, factoryId: \\\""
                        + FACTORY_ID
                        + "\\\"\\r"
                        + "\\n"
                        + ") {code\\r"
                        + "\\n"
                        + "\\tmessage\\r"
                        + "\\n"
                        + " product {id sourceId label labelLanguageCode description"
                        + " descriptionLanguageCode}\\r"
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
                createResponse.get("data").get("createProductForFactory").get("code").asInt(), 200);
        assertEquals(
                createResponse
                        .get("data")
                        .get("createProductForFactory")
                        .get("message")
                        .textValue(),
                "Success");

        var productId =
                createResponse
                        .get("data")
                        .get("createProductForFactory")
                        .get("product")
                        .get("id")
                        .textValue();
        var sourceId =
                createResponse
                        .get("data")
                        .get("createProductForFactory")
                        .get("product")
                        .get("sourceId")
                        .textValue();
        var label =
                createResponse
                        .get("data")
                        .get("createProductForFactory")
                        .get("product")
                        .get("label")
                        .textValue();
        var labelLang =
                createResponse
                        .get("data")
                        .get("createProductForFactory")
                        .get("product")
                        .get("labelLanguageCode")
                        .textValue();
        var description =
                createResponse
                        .get("data")
                        .get("createProductForFactory")
                        .get("product")
                        .get("description")
                        .textValue();

        assertEquals(sourceId, "Battery");
        assertEquals(label, "Battery");
        assertEquals(labelLang, "en");
        assertEquals(description, "This is a test product");

        var controlResponseData = this.getProductDataForFactory(FACTORY_ID);
        var factoryArray = (ArrayNode) controlResponseData.get("factory");
        assertFalse(factoryArray.isEmpty());
        var productsArray = (ArrayNode) factoryArray.get(0).get("products");
        assertFalse(productsArray.isEmpty());
        boolean isExisting = false;

        for (com.fasterxml.jackson.databind.JsonNode product : productsArray) {
            if (product.get("id").textValue().equals(productId)) {
                isExisting = true;
            }
        }

        assertTrue(isExisting);
        deleteProduct(productId);
        isExisting = false;

        var secondControlResponseData = this.getProductDataForFactory(FACTORY_ID);
        var secondFactoryArray = (ArrayNode) secondControlResponseData.get("factory");
        var secondProductsArray = (ArrayNode) secondFactoryArray.get(0).get("products");
        for (com.fasterxml.jackson.databind.JsonNode product : secondProductsArray) {
            if (product.get("id").textValue().equals(productId)) {
                isExisting = true;
            }
        }
        assertFalse(isExisting);
    }

    public JsonNode getProductDataForFactory(String factoryId) throws Exception {
        String controlRequestText =
                "{\"query\":\"query {\\r\\n  factory("
                        + "filter: {id: \\\""
                        + factoryId
                        + "\\\"}){\\r"
                        + "\\n"
                        + "    id \\r"
                        + "\\n"
                        + "    products{\\r"
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

    public void deleteProduct(String id) throws Exception {
        String deleteRequestText =
                "{\"query\":\"mutation {\\r\\n  deleteProduct(productId:\\r\\n  "
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
    public void createProcessForFactory() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r"
                        + "\\n"
                        + " createProcessForFactory(process: {\\r"
                        + "\\n"
                        + "    sourceId: \\\"Assembling Process AP003\\\",\\r"
                        + "\\n"
                        + "    description: \\\"This is a test process\\\",\\r"
                        + "\\n"
                        + "    descriptionLanguageCode: \\\"en\\\"\\r"
                        + "\\n"
                        + "}, factoryId: \\\""
                        + FACTORY_ID
                        + "\\\"\\r"
                        + "\\n"
                        + ") {code\\r"
                        + "\\n"
                        + "\\tmessage process{id sourceId description descriptionLanguageCode}\\r"
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
                createResponse.get("data").get("createProcessForFactory").get("code").asInt(), 200);
        assertEquals(
                createResponse
                        .get("data")
                        .get("createProcessForFactory")
                        .get("message")
                        .textValue(),
                "Success");

        var processId =
                createResponse
                        .get("data")
                        .get("createProcessForFactory")
                        .get("process")
                        .get("id")
                        .textValue();
        var sourceId =
                createResponse
                        .get("data")
                        .get("createProcessForFactory")
                        .get("process")
                        .get("sourceId")
                        .textValue();
        var description =
                createResponse
                        .get("data")
                        .get("createProcessForFactory")
                        .get("process")
                        .get("description")
                        .textValue();
        var descriptionLang =
                createResponse
                        .get("data")
                        .get("createProcessForFactory")
                        .get("process")
                        .get("descriptionLanguageCode")
                        .textValue();

        assertEquals(sourceId, "Assembling Process AP003");
        assertEquals(descriptionLang, "en");
        assertEquals(description, "This is a test process");

        var controlResponseData = this.getProcessDataForFactory(FACTORY_ID);
        var factoryArray = (ArrayNode) controlResponseData.get("factory");
        assertFalse(factoryArray.isEmpty());
        var processArray = (ArrayNode) factoryArray.get(0).get("processes");
        assertFalse(processArray.isEmpty());
        boolean isExisting = false;

        for (com.fasterxml.jackson.databind.JsonNode process : processArray) {
            if (process.get("id").textValue().equals(processId)) {
                isExisting = true;
            }
        }

        assertTrue(isExisting);

        deleteProcess(processId);
        isExisting = false;

        var secondControlResponseData = this.getProcessDataForFactory(FACTORY_ID);
        var secondFactoryArray = (ArrayNode) secondControlResponseData.get("factory");
        var secondProcessArray = (ArrayNode) secondFactoryArray.get(0).get("processes");
        for (com.fasterxml.jackson.databind.JsonNode process : secondProcessArray) {
            if (process.get("id").textValue().equals(processId)) {
                isExisting = true;
            }
        }
        assertFalse(isExisting);
    }

    public JsonNode getProcessDataForFactory(String factoryId) throws Exception {
        String controlRequestText =
                "{\"query\":\"query {\\r\\n  factory("
                        + "filter: {id: \\\""
                        + factoryId
                        + "\\\"}){\\r"
                        + "\\n"
                        + "    id \\r"
                        + "\\n"
                        + "    processes{\\r"
                        + "\\n"
                        + "        id\\r"
                        + "\\n"
                        + "        description\\r"
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

    public void deleteProcess(String id) throws Exception {
        String deleteRequestText =
                "{\"query\":\"mutation {\\r\\n    "
                        + "deleteProcess(\\r\\n    processId: \\\""
                        + id
                        + "\\\", deleteChildren: true\\r"
                        + "\\n"
                        + "    ) {\\r"
                        + "\\n"
                        + "    message\\r"
                        + "\\n"
                        + "  }\\r"
                        + "\\n"
                        + "}\",\"variables\":{}}";

        var deleteRequest =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(deleteRequestText)
                                        .header(HEADER_NAME, "Bearer " + token))
                        .andDo(print())
                        .andReturn();
    }

    @Test
    @Order(1)
    public void createMachineForFactory() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r"
                        + "\\n"
                        + " createMachineForFactory(machine: {\\r"
                        + "\\n"
                        + "      sourceId : \\\"Assembling_Machine\\\",\\r"
                        + "\\n"
                        + "      label: \\\"Assembling Machine\\\",\\r"
                        + "\\n"
                        + "      labelLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "      description: \\\"This is a test machine\\\",\\r"
                        + "\\n"
                        + "      descriptionLanguageCode: \\\"en\\\"\\r"
                        + "\\n"
                        + "}, factoryId: \\\""
                        + FACTORY_ID
                        + "\\\"\\r"
                        + "\\n"
                        + ") {code\\r"
                        + "\\n"
                        + "\\tmessage machine {id sourceId label labelLanguageCode description"
                        + " descriptionLanguageCode}\\r"
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
                createResponse.get("data").get("createMachineForFactory").get("code").asInt(), 200);
        assertEquals(
                createResponse
                        .get("data")
                        .get("createMachineForFactory")
                        .get("message")
                        .textValue(),
                "Success");

        var machineId =
                createResponse
                        .get("data")
                        .get("createMachineForFactory")
                        .get("machine")
                        .get("id")
                        .textValue();
        var sourceId =
                createResponse
                        .get("data")
                        .get("createMachineForFactory")
                        .get("machine")
                        .get("sourceId")
                        .textValue();
        var label =
                createResponse
                        .get("data")
                        .get("createMachineForFactory")
                        .get("machine")
                        .get("label")
                        .textValue();
        var labelLang =
                createResponse
                        .get("data")
                        .get("createMachineForFactory")
                        .get("machine")
                        .get("labelLanguageCode")
                        .textValue();
        var description =
                createResponse
                        .get("data")
                        .get("createMachineForFactory")
                        .get("machine")
                        .get("description")
                        .textValue();
        var descriptionLang =
                createResponse
                        .get("data")
                        .get("createMachineForFactory")
                        .get("machine")
                        .get("descriptionLanguageCode")
                        .textValue();

        assertEquals(sourceId, "Assembling_Machine");
        assertEquals(labelLang, "en");
        assertEquals(label, "Assembling Machine");
        assertEquals(descriptionLang, "en");
        assertEquals(description, "This is a test machine");

        var controlResponseData = this.getMachineDataForFactory(FACTORY_ID);
        var factoryArray = (ArrayNode) controlResponseData.get("factory");
        assertFalse(factoryArray.isEmpty());
        var machineArray = (ArrayNode) factoryArray.get(0).get("machines");
        assertFalse(machineArray.isEmpty());
        boolean isExisting = false;

        for (com.fasterxml.jackson.databind.JsonNode machine : machineArray) {
            if (machine.get("id").textValue().equals(machineId)) {
                isExisting = true;
            }
        }

        assertTrue(isExisting);

        deleteMachine(machineId);
        isExisting = false;

        var secondControlResponseData = this.getMachineDataForFactory(FACTORY_ID);
        var secondFactoryArray = (ArrayNode) secondControlResponseData.get("factory");
        var secondMachineArray = (ArrayNode) secondFactoryArray.get(0).get("machines");
        for (com.fasterxml.jackson.databind.JsonNode machine : secondMachineArray) {
            if (machine.get("id").textValue().equals(machineId)) {
                isExisting = true;
            }
        }
        assertFalse(isExisting);
    }

    public JsonNode getMachineDataForFactory(String factoryId) throws Exception {
        String controlRequestText =
                "{\"query\":\"query {\\r\\n  factory("
                        + "filter: {id: \\\""
                        + factoryId
                        + "\\\"}){\\r"
                        + "\\n"
                        + "    id \\r"
                        + "\\n"
                        + "    machines{\\r"
                        + "\\n"
                        + "        id\\r"
                        + "\\n"
                        + "        description\\r"
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

    public void deleteMachine(String id) throws Exception {
        String deleteRequestText =
                "{\"query\":\"mutation {\\r\\n  deleteMachine(machineId:\\r\\n  "
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
    public void createHumanResourceForFactory() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r"
                        + "\\n"
                        + " createHumanResourceForFactory(humanResource: {\\r"
                        + "\\n"
                        + "      sourceId : \\\"HumanResource HR002\\\",\\r"
                        + "\\n"
                        + "      label: \\\"Machine Operator\\\",\\r"
                        + "\\n"
                        + "      labelLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "      description: \\\"This is a test human resource\\\",\\r"
                        + "\\n"
                        + "      descriptionLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "}, factoryId: \\\""
                        + FACTORY_ID
                        + "\\\"\\r"
                        + "\\n"
                        + ") {code\\r"
                        + "\\n"
                        + "\\tmessage\\r"
                        + "\\n"
                        + "    humanResource {\\r"
                        + "\\n"
                        + "        id sourceId label labelLanguageCode description"
                        + " descriptionLanguageCode\\r"
                        + "\\n"
                        + "    }\\r"
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
                createResponse.get("data").get("createHumanResourceForFactory").get("code").asInt(),
                200);
        assertEquals(
                createResponse
                        .get("data")
                        .get("createHumanResourceForFactory")
                        .get("message")
                        .textValue(),
                "Success");

        var hrId =
                createResponse
                        .get("data")
                        .get("createHumanResourceForFactory")
                        .get("humanResource")
                        .get("id")
                        .textValue();
        var sourceId =
                createResponse
                        .get("data")
                        .get("createHumanResourceForFactory")
                        .get("humanResource")
                        .get("sourceId")
                        .textValue();
        var label =
                createResponse
                        .get("data")
                        .get("createHumanResourceForFactory")
                        .get("humanResource")
                        .get("label")
                        .textValue();
        var labelLang =
                createResponse
                        .get("data")
                        .get("createHumanResourceForFactory")
                        .get("humanResource")
                        .get("labelLanguageCode")
                        .textValue();
        var description =
                createResponse
                        .get("data")
                        .get("createHumanResourceForFactory")
                        .get("humanResource")
                        .get("description")
                        .textValue();
        var descriptionLang =
                createResponse
                        .get("data")
                        .get("createHumanResourceForFactory")
                        .get("humanResource")
                        .get("descriptionLanguageCode")
                        .textValue();

        assertEquals(sourceId, "HumanResource HR002");
        assertEquals(labelLang, "en");
        assertEquals(label, "Machine Operator");
        assertEquals(descriptionLang, "en");
        assertEquals(description, "This is a test human resource");

        var controlResponseData = this.getHumanResourceDataForFactory(FACTORY_ID);
        var factoryArray = (ArrayNode) controlResponseData.get("factory");
        assertFalse(factoryArray.isEmpty());
        var hrArray = (ArrayNode) factoryArray.get(0).get("humanResources");
        assertFalse(hrArray.isEmpty());
        boolean isExisting = false;

        for (com.fasterxml.jackson.databind.JsonNode humanResource : hrArray) {
            if (humanResource.get("id").textValue().equals(hrId)) {
                isExisting = true;
            }
        }

        assertTrue(isExisting);

        deleteHumanResource(hrId);
        isExisting = false;

        var secondControlResponseData = this.getHumanResourceDataForFactory(FACTORY_ID);
        var secondFactoryArray = (ArrayNode) secondControlResponseData.get("factory");
        var secondHrArray = (ArrayNode) secondFactoryArray.get(0).get("humanResources");
        for (com.fasterxml.jackson.databind.JsonNode humanResource : secondHrArray) {
            if (humanResource.get("id").textValue().equals(hrId)) {
                isExisting = true;
            }
        }
        assertFalse(isExisting);
    }

    public JsonNode getHumanResourceDataForFactory(String factoryId) throws Exception {
        String controlRequestText =
                "{\"query\":\"query {\\r\\n  factory("
                        + "filter: {id: \\\""
                        + factoryId
                        + "\\\"}){\\r"
                        + "\\n"
                        + "    id \\r"
                        + "\\n"
                        + "    humanResources{\\r"
                        + "\\n"
                        + "        id\\r"
                        + "\\n"
                        + "        description\\r"
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

    public void deleteHumanResource(String id) throws Exception {
        String deleteRequestText =
                "{\"query\":\"mutation {\\r\\n  deleteHumanResource(humanResourceId:\\r\\n  "
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

    @AfterEach
    public void tearDown() throws Exception {
        for (String id : createdIds) {
            var request =
                    mockMvc.perform(
                                    post("/graphql")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(
                                                    "{\"query\":\"mutation {\\r"
                                                            + "\\n"
                                                            + "  deleteFactory(id: \\r"
                                                            + "\\n"
                                                            + "    \\\""
                                                            + id
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
            var message = bodyJson.get("data").get("deleteFactory").get("message");
            assertThat(message.textValue()).isEqualTo("Deleted " + id);
        }
        createdIds.clear();
        createdIds = null;
    }
}
