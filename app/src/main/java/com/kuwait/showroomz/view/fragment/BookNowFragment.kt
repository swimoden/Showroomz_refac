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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.*
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.data.Model
import com.kuwait.showroomz.model.data.User
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.model.simplifier.ModelSimplifier
import com.kuwait.showroomz.model.simplifier.UserSimplifier
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.viewModel.BookNowVM
import kotlinx.android.synthetic.main.email_update_dialog.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.text.isNullOrEmpty


class BookNowFragment : Fragment() {
    private val SDK_REQUEST_CODE = 1001
    lateinit var binding: FragmentBookNowBinding
    lateinit var viewModel: BookNowVM
    lateinit var model: Model
    val prefs = Shared()
    override fun onResume() {
        super.onResume()

        LogProgressRepository.logProgress(
            "Buying_screen",
            category = model.let {
                ModelSimplifier(
                    it
                ).brand?.cat?.id
            }
                ?: "",
            dealerData = model.let { ModelSimplifier(it).brand?.id } ?: "",
            modelData = model.let {
                ModelSimplifier(it).id
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(BookNowVM::class.java)
        viewModel.getConnectedUser()

        binding.viewModel = viewModel
        arguments.let {
            model = it?.let { it1 -> BookNowFragmentArgs.fromBundle(it1).model }!!
            binding.model = ModelSimplifier(model)
            viewModel.program = it.let { it1 -> BookNowFragmentArgs.fromBundle(it1).program }
            viewModel.model = model
        }
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
         viewModel.noConectionError.observe(viewLifecycleOwner, Observer {
            if (it){
                DesignUtils.instance.showErrorDialog(requireContext(), null,null){

                }
            }
        })
        viewModel.successUploadFile.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it) {
                binding.uploadTextInputEditText.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorLightGreen
                    )
                )
            }
        })
        viewModel.success.observe(viewLifecycleOwner, Observer {
            if (it)
                showSuccessBottomSheet()
        })
        Navigation.findNavController(requireView()).currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(
            PAYMENT_RESULT
        )?.observe(viewLifecycleOwner,
            androidx.lifecycle.Observer { result ->
                if (result) {
                    viewModel.requestPayment()
                } else showErrorBottomSheet()

            })
        viewModel.successUrl.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                Navigation.findNavController(requireView())
                    .navigate(BookNowFragmentDirections.showPaymentWebFragment(it,model))
                viewModel.successUrl.value = ""
            }
        })
        viewModel.error.observe(viewLifecycleOwner, Observer {
            if (it) {
                showErrorBottomSheet()
            }
        })
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
        binding.payBtn.setOnClickListener {
            if (viewModel.civilIdUrl.isNullOrEmpty()) {

                binding.uploadContainer.startAnimation(
                    AnimationUtils.loadAnimation(
                        activity,
                        R.anim.shake
                    )
                )
            } else {
//                prefs.string(USER_ID)?.let {
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
            //}
            }
        }
        binding.uploadImage.setOnClickListener {

            if (NetworkUtils.instance.connected) {
                showPickFileBottomSheet(SDK_REQUEST_CODE)
                    //pickFile(SDK_REQUEST_CODE)
            }else{
                DesignUtils.instance.showErrorDialog(requireContext(), null, null){

                }
            }
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_book_now, container, false)
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
           // intentPDF.addCategory(Intent.CATEGORY_OPENABLE);
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
            if (requestCode == SDK_REQUEST_CODE) {
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
                                "*/*"
                            )
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (requestCode == SDK_REQUEST_CODE + 10) {
                binding.uploadTextInputEditText.setText(
                    requireActivity().getString(R.string.civil_id)
                )
                val filePath = getPathFromUri(fileUri)
                filePath?.let {
                    viewModel.uploadMedia(
                        it,
                        "images/jpeg"
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
       // if (ActivityCompat.checkSelfPermission(requireActivity(),Manifest.permission.MANAGE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
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
       /* }else{
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE),
                10001
            )
        }*/
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

            pickFromGallery(request)
            pickFileDialog.dismiss()
        }
    }
    private fun pickFromGallery(request: Int) {
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

            startActivityForResult(intent, request)
        } else {
            if (PermissionUtils.getNBR(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) > 1) {
                displayNeverAskAgainDialog()

            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), request
                )
                PermissionUtils.setNBR(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

}