package com.example.boardgamecollector

import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.ListAdapter
import android.widget.ListView

//https://stackoverflow.com/a/3495908/14595589

fun setListViewHeightBasedOnChildren(listView: ListView) {
    val listAdapter: ListAdapter = listView.adapter

    var totalHeight = listView.paddingTop + listView.paddingBottom

    for (i in 0 until listAdapter.count) {
        val listItem = listAdapter.getView(i, null, listView);
        if (listItem is ViewGroup) {
            listItem.setLayoutParams(LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
         }

         listItem.measure(0, 0);
         totalHeight += listItem.measuredHeight;
    }

    val params = listView.layoutParams;
    params.height = totalHeight + (listView.dividerHeight * (listAdapter.count - 1));
    listView.layoutParams = params;
}