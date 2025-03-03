package com.example.studentdata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment

class SearchFragment : Fragment() {

    private lateinit var searchEditText: EditText
    private lateinit var listView: ListView
    private lateinit var dbHelper: StudentDatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        searchEditText = view.findViewById(R.id.searchEditText)
        listView = view.findViewById(R.id.listView)
        dbHelper = StudentDatabaseHelper(requireContext())

        searchEditText.addTextChangedListener {
            val query = it.toString()
            filterStudents(query)
        }

        return view
    }

    private fun filterStudents(query: String) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM students WHERE name LIKE ? OR course LIKE ?",
            arrayOf("%$query%", "%$query%")
        )

        val students = mutableListOf<String>()
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val course = cursor.getString(cursor.getColumnIndexOrThrow("course"))
            students.add("$name - $course")
        }

        cursor.close()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, students)
        listView.adapter = adapter
    }
}
