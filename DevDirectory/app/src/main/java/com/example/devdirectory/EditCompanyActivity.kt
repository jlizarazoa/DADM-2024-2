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

class EditCompanyActivity : ComponentActivity() {

    private lateinit var name: EditText
    private lateinit var url: EditText
    private lateinit var phone: EditText
    private lateinit var email: EditText
    private lateinit var products: EditText
    private lateinit var classification: Spinner
    private lateinit var save: Button
    private lateinit var cancel: Button
    private lateinit var dbHelper: DatabaseHelper
    private var companyId: Long = 0

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_company)

        initializeViews()
        loadCompanyData()

        save.setOnClickListener { editCompany() }
        cancel.setOnClickListener { cancel() }
    }

    private fun initializeViews() {
        name = findViewById(R.id.etEditCompanyName)
        url = findViewById(R.id.etEditUrlWeb)
        phone = findViewById(R.id.etEditPhone)
        email = findViewById(R.id.etEditEmail)
        products = findViewById(R.id.etEditProducts)
        classification = findViewById(R.id.spEditClasificacion)
        save = findViewById(R.id.btnSaveEdit)
        cancel = findViewById(R.id.btnCancelEdit)
        dbHelper = DatabaseHelper(this)
    }

    private fun loadCompanyData() {
        companyId = intent.getLongExtra("company_id", 0)
        name.setText(intent.getStringExtra("company_name"))
        url.setText(intent.getStringExtra("company_url"))
        phone.setText(intent.getStringExtra("company_phone"))
        email.setText(intent.getStringExtra("company_email"))
        products.setText(intent.getStringExtra("company_products"))

        val classifications = resources.getStringArray(R.array.clasificacion_opciones)
        val classificationPosition = classifications.indexOf(intent.getStringExtra("company_classification"))
        if (classificationPosition != -1) {
            classification.setSelection(classificationPosition)
        }
    }

    private fun editCompany() {
        val updatedCompany = Company(
            id = companyId,
            name = name.text.toString(),
            urlWeb = url.text.toString(),
            phone = phone.text.toString(),
            email = email.text.toString(),
            products = products.text.toString(),
            classification = classification.selectedItem.toString()
        )

        val updatedRows = dbHelper.updateCompany(updatedCompany)
        if (updatedRows > 0) {
            Toast.makeText(this, "Company updated successfully", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Error updating company", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cancel() {
        finish()
    }

}