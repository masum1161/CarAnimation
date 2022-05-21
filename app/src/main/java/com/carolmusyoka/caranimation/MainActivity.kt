package com.carolmusyoka.caranimation

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.carolmusyoka.caranimation.util.AnimationUtils
import com.carolmusyoka.caranimation.util.MapUtils

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var defaultLocation: LatLng
    private var originMarker: Marker? = null
    private var destinationMarker: Marker? = null
    private var grayPolyline: Polyline? = null
    private var blackPolyline: Polyline? = null
    private lateinit var movingCabMarker: ArrayList<Marker?>
    private lateinit var previousLatLng: ArrayList<LatLng?>
    private lateinit var currentLatLng: ArrayList<LatLng?>
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun moveCamera(latLng: LatLng) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    private fun animateCamera(latLng: LatLng) {
        val cameraPosition = CameraPosition.Builder().target(latLng).zoom(15.5f).build()
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    private fun addCarMarkerAndGet(latLng: LatLng): Marker {
        val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(MapUtils.getCarBitmap(this))
        return googleMap.addMarker(
            MarkerOptions().position(latLng).flat(true).icon(bitmapDescriptor)
        )
    }

    private fun addOriginDestinationMarkerAndGet(latLng: LatLng): Marker {
        val bitmapDescriptor =
            BitmapDescriptorFactory.fromBitmap(MapUtils.getOriginDestinationMarkerBitmap())
        return googleMap.addMarker(
            MarkerOptions().position(latLng).flat(true).icon(bitmapDescriptor)
        )
    }

    private fun showDefaultLocationOnMap(latLng: LatLng) {
        moveCamera(latLng)
        animateCamera(latLng)
    }

    /**
     * This function is used to draw the path between the Origin and Destination.
     */
    private fun showPath(latLngListOfList: ArrayList<ArrayList<LatLng>>) {

        latLngListOfList.forEach {
            var latLngList = it
            val builder = LatLngBounds.Builder()
            for (latLng in latLngList) {
                builder.include(latLng)
            }
            val bounds = builder.build()
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 4))

            val polylineOptions = PolylineOptions()
            polylineOptions.color(Color.GRAY)
            polylineOptions.width(5f)
            polylineOptions.addAll(latLngList)
            grayPolyline = googleMap.addPolyline(polylineOptions)

            val blackPolylineOptions = PolylineOptions()
            blackPolylineOptions.color(Color.BLACK)
            blackPolylineOptions.width(5f)
            blackPolyline = googleMap.addPolyline(blackPolylineOptions)

            originMarker = addOriginDestinationMarkerAndGet(latLngList[0])
            originMarker?.setAnchor(0.5f, 0.5f)
            destinationMarker = addOriginDestinationMarkerAndGet(latLngList[latLngList.size - 1])
            destinationMarker?.setAnchor(0.5f, 0.5f)

            val polylineAnimator = AnimationUtils.polylineAnimator()
            polylineAnimator.addUpdateListener { valueAnimator ->
                val percentValue = (valueAnimator.animatedValue as Int)
                val index = (grayPolyline?.points!!.size) * (percentValue / 100.0f).toInt()
                blackPolyline?.points = grayPolyline?.points!!.subList(0, index)
            }
            polylineAnimator.start()
        }

    }

    /**
     * This function is used to update the location of the Cab while moving from Origin to Destination
     */
    private fun updateCarLocation(latLng: LatLng, index: Int) {

        if (movingCabMarker[index] == null) {
            movingCabMarker[index] = addCarMarkerAndGet(latLng)
        }
        if (previousLatLng[index] == null) {
            currentLatLng[index] = latLng
            previousLatLng[index] = currentLatLng[index]
            movingCabMarker[index]?.position = currentLatLng[index]
            movingCabMarker[index]?.setAnchor(0.5f, 0.5f)
            animateCamera(currentLatLng[index]!!)
        } else {
            previousLatLng[index] = currentLatLng[index]
            currentLatLng[index] = latLng
            val valueAnimator = AnimationUtils.carAnimator()
            valueAnimator.addUpdateListener { va ->
                if (currentLatLng[index] != null && previousLatLng[index] != null) {
                    val multiplier = va.animatedFraction
                    val nextLocation = LatLng(
                        multiplier * currentLatLng[index]!!.latitude + (1 - multiplier) * previousLatLng[index]!!.latitude,
                        multiplier * currentLatLng[index]!!.longitude + (1 - multiplier) * previousLatLng[index]!!.longitude
                    )
                    movingCabMarker[index]?.position = nextLocation
                    val rotation = MapUtils.getRotation(previousLatLng[index]!!, nextLocation)
                    if (!rotation.isNaN()) {
                        movingCabMarker[index]?.rotation = rotation
                    }
                    movingCabMarker[index]?.setAnchor(0.5f, 0.5f)
                    animateCamera(nextLocation)
                }
            }
            valueAnimator.start()
        }
    }

    private fun showMovingCab(cabLatLngList: ArrayList<ArrayList<LatLng>>) {
        handler = Handler()
        var index = 0

        cabLatLngList.forEach { _ ->
            movingCabMarker.add(null)
            previousLatLng.add(null)
            currentLatLng.add(null)
        }

        runnable = Runnable {
            run {
                if (index < 9) {
                    cabLatLngList.forEach {
                        updateCarLocation(it[index], cabLatLngList.indexOf(it))
                    }

                    handler.postDelayed(runnable, 3000)
                    ++index
                } else {
                    handler.removeCallbacks(runnable)
                    Toast.makeText(this@MainActivity, "Trip Ends", Toast.LENGTH_LONG).show()
                }
            }
        }
        handler.postDelayed(runnable, 5000)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        defaultLocation = LatLng(-1.286194,36.888000)
        showDefaultLocationOnMap(defaultLocation)

        movingCabMarker = ArrayList()
        previousLatLng = ArrayList()
        currentLatLng = ArrayList()

        Handler().postDelayed(Runnable {

            showPath(MapUtils.getListOfLocations())
            showMovingCab(MapUtils.getListOfLocations())

        }, 3000)
    }

}
