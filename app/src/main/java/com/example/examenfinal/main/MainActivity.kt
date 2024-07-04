package com.example.examenfinal.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.examen.api.Api
import com.example.examenfinal.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.*
import android.location.Location
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import android.preference.PreferenceManager
import org.osmdroid.views.overlay.Marker
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory



class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var Christian_ejex: TextView
    private lateinit var Christian_ejey: TextView
    private lateinit var mapView: MapView
    var latitud: Double = 0.0
    var longitud: Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sample_main_activiti)

        Christian_ejex = findViewById(R.id.ejex)
        Christian_ejey = findViewById(R.id.ejey)
        mapView = findViewById(R.id.mapa)

        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1000)
        } else {
            startLocationUpdates()
        }

        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setBuiltInZoomControls(true)
        mapView.setMultiTouchControls(true)
        val mapController = mapView.controller
        mapController.setZoom(18.0)


        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(5000)
                val api = Api()
                val Datos_Christian = JsonObject(
                    mapOf(
                        "ejex" to JsonPrimitive(latitud),
                        "ejey" to JsonPrimitive(longitud)
                    )
                )
                try {
                    api.putDatosChristianG(Datos_Christian)
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Error al enviar datos", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun startLocationUpdates() {
        locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for (location in locationResult.locations){
                    updateUI(location)
                }
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    private fun updateUI(location: Location) {
        latitud = location.latitude
        longitud = location.longitude
        Christian_ejex.text = "Latitud = $latitud"
        Christian_ejey.text = "Longitud = $longitud"
        val mapController = mapView.controller
        mapController.setZoom(18.0)
        val geoPoint = GeoPoint(latitud, longitud)
        mapController.setCenter(geoPoint)

        mapView.overlays.clear() // Limpia marcadores anteriores si deseas solo mostrar el último
        val marker = Marker(mapView)
        marker.position = geoPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView.overlays.add(marker)
        mapView.invalidate() // Refresca el mapa para mostrar el marcador
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1000) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }
}