package com.newhome.services.concrete

import com.newhome.dao.IContaProvider
import com.newhome.dao.IUsuarioProvider
import com.newhome.dto.Credenciais
import com.newhome.dto.NovaConta
import com.newhome.dto.NovoUsuario
import com.newhome.services.IContaService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class ContaService(
    private val usuarioProvider: IUsuarioProvider,
    private val contaProvider: IContaProvider
) : IContaService {
    override fun getContaID(): String? {
        return contaProvider.getContaID()
    }

    override suspend fun cadastrar(novaConta: NovaConta): Deferred<Unit> =
        CoroutineScope(Dispatchers.IO).async {
            val credenciais = Credenciais(novaConta.email, novaConta.senha)
            contaProvider.criarConta(credenciais).await()

            val uid = contaProvider.getContaID()!!

            val usuario = NovoUsuario(uid, novaConta.nome, "", novaConta.idade)
            usuarioProvider.criarUsuario(usuario).await()
        }

    override suspend fun logar(credenciais: Credenciais): Deferred<Unit> =
        CoroutineScope(Dispatchers.IO).async {
            contaProvider.logar(credenciais).await()
        }

    override fun tentarUsarContaLogada() {
        if (contaProvider.getContaID() == null) throw Exception("User not signed in.")
    }

    override suspend fun sair(): Deferred<Unit> =
        CoroutineScope(Dispatchers.IO).async {
            contaProvider.sair().await()
        }

    override suspend fun excluirConta(): Deferred<Unit> =
        CoroutineScope(Dispatchers.IO).async {
            // TODO implementar
            usuarioProvider.deleteUsuario(contaProvider.getContaID()!!).await()
            contaProvider.excluirConta().await()
        }
}
