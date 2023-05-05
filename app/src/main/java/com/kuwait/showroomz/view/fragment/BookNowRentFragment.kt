package com.kuwait.showroomz.view.fragment

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.*
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.data.Model
import com.kuwait.showroomz.model.data.Program
import com.kuwait.showroomz.model.data.Trim
import com.kuwait.showroomz.model.data.User
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.model.simplifier.ModelSimplifier
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.view.adapters.AddonsAdapter
import com.kuwait.showroomz.view.adapters.AddonsAmountAdapter
import com.kuwait.showroomz.viewModel.BookRentVM
import kotlinx.android.synthetic.main.email_update_dialog.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.text.isNullOrEmpty


class BookNowRentFragment : Fragment() {
    private val CIVIL_ID_CODE = 1
    private val LICENSE_CODE = 2
    lateinit var binding: FragmentBookNowRentBinding
    lateinit var viewModel: BookRentVM
    lateinit var model: Model
    lateinit var trim: Trim
    var programId:String = ""
    private var startDatePickerDialog: DatePickerDialog? = null
    private var endDatePickerDialog: DatePickerDialog? = null
    val prefs = Shared()
    private val addonsAdapter: AddonsAdapter by lazy {
        AddonsAdapter(listOf(), context, viewModel)

    }

    private val addonsAmountsAdapter: AddonsAmountAdapter by lazy {
        AddonsAmountAdapter(arrayListOf(), context)

    }
    override fun onResume() {
        super.onResume()

        LogProgressRepository.logProgress("Renting_screen",category = model.let {
            ModelSimplifier(
                it
            ).brand?.cat?.id
        }
            ?:"", dealerData =model.let { ModelSimplifier(it).brand?.id } ?:"",modelData = model.let {
            ModelSimplifier(it).id
        }
            ,trim = trim.id)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("BookNowRentFragment", "onCreate: ")
        (context as MainActivity).showTermsPopUps(""){
            if (it == 0){
                Navigation.findNavController(requireView()).navigateUp()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(BookRentVM::class.java)
        viewModel.getConnectedUser()

        arguments.let {
            model = it?.let { it1 -> BookNowRentFragmentArgs.fromBundle(it1).model }!!
            viewModel.getOffer(model.id)
             BookNowRentFragmentArgs.fromBundle(it).programId?.let {
                 programId = it
                 val local = LocalRepo()
                 val program = local.getOne<Program>(programId)

                 viewModel.endDate =  program?.let { it1 ->
                     val period = if (it1.contractPeriod == 31) 30 else it1.contractPeriod
                         Utils.instance.addDaysToDate(Calendar.getInstance().time, period)
                 }!!
            }
            trim = BookNowRentFragmentArgs.fromBundle(it).trim
            viewModel.trim = trim
            binding.model = ModelSimplifier(model)
            viewModel.model = model
            binding.viewModel = viewModel

        }

        viewModel.getAddons()
        DesignUtils.instance.setStatusBar(
            requireActivity() as MainActivity,
            false,
            ModelSimplifier(model).brand?.category?.setBgColor()
        )
        updateStartDateContainer(viewModel.startDate)
        updateEndDateContainer(viewModel.endDate)

        binding.addOnSRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = addonsAdapter
        }

        binding.addOnSList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = addonsAmountsAdapter
        }

        ModelSimplifier(model).brand?.id?.let { viewModel.getModelLocations(it) }
        onClickListener()
        observeData()
    }

    private fun updateStartDateContainer(time: Date) {

        val dayNbr = Utils.instance.dateToStringLocalized(time, "dd")
        val month = Utils.instance.dateToStringLocalized(time, "MMMM")
        val dayName = Utils.instance.dateToStringLocalized(time, "EEEE")
        binding.dayNbrText.text = dayNbr
        binding.month.text = month
        binding.day.text = dayName
        viewModel.startDate = time
        viewModel.updateHours()
        checkDates(0)

    }

    private fun updateEndDateContainer(time: Date) {
        if (time <= viewModel.startDate){
            showErrorBottomSheet(getString(R.string.end_date_should_be_bigger_than_start_date))
        } else {
            val dayNbr = Utils.instance.dateToStringLocalized(time, "dd")
            val month = Utils.instance.dateToStringLocalized(time, "MMMM")
            val dayName = Utils.instance.dateToStringLocalized(time, "EEEE")
            binding.endDayNbrText.text = dayNbr
            binding.endMonth.text = month
            binding.endDay.text = dayName
            viewModel.endDate = time
            viewModel.updateHours()
            checkDates(1)
        }
    }
    private fun checkDates(source:Int){
        if (viewModel.startDate < viewModel.endDate) {
            viewModel.calculateAmount()
        }else{
            if (source == 0){
                val newDate = Utils.instance.addDaysToDate(viewModel.startDate, 1)!!
                updateEndDateContainer(newDate)
            }

        }
    }
    private val dialogEmail: BottomSheetDialog by lazy {
        BottomSheetDialog(
            requireContext(),
            R.style.BottomSheetDialog
        )
    }

    fun showUpdateEmailDialog(ctx: Context, action: () -> Unit) {
        val binding: EmailUpdateDialogBinding = DataBindingUtil.inflate(
            LayoutInflater.from(ctx),
            R.layout.email_update_dialog,
            null,
            false
        )

        dialogEmail.setContentView(binding.root)
        dialogEmail.setCancelable(false)
        dialogEmail.ok_btn.setOnClickListener {
            if (Utils.instance.isValidEmail(dialogEmail.email_txt.text)) {
                viewModel.updateEmail(dialogEmail.email_txt.text.toString()) {
                    if (it) {
                        dialogEmail.dismiss()
                        action.invoke()
                    } else {
                        dialogEmail.email_txt.startAnimation(
                            AnimationUtils.loadAnimation(
                                requireContext(),
                                R.anim.shake
                            )
                        )
                    }
                }
            } else {
                dialogEmail.email_txt.startAnimation(
                    AnimationUtils.loadAnimation(
                        requireContext(),
                        R.anim.shake
                    )
                )
            }
        }

        dialogEmail.exit_btn.setOnClickListener {
            action.invoke()
            dialogEmail.dismiss()
        }
        dialogEmail.show()
    }
    private fun observeData() {
         viewModel.noConectionError.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it){
                DesignUtils.instance.showErrorDialog(requireContext(), null,null){

                }
            }
        })
        viewModel.successUploadCivilIdFile.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it){
                binding.uploadTextInputEditText.setTextColor(ContextCompat.getColor(requireContext(),R.color.colorLightGreen))
            }
        })
        viewModel.successUploadLicenseFile.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it){
                binding.uploadLicenseTextInputEditText.setTextColor(ContextCompat.getColor(requireContext(),R.color.colorLightGreen))
            }
        })
        viewModel.successUrl.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it.isNotEmpty()){
                Navigation.findNavController(requireView()).navigate(BookNowRentFragmentDirections.showPaymentWebFragment(it,model))
                viewModel.successUrl.value=""
            }
        })


        viewModel.workingHours.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
                requireContext(),
                R.layout.time_spinner_item, it
            )
            binding.pickUpTimeSpinner.adapter = adapter
            binding.dropOffTimeSpinner.adapter = adapter
        })
        viewModel.addons.observe(viewLifecycleOwner, androidx.lifecycle.Observer {

            addonsAdapter.refreshActions(it)
            binding.addOnContainer.visibility = if (it.isNotEmpty()) View.VISIBLE else View.GONE
        })
        Navigation.findNavController(requireView()).currentBackStackEntry?.savedStateHandle?.getLiveData<LatLng>(
            "LOCATION"
        )?.observe(viewLifecycleOwner,
            androidx.lifecycle.Observer { location ->
                viewModel.getLocation(location)
            })
        Navigation.findNavController(requireView()).currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(
            PAYMENT_RESULT
        )?.observe(viewLifecycleOwner,
            androidx.lifecycle.Observer { result ->
                if (result){
                    viewModel.requestPayment()
                }else showErrorBottomSheet()

            })
        viewModel.addonsFormula.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it.isNotEmpty()) {
                binding.driverBilling.visibility = View.VISIBLE
                addonsAmountsAdapter.refreshActions(it)
            } else {
                binding.driverBilling.visibility = View.GONE
            }
        })
        viewModel.success.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it){
                showSuccessBottomSheet()
            }
        })
        viewModel.error.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it){
                showErrorBottomSheet()
            }
        })


    }
    private fun showErrorBottomSheet(error:String = getString(R.string.booking_error) ) {
        val errorDialog =
            BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        val bindingSuccess: ErrorBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.error_bottom_sheet,
            null,
            false
        )
        errorDialog.setContentView(bindingSuccess.root)
        bindingSuccess.titleText.text = getString(R.string.error)
        bindingSuccess.messageText.text = error
            //getString(R.string.booking_error)


        errorDialog.show()

        bindingSuccess.exitBtn.setOnClickListener {
            errorDialog.dismiss()


        }

    }
    private fun showSuccessBottomSheet() {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        val binding: SuccessBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.success_bottom_sheet,
            null,
            false
        )
        dialog.setContentView(binding.root)
        binding.titleText.text = getString(R.string.form_submitted_successfully)
        binding.messageText.text = getString(R.string.form_submitted_successfully_message)
        dialog.show()
        binding.exitBtn.setOnClickListener {
            dialog.dismiss()
            Navigation.findNavController(requireView()).navigateUp()
        }
    }
    private fun onClickListener() {
        binding.knetId.setOnClickListener {
            viewModel.paymentMethod.set("knet")
        }
        binding.masterId.setOnClickListener {
            viewModel.paymentMethod.set("cc")
        }
        binding.payBtn.setOnClickListener {
            if (viewModel.fullAddressPickUp.get().isNullOrEmpty()) {
                binding.pickUpEditText.startAnimation(
                    AnimationUtils.loadAnimation(
                        activity,
                        R.anim.shake
                    )
                )
                return@setOnClickListener
            }
            if (viewModel.fullAddressDropOff.get().isNullOrEmpty()) {
                binding.dropOffEdit.startAnimation(
                    AnimationUtils.loadAnimation(
                        activity,
                        R.anim.shake
                    )
                )
                return@setOnClickListener
            }
            if (viewModel.civilIdUrl.isNullOrEmpty()) {
                binding.uploadCivilIdContainer.startAnimation(
                    AnimationUtils.loadAnimation(
                        activity,
                        R.anim.shake
                    )
                )
                return@setOnClickListener
            }
            if (viewModel.uploadLicenseUrl.isNullOrEmpty()) {
                binding.uploadCivilIdContainer.startAnimation(
                    AnimationUtils.loadAnimation(
                        activity,
                        R.anim.shake
                    )
                )
                return@setOnClickListener
            }

//            prefs.string(USER_ID)?.let {
//                val local = LocalRepo()
//                local.getOne<User>(it)?.let {
//                    if (it.email?.startsWith("+") == true) {
//                        showUpdateEmailDialog(requireActivity()) {
//                            viewModel.pay()
//                        }
//                    } else {
//                        viewModel.pay()
//                    }
//                } ?: run {
//                    viewModel.pay()
//                }
//            } ?: run {
                viewModel.pay()
           // }
        }
        binding.uploadLicenseImage.setOnClickListener {
            if (NetworkUtils.instance.connected) {
                showPickFileBottomSheet(LICENSE_CODE)
            }else{
                DesignUtils.instance.showErrorDialog(requireContext(), null, null){

                }
            }

        }
        binding.uploadImage.setOnClickListener {
            if (NetworkUtils.instance.connected) {
                showPickFileBottomSheet(CIVIL_ID_CODE)
            }else{
                DesignUtils.instance.showErrorDialog(requireContext(), null, null){

                }
            }

        }
        binding.pickUpTimeSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    Log.e(
                        "pickUpTimeSpinner",
                        "onItemSelected: ${viewModel.workingHours.value?.get(position)}"
                    )
                    viewModel.startTime = viewModel.workingHours.value?.get(position).toString()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }
        binding.dropOffTimeSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    Log.e(
                        "dropOffTimeSpinner",
                        "onItemSelected: ${viewModel.workingHours.value?.get(position)}"
                    )
                    viewModel.endTime = viewModel.workingHours.value?.get(position).toString()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }
        if (model.testDriveStatus != 4) {
            binding.pickUp.setOnClickListener {
                Navigation.findNavController(it).navigate(BookNowRentFragmentDirections.showMap(1))
            }
            binding.dropOff.setOnClickListener {
                Navigation.findNavController(it).navigate(BookNowRentFragmentDirections.showMap(2))
            }
        }
        binding.startDayDateContainer.setOnClickListener {
            val cldr: Calendar = Calendar.getInstance()
            cldr.time = viewModel.startDate
            val day: Int = cldr.get(Calendar.DAY_OF_MONTH)
            val month: Int = cldr.get(Calendar.MONTH)
            val year: Int = cldr.get(Calendar.YEAR)

            startDatePickerDialog = DatePickerDialog(
                requireContext(),
                { view, year, monthOfYear, dayOfMonth ->
                    val cal = Calendar.getInstance()
                    cal.set(year, monthOfYear, dayOfMonth)

                    updateStartDateContainer(cal.time)

                },
                year,
                month,
                day
            )
            startDatePickerDialog!!.datePicker.minDate = System.currentTimeMillis() - 1000
            startDatePickerDialog!!.show()
        }
        binding.endDayDateContainer.setOnClickListener {
            val cldr: Calendar = Calendar.getInstance()
            cldr.time = viewModel.endDate
            val day: Int = cldr.get(Calendar.DAY_OF_MONTH)
            val month: Int = cldr.get(Calendar.MONTH)
            val year: Int = cldr.get(Calendar.YEAR)

            endDatePickerDialog = DatePickerDialog(
                requireContext(),
                { view, year, monthOfYear, dayOfMonth ->
                    val cal = Calendar.getInstance()

                    cal.set(year, monthOfYear, dayOfMonth)

                    updateEndDateContainer(cal.time)

                },
                year,
                month,
                day
            )

            endDatePickerDialog!!.datePicker.minDate =
                Utils.instance.addDaysToDate(Calendar.getInstance().time, 1)?.time!! - 1000
            endDatePickerDialog!!.show()
        }
        binding.backBtn.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.e("BookNowRentFragment", "onCreateView: ")
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_book_now_rent, container, false)
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickFile(requestCode)
        }

    }

    private fun pickFile(code: Int) {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            val intentPDF = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI
            )
            intentPDF.type = "image/*"
            startActivityForResult(intentPDF, code)
        } else {
            if (PermissionUtils.getNBR(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) > 1) {
                displayNeverAskAgainDialog()

            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), code
                )
                PermissionUtils.setNBR(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }
    private fun displayNeverAskAgainDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setMessage(
            """
            We need to update your profile. Please permit the permission through Settings screen.
            
            Select Permissions -> Enable permission
            """.trimIndent()
        )
        builder.setCancelable(false)
        builder.setPositiveButton("Permit Manually",
            DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri: Uri = Uri.fromParts("package", requireActivity().packageName, null)
                intent.data = uri
                startActivity(intent)
            })
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CIVIL_ID_CODE) {
                try {

                    binding.uploadTextInputEditText.setText(
                        Utils.instance.getNameFromContentUri(
                            requireContext(),
                            data?.data
                        )
                    )
                    val path =
                        data?.data?.let {
                            context?.let { it1 ->
                                FileUtils.instance.getPath(
                                    it1,
                                    it
                                )
                            }
                        }
                    data?.data?.let {
                        path?.let { it1 ->
                            viewModel.uploadMedia(
                                it1,
                                "*/*",
                                CIVIL_ID_CODE
                            )
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (requestCode == CIVIL_ID_CODE + 10) {
                binding.uploadTextInputEditText.setText(
                    requireActivity().getString(R.string.civil_id)
                )
                val filePath = getPathFromUri(fileUri)
                filePath?.let {
                    viewModel.uploadMedia(
                        it,
                        "images/jpeg",
                        CIVIL_ID_CODE
                    )
                }
            }
            if (requestCode == LICENSE_CODE) {
                try {

                    binding.uploadLicenseTextInputEditText.setText(
                        Utils.instance.getNameFromContentUri(
                            requireContext(),
                            data?.data
                        )
                    )
                    val path =
                        data?.data?.let {
                            context?.let { it1 ->
                                FileUtils.instance.getPath(
                                    it1,
                                    it
                                )
                            }
                        }
                    data?.data?.let {
                        path?.let { it1 ->
                            viewModel.uploadMedia(
                                it1,
                                "*/*",
                                LICENSE_CODE
                            )
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (requestCode == LICENSE_CODE + 10) {
                binding.uploadLicenseTextInputEditText.setText(
                    requireActivity().getString(R.string.license_uploaded)
                )
                val filePath = getPathFromUri(fileUri)
                filePath?.let {
                    viewModel.uploadMedia(
                        it,
                        "images/jpeg",
                        LICENSE_CODE
                    )
                }
            }
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


    private fun clickPicturesThroughCamera(request:Int) {
        try {
            initImageUri()
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
            startActivityForResult(intent, request)
        } catch (e: java.lang.Exception) {
        }
    }
    private fun pickImage(request: Int) {
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

    }

    private fun showPickFileBottomSheet(request:Int) {
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
            pickImage(request + 10)
            pickFileDialog.dismiss()
        }
        bindingPickFile.galleryTxt.setOnClickListener {

            pickFile(request)
            pickFileDialog.dismiss()
        }
    }
}