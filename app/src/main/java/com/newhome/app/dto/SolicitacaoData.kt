package com.newhome.app.dto

import java.io.Serializable

data class SolicitacaoData(
    var id: SolicitacaoID? = null,
    var animal: AnimalData? = null,
    var solicitador: UserData? = null
) : Serializable
