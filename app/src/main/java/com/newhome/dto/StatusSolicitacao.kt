package com.newhome.dto

import java.io.Serializable

data class StatusSolicitacao(
    var solicitado: Boolean = false,
    var solicitacaoAceita: Boolean = false,
    var detalhesAdocao: String = ""
) : Serializable
