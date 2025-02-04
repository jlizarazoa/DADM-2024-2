package com.example.catalogo

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlaceAdapter(private var places: List<Place>) :
    RecyclerView.Adapter<PlaceAdapter.EmpresaViewHolder>() {

    class EmpresaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCode: TextView = view.findViewById(R.id.tvCodigo)
        val tvName: TextView = view.findViewById(R.id.tvNombre)
        val tvMun: TextView = view.findViewById(R.id.tvMunicipio)
        val tvPap: TextView = view.findViewById(R.id.tvPap)
        val tvDesc: TextView = view.findViewById(R.id.tvDescripcion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmpresaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_place, parent, false)
        return EmpresaViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmpresaViewHolder, position: Int) {
        val place = places[position]
        holder.tvName.text = place.name
        holder.tvCode.text = "Código DANE: ${place.code}"
        holder.tvMun.text = "Ciudad / Municipio: ${place.municipality}"
        holder.tvPap.text = "PAP: ${place.pap}"
        holder.tvDesc.text = "Descripción: ${place.description}"
    }

    override fun getItemCount(): Int = places.size

    @SuppressLint("NotifyDataSetChanged")
    fun actualizarLista(nuevasEmpresas: List<Place>) {
        places = nuevasEmpresas
        notifyDataSetChanged()
    }
}