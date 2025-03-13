package com.example.recipebook.data

import android.annotation.SuppressLint
import android.os.Build
import android.text.Html


// Decode les strings HTML ( &amp;, &quot;, etc...)
@SuppressLint("ObsoleteSdkInt")
fun decodeHtml(htmlText: String): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY).toString()
    } else {
        @Suppress("DEPRECATION")
        Html.fromHtml(htmlText).toString()
    }
}