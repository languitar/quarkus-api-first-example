package de.semipol

import com.fasterxml.jackson.databind.ObjectMapper
import de.semipol.adapters.rest.ObjectMapperCustomizer
import de.semipol.adapters.rest.Pet
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusIntegrationTest
import io.restassured.RestAssured
import io.restassured.common.mapper.TypeRef
import io.restassured.config.ObjectMapperConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.jboss.resteasy.reactive.RestResponse.StatusCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusIntegrationTest
@QuarkusTestResource(PrismProxyResource::class)
class PetsApiIT {


    @BeforeEach
    fun beforeEach() {
        RestAssured.port = PrismProxyResource.proxyPort()
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
    }

    @Test
    fun `can list created pets`() {
        val pet = Pet(42, "new name", "some tag")
        Given {
            body(pet)
            contentType(ContentType.JSON)
        } When {
            post("/pets")
        } Then {
            statusCode(StatusCode.CREATED)
        }

        val responseBody = When {
            get("/pets")
        } Then {
            statusCode(StatusCode.OK)
            contentType(ContentType.JSON)
        } Extract {
            body().`as`(petListTypeRef)
        }

        assertTrue(responseBody.contains(pet))
    }

    @Test
    fun `can receive created pets`() {
        val pet = Pet(17, "new name")
        Given {
            body(pet)
            contentType(ContentType.JSON)
        } When {
            post("/pets")
        } Then {
            statusCode(StatusCode.CREATED)
        }

        val responseBody = When {
            get("/pets/${pet.id}")
        } Then {
            statusCode(StatusCode.OK)
            contentType(ContentType.JSON)
        } Extract {
            body().`as`(Pet::class.java)
        }

        assertEquals(pet, responseBody)
    }

    companion object {
        private val petListTypeRef = object : TypeRef<List<Pet>>() {}

        @BeforeAll
        @JvmStatic
        fun configureObjectMapper() {
            RestAssured.config = RestAssuredConfig.config()
                .objectMapperConfig(ObjectMapperConfig().jackson2ObjectMapperFactory { cls, charset ->
                    ObjectMapper().apply {
                        findAndRegisterModules()
                        ObjectMapperCustomizer().customize(this)
                    }
                })
        }
    }

}
