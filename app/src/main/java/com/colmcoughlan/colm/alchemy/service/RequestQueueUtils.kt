package com.colmcoughlan.colm.alchemy.service

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.colmcoughlan.colm.alchemy.R

class RequestQueueUtils constructor(context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: RequestQueueUtils? = null
        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: RequestQueueUtils(context).also {
                    INSTANCE = it
                }
            }
    }

    val requestQueue: RequestQueue by lazy {
        // applicationContext is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        Volley.newRequestQueue(context.applicationContext)
    }

    val url: String = context.resources.getString(R.string.server_url)
    fun <T> addToRequestQueue(req: Request<T>) {
        requestQueue.add(req)
    }
}