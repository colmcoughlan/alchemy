package com.colmcoughlan.colm.alchemy.utils

import android.app.AlertDialog
import android.content.Context
import com.colmcoughlan.colm.alchemy.R
import com.colmcoughlan.colm.alchemy.model.Callback
import com.colmcoughlan.colm.alchemy.model.Charity

object DialogUtils {
    // check with the user if they want to confirm a donation
    fun confirmDialog(context: Context, charity: Charity, keyword: String, freq: String,
    successCallback: Callback) {
        val builder = AlertDialog.Builder(context)
        val msg: String = when (freq) {
            "once" -> "Donate "
            "week" -> "Set up a weekly donation of "
            "month" -> "Set up a monthly donation of "
            else -> "ERROR! Please report this and try a different donation option."
        }
        builder.setTitle(msg + charity.getCost(keyword) + " to " + charity.name + "?")
        builder.setMessage(context.getString(R.string.likecharity_tcs))
        builder.setPositiveButton(R.string.confirm_yes) { dialog, _ ->
            successCallback.onComplete()
            dialog.dismiss()
        }
        builder.setNegativeButton(
            android.R.string.cancel
        ) { dialog, _ -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
    }
}