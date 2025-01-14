package com.example.devdirectory

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.devdirectory.database.Company

class CompanyAdapter(
    private var companies: MutableList<Company>,
    private val onEditClick: (Company) -> Unit,
    private val onDeleteClick: (Company) -> Unit
) : RecyclerView.Adapter<CompanyAdapter.CompanyViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun updateCompanies(newCompanies: List<Company>) {
        companies.clear()
        companies.addAll(newCompanies)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompanyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_company, parent, false)
        return CompanyViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompanyViewHolder, position: Int) {
        val company = companies[position]
        holder.bind(company)
    }

    override fun getItemCount() = companies.size

    inner class CompanyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.companyName)
        private val urlTextView: TextView = itemView.findViewById(R.id.companyUrlWeb)
        private val phoneTextView: TextView = itemView.findViewById(R.id.companyPhone)
        private val emailTextView: TextView = itemView.findViewById(R.id.companyEmail)
        private val productsTextView: TextView = itemView.findViewById(R.id.companyProducts)
        private val classificationTextView: TextView = itemView.findViewById(R.id.companyClassification)
        private val editButton: Button = itemView.findViewById(R.id.buttonEdit)
        private val deleteButton: Button = itemView.findViewById(R.id.buttonDelete)

        fun bind(company: Company) {
            nameTextView.text = company.name
            urlTextView.text = company.urlWeb
            phoneTextView.text = company.phone
            emailTextView.text = company.email
            productsTextView.text = company.products
            classificationTextView.text = company.classification

            editButton.setOnClickListener { onEditClick(company) }
            deleteButton.setOnClickListener { onDeleteClick(company) }
        }
    }
}