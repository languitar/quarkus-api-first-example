package de.semipol

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import io.restassured.RestAssured
import org.eclipse.microprofile.config.ConfigProvider
import org.testcontainers.Testcontainers
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import org.testcontainers.utility.MountableFile


class PrismProxyResource : QuarkusTestResourceLifecycleManager {
    override fun start(): MutableMap<String, String> {
        Testcontainers.exposeHostPorts(testPort())
        proxyContainer.start()
        RestAssured.port = 42
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
                    "--errors",
                    "-h",
                    "0.0.0.0",
                    "/tmp/openapi.yaml",
                    "http://host.testcontainers.internal:${testPort()}"
                )
                withLogConsumer { println(it.utf8StringWithoutLineEnding) }
            }

        private fun testPort(): Int = ConfigProvider.getConfig().getOptionalValue(
            "quarkus.http.test-port",
            Int::class.java
        ).orElse(8081)

        fun proxyPort() = proxyContainer.firstMappedPort
    }
}