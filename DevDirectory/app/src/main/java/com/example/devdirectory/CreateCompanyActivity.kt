package com.example.devdirectory

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.devdirectory.database.Company
import com.example.devdirectory.database.DatabaseHelper


class CreateCompanyActivity : ComponentActivity() {

    private lateinit var name: EditText
    private lateinit var url: EditText
    private lateinit var phone: EditText
    private lateinit var email: EditText
    private lateinit var products: EditText
    private lateinit var classification: Spinner
    private lateinit var save: Button
    private lateinit var cancel: Button
    private lateinit var dbHelper: DatabaseHelper

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_company)

        initializeViews()

        save.setOnClickListener { createCompany() }
        cancel.setOnClickListener { cancel() }
    }

    private fun initializeViews() {
        name = findViewById(R.id.etCompanyName)
        url = findViewById(R.id.etUrlWeb)
        phone = findViewById(R.id.etPhone)
        email = findViewById(R.id.etEmail)
        products = findViewById(R.id.etProducts)
        classification = findViewById(R.id.spClasificacion)
        save = findViewById(R.id.btnSave)
        cancel = findViewById(R.id.btnCancel)
        dbHelper = DatabaseHelper(this)
    }

    private fun createCompany() {
        val newCompany = Company(
            name = name.text.toString(),
            urlWeb = url.text.toString(),
            phone = phone.text.toString(),
            email = email.text.toString(),
            products = products.text.toString(),
            classification = classification.selectedItem.toString()
        )

        val id = dbHelper.createCompany(newCompany)
        if (id > 0) {
            Toast.makeText(this, "Company created successfully", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Error creating company", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cancel() {
        finish()
    }

}