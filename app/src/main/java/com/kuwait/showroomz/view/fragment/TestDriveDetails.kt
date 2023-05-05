package com.kuwait.showroomz.view.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.FragmentTestDriveDetailsBinding
import com.kuwait.showroomz.model.data.TestDrive
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.model.simplifier.ModelSimplifier
import com.kuwait.showroomz.model.simplifier.TestDriveSimplifier


class TestDriveDetails : Fragment(), OnMapReadyCallback {
    lateinit var binding: FragmentTestDriveDetailsBinding
    private lateinit var testDrive: TestDrive
    private var simplifier:TestDriveSimplifier? = null
    private lateinit var map: GoogleMap
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_test_drive_details,
            container,
            false
        )
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root
    }

    override fun onResume() {
        super.onResume()
simplifier?.let {
    LogProgressRepository.logProgress(
        "Testdrive_details_screen",
        category = it.catId ?: "",
        dealerData = simplifier?.model?.brand?.id ?: "",
        modelData = simplifier?.model?.id ?: "")
}

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments.let {
             it?.let { it1 ->
               testDrive = TestDriveDetailsArgs.fromBundle(it1).testDrive
                 simplifier = TestDriveSimplifier(testDrive)
                 simplifier?.let {
                     binding.testDriveSimplifier = it
                     if (it.hasLocation) {
                         val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
                         mapFragment?.getMapAsync(this)
                     }
                 }

            }
        }

        onClickListener()

    }

    private fun onClickListener() {
        binding.modelDetail.setOnClickListener {
            testDrive.modelData?.let { it1 ->
                TestDriveDetailsDirections.actionShowModelDetail(
                    simplifier?.modelData,
                    null, null
                )
            }?.let { it2 -> Navigation.findNavController(it).navigate(it2) }
        }
        binding.backButton.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }
        binding.submitBtn.setOnClickListener {
            val gmmIntentUri = Uri.parse(
                "google.navigation:q=${testDrive.location?.address?.latitude},${testDrive.location?.address?.longitude}&mode=d"
            )
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            ContextCompat.startActivity(requireContext(), mapIntent, null)
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        if (p0 != null) {
            this.map = p0
            map.clear()
            if (testDrive.location != null)
                testDrive.location?.address?.latitude?.let {
                    testDrive.location?.address?.longitude?.let { it1 ->
                        LatLng(
                            it,
                            it1
                        )
                    }
                }
                    ?.let {
                        MarkerOptions()
                            .position(it)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.location_pointer))
                    }?.let {
                        map.addMarker(
                            it

                        )
                    }

            testDrive.location?.address?.latitude?.let {
                testDrive.location?.address?.longitude?.let { it1 ->
                    LatLng(
                        it, it1
                    )
                }
            }?.let { CameraUpdateFactory.newLatLngZoom(it, 16f) }?.let { map.moveCamera(it) }
        }
    }


}