package com.example.notesmart.presentation.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.CalendarView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.notesmart.R

class StudentActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var lvEventos: ListView
    private lateinit var tvStudentName: TextView
    private lateinit var tvStudentSurname: TextView
    private lateinit var tvStudentGrade: TextView
    private lateinit var tvEventosDelDia: TextView

    private val eventList = mutableMapOf<String, List<String>>()

    @SuppressLint("SetTextI18n", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student)

        tvStudentName = findViewById(R.id.tvStudentNombre)
        tvStudentSurname = findViewById(R.id.tvStudentApellido)
        tvStudentGrade = findViewById(R.id.tvStudentCurso)
        calendarView = findViewById(R.id.calendarView)
        lvEventos = findViewById(R.id.lvHorario)
        tvEventosDelDia = findViewById(R.id.tvEventosDelDia)

        val studentName = intent.getStringExtra("name") ?: "Nombre no disponible"
        val studentLastName = intent.getStringExtra("surname") ?: "Apellido no disponible"
        val studentGradeId = intent.getIntExtra("grade", 0)

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

        initializeEvents()

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = "$dayOfMonth/${month + 1}/$year"
            tvEventosDelDia.text = "Eventos para el $selectedDate"

            val events = eventList[selectedDate] ?: emptyList()
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, events)
            lvEventos.adapter = adapter
        }
    }

    private fun initializeEvents() {
        eventList["25/12/2024"] = listOf("Examen de Matemáticas", "Entrega de Proyecto de Historia")
        eventList["26/12/2024"] = listOf("Examen de Lengua Española")
        eventList["27/12/2024"] = listOf("Entrega de Proyecto de Biología")
    }
}
