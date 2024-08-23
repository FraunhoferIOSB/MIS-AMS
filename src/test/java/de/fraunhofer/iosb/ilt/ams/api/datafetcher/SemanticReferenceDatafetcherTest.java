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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.ams.TestConfig;
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
public class SemanticReferenceDatafetcherTest {
    private static final String HEADER_NAME = "Authorization";

    @Value("${test.token}")
    String token;

    @Value("${test.graph}")
    String graph;

    private static String SEMANTIC_REFERENCE_ID = "";

    @Autowired private MockMvc mockMvc;

    private ObjectMapper mapper;

    @BeforeEach
    public void setUp() {
        mapper = new ObjectMapper();
    }

    @Test
    @Order(1)
    public void semanticReference() throws Exception {
        String requestText =
                "{\"query\":\"query {\\r"
                        + "\\n"
                        + "  semanticReference {\\r"
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
    public void createSemanticReference() throws Exception {
        String requestText =
                "{\"query\":\"mutation {\\r"
                        + "\\n"
                        + "  createSemanticReference(semanticReference: {\\r"
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
                        + "  }) {code\\r"
                        + "\\n"
                        + "  message semanticReference { id sourceUri label description}}\\r"
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
                        .get("createSemanticReference")
                        .get("semanticReference")
                        .get("id")
                        .textValue();
        var sourceId =
                bodyJson.get("data")
                        .get("createSemanticReference")
                        .get("semanticReference")
                        .get("sourceUri")
                        .textValue();
        var label =
                bodyJson.get("data")
                        .get("createSemanticReference")
                        .get("semanticReference")
                        .get("label")
                        .textValue();
        var description =
                bodyJson.get("data")
                        .get("createSemanticReference")
                        .get("semanticReference")
                        .get("description")
                        .textValue();
        SEMANTIC_REFERENCE_ID = id;

        assertEquals(sourceId, "sem_ref_working_conditions");
        assertEquals("Working Conditions", label);
        assertEquals(description, "This is a test semantic reference");

        var checkRequest =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"query\":\"query {\\r"
                                                        + "\\n"
                                                        + "  semanticReference {\\r"
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
        var idArray = checkBodyJson.get("data").get("semanticReference");
        if (idArray.isArray()) {
            var iterator = idArray.elements();
            while (iterator.hasNext()) {
                var semanticReference = iterator.next();
                if (semanticReference.get("id").textValue().equals(id)) {
                    isThere = true;
                }
            }
            assertTrue(isThere);
        }

        System.out.println(id);
        System.out.println(bodyJson.toPrettyString());
    }

    @Test
    @Order(1)
    public void updateSemanticReference() throws Exception {
        String queryText =
                "{\"query\":\"query semanticReference{\\r\\n  semanticReference(filter: {id: \\\""
                        + SEMANTIC_REFERENCE_ID
                        + "\\\"}){\\r"
                        + "\\n"
                        + "    id\\r"
                        + "\\n"
                        + "    sourceUri\\r"
                        + "\\n"
                        + "    description\\r"
                        + "\\n"
                        + "    label }\\r"
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
        var semanticReference = queryResponse.get("data").get("semanticReference").get(0);
        var sourceId = semanticReference.get("sourceUri").textValue();
        var label = semanticReference.get("label").textValue();
        var description = semanticReference.get("description").textValue();

        var newSourceId = sourceId + "updated";
        var newLabel = "Updated: " + label;
        var newDescription = "Updated: " + description;

        var updateText =
                "{\"query\":\"mutation {\\r\\n  "
                        + "updateSemanticReference(semanticReferenceId: \\\""
                        + SEMANTIC_REFERENCE_ID
                        + "\\\", semanticReference: {\\r\\n    "
                        + "sourceUri: \\\""
                        + newSourceId
                        + "\\\",\\r\\n    "
                        + "label: \\\""
                        + newLabel
                        + "\\\",\\r\\n    "
                        + "labelLanguageCode: \\\"en\\\",\\r\\n    "
                        + "description: \\\""
                        + newDescription
                        + "\\\",\\r"
                        + "\\n"
                        + "    descriptionLanguageCode: \\\"en\\\"\\r"
                        + "\\n"
                        + "  }) {code\\r"
                        + "\\n"
                        + "  message\\r"
                        + "\\n"
                        + "  semanticReference {id sourceUri label description}}\\r"
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
                        .get("updateSemanticReference")
                        .get("semanticReference")
                        .get("sourceUri")
                        .textValue();
        var updatedLabel =
                updateResponse
                        .get("data")
                        .get("updateSemanticReference")
                        .get("semanticReference")
                        .get("label")
                        .textValue();
        var updatedDescription =
                updateResponse
                        .get("data")
                        .get("updateSemanticReference")
                        .get("semanticReference")
                        .get("description")
                        .textValue();

        assertEquals(newSourceId, updatedSourceId);
        assertEquals(newLabel, updatedLabel);
        assertEquals(newDescription, updatedDescription);
    }

    @Test
    @Order(Integer.MAX_VALUE)
    public void deleteSemanticReference() throws Exception {
        var request =
                mockMvc.perform(
                                post("/graphql")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"query\":\"mutation {\\r"
                                                    + "\\n"
                                                    + "  deleteSemanticReference(semanticReferenceId:"
                                                    + " \\r"
                                                    + "\\n"
                                                    + "    \\\""
                                                        + SEMANTIC_REFERENCE_ID
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
        var message = bodyJson.get("data").get("deleteSemanticReference").get("message");
        assertThat(message.textValue()).isEqualTo("Deleted " + SEMANTIC_REFERENCE_ID);
        System.out.println(message.textValue());
    }
}
