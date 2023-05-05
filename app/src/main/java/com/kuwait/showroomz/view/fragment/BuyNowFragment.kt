package com.kuwait.showroomz.view.fragment

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.*
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.data.*
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.model.simplifier.ModelSimplifier
import com.kuwait.showroomz.model.simplifier.OfferSimplifier
import com.kuwait.showroomz.model.simplifier.TrimSimplifier
import com.kuwait.showroomz.model.simplifier.UserSimplifier
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.view.adapters.CallbackAdapter
import com.kuwait.showroomz.view.adapters.OfferDetailsAdapter
import com.kuwait.showroomz.viewModel.BuyNowVM
import kotlinx.android.synthetic.main.email_update_dialog.*
import kotlinx.android.synthetic.main.fragment_buy_now.*
import kotlinx.android.synthetic.main.fragment_buy_now.email_txt
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.text.isNullOrEmpty


class BuyNowFragment : Fragment() {
    private val CIVIL_ID_CODE = 1
    private val LICENSE_CODE = 2
    lateinit var binding: FragmentBuyNowBinding
    lateinit var viewModel: BuyNowVM
    lateinit var model: Model
    lateinit var trim: Trim
    val prefs = Shared()
    private val offerAdapter: OfferDetailsAdapter by lazy {
        OfferDetailsAdapter(arrayListOf())

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_buy_now, container, false)
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root

    }
    override fun onResume(){
     super.onResume()

        LogProgressRepository.logProgress("Buying_screen",category = model.let {
            ModelSimplifier(
                it
            ).brand?.cat?.id
        }
            ?:"", dealerData =model.let { ModelSimplifier(it).brand?.id } ?:"",modelData = model.let {
            ModelSimplifier(it).id
        }
            ,trim = trim.id)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(BuyNowVM::class.java)
        viewModel.getConnectedUser()
        binding.viewModel = viewModel
        binding.recycler.apply {
            layoutManager= LinearLayoutManager(context)
            adapter = offerAdapter
        }
        arguments?.let {
             model =  BuyNowFragmentArgs.fromBundle(it).model
            trim = BuyNowFragmentArgs.fromBundle(it).trim

            binding.model = ModelSimplifier(model)
            binding.trim = TrimSimplifier(trim)
            viewModel.trim = trim
            viewModel.model = model
            viewModel.getOffers(model.id)

            if (trim.price != 0){
                binding.price = viewModel.priceFormatter(trim.price.toString())
                viewModel.amountPaid = trim.price.toString()
            }else{
                binding.price = viewModel.priceFormatter(model.price())
                viewModel.amountPaid = model.price()
            }
            if (model.hasAllOffer || viewModel.offers.size < 2){
                //change_offer.isVisible = false
                select_offer.isVisible = false
                //pay_btn.width = 500
                viewModel.calculateList()
            }else{
                //change_offer.isVisible = false
                select_offer.isVisible = true
            }



            //change_offer.isVisible = !viewModel.showSelectOffer(model.id)


        }

       // viewModel.calculate()
        DesignUtils.instance.setStatusBar(
            requireActivity() as MainActivity,
            false,
            ModelSimplifier(model).brand?.category?.setBgColor()
        )
        if (viewModel.user != null) {
            binding.user = UserSimplifier(viewModel.user!!)
        }
        onClickListener()
        observeData()
    }

    private fun onClickListener() {
        binding.knetId.setOnClickListener {
            viewModel.paymentMethod.set("knet")
        }
        binding.masterId.setOnClickListener {
            viewModel.paymentMethod.set("cc")
        }
        binding.backBtn.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }
        binding.uploadImage.setOnClickListener {
            if (NetworkUtils.instance.connected) {
                showPickFileBottomSheet(CIVIL_ID_CODE)
            }else{
                DesignUtils.instance.showErrorDialog(requireContext(), null, null){

                }
            }
        }
        binding.uploadLicenseImage.setOnClickListener {
            if (NetworkUtils.instance.connected) {
                showPickFileBottomSheet(LICENSE_CODE)
            }else{
                DesignUtils.instance.showErrorDialog(requireContext(), null, null){

                }
            }
        }
        binding.selectOffer.setOnClickListener {
            Navigation.findNavController(it).navigate(
                BuyNowFragmentDirections.actionShowModelOffers(
                    ModelSimplifier(model), true, viewModel.offer,  viewModel.selectedOffersObj.toTypedArray()
                )
            )
        }
        binding.payBtn.setOnClickListener {
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
                binding.uploadLicenseContainer.startAnimation(
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
    private fun showErrorBottomSheet() {
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
        bindingSuccess.messageText.text =
            getString(R.string.booking_error)


        errorDialog.show()

        bindingSuccess.exitBtn.setOnClickListener {
            errorDialog.dismiss()


        }

    }
    private fun observeData() {
         viewModel.noConectionError.observe(viewLifecycleOwner, Observer {
            if (it){
                DesignUtils.instance.showErrorDialog(requireContext(), null,null){}
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
                Navigation.findNavController(requireView()).navigate(BuyNowFragmentDirections.showPaymentWebFragment(it,model))
                viewModel.successUrl.value=""
            }
        })
        Navigation.findNavController(requireView()).currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(
            PAYMENT_RESULT
        )?.observe(viewLifecycleOwner,
            androidx.lifecycle.Observer { result ->
                if (result){
                    viewModel.buyNow()
                }else showErrorBottomSheet()

            })
        Navigation.findNavController(requireView()).currentBackStackEntry?.savedStateHandle?.getLiveData<ArrayList<Offer>>(
            "OFFER"
        )?.observe(viewLifecycleOwner,
            Observer { offers ->
                viewModel.selectedOffersObj = offers
                viewModel.selectedOffers.clear()
                offers.forEach {
                    viewModel.selectedOffers.add(OfferSimplifier(it))
                    viewModel.offer = it
                }
                viewModel.calculateList()
               // change_offer.isVisible = offers.size > 0

            })
        viewModel.success.observe(viewLifecycleOwner, Observer {
            if (it)
                showSuccessBottomSheet()
        })
        viewModel.error.observe(viewLifecycleOwner, Observer {
            if (it){
                showErrorBottomSheet()
            }
        })
        viewModel.details.observe(viewLifecycleOwner, Observer {
            offerAdapter.refresh(it)
            recycler.isVisible = it.size > 0
        })
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
//            val intentPDF = Intent(Intent.ACTION_GET_CONTENT);
//            intentPDF.type = "*/*";
            val intentPDF = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI
            )
            intentPDF.type = "image/*"
            //intentPDF.addCategory(Intent.CATEGORY_OPENABLE);
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
        if(resultCode == Activity.RESULT_OK) {
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