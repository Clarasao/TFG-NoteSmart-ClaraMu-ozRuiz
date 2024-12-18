package com.example.notesmart.presentation.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
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

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher)

        val teacherName = intent.getStringExtra("name") ?: "Nombre no disponible"
        val teacherLastName = intent.getStringExtra("surname") ?: "Apellido no disponible"
        val teacherGradeId = intent.getIntExtra("courseId", 0)

        val tvProfesorName = findViewById<TextView>(R.id.tvProfesorNombre)
        val tvProfesorSurname = findViewById<TextView>(R.id.tvProfesorApellido)
        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        val tvEventosDelDia = findViewById<TextView>(R.id.tvEventosDelDia)
        val btnEditarEvento = findViewById<Button>(R.id.btnEditarEvento)
        val lvStudents = findViewById<ListView>(R.id.lvStudents)
        val tvCurso = findViewById<TextView>(R.id.tvCurso)

        tvProfesorName.text = teacherName
        tvProfesorSurname.text = teacherLastName

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

    private fun showEditGradeDialog(student: StudentModel, teacherGradeId: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Editar calificación de ${student.name} ${student.surname}")

        val input = EditText(this)
        input.setText(student.grade.toString())
        input.hint = "Ingrese la nueva calificación"
        builder.setView(input)

        builder.setPositiveButton("Guardar") { dialog, _ ->
            val newGrade = input.text.toString().toIntOrNull()
            if (newGrade != null) {
                student.grade = newGrade
                updateStudentGradeInDatabase(student, teacherGradeId) // Se pasa el courseId también
                Toast.makeText(this, "Calificación actualizada", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Por favor ingresa una calificación válida", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun updateStudentGradeInDatabase(student: StudentModel, teacherGradeId: Int) {
        val url = "http://10.0.2.2/school/update_student_grade.php"

        val params = HashMap<String, String>()
        params["studentId"] = student.id.toString()
        params["grade"] = student.grade.toString()
        params["courseId"] = teacherGradeId.toString()

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener { response ->
                try {
                    if (response == "success") {
                        Toast.makeText(this, "Calificación actualizada exitosamente", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error al actualizar la calificación", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error al procesar la respuesta: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Error al enviar la solicitud: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): Map<String, String> {
                return params
            }
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