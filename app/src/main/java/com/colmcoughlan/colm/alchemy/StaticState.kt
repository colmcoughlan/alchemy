package com.colmcoughlan.colm.alchemy

import com.colmcoughlan.colm.alchemy.model.Charity

object StaticState {
    @JvmStatic
    var category = "All"

    @JvmStatic
    var charities: List<Charity> = emptyList()
}