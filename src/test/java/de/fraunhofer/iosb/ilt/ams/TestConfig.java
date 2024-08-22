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
