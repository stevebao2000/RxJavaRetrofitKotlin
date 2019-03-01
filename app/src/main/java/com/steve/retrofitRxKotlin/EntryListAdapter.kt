package com.steve.retrofitRxKotlin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class EntryListAdapter(context: Context, private val entries: List<GitHubEntry>) : ArrayAdapter<GitHubEntry>(context, R.layout.list_item, entries) {
    private val inflater: LayoutInflater

    private val lastPosition = -1

    init {

        //inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //inflater = Activity.getLayoutInflater();
        inflater = LayoutInflater.from(context)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (null == view) {
            view = inflater.inflate(R.layout.list_item, parent, false)
        }
        val tvId = view!!.findViewById<View>(R.id.item_id) as TextView
        val tview = view.findViewById<View>(R.id.item_login) as TextView
        tview.text = entries[position].login
        tvId.text = entries[position].id.toString()
        return view
    }
}
