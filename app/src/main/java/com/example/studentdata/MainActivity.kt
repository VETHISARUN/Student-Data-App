package com.example.studentdata

import android.app.DatePickerDialog
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: StudentDatabaseHelper
    private lateinit var db: SQLiteDatabase
    private val students = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = StudentDatabaseHelper(this)
        db = dbHelper.writableDatabase

        val nameEditText: EditText = findViewById(R.id.nameEditText)
        val gradeEditText: EditText = findViewById(R.id.gradeEditText)
        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val phoneEditText: EditText = findViewById(R.id.phoneEditText)
        val spinner: Spinner = findViewById(R.id.courseSpinner)
        val datePickerButton: Button = findViewById(R.id.datePickerButton)
        val addButton: Button = findViewById(R.id.addButton)
        val resetButton: Button = findViewById(R.id.resetButton)
        val listView: ListView = findViewById(R.id.listView)
        val errorTextView: TextView = findViewById(R.id.errorTextView)
        val searchEditText: EditText = findViewById(R.id.searchEditText)

        val courses = arrayOf("Mathematics", "Physics", "Chemistry", "Computer Science", "Biology")
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, courses)

        datePickerButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                datePickerButton.text = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            }, year, month, day).show()
        }

        addButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val course = spinner.selectedItem.toString()
            val grade = gradeEditText.text.toString()
            val email = emailEditText.text.toString()
            val phone = phoneEditText.text.toString()
            val date = datePickerButton.text.toString()

            if (!email.contains("@")) {
                errorTextView.text = "Invalid email format. Email must contain '@'."
                errorTextView.visibility = View.VISIBLE
                return@setOnClickListener
            }

            if (!phone.matches(Regex("\\d{10}"))) {
                errorTextView.text = "Invalid phone number. Must be exactly 10 digits."
                errorTextView.visibility = View.VISIBLE
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(grade) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phone) || date == "Select Date") {
                errorTextView.text = "Please fill in all fields."
                errorTextView.visibility = View.VISIBLE
            } else {
                errorTextView.visibility = View.GONE
                val currentTime = Calendar.getInstance().time.toString()
                try {
                    db.execSQL(
                        "INSERT INTO students (name, course, grade, email, phone, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                        arrayOf(name, course, grade, email, phone, currentTime, currentTime)
                    )
                    Toast.makeText(this, "Student added successfully!", Toast.LENGTH_SHORT).show()
                    resetFields(nameEditText, gradeEditText, emailEditText, phoneEditText, datePickerButton, spinner)
                    displayStudents()
                } catch (e: Exception) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        resetButton.setOnClickListener {
            resetFields(nameEditText, gradeEditText, emailEditText, phoneEditText, datePickerButton, spinner)
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            showEditDeleteDialog(students[position])
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val filteredList = students.filter { it.contains(s.toString(), ignoreCase = true) }
                val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, filteredList)
                listView.adapter = adapter
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        displayStudents()
    }

    private fun resetFields(
        nameEditText: EditText, gradeEditText: EditText, emailEditText: EditText,
        phoneEditText: EditText, datePickerButton: Button, spinner: Spinner
    ) {
        nameEditText.text.clear()
        gradeEditText.text.clear()
        emailEditText.text.clear()
        phoneEditText.text.clear()
        datePickerButton.text = "Select Date"
        spinner.setSelection(0)
    }

    private fun displayStudents() {
        val cursor = db.rawQuery("SELECT * FROM students", null)
        students.clear()

        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val course = cursor.getString(cursor.getColumnIndexOrThrow("course"))
            val grade = cursor.getString(cursor.getColumnIndexOrThrow("grade"))
            val email = cursor.getString(cursor.getColumnIndexOrThrow("email"))
            val phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"))
            val createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at"))
            val updatedAt = cursor.getString(cursor.getColumnIndexOrThrow("updated_at"))
            students.add("Name: $name\nCourse: $course\nGrade: $grade\nEmail: $email\nPhone: $phone\nCreated: $createdAt\nUpdated: $updatedAt")
        }

        cursor.close()

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, students)
        val listView: ListView = findViewById(R.id.listView)
        listView.adapter = adapter
    }

    private fun showEditDeleteDialog(studentDetails: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_delete, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val editButton: Button = dialogView.findViewById(R.id.editButton)
        val deleteButton: Button = dialogView.findViewById(R.id.deleteButton)

        editButton.setOnClickListener {
            val details = studentDetails.split("\n")
            val name = details[0].substringAfter(": ")

            val editDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_student, null)
            val editDialog = AlertDialog.Builder(this)
                .setView(editDialogView)
                .setTitle("Edit Student")
                .setPositiveButton("Save") { _, _ ->
                    val newName = editDialogView.findViewById<EditText>(R.id.editNameEditText).text.toString()
                    val newGrade = editDialogView.findViewById<EditText>(R.id.editGradeEditText).text.toString()
                    val currentTime = Calendar.getInstance().time.toString()

                    db.execSQL(
                        "UPDATE students SET name = ?, grade = ?, updated_at = ? WHERE name = ?",
                        arrayOf(newName, newGrade, currentTime, name)
                    )
                    displayStudents()
                }
                .setNegativeButton("Cancel", null)
                .create()

            editDialog.show()
        }

        deleteButton.setOnClickListener {
            val name = studentDetails.split("\n")[0].substringAfter(": ")
            db.execSQL("DELETE FROM students WHERE name = ?", arrayOf(name))
            displayStudents()
            dialog.dismiss()
        }

        dialog.show()
    }
}
