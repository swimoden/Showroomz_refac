package com.kuwait.showroomz.view.fragment

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.view.animation.AnimationUtils
import android.webkit.WebSettings
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.*
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.model.data.*
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.model.simplifier.ImageSimplifier
import com.kuwait.showroomz.model.simplifier.ModelSimplifier
import com.kuwait.showroomz.model.simplifier.OfferSimplifier
import com.kuwait.showroomz.model.simplifier.TrimSimplifier
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.view.adapters.*
import com.kuwait.showroomz.viewModel.ModelDetailVM
import com.kuwait.showroomz.viewModel.SearchVM
import kotlinx.android.synthetic.main.fragment_model_details.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ModelDetailFragment : Fragment() {
    private lateinit var simplifier: ModelSimplifier
    private val viewModel by viewModels<ModelDetailVM>()
    lateinit var binding: FragmentModelDetailsBinding
    private var imageList: List<Image> = arrayListOf()
    private var colorList: List<Color> = arrayListOf()
    private var actionList: List<Action> = arrayListOf()
    private var offerList: List<Offer> = arrayListOf()
    private var listOfferList: ArrayList<List<Offer>> = arrayListOf()
    private var mainListOfferList: ArrayList<List<Offer>> = arrayListOf()
    private lateinit var selectedTrim: Trim
    private val dialogCallback: BottomSheetDialog by lazy {
        BottomSheetDialog(
            requireContext(),
            R.style.BottomSheetDialog
        )
    }
    private val successCallbackDialog: BottomSheetDialog by lazy {
        BottomSheetDialog(
            requireContext(),
            R.style.BottomSheetDialog
        )
    }

    var dialog = SimpleDialog(null, null)

    private val imageGalleryAdapter: ImageGalleryAdapter by lazy {
        ImageGalleryAdapter(imageList)
    }
    private val imageSliderAdapter: ImageSliderAdapter by lazy {
        ImageSliderAdapter(imageList, viewModel.showVerticalGallery.get(), requireContext())
    }
    private val trimsColorAdapter: TrimColorsAdapter by lazy {
        TrimColorsAdapter(colorList)
    }
    private val actionAdapter: ActionsAdapter by lazy {
        ActionsAdapter(actionList.distinct(), 0, 0)
    }

    private val offerAdapter: OfferAdapter by lazy {
        OfferAdapter(offerList)
    }

    @Suppress("UNCHECKED_CAST")
    private val offersAdapter: OffersAdapter by lazy {
        OffersAdapter(listOfferList, offerList) { list ->
            if (list.size == 1) {
                val offer = OfferSimplifier(list.first())
                offer.contents?.let {
                    if (it.size > 1) {
                        val action =
                            ModelDetailFragmentDirections.actionShowModelOffers(
                                simplifier,
                                false,
                                null,
                                list.toTypedArray()
                            )
                        view?.let { it1 -> Navigation.findNavController(it1).navigate(action) }

                    } else {
                        showCashbackOfferWithoutBank(offer)
                    }
                } ?: run {
                    val action =
                        ModelDetailFragmentDirections.actionShowModelOffers(
                            simplifier,
                            false,
                            null,
                            list.toTypedArray()
                        )
                    view?.let { it1 -> Navigation.findNavController(it1).navigate(action) }

                }
            } else {
                val action =
                    ModelDetailFragmentDirections.actionShowModelOffers(
                        simplifier, false, null,
                        list.toTypedArray()
                    )
                view?.let { it1 -> Navigation.findNavController(it1).navigate(action) }

            }
        }
    }
    private val videoAdapter: VideoAdapter by lazy {
        VideoAdapter(offerList)
    }

    override fun onResume() {
        super.onResume()
        if (::simplifier.isInitialized) {
            simplifier.brand?.cat?.let {
                LogProgressRepository.logProgress(
                    "Model_details_screen",
                    category = it.id,
                    dealerData = simplifier.brand!!.id,
                    modelData = simplifier.id
                )
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val helper = context?.let { WebViewLocaleHelper(it) }
        helper?.implementWorkaround()

        container?.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_model_details, container, false)
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE


        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //lifecycleScope.launch() {
        // delay(220)

        arguments?.let {
            ModelDetailFragmentArgs.fromBundle(it).slug?.let { slug ->
                viewModel.getModelBySlug(slug) {
                    populate(it)
                }
            } ?: run {
                ModelDetailFragmentArgs.fromBundle(it).id?.let { slug ->
                    viewModel.getRemoteModelById(slug) {
                        populate(it)
                    }
                } ?: run {
                    val model = ModelDetailFragmentArgs.fromBundle(it).model
                    model?.let { model ->
                        populate(model)
                    }
                }
                /*val model = ModelDetailFragmentArgs.fromBundle(it).model
                model?.let { model ->
                    populate(model)
                }*/
            }
        }
        //  }

        /*Handler(Looper.getMainLooper()).postDelayed({

            binding.viewModel = viewModel
            arguments?.let {
                ModelDetailFragmentArgs.fromBundle(it).slug?.let { slug ->
                    viewModel.getModelBySlug(slug) {
                        populate(it)
                    }
                } ?: run {
                    val model = ModelDetailFragmentArgs.fromBundle(it).model
                    model?.let { model ->
                        populate(model)
                    }
                }
            }

        },220)*/

    }

    private fun populate(model: Model) {
        binding.viewModel = viewModel
        simplifier = ModelSimplifier(model)
        viewModel.modelId = simplifier.id
        viewModel.setIsFavorite()
        viewModel.simplifier = simplifier
        simplifier.category?.let {
            viewModel.showVerticalGallery.set(it.isVertical)
            viewModel.isKfh.set(it.isKFh)
            it.isCivilIdMandatory?.let { it1 -> viewModel.isCivilIdMandatory.set(it1) }

        }
        viewModel.getTrimsByModelId(simplifier.id)
        activity?.let { act ->
            simplifier.category?.id?.let { (act as MainActivity).checkForModelAds(it) }
        }
        actionAdapter.refreshActions(
            simplifier.actions.filter { action -> action.isEnabled == true }.distinct()
                .sortedBy { action -> action.position },
            simplifier.color
        )
        iniDrawables()
        populateModel()
        lifecycleScope.launch {
            delay(500)
            viewModel.incrementModelNbViews(simplifier.id)
            viewModel.saveModelToHistory(model)
        }

        gallery_recycler.apply {
            adapter = imageGalleryAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
        gallery_image_slider.apply {
            adapter = imageSliderAdapter
        }
        color_recycler.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = trimsColorAdapter
        }
        binding.endArrow.isVisible = trimsColorAdapter.list.size > 9
        binding.startArrow.isVisible = trimsColorAdapter.list.size > 9
        binding.offersRecycler.apply {
            // adapter = offerAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = offersAdapter
        }
        binding.videosRecycler.apply {
            adapter = videoAdapter
        }

        binding.gridActionsRecycler.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = actionAdapter

        }
        onClickListener()
        observeData()
    }

    private fun iniDrawables() {
        binding.gallery.apply {
            background = simplifier.brand?.category?.color?.let {
                DesignUtils.instance.createDrawable(
                    it,
                    GradientDrawable.OVAL
                )
            }
        }
        binding.galleryBtn.apply {
            background = simplifier.brand?.category?.color?.let {
                DesignUtils.instance.createDrawable(
                    it,
                    GradientDrawable.RECTANGLE
                )
            }
        }
        binding.interiorButton.apply {
            background = simplifier.brand?.category?.color?.let {
                DesignUtils.instance.createDrawable(
                    it,
                    GradientDrawable.OVAL
                )
            }
        }
        binding.exteriorButton.apply {
            background = simplifier.brand?.category?.color?.let {
                DesignUtils.instance.createDrawable(
                    it,
                    GradientDrawable.OVAL
                )
            }
        }
        binding.redInteriorButton.apply {
            background = simplifier.brand?.category?.color?.let {
                DesignUtils.instance.createDrawable(
                    it,
                    GradientDrawable.RECTANGLE
                )
            }
        }
        binding.redInteriorButton.apply {
            background = simplifier.brand?.category?.color?.let {
                DesignUtils.instance.createDrawable(
                    it,
                    GradientDrawable.RECTANGLE
                )
            }
        }
        binding.redGalleryButton.apply {
            background = simplifier.brand?.category?.color?.let {
                DesignUtils.instance.createDrawable(
                    it,
                    GradientDrawable.RECTANGLE
                )
            }
        }
        binding.buttonBackFromExterior.apply {
            background = simplifier.brand?.category?.color?.let {
                DesignUtils.instance.createDrawable(
                    it,
                    GradientDrawable.RECTANGLE
                )
            }
        }
        binding.buttonBackFromInterior.apply {
            background = simplifier.brand?.category?.color?.let {
                DesignUtils.instance.createDrawable(
                    it,
                    GradientDrawable.RECTANGLE
                )
            }
        }
        binding.redExteriorButton.apply {
            background = simplifier.brand?.category?.color?.let {
                DesignUtils.instance.createDrawable(
                    it,
                    GradientDrawable.RECTANGLE
                )
            }
        }

    }

    private fun observeData() {

        viewModel.callbackError.observe(viewLifecycleOwner, Observer {
            if (it) {
                showErrorDialog("callback error")
                viewModel.callbackError.value = false
            }

        })
        viewModel.videos.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                offersAdapter.refresh(it)
                offers_container.visibility = View.VISIBLE
                //checkForRecyclerPosition(it)

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
                viewModel.acceptConditionError.value = false
            }
        })
        viewModel.acceptConditionError.observe(viewLifecycleOwner, Observer {
            if (it) {
                showErrorDialog(getString(R.string.accept_conditions))
                viewModel.acceptConditionError.value = false
            }
        })
        viewModel.phoneError.observe(viewLifecycleOwner, Observer {
            if (it) {
                showErrorDialog(getString(R.string.invalid_phone))
                viewModel.phoneError.value = false
            }
        })

        viewModel.nameError.observe(viewLifecycleOwner, Observer {
            if (it) {
                showErrorDialog(getString(R.string.empty_name))
                viewModel.nameError.value = false
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
        viewModel.errorFavorite.observe(viewLifecycleOwner, Observer {
            if (it) {
                binding.favoriteImg.setColorFilter(R.color.colorWhite)
            }


        })
        viewModel.trimsLoading.observe(viewLifecycleOwner, Observer {
            binding.progressCircular.isVisible = it
            binding.clickHereUpIcon.isClickable = false
            binding.clickHereTxtV.isClickable = false
            binding.programClickHereTxtV.isClickable = !it
            binding.programsClickHereUpIcon.isClickable = !it
            binding.clickHereContainer.isClickable = false
            if (it) {
                binding.emptyDataTxt.container.isVisible = false
            }
        })

        viewModel.images.observe(viewLifecycleOwner, Observer { imageGallery ->
            if (!imageGallery.isNullOrEmpty()) {
                imageGalleryAdapter.refreshData(imageGallery)
                simplifier.category?.let {
                    imageSliderAdapter.refreshData(imageGallery, it.isVertical)
                } ?: run {
                    imageSliderAdapter.refreshData(imageGallery)
                }

                gallery_recycler.findViewHolderForAdapterPosition(0)?.itemView?.performClick()
                binding.selectedImage = ImageSimplifier(imageGallery[0])
            }
//            else{
//                binding.galleryBtn.isVisible = false
//                binding.galleryButton.isVisible = false
//            }
        })

        viewModel.trimsList.observe(viewLifecycleOwner) { trims ->
            val filtered = trims.filter { it.isEnabled == true }
            if (filtered.isNotEmpty()) {
                binding.clickHereUpIcon.isClickable = true
                binding.clickHereTxtV.isClickable = true
                binding.clickHereContainer.isClickable = true

                filtered[0].let { trim ->
                            selectedTrim = trim
                            trim.colors?.let { colors ->
                                trimsColorAdapter.refreshData(colors)
                                binding.endArrow.isVisible = colors.size > 9
                                binding.startArrow.isVisible = colors.size > 9
                            }
                        }
                    createTrimsLayout(filtered)
            }
        }

        viewModel.offersList.observe(viewLifecycleOwner){ offers ->
            if (offers?.filter { s -> s.isEnabled == true }?.isNotEmpty() == true) {

                listOfferList.clear()
                offers.filter { s -> s.type == 2 && s.isEnabled == true }.let {
                    if (it.isNotEmpty()) {
                        listOfferList.add(it as java.util.ArrayList<Offer>)
                        mainListOfferList.add(it)
                    }
                }
                offers.filter { s -> s.type != 2 && s.isEnabled == true }.let {
                    if (it.isNotEmpty()) {
                        listOfferList.add(it as java.util.ArrayList<Offer>)
                        mainListOfferList.add(it)
                    }
                }
                //mainListOfferList = listOfferList
                offersAdapter.refreshOffers(listOfferList)
                offers_container.visibility = View.VISIBLE

            }
            if(::selectedTrim.isInitialized)
                viewModel.getOffersByTrimID(selectedTrim.id)
        }

        viewModel.trims.observe(viewLifecycleOwner, Observer { trims ->
            if (!trims.isNull()) {
                if (trims.trims?.filter { it.isEnabled == true }!!.isNotEmpty()) {
                    trims.trims?.let{
                        if (it.size > 0) {
                            it[0]?.let{ trim ->
                                selectedTrim = trim
                                    trim.colors?.let { colors ->
                                    trimsColorAdapter.refreshData(colors)
                                    binding.endArrow.isVisible = colors.size > 9
                                    binding.startArrow.isVisible = colors.size > 9
                                }
                            }
                         }
                       // createTrimsLayout(trims)
                    }
                }

                if (trims.offers?.filter { s -> s.isEnabled == true }?.isNotEmpty() == true) {

                    listOfferList.clear()
                    trims.offers?.filter { s -> s.type == 2 && s.isEnabled == true }?.let {
                        if (it.isNotEmpty()) {
                            listOfferList.add(it as java.util.ArrayList<Offer>)
                            mainListOfferList.add(it)
                        }
                    }
                    trims.offers?.filter { s -> s.type != 2 && s.isEnabled == true }?.let {
                        if (it.isNotEmpty()) {
                            listOfferList.add(it as java.util.ArrayList<Offer>)
                            mainListOfferList.add(it)
                        }
                    }
                    //mainListOfferList = listOfferList
                    offersAdapter.refreshOffers(listOfferList)
                    offers_container.visibility = View.VISIBLE

                }
                if(::selectedTrim.isInitialized)
                viewModel.getOffersByTrimID(selectedTrim.id)
            }
        })
        viewModel.offers.observe(viewLifecycleOwner, Observer { offers ->
            if (offers.filter { s -> s.isEnabled == true }?.isNotEmpty()) {
                var list: ArrayList<List<Offer>> = arrayListOf()
                //listOfferList.clear()
                offers.filter { s -> s.type == 10 && s.isEnabled == true }.let {
                    if (it.isNotEmpty()) {
                        list.add(it as java.util.ArrayList<Offer>)
                    }
                }
                offers.filter { s -> s.type != 10 && s.isEnabled == true }.let {
                    if (it.isNotEmpty()) {
                        list.add(it as java.util.ArrayList<Offer>)
                    }
                }
                offersAdapter.refreshOffers(list)
                offers_container.visibility = View.VISIBLE

            }else{
                if (listOfferList.size > 0){
                   // listOfferList = mainListOfferList
                      /* mainListOfferList.forEach{
                           val name = it.first().translations?.en?.name
                           print(name)
                       }*/
                    offersAdapter.refreshOffers(listOfferList)
                    offers_container.visibility = View.VISIBLE
                }else{
                    offers_container.visibility = View.GONE
                }
            }
        })

        viewModel.empty.observe(viewLifecycleOwner, Observer {
            binding.emptyDataTxt.container.isVisible = it
        })
        viewModel.error.observe(viewLifecycleOwner, Observer {
            if (it != "") {
                DesignUtils.instance.showErrorDialog(requireContext(), "", it) {

                }
            }
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer {
            binding.progressCircular.isVisible = it
            binding.favoriteImg.isEnabled = !it
        })
        viewModel.noConectionError.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it) {
                viewModel.verifyPhone.value = false
                DesignUtils.instance.showErrorDialog(requireContext(), null, null) {

                }
            }
        })

        /*viewModel.verifyPhone.observe(viewLifecycleOwner, Observer {
            if (it) {
                viewModel.phoneNumber.get()?.let { it1 ->
                    dialog = SimpleDialog.emptyInstance(it1, {
                        viewModel.verifyPhone.value = true
                        viewModel.requestCallback()
                        dialog.dismiss()
                    }, {
                        viewModel.verifyPhone.value = false
                    })
                } ?: kotlin.run { return@Observer }
                fragmentManager?.let { dialog.show(it, SimpleDialog.TAG) }
            }

        })*/
        viewModel.emptyGallery.observe(viewLifecycleOwner, Observer {
            red_gallery_button.isVisible = !it
            gallery_btn.isVisible = !it
            var x = simplifier.internalImage
            var y = simplifier.externalImage
            if (simplifier.internalImage == null || simplifier.internalImage == "" ) {
                end_gallery_layout.isVisible = !it
                interior_layout.isVisible = false
                red_interior_button.isVisible = false
            } else if (simplifier.externalImage == null || simplifier.externalImage == "") {
                start_gallery_layout.isVisible = !it
                exterior_layout.isVisible = false
                red_exterior_button.isVisible = false
            }
        })


        /*viewModel.model.observe(viewLifecycleOwner, Observer {
           populate(it)
        })*/

    }

    private fun checkForRecyclerPosition(list: List<Image>) {
        if (list.isNotEmpty()) {
            if (viewModel.trims.value?.offers?.isEmpty() == true) {
                offerAdapter.refresh(list)

            } else {
                if (list.size > 1) {
                    binding.videosContainer.isVisible = true
                    videoAdapter.refresh(list)
                }
                if (list.size == 1) {
                    offerAdapter.datas = listOf()
                    offerAdapter.add(list)
                    binding.allOffersText.text = getString(R.string.videos_and_offers)
                }
            }
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
            viewModel.verifyPhone.value = false
            errorDialog.dismiss()


        }

    }

    private fun createTrimsLayout(trims: List<Trim>) {
        val enabledTrims = trims.filter { it.isEnabled == true }.sortedBy { it.position }
       /* enabledTrims?.forEach {
            val trimSimplifier = TrimSimplifier(it)
            trims_tabLayout.addTab(trims_tabLayout.newTab().setText(trimSimplifier.name))
        }*/
        trims_viewPager.apply {

            adapter = enabledTrims.let { ViewStateAdapter(it, childFragmentManager, simplifier,lifecycle) }

            /*var pagerAdapter =
                enabledTrims?.let { TrimsPagerAdapter(it, childFragmentManager, simplifier) }
            pagerAdapter?.notifyDataSetChanged()
            adapter = pagerAdapter*/

        }
        TabLayoutMediator(trims_tabLayout, trims_viewPager) { tab, position ->
            enabledTrims?.let { list ->
                list[position]?.let { trim->
                    val trimSimplifier = TrimSimplifier(trim)
                    tab.text = trimSimplifier.name
                }
            }

        }.attach()

        if (trims_tabLayout.tabCount == 2) {
            trims_tabLayout.tabMode = TabLayout.MODE_FIXED
        } else {
            trims_tabLayout.tabMode = TabLayout.MODE_SCROLLABLE
        }
        trims_tabLayout.setTabTextColors(
            R.color.colorBlack,
            simplifier.brand?.category?.setBgColor() ?: R.color.colorPrimaryDark
        )
        allotEachTabWithEqualWidth()
        trims_tabLayout.selectTab(trims_tabLayout.getTabAt(0))
        if (enabledTrims?.size!! > 0) {
            selectedTrim = enabledTrims.get(0)!!
        }

        if (enabledTrims[0].price == 0) {
            if (simplifier.price != "0" && simplifier.price != "") {
                model_price.text = simplifier.price
            } else {
                model_price.text = ""
                val listAction = simplifier.actions.filter { it.identifier != "finance" }
                simplifier.brand?.category?.setBgColor()?.let {
                    actionAdapter.refreshActions(
                        listAction.filter { action -> action.isEnabled == true }.distinct(),
                        it
                    )
                }
                if (listAction.size == 2) {
                    (binding.gridActionsRecycler.layoutManager as GridLayoutManager).spanCount = 2
                }
                simplifier.actions = listAction.filter { action -> action.isEnabled == true }
            }
            binding.model = simplifier
        } else
            model_price.text = enabledTrims[0].let { TrimSimplifier(it).price }
        trims_tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    try {

                        trimsColorAdapter.refreshData(trims[tab.position].colors)
                        selectedTrim = enabledTrims[tab.position]
                        binding.endArrow.isVisible = false
                            //enabledTrims[tab.position].colors?.size > 9
                        binding.startArrow.isVisible = false
                            //enabledTrims[tab.position].colors.size > 9
                        if (enabledTrims[tab.position].price == 0) {
                            if (simplifier.price != "0" && simplifier.price != "") {
                                model_price.text = simplifier.price
                                var listAction =
                                    simplifier.actions
                                if (listAction.size == 2) {
                                    (binding.gridActionsRecycler.layoutManager as GridLayoutManager).spanCount =
                                        2
                                }
                                simplifier.brand?.category?.setBgColor()?.let {
                                    actionAdapter.refreshActions(
                                        listAction.filter { action -> action.isEnabled == true }.distinct(),
                                        it
                                    )
                                }
                            } else {
                                model_price.text = ""
                                val listAction =
                                    simplifier.actions.filter { it.identifier != "finance" }
                                if (listAction.size == 2) {
                                    (binding.gridActionsRecycler.layoutManager as GridLayoutManager).spanCount =
                                        2
                                }
                                simplifier.brand?.category?.setBgColor()?.let {
                                    actionAdapter.refreshActions(
                                        listAction.filter { action -> action.isEnabled == true }.distinct(),
                                        it
                                    )
                                }
                            }
                        } else model_price.text =
                            enabledTrims.get(tab.position).let { TrimSimplifier(it).price }

                        viewModel.getOffersByTrimID(selectedTrim.id)
                    } catch (e: IndexOutOfBoundsException) {
                    }

                }
            }

        })


    }

    private fun allotEachTabWithEqualWidth() {
        val slidingTabStrip = binding.trimsTabLayout.getChildAt(0) as ViewGroup
        for (i in 0 until binding.trimsTabLayout.tabCount) {
            val tab = slidingTabStrip.getChildAt(i)
            val layoutParams = tab.layoutParams as LinearLayout.LayoutParams
            layoutParams.weight = 1f
            tab.layoutParams = layoutParams
        }
    }

    private fun populateModel() {

        binding.model = simplifier
        if (simplifier.hasGallery) {
            viewModel.getModelGallery(simplifier.id)

        }else{
            gallery_btn.isVisible = false
            red_gallery_button.isVisible = false
            gallery_btn.isVisible = false
        }
        if (simplifier.hasVideo) {
            //lifecycleScope.launch {
            //delay(500)
            viewModel.getModelVideos(simplifier.id)
            // }
        }
        if (simplifier.externalImage != null && simplifier.externalImage != "") {
            exterior_layout.isVisible = true
            red_exterior_button.isVisible = true
            binding.exteriorWebView.settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
            //binding.exteriorWebView.settings.setAppCacheMaxSize(5 * 1024 * 1024) // 5MB
            //binding.exteriorWebView.settings.setAppCachePath(context?.cacheDir?.absolutePath)
            binding.exteriorWebView.settings.allowFileAccess = true
            //binding.exteriorWebView.settings.setAppCacheEnabled(true)
            binding.exteriorWebView.settings.cacheMode = WebSettings.LOAD_DEFAULT
            binding.exteriorWebView.settings.javaScriptEnabled = true
            if (!NetworkUtils.instance.connected) { // loading offline
                binding.exteriorWebView.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            }
            binding.exteriorWebView.loadUrl(simplifier.externalImage!!)
        } else red_exterior_button.isVisible = false
        if (simplifier.internalImage != null && simplifier.internalImage != "") {
            interior_layout.isVisible = true
            red_interior_button.isVisible = true
            binding.interiorWebView.settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
            //binding.interiorWebView.settings.setAppCacheMaxSize(5 * 1024 * 1024) // 5MB
            //binding.interiorWebView.settings.setAppCachePath(context?.cacheDir?.absolutePath)
            binding.interiorWebView.settings.allowFileAccess = true
            //binding.interiorWebView.settings.setAppCacheEnabled(true)
            binding.interiorWebView.settings.cacheMode = WebSettings.LOAD_DEFAULT
            binding.interiorWebView.settings.javaScriptEnabled = true
            if (!NetworkUtils.instance.connected) {  // loading offline
                binding.interiorWebView.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            }
            binding.interiorWebView.loadUrl(simplifier.internalImage!!)
        }else interior_layout.isVisible = false
    }

    private fun onClickListener() {
        binding.shareButton.setOnClickListener {
            val txtToShare =
                if (isEnglish) "Check Now ${simplifier.name} on Showroomz App📱With our 360 Degree feature 🚘" else
                    "شاهد ${simplifier.name} الآن على تطبيق شورومز📱..حصريا بطريقة 360 درجة 🚘"
            /* val share =  Intent(Intent.ACTION_SEND)
             share.type = "text/html"
             share.putExtra(Intent.EXTRA_SUBJECT, "Share");
             share.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(txtToShare));

             //share.putExtra(Intent.EXTRA_TEXT, "https://prod.showroomz.com/md-${simplifier.slug}")
             startActivity(Intent.createChooser(share, "share"))*/



            ShareCompat.IntentBuilder.from(requireActivity())
                .setType("text/plain")
                .setChooserTitle(txtToShare)
                .setText("$txtToShare - https://prod.showroomz.com/md-${simplifier.slug}")
                .startChooser();
        }
        binding.favoriteImg.setOnClickListener {
            if (!viewModel.isFavorite.get()) {
                viewModel.addToFavorite()
                // viewModel.isFavorite.set(true)
            } else {
                viewModel.removeFromFavoriteList()
                // viewModel.isFavorite.set(false)
            }


        }
        binding.startArrow.setOnClickListener {
            if (isEnglish) binding.colorRecycler.smoothScrollToPosition(0) else binding.colorRecycler.smoothScrollToPosition(
                binding.colorRecycler.childCount
            )
        }
        binding.endArrow.setOnClickListener {
            if (isEnglish) binding.colorRecycler.smoothScrollToPosition(binding.colorRecycler.childCount) else binding.colorRecycler.smoothScrollToPosition(
                0
            )

        }
        /*offerAdapter.setOnItemCLickListener(object : OfferAdapter.OnItemClickListener {
            override fun onItemClick(offer:OfferSimplifier) {
                offer.contents?.let{
                    if(it.size > 1) {
                        val action =
                            ModelDetailFragmentDirections.actionShowModelOffers(simplifier, false, null)
                        view?.let { it1 -> Navigation.findNavController(it1).navigate(action) }
                        simplifier.name?.let { Log.e("onClickListener", it) }
                    }else{
                        showCashbackOfferWithoutBank(offer)
                    }
                } ?: run {
                    val action =
                        ModelDetailFragmentDirections.actionShowModelOffers(simplifier, false, null)
                    view?.let { it1 -> Navigation.findNavController(it1).navigate(action) }
                    simplifier.name?.let { Log.e("onClickListener", it) }
                }

            }
        })*/

        actionAdapter.setOnItemCLickListener(object : ActionsAdapter.OnItemClickListener {
            override fun onItemClick(action: Action?) {
                if (action?.identifier.equals("finance_with_appraisal")) {
                    showApparaiseCarBottomSheet(1)

                }
                if (action?.identifier.equals("finance")) {
                    navigateToFinanceFragment()

                }
                if (action?.identifier.equals("compare")) {
//                    viewModel.putModelToCompare()
                    Navigation.findNavController(requireView())
                        .navigate(ModelDetailFragmentDirections.showCompare(simplifier.model))
                }
                if (action?.identifier.equals("callback")) {

                    showCallbackDialog()

                    //showCallbackDialog()
                }
                if (action?.identifier.equals("book_now")) {
                    navigateToBookNow()

                }
                if (action?.identifier.equals("book_now_with_appraisal")) {
                    showApparaiseCarBottomSheet(2)

                }
                if (action?.identifier.equals("buy_now")) {

                    navigatetoBuyNow()
                }
                if (action?.identifier.equals("buy_now_with_appraisal")) {

                    showApparaiseCarBottomSheet(3)
                }

                if (action?.identifier.equals("test_drive")) {
                    if (simplifier.testDrive == 40 ) {
                        showTestDiriveDialog()
                    }

                    if (simplifier.testDrive == 30) {
                        Navigation.findNavController(requireView()).navigate(
                            ModelDetailFragmentDirections.actionShowTestDriveDateReservationFragment(
                                simplifier.model, 0
                            )
                        )
                    }
                    if (simplifier.testDrive == 20 || simplifier.testDrive == 0) {
                        Navigation.findNavController(requireView()).navigate(
                            ModelDetailFragmentDirections.actionShowTestDriveDateReservationFragment(
                                simplifier.model, 1
                            )
                        )
                    }
                }

                if (action?.identifier.equals("navigate_to_link")){
                    if (!simplifier.link.isNullOrBlank()) {
                        simplifier.link?.let{
                            var url = it
                            if (!url.startsWith("http://") && !url.startsWith("https://"))
                                url = "http://$url"
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            startActivity(browserIntent)
                        }
                    }
                }

                if (action?.identifier.equals("call")) {
                    val callIntent = Intent(Intent.ACTION_CALL)
                    callIntent.data = Uri.parse("tel:${simplifier.phoennbr}")
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.CALL_PHONE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        startActivity(callIntent)
                    } else {

                        if (PermissionUtils.getNBR(
                                requireContext(),
                                Manifest.permission.CALL_PHONE
                            ) > 1
                        ) {
                            displayNeverAskAgainDialog()

                        } else {
                            requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), 1)
                            PermissionUtils.setNBR(
                                requireActivity(),
                                Manifest.permission.CALL_PHONE
                            )
                        }
                    }
                }
            }
        })
        programs_click_here_up_icon.setOnClickListener {
            showProgramBottomSheet()
            simplifier.brand?.id?.let { it1 ->
                simplifier.brand?.cat?.id?.let { it2 ->
                    LogProgressRepository.logProgress(
                        "Programs_screen",
                        category = it2,
                        dealerData = it1,
                        modelData = simplifier.id,
                        trim = if (::selectedTrim.isInitialized) selectedTrim.id else ""
                    )
                }
            }

        }
        program_click_here_txtV.setOnClickListener {
            showProgramBottomSheet()
            simplifier.brand?.id?.let { it1 ->
                simplifier.brand?.cat?.id?.let { it2 ->
                    LogProgressRepository.logProgress(
                        "Programs_screen",
                        category = it2,
                        dealerData = it1,
                        modelData = simplifier.id,
                        trim = selectedTrim.id
                    )
                }
            }
        }
        gallery_image_slider.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                gallery_recycler.smoothScrollToPosition(position)

                imageGalleryAdapter.selectedItem = position
                imageGalleryAdapter.notifyDataSetChanged()
                (gallery_recycler.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                    position,
                    0
                )


            }

        })
        imageSliderAdapter.setOnItemCLickListener(object : ImageSliderAdapter.OnItemClickListener {
            override fun onItemClick(image: Image) {
                DesignUtils.instance.setVisibilityWithSlide(gallery_recycler, Gravity.BOTTOM)


            }
        })

        exit_icon.setOnClickListener {
            hideModelImage()
        }
        imageGalleryAdapter.setOnItemCLickListener(object :
            ImageGalleryAdapter.OnItemClickListener {
            override fun onItemClick(image: Image) {
                gallery_image_slider.currentItem = viewModel.images.value!!.indexOf(image)
            }

        })
        gallery_btn.setOnClickListener {
            activity?.let { act ->
                (act as MainActivity).hideBannerLayout()
            }
            gallery_container.visibility = View.VISIBLE
            interior_container.visibility = View.GONE
            exterior_container.visibility = View.GONE
            simplifier.brand?.id?.let { it1 ->
                simplifier.brand?.cat?.id?.let { it2 ->
                    LogProgressRepository.logProgress(
                        "Gallery_screen",
                        category = it2,
                        dealerData = it1,
                        modelData = simplifier.id
                    )
                }
            }
            if (!NetworkUtils.instance.connected) {
                DesignUtils.instance.showErrorDialog(requireContext(), null, null) {

                }
            }
        }
        red_gallery_button.setOnClickListener {
            activity?.let { act ->
                (act as MainActivity).hideBannerLayout()
            }
            gallery_container.visibility = View.VISIBLE
            interior_container.visibility = View.GONE
            exterior_container.visibility = View.GONE
            simplifier.brand?.id?.let { it1 ->
                simplifier.brand?.cat?.id?.let { it2 ->
                    LogProgressRepository.logProgress(
                        "Gallery_screen",
                        category = it2,
                        dealerData = it1,
                        modelData = simplifier.id
                    )
                }
            }
            if (!NetworkUtils.instance.connected) {
                DesignUtils.instance.showErrorDialog(requireContext(), null, null) {

                }
            }
        }

        gallery_button.setOnClickListener {
            activity?.let { act ->
                (act as MainActivity).hideBannerLayout()
            }
            gallery_container.visibility = View.VISIBLE
            interior_container.visibility = View.GONE
            exterior_container.visibility = View.GONE
            simplifier.brand?.id?.let { it1 ->
                simplifier.brand?.cat?.id?.let { it2 ->
                    LogProgressRepository.logProgress(
                        "Gallery_screen",
                        category = it2,
                        dealerData = it1,
                        modelData = simplifier.id
                    )
                }
            }
            if (!viewModel.showVerticalGallery.get()) {
                activity?.let {
                    DesignUtils.instance.changeScreenOrientation(
                        it,
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    )
                }
            }
            if (!NetworkUtils.instance.connected) {
                DesignUtils.instance.showErrorDialog(requireContext(), null, null) {

                }
            }

        }
        gallery.setOnClickListener {
            activity?.let { act ->
                (act as MainActivity).hideBannerLayout()
            }
            gallery_container.visibility = View.VISIBLE
            interior_container.visibility = View.GONE
            exterior_container.visibility = View.GONE
            simplifier.brand?.id?.let { it1 ->
                simplifier.brand?.cat?.id?.let { it2 ->
                    LogProgressRepository.logProgress(
                        "Gallery_screen",
                        category = it2,
                        dealerData = it1,
                        modelData = simplifier.id
                    )
                }
            }
            if (!viewModel.showVerticalGallery.get()) {
                activity?.let {
                    DesignUtils.instance.changeScreenOrientation(
                        it,
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    )
                }
            }
            if (!NetworkUtils.instance.connected) {
                DesignUtils.instance.showErrorDialog(requireContext(), null, null) {

                }
            }
        }
        back_button.setOnClickListener {
            if (Navigation.findNavController(it).previousBackStackEntry?.destination?.id == R.id.splashFragment) {
                val navHostFragment =
                    activity?.supportFragmentManager?.findFragmentById(R.id.fragment) as NavHostFragment
                val navController = navHostFragment.navController
                navController.navigate(R.id.dashboardFragment)
            } else {
                Navigation.findNavController(it).navigateUp()
            }
        }

        exterior_button.setOnClickListener {
            activity?.let { act ->
                (act as MainActivity).hideBannerLayout()
            }
            simplifier.brand?.id?.let { it1 ->
                simplifier.brand?.cat?.id?.let { it2 ->
                    LogProgressRepository.logProgress(
                        "Exterior_screen",
                        category = it2,
                        dealerData = it1,
                        modelData = simplifier.id
                    )
                }
            }
           // if (!viewModel.showVerticalGallery.get()) {
                activity?.let {
                    DesignUtils.instance.changeScreenOrientation(
                        it,
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    )
                }
           // }
            interior_container.visibility = View.GONE
            exterior_container.visibility = View.VISIBLE
            if (!NetworkUtils.instance.connected) {
                DesignUtils.instance.showErrorDialog(requireContext(), null, null) {

                }
            }
        }
        start_gallery_layout.setOnClickListener {
            activity?.let { act ->
                (act as MainActivity).hideBannerLayout()
            }
            gallery_container.visibility = View.VISIBLE
            interior_container.visibility = View.GONE
            exterior_container.visibility = View.GONE
            simplifier.brand?.id?.let { it1 ->
                simplifier.brand?.cat?.id?.let { it2 ->
                    LogProgressRepository.logProgress(
                        "Gallery_screen",
                        category = it2,
                        dealerData = it1,
                        modelData = simplifier.id
                    )
                }
            }
            if (!viewModel.showVerticalGallery.get()) {
                activity?.let {
                    DesignUtils.instance.changeScreenOrientation(
                        it,
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    )
                }
            }
            if (!NetworkUtils.instance.connected) {
                DesignUtils.instance.showErrorDialog(requireContext(), null, null) {

                }
            }
        }
        end_gallery_layout.setOnClickListener {
            activity?.let { act ->
                (act as MainActivity).hideBannerLayout()
            }
            gallery_container.visibility = View.VISIBLE
            interior_container.visibility = View.GONE
            exterior_container.visibility = View.GONE
            simplifier.brand?.id?.let { it1 ->
                simplifier.brand?.cat?.id?.let { it2 ->
                    LogProgressRepository.logProgress(
                        "Gallery_screen",
                        category = it2,
                        dealerData = it1,
                        modelData = simplifier.id
                    )
                }
            }
            if (!viewModel.showVerticalGallery.get()) {
                activity?.let {
                    DesignUtils.instance.changeScreenOrientation(
                        it,
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    )
                }
            }
            if (!NetworkUtils.instance.connected) {
                DesignUtils.instance.showErrorDialog(requireContext(), null, null) {

                }
            }
        }
        interior_button.setOnClickListener {
            activity?.let { act ->
                (act as MainActivity).hideBannerLayout()
            }
            interior_container.visibility = View.VISIBLE
            exterior_container.visibility = View.GONE
            simplifier.brand?.id?.let { it1 ->
                simplifier.brand?.cat?.id?.let { it2 ->
                    LogProgressRepository.logProgress(
                        "Interior_screen",
                        category = it2,
                        dealerData = it1,
                        modelData = simplifier.id
                    )
                }
            }
            //if (!viewModel.showVerticalGallery.get()) {
                activity?.let {
                    DesignUtils.instance.changeScreenOrientation(
                        it,
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    )
                }
            //}
            if (!NetworkUtils.instance.connected) {
                DesignUtils.instance.showErrorDialog(requireContext(), null, null) {

                }
            }
        }

        red_exterior_button.setOnClickListener {
            activity?.let { act ->
                (act as MainActivity).hideBannerLayout()
            }
            interior_container.visibility = View.GONE
            exterior_container.visibility = View.VISIBLE
            simplifier.brand?.id?.let { it1 ->
                simplifier.brand?.cat?.id?.let { it2 ->
                    LogProgressRepository.logProgress(
                        "Exterior_screen",
                        category = it2,
                        dealerData = it1,
                        modelData = simplifier.id
                    )
                }
            }
            if (!NetworkUtils.instance.connected) {
                DesignUtils.instance.showErrorDialog(requireContext(), null, null) {

                }
            }

        }

        red_interior_button.setOnClickListener {
            activity?.let { act ->
                (act as MainActivity).hideBannerLayout()
            }
            interior_container.visibility = View.VISIBLE
            exterior_container.visibility = View.GONE
            simplifier.brand?.id?.let { it1 ->
                simplifier.brand?.cat?.id?.let { it2 ->
                    LogProgressRepository.logProgress(
                        "Interior_screen",
                        category = it2,
                        dealerData = it1,
                        modelData = simplifier.id
                    )
                }
            }
            if (!NetworkUtils.instance.connected) {
                DesignUtils.instance.showErrorDialog(requireContext(), null, null) {

                }
            }
        }

        button_back_from_exterior.setOnClickListener {
            hideModelImage()
        }

        button_back_from_interior.setOnClickListener {
            hideModelImage()
        }

        click_here_container.setOnClickListener {
            small_actions_container.visibility = View.GONE
            grid_actions_container.visibility = View.VISIBLE
        }
        click_here_up_icon.setOnClickListener {
            small_actions_container.visibility = View.GONE
            grid_actions_container.visibility = View.VISIBLE
        }
        click_here_txtV.setOnClickListener {
            small_actions_container.visibility = View.GONE
            grid_actions_container.visibility = View.VISIBLE
        }
        click_here_dow_icon.setOnClickListener {
            small_actions_container.visibility = View.VISIBLE
            grid_actions_container.visibility = View.GONE
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

    private fun showApparaiseCarBottomSheet(direction: Int) {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        val showProgramBottomSheetBinding: ApprasialCarBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.apprasial_car_bottom_sheet,
            null,
            false
        )

        dialog.setContentView(showProgramBottomSheetBinding.root)
        dialog.show()
        showProgramBottomSheetBinding.yesTV.setOnClickListener {
            showAppraisalCar()
            dialog.dismiss()
        }
        showProgramBottomSheetBinding.noTV.setOnClickListener {
            dialog.dismiss()
            when (direction) {
                1 -> {
                    navigateToFinanceFragment()
                    dialog.dismiss()
                }
                2 -> {
                    navigateToBookNow()
                    dialog.dismiss()
                }
                3 -> {
                    navigatetoBuyNow()
                    dialog.dismiss()
                }
            }

        }
        showProgramBottomSheetBinding.exitBtn.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun showAppraisalCar() {
        if (viewModel.isConnected())
            Navigation.findNavController(requireView())
                .navigate(ModelDetailFragmentDirections.actionShowYourCarDetailsFragment(simplifier.model))
        else {
            CacheObjects.model = simplifier.model

            Navigation.findNavController(requireView())
                .navigate(ModelDetailFragmentDirections.showLogin(R.id.yourCarDetailsFragment))
        }
    }

    private fun navigatetoBuyNow() {
        if (viewModel.isConnected()) {
            Navigation.findNavController(requireView()).navigate(
                ModelDetailFragmentDirections.actionShowBuyNow(
                    simplifier.model,
                    selectedTrim
                )
            )
        } else {
            CacheObjects.model = simplifier.model
            CacheObjects.trim = selectedTrim
            Navigation.findNavController(requireView())
                .navigate(ModelDetailFragmentDirections.showLogin(R.id.buyNowFragment))
        }
    }

    private fun navigateToBookNow() {
        if (viewModel.isConnected()) {
            Navigation.findNavController(requireView()).navigate(
                ModelDetailFragmentDirections.actionShowBook(
                    simplifier.model,
                    selectedTrim,
                    null
                )
            )
        } else {
            CacheObjects.model = simplifier.model
            CacheObjects.trim = selectedTrim
            Navigation.findNavController(requireView())
                .navigate(ModelDetailFragmentDirections.showLogin(R.id.bookNowFragment))

        }
    }

    private fun navigateToFinanceFragment() {
        var trimSimplifier =
            viewModel.trimsList.value?.get(binding.trimsTabLayout.selectedTabPosition)
                ?.let {
                    TrimSimplifier(it)
                }
        viewModel.trimsList.value?.get(binding.trimsTabLayout.selectedTabPosition)
            ?.let {
                if ((simplifier.price != "0" && simplifier.price != "") && (it.price == 0 )) {
                    trimSimplifier?.price = simplifier.price
                }

            }
        val trim =
            viewModel.trimsList.value?.get(binding.trimsTabLayout.selectedTabPosition)
        val action =
            trim?.let {
                trimSimplifier?.let { it1 ->
                    ModelDetailFragmentDirections.actionShowFinance(
                        it1, null, simplifier.model
                    )
                }
            }
        view?.let { it1 ->
            action?.let {
                Navigation.findNavController(it1).navigate(it)
            }
        }
    }

    private fun showProgramBottomSheet() {
        if (::selectedTrim.isInitialized) {
            var selectedProgram: Program? = null
            val programAdapter = context?.let { it1 ->
                TrimProgramAdapter(
                    selectedTrim.programs.sortedBy { s -> s.contractPeriod },
                    it1
                )
            }
            val programsActionAdapter = ProgramActionsAdapter(actionList)

            programAdapter?.setOnItemCLickListener(object : TrimProgramAdapter.OnItemClickListener {
                override fun onItemClick(program: Program) {
                    selectedProgram = program
                }

            })
            val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
            val showProgramBottomSheetBinding: ModelDetailsProgramsServiceBottomSheetBinding =
                DataBindingUtil.inflate(
                    LayoutInflater.from(requireContext()),
                    R.layout.model_details_programs_service_bottom_sheet,
                    null,
                    false
                )

            dialog.setContentView(showProgramBottomSheetBinding.root)
            showProgramBottomSheetBinding.programActions.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = programsActionAdapter
            }
            programsActionAdapter.refreshActions(simplifier.actions.sortedBy { action -> action.position })
            programsActionAdapter.setOnItemCLickListener(object :
                ProgramActionsAdapter.OnItemClickListener {
                override fun onItemClick(action: Action?) {
                    if (action?.identifier.equals("finance")) {
                        var trimSimplifier =
                            viewModel.trimsList.value?.get(binding.trimsTabLayout.selectedTabPosition)
                                ?.let {
                                    TrimSimplifier(it)
                                }
                        viewModel.trimsList.value?.get(binding.trimsTabLayout.selectedTabPosition)
                            ?.let {
                                if ((simplifier.price != "0" && simplifier.price != "") && (it.price == 0)) {
                                    trimSimplifier?.price = simplifier.price
                                }

                            }
                        val trim =
                            viewModel.trimsList.value?.get(binding.trimsTabLayout.selectedTabPosition)
                        val action =
                            trim?.let {
                                trimSimplifier?.let { it1 ->
                                    ModelDetailFragmentDirections.actionShowFinance(
                                        it1, null, simplifier.model
                                    )
                                }
                            }
                        view?.let { it1 ->
                            action?.let {
                                Navigation.findNavController(it1).navigate(it)
                            }
                        }
                        dialog.dismiss()
                    }
                    if (action?.identifier.equals("compare")) {
//                    viewModel.putModelToCompare()
                        Navigation.findNavController(requireView())
                            .navigate(ModelDetailFragmentDirections.showCompare(simplifier.model))
                        dialog.dismiss()
                    }
                    if (action?.identifier.equals("test_drive")) {
                        if (simplifier.testDrive == 40 && simplifier.deliveryHours.size > 0) {
                            showTestDiriveDialog()

                        }
                        if (simplifier.testDrive == 40 && simplifier.deliveryHours.size == 0) {
                            Navigation.findNavController(requireView()).navigate(
                                ModelDetailFragmentDirections.actionShowTestDriveDateReservationFragment(
                                    simplifier.model, 0
                                )
                            )
                        }
                        if (simplifier.testDrive == 30) {
                            Navigation.findNavController(requireView()).navigate(
                                ModelDetailFragmentDirections.actionShowTestDriveDateReservationFragment(
                                    simplifier.model, 0
                                )
                            )
                        }
                        if (simplifier.testDrive == 10 && simplifier.deliveryHours.size > 0) {
                            Navigation.findNavController(requireView()).navigate(
                                ModelDetailFragmentDirections.actionShowTestDriveDateReservationFragment(
                                    simplifier.model, 1
                                )
                            )
                        }

                        dialog.dismiss()
                    }
                    if (action?.identifier == "book_rent") {
                        if (selectedProgram != null) {
                            if (viewModel.isConnected()) {

                                view?.let {
                                    Navigation.findNavController(it).navigate(
                                        ModelDetailFragmentDirections.actionShowBookNowForRent(
                                            simplifier.model,
                                            selectedTrim, selectedProgram?.id
                                        )
                                    )
                                }

                            } else {
                                CacheObjects.model = simplifier.model
                                CacheObjects.trim = selectedTrim
                                CacheObjects.program = selectedProgram
                                view?.let {
                                    Navigation.findNavController(it)
                                        .navigate(ModelDetailFragmentDirections.showLogin(R.id.bookNowRentFragment))
                                }
                            }
                            dialog.dismiss()
                        } else {
                            showProgramBottomSheetBinding.programsRecyclerView.startAnimation(
                                AnimationUtils.loadAnimation(
                                    activity,
                                    R.anim.shake
                                )
                            )
                        }
                    }
                    if (action?.identifier == "book_now") {
                        if (selectedProgram != null) {
                            if (viewModel.isConnected()) {
                                view?.let {
                                    Navigation.findNavController(it).navigate(
                                        ModelDetailFragmentDirections.actionShowBook(
                                            simplifier.model,
                                            selectedTrim, selectedProgram
                                        )
                                    )
                                }
                            } else {
                                CacheObjects.model = simplifier.model
                                CacheObjects.trim = selectedTrim
                                CacheObjects.program = selectedProgram
                                view?.let {
                                    Navigation.findNavController(it)
                                        .navigate(ModelDetailFragmentDirections.showLogin(R.id.bookNowFragment))
                                }
                            }

                            dialog.dismiss()
                        } else {
                            showProgramBottomSheetBinding.programsRecyclerView.startAnimation(
                                AnimationUtils.loadAnimation(
                                    activity,
                                    R.anim.shake
                                )
                            )
                        }


                    }
                    if (action?.identifier == "callback") {
                        //if (viewModel.isConnected())
                        showCallbackDialog()
                        // else
                        // (activity as MainActivity).showSMSVerificationBottomSheet()
                        dialog.dismiss()
                    }
                }

            })
            dialog.setCanceledOnTouchOutside(false)
            //if (!viewModel.trimsList.value.isNull()) {
                if (viewModel.trimsList.value?.isNotEmpty() == true) {
                    showProgramBottomSheetBinding.trim =
                        viewModel.trimsList.value?.get(trims_tabLayout.selectedTabPosition)

                    dialog.show()
                }
           // }
            showProgramBottomSheetBinding.programsRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = programAdapter
            }
            showProgramBottomSheetBinding.exitBtn.setOnClickListener {
                viewModel.verifyPhone.value = false
                dialog.dismiss()
            }


            showProgramBottomSheetBinding.moreImg.setOnClickListener {
                showProgramBottomSheetBinding.servicesRecycler.layoutManager?.childCount?.plus(1)
                    ?.let { it1 ->
                        showProgramBottomSheetBinding.servicesRecycler.smoothScrollToPosition(
                            it1
                        )
                    }
            }
        }
    }

    private fun showTestDiriveDialog() {
        var dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        var binding: TestDriveBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.test_drive_bottom_sheet,
            null,
            false
        )
        binding.viewModel = viewModel
        dialog.setContentView(binding.root)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        binding.exitBtn.setOnClickListener {
            viewModel.verifyPhone.value = false
            dialog.dismiss()
        }
        binding.testDriveAtShowroomBtn.setOnClickListener {
            dialog.dismiss()
            Navigation.findNavController(requireView()).navigate(
                ModelDetailFragmentDirections.actionShowTestDriveDateReservationFragment(
                    simplifier.model,
                    1
                )
            )
        }
        binding.confirmDeliverToMyLocation.setOnClickListener {
            dialog.dismiss()
            Navigation.findNavController(requireView()).navigate(
                ModelDetailFragmentDirections.actionShowTestDriveDateReservationFragment(
                    simplifier.model,
                    0
                )
            )
        }
    }

    private fun showCallbackDialog() {
        simplifier.brand?.id?.let { it1 ->
            simplifier.brand?.cat?.id?.let { it2 ->
                LogProgressRepository.logProgress(
                    "Model_callback_screen",
                    category = it2,
                    dealerData = it1,
                    modelData = simplifier.id,
                    trim = selectedTrim.id
                )
            }
        }


        val binding: CallbackBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.callback_bottom_sheet,
            null,
            false
        )

        binding.viewModel = viewModel
        binding.model = simplifier
        binding.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.acceptCondition.set(isChecked)
        }
        dialogCallback.setContentView(binding.root)
        dialogCallback.setCanceledOnTouchOutside(false)
        dialogCallback.show()
        binding.exitBtn.setOnClickListener {
            viewModel.verifyPhone.value = false
            dialogCallback.dismiss()

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
            viewModel.verifyPhone.value = false
            dialogCallback.dismiss()
            successCallbackDialog.dismiss()

        }
    }

    private fun hideModelImage() {
        interior_container.visibility = View.GONE
        exterior_container.visibility = View.GONE
        gallery_container.visibility = View.GONE
        activity?.let {
            DesignUtils.instance.changeScreenOrientation(
                it,
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //viewModel = ViewModelProviders.of(this).get(ModelDetailVM::class.java)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {


                if (interior_container.visibility == View.VISIBLE || exterior_container.visibility == View.VISIBLE || gallery_container.isVisible)
                    hideModelImage()
                else {
                    if (Navigation.findNavController(view!!).previousBackStackEntry?.destination?.id == R.id.splashFragment) {
                        val navHostFragment =
                            activity?.supportFragmentManager?.findFragmentById(R.id.fragment) as NavHostFragment
                        val navController = navHostFragment.navController
                        navController.navigate(R.id.dashboardFragment)
                    } else {
                        Navigation.findNavController(view!!).navigateUp()
                    }


                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    fun showCashbackOfferWithoutBank(offer: OfferSimplifier) {
        val binding: OffersPopUpBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(requireContext()),
                R.layout.offers_pop_up,
                null,
                false
            )
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        dialog.setContentView(binding.root)
        dialog.setCancelable(false)
        binding.offerType.text = offer.name
        binding.price.text = offer.contents?.first()
        TextViewCompat.setAutoSizeTextTypeWithDefaults(
            binding.price,
            TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE
        )
        binding.cancel.setOnClickListener({ v -> dialog.dismiss() })
        val window = dialog.window
        window!!.setLayout(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        window.setGravity(Gravity.CENTER)
        dialog.show()
    }
}