package com.kuwait.showroomz.view.fragment

import android.Manifest
import android.app.Activity
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.FragmentUserAddressMapsBinding
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.viewModel.UserAddressMapVM
import java.util.*


class UserAddressMapsFragment : Fragment(), OnMapReadyCallback,
    GoogleMap.OnCameraMoveListener, GoogleMap.OnCameraIdleListener {
    private lateinit var mMap: GoogleMap
    private val TAG = "UserAddressMapsFragment"
    private lateinit var binding: FragmentUserAddressMapsBinding
    private lateinit var viewModel: UserAddressMapVM
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var mLocationManager: LocationManager? = null
    private var location: Location? = null
    private val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 0
    private val MIN_TIME_BW_UPDATES: Float = 0F
    private var navigated = false
    private val mLocationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(loc: Location) {
            if (!navigated) {
                navigated = false
                location = loc
                location?.let {
                    mMap.clear()
                    mMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(loc.latitude, loc.longitude))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.location_pointer))

                    )

                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                loc.latitude,
                                loc.longitude
                            ), 18F
                        )
                    )
                }
            }
            //getCurrentLocation()
        }

        override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}
        override fun onProviderEnabled(s: String) {}
        override fun onProviderDisabled(s: String) {}
    }

    override fun onResume() {
        super.onResume()

        LogProgressRepository.logProgress("Pin_location_screen")
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_user_address_maps,
                container,
                false
            )
        Log.e(TAG, "onCreateView: ")
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(UserAddressMapVM::class.java)
        arguments.let {
            viewModel.from = it?.let { it1 -> UserAddressMapsFragmentArgs.fromBundle(it1).from }!!
        }
        initMap()
        Log.e(TAG, "onViewCreated: ")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())


        mLocationManager = requireActivity().getSystemService(LOCATION_SERVICE) as LocationManager
        /*if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) !== PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) !== PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                999
            )
            return
        }
        if (mLocationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mLocationManager!!.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                MIN_TIME_BW_UPDATES,
                mLocationListener
            )
        }
        if (mLocationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            mLocationManager!!.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                MIN_TIME_BW_UPDATES,
                mLocationListener
            )
        }*/
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            prompt()
        }
        // getCurrentLocation()


        onClickListener()



    }
    fun prompt(){
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val result: Task<LocationSettingsResponse> =
            LocationServices.getSettingsClient(requireActivity()).checkLocationSettings(builder.build())



        result.addOnCompleteListener { task ->
            try {
                val response: LocationSettingsResponse =
                    task.getResult(ApiException::class.java)!!
                getCurrentLocation()
                // All location settings are satisfied. The client can initialize location
                // requests here.
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->                             // Location settings are not satisfied. But could be fixed by showing the
                        // user a dialog.
                        try {
                            // Cast to a resolvable exception.
                            val resolvable = exception as ResolvableApiException
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            activity?.let {
                                resolvable.startResolutionForResult(
                                    it,
                                    LocationRequest.PRIORITY_HIGH_ACCURACY
                                )
                            }


                        } catch (e: SendIntentException) {
                            // Ignore the error.
                        } catch (e: ClassCastException) {
                            // Ignore, should be an impossible error.
                        }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            LocationRequest.PRIORITY_HIGH_ACCURACY -> when (resultCode) {
                Activity.RESULT_OK -> {
                    getCurrentLocation()
                    Log.i(TAG, "onActivityResult: GPS Enabled by user")
                }


                Activity.RESULT_CANCELED ->                 // The user was asked to change settings, but chose not to
                    Log.i(TAG, "onActivityResult: User rejected GPS request")
                else -> {
                }
            }


        }

    }

    private fun onClickListener() {
        binding.submitBtn.setOnClickListener {
            viewModel.saveAddress(mMap.cameraPosition.target)
            Navigation.findNavController(requireView()).previousBackStackEntry?.savedStateHandle?.set(
                "LOCATION",
                mMap.cameraPosition.target
            )
            Navigation.findNavController(requireView()).popBackStack()
        }
        binding.backBtn.setOnClickListener {
            Navigation.findNavController(requireView()).popBackStack()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.isNotEmpty())
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initMap()

                    if (ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        prompt()
                        if (mLocationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            mLocationManager!!.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                                MIN_TIME_BW_UPDATES,
                                mLocationListener
                            )
                        }
                        if (mLocationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                            mLocationManager!!.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                                MIN_TIME_BW_UPDATES,
                                mLocationListener
                            )
                        }

                    }
                }
        }

    }

    private fun initMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }


    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                999
            )
            return
        }

        if (mLocationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mLocationManager!!.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                MIN_TIME_BW_UPDATES,
                mLocationListener
            )
        }
        if (mLocationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            mLocationManager!!.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                MIN_TIME_BW_UPDATES,
                mLocationListener
            )
        }



        /*fusedLocationClient.lastLocation.addOnSuccessListener { location ->
//            this.location = location

            location?.let {
                mMap.clear()
                mMap.addMarker(
                    MarkerOptions()
                        .position(LatLng(location.latitude, location.longitude))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.location_pointer))

                )

                mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            location.latitude,
                            location.longitude
                        ), 18F
                    )
                );
            }

        }*/


    }


    override fun onMapReady(map: GoogleMap) {
        if (map != null) {
            mMap = map
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ), 101
                )
                return
            }

            mMap.isMyLocationEnabled = true
            mMap.setOnCameraIdleListener(this)
            mMap.setOnCameraMoveListener(this)
            getCurrentLocation()

        }
    }


    override fun onCameraMove() {
        mMap.clear()
        binding.locationMarker.visibility = View.VISIBLE
    }

    override fun onCameraIdle() {
        binding.locationMarker.visibility = View.GONE
        val markerOptions = MarkerOptions().position(mMap.cameraPosition.target)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.location_pointer))
        mMap.addMarker(markerOptions)
    }
}