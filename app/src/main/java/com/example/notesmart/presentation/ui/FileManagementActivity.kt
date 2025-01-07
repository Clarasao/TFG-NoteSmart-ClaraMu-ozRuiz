package com.example.notesmart.presentation.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.notesmart.R
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class FileManagementActivity : AppCompatActivity() {

    private lateinit var fileListLayout: LinearLayout
    private val fileList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_management)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fileListLayout = findViewById(R.id.llFileList)
        val uploadButton = findViewById<Button>(R.id.btnUploadFile)

        uploadButton.setOnClickListener {
            openFilePicker()
        }

        loadUploadedFiles()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/pdf", "text/plain"))
        }
        startActivityForResult(Intent.createChooser(intent, "Selecciona un archivo"), REQUEST_FILE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_FILE_PICK && resultCode == Activity.RESULT_OK) {
            val uri = data?.data ?: return
            uploadFileToServer(uri)
        }
    }

    private fun getFileName(uri: Uri): String {
        var fileName = ""
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && it.moveToFirst()) {
                fileName = it.getString(nameIndex)
            }
        }
        return fileName.ifEmpty { "archivo_subido_${System.currentTimeMillis()}" }
    }

    private fun uploadFileToServer(uri: Uri) {
        val studentId = intent.getIntExtra("studentId", 0)
        val uploadUrl = "http://10.0.2.2/school/upload_file.php"

        val fileName = getFileName(uri)
        val inputStream = contentResolver.openInputStream(uri) ?: return
        val fileBytes = inputStream.readBytes()

        val request = object : VolleyMultipartRequest(Method.POST, uploadUrl,
            Response.Listener { response ->
                try {
                    val jsonResponse = JSONObject(String(response.data))
                    if (jsonResponse.getString("status") == "success") {
                        Toast.makeText(this, "Archivo subido correctamente", Toast.LENGTH_SHORT).show()
                        loadUploadedFiles()
                    } else {
                        Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error al procesar la respuesta del servidor", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Error de conexión: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {

            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["studentId"] = studentId.toString()
                return params
            }

            override fun getByteData(): Map<String, DataPart> {
                val params = HashMap<String, DataPart>()
                params["file"] = DataPart(fileName, fileBytes)
                return params
            }
        }

        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(request)
    }

    private fun loadUploadedFiles() {
        val studentId = intent.getIntExtra("studentId", 0)
        val fetchUrl = "http://10.0.2.2/school/fetch_files.php?studentId=$studentId"

        val request = StringRequest(
            Request.Method.GET, fetchUrl,
            { response ->
                try {
                    val jsonArray = JSONArray(response)
                    fileList.clear()
                    fileListLayout.removeAllViews()
                    for (i in 0 until jsonArray.length()) {
                        val fileObject = jsonArray.getJSONObject(i)
                        val fileName = fileObject.getString("file_name")
                        fileList.add(fileName)

                        val fileLayout = layoutInflater.inflate(R.layout.item_file, null)
                        val fileTextView = fileLayout.findViewById<TextView>(R.id.tvFileName)
                        val deleteButton = fileLayout.findViewById<Button>(R.id.btnDeleteFile)

                        fileTextView.text = fileName

                        deleteButton.setOnClickListener {
                            deleteFileFromServer(fileName)
                        }

                        fileListLayout.addView(fileLayout)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error al procesar la lista de archivos", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error de conexión: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(request)
    }

    private fun deleteFileFromServer(fileName: String) {
        val studentId = intent.getIntExtra("studentId", 0)
        val deleteUrl = "http://10.0.2.2/school/delete_file.php"

        val request = object : StringRequest(Method.POST, deleteUrl,
            Response.Listener { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("status") == "success") {
                        Toast.makeText(this, "Archivo eliminado correctamente", Toast.LENGTH_SHORT).show()
                        loadUploadedFiles()
                    } else {
                        Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error al procesar la respuesta del servidor", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Error de conexión: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {

            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["studentId"] = studentId.toString()
                params["fileName"] = fileName
                return params
            }
        }

        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(request)
    }

    companion object {
        private const val REQUEST_FILE_PICK = 1
    }
}
