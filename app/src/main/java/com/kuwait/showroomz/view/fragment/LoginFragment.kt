package com.kuwait.showroomz.view.fragment

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonObject
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.EmailUpdateDialogBinding
import com.kuwait.showroomz.databinding.FragmentLoginBinding
import com.kuwait.showroomz.extras.CacheObjects
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.extras.Utils
import com.kuwait.showroomz.extras.hideKeyboard
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.viewModel.LoginVM
import com.kuwait.showroomz.viewModel.MainVM
import com.kuwait.showroomz.viewModel.ProfileVM
import kotlinx.android.synthetic.main.email_update_dialog.*

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val viewModel by viewModels<LoginVM>()
    //private val dataVm by viewModels<MainVM>()

    lateinit var mGoogleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001
    lateinit var callbackManager: CallbackManager
    var json = JsonObject()
    private val dialogEmail: BottomSheetDialog by lazy {
        BottomSheetDialog(
            requireContext(),
            R.style.BottomSheetDialog
        )
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root

    }
    override fun onResume() {
        super.onResume()
        LogProgressRepository.logProgress("Login_screen")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        callbackManager = CallbackManager.Factory.create()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1040527997497-gc18lurjc8tqerpfmdl2tggplp9tcrdk.apps.googleusercontent.com")
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        observeData()
        onclickListener()
        view.setOnClickListener {
            it.hideKeyboard()
        }
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult?> {
            override fun onSuccess(loginResult: LoginResult?) {
                Log.d("TAG", "Success Login")
                getUserProfile(loginResult?.accessToken, loginResult?.accessToken?.userId)
            }

            override fun onCancel() {
                Toast.makeText(requireContext(), "Login Cancelled", Toast.LENGTH_LONG).show()
            }

            override fun onError(exception: FacebookException) {
                Toast.makeText(requireContext(), exception.message, Toast.LENGTH_LONG).show()
            }
        })

    }

    private fun observeData() {

        viewModel.validEmail.observe(viewLifecycleOwner, Observer { valid ->
            if (!valid) {
                binding.emailTextInputLayout.error = getString(R.string.invalid_email)
            } else binding.emailTextInputLayout.error = null
        })

        viewModel.validPassword.observe(viewLifecycleOwner, Observer { error ->
            if (!error) {
                binding.passwordTextInputLayout.error = getString(R.string.invalid_password)
            } else binding.passwordTextInputLayout.error = null
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer { loading ->
            if (loading) {
                activity?.let { DesignUtils.instance.disableUserInteraction(it) }
                val imm: InputMethodManager =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(requireView().windowToken, 0)
            } else activity?.let { DesignUtils.instance.enableUserInteraction(it) }
            binding.progressCircular.isVisible = loading
        })

        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            if (error.isNotEmpty() && error=="login_error"){
                //binding.emailTextInputLayout.error = getString(R.string.invalid_email)
                binding.passwordTextInputLayout.error = getString(R.string.invalid_password)
            }
        })

        viewModel.success.observe(viewLifecycleOwner, Observer {
            if (it) {
                LogProgressRepository.refreshMainUserData()
                if (arguments?.let { it1 -> LoginFragmentArgs.fromBundle(it1).from } == R.id.profileFragment) {
                    Navigation.findNavController(requireView())
                        .navigate(LoginFragmentDirections.showProfile())
                }
                if (arguments?.let { it1 -> LoginFragmentArgs.fromBundle(it1).from } == R.id.testDriveAddressFragment) {
                    Navigation.findNavController(requireView())
                        .navigate(LoginFragmentDirections.showTestDriveAddress())
                }
                if (arguments?.let { it1 -> LoginFragmentArgs.fromBundle(it1).from } == R.id.applyForFinance) {
                    Navigation.findNavController(requireView())
                        .navigate(LoginFragmentDirections.showCallbackBank())
                }
                if (arguments?.let { it1 -> LoginFragmentArgs.fromBundle(it1).from } == R.id.applyForFinanceStepOne) {
                    CacheObjects.bank?.let { it1 ->
                        LoginFragmentDirections.showApplyForFinanceStepOne(
                            it1
                        )
                    }?.let { it2 -> Navigation.findNavController(requireView()).navigate(it2) }
                    CacheObjects.bank = null
                }
                if (arguments?.let { it1 -> LoginFragmentArgs.fromBundle(it1).from } == R.id.buyNowFragment) {
                    val model = CacheObjects.model
                    val trim = CacheObjects.trim
                    CacheObjects.trim = null
                    CacheObjects.model = null
                    model?.let { it1 ->
                        trim?.let { it2 ->
                            LoginFragmentDirections.actionShowBuyNow(
                                it1, it2
                            )
                        }
                    }?.let { it2 -> Navigation.findNavController(requireView()).navigate(it2) }
                }
                if (arguments?.let { it1 -> LoginFragmentArgs.fromBundle(it1).from } == R.id.bookNowFragment) {
                    val model = CacheObjects.model
                    val trim = CacheObjects.trim
                    val program = CacheObjects.program
                    CacheObjects.trim = null
                    CacheObjects.model = null
                    CacheObjects.program = null
                    model?.let { it1 ->
                        trim?.let { it2 ->
                            LoginFragmentDirections.actionShowBook(
                                it1, it2, program
                            )
                        }
                    }?.let { it2 -> Navigation.findNavController(requireView()).navigate(it2) }
                }
                if (arguments?.let { it1 -> LoginFragmentArgs.fromBundle(it1).from } == R.id.bookNowRentFragment) {
                    val model = CacheObjects.model
                    val trim = CacheObjects.trim
                    val program = CacheObjects.program
                    CacheObjects.trim = null
                    CacheObjects.model = null
                    CacheObjects.program = null
                    model?.let { it1 ->
                        trim?.let { it2 ->
                            LoginFragmentDirections.actionShowBookNowForRent(
                                it1, it2, program?.id
                            )
                        }
                    }?.let { it2 -> Navigation.findNavController(requireView()).navigate(it2) }
                }
                if (arguments?.let { it1 -> LoginFragmentArgs.fromBundle(it1).from } == R.id.yourCarDetailsFragment) {
                    val model = CacheObjects.model
                    CacheObjects.trim = null
                    CacheObjects.model = null
                    Navigation.findNavController(requireView()).navigate(LoginFragmentDirections.actionShowAppraisalCar(
                        model
                    ))

                    /*if (model != null) {
                        CacheObjects.trim = null
                        CacheObjects.model = null
                        model.let { it1 ->
                            LoginFragmentDirections.actionShowAppraisalCar(
                                it1
                            )
                        }.let { it2 -> Navigation.findNavController(requireView()).navigate(it2) }
                    } else {

                    }*/
                }
            }
        })
        viewModel.noConectionError.observe(viewLifecycleOwner, Observer {
            if (it){
                DesignUtils.instance.showErrorDialog(requireContext(), null, null){

                }
            }
        })
        viewModel.callRegister.observe(viewLifecycleOwner, Observer {
            if (it){
                if (json.has("email")) {
                    viewModel.registerSocial(json)
                }else{
                    viewModel.loading.value = false
                    showUpdateEmailDialog(requireActivity()) {
                           viewModel.registerSocial(json)
                    }
                }
            }
        })
        viewModel.existEmail.observe(viewLifecycleOwner, Observer { error ->
            run {
                if (error) {
                    binding.emailTextInputLayout.error = getString(R.string.email_exist_already)
                }
            }
        })
        viewModel.updatePass.observe(viewLifecycleOwner, Observer {
            if (it){
                //val action = nav
                //view?.let { it1 ->
                    Navigation.findNavController(requireView()).navigate(
                        LoginFragmentDirections.actionLoginFragmentToChangePasswordFragment(binding.emailTextInputEditText.text.toString())
                    )
               // }
            }
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RC_SIGN_IN -> {
                val task =
                    GoogleSignIn.getSignedInAccountFromIntent(data)
                handleSignInResult(task)
            }
            else -> {
                callbackManager.onActivityResult(requestCode, resultCode, data)
            }

        }
    }
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(
                ApiException::class.java
            )

            // Signed in successfully
            val googleId = account?.id ?: ""
            Log.i("Google ID",googleId)

            val googleFirstName = account?.givenName ?: ""
            Log.i("Google First Name", googleFirstName)

            val googleLastName = account?.familyName ?: ""
            Log.i("Google Last Name", googleLastName)



            val googleProfilePicURL = account?.photoUrl.toString()
            Log.i("Google Profile Pic URL", googleProfilePicURL)

            val googleIdToken = account?.idToken ?: ""
            Log.i("Google ID Token", googleIdToken)

            json = JsonObject()
            account?.email?.let{
                json.addProperty("email", it)
            }
            json.addProperty("socialType", "google")
            json.addProperty("socialUserId", googleIdToken)
            googleId.let {
                json.addProperty("googleId", it)
            }
            json.addProperty("fullName", account?.displayName)
            viewModel.loginSocial(json)

        } catch (e: ApiException) {
            // Sign in was unsuccessful
                Toast.makeText(requireContext(),e.statusCode.toString(), Toast.LENGTH_LONG).show()
            Log.e(
                "failed code=", e.statusCode.toString()
            )
        }
    }
    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        requireActivity().startActivityForResult(
            signInIntent, RC_SIGN_IN
        )
    }

    private fun onclickListener() {
        binding.facebookBtn.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile", "email"))
        }
        binding.googleBtn.setOnClickListener {
            signIn()
        }
        binding.backBnt.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }
        binding.bottomContainer.setOnClickListener {
            arguments?.let { it1 ->
                LoginFragmentArgs.fromBundle(
                    it1
                ).from
            }?.let { it2 -> LoginFragmentDirections.showRegister(it2) }?.let { it3 ->
                Navigation.findNavController(it).navigate(
                    it3
                )
            }
        }
        binding.forgetPasswordText.setOnClickListener {
            Navigation.findNavController(it).navigate(
                LoginFragmentDirections.showForgetPassword()
            )
        }

        binding.emailTextInputEditText.doOnTextChanged { text, start, count, after ->
            viewModel.validEmail.value = Utils.instance.isValidEmail(binding.emailTextInputEditText.text)
        }

        binding.passwordTextInputEditText.doOnTextChanged { text, start, count, after ->
            viewModel.validPassword.value = binding.passwordTextInputEditText.text.toString().length > 7
        }

    }
    fun getUserProfile(token: AccessToken?, userId: String?) {

        val parameters = Bundle()
        parameters.putString(
            "fields",
            "id, first_name, middle_name, last_name, name, picture, email"
        )
        GraphRequest(token,
            "/$userId/",
            parameters,
            HttpMethod.GET,
            GraphRequest.Callback { response ->
                json = JsonObject()
                val jsonObject = response.jsonObject

                // Facebook Access Token
                // You can see Access Token only in Debug mode.
                // You can't see it in Logcat using Log.d, Facebook did that to avoid leaking user's access token.
                if (BuildConfig.DEBUG) {
                    FacebookSdk.setIsDebugEnabled(true)
                    FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS)
                }
                json.addProperty("socialType", "facebook")
                json.addProperty("socialUserId", token?.token)
                // Facebook Id
                if (jsonObject?.has("id") == true) {
                    val facebookId = jsonObject.getString("id")

                    json.addProperty("facebookId", facebookId)
                    Log.i("Facebook Id: ", facebookId.toString())
                } else {
                    Log.i("Facebook Id: ", "Not exists")
                }


                // Facebook Name
                if (jsonObject?.has("name") == true) {
                    val facebookName = jsonObject.getString("name")
                    json.addProperty("fullName", facebookName)
                    Log.i("Facebook Name: ", facebookName)
                } else {
                    Log.i("Facebook Name: ", "Not exists")
                }


                // Facebook Profile Pic URL
                if (jsonObject?.has("picture") == true) {
                    val facebookPictureObject = jsonObject.getJSONObject("picture")
                    if (facebookPictureObject.has("data")) {
                        val facebookDataObject = facebookPictureObject.getJSONObject("data")
                        if (facebookDataObject.has("url")) {
                            val facebookProfilePicURL = facebookDataObject.getString("url")
                            Log.i("Facebook Profil", facebookProfilePicURL)
                        }
                    }
                } else {
                    Log.i("Facebook ", "Not exists")
                }

                // Facebook Email
                if (jsonObject?.has("email") == true) {
                    val facebookEmail = jsonObject.getString("email")
                    json.addProperty("email", facebookEmail)
                    Log.i("Facebook Email: ", facebookEmail)
                } else {
                    Log.i("Facebook Email: ", "Not exists")
                }






                viewModel.loginSocial(json)
            }).executeAsync()
    }

    private fun showUpdateEmailDialog(ctx: Context, action: () -> Unit) {
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
                json.addProperty("email", dialogEmail.email_txt.text.toString())
                action.invoke()
                dialogEmail.dismiss()
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
            viewModel.loading.value = false
            dialogEmail.dismiss()
        }
        dialogEmail.show()
    }

}