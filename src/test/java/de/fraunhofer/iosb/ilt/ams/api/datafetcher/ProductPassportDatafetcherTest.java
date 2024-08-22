package de.fraunhofer.iosb.ilt.ams.api.datafetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.*;
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
class ProductPassportDatafetcherTest {
    private static final String HEADER_NAME = "Authorization";

    @Value("${test.token}")
    String token;

    @Value("${test.graph}")
    String graph;

    private static String PRODUCT_PASSPORT_ID = "";

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
    public void productPassport() throws Exception {
        String requestText =
                "{\"query\":\"query {\\r"
                        + "\\n"
                        + "  productPassport {\\r"
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
    public void createProductPassport() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r"
                        + "\\n"
                        + "  createProductPassport(productPassport: {\\r"
                        + "\\n"
                        + "    sourceId: \\\"Product Passport Source Id Test\\\",\\r"
                        + "\\n"
                        + "    identifier: \\\"testing_product_passport\\\"}) {code\\r"
                        + "\\n"
                        + "  message productPassport { id sourceId identifier}}\\r"
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
                        .get("createProductPassport")
                        .get("productPassport")
                        .get("id")
                        .textValue();
        var sourceId =
                bodyJson.get("data")
                        .get("createProductPassport")
                        .get("productPassport")
                        .get("sourceId")
                        .textValue();
        var identifier =
                bodyJson.get("data")
                        .get("createProductPassport")
                        .get("productPassport")
                        .get("identifier")
                        .textValue();
        PRODUCT_PASSPORT_ID = id;

        assertEquals("Product Passport Source Id Test", sourceId);
        assertEquals("testing_product_passport", identifier);

        var checkRequest =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"query\":\"query {\\r"
                                                        + "\\n"
                                                        + "  productPassport {\\r"
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
        var idArray = checkBodyJson.get("data").get("productPassport");
        if (idArray.isArray()) {
            var iterator = idArray.elements();
            while (iterator.hasNext()) {
                var productPassport = iterator.next();
                if (productPassport.get("id").textValue().equals(id)) {
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
    public void updateProductPassport() throws Exception {
        String queryText =
                "{\"query\":\"query productPassport{\\r\\n  productPassport(filter: {id: \\\""
                        + PRODUCT_PASSPORT_ID
                        + "\\\"}){\\r"
                        + "\\n"
                        + "    id\\r"
                        + "\\n"
                        + "    identifier\\r"
                        + "\\n"
                        + "    sourceId\\r"
                        + "\\n"
                        + "    }\\r"
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
        var productPassport = queryResponse.get("data").get("productPassport").get(0);
        var sourceId = productPassport.get("sourceId").textValue();
        var identifier = productPassport.get("identifier").textValue();

        var newSourceId = sourceId + "updated";
        var newIdentifier = "Updated: " + identifier;

        var updateText =
                "{\"query\":\"mutation {\\r\\n  "
                        + "updateProductPassport(productPassportId: \\\""
                        + PRODUCT_PASSPORT_ID
                        + "\\\", productPassport: {\\r\\n    "
                        + "sourceId: \\\""
                        + newSourceId
                        + "\\\",\\r\\n    "
                        + "identifier: \\\""
                        + newIdentifier
                        + "\\\",\\r"
                        + "\\n"
                        + "}) {code\\r"
                        + "\\n"
                        + "  message\\r"
                        + "\\n"
                        + "  productPassport {id sourceId identifier}}\\r"
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
        System.out.println(updateResponse.textValue());
        var updatedSourceId =
                updateResponse
                        .get("data")
                        .get("updateProductPassport")
                        .get("productPassport")
                        .get("sourceId")
                        .textValue();
        var updatedLabel =
                updateResponse
                        .get("data")
                        .get("updateProductPassport")
                        .get("productPassport")
                        .get("identifier")
                        .textValue();

        assertEquals(newSourceId, updatedSourceId);
        assertEquals(newIdentifier, updatedLabel);
    }

    @Test
    @Order(2)
    public void deleteProductPassport() throws Exception {
        String deleteRequestText =
                "{\"query\":\"mutation {\\r\\n    "
                        + "deleteProductPassport(productPassportId: \\\""
                        + PRODUCT_PASSPORT_ID
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
        var message = bodyJson.get("data").get("deleteProductPassport").get("message");
        assertThat(message.textValue()).isEqualTo("Deleted " + PRODUCT_PASSPORT_ID);
        System.out.println(message.textValue());
    }
}
