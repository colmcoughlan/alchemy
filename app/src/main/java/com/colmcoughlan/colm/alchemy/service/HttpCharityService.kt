package com.colmcoughlan.colm.alchemy.service

import android.content.Context
import android.util.Log
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.VolleyLog
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.colmcoughlan.colm.alchemy.R
import com.colmcoughlan.colm.alchemy.StaticState
import com.colmcoughlan.colm.alchemy.model.Callback
import com.colmcoughlan.colm.alchemy.model.CharitiesDto
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

class HttpCharityService(context: Context) : CharityService {
    private val queue = Volley.newRequestQueue(context)
    private val url = context.resources.getString(R.string.server_url)
    private val objectMapper = jacksonObjectMapper()

    override fun getCharities(callback: Callback) {

        VolleyLog.DEBUG = true

        if (!StaticState.shouldRefreshCharities()) {
            callback.onComplete()
            return
        }

        val request = object : StringRequest(Method.GET, url,
            { charitiesString ->
                    Log.i("http", charitiesString)
            },
            { e -> Log.e("http", "error fetching charities", e) }) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf(Pair("client-id", "alchemy"))
            }
        }

        queue.add(request)
    }
}