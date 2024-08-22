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
class PropertyDatafetcherTest {
    private static final String HEADER_NAME = "Authorization";

    @Value("${test.token}")
    String token;

    @Value("${test.graph}")
    String graph;

    private static String PROPERTY_ID = "";

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
    public void property() throws Exception {
        String requestText =
                "{\"query\":\"query {\\r"
                        + "\\n"
                        + "  property {\\r"
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
    public void createProperty() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r"
                    + "\\n"
                    + "  createProperty(property: {\\r"
                    + "\\n"
                    + "    sourceId: \\\"Working Conditions\\\",\\r"
                    + "\\n"
                    + "    label: \\\"Working Conditions\\\",\\r"
                    + "\\n"
                    + "    labelLanguageCode: \\\"en\\\",\\r"
                    + "\\n"
                    + "    description: \\\"This is a test property\\\",\\r"
                    + "\\n"
                    + "    descriptionLanguageCode: \\\"en\\\",\\r"
                    + "\\n"
                    + "    value: \\\"fair\\\",\\r"
                    + "\\n"
                    + "    semanticReferences: [\\r"
                    + "\\n"
                    + "        {\\r"
                    + "\\n"
                    + "        sourceUri: \\\"sem_ref_working_conditions\\\",\\r"
                    + "\\n"
                    + "        label: \\\"Working Conditions\\\",\\r"
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
                    + "  }) {\\r"
                    + "\\n"
                    + "      code message property {id sourceId label labelLanguageCode description"
                    + " descriptionLanguageCode value}\\r"
                    + "\\n"
                    + "      }\\r"
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
        var id = bodyJson.get("data").get("createProperty").get("property").get("id").textValue();
        var sourceId =
                bodyJson.get("data")
                        .get("createProperty")
                        .get("property")
                        .get("sourceId")
                        .textValue();
        var description =
                bodyJson.get("data")
                        .get("createProperty")
                        .get("property")
                        .get("description")
                        .textValue();
        var value =
                bodyJson.get("data").get("createProperty").get("property").get("value").textValue();
        var descriptionLang =
                bodyJson.get("data")
                        .get("createProperty")
                        .get("property")
                        .get("descriptionLanguageCode")
                        .textValue();
        var label =
                bodyJson.get("data").get("createProperty").get("property").get("label").textValue();
        var labelLang =
                bodyJson.get("data")
                        .get("createProperty")
                        .get("property")
                        .get("labelLanguageCode")
                        .textValue();
        PROPERTY_ID = id;

        assertEquals(sourceId, "Working Conditions");
        assertEquals(label, "Working Conditions");
        assertEquals(labelLang, "en");
        assertEquals(value, "fair");
        assertEquals(description, "This is a test property");
        assertEquals(descriptionLang, "en");

        var checkRequest =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"query\":\"query {\\r"
                                                        + "\\n"
                                                        + "  property {\\r"
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
        var idArray = checkBodyJson.get("data").get("property");
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
    public void createSemanticReferenceForProperty() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r"
                        + "\\n"
                        + " createSemanticReferenceForProperty(semanticReference: {\\r"
                        + "\\n"
                        + "    sourceUri: \\\"sem_ref_working_conditions\\\",\\r"
                        + "\\n"
                        + "    label: \\\"Working Conditions\\\",\\r"
                        + "\\n"
                        + "    labelLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "    description: \\\"This is a test semantic reference\\\",\\r"
                        + "\\n"
                        + "    descriptionLanguageCode: \\\"en\\\"\\r"
                        + "\\n"
                        + "}, propertyId: \\\""
                        + PROPERTY_ID
                        + "\\\"\\r"
                        + "\\n"
                        + ") {code\\r"
                        + "\\n"
                        + "\\tmessage\\r"
                        + "\\n"
                        + "    semanticReference { id sourceUri label labelLanguageCode description"
                        + " descriptionLanguageCode }\\r"
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
                        .get("createSemanticReferenceForProperty")
                        .get("code")
                        .asInt(),
                200);
        assertEquals(
                createResponse
                        .get("data")
                        .get("createSemanticReferenceForProperty")
                        .get("message")
                        .textValue(),
                "Success");

        var id =
                createResponse
                        .get("data")
                        .get("createSemanticReferenceForProperty")
                        .get("semanticReference")
                        .get("id")
                        .textValue();
        var sourceId =
                createResponse
                        .get("data")
                        .get("createSemanticReferenceForProperty")
                        .get("semanticReference")
                        .get("sourceUri")
                        .textValue();
        var description =
                createResponse
                        .get("data")
                        .get("createSemanticReferenceForProperty")
                        .get("semanticReference")
                        .get("description")
                        .textValue();
        var descriptionLang =
                createResponse
                        .get("data")
                        .get("createSemanticReferenceForProperty")
                        .get("semanticReference")
                        .get("descriptionLanguageCode")
                        .textValue();
        var label =
                createResponse
                        .get("data")
                        .get("createSemanticReferenceForProperty")
                        .get("semanticReference")
                        .get("label")
                        .textValue();
        var labelLang =
                createResponse
                        .get("data")
                        .get("createSemanticReferenceForProperty")
                        .get("semanticReference")
                        .get("labelLanguageCode")
                        .textValue();

        assertEquals(sourceId, "sem_ref_working_conditions");
        assertEquals(descriptionLang, "en");
        assertEquals(description, "This is a test semantic reference");
        assertEquals(label, "Working Conditions");
        assertEquals(labelLang, "en");

        var controlResponseData = this.getSemRefDataForProperty(PROPERTY_ID);
        var propertyArray = (ArrayNode) controlResponseData.get("property");
        assertFalse(propertyArray.isEmpty());
        var semRefArray = (ArrayNode) propertyArray.get(0).get("semanticReferences");
        assertFalse(semRefArray.isEmpty());
        boolean isExisting = false;

        for (com.fasterxml.jackson.databind.JsonNode semRef : semRefArray) {
            if (semRef.get("id").textValue().equals(id)) {
                isExisting = true;
            }
        }

        assertTrue(isExisting);

        deleteSemRef(id);
        isExisting = false;

        var secondControlResponseData = this.getSemRefDataForProperty(PROPERTY_ID);
        var secondPropertyArray = (ArrayNode) secondControlResponseData.get("property");
        var secondSemRefArray = (ArrayNode) secondPropertyArray.get(0).get("semanticReferences");
        for (com.fasterxml.jackson.databind.JsonNode semRef : secondSemRefArray) {
            if (semRef.get("id").textValue().equals(id)) {
                isExisting = true;
            }
        }
        assertFalse(isExisting);
    }

    public JsonNode getSemRefDataForProperty(String propertyId) throws Exception {
        String controlRequestText =
                "{\"query\":\"query {\\r\\n  property("
                        + "filter: {id: \\\""
                        + propertyId
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

    public void deleteSemRef(String id) throws Exception {
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
    @Order(Integer.MAX_VALUE)
    public void deleteProperty() throws Exception {
        String deleteRequestText =
                "{\"query\":\"mutation {\\r\\n    "
                        + "deleteProperty(propertyId: \\\""
                        + PROPERTY_ID
                        + "\\\") "
                        + "{\\r\\n    message\\r\\n    code\\r\\n  }\\r\\n}\",\"variables\":{}}";

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
        var message = bodyJson.get("data").get("deleteProperty").get("message");
        assertThat(message.textValue()).isEqualTo("Deleted " + PROPERTY_ID);
        System.out.println(message.textValue());
    }
}
