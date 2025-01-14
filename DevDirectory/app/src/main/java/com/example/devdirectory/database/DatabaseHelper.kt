package com.example.devdirectory.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "CompanyDirectory.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "companies"

        // Columnas
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_URL = "urlWeb"
        private const val COLUMN_PHONE = "phone"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PRODUCTS = "products"
        private const val COLUMN_CLASSIFICATION = "classification"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_URL TEXT,
                $COLUMN_PHONE TEXT,
                $COLUMN_EMAIL TEXT,
                $COLUMN_PRODUCTS TEXT,
                $COLUMN_CLASSIFICATION TEXT
            )
        """.trimIndent()

        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // CRUD Operations
    fun createCompany (company: Company): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, company.name)
            put(COLUMN_URL, company.urlWeb)
            put(COLUMN_PHONE, company.phone)
            put(COLUMN_EMAIL, company.email)
            put(COLUMN_PRODUCTS, company.products)
            put(COLUMN_CLASSIFICATION, company.classification)
        }
        return db.insert(TABLE_NAME, null, values)
    }

    fun getCompanies(): List<Company> {
        val empresasList = mutableListOf<Company>()
        val db = this.readableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, "$COLUMN_NAME ASC")

        with(cursor) {
            while (moveToNext()) {
                val company = Company(
                    id = getLong(getColumnIndexOrThrow(COLUMN_ID)),
                    name = getString(getColumnIndexOrThrow(COLUMN_NAME)),
                    urlWeb = getString(getColumnIndexOrThrow(COLUMN_URL)),
                    phone = getString(getColumnIndexOrThrow(COLUMN_PHONE)),
                    email = getString(getColumnIndexOrThrow(COLUMN_EMAIL)),
                    products = getString(getColumnIndexOrThrow(COLUMN_PRODUCTS)),
                    classification = getString(getColumnIndexOrThrow(COLUMN_CLASSIFICATION))
                )
                empresasList.add(company)
            }
        }
        cursor.close()
        return empresasList
    }

    fun searchCompany(name: String? = null, classification: String? = null): List<Company> {
        val empresasList = mutableListOf<Company>()
        val db = this.readableDatabase

        val conditions = mutableListOf<String>()
        val args = mutableListOf<String>()

        name?.let {
            conditions.add("$COLUMN_NAME LIKE ?")
            args.add("%$it%")
        }

        classification?.let {
            conditions.add("${COLUMN_CLASSIFICATION} = ?")
            args.add(it)
        }

        val whereClause = if (conditions.isNotEmpty()) conditions.joinToString(" AND ") else null
        val cursor = db.query(TABLE_NAME, null, whereClause, args.toTypedArray(), null, null, "$COLUMN_NAME ASC")

        cursor.use {
            while (it.moveToNext()) {
                val company = Company(
                    id = it.getLong(it.getColumnIndexOrThrow(COLUMN_ID)),
                    name = it.getString(it.getColumnIndexOrThrow(COLUMN_NAME)),
                    urlWeb = it.getString(it.getColumnIndexOrThrow(COLUMN_URL)),
                    phone = it.getString(it.getColumnIndexOrThrow(COLUMN_PHONE)),
                    email = it.getString(it.getColumnIndexOrThrow(COLUMN_EMAIL)),
                    products = it.getString(it.getColumnIndexOrThrow(COLUMN_PRODUCTS)),
                    classification = it.getString(it.getColumnIndexOrThrow(COLUMN_CLASSIFICATION))
                )
                empresasList.add(company)
            }
        }
        return empresasList
    }

    fun deleteCompany(id: Long): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun updateCompany(company: Company): Int {
        val db = this.writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_NAME, company.name)
            put(COLUMN_URL, company.urlWeb)
            put(COLUMN_PHONE, company.phone)
            put(COLUMN_EMAIL, company.email)
            put(COLUMN_PRODUCTS, company.products)
            put(COLUMN_CLASSIFICATION, company.classification)
        }

        return db.update(
            TABLE_NAME,
            values,
            "$COLUMN_ID = ?",
            arrayOf(company.id.toString())
        )
    }
}