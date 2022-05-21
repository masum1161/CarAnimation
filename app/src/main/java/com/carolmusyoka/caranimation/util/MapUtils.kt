package com.carolmusyoka.caranimation.util

import android.content.Context
import android.graphics.*
import com.carolmusyoka.caranimation.R
import com.google.android.gms.maps.model.LatLng
import kotlin.math.abs
import kotlin.math.atan

object MapUtils {

    fun getCarBitmap(context: Context): Bitmap {
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_car)
        return Bitmap.createScaledBitmap(bitmap, 50, 100, false)
    }

    fun getOriginDestinationMarkerBitmap(): Bitmap {
        val height = 20
        val width = 20
        val bitmap = Bitmap.createBitmap(height, width, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
        canvas.drawRect(0F, 0F, width.toFloat(), height.toFloat(), paint)
        return bitmap
    }

    fun getRotation(start: LatLng, end: LatLng): Float {
        val latDifference: Double = abs(start.latitude - end.latitude)
        val lngDifference: Double = abs(start.longitude - end.longitude)
        var rotation = -1F
        when {
            start.latitude < end.latitude && start.longitude < end.longitude -> {
                rotation = Math.toDegrees(atan(lngDifference / latDifference)).toFloat()
            }
            start.latitude >= end.latitude && start.longitude < end.longitude -> {
                rotation = (90 - Math.toDegrees(atan(lngDifference / latDifference)) + 90).toFloat()
            }
            start.latitude >= end.latitude && start.longitude >= end.longitude -> {
                rotation = (Math.toDegrees(atan(lngDifference / latDifference)) + 180).toFloat()
            }
            start.latitude < end.latitude && start.longitude >= end.longitude -> {
                rotation =
                    (90 - Math.toDegrees(atan(lngDifference / latDifference)) + 270).toFloat()
            }
        }
        return rotation
    }

    /**
     * This function returns the list of locations of Car during the trip i.e. from Origin to Destination
     */
    fun getListOfLocations(): ArrayList<ArrayList<LatLng>> {
        val locationList = ArrayList<ArrayList<LatLng>>()
        var location1 = ArrayList<LatLng>()
        location1.add(LatLng(-1.286194,36.888000))
        location1.add(LatLng(-1.286666,36.888761))
        location1.add(LatLng(-1.286880,36.889534))
        location1.add(LatLng(-1.287245,36.890329))
        location1.add(LatLng(-1.287760,36.891273))
        location1.add(LatLng(-1.288596,36.890844))
        location1.add(LatLng(-1.289755,36.891059))
        location1.add(LatLng(-1.290741,36.891316))
        location1.add(LatLng(-1.291535,36.891510))


        val location2 = ArrayList<LatLng>()

        location2.add(LatLng(-1.286680,36.888700))
        location2.add(LatLng(-1.286870,36.889954))
        location2.add(LatLng(-1.287250,36.891484))
        location2.add(LatLng(-1.287765,36.891589))
        location2.add(LatLng(-1.288580,36.891866))
        location2.add(LatLng(-1.289740,36.892098))
        location2.add(LatLng(-1.290745,36.893334))
        location2.add(LatLng(-1.291535,36.894543))
        location2.add(LatLng(-1.291322,36.895190))

        locationList.add(location1)
        locationList.add(location2)

        return locationList
    }

}