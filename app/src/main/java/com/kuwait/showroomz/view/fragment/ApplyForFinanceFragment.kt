package com.kuwait.showroomz.view.fragment

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
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
import com.kuwait.showroomz.databinding.FragmentApplyForFinanceBinding
import com.kuwait.showroomz.databinding.PickFileBottomSheetBinding
import com.kuwait.showroomz.databinding.SuccessBottomSheetBinding
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.extras.FileUtils
import com.kuwait.showroomz.extras.PermissionUtils
import com.kuwait.showroomz.extras.Utils
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.viewModel.ApplyForFinanceVM
import kotlinx.android.synthetic.main.fragment_apply_for_finance.*
import java.text.SimpleDateFormat
import java.util.*


class ApplyForFinanceFragment : Fragment() {
    lateinit var binding: FragmentApplyForFinanceBinding
    private lateinit var viewModel: ApplyForFinanceVM
    val SALARY_CODE = 1
    val AKAKATHOPIA = 2
    val BANK_STATEMENT = 3
    override fun onResume() {
        super.onResume()

        LogProgressRepository.logProgress("Request_finance_callback_screen")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_apply_for_finance, container, false)
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ApplyForFinanceVM::class.java)
        arguments.let {
//           viewModel.bank =ApplyForFinanceFragmentArgs.fromBundle(it!!).bank
//            viewModel.model =ApplyForFinanceFragmentArgs.fromBundle(it).model
        }

        DesignUtils.instance.setStatusBar(
            requireActivity() as MainActivity,
            false,
            viewModel.simplifier.brand?.category?.setBgColor()
        )
        binding.viewModel = viewModel
        viewModel.simplifier.brand?.color?.let{
            top_container.setBackgroundColor(Color.parseColor(it as String?))
        }
        onClickListener()
        observeData()
    }

    private fun observeData() {
        viewModel.noConnectionError.observe(viewLifecycleOwner, Observer {
            if (it){
                DesignUtils.instance.showErrorDialog(requireContext(), null,null){
                    Navigation.findNavController(requireView())
                        .popBackStack(R.id.applyForFinance, true)
                    Navigation.findNavController(requireView())
                        .navigateUp()
                }
            }
        })
        viewModel.successUploadNetSalaryFile.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer {
                if (it) {
                    binding.netSalaryCertificateTextInputEditText.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.colorLightGreen
                        )
                    )
                }else{
                    binding.netSalaryCertificateTextInputEditText.setText("")
                    DesignUtils.instance.showErrorDialog(requireContext(),getString(R.string.error), getString(R.string.error_uploding)){

                    }
                }
            })
        viewModel.successUploadAkaFile.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it) {
                binding.akamathopiaTextInputEditText.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorLightGreen
                    )
                )
            }else{
                binding.akamathopiaTextInputEditText.setText("")
                    DesignUtils.instance.showErrorDialog(requireContext(),getString(R.string.error), getString(R.string.error_uploding)){

                    }
            }
        })
        viewModel.successUploadBankStatementFile.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer {
                if (it) {
                    binding.statementThreeMonthTextInputEditText.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.colorLightGreen
                        )
                    )
                }else{
                    binding.statementThreeMonthTextInputEditText.setText("")
                    DesignUtils.instance.showErrorDialog(requireContext(),getString(R.string.error), getString(R.string.error_uploding)){

                    }
                }
            })
        viewModel.netSalaryError.observe(viewLifecycleOwner, Observer {
            if (it) {
                binding.netSalaryCertificateContainer.startAnimation(
                    AnimationUtils.loadAnimation(
                        activity,
                        R.anim.shake
                    )
                )
            }
        })
        viewModel.akaError.observe(viewLifecycleOwner, Observer {
            if (it) {
                binding.akamathopiaContainer.startAnimation(
                    AnimationUtils.loadAnimation(
                        activity,
                        R.anim.shake
                    )
                )
            }
        })
        viewModel.bankStatementError.observe(viewLifecycleOwner, Observer {
            if (it) {
                binding.statementThreeMonthContainer.startAnimation(
                    AnimationUtils.loadAnimation(
                        activity,
                        R.anim.shake
                    )
                )
            }
        })
        viewModel.bankNumberError.observe(viewLifecycleOwner, Observer {
            if (!it) {
                DesignUtils.instance.showErrorDialog(requireContext(),getString(R.string.error), getString(R.string.invalid_account_number)){}
                /*binding.bankAccountTextInputLayout.startAnimation(
                    AnimationUtils.loadAnimation(
                        activity,
                        R.anim.shake
                    )
                )*/
            }
        })
         viewModel.civilIdError.observe(viewLifecycleOwner, Observer {
             if (it) {
                 DesignUtils.instance.showErrorDialog(requireContext(),getString(R.string.error), getString(R.string.invalid_civil_id)){}
             }
         })
        viewModel.success.observe(viewLifecycleOwner, Observer {
            if (it) {
                var dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
                var binding: SuccessBottomSheetBinding = DataBindingUtil.inflate(
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
                    Navigation.findNavController(requireView())
                        .popBackStack(R.id.applyForFinance, true)
                    Navigation.findNavController(requireView())
                        .navigateUp()
                }
            }else{
                DesignUtils.instance.showErrorDialog(requireContext(), getString(R.string.server_error),
                    getString(R.string.apologize)){
                }
            }
        })


    }

    private fun onClickListener() {
        binding.salaryUploadImage.setOnClickListener {
            showPickFileBottomSheet(SALARY_CODE)
            //pickFile(SALARY_CODE)
        }
        binding.akamathopiaUploadImage.setOnClickListener {
            //pickFile(AKAKATHOPIA)
            showPickFileBottomSheet(AKAKATHOPIA)
        }
        binding.statementThreeMonthUploadImage.setOnClickListener {
           // pickFile(BANK_STATEMENT)
            showPickFileBottomSheet(BANK_STATEMENT)
        }
        binding.governmentTxt.setOnClickListener {
            viewModel.isPrivate.set(false)
        }
        binding.privateTxt.setOnClickListener {
            viewModel.isPrivate.set(true)
        }
        binding.backBtn.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            if (requestCode == SALARY_CODE) {
                try {

                    binding.netSalaryCertificateTextInputEditText.setText(
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
                                SALARY_CODE
                            )
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (requestCode == SALARY_CODE + 10) {
                binding.netSalaryCertificateTextInputEditText.setText(
                    requireActivity().getString(R.string.net_salary_certificate)
                )
                val filePath = getPathFromUri(fileUri)
                filePath?.let {
                    viewModel.uploadMedia(
                        it,
                        "images/jpeg",
                        SALARY_CODE
                    )
                }


            }
            if (requestCode == AKAKATHOPIA) {
                try {

                    binding.akamathopiaTextInputEditText.setText(
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
                                AKAKATHOPIA
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (requestCode == AKAKATHOPIA + 10) {
                binding.akamathopiaTextInputEditText.setText(
                    requireActivity().getString(R.string.akamathopia)
                )
                val filePath = getPathFromUri(fileUri)
                filePath?.let {
                    viewModel.uploadMedia(
                        it,
                        "images/jpeg",
                        AKAKATHOPIA
                    )
                }
            }
            if (requestCode == BANK_STATEMENT) {
                try {

                    binding.statementThreeMonthTextInputEditText.setText(
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
                                BANK_STATEMENT
                            )
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (requestCode == BANK_STATEMENT + 10) {
                binding.statementThreeMonthTextInputEditText.setText(
                    requireActivity().getString(R.string.net_salary_certificate)
                )
                val filePath = getPathFromUri(fileUri)
                filePath?.let {
                    viewModel.uploadMedia(
                        it,
                        "images/jpeg",
                        BANK_STATEMENT
                    )
                }
            }
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