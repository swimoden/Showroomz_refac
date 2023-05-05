package com.kuwait.showroomz.view.fragment

import android.content.Intent
import android.graphics.Color
import android.location.GpsStatus
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.*
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.extras.GPSTracker
import com.kuwait.showroomz.model.data.Brand
import com.kuwait.showroomz.model.data.ClickType
import com.kuwait.showroomz.model.data.Location
import com.kuwait.showroomz.model.data.Types
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.model.simplifier.BrandSimplifier
import com.kuwait.showroomz.model.simplifier.LocationSimplifier
import com.kuwait.showroomz.view.adapters.LocationsAdapter
import com.kuwait.showroomz.viewModel.LocationVM
import kotlinx.android.synthetic.main.fragment_maps.*


class MapsFragment : Fragment() {
    lateinit var binding: FragmentMapsBinding
    private lateinit var viewModel: LocationVM
    private var locations: List<Location> = arrayListOf()
    private var locArray: List<Location> = arrayListOf()
    private var quickService: List<Location> = arrayListOf()
    private var workShop: List<Location> = arrayListOf()
    private var mapFragment: SupportMapFragment? = null
    private lateinit var brand: BrandSimplifier
    private  var br:Brand?=null
    private val dialogCallback: BottomSheetDialog by lazy {
        BottomSheetDialog(
            requireContext(),
            R.style.BottomSheetDialog
        )
    }
    val successCallbackDialog: BottomSheetDialog by lazy {
        BottomSheetDialog(
            requireContext(),
            R.style.BottomSheetDialog
        )
    }
    private var allMarkersMap: HashMap<Marker, Int> = HashMap<Marker, Int>()
    lateinit var googleMap: GoogleMap
    private var showKuwait = true
    var dialog = SimpleDialog(null, null)
    var oldposition = 0

    private fun turnGPSOn() {

        GPSTracker(requireContext())

        //val intent1 = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
       // startActivity(intent1);
        /*val provider: String = Settings.Secure.getString(
            context?.contentResolver,
            Settings.Secure.LOCATION_PROVIDERS_ALLOWED
        )
        if (!provider.contains("gps")) { //if gps is disabled
            val poke = Intent()
            poke.setClassName(
                "com.android.settings",
                "com.android.settings.widget.SettingsAppWidgetProvider"
            )
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE)
            poke.data = Uri.parse("3")
            context?.sendBroadcast(poke)
        }*/
    }

    private val callback = OnMapReadyCallback { map ->

        map.mapType = GoogleMap.MAP_TYPE_NORMAL
        if (!showKuwait) {
            allMarkersMap = HashMap()
            var pos = 0
            for (location in locArray) {
                val loc = LocationSimplifier(location)
                val latLng = loc.latitude?.let { loc.longitude?.let { it1 -> LatLng(it, it1) } }
                val mark = latLng?.let { MarkerOptions().position(it).title(loc.name) }
                mark?.let{
                    val marker = map.addMarker(it)
                    marker?.let { allMarkersMap.put(it, pos) }
                    if (pos == 0) {
                        latLng?.let { CameraUpdateFactory.newLatLngZoom(it, 18F) }
                            ?.let { map.moveCamera(it) }
                    }
                }

                pos += 1
            }
            map.setOnMarkerClickListener { marker ->
                allMarkersMap[marker]?.let {
                    loc_recycler.smoothScrollToPosition(it)
                }
                true
            }
            googleMap = map
            locationAdapter.refresh(locArray)
        } else {
            showKuwait = false
            val kuwait =
                LatLngBounds(LatLng(28.5243622, 46.5526837), LatLng(30.1038082, 49.0046809))
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(kuwait, 0))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_maps, container, false)
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root
    }
    override fun onResume() {
        super.onResume()

    LogProgressRepository.logProgress(
        "Locations_screen",
        category = br?.category.toString() ,
        dealerData = br?.id ?: ""
    )

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(LocationVM::class.java)
        turnGPSOn()
        viewModel.verifyPhone.value = true
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        arguments?.let {
            val id = MapsFragmentArgs.fromBundle(it).brandataId
             br = MapsFragmentArgs.fromBundle(it).brand
            val ids = MapsFragmentArgs.fromBundle(it).ids
            br?.let { it1 ->
                BrandSimplifier(it1).category?.isCivilIdMandatory?.let { it1 ->
                    viewModel.isCivilIdMandatory.set(
                        it1
                    )
                }
            }
            if (ids.isNotEmpty()) {
                viewModel.refresh(ids.toList())
            } else {
                br?.let { br ->
                    brand = BrandSimplifier(br)
                }
                id?.let { id ->
                    val ids = id.split(",")
                    viewModel.refresh(ids)

                }
            }
            viewModel.getUser()
            loc_recycler.apply {
                layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = locationAdapter
            }
        }

        clickListener()
        observeBrands()
    }

    private val locationAdapter: LocationsAdapter by lazy {
        LocationsAdapter(locations) { loc, position, clickType ->
            if (clickType == ClickType.ITEM) {
                val latLng = loc.latitude?.let { loc.longitude?.let { it1 -> LatLng(it, it1) } }
                latLng?.let { CameraUpdateFactory.newLatLngZoom(it, 18F) }
                    ?.let { googleMap.moveCamera(it) }
                loc_recycler.smoothScrollToPosition(position)
                Handler(Looper.getMainLooper()).postDelayed({
                    if (position > oldposition){
                        loc_recycler.smoothScrollBy(80, 0)
                    }else{
                        loc_recycler.smoothScrollBy(-80, 0)
                    }
                    oldposition = position
                },400)

            }
            if (clickType == ClickType.CALLBACK) {
                viewModel.location =loc
                showCallbackDialog()
            }
        }
    }

    private fun showCallbackDialog() {
        viewModel.location.brand()?.id?.let { it1 -> viewModel.location.brand()?.cat?.id?.let { it2 -> LogProgressRepository.logProgress(
            "Location_callback_screen",
            category = it2,
            dealerData = it1
        )} }

        var binding: LocationCallbackBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.location_callback_bottom_sheet,
            null,
            false
        )
        binding.viewModel = viewModel
        dialogCallback.setContentView(binding.root)
        dialogCallback.setCanceledOnTouchOutside(false)
        dialogCallback.show()
        binding.exitBtn.setOnClickListener {
            dialogCallback.dismiss()

        }
        /*binding.okBtn.setOnClickListener {

        }*/

    }

    private fun clickListener() {
        back.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }
        workshop_btn.setOnClickListener {
            if (::brand.isInitialized) {
                brand.color?.let {
                    workshop_btn.setTextColor(Color.parseColor(it.toString()))
                } ?: run {
                    workshop_btn.setTextColor(Color.parseColor("#C7112A"))
                }

            }
            quick.setTextColor(resources.getColor(R.color.colorBlack))
            locArray = emptyList()
            locArray = workShop
            mapFragment?.getMapAsync(callback)

        }

        quick.setOnClickListener {
            if (::brand.isInitialized) {
                brand.color?.let {
                    quick.setTextColor(Color.parseColor(it.toString()))
                } ?: run {
                    quick.setTextColor(Color.parseColor("#C7112A"))
                }

            }
            workshop_btn.setTextColor(resources.getColor(R.color.colorBlack))
            locArray = emptyList()
            locArray = quickService
            mapFragment?.getMapAsync(callback)
        }
    }
    private fun showSuccessCallbackBottomSheet() {
        val bindingSuccess: CallbackSuccessBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.callback_success_bottom_sheet,
            null,
            false
        )
        successCallbackDialog.setContentView(bindingSuccess.root)
        successCallbackDialog.setCanceledOnTouchOutside(false)
        bindingSuccess.titleText.text = getString(R.string.done_successfully)
        bindingSuccess.messageText.text = getString(R.string.representative_contact)


        successCallbackDialog.show()

        bindingSuccess.exitBtn.setOnClickListener {
            dialogCallback.dismiss()
            successCallbackDialog.dismiss()

        }
    }
    private fun showErrorDialog(message: String) {
        val errorDialog =
            BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        val bindingError: ErrorBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.error_bottom_sheet,
            null,
            false
        )
        errorDialog.setContentView(bindingError.root)
        errorDialog.setCancelable(false)
        bindingError.titleText.text = getString(R.string.error)
        bindingError.messageText.text =
            message


        errorDialog.show()

        bindingError.exitBtn.setOnClickListener {
            errorDialog.dismiss()


        }

    }
    private fun observeBrands() {
         viewModel.noConnectionError.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
             if (it) {
                 DesignUtils.instance.showErrorDialog(requireContext(), null, null) {

                 }
             }
         })
          viewModel.successCallback.observe(viewLifecycleOwner, Observer {

              if (it) {
                  viewModel.successCallback.value = false
                  showSuccessCallbackBottomSheet()

              }
          })
        viewModel.civilIdError.observe(viewLifecycleOwner, Observer {
            if (it) {
                showErrorDialog(getString(R.string.invalid_civil_id))
            }
        })
        viewModel.phoneError.observe(viewLifecycleOwner, Observer {
            if (it) {
                showErrorDialog(getString(R.string.invalid_phone))
            }
        })
        viewModel.nameError.observe(viewLifecycleOwner, Observer {
            if (it) {
                showErrorDialog(getString(R.string.empty_name))
            }
        })
        viewModel.callbackLoading.observe(viewLifecycleOwner, Observer {
            if (it) {
                dialogCallback.dismiss()
                binding.progressCircularCallback.isVisible = true
                activity?.let { it1 -> DesignUtils.instance.disableUserInteraction(it1) }
            } else {
                binding.progressCircularCallback.isVisible = false
                activity?.let { it1 -> DesignUtils.instance.enableUserInteraction(it1) }
            }

        })
        viewModel.locations.observe(viewLifecycleOwner, Observer { locations ->
            this.locations = locations
            quickService = emptyList()
            workShop = emptyList()
            locArray = emptyList()
            if (::brand.isInitialized) {
                if (brand.hasModels!!) {
                    quickService =
                        locations.filter {
                            val type = LocationSimplifier(it).type
                            LocationSimplifier(it).type == Types.SHOWROOM
                        }
                } else {
                    quickService =
                        locations.filter { LocationSimplifier(it).type == Types.QUICKSERVICE }
                    workShop = locations.filter { LocationSimplifier(it).type == Types.WORKSHOP }
                }
                brand.color?.let {
                    quick.setTextColor(Color.parseColor(it.toString()))
                } ?: run {
                    quick.setTextColor(Color.parseColor("#C7112A"))
                }
            } else {
                quickService =
                    locations.filter { LocationSimplifier(it).type == Types.QUICKSERVICE }
                workShop = locations.filter { LocationSimplifier(it).type == Types.WORKSHOP }
                quick.setTextColor(resources.getColor(R.color.colorPrimary))
            }

            locArray = if (quickService.isNotEmpty()) quickService else workShop
            loc_recycler.isVisible = locArray.isNotEmpty()
            binding.emptyDataTxt.container.isVisible = locArray.isEmpty()
            topbar.isVisible = !(quickService.isEmpty() || workShop.isEmpty())
            mapFragment?.getMapAsync(callback)
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer { isloading ->
            if (isloading) print("Loading") else print("not loading")
            if (isloading) {
                binding.emptyDataTxt.container.isVisible = false
            }
        })

        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            print(error)
        })
        viewModel.empty.observe(viewLifecycleOwner, Observer {
            binding.emptyDataTxt.container.isVisible = it
        })
       /* viewModel.verifyPhone.observe(viewLifecycleOwner, Observer {
            if (it) {
                viewModel.phoneNumber.get()?.let { it1 ->
                    dialog = SimpleDialog.emptyInstance(it1, {
                        viewModel.verifyPhone.value = true
                        viewModel.requestCallback()
                        dialog.dismiss()
                    },{
                        viewModel.verifyPhone.value = false
                    })
                } ?: kotlin.run { return@Observer }
                fragmentManager?.let { dialog.show(it, SimpleDialog.TAG) }

            }
        })*/
    }


}