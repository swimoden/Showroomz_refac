package com.kuwait.showroomz.view.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.FragmentProfileBinding
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.model.simplifier.UserSimplifier
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.viewModel.CategoryVM
import com.kuwait.showroomz.viewModel.ProfileVM


class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private val viewModel by viewModels<ProfileVM>()
    lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root

    }
    override fun onResume() {
        super.onResume()
        LogProgressRepository.logProgress("Profile_screen")
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel=viewModel
        viewModel.getCounts()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        DesignUtils.instance.setStatusBar(requireActivity() as MainActivity,false,
        ContextCompat.getColor(requireContext(),R.color.colorPrimaryDark))
        onclickListener()
        observeBrands()
    }

    private fun onclickListener() {
        binding.backBnt.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }
        binding.editProfileBtn.setOnClickListener {
            Navigation.findNavController(it).navigate(ProfileFragmentDirections.showEditProfile())
        }
        binding.changePasswordBtn.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(ProfileFragmentDirections.showChangePassword(null))
        }
        binding.logout.setOnClickListener {
            viewModel.logout()
            mGoogleSignInClient.signOut()
            Navigation.findNavController(it).navigateUp()
        }
        binding.callbackContainer.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(ProfileFragmentDirections.showCallbacks())
        }
        binding.financeContainer.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(ProfileFragmentDirections.showFinanceCallback())
        }
        binding.testDrives.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(ProfileFragmentDirections.showTestDriveList())
        }
        binding.favoritesContainer.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(ProfileFragmentDirections.showFavoriteList())
        }
        binding.financeRequest.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(ProfileFragmentDirections.showFinanceRequest())
        }
        binding.payment.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(ProfileFragmentDirections.showPaymentListFragment())
        }
        binding.apparasialRequest.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(ProfileFragmentDirections.showAppraisalList())
        }
    }

    private fun observeBrands() {
        viewModel.userModel.observe(viewLifecycleOwner, Observer {
            binding.user = UserSimplifier(it)
//            if (!viewModel.userImage.isNullOrEmpty()) {
//                binding.imageView.setImageURI(Uri.parse(viewModel.userImage!!))
//            }
        })

    }
}