package com.colmcoughlan.colm.alchemy.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.colmcoughlan.colm.alchemy.R

class DonationsAdapter(
    private val mContext: Context,
    private val donations: Map<String, Int>
) : BaseAdapter() {

    private val charities = donations.entries.sortedBy { (_, v) -> v }.map { (k, _) -> k }

    override fun getCount(): Int {
        return donations.size
    }

    override fun getItem(position: Int): String {
        return charities[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    // create a new ImageView for each item referenced by the Adapter
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return if (convertView == null) {
            val inflater = mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val newView = inflater.inflate(R.layout.my_donations_layout, null)
            val textView = newView.findViewById<TextView>(R.id.my_donations_gridview_text)
            newView.tag = textView
            setTextView(position, textView)
            newView
        } else {
            val textView = convertView.tag as TextView
            setTextView(position, textView)
            convertView;
        }
    }

    private fun setTextView(position: Int, textView: TextView) {
        val charityName = charities[position]
        textView.text = String.format("%s: €%s", charityName, donations[charityName])
    }
}