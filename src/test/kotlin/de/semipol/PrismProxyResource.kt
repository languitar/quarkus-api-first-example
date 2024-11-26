package de.semipol

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import org.eclipse.microprofile.config.ConfigProvider
import org.testcontainers.Testcontainers
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import org.testcontainers.utility.MountableFile


/**
 * A Quarkus test resource that starts stoplight prism for validating responses from our application against the
 * OpenAPI specification.
 */
class PrismProxyResource : QuarkusTestResourceLifecycleManager {

    override fun start(): MutableMap<String, String> {
        Testcontainers.exposeHostPorts(testPort())
        proxyContainer.start()
        return emptyMap<String, String>().toMutableMap()
    }

    override fun stop() {
        proxyContainer.stop()
    }

    companion object {
        private var proxyContainer: GenericContainer<*> =
            GenericContainer<Nothing>(DockerImageName.parse("stoplight/prism:5")).apply {
                withExposedPorts(4010)
                withCopyFileToContainer(
                    MountableFile.forClasspathResource("META-INF/openapi.yaml"),
                    "/tmp/openapi.yaml"
                )
                withAccessToHost(true)
                withCommand(
                    "proxy",
                    // Violations should result in 500 responses so we can't ignore them in tests
                    "--errors",
                    // We need to accept connections from outside the container
                    "-h",
                    "0.0.0.0",
                    // This is where we have mounted the specification
                    "/tmp/openapi.yaml",
                    // We have exposed the host system with the Quarkus app to the container and will use that as the
                    // upstream to be validated against our specification
                    "http://host.testcontainers.internal:${testPort()}"
                )
                // This makes the output of prism visible on the shell. Prism logs each request. So, it's seomtimes
                // useful to see the output.
                withLogConsumer { println(it.utf8StringWithoutLineEnding) }
            }

        private fun testPort(): Int = ConfigProvider.getConfig().getOptionalValue(
            "quarkus.http.test-port",
            Int::class.java
        ).orElse(8081) // The default as documented by Quarkus

        fun proxyPort(): Int = proxyContainer.firstMappedPort
    }
}