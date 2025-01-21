package com.example.devdirectory

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.devdirectory.database.Company
import com.example.devdirectory.database.DatabaseHelper

class MainActivity : ComponentActivity() {

    private lateinit var listCompanies: RecyclerView
    private lateinit var companies: MutableList<Company>
    private lateinit var filteredCompanies: MutableList<Company>
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: CompanyAdapter
    private lateinit var addButton: Button
    private lateinit var searchEdit: EditText
    private lateinit var filterSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupRecyclerView()
        setupFilters()
        loadCompanies()

    }

    private fun initializeViews() {
        listCompanies = findViewById(R.id.listCompanies)
        addButton = findViewById(R.id.addButton)
        searchEdit = findViewById(R.id.etSearch)
        filterSpinner = findViewById(R.id.spFilterClassification)
        dbHelper = DatabaseHelper(this)
        companies = mutableListOf()
        filteredCompanies = mutableListOf()

        addButton.setOnClickListener {
            startActivity(Intent(this, CreateCompanyActivity::class.java))
        }
    }

    private fun setupFilters() {
        searchEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterCompanies()
            }
        })

        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterCompanies()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                filterCompanies()
            }
        }
    }

    private fun filterCompanies() {
        val searchText = searchEdit.text.toString().lowercase()
        val selectedClassification = filterSpinner.selectedItem.toString()

        filteredCompanies = companies.filter { company ->
            val matchesSearch = company.name.lowercase().contains(searchText) || searchText.isEmpty()
            val matchesClassification = selectedClassification == "All" ||
                    company.classification == selectedClassification

            matchesSearch && matchesClassification
        }.toMutableList()

        adapter.updateCompanies(filteredCompanies)
    }

    private fun setupRecyclerView() {
        adapter = CompanyAdapter(
            companies = mutableListOf(),
            onEditClick = { company ->
                val intent = Intent(this, EditCompanyActivity::class.java)
                intent.putExtra("company_id", company.id)
                intent.putExtra("company_name", company.name)
                intent.putExtra("company_url", company.urlWeb)
                intent.putExtra("company_phone", company.phone)
                intent.putExtra("company_email", company.email)
                intent.putExtra("company_products", company.products)
                intent.putExtra("company_classification", company.classification)
                startActivity(intent)
            },
            onDeleteClick = { company ->
                showDeleteDialog(company)
            }
        )
        listCompanies.adapter = adapter
        listCompanies.layoutManager = LinearLayoutManager(this)
    }


    private fun loadCompanies() {
        companies = dbHelper.getCompanies().toMutableList()
        filterCompanies()
    }

    private fun showDeleteDialog(company: Company) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_confirm_delete)

        val btnDelete = dialog.findViewById<Button>(R.id.btnEliminar)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancelar)

        btnDelete.setOnClickListener {
            val deletedRows = dbHelper.deleteCompany(company.id)
            if (deletedRows > 0) {
                Toast.makeText(this, "Company deleted successfully", Toast.LENGTH_SHORT).show()
                loadCompanies()
            } else {
                Toast.makeText(this, "Error deleting company", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        loadCompanies()
    }
}
