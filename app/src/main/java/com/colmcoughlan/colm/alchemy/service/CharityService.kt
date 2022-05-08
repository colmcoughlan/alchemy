package com.colmcoughlan.colm.alchemy.service

import com.colmcoughlan.colm.alchemy.model.Callback

interface CharityService {
    fun getCharities(callback: Callback)
}