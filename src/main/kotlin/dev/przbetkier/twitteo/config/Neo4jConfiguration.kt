package dev.przbetkier.twitteo.config

import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Config
import org.neo4j.driver.Driver
import org.neo4j.driver.Logging
import org.neo4j.driver.internal.DriverFactory
import org.neo4j.driver.internal.cluster.RoutingSettings
import org.neo4j.driver.internal.retry.RetrySettings
import org.neo4j.driver.internal.security.SecurityPlanImpl
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.neo4j.config.AbstractNeo4jConfig
import org.springframework.data.neo4j.core.DatabaseSelectionProvider
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.net.URI
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.MINUTES

@Configuration
@EnableNeo4jRepositories(
    considerNestedRepositories = true,
    basePackages = ["dev.przbetkier.twitteo.domain"]
)
@EnableTransactionManagement
class Neo4jConfiguration(
    private val neo4jConfig: Neo4jConfigProperties
) : AbstractNeo4jConfig() {

    @Bean
    override fun driver(): Driver =
        DriverFactory().newInstance(
            URI(neo4jConfig.uri),
            AuthTokens.basic(neo4jConfig.username, neo4jConfig.password),
            RoutingSettings.DEFAULT,
            RetrySettings.DEFAULT,
            config(),
            SecurityPlanImpl.forAllCertificates(false, null)
        )

    @Bean
    override fun databaseSelectionProvider(): DatabaseSelectionProvider {
        return DatabaseSelectionProvider.createStaticDatabaseSelectionProvider("neo4j")
    }

    @Bean
    override fun neo4jClient(driver: Driver, databaseSelectionProvider: DatabaseSelectionProvider): Neo4jClient {
        return Neo4jClient.create(driver, databaseSelectionProvider)
    }

    private fun config() =
        Config.builder()
            .withConnectionAcquisitionTimeout(neo4jConfig.client.connectionAcquisitionTimeout, MILLISECONDS)
            .withConnectionTimeout(neo4jConfig.client.connectionTimeout, MILLISECONDS)
            .withLogging(Logging.slf4j())
            .withMaxConnectionPoolSize(neo4jConfig.client.connectionPoolSize)
            .withMaxTransactionRetryTime(neo4jConfig.client.maxTransactionRetryTimeMs, MILLISECONDS)
            .withMaxConnectionLifetime(neo4jConfig.client.maxConnectionLifetimeMinutes, MINUTES)
            .withDriverMetrics()
            .build()
}

@ConfigurationProperties(prefix = "neo4j")
@ConstructorBinding
data class Neo4jConfigProperties(
    val uri: String,
    val username: String,
    val password: String,
    val client: Neo4jClientProperties
)

data class Neo4jClientProperties(
    val connectionTimeout: Long,
    val connectionAcquisitionTimeout: Long,
    val connectionPoolSize: Int,
    val maxTransactionRetryTimeMs: Long,
    val maxConnectionLifetimeMinutes: Long
)
