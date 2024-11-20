package de.semipol.adapters.rest

import de.semipol.domain.PetRepository
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.Response.Status
import org.jboss.resteasy.reactive.server.ServerExceptionMapper
import java.net.URI

class PetsApiImpl(private val petRepository: PetRepository) : PetsApi {

    @ServerExceptionMapper
    public fun mapNoSuchElementException(e: NoSuchElementException) =
        Response
            .status(Status.NOT_FOUND)
            .entity(Error(Status.NOT_FOUND.statusCode, e.message ?: ""))
            .build()

    override suspend fun createPets(pet: Pet): Response {
        petRepository.addPet(pet)
        return Response.created(URI.create("/pets/${pet.id}")).build()
    }

    override suspend fun listPets(limit: Int?): Response {
        return Response.ok(petRepository.listPets()).header("x-next", "fake value").build()
    }

    override suspend fun showPetById(petId: Long): Response {
        return Response.ok(petRepository.getPet(petId)).build()
    }
}