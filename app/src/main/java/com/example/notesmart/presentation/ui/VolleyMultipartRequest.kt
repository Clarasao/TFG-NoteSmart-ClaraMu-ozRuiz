package com.example.notesmart.presentation.ui

import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.HttpHeaderParser
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException

open class VolleyMultipartRequest(
    method: Int,
    url: String,
    private val listener: Response.Listener<NetworkResponse>,
    private val errorListener: Response.ErrorListener
) : Request<NetworkResponse>(method, url, errorListener) {

    override fun parseNetworkResponse(response: NetworkResponse): Response<NetworkResponse> {
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response))
    }

    override fun deliverResponse(response: NetworkResponse) {
        listener.onResponse(response)
    }

    override fun deliverError(error: VolleyError) {
        errorListener.onErrorResponse(error)
    }

    override fun getBodyContentType(): String {
        return "multipart/form-data;boundary=$boundary"
    }

    override fun getBody(): ByteArray {
        val bos = ByteArrayOutputStream()
        val dos = DataOutputStream(bos)

        try {
            params?.forEach { (key, value) ->
                buildTextPart(dos, key, value)
            }

            getByteData().forEach { (key, dataPart) ->
                buildFilePart(dos, key, dataPart)
            }

            dos.writeBytes("--$boundary--\r\n")
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return bos.toByteArray()
    }

    open fun getByteData(): Map<String, DataPart> {
        return emptyMap()
    }

    private val boundary = "boundary_${System.currentTimeMillis()}"

    private fun buildTextPart(dos: DataOutputStream, paramName: String, paramValue: String) {
        try {
            dos.writeBytes("--$boundary\r\n")
            dos.writeBytes("Content-Disposition: form-data; name=\"$paramName\"\r\n\r\n")
            dos.writeBytes("$paramValue\r\n")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun buildFilePart(dos: DataOutputStream, paramName: String, dataPart: DataPart) {
        try {
            dos.writeBytes("--$boundary\r\n")
            dos.writeBytes("Content-Disposition: form-data; name=\"$paramName\"; filename=\"${dataPart.fileName}\"\r\n")
            dos.writeBytes("Content-Type: ${dataPart.mimeType}\r\n\r\n")
            dos.write(dataPart.content)
            dos.writeBytes("\r\n")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    data class DataPart(
        val fileName: String,
        val content: ByteArray,
        val mimeType: String = "application/octet-stream"
    )
}
