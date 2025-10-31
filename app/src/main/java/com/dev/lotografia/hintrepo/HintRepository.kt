package com.dev.lotografia.hintrepo

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HintRepository(private val context: Context) {
  val hintFilePath = "hints.json"

  fun loadHintsFromAssets(): List<CategoryHints> {
    return try {
      val json = context.assets
        .open(hintFilePath)
        .bufferedReader().use { it.readText() }
      val type = object : TypeToken<List<CategoryHints>>() {}.type
      Gson().fromJson(json, type)
    } catch (e: Exception) {
      e.printStackTrace()
      emptyList()
    }
  }
}