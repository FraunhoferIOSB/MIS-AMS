package de.fraunhofer.iosb.ilt.ams.api.datafetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import com.fasterxml.jackson.databind.ObjectMapper;
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
class SupplyChainElementDatafetcherTest {
    private static final String HEADER_NAME = "Authorization";

    @Value("${test.token}")
    String token;

    @Value("${test.graph}")
    String graph;

    @Value("${test.supply-chain-element-id}")
    private String SUPPLY_CHAIN_ELEMENT_ID;

    @Autowired private MockMvc mockMvc;

    private List<String> createdIds;
    private ObjectMapper mapper;

    @Test
    @Order(0)
    void createRootSupplyChainElement() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r"
                        + "\\n"
                        + " createSupplyChainElement(supplyChainElement: {\\r"
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
                        + "}) {code message supplyChainElement {id sourceId description"
                        + " descriptionLanguageCode}}\\r"
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
        SUPPLY_CHAIN_ELEMENT_ID =
                bodyJson.get("data")
                        .get("createSupplyChainElement")
                        .get("supplyChainElement")
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
    void removeRootSupplyChainElement() throws Exception {
        var request =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"query\":\"mutation {\\r\\n    "
                                                        + "deleteSupplyChainElement(id: \\\""
                                                        + SUPPLY_CHAIN_ELEMENT_ID
                                                        + "\\\") {\\r"
                                                        + "\\n"
                                                        + "    message\\r"
                                                        + "\\n"
                                                        + "    code\\r"
                                                        + "\\n"
                                                        + "  }\\r"
                                                        + "\\n"
                                                        + "}\",\"variables\":{}}")
                                        .header(HEADER_NAME, "Bearer " + token))
                        .andReturn();
        var body = request.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        var bodyJson = objectMapper.readTree(body);
        var message = bodyJson.get("data").get("deleteSupplyChainElement").get("message");
        assertThat(message.textValue()).isEqualTo("Deleted " + SUPPLY_CHAIN_ELEMENT_ID);
        System.out.println(message.textValue());
    }

    @Test
    @Order(1)
    public void supplyChainElement() throws Exception {
        String requestText =
                "{\"query\":\"query {\\r"
                        + "\\n"
                        + "  supplyChainElement {\\r"
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
    public void createSupplyChainElement() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r"
                        + "\\n"
                        + " createSupplyChainElement(supplyChainElement: {\\r"
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
                        + "}) {code message supplyChainElement {id sourceId description"
                        + " descriptionLanguageCode}}\\r"
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
                        .get("createSupplyChainElement")
                        .get("supplyChainElement")
                        .get("id")
                        .textValue();
        var sourceId =
                bodyJson.get("data")
                        .get("createSupplyChainElement")
                        .get("supplyChainElement")
                        .get("sourceId")
                        .textValue();
        var description =
                bodyJson.get("data")
                        .get("createSupplyChainElement")
                        .get("supplyChainElement")
                        .get("description")
                        .textValue();

        assertEquals(sourceId, "Batteriezellen_Lieferant_S001");
        assertEquals(description, "Batteriezellen Lieferant");

        var checkRequest =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"query\":\"query {\\r"
                                                        + "\\n"
                                                        + "  supplyChainElement {\\r"
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
        var idArray = checkBodyJson.get("data").get("supplyChainElement");
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
    public void updateSupplyChainElement() throws Exception {
        String queryText =
                "{\"query\":\"query supplyChainElement{\\r\\n  supplyChainElement(filter: {id: \\\""
                        + SUPPLY_CHAIN_ELEMENT_ID
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
        var supplyChain = queryResponse.get("data").get("supplyChainElement").get(0);

        if (supplyChain != null) {
            var sourceId = supplyChain.get("sourceId").textValue();
            var description = supplyChain.get("description").textValue();

            var newSourceId = sourceId + "updated";
            var newDescription = "Updated: " + description;

            var updateText =
                    "{\"query\":\"mutation {\\r\\n  "
                            + "updateSupplyChainElement(id: \\\""
                            + SUPPLY_CHAIN_ELEMENT_ID
                            + "\\\""
                            + ",\\r\\n    supplyChainElement:{\\r\\n      sourceId : \\\""
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
                            + "  message supplyChainElement{ sourceId description}}\\r"
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
                            .get("updateSupplyChainElement")
                            .get("supplyChainElement")
                            .get("sourceId")
                            .textValue();
            var updatedDescription =
                    updateResponse
                            .get("data")
                            .get("updateSupplyChainElement")
                            .get("supplyChainElement")
                            .get("description")
                            .textValue();

            assertEquals(newSourceId, updatedSourceId);
            assertEquals(newDescription, updatedDescription);
        }
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
                                                            + "  deleteSupplyChainElement(id: \\r"
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
            var message = bodyJson.get("data").get("deleteSupplyChainElement").get("message");
            assertThat(message.textValue()).isEqualTo("Deleted " + id);
            System.out.println(message.textValue());
        }
        createdIds.clear();
        createdIds = null;
    }
}
