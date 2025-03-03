package com.example.studentdata

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri

class StudentContentProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.example.studentdata.provider"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/students")
    }

    private lateinit var dbHelper: StudentDatabaseHelper

    override fun onCreate(): Boolean {
        dbHelper = StudentDatabaseHelper(context!!)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val db = dbHelper.readableDatabase
        return db.query("students", projection, selection, selectionArgs, null, null, sortOrder)
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val db = dbHelper.writableDatabase
        val id = db.insert("students", null, values)
        return Uri.withAppendedPath(CONTENT_URI, id.toString())
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        val db = dbHelper.writableDatabase
        return db.update("students", values, selection, selectionArgs)
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val db = dbHelper.writableDatabase
        return db.delete("students", selection, selectionArgs)
    }

    override fun getType(uri: Uri): String? = null
}
