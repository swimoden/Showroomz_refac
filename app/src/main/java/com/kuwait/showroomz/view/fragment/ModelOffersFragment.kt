package com.kuwait.showroomz.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.FragmentModelOffersBinding
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.extras.WebViewLocaleHelper
import com.kuwait.showroomz.extras.isNull
import com.kuwait.showroomz.model.data.Model
import com.kuwait.showroomz.model.data.Offer
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.model.simplifier.ModelSimplifier
import com.kuwait.showroomz.model.simplifier.OfferSimplifier
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.view.adapters.ModelOfferAdapter
import com.kuwait.showroomz.viewModel.ModelOffersVM
import kotlinx.android.synthetic.main.fragment_model_details.*


class ModelOffersFragment : Fragment() {

    private lateinit var simplifier: ModelSimplifier
    private lateinit var viewModel: ModelOffersVM
    private lateinit var binding: FragmentModelOffersBinding
    private lateinit var model: Model
    private var offerList: List<Offer> = arrayListOf()
    private val offersAdapter: ModelOfferAdapter by lazy {
        ModelOfferAdapter(activity, offerList, viewModel)
    }
    var dialog = SimpleDialog(null,null)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                /*if (Navigation.findNavController(requireView()).previousBackStackEntry?.destination?.id == R.id.buyNowFragment) {
                    val navHostFragment = activity?.supportFragmentManager?.findFragmentById(R.id.fragment) as NavHostFragment
                    val navController = navHostFragment.navController
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        "OFFER",
                        viewModel.offers
                    )
                    val bundle = Bundle()
                    bundle.putParcelable("model", model)
                    navController.navigate(R.id.buyNowFragment, bundle)
                } else {*/
                Navigation.findNavController(requireView()).previousBackStackEntry?.savedStateHandle?.set(
                    "OFFER",
                    viewModel.offers
                )
                    findNavController(requireView()).navigateUp()
               // }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val helper = context?.let { WebViewLocaleHelper(it) }
        helper?.implementWorkaround()
        container?.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_model_offers, container, false)
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (::simplifier.isInitialized)
            LogProgressRepository.logProgress(
                "Offers_screen",
                category = simplifier.brand?.cat?.id ?: "",
                dealerData = simplifier.brand?.id ?: "",
                modelData = simplifier.id
            )
        else LogProgressRepository.logProgress("Offers_screen")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ModelOffersVM::class.java)
        arguments?.let {
            simplifier = ModelOffersFragmentArgs.fromBundle(it).model
            model = simplifier.model
            simplifier.getOffers()?.let { it1 -> offersAdapter.refresh(it1, simplifier) }
             ModelOffersFragmentArgs.fromBundle(it).offers?.let {
                 viewModel.offers = it.toMutableList() as ArrayList<Offer>
                 offersAdapter.refresh(viewModel.offers, simplifier)
             }?: run {
                 viewModel.selectOffer?.let {
                     viewModel.offers.add(it)
                     offersAdapter.refresh(viewModel.offers, simplifier)
                 }
             }
            viewModel.modelId = simplifier.id
            viewModel.selectOffer = ModelOffersFragmentArgs.fromBundle(it).offer
            viewModel.fromBuyNow = ModelOffersFragmentArgs.fromBundle(it).fromBuyNow
            simplifier.name.let { it1 -> Log.e("onViewCreated", it1) }
            simplifier.brand!!.cat?.usedFor?.let { it1 -> viewModel.getTrims(it1, simplifier.id) }
            binding.model = simplifier
        }

        DesignUtils.instance.setStatusBar(
            requireActivity() as MainActivity, true,
            ContextCompat.getColor(requireContext(), R.color.off_white)
        )

        binding.offersRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = offersAdapter
        }
        setOnclickListener()
        observeData()
    }

    private fun observeData() {
        viewModel.noConectionError.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it) {
                DesignUtils.instance.showErrorDialog(requireContext(), null, null) {

                }
            }
        })
        viewModel.trims.observe(viewLifecycleOwner, Observer { trims ->
            if (!trims.isNull()) {
                if (trims.offers?.isNotEmpty()!!) {
                    trims.offers!!.forEach {
                        it.translations?.en?.name?.let { it1 -> Log.e("ModelOffersFragment", it1) }
                    }
                    offersAdapter.refresh(trims.offers!!, simplifier)
                }else{
                    offersAdapter.refresh(viewModel.offers)
                }
            }
        })

        viewModel.callbackLoading.observe(viewLifecycleOwner, Observer {
            binding.progressCircularCallback.isVisible = it
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

    private fun setOnclickListener() {
        binding.backBnt.setOnClickListener {
            Navigation.findNavController(requireView()).previousBackStackEntry?.savedStateHandle?.set(
                "OFFER",
                viewModel.offers
            )
            Navigation.findNavController(requireView()).popBackStack()
            //Navigation.findNavController(it).navigateUp()
        }
        offersAdapter.setOnItemCLickListener(object : ModelOfferAdapter.OnItemClickListener {
            override fun onItemClick(offer: Offer?) {
             }
        })




       /* requireActivity()
            .onBackPressedDispatcher
            .addCallback(requireActivity(), object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController(requireView()).previousBackStackEntry?.savedStateHandle?.set(
                        "OFFER",
                        viewModel.offers
                    )
                    findNavController(requireView()).navigateUp()
                    /*if (isEnabled) {
                        isEnabled = false
                      // findNavController(requireView()).navigateUp()
                        requireActivity().onBackPressed()
                    }*/
                }
            }
            )*/
    }


}