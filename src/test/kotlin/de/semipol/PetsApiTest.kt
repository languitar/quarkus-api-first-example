package de.semipol

import de.semipol.adapters.rest.Error
import de.semipol.adapters.rest.Pet
import de.semipol.domain.PetRepository
import io.quarkus.test.junit.QuarkusTest
import io.restassured.common.mapper.TypeRef
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import jakarta.inject.Inject
import org.hamcrest.CoreMatchers
import org.jboss.resteasy.reactive.RestResponse.StatusCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@QuarkusTest
class PetsApiTest {

    @Inject
    lateinit var petRepository: PetRepository

    @BeforeEach
    fun clearRepo() {
        petRepository.clear()
    }

    @Nested
    inner class CreatePets {

        @Test
        fun `can create new pets`() {
            val pet = Pet(42, "new name", "some tag")
            Given {
                body(pet)
                contentType(ContentType.JSON)
            } When {
                post("/pets")
            } Then {
                statusCode(StatusCode.CREATED)
            }
            assertTrue(petRepository.listPets().contains(pet))
        }

    }

    @Nested
    inner class ListPets {

        @Test
        fun `works without data`() {
            val responseBody = When {
                get("/pets")
            } Then {
                statusCode(StatusCode.OK)
                contentType(ContentType.JSON)
            } Extract {
                body().`as`(petListTypeRef)
            }

            assertTrue(responseBody.isEmpty())
        }

        @Test
        fun `returns existing pets`() {
            val expected = Pet(id=42, name="test pet")
            petRepository.addPet(expected)

            val responseBody = When {
                get("/pets")
            } Then {
                statusCode(StatusCode.OK)
                contentType(ContentType.JSON)
            } Extract {
                body().`as`(petListTypeRef)
            }

            assertEquals(listOf(expected), responseBody)
        }

        @Test
        fun `sets the x-next header`() {
            When {
                get("/pets")
            } Then {
                statusCode(StatusCode.OK)
                header("x-next", CoreMatchers.notNullValue())
            }
        }

    }

    @Nested
    inner class ShowPetById {

        @Test
        fun ` returns 404 for unknown IDs`() {
            val responseBody = When {
                get("pets/42")
            } Then {
                statusCode(StatusCode.NOT_FOUND)
                contentType(ContentType.JSON)
            } Extract {
                body().`as`(Error::class.java)
            }
        }

        @Test
        fun `returns existing pets`() {
            val existing = Pet(16L,"test pet")
            petRepository.addPet(existing)

            val responseBody = When {
                get("pets/${existing.id}")
            } Then {
                statusCode(StatusCode.OK)
                contentType(ContentType.JSON)
            } Extract {
                body().`as`(Pet::class.java)
            }

            assertEquals(existing, responseBody)
        }

    }

    companion object {
        private val petListTypeRef = object : TypeRef<List<Pet>>() {}
    }

}