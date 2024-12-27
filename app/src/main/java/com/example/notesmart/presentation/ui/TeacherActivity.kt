package com.example.notesmart.presentation.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.widget.*
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.notesmart.R
import com.example.notesmart.data.model.StudentModel
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random

class TeacherActivity : Activity() {

    private val eventMap = mutableMapOf<String, String>()
    private val eventList = listOf(
        "Examen de Matemáticas",
        "Revisión de Tareas",
        "Clase de Historia",
        "Reunión de Padres",
        "Examen de Física",
        "Entrega de Proyectos"
    )

    private val studentList = mutableListOf<StudentModel>()
    private val subjectMap = mutableMapOf<Int, String>()
    private lateinit var tvClock: TextView
    private val handler = Handler()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher)

        val teacherName = intent.getStringExtra("name") ?: "Nombre no disponible"
        val teacherLastName = intent.getStringExtra("surname") ?: "Apellido no disponible"
        val teacherGradeId = intent.getIntExtra("courseId", 0)

        tvClock = findViewById(R.id.tvClock)

        val tvProfesorName = findViewById<TextView>(R.id.tvProfesorNombre)
        val tvProfesorSurname = findViewById<TextView>(R.id.tvProfesorApellido)
        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        val tvEventosDelDia = findViewById<TextView>(R.id.tvEventosDelDia)
        val btnEditarEvento = findViewById<Button>(R.id.btnEditarEvento)
        val lvStudents = findViewById<ListView>(R.id.lvStudents)
        val tvCurso = findViewById<TextView>(R.id.tvCurso)
        val tvProfesorCurso = findViewById<TextView>(R.id.tvProfesorCurso)


        val runnable = object : Runnable {
            override fun run() {
                val currentDateTime = Calendar.getInstance().time
                val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                val formattedTime = formatter.format(currentDateTime)

                tvClock.text = formattedTime

                handler.postDelayed(this, 1000)
            }
        }

        val teacherCourse = when (teacherGradeId) {
            1 -> "1º ESO"
            2 -> "2º ESO"
            3 -> "3º ESO"
            4 -> "4º ESO"
            5 -> "1º Bachillerato"
            6 -> "2º Bachillerato"
            else -> "Curso desconocido"
        }

        tvProfesorName.text = teacherName
        tvProfesorSurname.text = teacherLastName
        tvProfesorCurso.text = "Curso: $teacherCourse"

        generateRandomEvents()
        val today = getCurrentDate(calendarView)
        updateEventosDelDia(today, tvEventosDelDia)

        getStudents(teacherGradeId, lvStudents)

        lvStudents.setOnItemClickListener { _, _, position, _ ->
            val student = studentList[position]
            showEditGradeDialog(student, teacherGradeId)

            tvCurso.text = "Asignatura: ${student.subjectName}"
        }

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = "$dayOfMonth/${month + 1}/$year"
            updateEventosDelDia(selectedDate, tvEventosDelDia)

            val event = eventMap[selectedDate]
            btnEditarEvento.isEnabled = event != null

            btnEditarEvento.setOnClickListener {
                showEditEventDialog(selectedDate)
            }
        }
        handler.post(runnable)
    }


    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
    }

    private fun generateRandomEvents() {
        val random = Random(System.currentTimeMillis())
        val calendar = java.util.Calendar.getInstance()

        for (i in 1..30) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            val month = calendar.get(java.util.Calendar.MONTH) + 1
            val year = calendar.get(java.util.Calendar.YEAR)
            val eventDate = "$day/$month/$year"
            val event = eventList[random.nextInt(eventList.size)]
            eventMap[eventDate] = event
        }
    }

    private fun updateEventosDelDia(date: String, tvEventosDelDia: TextView) {
        val event = eventMap[date]
        tvEventosDelDia.text = if (event != null) {
            "Eventos para $date: $event"
        } else {
            "No hay eventos programados para $date."
        }
    }

    private fun showEditEventDialog(date: String) {
        val currentEvent = eventMap[date]
        val builder = AlertDialog.Builder(this)
        builder.setTitle(if (currentEvent != null) "Editar Evento para $date" else "Crear Evento para $date")

        val input = EditText(this)
        input.setText(currentEvent ?: "")
        input.hint = "Nombre del evento (Ej. Examen de Matemáticas)"
        builder.setView(input)

        builder.setPositiveButton("Guardar") { dialog, _ ->
            val eventDescription = input.text.toString().trim()
            if (eventDescription.isNotEmpty()) {
                eventMap[date] = eventDescription
                Toast.makeText(this, "Evento guardado para $date", Toast.LENGTH_SHORT).show()
                updateEventosDelDia(date, findViewById(R.id.tvEventosDelDia))
            } else {
                Toast.makeText(this, "Por favor ingresa una descripción del evento", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun showEditGradeDialog(student: StudentModel, subjectId: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Editar nota de ${student.name} ${student.surname} en la asignatura")

        val input = EditText(this).apply {
            hint = "Ingrese la nueva nota (0-10)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        builder.setView(input)

        builder.setPositiveButton("Guardar") { dialog, _ ->
            val newGrade = input.text.toString().toFloatOrNull()
            if (newGrade != null && newGrade in 0.0..10.0) {
                updateStudentGradeInDatabase(student.id, subjectId, newGrade)

                val studentIndex = studentList.indexOfFirst { it.id == student.id }
                if (studentIndex != -1) {
                    studentList[studentIndex] = student.copy(grade = newGrade.toInt())
                }

                val updatedNames = studentList.map { "${it.name} ${it.surname} \n Asignatura: ${it.subjectName} - Nota: ${it.grade}" }
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, updatedNames)
                findViewById<ListView>(R.id.lvStudents).adapter = adapter

                Toast.makeText(this, "Nota actualizada exitosamente", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Por favor ingrese un número válido entre 0 y 10", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun updateStudentGradeInDatabase(studentId: Int, subjectId: Int, newGrade: Float) {
        val url = "http://10.0.2.2/school/update_student_grade.php"

        val params = HashMap<String, String>().apply {
            put("studentId", studentId.toString())
            put("subjectId", subjectId.toString())
            put("grade", newGrade.toString())
        }

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    val status = jsonResponse.getString("status")
                    val message = jsonResponse.getString("message")

                    if (status == "success") {
                        Toast.makeText(this, "Nota actualizada exitosamente", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error al procesar la respuesta: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Error al enviar la solicitud: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): Map<String, String> = params
        }

        val queue = Volley.newRequestQueue(this)
        queue.add(stringRequest)
    }

    private fun getStudents(courseId: Int, lvStudents: ListView) {
        val url = "http://10.0.2.2/school/get_students_with_subjects.php?courseId=$courseId"

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val jsonArray = JSONArray(response)
                    val studentNames = mutableListOf<String>()
                    studentList.clear()

                    for (i in 0 until jsonArray.length()) {
                        val studentJson = jsonArray.getJSONObject(i)
                        val id = studentJson.getInt("id")
                        val name = studentJson.getString("name")
                        val surname = studentJson.getString("surname")
                        val grade = studentJson.optInt("grade", 0)
                        val subjectId = studentJson.getInt("subjectId")

                        getSubjectName(subjectId) { subjectName ->
                            val student = StudentModel(
                                id,
                                name,
                                surname,
                                studentJson.getString("email"),
                                studentJson.getString("password"),
                                studentJson.getString("phone"),
                                grade,
                                studentJson.getString("enrollmentDate"),
                                subjectName
                            )

                            studentNames.add("$name $surname \n Asignatura: $subjectName - Nota: $grade")
                            studentList.add(student)

                            if (i == jsonArray.length() - 1) {
                                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, studentNames)
                                lvStudents.adapter = adapter
                            }
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error al procesar los datos", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error al obtener los estudiantes: ${error.message}", Toast.LENGTH_SHORT).show()
            })

        val queue = Volley.newRequestQueue(this)
        queue.add(stringRequest)
    }

    private fun getSubjectName(subjectId: Int, callback: (String) -> Unit) {
        val url = "http://10.0.2.2/school/get_subject_name.php?subjectId=$subjectId"

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    val subjectName = jsonResponse.getString("name")
                    subjectMap[subjectId] = subjectName
                    callback(subjectName)
                } catch (e: JSONException) {
                    Toast.makeText(this, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show()
                    callback("Desconocida")
                }
            },
            { error ->
                Toast.makeText(this, "Error al obtener el nombre de la asignatura: ${error.message}", Toast.LENGTH_SHORT).show()
                callback("Desconocida")
            })

        val queue = Volley.newRequestQueue(this)
        queue.add(stringRequest)
    }

    private fun getCurrentDate(calendarView: CalendarView): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = calendarView.date
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        val month = calendar.get(java.util.Calendar.MONTH) + 1
        val year = calendar.get(java.util.Calendar.YEAR)

        return "$day/$month/$year"
    }
}