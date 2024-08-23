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
import java.util.LinkedList;
import java.util.List;
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
class ProductDatafetcherTest {
    private static final String HEADER_NAME = "Authorization";

    @Value("${test.token}")
    String token;

    @Value("${test.graph}")
    String graph;

    private static String PRODUCT_ID = "";

    @Autowired private MockMvc mockMvc;

    private List<String> createdIds;
    private ObjectMapper mapper;

    @Test
    @Order(0)
    public void createRootProduct() throws Exception {
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
        var id = bodyJson.get("data").get("createProduct").get("product").get("id").textValue();
        PRODUCT_ID = id;
    }

    @Test
    @Order(Integer.MAX_VALUE)
    public void deleteRootProduct() throws Exception {
        String deleteRequestText =
                "{\"query\":\"mutation {\\r\\n  deleteProduct(productId: \\r\\n    \\\""
                        + PRODUCT_ID
                        + "\\\") "
                        + "{code\\r\\n  message}\\r\\n}\",\"variables\":{}}";

        var request =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(deleteRequestText)
                                        .header(HEADER_NAME, "Bearer " + token))
                        .andReturn();
        var body = request.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        var bodyJson = objectMapper.readTree(body);
        var message = bodyJson.get("data").get("deleteProduct").get("message");
        assertThat(message.textValue()).isEqualTo("Deleted " + PRODUCT_ID);
        System.out.println(message.textValue());
    }

    @BeforeEach
    public void setUp() {
        createdIds = new LinkedList<>();
        mapper = new ObjectMapper();
    }

    @Test
    public void product() throws Exception {
        String requestText =
                "{\"query\":\"query {\\r"
                        + "\\n"
                        + "  product {\\r"
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
    public void createProduct() throws Exception {
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
        var id = bodyJson.get("data").get("createProduct").get("product").get("id").textValue();
        var sourceId =
                bodyJson.get("data")
                        .get("createProduct")
                        .get("product")
                        .get("sourceId")
                        .textValue();
        var description =
                bodyJson.get("data")
                        .get("createProduct")
                        .get("product")
                        .get("description")
                        .textValue();
        var descriptionLang =
                bodyJson.get("data")
                        .get("createProduct")
                        .get("product")
                        .get("descriptionLanguageCode")
                        .textValue();
        var label =
                bodyJson.get("data").get("createProduct").get("product").get("label").textValue();
        var labelLang =
                bodyJson.get("data")
                        .get("createProduct")
                        .get("product")
                        .get("labelLanguageCode")
                        .textValue();

        var productPassportJson =
                bodyJson.get("data").get("createProduct").get("product").get("productPassport");

        var identifier = productPassportJson.get("identifier").textValue();

        assertEquals(sourceId, "Battery");
        assertEquals(label, "Battery");
        assertEquals(labelLang, "en");
        assertEquals(description, "This is a test product");
        assertEquals(descriptionLang, "en");

        assertEquals(identifier, "BatteryPassportA0001");

        var checkRequest =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"query\":\"query {\\r"
                                                        + "\\n"
                                                        + "  product {\\r"
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
        var idArray = checkBodyJson.get("data").get("product");
        if (idArray.isArray()) {
            var iterator = idArray.elements();
            while (iterator.hasNext()) {
                var property = iterator.next();
                if (property.get("id").textValue().equals(id)) {
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
    @Order(1)
    public void createPropertyForProduct() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r"
                        + "\\n"
                        + " createPropertyForProduct(property: {\\r"
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
                        + "    value: \\\"3 tCO2e\\\",\\r"
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
                        + "}, productId: \\\""
                        + PRODUCT_ID
                        + "\\\"\\r"
                        + "\\n"
                        + ") {code message property {id sourceId label description value}\\r"
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
                createResponse.get("data").get("createPropertyForProduct").get("code").asInt(),
                200);
        assertEquals(
                createResponse
                        .get("data")
                        .get("createPropertyForProduct")
                        .get("message")
                        .textValue(),
                "Success");

        var id =
                createResponse
                        .get("data")
                        .get("createPropertyForProduct")
                        .get("property")
                        .get("id")
                        .textValue();
        var sourceId =
                createResponse
                        .get("data")
                        .get("createPropertyForProduct")
                        .get("property")
                        .get("sourceId")
                        .textValue();
        var description =
                createResponse
                        .get("data")
                        .get("createPropertyForProduct")
                        .get("property")
                        .get("description")
                        .textValue();
        var label =
                createResponse
                        .get("data")
                        .get("createPropertyForProduct")
                        .get("property")
                        .get("label")
                        .textValue();

        assertEquals(sourceId, "CO2_Emissions");
        assertEquals(description, "This is a test property");
        assertEquals(label, "CO2 Emissions");

        var controlResponseData = this.getPropertyDataForProduct(PRODUCT_ID);
        var productArray = (ArrayNode) controlResponseData.get("product");
        assertFalse(productArray.isEmpty());
        var propertyArray = (ArrayNode) productArray.get(0).get("properties");
        assertFalse(propertyArray.isEmpty());
        boolean isExisting = false;

        for (com.fasterxml.jackson.databind.JsonNode semRef : propertyArray) {
            if (semRef.get("id").textValue().equals(id)) {
                isExisting = true;
            }
        }

        assertTrue(isExisting);

        deleteProperty(id);
        isExisting = false;

        var secondControlResponseData = this.getPropertyDataForProduct(PRODUCT_ID);
        var secondProductArray = (ArrayNode) secondControlResponseData.get("product");
        var secondPropertyArray = (ArrayNode) secondProductArray.get(0).get("properties");
        for (com.fasterxml.jackson.databind.JsonNode semRef : secondPropertyArray) {
            if (semRef.get("id").textValue().equals(id)) {
                isExisting = true;
            }
        }
        assertFalse(isExisting);
    }

    public JsonNode getPropertyDataForProduct(String productId) throws Exception {
        String controlRequestText =
                "{\"query\":\"query {\\r\\n  product("
                        + "filter: {id: \\\""
                        + productId
                        + "\\\"}){\\r"
                        + "\\n"
                        + "    id \\r"
                        + "\\n"
                        + "    properties{\\r"
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

    public void deleteProperty(String id) throws Exception {
        String deleteRequestText =
                "{\"query\":\"mutation {\\r\\n    "
                        + "deleteProperty(\\r\\n    propertyId: \\\""
                        + id
                        + "\\\""
                        + ") {\\r\\n    message\\r\\n  }\\r\\n}\",\"variables\":{}}";

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
    public void createProductClassForProduct() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r"
                        + "\\n"
                        + " createProductClassForProduct(productClass: {\\r"
                        + "\\n"
                        + "      sourceId : \\\"Automobile Battery Passenger Car\\\",\\r"
                        + "\\n"
                        + "      label: \\\"Automobile Battery Passenger Car\\\",\\r"
                        + "\\n"
                        + "      labelLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "      description: \\\"This is a test product class\\\",\\r"
                        + "\\n"
                        + "      descriptionLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "}, productId: \\\""
                        + PRODUCT_ID
                        + "\\\"\\r"
                        + "\\n"
                        + ") {code\\r"
                        + "\\n"
                        + "\\tmessage productClass{id sourceId label description}\\r"
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
                createResponse.get("data").get("createProductClassForProduct").get("code").asInt(),
                200);
        assertEquals(
                createResponse
                        .get("data")
                        .get("createProductClassForProduct")
                        .get("message")
                        .textValue(),
                "Success");

        var id =
                createResponse
                        .get("data")
                        .get("createProductClassForProduct")
                        .get("productClass")
                        .get("id")
                        .textValue();
        var sourceId =
                createResponse
                        .get("data")
                        .get("createProductClassForProduct")
                        .get("productClass")
                        .get("sourceId")
                        .textValue();
        var description =
                createResponse
                        .get("data")
                        .get("createProductClassForProduct")
                        .get("productClass")
                        .get("description")
                        .textValue();
        var label =
                createResponse
                        .get("data")
                        .get("createProductClassForProduct")
                        .get("productClass")
                        .get("label")
                        .textValue();

        assertEquals(sourceId, "Automobile Battery Passenger Car");
        assertEquals(description, "This is a test product class");
        assertEquals(label, "Automobile Battery Passenger Car");

        var controlResponseData = this.getProductClassDataForProduct(PRODUCT_ID);
        var productArray = (ArrayNode) controlResponseData.get("product");
        assertFalse(productArray.isEmpty());
        var productClassArray = (ArrayNode) productArray.get(0).get("productClasses");
        assertFalse(productClassArray.isEmpty());
        boolean isExisting = false;

        for (com.fasterxml.jackson.databind.JsonNode productClass : productClassArray) {
            if (productClass.get("id").textValue().equals(id)) {
                isExisting = true;
            }
        }

        assertTrue(isExisting);

        deleteProductClass(id);
        isExisting = false;

        var secondControlResponseData = this.getProductClassDataForProduct(PRODUCT_ID);
        var secondProductArray = (ArrayNode) secondControlResponseData.get("product");
        var secondProductClassArray = (ArrayNode) secondProductArray.get(0).get("productClasses");
        for (com.fasterxml.jackson.databind.JsonNode productClass : secondProductClassArray) {
            if (productClass.get("id").textValue().equals(id)) {
                isExisting = true;
            }
        }
        assertFalse(isExisting);
    }

    public JsonNode getProductClassDataForProduct(String productId) throws Exception {
        String controlRequestText =
                "{\"query\":\"query {\\r\\n  product("
                        + "filter: {id: \\\""
                        + productId
                        + "\\\"}){\\r"
                        + "\\n"
                        + "    id \\r"
                        + "\\n"
                        + "    productClasses{\\r"
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

    public void deleteProductClass(String id) throws Exception {
        String deleteRequestText =
                "{\"query\":\"mutation {\\r\\n    "
                        + "deleteProductClass(\\r\\n    productClassId: \\\""
                        + id
                        + "\\\", deleteChildren: true) {\\r"
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
    public void createSubProductForProduct() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r"
                        + "\\n"
                        + " createSubProductForProduct(subProductProductApplication: {\\r"
                        + "\\n"
                        + "      sourceId : \\\"Product Application Automobile Battery\\\",\\r"
                        + "\\n"
                        + "      product: {\\r"
                        + "\\n"
                        + "        sourceId: \\\"Battery\\\",\\r"
                        + "\\n"
                        + "        label: \\\"Battery\\\",\\r"
                        + "\\n"
                        + "        labelLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "        description: \\\"This is a test product\\\",\\r"
                        + "\\n"
                        + "\\t    descriptionLanguageCode: \\\"en\\\"\\r"
                        + "\\n"
                        + "      },\\r"
                        + "\\n"
                        + "      quantity: {\\r"
                        + "\\n"
                        + "        label: \\\"Batteriezelle Automobil\\\",\\r"
                        + "\\n"
                        + "        labelLanguageCode: \\\"de\\\",\\r"
                        + "\\n"
                        + "        description: \\\"Batteriezelle Automobil\\\",\\r"
                        + "\\n"
                        + "\\t    descriptionLanguageCode: \\\"de\\\",\\r"
                        + "\\n"
                        + "        value : \\\"20\\\"\\r"
                        + "\\n"
                        + "      }\\r"
                        + "\\n"
                        + "}, productId: \\\""
                        + PRODUCT_ID
                        + "\\\"\\r"
                        + "\\n"
                        + ") {code\\r"
                        + "\\n"
                        + "\\tmessage productApplication{ id sourceId product{id} }\\r"
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
                createResponse.get("data").get("createSubProductForProduct").get("code").asInt(),
                200);
        assertEquals(
                createResponse
                        .get("data")
                        .get("createSubProductForProduct")
                        .get("message")
                        .textValue(),
                "Success");

        var id =
                createResponse
                        .get("data")
                        .get("createSubProductForProduct")
                        .get("productApplication")
                        .get("id")
                        .textValue();
        // var sourceId =
        // createResponse.get("data").get("createSubProductForProduct").get("productApplication").get("sourceId").textValue();
        // var description =
        // createResponse.get("data").get("createSubProductForProduct").get("productApplication").get("product").get("description").textValue();
        // var label =
        // createResponse.get("data").get("createSubProductForProduct").get("productApplication").get("product").get("label").textValue();

        // assertEquals(sourceId, "Product Application Automobile Battery");
        // assertEquals(description, "This is a test product");
        // assertEquals(label, "Battery");

        var controlResponseData = this.getSubProductDataForProduct(PRODUCT_ID);
        var productArray = (ArrayNode) controlResponseData.get("product");
        assertFalse(productArray.isEmpty());
        var subProductArray = (ArrayNode) productArray.get(0).get("billOfMaterials");
        assertFalse(subProductArray.isEmpty());
        boolean isExisting = false;

        for (com.fasterxml.jackson.databind.JsonNode subProduct : subProductArray) {
            if (subProduct.get("id").textValue().equals(id)) {
                isExisting = true;
            }
        }

        assertTrue(isExisting);

        deleteProductApplication(id);
        isExisting = false;

        var secondControlResponseData = this.getSubProductDataForProduct(PRODUCT_ID);
        var secondProductArray = (ArrayNode) secondControlResponseData.get("product");
        var secondSubProductArray = (ArrayNode) secondProductArray.get(0).get("billOfMaterials");
        for (com.fasterxml.jackson.databind.JsonNode subProduct : secondSubProductArray) {
            if (subProduct.get("id").textValue().equals(id)) {
                isExisting = true;
            }
        }
        assertFalse(isExisting);
    }

    public JsonNode getSubProductDataForProduct(String productId) throws Exception {
        String controlRequestText =
                "{\"query\":\"query {\\r\\n  product("
                        + "filter: {id: \\\""
                        + productId
                        + "\\\"}){\\r"
                        + "\\n"
                        + "    id \\r"
                        + "\\n"
                        + "    billOfMaterials{\\r"
                        + "\\n"
                        + "        id\\r"
                        + "\\n"
                        + "        sourceId\\r"
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

    public void deleteProductApplication(String id) throws Exception {
        String deleteRequestText =
                "{\"query\":\"mutation {\\r\\n    "
                        + "deleteProductApplication(\\r\\n    productApplicationId: \\\""
                        + id
                        + "\\\""
                        + ") {\\r\\n    message\\r\\n  }\\r\\n}\",\"variables\":{}}";

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
    public void createSemanticReferenceForProduct() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r"
                        + "\\n"
                        + " createSemanticReferenceForProduct(semanticReference: {\\r"
                        + "\\n"
                        + "    sourceUri: \\\"sem_ref_CO2_emissions\\\",\\r"
                        + "\\n"
                        + "    label: \\\"CO2 Emissions\\\",\\r"
                        + "\\n"
                        + "    labelLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "    description: \\\"This is a test semantic reference\\\",\\r"
                        + "\\n"
                        + "    descriptionLanguageCode: \\\"en\\\"\\r"
                        + "\\n"
                        + "}, productId: \\\""
                        + PRODUCT_ID
                        + "\\\"\\r"
                        + "\\n"
                        + ") {code\\r"
                        + "\\n"
                        + "\\tmessage semanticReference{ id sourceUri label labelLanguageCode"
                        + " description}\\r"
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
                createResponse
                        .get("data")
                        .get("createSemanticReferenceForProduct")
                        .get("code")
                        .asInt(),
                200);
        assertEquals(
                createResponse
                        .get("data")
                        .get("createSemanticReferenceForProduct")
                        .get("message")
                        .textValue(),
                "Success");

        var id =
                createResponse
                        .get("data")
                        .get("createSemanticReferenceForProduct")
                        .get("semanticReference")
                        .get("id")
                        .textValue();
        var sourceId =
                createResponse
                        .get("data")
                        .get("createSemanticReferenceForProduct")
                        .get("semanticReference")
                        .get("sourceUri")
                        .textValue();
        var description =
                createResponse
                        .get("data")
                        .get("createSemanticReferenceForProduct")
                        .get("semanticReference")
                        .get("description")
                        .textValue();
        var label =
                createResponse
                        .get("data")
                        .get("createSemanticReferenceForProduct")
                        .get("semanticReference")
                        .get("label")
                        .textValue();
        var labelLang =
                createResponse
                        .get("data")
                        .get("createSemanticReferenceForProduct")
                        .get("semanticReference")
                        .get("labelLanguageCode")
                        .textValue();

        assertEquals(sourceId, "sem_ref_CO2_emissions");
        assertEquals(description, "This is a test semantic reference");
        assertEquals(label, "CO2 Emissions");
        assertEquals(labelLang, "en");

        var controlResponseData = this.getSemanticReferenceDataForProduct(PRODUCT_ID);
        var productArray = (ArrayNode) controlResponseData.get("product");
        assertFalse(productArray.isEmpty());
        var semRefArray = (ArrayNode) productArray.get(0).get("semanticReferences");
        assertFalse(semRefArray.isEmpty());
        boolean isExisting = false;

        for (com.fasterxml.jackson.databind.JsonNode semRef : semRefArray) {
            if (semRef.get("id").textValue().equals(id)) {
                isExisting = true;
            }
        }

        assertTrue(isExisting);

        deleteSemanticReference(id);
        isExisting = false;

        var secondControlResponseData = this.getSemanticReferenceDataForProduct(PRODUCT_ID);
        var secondProductArray = (ArrayNode) secondControlResponseData.get("product");
        var secondSemRefArray = (ArrayNode) secondProductArray.get(0).get("semanticReferences");
        for (com.fasterxml.jackson.databind.JsonNode semRef : secondSemRefArray) {
            if (semRef.get("id").textValue().equals(id)) {
                isExisting = true;
            }
        }
        assertFalse(isExisting);
    }

    public JsonNode getSemanticReferenceDataForProduct(String productId) throws Exception {
        String controlRequestText =
                "{\"query\":\"query {\\r\\n  product("
                        + "filter: {id: \\\""
                        + productId
                        + "\\\"}){\\r"
                        + "\\n"
                        + "    id \\r"
                        + "\\n"
                        + "    semanticReferences{\\r"
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

    public void deleteSemanticReference(String id) throws Exception {
        String deleteRequestText =
                "{\"query\":\"mutation {\\r\\n    "
                        + "deleteSemanticReference(\\r\\n    semanticReferenceId: \\\""
                        + id
                        + "\\\""
                        + ") {\\r\\n    message\\r\\n  }\\r\\n}\",\"variables\":{}}";

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
    public void createSupplyChainForProduct() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r"
                        + "\\n"
                        + " createSupplyChainForProduct(supplyChain: {\\r"
                        + "\\n"
                        + "      sourceId : \\\"Battery Supply Chain\\\",\\r"
                        + "\\n"
                        + "      description: \\\"This is a test supply chain\\\",\\r"
                        + "\\n"
                        + "      descriptionLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "}, productId: \\\""
                        + PRODUCT_ID
                        + "\\\"\\r"
                        + "\\n"
                        + ") {code\\r"
                        + "\\n"
                        + "\\tmessage supplyChain {id sourceId description"
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
                createResponse.get("data").get("createSupplyChainForProduct").get("code").asInt(),
                200);
        assertEquals(
                createResponse
                        .get("data")
                        .get("createSupplyChainForProduct")
                        .get("message")
                        .textValue(),
                "Success");

        var id =
                createResponse
                        .get("data")
                        .get("createSupplyChainForProduct")
                        .get("supplyChain")
                        .get("id")
                        .textValue();
        var sourceId =
                createResponse
                        .get("data")
                        .get("createSupplyChainForProduct")
                        .get("supplyChain")
                        .get("sourceId")
                        .textValue();
        var description =
                createResponse
                        .get("data")
                        .get("createSupplyChainForProduct")
                        .get("supplyChain")
                        .get("description")
                        .textValue();

        assertEquals(sourceId, "Battery Supply Chain");
        assertEquals(description, "This is a test supply chain");

        var controlResponseData = this.getSupplyChainDataForProduct(PRODUCT_ID);
        var productArray = (ArrayNode) controlResponseData.get("product");
        assertFalse(productArray.isEmpty());
        var supplyChainArray = (ArrayNode) productArray.get(0).get("supplyChains");
        assertFalse(supplyChainArray.isEmpty());
        boolean isExisting = false;

        for (com.fasterxml.jackson.databind.JsonNode supplyChain : supplyChainArray) {
            if (supplyChain.get("id").textValue().equals(id)) {
                isExisting = true;
            }
        }

        assertTrue(isExisting);

        deleteSupplyChain(id);
        isExisting = false;

        var secondControlResponseData = this.getSupplyChainDataForProduct(PRODUCT_ID);
        var secondProductArray = (ArrayNode) secondControlResponseData.get("product");
        var secondSupplyChainArray = (ArrayNode) secondProductArray.get(0).get("supplyChains");
        for (com.fasterxml.jackson.databind.JsonNode supplyChain : secondSupplyChainArray) {
            if (supplyChain.get("id").textValue().equals(id)) {
                isExisting = true;
            }
        }
        assertFalse(isExisting);
    }

    public JsonNode getSupplyChainDataForProduct(String productId) throws Exception {
        String controlRequestText =
                "{\"query\":\"query {\\r\\n  product("
                        + "filter: {id: \\\""
                        + productId
                        + "\\\"}){\\r"
                        + "\\n"
                        + "    id \\r"
                        + "\\n"
                        + "    supplyChains{\\r"
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

    public void deleteSupplyChain(String id) throws Exception {
        String deleteRequestText =
                "{\"query\":\"mutation {\\r\\n    "
                        + "deleteSupplyChain(\\r\\n    id: \\\""
                        + id
                        + "\\\""
                        + ") {\\r\\n    message\\r\\n  }\\r\\n}\",\"variables\":{}}";

        var deleteRequest =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(deleteRequestText)
                                        .header(HEADER_NAME, "Bearer " + token))
                        .andDo(print())
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
                                                            + "  deleteProduct(productId: \\r"
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
            var message = bodyJson.get("data").get("deleteProduct").get("message");
            assertThat(message.textValue()).isEqualTo("Deleted " + id);
        }
        createdIds.clear();
        createdIds = null;
    }
}
