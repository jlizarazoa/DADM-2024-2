package com.example.gpsapp

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat.getCurrentLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.gpsapp.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val PREFERENCES_NAME = "MapsPrefs"

class MapsActivity : AppCompatActivity(), GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener, OnMapReadyCallback,
    ActivityCompat.OnRequestPermissionsResultCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var selectedRadius: Int = 10
    private val selectedCategories = mutableListOf<String>()

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val DEFAULT_ZOOM = 15f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

        // Inicializar FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Inicializar binding
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar el fragmento del mapa
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupUIControls()
    }

    private fun setupUIControls() {
        val checkboxHospital = findViewById<CheckBox>(R.id.checkbox_hospital)
        val checkboxRestaurant = findViewById<CheckBox>(R.id.checkbox_restaurant)
        val checkboxTouristSpot = findViewById<CheckBox>(R.id.checkbox_tourist_spot)
        val seekBar = findViewById<SeekBar>(R.id.seekbar_radius)
        val radiusLabel = findViewById<TextView>(R.id.label_radius_value)
        val updateButton = findViewById<Button>(R.id.button_update)

        // Cargar el radio guardado en las preferencias
        selectedRadius = sharedPreferences.getInt("search_radius", 10)
        seekBar.progress = selectedRadius
        radiusLabel.text = "Radio: $selectedRadius km"

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                selectedRadius = progress
                radiusLabel.text = "Radio: $progress km"
                // Guardar el nuevo radio en las preferencias
                sharedPreferences.edit().putInt("search_radius", progress).apply()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        updateButton.setOnClickListener {
            selectedCategories.clear()
            if (checkboxHospital.isChecked) selectedCategories.add("hospital")
            if (checkboxRestaurant.isChecked) selectedCategories.add("restaurant")
            if (checkboxTouristSpot.isChecked) selectedCategories.add("tourist_attraction")

            if (selectedCategories.isEmpty()) {
                Toast.makeText(this, "Selecciona al menos un tipo de lugar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            focusOnCurrentLocation()
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        try {
            // Habilitar el botón de mi ubicación si los permisos están concedidos
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mMap.isMyLocationEnabled = true
                mMap.setOnMyLocationButtonClickListener(this)
                mMap.setOnMyLocationClickListener(this)
                focusOnCurrentLocation()
            } else {
                requestLocationPermission()
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Error al acceder a la ubicación", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onMapReady(mMap)
            } else {
                Toast.makeText(
                    this,
                    "Se requiere permiso de ubicación para mostrar lugares cercanos",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun focusOnCurrentLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val latLng = LatLng(location.latitude, location.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM))
                        getNearbyPlaces(location.latitude, location.longitude)
                    } ?: run {
                        Toast.makeText(
                            this,
                            "No se pudo obtener la ubicación actual",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                requestLocationPermission()
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Error al acceder a la ubicación", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getNearbyPlaces(lat: Double, lng: Double) {
        val radiusInMeters = selectedRadius * 1000 // Convertir km a metros
        val location = "$lat,$lng"
        val types = selectedCategories.joinToString("|")

        val apiKey = "AIzaSyA5mCPCwsujO8HfwwMwuZokSBPkpeY2W1Q"

        RetrofitInstance.api.getNearbyPlaces(
            location = location,
            radius = radiusInMeters,
            type = types,
            apiKey = apiKey
        ).enqueue(object : Callback<PlaceResponse> {
            override fun onResponse(call: Call<PlaceResponse>, response: Response<PlaceResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { placesResponse ->
                        mMap.clear()

                        val filteredPlaces = placesResponse.results.filter { place ->
                            val distance = FloatArray(1)
                            Location.distanceBetween(
                                lat, lng,
                                place.geometry.location.lat,
                                place.geometry.location.lng,
                                distance
                            )
                            distance[0] <= radiusInMeters
                        }

                        for (place in filteredPlaces) {
                            val placeLatLng = LatLng(
                                place.geometry.location.lat,
                                place.geometry.location.lng
                            )
                            mMap.addMarker(
                                MarkerOptions()
                                    .position(placeLatLng)
                                    .title(place.name)
                                    .snippet(place.vicinity)
                            )
                        }

                        if (filteredPlaces.isEmpty()) {
                            Toast.makeText(
                                this@MapsActivity,
                                "No se encontraron lugares cercanos",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        this@MapsActivity,
                        "Error al buscar lugares: ${response.errorBody()?.string()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<PlaceResponse>, t: Throwable) {
                Toast.makeText(
                    this@MapsActivity,
                    "Error de conexión: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    override fun onMyLocationButtonClick(): Boolean {
        focusOnCurrentLocation()
        return true
    }

    override fun onMyLocationClick(location: Location) {
        Toast.makeText(
            this,
            "Ubicación actual: ${location.latitude}, ${location.longitude}",
            Toast.LENGTH_LONG
        ).show()
    }
}