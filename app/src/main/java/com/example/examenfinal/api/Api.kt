package com.example.examen.api

import kotlinx.serialization.json.JsonObject
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class Api {
    private val cliente = OkHttpClient()
    private val URL_IP: String = "http://52.0.17.176:8081"

    private fun formatJson_toRequestBody(json: JsonObject): RequestBody {
        val jsonString: String = json.toString()
        val jsonMediaType = "application/json; charset=utf-8".toMediaType()
        return jsonString.toRequestBody(jsonMediaType)
    }

    fun putDatosChristianG(datos: JsonObject) {
        val request = Request.Builder().url("$URL_IP/PUT").put(formatJson_toRequestBody(datos)).build()
        cliente.newCall(request).execute()
    }

}