package com.example.studentdata

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class StudentDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        // Create the students table
        db.execSQL(
            """
            CREATE TABLE students (
                student_id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                course TEXT NOT NULL,
                grade TEXT NOT NULL,
                email TEXT UNIQUE,
                phone TEXT UNIQUE,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
            """
        )

        // Create a trigger to update the 'updated_at' field whenever a record is updated
        db.execSQL(
            """
            CREATE TRIGGER update_student_timestamp
            AFTER UPDATE ON students
            FOR EACH ROW
            BEGIN
                UPDATE students SET updated_at = CURRENT_TIMESTAMP WHERE student_id = OLD.student_id;
            END;
            """
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop the old table if it exists and recreate it
        db.execSQL("DROP TABLE IF EXISTS students")
        db.execSQL("DROP TRIGGER IF EXISTS update_student_timestamp")
        onCreate(db)
    }

    companion object {
        private const val DATABASE_NAME = "students.db"
        private const val DATABASE_VERSION = 2 // Incremented the version number
    }
}
