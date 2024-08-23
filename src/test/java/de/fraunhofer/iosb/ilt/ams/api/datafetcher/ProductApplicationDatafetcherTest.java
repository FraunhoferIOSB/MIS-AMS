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
class ProductApplicationDatafetcherTest {
    private static final String HEADER_NAME = "Authorization";

    @Value("${test.token}")
    String token;

    @Value("${test.graph}")
    String graph;

    private static String PRODUCT_APPLICATION_ID = "";

    @Autowired private MockMvc mockMvc;

    private List<String> createdIds;
    private ObjectMapper mapper;

    @BeforeEach
    public void setUp() {
        createdIds = new LinkedList<>();
        mapper = new ObjectMapper();
    }

    @Test
    public void productApplication() throws Exception {
        String requestText =
                "{\"query\":\"query {\\r"
                        + "\\n"
                        + "  productApplication {\\r"
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
    public void createProductApplication() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r"
                        + "\\n"
                        + "  createProductApplication(productApplication: \\r"
                        + "\\n"
                        + "        {\\r"
                        + "\\n"
                        + "            sourceId : \\\"Product Application Test\\\"}\\r"
                        + "\\n"
                        + "    ) {code\\r"
                        + "\\n"
                        + "  message productApplication {id sourceId }}\\r"
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
                        .get("createProductApplication")
                        .get("productApplication")
                        .get("id")
                        .textValue();
        var sourceId =
                bodyJson.get("data")
                        .get("createProductApplication")
                        .get("productApplication")
                        .get("sourceId")
                        .textValue();
        PRODUCT_APPLICATION_ID = id;

        assertEquals(sourceId, "Product Application Test");

        var checkRequest =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"query\":\"query {\\r"
                                                        + "\\n"
                                                        + "  productApplication {\\r"
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
        var idArray = checkBodyJson.get("data").get("productApplication");
        if (idArray.isArray()) {
            var iterator = idArray.elements();
            while (iterator.hasNext()) {
                var productApplication = iterator.next();
                if (productApplication.get("id").textValue().equals(id)) {
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
    public void createPropertyForProductApplication() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r"
                        + "\\n"
                        + "  createPropertyForProductApplication(property: {\\r"
                        + "\\n"
                        + "    sourceId: \\\"Property Test\\\",\\r"
                        + "\\n"
                        + "    label: \\\"Test Property\\\",\\r"
                        + "\\n"
                        + "    labelLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "    description: \\\"This is a test property\\\",\\r"
                        + "\\n"
                        + "    descriptionLanguageCode: \\\"en\\\"\\r"
                        + "\\n"
                        + "  }, productApplicationId: \\\""
                        + PRODUCT_APPLICATION_ID
                        + "\\\") {\\r"
                        + "\\n"
                        + "    property {\\r"
                        + "\\n"
                        + "      id sourceId label description descriptionLanguageCode"
                        + " labelLanguageCode\\r"
                        + "\\n"
                        + "    }\\r"
                        + "\\n"
                        + "    code\\r"
                        + "\\n"
                        + "    message\\r"
                        + "\\n"
                        + "  }\\r"
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
                        .get("createPropertyForProductApplication")
                        .get("code")
                        .asInt(),
                200);
        assertEquals(
                createResponse
                        .get("data")
                        .get("createPropertyForProductApplication")
                        .get("message")
                        .textValue(),
                "Success");

        var propertyId =
                createResponse
                        .get("data")
                        .get("createPropertyForProductApplication")
                        .get("property")
                        .get("id")
                        .textValue();
        var sourceId =
                createResponse
                        .get("data")
                        .get("createPropertyForProductApplication")
                        .get("property")
                        .get("sourceId")
                        .textValue();
        var description =
                createResponse
                        .get("data")
                        .get("createPropertyForProductApplication")
                        .get("property")
                        .get("description")
                        .textValue();
        var descriptionLang =
                createResponse
                        .get("data")
                        .get("createPropertyForProductApplication")
                        .get("property")
                        .get("descriptionLanguageCode")
                        .textValue();
        var label =
                createResponse
                        .get("data")
                        .get("createPropertyForProductApplication")
                        .get("property")
                        .get("label")
                        .textValue();
        var labelLang =
                createResponse
                        .get("data")
                        .get("createPropertyForProductApplication")
                        .get("property")
                        .get("labelLanguageCode")
                        .textValue();

        assertEquals("Property Test", sourceId);
        assertEquals("Test Property", label);
        assertEquals("en", labelLang);
        assertEquals("This is a test property", description);
        assertEquals("en", descriptionLang);

        var controlResponseData = this.getPropertyForProductApplication(PRODUCT_APPLICATION_ID);
        var productApplicationArray = (ArrayNode) controlResponseData.get("productApplication");
        assertFalse(productApplicationArray.isEmpty());
        var propertyArray = (ArrayNode) productApplicationArray.get(0).get("properties");
        assertFalse(propertyArray.isEmpty());
        boolean isExisting = false;
        for (com.fasterxml.jackson.databind.JsonNode property : propertyArray) {
            if (property.get("id").textValue().equals(propertyId)) {
                isExisting = true;
            }
        }
        assertTrue(isExisting);
        removePropertyFromProductApplication(propertyId, PRODUCT_APPLICATION_ID);
        isExisting = false;
        var secondControlResponseData =
                this.getPropertyForProductApplication(PRODUCT_APPLICATION_ID);
        var secondProductApplicationArray =
                (ArrayNode) secondControlResponseData.get("productApplication");
        var secondPropertyArray =
                (ArrayNode) secondProductApplicationArray.get(0).get("properties");
        for (com.fasterxml.jackson.databind.JsonNode property : secondPropertyArray) {
            if (property.get("id").textValue().equals(propertyId)) {
                isExisting = true;
            }
        }
        assertFalse(isExisting);
    }

    @Test
    @Order(1)
    public void updateProductApplication() throws Exception {
        String queryText =
                "{\"query\":\"query productApplication{\\r\\n  productApplication(filter: {id: \\\""
                        + PRODUCT_APPLICATION_ID
                        + "\\\"}){\\r"
                        + "\\n"
                        + "    id\\r"
                        + "\\n"
                        + "    sourceId\\r"
                        + "\\n"
                        + "    \\r"
                        + "\\n"
                        + "  }\\r"
                        + "\\n"
                        + "}\",\"variables\":{}}";

        var queryRequest =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(queryText)
                                        .header(HEADER_NAME, "Bearer " + token))
                        .andReturn();
        var queryResponse = mapper.readTree(queryRequest.getResponse().getContentAsString());
        var productApplication = queryResponse.get("data").get("productApplication").get(0);
        var sourceId = productApplication.get("sourceId").textValue();

        var newSourceId = sourceId + "updated";

        var updateText =
                "{\"query\":\"mutation {\\r\\n  "
                        + "updateProductApplication(productApplicationId: \\\""
                        + PRODUCT_APPLICATION_ID
                        + "\\\""
                        + ",\\r\\n    productApplication:{\\r\\n      sourceId : \\\""
                        + newSourceId
                        + "\\\"         }) {code\\r"
                        + "\\n"
                        + "  message productApplication{ sourceId}}\\r"
                        + "\\n"
                        + "}\",\"variables\":{}}";

        var updateRequest =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(updateText)
                                        .header(HEADER_NAME, "Bearer " + token))
                        .andReturn();

        var updateResponse = mapper.readTree(updateRequest.getResponse().getContentAsString());

        var updatedSourceId =
                updateResponse
                        .get("data")
                        .get("updateProductApplication")
                        .get("productApplication")
                        .get("sourceId")
                        .textValue();

        assertEquals(newSourceId, updatedSourceId);
    }

    public JsonNode getPropertyForProductApplication(String productApplicationId) throws Exception {
        String controlRequestText =
                "{\"query\":\"query {\\r\\n  productApplication("
                        + "filter: {id: \\\""
                        + productApplicationId
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

    public void removePropertyFromProductApplication(String propertyId, String productApplicationId)
            throws Exception {

        String text =
                "{\"query\":\"mutation {\\r\\n removePropertyFromProductApplication(\\r\\n    "
                        + "propertyId: \\\""
                        + propertyId
                        + "\\\",\\r\\n    "
                        + "productApplicationId: \\\""
                        + productApplicationId
                        + "\\\"\\r\\n) {code\\r\\n\\tmessage\\r\\n}\\r\\n}\",\"variables\":{}}";

        var deleteRequest =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(text)
                                        .header(HEADER_NAME, "Bearer " + token))
                        .andReturn();
    }

    @Test
    @Order(2)
    public void deleteProductApplication() throws Exception {
        String deleteRequest =
                "{\"query\":\"mutation {\\r"
                        + "\\n"
                        + "    deleteProductApplication(productApplicationId: \\\""
                        + PRODUCT_APPLICATION_ID
                        + "\\\") "
                        + "{\\r\\n    code\\r\\n    message\\r\\n  }\\r\\n}\",\"variables\":{}}";

        var request =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(deleteRequest)
                                        .header(HEADER_NAME, "Bearer " + token))
                        .andReturn();
        var body = request.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        var bodyJson = objectMapper.readTree(body);
        var message = bodyJson.get("data").get("deleteProductApplication").get("message");
        assertThat(message.textValue()).isEqualTo("Deleted " + PRODUCT_APPLICATION_ID);
    }
}
