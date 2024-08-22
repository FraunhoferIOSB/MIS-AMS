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
class CapabilityDatafetcherTest {
    private static final String HEADER_NAME = "Authorization";

    @Value("${test.token}")
    String token;

    @Value("${test.graph}")
    String graph;

    private static String CAPABILITY_ID = "";

    @Autowired private MockMvc mockMvc;

    private List<String> createdIds;
    private ObjectMapper mapper;

    @Test
    @Order(0)
    public void createRootCapability() throws Exception {
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
        var bodyJson = new ObjectMapper().readTree(body);
        CAPABILITY_ID =
                bodyJson.get("data")
                        .get("createCapability")
                        .get("capability")
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
    void removeRootCapability() throws Exception {
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
                                                        + CAPABILITY_ID
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
        assertThat(message.textValue()).isEqualTo("Deleted " + CAPABILITY_ID);
        System.out.println(message.textValue());
    }

    @Test
    public void capability() throws Exception {
        String requestText =
                "{\"query\":\"query {\\r"
                        + "\\n"
                        + "  capability {\\r"
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
    void createCapability() throws Exception {
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
        var id =
                bodyJson.get("data")
                        .get("createCapability")
                        .get("capability")
                        .get("id")
                        .textValue();
        var sourceId =
                bodyJson.get("data")
                        .get("createCapability")
                        .get("capability")
                        .get("sourceId")
                        .textValue();
        var label =
                bodyJson.get("data")
                        .get("createCapability")
                        .get("capability")
                        .get("label")
                        .textValue();
        var description =
                bodyJson.get("data")
                        .get("createCapability")
                        .get("capability")
                        .get("description")
                        .textValue();

        assertEquals(sourceId, "Heating");
        assertEquals(label, "Heating");
        assertEquals(description, "This is a test capability");

        var checkRequest =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"query\":\"query {\\r"
                                                        + "\\n"
                                                        + "  capability {\\r"
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
        var idArray = checkBodyJson.get("data").get("capability");
        if (idArray.isArray()) {
            var iterator = idArray.elements();
            while (iterator.hasNext()) {
                var capability = iterator.next();
                if (capability.get("id").textValue().equals(id)) {
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
    public void updateCapability() throws Exception {
        String queryText =
                "{\"query\":\"query capability{\\r\\n  capability(filter: {id: \\\""
                        + CAPABILITY_ID
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
        var capability = queryResponse.get("data").get("capability").get(0);
        var sourceId = capability.get("sourceId").textValue();
        var description = capability.get("description").textValue();
        var label = capability.get("label").textValue();

        var newSourceId = sourceId + "updated";
        var newDescription = "Updated: " + description;
        var newLabel = label + "updated";

        var updateText =
                "{\"query\":\"mutation {\\r\\n  "
                        + "updateCapability(capabilityId: \\\""
                        + CAPABILITY_ID
                        + "\\\""
                        + ",\\r\\n    capability:{\\r\\n      sourceId : \\\""
                        + newSourceId
                        + "\\\",\\r\\n      "
                        + "description: \\\""
                        + newDescription
                        + "\\\",\\r\\n      "
                        + "descriptionLanguageCode: \\\"en\\\",\\r\\n"
                        + "label: \\\""
                        + newLabel
                        + "\\\",\\r"
                        + "\\n"
                        + "      labelLanguageCode: \\\"en\\\"}) {code\\r"
                        + "\\n"
                        + "  message capability { sourceId description label}}\\r"
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
                        .get("updateCapability")
                        .get("capability")
                        .get("sourceId")
                        .textValue();
        var updatedDescription =
                updateResponse
                        .get("data")
                        .get("updateCapability")
                        .get("capability")
                        .get("description")
                        .textValue();
        var updatedLabel =
                updateResponse
                        .get("data")
                        .get("updateCapability")
                        .get("capability")
                        .get("label")
                        .textValue();

        assertEquals(newSourceId, updatedSourceId);
        assertEquals(newDescription, updatedDescription);
        assertEquals(newLabel, updatedLabel);
    }

    @Test
    @Order(1)
    public void createPropertyForCapability() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r"
                        + "\\n"
                        + "  createPropertyForCapability(property: {\\r"
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
                        + "  }, capabilityId: \\\""
                        + CAPABILITY_ID
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
                createResponse.get("data").get("createPropertyForCapability").get("code").asInt(),
                200);
        assertEquals(
                createResponse
                        .get("data")
                        .get("createPropertyForCapability")
                        .get("message")
                        .textValue(),
                "Success");

        var propertyId =
                createResponse
                        .get("data")
                        .get("createPropertyForCapability")
                        .get("property")
                        .get("id")
                        .textValue();
        var sourceId =
                createResponse
                        .get("data")
                        .get("createPropertyForCapability")
                        .get("property")
                        .get("sourceId")
                        .textValue();
        var description =
                createResponse
                        .get("data")
                        .get("createPropertyForCapability")
                        .get("property")
                        .get("description")
                        .textValue();
        var descriptionLang =
                createResponse
                        .get("data")
                        .get("createPropertyForCapability")
                        .get("property")
                        .get("descriptionLanguageCode")
                        .textValue();
        var label =
                createResponse
                        .get("data")
                        .get("createPropertyForCapability")
                        .get("property")
                        .get("label")
                        .textValue();
        var labelLang =
                createResponse
                        .get("data")
                        .get("createPropertyForCapability")
                        .get("property")
                        .get("labelLanguageCode")
                        .textValue();

        assertEquals("Property Test", sourceId);
        assertEquals("Test Property", label);
        assertEquals("en", labelLang);
        assertEquals("This is a test property", description);
        assertEquals("en", descriptionLang);

        var controlResponseData = this.getPropertyForCapability(CAPABILITY_ID);
        var capabilityArray = (ArrayNode) controlResponseData.get("capability");
        assertFalse(capabilityArray.isEmpty());
        var propertyArray = (ArrayNode) capabilityArray.get(0).get("properties");
        assertFalse(propertyArray.isEmpty());
        boolean isExisting = false;
        for (com.fasterxml.jackson.databind.JsonNode property : propertyArray) {
            if (property.get("id").textValue().equals(propertyId)) {
                isExisting = true;
            }
        }
        assertTrue(isExisting);
        removePropertyFromCapability(propertyId, CAPABILITY_ID);
        isExisting = false;
        var secondControlResponseData = this.getPropertyForCapability(CAPABILITY_ID);
        var secondCapabilityArray = (ArrayNode) secondControlResponseData.get("capability");
        var secondPropertyArray = (ArrayNode) secondCapabilityArray.get(0).get("properties");
        for (com.fasterxml.jackson.databind.JsonNode property : secondPropertyArray) {
            if (property.get("id").textValue().equals(propertyId)) {
                isExisting = true;
            }
        }
        assertFalse(isExisting);
    }

    public JsonNode getPropertyForCapability(String capabilityId) throws Exception {
        String controlRequestText =
                "{\"query\":\"query {\\r\\n  capability("
                        + "filter: {id: \\\""
                        + capabilityId
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

    public void removePropertyFromCapability(String propertyId, String capabilityId)
            throws Exception {

        String text =
                "{\"query\":\"mutation {\\r\\n removePropertyFromCapability(\\r\\n    "
                        + "propertyId: \\\""
                        + propertyId
                        + "\\\",\\r\\n    "
                        + "capabilityId: \\\""
                        + capabilityId
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
    @Order(1)
    public void createProcessForCapability() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r"
                        + "\\n"
                        + "  createProcessForCapability(process: {\\r"
                        + "\\n"
                        + "    sourceId: \\\"Drilling Process\\\",\\r"
                        + "\\n"
                        + "    description: \\\"This is a test process\\\",\\r"
                        + "\\n"
                        + "    descriptionLanguageCode: \\\"en\\\",\\r"
                        + "\\n"
                        + "  }, capabilityId: \\\""
                        + CAPABILITY_ID
                        + "\\\") {\\r"
                        + "\\n"
                        + "    code message\\r"
                        + "\\n"
                        + "    process {\\r"
                        + "\\n"
                        + "      id sourceId description descriptionLanguageCode\\r"
                        + "\\n"
                        + "    }\\r"
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
                createResponse.get("data").get("createProcessForCapability").get("code").asInt(),
                200);
        assertEquals(
                createResponse
                        .get("data")
                        .get("createProcessForCapability")
                        .get("message")
                        .textValue(),
                "Success");

        var processId =
                createResponse
                        .get("data")
                        .get("createProcessForCapability")
                        .get("process")
                        .get("id")
                        .textValue();
        var sourceId =
                createResponse
                        .get("data")
                        .get("createProcessForCapability")
                        .get("process")
                        .get("sourceId")
                        .textValue();
        var description =
                createResponse
                        .get("data")
                        .get("createProcessForCapability")
                        .get("process")
                        .get("description")
                        .textValue();
        var descriptionLang =
                createResponse
                        .get("data")
                        .get("createProcessForCapability")
                        .get("process")
                        .get("descriptionLanguageCode")
                        .textValue();

        assertEquals("Drilling Process", sourceId);
        assertEquals("This is a test process", description);
        assertEquals("en", descriptionLang);

        var controlResponseData = this.getProcessForCapability(CAPABILITY_ID);
        var capabilityArray = (ArrayNode) controlResponseData.get("capability");
        assertFalse(capabilityArray.isEmpty());
        var processArray = (ArrayNode) capabilityArray.get(0).get("processes");
        assertFalse(processArray.isEmpty());
        boolean isExisting = false;
        for (com.fasterxml.jackson.databind.JsonNode process : processArray) {
            if (process.get("id").textValue().equals(processId)) {
                isExisting = true;
            }
        }
        assertTrue(isExisting);
        removeProcessFromCapability(processId, CAPABILITY_ID);
        isExisting = false;
        var secondControlResponseData = this.getProcessForCapability(CAPABILITY_ID);
        var secondCapabilityArray = (ArrayNode) secondControlResponseData.get("capability");
        var secondProcessArray = (ArrayNode) secondCapabilityArray.get(0).get("processes");
        for (com.fasterxml.jackson.databind.JsonNode process : secondProcessArray) {
            if (process.get("id").textValue().equals(processId)) {
                isExisting = true;
            }
        }
        assertFalse(isExisting);
    }

    public JsonNode getProcessForCapability(String capabilityId) throws Exception {
        String controlRequestText =
                "{\"query\":\"query {\\r\\n  capability("
                        + "filter: {id: \\\""
                        + capabilityId
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

    public void removeProcessFromCapability(String processId, String capabilityId)
            throws Exception {

        String text =
                "{\"query\":\"mutation {\\r\\n removeProcessFromCapability(\\r\\n    "
                        + "processId: \\\""
                        + processId
                        + "\\\",\\r\\n    "
                        + "capabilityId: \\\""
                        + capabilityId
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
    @Order(1)
    public void createSemanticReferenceForCapability() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r"
                        + "\\n"
                        + "  createSemanticReferenceForCapability(semanticReference: {\\r"
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
                        + "  }, capabilityId: \\\""
                        + CAPABILITY_ID
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
                        .get("createSemanticReferenceForCapability")
                        .get("code")
                        .asInt(),
                200);
        assertEquals(
                createResponse
                        .get("data")
                        .get("createSemanticReferenceForCapability")
                        .get("message")
                        .textValue(),
                "Success");

        var semRefId =
                createResponse
                        .get("data")
                        .get("createSemanticReferenceForCapability")
                        .get("semanticReference")
                        .get("id")
                        .textValue();
        var sourceId =
                createResponse
                        .get("data")
                        .get("createSemanticReferenceForCapability")
                        .get("semanticReference")
                        .get("sourceUri")
                        .textValue();
        var description =
                createResponse
                        .get("data")
                        .get("createSemanticReferenceForCapability")
                        .get("semanticReference")
                        .get("description")
                        .textValue();
        var descriptionLang =
                createResponse
                        .get("data")
                        .get("createSemanticReferenceForCapability")
                        .get("semanticReference")
                        .get("descriptionLanguageCode")
                        .textValue();
        var label =
                createResponse
                        .get("data")
                        .get("createSemanticReferenceForCapability")
                        .get("semanticReference")
                        .get("label")
                        .textValue();
        var labelLang =
                createResponse
                        .get("data")
                        .get("createSemanticReferenceForCapability")
                        .get("semanticReference")
                        .get("labelLanguageCode")
                        .textValue();

        assertEquals("sem_ref_conflict_minerals", sourceId);
        assertEquals("Conflict Minerals", label);
        assertEquals("en", labelLang);
        assertEquals("This is a test semantic reference", description);
        assertEquals("en", descriptionLang);

        var controlResponseData = this.getSemanticReferenceForCapability(CAPABILITY_ID);
        var capabilityArray = (ArrayNode) controlResponseData.get("capability");
        assertFalse(capabilityArray.isEmpty());
        var semRefArray = (ArrayNode) capabilityArray.get(0).get("semanticReferences");
        assertFalse(semRefArray.isEmpty());
        boolean isExisting = false;
        for (com.fasterxml.jackson.databind.JsonNode semRef : semRefArray) {
            if (semRef.get("id").textValue().equals(semRefId)) {
                isExisting = true;
            }
        }
        assertTrue(isExisting);
        removeSemanticReferenceFromCapability(semRefId, CAPABILITY_ID);
        isExisting = false;
        var secondControlResponseData = this.getSemanticReferenceForCapability(CAPABILITY_ID);
        var secondCapabilityArray = (ArrayNode) secondControlResponseData.get("capability");
        var secondSemRefArray = (ArrayNode) secondCapabilityArray.get(0).get("semanticReferences");
        for (com.fasterxml.jackson.databind.JsonNode sce : secondSemRefArray) {
            if (sce.get("id").textValue().equals(semRefId)) {
                isExisting = true;
            }
        }
        assertFalse(isExisting);
    }

    public JsonNode getSemanticReferenceForCapability(String capabilityId) throws Exception {
        String controlRequestText =
                "{\"query\":\"query {\\r\\n  capability("
                        + "filter: {id: \\\""
                        + capabilityId
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

    public void removeSemanticReferenceFromCapability(String semRefId, String capabilityId)
            throws Exception {

        String text =
                "{\"query\":\"mutation {\\r\\n removeSemanticReferenceFromCapability(\\r\\n    "
                        + "semanticReferenceId: \\\""
                        + semRefId
                        + "\\\",\\r\\n    "
                        + "capabilityId: \\\""
                        + capabilityId
                        + "\\\"\\r\\n) {code\\r\\n\\tmessage\\r\\n}\\r\\n}\",\"variables\":{}}";

        var deleteRequest =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(text)
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
                                                            + "  deleteCapability(capabilityId: \\r"
                                                            + "\\n"
                                                            + "    \\\""
                                                            + id
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
            assertThat(message.textValue()).isEqualTo("Deleted " + id);
            System.out.println(message.textValue());
        }
        createdIds.clear();
        createdIds = null;
    }
}
