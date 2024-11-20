package de.semipol.domain

import de.semipol.adapters.rest.Pet
import jakarta.enterprise.context.ApplicationScoped

/**
 * This is a very rough domain layer only needed to get the exemplary tests going. It violates the architectural ideas
 * of a hexagonal architecture by reusing the REST model Pet as a domain entity. It also lacks differentiating between
 * repository port and adapter. Issues of concurrency are also ignored.
 */
@ApplicationScoped
class PetRepository {

    private val pets = mutableListOf<Pet>()

    fun clear() {
        pets.clear()
    }

    fun addPet(pet: Pet) {
        pets.add(pet)
    }

    fun listPets(): List<Pet> = pets

    fun getPet(id: Long): Pet = pets.first { it.id == id }

}
