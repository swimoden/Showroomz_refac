package com.kuwait.showroomz.view.fragment

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.*
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.data.*
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.model.simplifier.ClientVehicleSimplifier
import com.kuwait.showroomz.model.simplifier.ModelSimplifier
import com.kuwait.showroomz.model.simplifier.TrimSimplifier
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.view.adapters.ActionsAdapter
import com.kuwait.showroomz.view.adapters.CarAppraisalAdapter
import com.kuwait.showroomz.view.adapters.CarImageAdapter
import com.kuwait.showroomz.view.adapters.OptionsAdapter
import com.kuwait.showroomz.viewModel.CarDetailsVM
import kotlinx.android.synthetic.main.fragment_your_car_details.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*


class YourCarDetailsFragment : Fragment() {
    private lateinit var binding: FragmentYourCarDetailsBinding
    private lateinit var viewModel: CarDetailsVM
    private val PICK_IMAGE = 1
    private val REQUEST_IMAGE_CAPTURE = 2

    val uriPathHelper = URIPathHelper()
    var index = -1

    private val imageAdapter: CarImageAdapter by lazy {
        CarImageAdapter(arrayListOf()) {
            imageAdapter.removeimage(it)
            viewModel.imagesArray.removeAt(it)
        }
    }
    private val optionsAdapter: OptionsAdapter by lazy {
        OptionsAdapter(arrayListOf(), context, viewModel)
    }
    private val vehicles: CarAppraisalAdapter by lazy {
        CarAppraisalAdapter(arrayListOf())
    }
    private val contactDialogRequest: BottomSheetDialog by lazy {
        BottomSheetDialog(
            requireContext(),
            R.style.BottomSheetDialog
        )
    }
    var model: Model? = null
    val local = LocalRepo()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        model?.let { model ->
            LogProgressRepository.logProgress(
                "Add_or_select_car_toappraisal_screen",
                category = ModelSimplifier(model).brand?.cat?.id ?: "",
                dealerData = ModelSimplifier(model).brand?.id ?: "",
                modelData = ModelSimplifier(model).id
            )
        } ?: run {
            LogProgressRepository.logProgress("Add_or_select_car_toappraisal_screen")
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentYourCarDetailsBinding.inflate(inflater, container, false)
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
       // lifecycleScope.launch {
            //delay(500)
        viewModel = ViewModelProviders.of(this).get(CarDetailsVM::class.java)
        binding.viewModel = viewModel
            viewModel.getUserCars()
        viewModel.uri1?.let{
            add_image_1.setImageURI(it)
        }
        viewModel.uri2?.let{
            add_image_2.setImageURI(it)
        }
        viewModel.uri3?.let{
            add_image_3.setImageURI(it)
        }
        viewModel.uri4?.let{
            add_image_4.setImageURI(it)
        }

            DesignUtils.instance.setStatusBar(
                requireActivity() as MainActivity,
                false,
                Color.parseColor("#C7112A")
            )
            binding.topContainer.setBackgroundColor(Color.parseColor("#C7112A"))

            arguments.let {
                YourCarDetailsFragmentArgs.fromBundle(it!!).model?.let {
                    model = it
                    model?.let {
                        binding.model = ModelSimplifier(it)
                        viewModel.modelToBuy = model

                    }
                }
            }

        binding.photosRecycler.apply {
            adapter = imageAdapter
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
        binding.optionsRecycler.apply {
            adapter = optionsAdapter
            layoutManager = LinearLayoutManager(context)
        }
        binding.modelsList.apply {
            adapter = vehicles
            layoutManager = LinearLayoutManager(context)
        }
        onClickListener()
        observeData()
      //  }
    }

    private fun observeData() {

        viewModel.vehicles.observe(viewLifecycleOwner, Observer {
            binding.selectContainer.isVisible = it.isNotEmpty()
            vehicles.refresh(it)
        })

        viewModel.options.observe(viewLifecycleOwner, Observer {
            optionsAdapter.refreshActions(it)
            binding.optionsContainer.isVisible = it.isNotEmpty()
        })
        viewModel.successRequestClientVehicle.observe(viewLifecycleOwner, Observer {
            if (it) showRequestBottomSheet()
        })

        viewModel.uploadSuccess.observe(viewLifecycleOwner, Observer {
            if (!it) {
                if (index == -1) {
                    imageAdapter.removeimage()
                }else{
                    index = -1
                }
            }
        })

        viewModel.successCallback.observe(viewLifecycleOwner, Observer {
            if (it) showSuccessDialog()
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
        viewModel.emailError.observe(viewLifecycleOwner, Observer {
            if (it) {
                showErrorDialog(getString(R.string.invalid_email))
            }
        })
        viewModel.imageError.observe(viewLifecycleOwner, Observer {
            if (it) {
                showErrorDialog(getString(R.string.select_images))
                viewModel.imageError.value = false
            }
        })
        viewModel.branError.observe(viewLifecycleOwner, Observer {
            if (it) {
                showErrorDialog(getString(R.string.select_brand))
                viewModel.branError.value = false
            }
        })
        viewModel.modelError.observe(viewLifecycleOwner, Observer {
            if (it) {
                showErrorDialog(getString(R.string.select_model))
                viewModel.modelError.value = false
            }
        })
        viewModel.yearError.observe(viewLifecycleOwner, Observer {
            if (it) {
                showErrorDialog(getString(R.string.select_year))
                viewModel.yearError.value = false
            }
        })
        viewModel.engineError.observe(viewLifecycleOwner, Observer {
            if (it) {
                showErrorDialog(getString(R.string.type_engine))
                viewModel.engineError.value = false
            }

        })
        viewModel.cylinderError.observe(viewLifecycleOwner, Observer {
            if (it) {
                showErrorDialog(getString(R.string.select_cylinder))
                viewModel.cylinderError.value = false
            }
        })
        viewModel.mileageError.observe(viewLifecycleOwner, Observer {
            if (it) {
                showErrorDialog(getString(R.string.type_meilage))
                viewModel.mileageError.value = false
            }
        })
        viewModel.conditionError.observe(viewLifecycleOwner, Observer {
            if (it) {
                showErrorDialog(getString(R.string.select_car_condition))
                viewModel.conditionError.value = false
            }
        })
        viewModel.descriptionError.observe(viewLifecycleOwner, Observer {
            if (it) {
                showErrorDialog(getString(R.string.enter_desc))
                viewModel.descriptionError.value = false
            }
        })


        viewModel.paintError.observe(viewLifecycleOwner, Observer {
            if (it) {
                showErrorDialog(getString(R.string.type_paint))
                viewModel.paintError.value = false
            }
        })
        viewModel.chasisError.observe(viewLifecycleOwner, Observer {
            if (it) {
                showErrorDialog(getString(R.string.chassis_condition))
                viewModel.chasisError.value = false
            }
        })
        viewModel.scratchError.observe(viewLifecycleOwner, Observer {
            if (it) {
                showErrorDialog(getString(R.string.scratch_on_paint))
                viewModel.scratchError.value = false
            }
        })
        viewModel.serviceError.observe(viewLifecycleOwner, Observer {
            if (it) {
                showErrorDialog(getString(R.string.regular_service_in_dealer))
                viewModel.serviceError.value = false
            }
        })

        Navigation.findNavController(requireView()).currentBackStackEntry?.savedStateHandle?.getLiveData<AppraisalInfo>(
            "brand"
        )?.observe(viewLifecycleOwner,
            Observer { brand ->
                if (brand != null) {
                     viewModel.brand = brand
                    val lang = if (isEnglish) brand.translations?.en else brand.translations?.ar
                    val name = lang?.let {
                        it.name
                    } ?: run {
                        ""
                    }
                    viewModel.brandName = name
                    viewModel.modelName.set("")
                    binding.brandEdit.setText(name)
                    // viewModel.model=null
                    binding.modelEdit.text.clear()
                    binding.modelConstraint.isVisible = true
                    binding.modelEdit.isEnabled = false
                }
            })

        Navigation.findNavController(requireView()).currentBackStackEntry?.savedStateHandle?.getLiveData<String>(
            "brandName"
        )?.observe(viewLifecycleOwner,
            Observer { brand ->
                if (brand != null) {
                    viewModel.brand = null
                    viewModel.brandName = brand
                    viewModel.modelName.set("")
                    binding.brandEdit.setText(brand)
                    // viewModel.model=null
                    binding.modelEdit.text.clear()
                    binding.modelEdit.hint = getString(R.string.enter_model_name)
                    binding.modelConstraint.isVisible = false
                    binding.modelEdit.isEnabled = true
                }
            })


        Navigation.findNavController(requireView()).currentBackStackEntry?.savedStateHandle?.getLiveData<AppraisalInfo>(
            "model"
        )?.observe(viewLifecycleOwner,
            Observer { model ->
                if (model != null) {
                    //viewModel.model = model
                    val lang = if (isEnglish) model.translations?.en else model.translations?.ar
                    val name = lang?.let {
                        it.name
                    } ?: run {
                        ""
                    }
                    viewModel.modelName.set(name)
                    binding.modelEdit.setText(name)
                }
            })
        Navigation.findNavController(requireView()).currentBackStackEntry?.savedStateHandle?.getLiveData<String>(
            "modelName"
        )?.observe(viewLifecycleOwner,
            Observer { model ->
                if (model != null) {
                    // viewModel.model = null
                    viewModel.modelName.set(model)
                    binding.modelEdit.setText(model)
                }
            })
        Navigation.findNavController(requireView()).currentBackStackEntry?.savedStateHandle?.getLiveData<String>(
            "year"
        )?.observe(viewLifecycleOwner,
            Observer { year ->
                if (year != null) {
                    viewModel.year = year
                    binding.yearEdit.setText(year)
                }
            })
        Navigation.findNavController(requireView()).currentBackStackEntry?.savedStateHandle?.getLiveData<String>(
            "cylinder"
        )?.observe(viewLifecycleOwner,
            Observer { cylinder ->
                if (cylinder != null) {
                    viewModel.cylinders = cylinder
                    binding.cylinderEdit.setText(cylinder)
                }
            })
        Navigation.findNavController(requireView()).currentBackStackEntry?.savedStateHandle?.getLiveData<String>(
            "condition"
        )?.observe(viewLifecycleOwner,
            Observer { condition ->
                if (condition != null) {
                    viewModel.conditions = condition
                    binding.conditionEdit.setText(condition)
                }
            })

        Navigation.findNavController(requireView()).currentBackStackEntry?.savedStateHandle?.getLiveData<String>(
            "chassis_condition"
        )?.observe(viewLifecycleOwner,
            Observer { condition ->
                if (condition != null) {
                    viewModel.chasis.set(condition)
                    binding.chasisEdit.setText(condition)
                }
            })

        viewModel.loading.observe(viewLifecycleOwner, Observer {
            if (it) {
                binding.progressCircular.isVisible = true
                DesignUtils.instance.disableUserInteraction(requireActivity())
                contactDialogRequest.dismiss()
            } else {
                binding.progressCircular.isVisible = false
                DesignUtils.instance.enableUserInteraction(requireActivity())
            }
        })
        viewModel.noConnectionError.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it) {
                DesignUtils.instance.showErrorDialog(requireContext(), null, null) {}
            }
        })
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
        bindingError.messageText.text = message
        errorDialog.show()

        bindingError.exitBtn.setOnClickListener {
            errorDialog.dismiss()
        }

    }

    private fun onClickListener() {
        add_image_1.setOnClickListener {
            index = 0
            showPickFileBottomSheet()
        }
        add_image_2.setOnClickListener {
            index = 1
            showPickFileBottomSheet()
        }
        add_image_3.setOnClickListener {
            index = 2
            showPickFileBottomSheet()
        }
        add_image_4.setOnClickListener {
            index = 3
            showPickFileBottomSheet()
        }
        vehicles.setOnItemCLickListener(object : CarAppraisalAdapter.OnItemClickListener {
            override fun onItemClick(car: ClientVehicleSimplifier) {
                setCarInfo(car)
                Navigation.findNavController(requireView())
                    .navigate(
                        YourCarDetailsFragmentDirections.showUserCarDetails(
                            car.clientVehicle,
                            model
                        )
                    )
            }

        })

        binding.showUserCars.setOnClickListener {
            binding.modelsList.isVisible = !binding.modelsList.isVisible
        }
        binding.addImage.setOnClickListener {
            showPickFileBottomSheet()
            //pickFromGallery()
        }
        binding.brandConstraint.setOnClickListener { component ->
            Navigation.findNavController(component)
                .navigate(YourCarDetailsFragmentDirections.showBrandList())

        }
        binding.modelConstraint.setOnClickListener {
            if (!viewModel.brand.isNull()) {

                Navigation.findNavController(it)
                    .navigate(YourCarDetailsFragmentDirections.showModelList(viewModel.brand))
            } else {
                viewModel.brandName?.let {
                    //Toast.makeText(requireContext(), "test", Toast.LENGTH_SHORT).show()
                } ?: run {
                    showErrorDialog(getString(R.string.choose_car_brand_first))
                }

            }

        }
        binding.yearConstraint.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(YourCarDetailsFragmentDirections.showYearList())
        }
        binding.cylinderConstraint.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(YourCarDetailsFragmentDirections.showCylinderList())
        }
        binding.conditionConstraint.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(YourCarDetailsFragmentDirections.showCarConditionsList(0))
        }

        binding.conditionChasisConstraint.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(YourCarDetailsFragmentDirections.showCarConditionsList(1))
        }

        binding.backBtn.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }

        binding.requestPriceBtn.setOnClickListener {
            viewModel.requestForPrice()
        }

        binding.yesCheckbox.setOnCheckedChangeListener { compoundButton, isChecked ->
            viewModel.scratch = if (isChecked) getString(R.string.scratch_available) else getString(
                R.string.scratch_not_available
            )
            binding.noCheckbox.isChecked = false
        }
        binding.noCheckbox.setOnCheckedChangeListener { compoundButton, isChecked ->
            viewModel.scratch =
                if (!isChecked) getString(R.string.scratch_available) else getString(
                    R.string.scratch_not_available
                )
            binding.yesCheckbox.isChecked = false
        }

        binding.yesCheckboxService.setOnCheckedChangeListener { compoundButton, isChecked ->
            viewModel.regularService =
                if (isChecked) getString(R.string.service_regulare_txt) else getString(
                    R.string.service_not_regular
                )
            binding.noCheckboxService.isChecked = false
        }

        binding.noCheckboxService.setOnCheckedChangeListener { compoundButton, isChecked ->
            viewModel.regularService =
                if (!isChecked) getString(R.string.service_regulare_txt) else getString(
                    R.string.service_not_regular
                )
            binding.yesCheckboxService.isChecked = false
        }

    }

    private fun setCarInfo(car: ClientVehicleSimplifier) {
        viewModel.selectedOptions?.clear()
        car.options.forEach {
            viewModel.selectedOptions?.add(it)
        }

        optionsAdapter.notifyDataSetChanged()
        viewModel.clientVehicle = car.clientVehicle
//        binding.modelEdit.setText(car.model?.name)
//        viewModel.model=car.mModel
//        viewModel.brand= car.model?.mBrand
//        binding.brandEdit.setText(car.model?.brand?.name)
//        binding.yearEdit.setText(car.year)
//        binding.engineEdit.setText(car.engine)
//        binding.cylinderEdit.setText(car.cylinders)
//        binding.mileageEdit.setText(car.mileage)
//        binding.conditionEdit.setText(car.condition)
//        binding.descriptionEdit.setText(car.description)
    }

    private fun showRequestBottomSheet() {
        model?.let { model ->
            LogProgressRepository.logProgress(
                "Request_appraisal_screen",
                category = ModelSimplifier(model).brand?.cat?.id ?: "",
                dealerData = ModelSimplifier(model).brand?.id ?: "",
                modelData = ModelSimplifier(model).id
            )
        } ?: run {
            LogProgressRepository.logProgress("Request_appraisal_screen")
        }

        val contactBottomSheetBinding: ApparasialCarContactBottomSheetBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(requireContext()),
                R.layout.apparasial_car_contact_bottom_sheet,
                null,
                false
            )
        contactBottomSheetBinding.viewModel = viewModel
        contactDialogRequest.setContentView(contactBottomSheetBinding.root)
        contactDialogRequest.setCancelable(false)
        contactDialogRequest.show()
        contactBottomSheetBinding.exitBtn.setOnClickListener {
            contactDialogRequest.dismiss()
        }
        contactBottomSheetBinding.okBtn.setOnClickListener {
            viewModel.appraisalRequest()
            DesignUtils.instance.hideKeyboard(requireActivity())
        }
    }

    private val actionsAdapter: ActionsAdapter by lazy {
        ActionsAdapter(arrayListOf(), 1, 0)
    }

    private fun showSuccessDialog() {
        var dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        var bottomSheetBinding: ApparasialCarSuccessRequestBottomSheetBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(requireContext()),
                R.layout.apparasial_car_success_request_bottom_sheet,
                null,
                false
            )

        dialog.setContentView(bottomSheetBinding.root)
        dialog.show()
        dialog.setCancelable(false)
        bottomSheetBinding.actionRecycler.apply {
            adapter = actionsAdapter
            layoutManager = GridLayoutManager(context, 3)
        }
        model?.let { model ->
            val simplifier = ModelSimplifier(model)
            val listActions = simplifier.actions.filter { it.identifier.contains("with_appraisal") }
            if (simplifier.brand?.color != null) {
                simplifier.brand?.color?.let {
                    actionsAdapter.refreshActions(
                        listActions,
                        Color.parseColor(it.toString())
                    )
                }
            } else {
                actionsAdapter.refreshActions(listActions, resources.getColor(R.color.colorDialog))
            }
        }


        //listActions?.let { simplifier.modelData?.brand?.category?.let { it1 -> actionsAdapter.refreshActions(it,  it1?.setBgColor()) } }

        /* model.actions.forEach { action ->
             if (action?.identifier.equals("finance_with_appraisal")) bottomSheetBinding.finance.isVisible = true
             if (action?.identifier.equals("book_now_with_appraisal")) bottomSheetBinding.bookNow.isVisible = true
             if (action?.identifier.equals("buy_now_with_appraisal")) bottomSheetBinding.buyNow.isVisible = true
         }*/
        actionsAdapter.setOnItemCLickListener(object : ActionsAdapter.OnItemClickListener {
            override fun onItemClick(action: Action?) {
                if (action?.identifier.equals("finance_with_appraisal")) {
                    val trim = Trim()
                    model?.priceFloat?.toInt()?.let {
                        trim.price=it
                    }
                    dialog.dismiss()
                    Navigation.findNavController(requireView())
                        .popBackStack(R.id.yourCarDetailsFragment, true)
                    Navigation.findNavController(requireView()).navigate(
                        YourCarDetailsFragmentDirections.actionShowFinance(
                            TrimSimplifier(trim), null, model!!
                        )
                    )
                    return
                }
                if (action?.identifier.equals("book_now_with_appraisal")) {
                    val trim = Trim()
                    model?.priceFloat?.toInt()?.let {
                        trim.price=it
                    }
                    dialog.dismiss()
                    Navigation.findNavController(requireView())
                        .popBackStack(R.id.yourCarDetailsFragment, true)
                    Navigation.findNavController(requireView()).navigate(
                        YourCarDetailsFragmentDirections.actionShowBook(
                            model!!,
                            trim,
                            null
                        )
                    )
                    return
                }

                if (action?.identifier.equals("buy_now_with_appraisal")) {
                    val trim = Trim()
                    model?.priceFloat?.toInt()?.let {
                        trim.price=it
                    }
                    dialog.dismiss()
                    Navigation.findNavController(requireView())
                        .popBackStack(R.id.yourCarDetailsFragment, true)
                    Navigation.findNavController(requireView())
                        .navigate(YourCarDetailsFragmentDirections.actionShowBuyNow(model!!, trim))
                    return
                } else {
                    showErrorDialog("this feature is not available in current version please update the app")
                }


            }

        })
        bottomSheetBinding.exitBtn.setOnClickListener {
            dialog.dismiss()
            Navigation.findNavController(requireView()).navigateUp()
        }


    }

    /* private fun showPickFileBottomSheet() {
         val pickFileDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
         val bindingPickFile: PickFileBottomSheetBinding = DataBindingUtil.inflate(
             LayoutInflater.from(requireContext()),
             R.layout.pick_file_bottom_sheet,
             null,
             false
         )
         pickFileDialog.setContentView(bindingPickFile.root)
         pickFileDialog.setCanceledOnTouchOutside(false)
         pickFileDialog.show()
         bindingPickFile.exitBtn.setOnClickListener {
             pickFileDialog.dismiss()
         }
         bindingPickFile.cameraTxt.setOnClickListener {
             pickImage()
             pickFileDialog.dismiss()
         }
         bindingPickFile.galleryTxt.setOnClickListener {

             pickFromGallery()
             pickFileDialog.dismiss()
         }
     }

     private fun pickImage() {
         if (ActivityCompat.checkSelfPermission(
                 requireActivity(),
                 Manifest.permission.CAMERA
             ) == PackageManager.PERMISSION_GRANTED
         ) {

             val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
             startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)
         } else {
             ActivityCompat.requestPermissions(
                 requireActivity(),
                 arrayOf(Manifest.permission.CAMERA),
                 REQUEST_IMAGE_CAPTURE
             )
         }
     }*/

    private fun pickFromGallery() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI
            )
            intent.type = "image/*"

            startActivityForResult(intent, PICK_IMAGE)
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PICK_IMAGE
            )
            PermissionUtils.setNBR(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private var fileUri: Uri? = null
    fun getPathFromUri(contentUri: Uri?): String? {
        val imagePath: String
        var  cursor: Cursor? = null
        return try {
            cursor= contentUri?.let {
                requireActivity().contentResolver.query(
                    it, null, null,
                    null, null
                )
            }
            val index = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor?.moveToFirst()
            imagePath = index?.let { cursor?.getString(it) }.toString()
            cursor?.close()
            Log.i("imagePath", imagePath)
            imagePath
        } catch (e: java.lang.Exception) {
            cursor?.close()
            null
        }
    }

    private fun initImageUri() {
        val cv = ContentValues()
        val simpleDateFormat = SimpleDateFormat(
            "yyyy-mm-dd-hh-MM-ss",
            Locale.ENGLISH
        )
        val name: String = ("img-"
                + simpleDateFormat.format(Calendar.getInstance().time)
                ).toString() + "_" + Random().nextInt(100).toString() + ".jpg"
        cv.put(MediaStore.Images.Media.TITLE, name)
        fileUri = requireActivity().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv
        )
    }


    private fun clickPicturesThroughCamera(request: Int) {
        try {
            initImageUri()
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
            startActivityForResult(intent, request)
        } catch (e: java.lang.Exception) {
            val x = e.localizedMessage
            print(e.localizedMessage)
            if (e.localizedMessage.contains("WRITE_EXTERNAL_STORAGE")){
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    200
                )
            }
        }
    }

    private fun pickImage(request: Int) {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                clickPicturesThroughCamera(request)
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.CAMERA),
                    2
                )
            }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), request
            )
            PermissionUtils.setNBR(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
        }

    }

    private fun showPickFileBottomSheet() {
        val pickFileDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        val bindingPickFile: PickFileBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.pick_file_bottom_sheet,
            null,
            false
        )
        pickFileDialog.setContentView(bindingPickFile.root)
        pickFileDialog.setCanceledOnTouchOutside(false)
        pickFileDialog.show()
        bindingPickFile.exitBtn.setOnClickListener {
            pickFileDialog.dismiss()
            index = -1
        }
        bindingPickFile.cameraTxt.setOnClickListener {
            pickImage(REQUEST_IMAGE_CAPTURE)
            pickFileDialog.dismiss()
        }
        bindingPickFile.galleryTxt.setOnClickListener {

            pickFromGallery()
            pickFileDialog.dismiss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            val selectedImage = data!!.data
            val filePath = arrayOf(MediaStore.Images.Media.DATA)
            val c: Cursor? = context?.contentResolver?.query(
                selectedImage!!, filePath,
                null, null, null
            )
            c?.moveToFirst()
            val columnIndex: Int = c?.getColumnIndex(filePath[0])!!
            val filePathStr = c.getString(columnIndex)
            c.close()

            when (index) {
                0,-> {
                    data.data?.let {
                        add_image_1.setImageURI(it)
                        viewModel.uri1 = it
                    }
                    viewModel.uploadImageWithIndex(filePathStr, "image/*", index)
                }
                1 -> {
                    data.data?.let {
                        add_image_2.setImageURI(it)
                        viewModel.uri2 = it
                    }
                    viewModel.uploadImageWithIndex(filePathStr, "image/*", index)
                }
                2 -> {
                    data.data?.let {
                        add_image_3.setImageURI(it)
                        viewModel.uri3 = it
                    }
                    viewModel.uploadImageWithIndex(filePathStr, "image/*", index)
                }
                3 -> {
                    data.data?.let {
                        add_image_4.setImageURI(it)
                        viewModel.uri4 = it
                    }
                    viewModel.uploadImageWithIndex(filePathStr, "image/*", index)
                }
                else -> {
                    data.data?.let { imageAdapter.addImage(it) }
                    viewModel.uploadImage(filePathStr, "image/*")
                }
            }
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val filePath = getPathFromUri(fileUri)
            when (index) {
                0 -> {
                    fileUri?.let {
                        add_image_1.setImageURI(it)
                        viewModel.uri1 = it
                    }
                    filePath?.let {
                        viewModel.uploadImageWithIndex(
                            it,
                            "images/jpeg", index
                        )
                    }
                }
                1 -> {
                    fileUri?.let {
                        add_image_2.setImageURI(it)
                        viewModel.uri2 = it
                    }
                    filePath?.let {
                        viewModel.uploadImageWithIndex(
                            it,
                            "images/jpeg", index
                        )
                    }
                }
                2 -> {
                    fileUri?.let {
                        add_image_3.setImageURI(it)
                        viewModel.uri3 = it
                    }
                    filePath?.let {
                        viewModel.uploadImageWithIndex(
                            it,
                            "images/jpeg", index
                        )
                    }

                }
                3 -> {
                    fileUri?.let {
                        add_image_4.setImageURI(it)
                        viewModel.uri4 = it
                    }
                    filePath?.let {
                        viewModel.uploadImageWithIndex(
                            it,
                            "images/jpeg", index
                        )
                    }

                }
                else -> {
                    fileUri?.let { imageAdapter.addImage(it) }
                    filePath?.let {
                        viewModel.uploadImage(
                            it,
                            "images/jpeg"
                        )
                    }
                }
            }
        }
    }


    fun getImageUri(inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            context?.contentResolver,
            inImage,
            "Title",
            null
        )
        return Uri.parse(path)
    }


}