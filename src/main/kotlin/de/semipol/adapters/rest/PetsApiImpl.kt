package de.semipol.adapters.rest

import jakarta.ws.rs.core.Response

class PetsApiImpl : PetsApi {

    override suspend fun createPets(pet: Pet): Response {
        TODO("Not yet implemented")
    }

    override suspend fun listPets(limit: Int?): Response {
        TODO("Not yet implemented")
    }

    override suspend fun showPetById(petId: String): Response {
        TODO("Not yet implemented")
    }
}