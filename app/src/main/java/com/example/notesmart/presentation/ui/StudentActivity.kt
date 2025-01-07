package com.example.notesmart.presentation.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.notesmart.R
import com.example.notesmart.data.model.Grade
import com.example.notesmart.presentation.adapter.GradesAdapter
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.random.Random

class StudentActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var tvStudentName: TextView
    private lateinit var tvStudentSurname: TextView
    private lateinit var tvStudentGrade: TextView
    private lateinit var tvEventosDelDia: TextView
    private lateinit var lvHorario: ListView
    private lateinit var btnEditarEvento: Button
    private lateinit var btnSubirArchivos: Button
    private var studentIdAdress : Int = 0

    private val eventMap = mutableMapOf<String, String>()
    private val eventList = listOf(
        "Examen de Matemáticas",
        "Entrega de Proyecto de Historia",
        "Examen de Lengua Española",
        "Reunión de Padres",
        "Clase especial de Física",
        "Entrega de Proyectos"
    )

    private var selectedDate: String? = null

    @SuppressLint("SetTextI18n", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student)

        tvStudentName = findViewById(R.id.tvStudentNombre)
        tvStudentSurname = findViewById(R.id.tvStudentApellido)
        tvStudentGrade = findViewById(R.id.tvStudentCurso)
        calendarView = findViewById(R.id.calendarView)
        tvEventosDelDia = findViewById(R.id.tvEventosDelDia)
        lvHorario = findViewById(R.id.lvHorario)
        btnEditarEvento = findViewById(R.id.btnEditarEvento)
        btnSubirArchivos = findViewById(R.id.btnSubirArchivos)

        btnEditarEvento.isEnabled = false

        val studentName = intent.getStringExtra("name") ?: "Nombre no disponible"
        val studentLastName = intent.getStringExtra("surname") ?: "Apellido no disponible"
        val studentGradeId = intent.getIntExtra("grade", 0)
        val studentEmail = intent.getStringExtra("email") ?: "Email no disponible"


        tvStudentName.text = studentName
        tvStudentSurname.text = studentLastName
        tvStudentGrade.text = when (studentGradeId) {
            1 -> "1º ESO"
            2 -> "2º ESO"
            3 -> "3º ESO"
            4 -> "4º ESO"
            5 -> "1º Bachillerato"
            6 -> "2º Bachillerato"
            else -> "Curso desconocido"
        }

        generateRandomEvents()

        val today = getCurrentDate(calendarView)
        updateEventosDelDia(today)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = "$dayOfMonth/${month + 1}/$year"
            updateEventosDelDia(selectedDate)
            btnEditarEvento.isEnabled = true
            this.selectedDate = selectedDate
        }

        btnEditarEvento.setOnClickListener {
            if (selectedDate != null) {
                editarEvento(selectedDate!!)
            } else {
                Toast.makeText(this, "Por favor, selecciona una fecha primero.", Toast.LENGTH_SHORT).show()
            }
        }

        getStudentId(studentEmail) { studentId ->
            if (studentId != null) {
                getStudentGrades(studentId)
                studentIdAdress = studentId
            } else {
                Toast.makeText(this, "No se pudo obtener el ID del estudiante", Toast.LENGTH_SHORT).show()
            }
        }

        btnSubirArchivos.setOnClickListener {
            val intent = Intent(this, FileManagementActivity::class.java)
            intent.putExtra("studentId", studentIdAdress)
            startActivity(intent)
        }

    }

    private fun generateRandomEvents() {
        val random = Random(System.currentTimeMillis())
        val calendar = Calendar.getInstance()

        for (i in 1..30) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH) + 1
            val year = calendar.get(Calendar.YEAR)
            val eventDate = "$day/$month/$year"
            val event = eventList[random.nextInt(eventList.size)]
            eventMap[eventDate] = event
        }
    }

    private fun updateEventosDelDia(date: String) {
        val event = eventMap[date]
        tvEventosDelDia.text = if (event != null) {
            "Eventos para $date: $event"
        } else {
            "No hay eventos programados para $date."
        }
    }

    private fun getCurrentDate(calendarView: CalendarView): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = calendarView.date
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        return "$day/$month/$year"
    }

    private fun getStudentId(email: String, callback: (Int?) -> Unit) {
        val url = "http://10.0.2.2/school/get_student_id.php?email=$email"

        val stringRequest = object : StringRequest(Method.GET, url,
            Response.Listener { response ->
                try {
                    val jsonResponse = JSONObject(response)

                    if (jsonResponse.getString("status") == "success") {
                        val studentId = jsonResponse.getInt("id")
                        callback(studentId)
                    } else {
                        Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                        callback(null)
                    }
                } catch (e: JSONException) {
                    Toast.makeText(this, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show()
                    callback(null)
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Error de conexión: ${error.message}", Toast.LENGTH_SHORT).show()
                callback(null)
            }) {}

        val queue = Volley.newRequestQueue(this)
        queue.add(stringRequest)
    }

    private fun getStudentGrades(studentId: Int) {
        val url = "http://10.0.2.2/school/get_student_grades.php"
        val urlWithParams = "$url?studentId=$studentId"

        val stringRequest = object : StringRequest(Method.GET, urlWithParams,
            Response.Listener { response ->
                try {
                    val jsonResponse = JSONObject(response)

                    if (jsonResponse.getString("status") == "success") {
                        val gradesArray = jsonResponse.getJSONArray("grades")
                        val gradesList = mutableListOf<Grade>()

                        for (i in 0 until gradesArray.length()) {
                            val gradeObject = gradesArray.getJSONObject(i)
                            val subjectName = gradeObject.getString("subjectName")
                            val grade = gradeObject.getDouble("grade")
                            gradesList.add(Grade(subjectName, grade))
                        }

                        val adapter = GradesAdapter(this, gradesList)
                        lvHorario.adapter = adapter

                    } else {
                        Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    Toast.makeText(this, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Error de conexión: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {}

        val queue = Volley.newRequestQueue(this)
        queue.add(stringRequest)
    }


    private fun editarEvento(date: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Editar Evento")
        builder.setMessage("Editar evento para la fecha: $date")

        val input = EditText(this)
        input.hint = "Escribe el nuevo evento"
        input.setText(eventMap[date] ?: "")
        builder.setView(input)

        builder.setPositiveButton("Guardar") { _, _ ->
            val nuevoEvento = input.text.toString().trim()
            if (nuevoEvento.isNotEmpty()) {
                eventMap[date] = nuevoEvento
                updateEventosDelDia(date)
                Toast.makeText(this, "Evento guardado para $date", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "El evento no puede estar vacío", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }

}