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
class ProductClassDatafetcherTest {

    private static final String HEADER_NAME = "Authorization";

    @Value("${test.token}")
    String token;

    @Value("${test.graph}")
    String graph;

    private static String PRODUCT_CLASS_ID = "";

    @Autowired private MockMvc mockMvc;

    private List<String> createdIds;
    private ObjectMapper mapper;

    @BeforeEach
    public void setUp() {
        createdIds = new LinkedList<>();
        mapper = new ObjectMapper();
    }

    @Test
    @Order(1)
    public void productClass() throws Exception {
        String requestText =
                "{\"query\":\"query {\\r"
                        + "\\n"
                        + "  productClass {\\r"
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
    public void createProductClass() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r"
                        + "\\n"
                        + "  createProductClass(productClass: \\r"
                        + "\\n"
                        + "        {\\r"
                        + "\\n"
                        + "            sourceId : \\\"Product Class Test\\\",\\r"
                        + "\\n"
                        + "            label: \\\"Product Class Test\\\",\\r"
                        + "\\n"
                        + "            labelLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "            description: \\\"This is a test product class\\\",\\r"
                        + "\\n"
                        + "            descriptionLanguageCode: \\\"en\\\" }\\r"
                        + "\\n"
                        + "    ) {code\\r"
                        + "\\n"
                        + "  message productClass {id sourceId label labelLanguageCode description"
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
                        .get("createProductClass")
                        .get("productClass")
                        .get("id")
                        .textValue();
        var sourceId =
                bodyJson.get("data")
                        .get("createProductClass")
                        .get("productClass")
                        .get("sourceId")
                        .textValue();
        var label =
                bodyJson.get("data")
                        .get("createProductClass")
                        .get("productClass")
                        .get("label")
                        .textValue();
        var description =
                bodyJson.get("data")
                        .get("createProductClass")
                        .get("productClass")
                        .get("description")
                        .textValue();
        PRODUCT_CLASS_ID = id;

        assertEquals(sourceId, "Product Class Test");
        assertEquals(label, "Product Class Test");
        assertEquals(description, "This is a test product class");

        var checkRequest =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"query\":\"query {\\r"
                                                        + "\\n"
                                                        + "  productClass {\\r"
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
        var idArray = checkBodyJson.get("data").get("productClass");
        if (idArray.isArray()) {
            var iterator = idArray.elements();
            while (iterator.hasNext()) {
                var productClass = iterator.next();
                if (productClass.get("id").textValue().equals(id)) {
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
    public void createSemanticReferenceForProductClass() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r"
                        + "\\n"
                        + "  createSemanticReferenceForProductClass(semanticReference: {\\r"
                        + "\\n"
                        + "    sourceUri: \\\"sem_ref_conflict_minerals\\\",\\r"
                        + "\\n"
                        + "    label: \\\"Conflict Minerals\\\",\\r"
                        + "\\n"
                        + "    labelLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "    description: \\\"This is a test semantic reference\\\",\\r"
                        + "\\n"
                        + "    descriptionLanguageCode: \\\"en\\\"\\r"
                        + "\\n"
                        + "  }, productClassId: \\\""
                        + PRODUCT_CLASS_ID
                        + "\\\") {\\r"
                        + "\\n"
                        + "    semanticReference {\\r"
                        + "\\n"
                        + "      id sourceUri label description descriptionLanguageCode"
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
                        .get("createSemanticReferenceForProductClass")
                        .get("code")
                        .asInt(),
                200);
        assertEquals(
                createResponse
                        .get("data")
                        .get("createSemanticReferenceForProductClass")
                        .get("message")
                        .textValue(),
                "Success");

        var semRefId =
                createResponse
                        .get("data")
                        .get("createSemanticReferenceForProductClass")
                        .get("semanticReference")
                        .get("id")
                        .textValue();
        var sourceId =
                createResponse
                        .get("data")
                        .get("createSemanticReferenceForProductClass")
                        .get("semanticReference")
                        .get("sourceUri")
                        .textValue();
        var description =
                createResponse
                        .get("data")
                        .get("createSemanticReferenceForProductClass")
                        .get("semanticReference")
                        .get("description")
                        .textValue();
        var descriptionLang =
                createResponse
                        .get("data")
                        .get("createSemanticReferenceForProductClass")
                        .get("semanticReference")
                        .get("descriptionLanguageCode")
                        .textValue();
        var label =
                createResponse
                        .get("data")
                        .get("createSemanticReferenceForProductClass")
                        .get("semanticReference")
                        .get("label")
                        .textValue();
        var labelLang =
                createResponse
                        .get("data")
                        .get("createSemanticReferenceForProductClass")
                        .get("semanticReference")
                        .get("labelLanguageCode")
                        .textValue();

        assertEquals("sem_ref_conflict_minerals", sourceId);
        assertEquals("Conflict Minerals", label);
        assertEquals("en", labelLang);
        assertEquals("This is a test semantic reference", description);
        assertEquals("en", descriptionLang);

        var controlResponseData = this.getSemanticReferenceForProductClass(PRODUCT_CLASS_ID);
        var productClassArray = (ArrayNode) controlResponseData.get("productClass");
        assertFalse(productClassArray.isEmpty());
        var semRefArray = (ArrayNode) productClassArray.get(0).get("semanticReferences");
        assertFalse(semRefArray.isEmpty());
        boolean isExisting = false;
        for (com.fasterxml.jackson.databind.JsonNode semRef : semRefArray) {
            if (semRef.get("id").textValue().equals(semRefId)) {
                isExisting = true;
            }
        }
        assertTrue(isExisting);
        removeSemanticReferenceFromProductClass(semRefId, PRODUCT_CLASS_ID);
        isExisting = false;
        var secondControlResponseData = this.getSemanticReferenceForProductClass(PRODUCT_CLASS_ID);
        var secondProductClassArray = (ArrayNode) secondControlResponseData.get("productClass");
        var secondSemRefArray =
                (ArrayNode) secondProductClassArray.get(0).get("semanticReferences");
        for (com.fasterxml.jackson.databind.JsonNode sce : secondSemRefArray) {
            if (sce.get("id").textValue().equals(semRefId)) {
                isExisting = true;
            }
        }
        assertFalse(isExisting);
    }

    @Test
    @Order(1)
    public void updateProductClass() throws Exception {
        String queryText =
                "{\"query\":\"query productClass{\\r\\n  productClass(filter: {id: \\\""
                        + PRODUCT_CLASS_ID
                        + "\\\"}){\\r"
                        + "\\n"
                        + "    id\\r"
                        + "\\n"
                        + "    sourceId\\r"
                        + "\\n"
                        + "    description label\\r"
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
        var supplyChain = queryResponse.get("data").get("productClass").get(0);
        var sourceId = supplyChain.get("sourceId").textValue();
        var description = supplyChain.get("description").textValue();

        var newSourceId = sourceId + "updated";
        var newDescription = "Updated: " + description;

        var updateText =
                "{\"query\":\"mutation {\\r\\n  "
                        + "updateProductClass(productClassId: \\\""
                        + PRODUCT_CLASS_ID
                        + "\\\""
                        + ",\\r\\n    productClass:{\\r\\n      sourceId : \\\""
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
                        + "  message productClass{ sourceId description}}\\r"
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
                        .get("updateProductClass")
                        .get("productClass")
                        .get("sourceId")
                        .textValue();
        var updatedDescription =
                updateResponse
                        .get("data")
                        .get("updateProductClass")
                        .get("productClass")
                        .get("description")
                        .textValue();

        assertEquals(newSourceId, updatedSourceId);
        assertEquals(newDescription, updatedDescription);
    }

    public JsonNode getSemanticReferenceForProductClass(String productClassId) throws Exception {
        String controlRequestText =
                "{\"query\":\"query {\\r\\n  productClass("
                        + "filter: {id: \\\""
                        + productClassId
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

    public void removeSemanticReferenceFromProductClass(String semRefId, String pcId)
            throws Exception {

        String text =
                "{\"query\":\"mutation {\\r\\n removeSemanticReferenceFromProductClass(\\r\\n    "
                        + "semanticReferenceId: \\\""
                        + semRefId
                        + "\\\",\\r\\n    "
                        + "productClassId: \\\""
                        + pcId
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
    public void deleteProductClass() throws Exception {
        String deleteRequestText =
                "{\"query\":\"mutation {\\r\\n    "
                        + "deleteProductClass(productClassId: \\\""
                        + PRODUCT_CLASS_ID
                        + "\\\",\\r"
                        + "\\n"
                        + "    deleteChildren: true) {\\r"
                        + "\\n"
                        + "    message\\r"
                        + "\\n"
                        + "    code\\r"
                        + "\\n"
                        + "  }\\r"
                        + "\\n"
                        + "}\",\"variables\":{}}";

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
        var message = bodyJson.get("data").get("deleteProductClass").get("message");
        assertThat(message.textValue()).isEqualTo("Deleted " + PRODUCT_CLASS_ID);
    }
}
