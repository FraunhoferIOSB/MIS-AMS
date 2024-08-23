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
class SupplyChainDatafetcherTest {
    private static final String HEADER_NAME = "Authorization";

    @Value("${test.token}")
    String token;

    @Value("${test.graph}")
    String graph;

    private static String SUPPLY_CHAIN_ID = "";

    @Autowired private MockMvc mockMvc;

    private List<String> createdIds;
    private ObjectMapper mapper;

    @Test
    @Order(0)
    void createRootSupplyChain() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r"
                    + "\\n"
                    + "  createSupplyChain(supplyChain: \\r"
                    + "\\n"
                    + "        {\\r"
                    + "\\n"
                    + "            sourceId : \\\"Battery Supply Chain\\\",\\r"
                    + "\\n"
                    + "            description: \\\"This is a test supply chain\\\",\\r"
                    + "\\n"
                    + "            descriptionLanguageCode: \\\"en\\\",\\r"
                    + "\\n"
                    + "            suppliers : []\\r"
                    + "\\n"
                    + "        }\\r"
                    + "\\n"
                    + "    ) {code\\r"
                    + "\\n"
                    + "  message supplyChain {id sourceId description descriptionLanguageCode}}\\r"
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
        SUPPLY_CHAIN_ID =
                bodyJson.get("data")
                        .get("createSupplyChain")
                        .get("supplyChain")
                        .get("id")
                        .textValue();
    }

    @BeforeEach
    public void setUp() {
        createdIds = new LinkedList<>();
        mapper = new ObjectMapper();
    }

    @Test
    @Order(Integer.MAX_VALUE)
    void removeRootSupplyChain() throws Exception {
        var request =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"query\":\"mutation {\\r\\n  "
                                                        + "deleteSupplyChain(id: \\r\\n    \\\""
                                                        + SUPPLY_CHAIN_ID
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
        var message = bodyJson.get("data").get("deleteSupplyChain").get("message");
        assertThat(message.textValue()).isEqualTo("Deleted " + SUPPLY_CHAIN_ID);
        System.out.println(message.textValue());
    }

    @Test
    public void supplyChain() throws Exception {
        String requestText =
                "{\"query\":\"query {\\r"
                        + "\\n"
                        + "  supplyChain {\\r"
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
    public void createSupplyChain() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r"
                    + "\\n"
                    + "  createSupplyChain(supplyChain: \\r"
                    + "\\n"
                    + "        {\\r"
                    + "\\n"
                    + "            sourceId : \\\"Battery Supply Chain\\\",\\r"
                    + "\\n"
                    + "            description: \\\"This is a test supply chain\\\",\\r"
                    + "\\n"
                    + "            descriptionLanguageCode: \\\"en\\\",\\r"
                    + "\\n"
                    + "            suppliers : []\\r"
                    + "\\n"
                    + "        }\\r"
                    + "\\n"
                    + "    ) {code\\r"
                    + "\\n"
                    + "  message supplyChain {id sourceId description descriptionLanguageCode}}\\r"
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
                        .get("createSupplyChain")
                        .get("supplyChain")
                        .get("id")
                        .textValue();
        var sourceId =
                bodyJson.get("data")
                        .get("createSupplyChain")
                        .get("supplyChain")
                        .get("sourceId")
                        .textValue();
        var description =
                bodyJson.get("data")
                        .get("createSupplyChain")
                        .get("supplyChain")
                        .get("description")
                        .textValue();

        assertEquals(sourceId, "Battery Supply Chain");
        assertEquals(description, "This is a test supply chain");

        var checkRequest =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"query\":\"query {\\r"
                                                        + "\\n"
                                                        + "  supplyChain {\\r"
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
        var idArray = checkBodyJson.get("data").get("supplyChain");
        if (idArray.isArray()) {
            var iterator = idArray.elements();
            while (iterator.hasNext()) {
                var supplyChain = iterator.next();
                if (supplyChain.get("id").textValue().equals(id)) {
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
    public void createSupplyChainElementForSupplyChain() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r"
                        + "\\n"
                        + " createSupplyChainElementForSupplyChain(supplyChainElement: {\\r"
                        + "\\n"
                        + "    sourceId : \\\"Batteriezellen_Lieferant_S001\\\",\\r"
                        + "\\n"
                        + "    description: \\\"Batteriezellen Lieferant\\\",\\r"
                        + "\\n"
                        + "    descriptionLanguageCode: \\\"de\\\",\\r"
                        + "\\n"
                        + "    enterprise: {\\r"
                        + "\\n"
                        + "        sourceId : \\\"Battery AG\\\",\\r"
                        + "\\n"
                        + "        label : \\\"Battery AG\\\",\\r"
                        + "\\n"
                        + "        labelLanguageCode : \\\"en\\\"\\r"
                        + "\\n"
                        + "    }\\r"
                        + "\\n"
                        + "}, supplyChainId: \\\""
                        + SUPPLY_CHAIN_ID
                        + "\\\"\\r"
                        + "\\n"
                        + ") {code message supplyChainElement{id sourceId description"
                        + " descriptionLanguageCode enterprise {id sourceId label"
                        + " labelLanguageCode}}\\r"
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
                        .get("createSupplyChainElementForSupplyChain")
                        .get("code")
                        .asInt(),
                200);
        assertEquals(
                createResponse
                        .get("data")
                        .get("createSupplyChainElementForSupplyChain")
                        .get("message")
                        .textValue(),
                "Success");

        var sceId =
                createResponse
                        .get("data")
                        .get("createSupplyChainElementForSupplyChain")
                        .get("supplyChainElement")
                        .get("id")
                        .textValue();
        var sourceId =
                createResponse
                        .get("data")
                        .get("createSupplyChainElementForSupplyChain")
                        .get("supplyChainElement")
                        .get("sourceId")
                        .textValue();
        var description =
                createResponse
                        .get("data")
                        .get("createSupplyChainElementForSupplyChain")
                        .get("supplyChainElement")
                        .get("description")
                        .textValue();
        var descriptionLang =
                createResponse
                        .get("data")
                        .get("createSupplyChainElementForSupplyChain")
                        .get("supplyChainElement")
                        .get("descriptionLanguageCode")
                        .textValue();

        assertEquals("Batteriezellen_Lieferant_S001", sourceId);
        assertEquals("Batteriezellen Lieferant", description);
        assertEquals("de", descriptionLang);

        var controlResponseData = this.getSupplyChainElementForSupplyChain(SUPPLY_CHAIN_ID);
        var enterpriseArray = (ArrayNode) controlResponseData.get("supplyChain");
        assertFalse(enterpriseArray.isEmpty());
        var sceArray = (ArrayNode) enterpriseArray.get(0).get("suppliers");
        assertFalse(sceArray.isEmpty());
        boolean isExisting = false;
        for (com.fasterxml.jackson.databind.JsonNode sce : sceArray) {
            if (sce.get("id").textValue().equals(sceId)) {
                isExisting = true;
            }
        }
        assertTrue(isExisting);
        removeSupplyChainElementFromSupplyChain(sceId, SUPPLY_CHAIN_ID);
        isExisting = false;
        var secondControlResponseData = this.getSupplyChainElementForSupplyChain(SUPPLY_CHAIN_ID);
        var secondEnterpriseArray = (ArrayNode) secondControlResponseData.get("supplyChain");
        var secondSceArray = (ArrayNode) secondEnterpriseArray.get(0).get("suppliers");
        for (com.fasterxml.jackson.databind.JsonNode sce : secondSceArray) {
            if (sce.get("id").textValue().equals(sceId)) {
                isExisting = true;
            }
        }
        assertFalse(isExisting);
    }

    @Test
    @Order(1)
    public void updateSupplyChain() throws Exception {
        String queryText =
                "{\"query\":\"query supplyChain{\\r\\n  supplyChain(filter: {id: \\\""
                        + SUPPLY_CHAIN_ID
                        + "\\\"}){\\r"
                        + "\\n"
                        + "    id\\r"
                        + "\\n"
                        + "    sourceId\\r"
                        + "\\n"
                        + "    description\\r"
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
        var supplyChain = queryResponse.get("data").get("supplyChain").get(0);
        var sourceId = supplyChain.get("sourceId").textValue();
        var description = supplyChain.get("description").textValue();

        var newSourceId = sourceId + "updated";
        var newDescription = "Updated: " + description;

        var updateText =
                "{\"query\":\"mutation {\\r\\n  "
                        + "updateSupplyChain(id: \\\""
                        + SUPPLY_CHAIN_ID
                        + "\\\""
                        + ",\\r\\n    supplyChain:{\\r\\n      sourceId : \\\""
                        + newSourceId
                        + "\\\",\\r\\n      "
                        + "description: \\\""
                        + newDescription
                        + "\\\",\\r"
                        + "\\n"
                        + "      descriptionLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "    }) {code\\r"
                        + "\\n"
                        + "  message supplyChain{ sourceId description}}\\r"
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
                        .get("updateSupplyChain")
                        .get("supplyChain")
                        .get("sourceId")
                        .textValue();
        var updatedDescription =
                updateResponse
                        .get("data")
                        .get("updateSupplyChain")
                        .get("supplyChain")
                        .get("description")
                        .textValue();

        assertEquals(newSourceId, updatedSourceId);
        assertEquals(newDescription, updatedDescription);
    }

    public JsonNode getSupplyChainElementForSupplyChain(String supplyChainId) throws Exception {
        String controlRequestText =
                "{\"query\":\"query {\\r\\n  supplyChain("
                        + "filter: {id: \\\""
                        + supplyChainId
                        + "\\\"}){\\r"
                        + "\\n"
                        + "    id \\r"
                        + "\\n"
                        + "    suppliers{\\r"
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

    public void removeSupplyChainElementFromSupplyChain(String sceId, String scId)
            throws Exception {

        String text =
                "{\"query\":\"mutation {\\r\\n removeSupplyChainElementFromSupplyChain(\\r\\n    "
                        + "supplyChainElementId: \\\""
                        + sceId
                        + "\\\",\\r\\n    "
                        + "supplyChainId: \\\""
                        + scId
                        + "\\\"\\r\\n) {code\\r\\n\\tmessage\\r\\n}\\r\\n}\",\"variables\":{}}";

        var deleteRequest =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(text)
                                        .header(HEADER_NAME, "Bearer " + token))
                        .andReturn();
    }
}
