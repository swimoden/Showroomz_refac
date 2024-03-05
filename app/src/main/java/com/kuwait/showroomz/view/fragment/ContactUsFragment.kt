package com.kuwait.showroomz.view.fragment

import android.Manifest
import android.Manifest.permission.CALL_PHONE
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.facebook.FacebookSdk.getApplicationContext
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.CallbackSuccessBottomSheetBinding
import com.kuwait.showroomz.databinding.FragmentContactUsBinding
import com.kuwait.showroomz.extras.CONTACT_EMAIL
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.extras.PermissionUtils
import com.kuwait.showroomz.model.data.Setting
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.viewModel.ContactUsVM


class ContactUsFragment : Fragment() {

    private lateinit var binding: FragmentContactUsBinding
    private lateinit var viewModel: ContactUsVM
    var local = LocalRepo()
    var phone = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_contact_us,
            container,
            false
        )
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root

    }
    override fun onResume() {
        super.onResume()

        LogProgressRepository.logProgress("Contact_us_screen")
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ContactUsVM::class.java)
        binding.viewModel = viewModel
        observeData()

        local.getAll<Setting>()?.let { list ->
            if (list.size > 0) {
                list[0].phoneNumber?.let {
                    phone = it
                }
            }
        }

        binding.callUsBtn.setOnClickListener {
            if (phone.isNotEmpty()) {
                val callIntent = Intent(Intent.ACTION_CALL)
                callIntent.data = Uri.parse("tel:$phone")
                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(),
                        CALL_PHONE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    startActivity(callIntent)
                } else {

                    if (PermissionUtils.getNBR(
                            requireContext(),
                            CALL_PHONE
                        ) > 1
                    ) {
                        displayNeverAskAgainDialog()

                    } else {
                        requestPermissions(arrayOf(CALL_PHONE), 1)
                        PermissionUtils.setNBR(
                            requireActivity(),
                            CALL_PHONE
                        )
                    }
                }
            }
        }

        binding.backBtn.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }

        binding.fbBtn.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/showroomzkw/"))
            startActivity(browserIntent)
        }
        binding.instaBtn.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/showroomzkw/"))
            startActivity(browserIntent)
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
    private fun observeData() {
        viewModel.noConnectionError.observe(viewLifecycleOwner, Observer {
            if (it) {
                DesignUtils.instance.showErrorDialog(requireContext(), null, null) {

                }
            }
        })
        viewModel.success.observe(viewLifecycleOwner, Observer {
            if (it) {
                val email = Intent(Intent.ACTION_SEND)
                email.putExtra(Intent.EXTRA_EMAIL, arrayOf<String>(CONTACT_EMAIL))
                email.putExtra(Intent.EXTRA_SUBJECT, viewModel.subject.get())
                email.putExtra(Intent.EXTRA_TEXT, viewModel.message.get())
                email.putExtra(Intent.EXTRA_PHONE_NUMBER, viewModel.phone.get())
                email.type = "message/rfc822"
                activity?.startActivityForResult(email, 11)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 11 && resultCode == RESULT_OK) {
            showSuccessBottomSheet()
        }
    }

    private fun showSuccessBottomSheet() {
        val successDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        val bindingSuccess: CallbackSuccessBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.callback_success_bottom_sheet,
            null,
            false
        )
        successDialog.setContentView(bindingSuccess.root)
        successDialog.setCanceledOnTouchOutside(false)
        bindingSuccess.titleText.text = getString(R.string.done_successfully)
        bindingSuccess.messageText.text = getString(R.string.representative_contact)
//                            binding.messageText.text=getString(R.string.representative_contact_delay)

        successDialog.show()

        bindingSuccess.exitBtn.setOnClickListener {

            successDialog.dismiss()

        }
    }
}


