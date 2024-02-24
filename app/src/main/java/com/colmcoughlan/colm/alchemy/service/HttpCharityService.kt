package com.colmcoughlan.colm.alchemy.service

import android.content.Context
import android.util.Log
import com.android.volley.VolleyLog
import com.android.volley.toolbox.StringRequest
import com.colmcoughlan.colm.alchemy.StaticState
import com.colmcoughlan.colm.alchemy.model.Callback
import com.colmcoughlan.colm.alchemy.model.CharitiesDto
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

class HttpCharityService(context: Context) : CharityService {

    private val utils = RequestQueueUtils.getInstance(context)
    private val url = utils.url
    private val objectMapper =
        jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    override fun getCharities(callback: Callback) {

        val request = object : StringRequest(Method.GET, url,
            { charitiesString ->
                val charitiesDto = objectMapper.readValue<CharitiesDto>(charitiesString)
                val charities = charitiesDto.charities.map { (k, v) ->
                    v.copy(
                        name = k,
                        number = charitiesDto.config.number.toString(),
                        donationOptions = objectMapper.readValue(v.donation_options),
                        frequencies = objectMapper.readValue(v.freq)
                    )
                }
                StaticState.charities = charities
                Log.i("http", charitiesString)
                callback.onComplete()
            },
            { e ->
                Log.e("http", "Error in charity fetch call", e)
                callback.onComplete()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf(Pair("client-id", "alchemy"))
            }
        }

        utils.addToRequestQueue(request)
    }
}