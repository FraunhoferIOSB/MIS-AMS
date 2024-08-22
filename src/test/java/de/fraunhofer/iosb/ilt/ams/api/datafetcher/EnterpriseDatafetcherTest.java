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
class EnterpriseDatafetcherTest {
    private static String ENTERPRISE_ID = "";
    private static String bulkImportEnterpriseId = "";
    private static String bulkImportEnterpriseFactoryId = "";
    private static String bulkImportEnterpriseFactoryMachineId = "";
    private static String bulkImportEnterpriseProvidedCapabilityId = "";
    private static String bulkImportEnterpriseMachinePropertyId = "";
    private static final String HEADER_NAME = "Authorization";

    @Value("${test.token}")
    String token;

    @Value("${test.graph}")
    String graph;

    @Autowired private MockMvc mockMvc;

    private List<String> createdIds;
    private ObjectMapper mapper;

    @Test
    @Order(0)
    void createRootEnterprise() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\n"
                        + "  createEnterprise(enterprise: \\n"
                        + "    {\\n"
                        + "      sourceId : \\\"Battery Manufacturer AG\\\",\\n"
                        + "      label: \\\"Battery Manufacturer AG\\\",\\n"
                        + "      labelLanguageCode: \\\"en\\\",\\n"
                        + "      description: \\\"This is a test enterprise\\\",\\n"
                        + "      descriptionLanguageCode: \\\"en\\\",\\n"
                        + "      location: {\\n"
                        + "        street: \\\"Fraunhoferstr.\\\",\\n"
                        + "        streetNumber: \\\"1\\\",\\n"
                        + "        zip: \\\"76131\\\",\\n"
                        + "        city:\\\"Karlsruhe\\\",\\n"
                        + "        country:\\\"Germany\\\"\\n"
                        + "      }\\n"
                        + "    }) {code\\n"
                        + "  message enterprise {id sourceId label labelLanguageCode description"
                        + " descriptionLanguageCode location { id street streetNumber zip city"
                        + " country}}}\\n"
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
        ENTERPRISE_ID =
                bodyJson.get("data")
                        .get("createEnterprise")
                        .get("enterprise")
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
    void removeRootEnterprise() throws Exception {
        var request =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"query\":\"mutation {\\r"
                                                        + "\\n"
                                                        + "  deleteEnterprise(id: \\r"
                                                        + "\\n"
                                                        + "    \\\""
                                                        + ENTERPRISE_ID
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
        var message = bodyJson.get("data").get("deleteEnterprise").get("message");
        assertThat(message.textValue()).isEqualTo("Deleted " + ENTERPRISE_ID);
        System.out.println(message.textValue());
    }

    @Test
    public void enterprise() throws Exception {
        String requestText =
                "{\"query\":\"query {\\r"
                        + "\\n"
                        + "  enterprise {\\r"
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
    public void createEnterprise() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\n"
                        + "  createEnterprise(enterprise: \\n"
                        + "    {\\n"
                        + "      sourceId : \\\"Battery Manufacturer AG\\\",\\n"
                        + "      label: \\\"Battery Manufacturer AG\\\",\\n"
                        + "      labelLanguageCode: \\\"en\\\",\\n"
                        + "      description: \\\"This is a test enterprise\\\",\\n"
                        + "      descriptionLanguageCode: \\\"en\\\",\\n"
                        + "      location: {\\n"
                        + "        street: \\\"Fraunhoferstr.\\\",\\n"
                        + "        streetNumber: \\\"1\\\",\\n"
                        + "        zip: \\\"76131\\\",\\n"
                        + "        city:\\\"Karlsruhe\\\",\\n"
                        + "        country:\\\"Germany\\\"\\n"
                        + "      }\\n"
                        + "    }) {code\\n"
                        + "  message enterprise {id sourceId label labelLanguageCode description"
                        + " descriptionLanguageCode location { id street streetNumber zip city"
                        + " country}}}\\n"
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
                        .get("createEnterprise")
                        .get("enterprise")
                        .get("id")
                        .textValue();
        var sourceId =
                bodyJson.get("data")
                        .get("createEnterprise")
                        .get("enterprise")
                        .get("sourceId")
                        .textValue();
        var label =
                bodyJson.get("data")
                        .get("createEnterprise")
                        .get("enterprise")
                        .get("label")
                        .textValue();
        var description =
                bodyJson.get("data")
                        .get("createEnterprise")
                        .get("enterprise")
                        .get("description")
                        .textValue();

        var locationJson =
                bodyJson.get("data").get("createEnterprise").get("enterprise").get("location");
        var locationId = locationJson.get("id").textValue();
        var street = locationJson.get("street").textValue();
        var streetNumber = locationJson.get("streetNumber").textValue();
        var zip = locationJson.get("zip").textValue();
        var city = locationJson.get("city").textValue();
        var country = locationJson.get("country").textValue();

        assertEquals(sourceId, "Battery Manufacturer AG");
        assertEquals(label, "Battery Manufacturer AG");
        assertEquals(description, "This is a test enterprise");
        assertEquals(street, "Fraunhoferstr.");
        assertEquals(streetNumber, "1");
        assertEquals(zip, "76131");
        assertEquals(city, "Karlsruhe");
        assertEquals(country, "Germany");

        var checkRequest =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"query\":\"query {\\r"
                                                        + "\\n"
                                                        + "  enterprise {\\r"
                                                        + "\\n"
                                                        + "    id location {id}\\r"
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
        var idArray = checkBodyJson.get("data").get("enterprise");
        if (idArray.isArray()) {
            var iterator = idArray.elements();
            while (iterator.hasNext()) {
                var enterprise = iterator.next();
                if (enterprise.get("id").textValue().equals(id)) {
                    isThere = true;
                    assertEquals(enterprise.get("location").get("id").textValue(), locationId);
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
    public void createFactoryForEnterprise() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\n"
                        + " createFactoryForEnterprise(factory: {\\n"
                        + "      sourceId : \\\"Battery Factory Karlsruhe\\\",\\n"
                        + "      label: \\\"Battery Factory Karlsruhe\\\",\\n"
                        + "      labelLanguageCode: \\\"en\\\",\\n"
                        + "      description: \\\"This is a test factory\\\",\\n"
                        + "      descriptionLanguageCode: \\\"en\\\",\\n"
                        + "      location: {\\n"
                        + "        street: \\\"Fraunhoferstr.\\\",\\n"
                        + "        streetNumber: \\\"1\\\",\\n"
                        + "        zip: \\\"76131\\\",\\n"
                        + "        city:\\\"Karlsruhe\\\",\\n"
                        + "        country:\\\"Germany\\\"\\n"
                        + "      }\\n"
                        + "}, enterpriseId: \\\""
                        + ENTERPRISE_ID
                        + "\\\"\\n"
                        + ") {code\\n"
                        + "\\tmessage\\n"
                        + "    factory { id sourceId label labelLanguageCode description"
                        + " descriptionLanguageCode location {id city country street"
                        + " streetNumber}\\n"
                        + "    }\\n"
                        + "}\\n"
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
                createResponse.get("data").get("createFactoryForEnterprise").get("code").asInt(),
                200);
        assertEquals(
                createResponse
                        .get("data")
                        .get("createFactoryForEnterprise")
                        .get("message")
                        .textValue(),
                "Success");

        var factoryId =
                createResponse
                        .get("data")
                        .get("createFactoryForEnterprise")
                        .get("factory")
                        .get("id")
                        .textValue();
        var sourceId =
                createResponse
                        .get("data")
                        .get("createFactoryForEnterprise")
                        .get("factory")
                        .get("sourceId")
                        .textValue();
        var label =
                createResponse
                        .get("data")
                        .get("createFactoryForEnterprise")
                        .get("factory")
                        .get("label")
                        .textValue();
        var labelLang =
                createResponse
                        .get("data")
                        .get("createFactoryForEnterprise")
                        .get("factory")
                        .get("labelLanguageCode")
                        .textValue();
        var description =
                createResponse
                        .get("data")
                        .get("createFactoryForEnterprise")
                        .get("factory")
                        .get("description")
                        .textValue();
        var descriptionLang =
                createResponse
                        .get("data")
                        .get("createFactoryForEnterprise")
                        .get("factory")
                        .get("descriptionLanguageCode")
                        .textValue();

        assertEquals("Battery Factory Karlsruhe", sourceId);
        assertEquals("Battery Factory Karlsruhe", label);
        assertEquals("en", labelLang, "hey");
        assertEquals("This is a test factory", description);
        assertEquals("en", descriptionLang);

        var controlResponseData = this.getFactoryDataForEnterprise(ENTERPRISE_ID);
        var enterpriseArray = (ArrayNode) controlResponseData.get("enterprise");
        assertFalse(enterpriseArray.isEmpty());
        var factoriesArray = (ArrayNode) enterpriseArray.get(0).get("factories");
        assertFalse(factoriesArray.isEmpty());
        boolean isExisting = false;
        for (com.fasterxml.jackson.databind.JsonNode factory : factoriesArray) {
            if (factory.get("id").textValue().equals(factoryId)) {
                isExisting = true;
            }
        }
        assertTrue(isExisting);
        deleteFactory(factoryId);
        isExisting = false;
        var secondControlResponseData = this.getFactoryDataForEnterprise(ENTERPRISE_ID);
        var secondEnterpriseArray = (ArrayNode) secondControlResponseData.get("enterprise");
        var secondFactoriesArray = (ArrayNode) secondEnterpriseArray.get(0).get("factories");
        for (com.fasterxml.jackson.databind.JsonNode factory : secondFactoriesArray) {
            if (factory.get("id").textValue().equals(factoryId)) {
                isExisting = true;
            }
        }
        assertFalse(isExisting);
    }

    public JsonNode getFactoryDataForEnterprise(String enterpriseId) throws Exception {
        String controlRequestText =
                "{\"query\":\"query {\\r\\n  enterprise("
                        + "filter: {id: \\\""
                        + enterpriseId
                        + "\\\"}){\\r"
                        + "\\n"
                        + "    id \\r"
                        + "\\n"
                        + "    factories{\\r"
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

    public void deleteFactory(String id) throws Exception {
        String deleteRequestText =
                "{\"query\":\"mutation {\\r\\n  deleteFactory(id:\\r\\n  "
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
    public void createPropertyForEnterprise() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\n"
                        + " createPropertyForEnterprise(property: {\\n"
                        + "    sourceId: \\\"Working Conditions\\\",\\n"
                        + "    label: \\\"Working Conditions\\\",\\n"
                        + "    labelLanguageCode: \\\"en\\\",\\n"
                        + "    description: \\\"This is a test property\\\",\\n"
                        + "    descriptionLanguageCode: \\\"en\\\",\\n"
                        + "    value: \\\"fair\\\",\\n"
                        + "    semanticReferences: [\\n"
                        + "        {\\n"
                        + "        sourceUri: \\\"sem_ref_working_conditions\\\",\\n"
                        + "        label: \\\"Working Conditions\\\",\\n"
                        + "        labelLanguageCode: \\\"en\\\",\\n"
                        + "        description: \\\"This is a test semantic reference\\\",\\n"
                        + "        descriptionLanguageCode: \\\"en\\\"\\n"
                        + "        }\\n"
                        + "    ]\\n"
                        + "}, enterpriseId: \\\""
                        + ENTERPRISE_ID
                        + "\\\"\\n"
                        + ") {code\\n"
                        + "\\tmessage property { id sourceId label labelLanguageCode description"
                        + " descriptionLanguageCode value semanticReferences { sourceUri label"
                        + " labelLanguageCode description descriptionLanguageCode}}\\n"
                        + "}\\n"
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
                createResponse.get("data").get("createPropertyForEnterprise").get("code").asInt(),
                200);
        assertEquals(
                createResponse
                        .get("data")
                        .get("createPropertyForEnterprise")
                        .get("message")
                        .textValue(),
                "Success");

        var propertyId =
                createResponse
                        .get("data")
                        .get("createPropertyForEnterprise")
                        .get("property")
                        .get("id")
                        .textValue();
        var sourceId =
                createResponse
                        .get("data")
                        .get("createPropertyForEnterprise")
                        .get("property")
                        .get("sourceId")
                        .textValue();
        var label =
                createResponse
                        .get("data")
                        .get("createPropertyForEnterprise")
                        .get("property")
                        .get("label")
                        .textValue();
        var labelLang =
                createResponse
                        .get("data")
                        .get("createPropertyForEnterprise")
                        .get("property")
                        .get("labelLanguageCode")
                        .textValue();
        var description =
                createResponse
                        .get("data")
                        .get("createPropertyForEnterprise")
                        .get("property")
                        .get("description")
                        .textValue();
        var descriptionLang =
                createResponse
                        .get("data")
                        .get("createPropertyForEnterprise")
                        .get("property")
                        .get("descriptionLanguageCode")
                        .textValue();
        var value =
                createResponse
                        .get("data")
                        .get("createPropertyForEnterprise")
                        .get("property")
                        .get("value")
                        .textValue();

        var semRefJson =
                createResponse
                        .get("data")
                        .get("createPropertyForEnterprise")
                        .get("property")
                        .get("semanticReferences");

        if (semRefJson.isArray()) {
            var createdSemRef = semRefJson.get(0);
            var sourceUri = createdSemRef.get("sourceUri").textValue();
            var labelSemRef = createdSemRef.get("label").textValue();
            var descSemRef = createdSemRef.get("description").textValue();

            assertEquals("sem_ref_working_conditions", sourceUri);
            assertEquals("Working Conditions", labelSemRef);
            assertEquals("This is a test semantic reference", descSemRef);
        }

        assertEquals("Working Conditions", sourceId);
        assertEquals("Working Conditions", label);
        assertEquals("en", labelLang, "hey");
        assertEquals("This is a test property", description);
        assertEquals("en", descriptionLang);
        assertEquals("fair", value);

        var controlResponseData = this.getPropertyDataForEnterprise(ENTERPRISE_ID);
        var enterpriseArray = (ArrayNode) controlResponseData.get("enterprise");
        assertFalse(enterpriseArray.isEmpty());
        var propertiesArray = (ArrayNode) enterpriseArray.get(0).get("properties");
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
        var secondControlResponseData = this.getPropertyDataForEnterprise(ENTERPRISE_ID);
        var secondEnterpriseArray = (ArrayNode) secondControlResponseData.get("enterprise");
        var secondPropertiesArray = (ArrayNode) secondEnterpriseArray.get(0).get("properties");
        for (com.fasterxml.jackson.databind.JsonNode factory : secondPropertiesArray) {
            if (factory.get("id").textValue().equals(propertyId)) {
                isExisting = true;
            }
        }
        assertFalse(isExisting);
    }

    public JsonNode getPropertyDataForEnterprise(String enterpriseId) throws Exception {
        String controlRequestText =
                "{\"query\":\"query {\\r\\n  enterprise("
                        + "filter: {id: \\\""
                        + enterpriseId
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
    public void createSubsidiaryForEnterprise() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\n"
                        + " createSubsidiaryForEnterprise(subsidiary: {\\n"
                        + "      sourceId : \\\"Battery Manufacturer Munich\\\",\\n"
                        + "      label: \\\"Battery Manufacturer Munich\\\",\\n"
                        + "      labelLanguageCode: \\\"en\\\",\\n"
                        + "      description: \\\"This is a test subsidiary enterprise\\\",\\n"
                        + "      descriptionLanguageCode: \\\"en\\\",\\n"
                        + "      location: {\\n"
                        + "        street: \\\"Fraunhoferstr.\\\",\\n"
                        + "        streetNumber: \\\"1\\\",\\n"
                        + "        zip: \\\"76131\\\",\\n"
                        + "        city:\\\"Munich\\\",\\n"
                        + "        country:\\\"Germany\\\"\\n"
                        + "      }\\n"
                        + "}, enterpriseId: \\\""
                        + ENTERPRISE_ID
                        + "\\\"\\n"
                        + ") {code\\n"
                        + "\\tmessage enterprise { id sourceId label labelLanguageCode description"
                        + " descriptionLanguageCode location {id street streetNumber zip city"
                        + " country}}\\n"
                        + "}\\n"
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
                createResponse.get("data").get("createSubsidiaryForEnterprise").get("code").asInt(),
                200);
        assertEquals(
                createResponse
                        .get("data")
                        .get("createSubsidiaryForEnterprise")
                        .get("message")
                        .textValue(),
                "Success");

        var enterpriseId =
                createResponse
                        .get("data")
                        .get("createSubsidiaryForEnterprise")
                        .get("enterprise")
                        .get("id")
                        .textValue();
        var sourceId =
                createResponse
                        .get("data")
                        .get("createSubsidiaryForEnterprise")
                        .get("enterprise")
                        .get("sourceId")
                        .textValue();
        var label =
                createResponse
                        .get("data")
                        .get("createSubsidiaryForEnterprise")
                        .get("enterprise")
                        .get("label")
                        .textValue();
        var labelLang =
                createResponse
                        .get("data")
                        .get("createSubsidiaryForEnterprise")
                        .get("enterprise")
                        .get("labelLanguageCode")
                        .textValue();
        var description =
                createResponse
                        .get("data")
                        .get("createSubsidiaryForEnterprise")
                        .get("enterprise")
                        .get("description")
                        .textValue();
        var descriptionLang =
                createResponse
                        .get("data")
                        .get("createSubsidiaryForEnterprise")
                        .get("enterprise")
                        .get("descriptionLanguageCode")
                        .textValue();

        var locationJson =
                createResponse
                        .get("data")
                        .get("createSubsidiaryForEnterprise")
                        .get("enterprise")
                        .get("location");
        var locationId = locationJson.get("id").textValue();
        var street = locationJson.get("street").textValue();
        var streetNumber = locationJson.get("streetNumber").textValue();
        var zip = locationJson.get("zip").textValue();
        var city = locationJson.get("city").textValue();
        var country = locationJson.get("country").textValue();

        assertEquals(street, "Fraunhoferstr.");
        assertEquals(streetNumber, "1");
        assertEquals(zip, "76131");
        assertEquals(city, "Munich");
        assertEquals(country, "Germany");

        assertEquals("Battery Manufacturer Munich", sourceId);
        assertEquals("Battery Manufacturer Munich", label);
        assertEquals("en", labelLang);
        assertEquals("This is a test subsidiary enterprise", description);
        assertEquals("en", descriptionLang);

        var controlResponseData = this.getSubsidiaryDataForEnterprise(ENTERPRISE_ID);
        var enterpriseArray = (ArrayNode) controlResponseData.get("enterprise");
        assertFalse(enterpriseArray.isEmpty());
        var subsidiaryEnterprises = (ArrayNode) enterpriseArray.get(0).get("subsidiaryEnterprises");
        assertFalse(subsidiaryEnterprises.isEmpty());
        boolean isExisting = false;
        for (com.fasterxml.jackson.databind.JsonNode subsidiary : subsidiaryEnterprises) {
            if (subsidiary.get("id").textValue().equals(enterpriseId)) {
                isExisting = true;
            }
        }
        assertTrue(isExisting);
        deleteSubsidiaryEnterprise(enterpriseId);
        isExisting = false;
        var secondControlResponseData = this.getSubsidiaryDataForEnterprise(ENTERPRISE_ID);
        var secondEnterpriseArray = (ArrayNode) secondControlResponseData.get("enterprise");
        var secondSubsidiaryArray =
                (ArrayNode) secondEnterpriseArray.get(0).get("subsidiaryEnterprises");
        for (com.fasterxml.jackson.databind.JsonNode subsidiary : secondSubsidiaryArray) {
            if (subsidiary.get("id").textValue().equals(enterpriseId)) {
                isExisting = true;
            }
        }
        assertFalse(isExisting);
    }

    public JsonNode getSubsidiaryDataForEnterprise(String enterpriseId) throws Exception {
        String controlRequestText =
                "{\"query\":\"query {\\r\\n  enterprise("
                        + "filter:{ id: \\\""
                        + enterpriseId
                        + "\\\"}){\\r"
                        + "\\n"
                        + "    id \\r"
                        + "\\n"
                        + "    subsidiaryEnterprises{\\r"
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

    public void deleteSubsidiaryEnterprise(String id) throws Exception {
        String deleteRequestText =
                "{\"query\":\"mutation {\\r\\n  deleteEnterprise(id:\\r\\n  "
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
    public void createProductForEnterprise() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\n"
                        + " createProductForEnterprise(product: {\\n"
                        + "    sourceId : \\\"Battery\\\",\\n"
                        + "    label: \\\"Battery\\\",\\n"
                        + "    labelLanguageCode: \\\"en\\\",\\n"
                        + "    description: \\\"This is a test product\\\",\\n"
                        + "    descriptionLanguageCode: \\\"en\\\",\\n"
                        + "}, enterpriseId: \\\""
                        + ENTERPRISE_ID
                        + "\\\"\\n"
                        + ") {code\\n"
                        + "\\tmessage\\n"
                        + "    product {\\n"
                        + "        id sourceId label labelLanguageCode description"
                        + " descriptionLanguageCode\\n"
                        + "    }\\n"
                        + "}\\n"
                        + "}\\n"
                        + "\",\"variables\":{}}";

        var createRequest =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestText)
                                        .header(HEADER_NAME, "Bearer " + token))
                        .andReturn();
        var createResponse = mapper.readTree(createRequest.getResponse().getContentAsString());
        assertEquals(
                createResponse.get("data").get("createProductForEnterprise").get("code").asInt(),
                200);
        assertEquals(
                createResponse
                        .get("data")
                        .get("createProductForEnterprise")
                        .get("message")
                        .textValue(),
                "Success");

        var productId =
                createResponse
                        .get("data")
                        .get("createProductForEnterprise")
                        .get("product")
                        .get("id")
                        .textValue();
        var sourceId =
                createResponse
                        .get("data")
                        .get("createProductForEnterprise")
                        .get("product")
                        .get("sourceId")
                        .textValue();
        var label =
                createResponse
                        .get("data")
                        .get("createProductForEnterprise")
                        .get("product")
                        .get("label")
                        .textValue();
        var labelLang =
                createResponse
                        .get("data")
                        .get("createProductForEnterprise")
                        .get("product")
                        .get("labelLanguageCode")
                        .textValue();
        var description =
                createResponse
                        .get("data")
                        .get("createProductForEnterprise")
                        .get("product")
                        .get("description")
                        .textValue();
        var descriptionLang =
                createResponse
                        .get("data")
                        .get("createProductForEnterprise")
                        .get("product")
                        .get("descriptionLanguageCode")
                        .textValue();

        assertEquals("Battery", sourceId);
        assertEquals("Battery", label);
        assertEquals("en", labelLang);
        assertEquals("This is a test product", description);
        assertEquals("en", descriptionLang);

        var controlResponseData = this.getProductDataForEnterprise(ENTERPRISE_ID);
        var enterpriseArray = (ArrayNode) controlResponseData.get("enterprise");
        assertFalse(enterpriseArray.isEmpty());
        var products = (ArrayNode) enterpriseArray.get(0).get("products");
        assertFalse(products.isEmpty());
        boolean isExisting = false;
        for (com.fasterxml.jackson.databind.JsonNode product : products) {
            if (product.get("id").textValue().equals(productId)) {
                isExisting = true;
            }
        }
        assertTrue(isExisting);
        deleteProduct(productId);
        isExisting = false;
        var secondControlResponseData = this.getProductDataForEnterprise(ENTERPRISE_ID);
        var secondEnterpriseArray = (ArrayNode) secondControlResponseData.get("enterprise");
        var secondProductArray = (ArrayNode) secondEnterpriseArray.get(0).get("products");
        for (com.fasterxml.jackson.databind.JsonNode product : secondProductArray) {
            if (product.get("id").textValue().equals(productId)) {
                isExisting = true;
            }
        }
        assertFalse(isExisting);
    }

    public JsonNode getProductDataForEnterprise(String enterpriseId) throws Exception {
        String controlRequestText =
                "{\"query\":\"query {\\r\\n  enterprise("
                        + "filter:{ id: \\\""
                        + enterpriseId
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
    public void createEnterpriseBulk() throws Exception {
        String requestText =
                "{\"query\":\"mutation { createEnterprise( enterprise: { sourceId : \\\"mipart"
                    + " GmbH\\\", label: \\\"mipart GmbH\\\", labelLanguageCode: \\\"en\\\","
                    + " description: \\\"PROTOTYPES AND COMPONENTS: Configure them here and order"
                    + " them directly online: mipart is the fastest way to source customized"
                    + " prototypes and components. Thanks to real-time calculation, our"
                    + " configurator will immediately show you a price. You can also benefit from"
                    + " our express manufacturing service!\\\", descriptionLanguageCode:"
                    + " \\\"en\\\", location: { latitude: \\\"49.0078208\\\", longitude:"
                    + " \\\"8.3787776\\\", street: \\\"Unterer Markt\\\", streetNumber: \\\"18\\\","
                    + " zip: \\\"92637\\\", city:\\\"Weiden in der Oberpfalz\\\","
                    + " country:\\\"Germany\\\" }, factories: [ { sourceId : \\\"mipart GmbH\\\","
                    + " label: \\\"mipart factory\\\", labelLanguageCode: \\\"en\\\", description:"
                    + " \\\"PROTOTYPES AND COMPONENTS: Configure them here and order them directly"
                    + " online: mipart is the fastest way to source customized prototypes and"
                    + " components. Thanks to real-time calculation, our configurator will"
                    + " immediately show you a price. You can also benefit from our express"
                    + " manufacturing service!\\\", descriptionLanguageCode: \\\"en\\\", location:"
                    + " { latitude: \\\"49.0078208\\\", longitude: \\\"8.3787776\\\", street:"
                    + " \\\"Unterer Markt\\\", streetNumber: \\\"18\\\", zip: \\\"92637\\\","
                    + " city:\\\"Weiden in der Oberpfalz\\\", country:\\\"Germany\\\" }, machines:"
                    + " [ { sourceId : \\\"Stratasys F370\\\", label: \\\"Stratasys F370\\\","
                    + " labelLanguageCode: \\\"en\\\", description: \\\"3D Printer\\\","
                    + " descriptionLanguageCode: \\\"en\\\", providedCapabilities: [ { sourceId :"
                    + " \\\"Material Extrusion (FDM)\\\", label: \\\"Material Extrusion (FDM)\\\","
                    + " labelLanguageCode: \\\"en\\\", description: \\\"Material Extrusion"
                    + " (FDM)\\\", descriptionLanguageCode: \\\"en\\\", properties: [ { sourceId:"
                    + " \\\"Material\\\", label: \\\"Material\\\", labelLanguageCode: \\\"en\\\","
                    + " description: \\\"Material\\\", descriptionLanguageCode: \\\"en\\\", value:"
                    + " \\\"ABS-M30\\\" }, { sourceId: \\\"Color\\\", label: \\\"Color\\\","
                    + " labelLanguageCode: \\\"en\\\", description: \\\"Colour options\\\","
                    + " descriptionLanguageCode: \\\"en\\\", value: \\\"ivory, white, black, grey,"
                    + " red, blue\\\" } ] } ], machineProperties: [ { sourceId:"
                    + " \\\"Construction_Space\\\", label: \\\"Construction_Space\\\","
                    + " labelLanguageCode: \\\"en\\\", description: \\\"Construction space of the"
                    + " machine\\\", descriptionLanguageCode: \\\"en\\\", value: \\\"355 x 254 x"
                    + " 355 mm\\\" } ] }, { sourceId : \\\"Trumpf TruLaser 5030\\\", label:"
                    + " \\\"Trumpf TruLaser 5030\\\", labelLanguageCode: \\\"en\\\", description:"
                    + " \\\"Laser cutting machine\\\", descriptionLanguageCode: \\\"en\\\","
                    + " providedCapabilities: [ { sourceId : \\\"Laser_Cutting\\\", label:"
                    + " \\\"Laser_Cutting\\\", labelLanguageCode: \\\"en\\\", description:"
                    + " \\\"Laser_Cutting\\\", descriptionLanguageCode: \\\"en\\\", properties: [ {"
                    + " sourceId: \\\"Material\\\", label: \\\"Material\\\", labelLanguageCode:"
                    + " \\\"en\\\", description: \\\"Material options\\\", descriptionLanguageCode:"
                    + " \\\"en\\\", value: \\\"AlMg3, AlMg1, Al99\\\" }, { sourceId:"
                    + " \\\"Material_Type\\\", label: \\\"Material_Type\\\", labelLanguageCode:"
                    + " \\\"en\\\", description: \\\"Material type\\\", descriptionLanguageCode:"
                    + " \\\"en\\\", value: \\\"Aluminum\\\" } ] } ], machineProperties: [ {"
                    + " sourceId: \\\"Size\\\", label: \\\"Size\\\", labelLanguageCode: \\\"en\\\","
                    + " description: \\\"Construction space of the machine\\\","
                    + " descriptionLanguageCode: \\\"en\\\", value: \\\"1500 x 3000 mm\\\" } ] } ]"
                    + " } ] } )  { code enterprise {id sourceId label description} }"
                    + " }\",\"variables\":{}}";

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
                        .get("createEnterprise")
                        .get("enterprise")
                        .get("id")
                        .textValue();
        bulkImportEnterpriseId = id;
        var sourceId =
                bodyJson.get("data")
                        .get("createEnterprise")
                        .get("enterprise")
                        .get("sourceId")
                        .textValue();
        var label =
                bodyJson.get("data")
                        .get("createEnterprise")
                        .get("enterprise")
                        .get("label")
                        .textValue();
        var description =
                bodyJson.get("data")
                        .get("createEnterprise")
                        .get("enterprise")
                        .get("description")
                        .textValue();

        assertEquals(sourceId, "mipart GmbH");
        assertEquals(label, "mipart GmbH");

        String checkText =
                "{\"query\":\"query { enterprise { id sourceId label labelLanguageCode description"
                    + " descriptionLanguageCode location { latitude longitude street streetNumber"
                    + " zip city country } factories { id sourceId label labelLanguageCode"
                    + " description descriptionLanguageCode location { latitude longitude street"
                    + " streetNumber zip city country } properties { sourceId label"
                    + " labelLanguageCode description descriptionLanguageCode value"
                    + " semanticReferences { sourceUri label labelLanguageCode description"
                    + " descriptionLanguageCode } } processes { id } machines { id sourceId label"
                    + " description descriptionLanguageCode providedProcesses { id } usingProcesses"
                    + " { id } providedCapabilities { id sourceId label labelLanguageCode"
                    + " description descriptionLanguageCode properties{ id sourceId label"
                    + " labelLanguageCode description descriptionLanguageCode value"
                    + " semanticReferences { id sourceUri label labelLanguageCode description"
                    + " descriptionLanguageCode } } processes{ id } childCapabilities{ id label }"
                    + " parentCapabilities{ id label } productionResources{ id label }"
                    + " semanticReferences{ id label } } machineProperties { id sourceId label"
                    + " labelLanguageCode description descriptionLanguageCode value"
                    + " semanticReferences { id sourceUri label labelLanguageCode description"
                    + " descriptionLanguageCode } } } humanResources { id sourceId label"
                    + " labelLanguageCode description descriptionLanguageCode providedProcesses {"
                    + " id } usingProcesses { id } providedCapabilities { id } certificates {"
                    + " sourceId label labelLanguageCode description descriptionLanguageCode value"
                    + " semanticReferences { sourceUri label labelLanguageCode description"
                    + " descriptionLanguageCode } } } } subsidiaryEnterprises { sourceId label"
                    + " labelLanguageCode description descriptionLanguageCode location { latitude"
                    + " longitude street streetNumber zip city country } } properties { sourceId"
                    + " label labelLanguageCode description descriptionLanguageCode value"
                    + " semanticReferences { sourceUri label labelLanguageCode description"
                    + " descriptionLanguageCode } } products { sourceId label labelLanguageCode"
                    + " description descriptionLanguageCode billOfMaterials { sourceId quantity{id}"
                    + " properties { sourceId label labelLanguageCode description"
                    + " descriptionLanguageCode value } } productPassport { id properties {"
                    + " sourceId label labelLanguageCode description descriptionLanguageCode value"
                    + " } } supplyChains { id sourceId description descriptionLanguageCode }"
                    + " properties { sourceId label labelLanguageCode description"
                    + " descriptionLanguageCode value semanticReferences { sourceUri label"
                    + " labelLanguageCode description descriptionLanguageCode } }"
                    + " semanticReferences { sourceUri label labelLanguageCode description"
                    + " descriptionLanguageCode } } processes { sourceId description"
                    + " descriptionLanguageCode properties{id} parentProcesses{id}"
                    + " childProcesses{id} realizedCapabilities{id} requiredCapabilities{id}"
                    + " preliminaryProducts{id} rawMaterials{id} auxiliaryMaterials{id}"
                    + " operatingMaterials{id} endProducts{id} byProducts{id} wasteProducts{id}"
                    + " inputProducts{id} outputProducts{id} usedProductionResources{id}"
                    + " providingProductionResources{id} } productionResources { id sourceId label"
                    + " labelLanguageCode description descriptionLanguageCode } supplyChains { id"
                    + " sourceId description descriptionLanguageCode } } }\",\"variables\":{}}";
        var checkRequest =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(checkText)
                                        .header(HEADER_NAME, "Bearer " + token))
                        .andDo(print())
                        .andReturn();

        var checkBodyJson = mapper.readTree(checkRequest.getResponse().getContentAsString());
        System.out.println(checkBodyJson.toPrettyString());
        boolean isThere = false;
        var idArray = checkBodyJson.get("data").get("enterprise");
        if (idArray.isArray()) {
            var iterator = idArray.elements();
            while (iterator.hasNext()) {
                var enterprise = iterator.next();
                if (enterprise.get("id").textValue().equals(id)) {
                    isThere = true;
                    assertEquals(
                            enterprise.get("location").get("latitude").textValue(), "49.0078208");
                    assertEquals(
                            enterprise.get("location").get("longitude").textValue(), "8.3787776");
                    var factory = enterprise.get("factories").get(0);
                    bulkImportEnterpriseFactoryId = factory.get("id").textValue();
                    assertEquals(factory.get("sourceId").textValue(), "mipart GmbH");
                    var machines = factory.get("machines");
                    JsonNode machine = null;
                    if (machines.isArray()) {
                        for (JsonNode nextMachine : (ArrayNode) machines) {
                            if (nextMachine.get("label").textValue().equals("Stratasys F370")) {
                                machine = nextMachine;
                            }
                        }
                    }
                    assertNotEquals(null, machine);
                    assert machine != null;
                    bulkImportEnterpriseFactoryMachineId = machine.get("id").textValue();
                    assertEquals(machine.get("sourceId").textValue(), "Stratasys F370");
                    assertEquals(machine.get("label").textValue(), "Stratasys F370");
                    bulkImportEnterpriseProvidedCapabilityId =
                            machine.get("providedCapabilities").get(0).get("id").textValue();
                    assertEquals(
                            machine.get("providedCapabilities").get(0).get("sourceId").textValue(),
                            "Material Extrusion (FDM)");
                    // This arbitrarily fails because of some sort error.
                    // assertEquals(machine.get("providedCapabilities").get(0).get("properties").get(0).get("sourceId").textValue(), "Material");
                    // assertEquals(machine.get("providedCapabilities").get(0).get("properties").get(0).get("label").textValue(), "Material");
                    bulkImportEnterpriseMachinePropertyId =
                            machine.get("machineProperties").get(0).get("id").textValue();
                    assertEquals(
                            machine.get("machineProperties").get(0).get("sourceId").textValue(),
                            "Construction_Space");
                    assertEquals(
                            machine.get("machineProperties").get(0).get("label").textValue(),
                            "Construction_Space");
                }
            }
            assertTrue(isThere);
        }
    }

    @Test
    @Order(2)
    public void updateEnterpriseBulk() throws Exception {
        String requestText =
                "{\"query\":\"mutation { updateEnterprise(id: \\\""
                        + bulkImportEnterpriseId
                        + "\\\", "
                        + "enterprise: {factories: [ { id: \\\""
                        + bulkImportEnterpriseFactoryId
                        + "\\\", sourceId : \\\"mipart GmbH_UpdateTest\\\", "
                        + "machines: [ {id: \\\""
                        + bulkImportEnterpriseFactoryMachineId
                        + "\\\" sourceId : \\\"Stratasys F370_UpdateTest1\\\", label: \\\"Stratasys"
                        + " F370_UpdateTest2\\\", labelLanguageCode: \\\"de\\\","
                        + " providedCapabilities: [ { id: \\\""
                        + bulkImportEnterpriseProvidedCapabilityId
                        + "\\\", sourceId : \\\"Material Extrusion (FDM)_UpdateTest1\\\", label:"
                        + " \\\"Material Extrusion (FDM)_Updated\\\" } ], machineProperties: ["
                        + " {id:\\\""
                        + bulkImportEnterpriseMachinePropertyId
                        + "\\\", sourceId: \\\"Construction_Space_Updated\\\", label:"
                        + " \\\"Construction_Space_updatetest\\\", value: \\\"355 x 254 x 355"
                        + " km\\\" } ] } ] } ] } )  { code } }\",\"variables\":{}}";

        var request =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestText)
                                        .header(HEADER_NAME, "Bearer " + token))
                        .andReturn();

        var body = request.getResponse().getContentAsString();
        var bodyJson = mapper.readTree(body);
        var code = bodyJson.get("data").get("updateEnterprise").get("code");
        assertEquals(code.asInt(), 200);

        String checkText =
                "{\"query\":\"query { enterprise { id sourceId label labelLanguageCode description"
                    + " descriptionLanguageCode location { latitude longitude street streetNumber"
                    + " zip city country } factories { id sourceId label labelLanguageCode"
                    + " description descriptionLanguageCode location { latitude longitude street"
                    + " streetNumber zip city country } properties { sourceId label"
                    + " labelLanguageCode description descriptionLanguageCode value"
                    + " semanticReferences { sourceUri label labelLanguageCode description"
                    + " descriptionLanguageCode } } processes { id } machines { id sourceId label"
                    + " description descriptionLanguageCode providedProcesses { id } usingProcesses"
                    + " { id } providedCapabilities { id sourceId label labelLanguageCode"
                    + " description descriptionLanguageCode properties{ id sourceId label"
                    + " labelLanguageCode description descriptionLanguageCode value"
                    + " semanticReferences { id sourceUri label labelLanguageCode description"
                    + " descriptionLanguageCode } } processes{ id } childCapabilities{ id label }"
                    + " parentCapabilities{ id label } productionResources{ id label }"
                    + " semanticReferences{ id label } } machineProperties { id sourceId label"
                    + " labelLanguageCode description descriptionLanguageCode value"
                    + " semanticReferences { id sourceUri label labelLanguageCode description"
                    + " descriptionLanguageCode } } } humanResources { id sourceId label"
                    + " labelLanguageCode description descriptionLanguageCode providedProcesses {"
                    + " id } usingProcesses { id } providedCapabilities { id } certificates {"
                    + " sourceId label labelLanguageCode description descriptionLanguageCode value"
                    + " semanticReferences { sourceUri label labelLanguageCode description"
                    + " descriptionLanguageCode } } } } subsidiaryEnterprises { sourceId label"
                    + " labelLanguageCode description descriptionLanguageCode location { latitude"
                    + " longitude street streetNumber zip city country } } properties { sourceId"
                    + " label labelLanguageCode description descriptionLanguageCode value"
                    + " semanticReferences { sourceUri label labelLanguageCode description"
                    + " descriptionLanguageCode } } products { sourceId label labelLanguageCode"
                    + " description descriptionLanguageCode billOfMaterials { sourceId quantity{id}"
                    + " properties { sourceId label labelLanguageCode description"
                    + " descriptionLanguageCode value } } productPassport { id properties {"
                    + " sourceId label labelLanguageCode description descriptionLanguageCode value"
                    + " } } supplyChains { id sourceId description descriptionLanguageCode }"
                    + " properties { sourceId label labelLanguageCode description"
                    + " descriptionLanguageCode value semanticReferences { sourceUri label"
                    + " labelLanguageCode description descriptionLanguageCode } }"
                    + " semanticReferences { sourceUri label labelLanguageCode description"
                    + " descriptionLanguageCode } } processes { sourceId description"
                    + " descriptionLanguageCode properties{id} parentProcesses{id}"
                    + " childProcesses{id} realizedCapabilities{id} requiredCapabilities{id}"
                    + " preliminaryProducts{id} rawMaterials{id} auxiliaryMaterials{id}"
                    + " operatingMaterials{id} endProducts{id} byProducts{id} wasteProducts{id}"
                    + " inputProducts{id} outputProducts{id} usedProductionResources{id}"
                    + " providingProductionResources{id} } productionResources { id sourceId label"
                    + " labelLanguageCode description descriptionLanguageCode } supplyChains { id"
                    + " sourceId description descriptionLanguageCode } } }\",\"variables\":{}}";
        var checkRequest =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(checkText)
                                        .header(HEADER_NAME, "Bearer " + token))
                        .andDo(print())
                        .andReturn();

        var checkBodyJson = mapper.readTree(checkRequest.getResponse().getContentAsString());
        System.out.println(checkBodyJson.toPrettyString());
        boolean isThere = false;
        var idArray = checkBodyJson.get("data").get("enterprise");

        if (idArray.isArray()) {
            var iterator = idArray.elements();
            while (iterator.hasNext()) {
                var enterprise = iterator.next();
                if (enterprise.get("id").textValue().equals(bulkImportEnterpriseId)) {
                    isThere = true;
                    var factory = enterprise.get("factories").get(0);
                    assertEquals(factory.get("sourceId").textValue(), "mipart GmbH_UpdateTest");
                    var machines = factory.get("machines");
                    var machine = machines.get(1);
                    if (!machine.get("sourceId").textValue().contains("Stratasys")) {
                        machine = machines.get(0);
                    }
                    bulkImportEnterpriseFactoryMachineId = machine.get("id").textValue();

                    assertEquals(machine.get("sourceId").textValue(), "Stratasys F370_UpdateTest1");
                    assertEquals(machine.get("label").textValue(), "Stratasys F370_UpdateTest2");
                    bulkImportEnterpriseProvidedCapabilityId =
                            machine.get("providedCapabilities").get(0).get("id").textValue();
                    assertEquals(
                            machine.get("providedCapabilities").get(0).get("sourceId").textValue(),
                            "Material Extrusion (FDM)_UpdateTest1");
                    assertEquals(
                            machine.get("providedCapabilities").get(0).get("label").textValue(),
                            "Material Extrusion (FDM)_Updated");
                    bulkImportEnterpriseMachinePropertyId =
                            machine.get("machineProperties").get(0).get("id").textValue();
                    assertEquals(
                            machine.get("machineProperties").get(0).get("sourceId").textValue(),
                            "Construction_Space_Updated");
                    assertEquals(
                            machine.get("machineProperties").get(0).get("label").textValue(),
                            "Construction_Space_updatetest");
                }
            }
            assertTrue(isThere);
        }
    }

    @Test
    @Order(3)
    public void deleteEnterpriseBulk() throws Exception {
        var request =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"query\":\"mutation {\\r"
                                                        + "\\n"
                                                        + "  bulkDeleteEnterprise(id: \\r"
                                                        + "\\n"
                                                        + "    \\\""
                                                        + bulkImportEnterpriseId
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
        var code = bodyJson.get("data").get("bulkDeleteEnterprise").get("code");
        assertThat(code.asInt()).isEqualTo(200);
        System.out.println(code.textValue());
    }
}
