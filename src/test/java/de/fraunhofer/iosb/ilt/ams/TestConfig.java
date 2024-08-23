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
package de.fraunhofer.iosb.ilt.ams;

import de.fraunhofer.iosb.ilt.ams.api.config.JwtSecurityConfiguration;
import de.fraunhofer.iosb.ilt.ams.api.config.KeycloakDataServiceProperties;
import org.eclipse.rdf4j.spring.test.RDF4JTestConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@TestConfiguration
@EnableTransactionManagement
@Import({
    RDF4JTestConfig.class,
    de.fraunhofer.iosb.ilt.ams.api.config.KeycloakDataServiceProperties.class,
    de.fraunhofer.iosb.ilt.ams.api.config.WebSecurityConfiguration.class,
    de.fraunhofer.iosb.ilt.ams.api.config.KeycloakDataServiceConfiguration.class,
    JwtSecurityConfiguration.class
})
@ComponentScan(basePackages = "de.fraunhofer.iosb.ilt.ams")
@EnableConfigurationProperties(KeycloakDataServiceProperties.class)
@ConfigurationProperties(prefix = "springkeycloak.auth")
public class TestConfig {}
