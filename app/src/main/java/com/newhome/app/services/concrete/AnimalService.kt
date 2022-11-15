package com.newhome.app.services.concrete

import android.graphics.Bitmap
import com.newhome.app.dao.IAnimalProvider
import com.newhome.app.dao.IUsuarioProvider
import com.newhome.app.dto.Animal
import com.newhome.app.dto.AnimalAsync
import com.newhome.app.dto.UsuarioAsync
import com.newhome.app.services.IAnimalService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class AnimalService(private val animalProvider: IAnimalProvider, private val usuarioProvider: IUsuarioProvider) :
    IAnimalService {
    override suspend fun getImagemAnimal(id: String): Deferred<Bitmap> =
        CoroutineScope(Dispatchers.Main).async {
            return@async animalProvider.getImagemAnimal(id).await()
        }

    override suspend fun getTodosAnimais(): Deferred<List<AnimalAsync>> =
        CoroutineScope(Dispatchers.Main).async {
            return@async animalProvider.getTodosAnimais().await().map { a ->
                AnimalAsync(a.id, a.nome, a.detalhes, animalProvider.getImagemAnimal(a.id))
            }
        }

    override suspend fun getAnimaisPostosAdocao(donoId: String): Deferred<List<AnimalAsync>> =
        CoroutineScope(Dispatchers.Main).async {
            return@async animalProvider.getAnimaisPostosAdocao(donoId).await().map { a ->
                AnimalAsync(a.id, a.nome, a.detalhes, animalProvider.getImagemAnimal(a.id))
            }
        }

    override suspend fun getAnimaisAdotados(adotadorId: String): Deferred<List<AnimalAsync>> =
        CoroutineScope(Dispatchers.Main).async {
            return@async animalProvider.getAnimaisAdotados(adotadorId).await().map { a ->
                AnimalAsync(a.id, a.nome, a.detalhes, animalProvider.getImagemAnimal(a.id))
            }
        }

    override suspend fun getAnimaisSolicitados(solicitadorId: String): Deferred<List<AnimalAsync>> =
        CoroutineScope(Dispatchers.Main).async {
            return@async animalProvider.getAnimaisSolicitados(solicitadorId).await().map { a ->
                AnimalAsync(a.id, a.nome, a.detalhes, animalProvider.getImagemAnimal(a.id))
            }
        }

    override suspend fun getDonoInicial(animalId: String): Deferred<UsuarioAsync> =
        CoroutineScope(Dispatchers.Main).async {
            val d = animalProvider.getDonoInicial(animalId).await()
            return@async UsuarioAsync(
                d.id, d.nome, d.detalhes, usuarioProvider.getUserImage(d.id)
            )
        }

    override suspend fun getAdotador(animalId: String): Deferred<UsuarioAsync?> =
        CoroutineScope(Dispatchers.Main).async {
            val a = animalProvider.getAdotador(animalId).await() ?: return@async null
            return@async UsuarioAsync(
                a.id, a.nome, a.detalhes, usuarioProvider.getUserImage(a.id)
            )
        }

    override suspend fun getAnimalSemImagem(id: String): Deferred<AnimalAsync> =
        CoroutineScope(Dispatchers.Main).async {
            val animal = animalProvider.getAnimal(id).await()
            return@async AnimalAsync(animal.id, animal.nome, animal.detalhes)
        }

    override suspend fun getAnimal(id: String): Deferred<AnimalAsync> =
        CoroutineScope(Dispatchers.Main).async {
            val imageTask = animalProvider.getImagemAnimal(id)
            val animal = animalProvider.getAnimal(id).await()
            return@async AnimalAsync(animal.id, animal.nome, animal.detalhes, imageTask)
        }

    override suspend fun adicionarAnimal(animal: Animal): Deferred<String> =
        CoroutineScope(Dispatchers.Main).async {
            if (animal.nome.isEmpty() || animal.nome.length > 64) {
                throw Exception("Nome deve ter entre 1 e 64 caracteres.")
            }
            if (animal.detalhes.length < 8 || animal.detalhes.length > 512) {
                throw Exception("Descrição deve ter entre 8 e 512 caracteres.")
            }

            return@async animalProvider.adicionarAnimal(animal).await()
        }

    override suspend fun editarAnimal(animal: Animal): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            return@async animalProvider.editarAnimal(animal).await()
        }

    override suspend fun removerAnimal(id: String): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            return@async animalProvider.removerAnimal(id).await()
        }

    override suspend fun animalBuscado(id: String): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            return@async animalProvider.animalBuscado(id).await()
        }
}
