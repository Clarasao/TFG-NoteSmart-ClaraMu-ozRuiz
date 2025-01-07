package com.example.notesmart.presentation.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.notesmart.data.model.Grade

class GradesAdapter(context: Context, private val gradesList: List<Grade>) : ArrayAdapter<Grade>(context, 0, gradesList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false)

        val grade = gradesList[position]
        val subjectNameTextView = view.findViewById<TextView>(android.R.id.text1)
        val gradeTextView = view.findViewById<TextView>(android.R.id.text2)

        subjectNameTextView.text = grade.subjectName
        gradeTextView.text = grade.grade.toString()

        when {
            grade.grade < 5 -> gradeTextView.setTextColor(Color.RED)
            grade.grade == 5.0 -> gradeTextView.setTextColor(Color.YELLOW)
            else -> gradeTextView.setTextColor(Color.GREEN)
        }

        return view
    }
}
