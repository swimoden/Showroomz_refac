package com.kuwait.showroomz.view.fragment

import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.FragmentBrandsBinding
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.extras.Shared.Companion.oldIndex
import com.kuwait.showroomz.extras.Shared.Companion.selectedIndex
import com.kuwait.showroomz.model.data.*
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.model.simplifier.BrandSimplifier
import com.kuwait.showroomz.model.simplifier.CategorySimplifier
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.view.adapters.BrandsAdapter
import com.kuwait.showroomz.view.adapters.BrandsCategoryAdapter
import com.kuwait.showroomz.viewModel.BrandsVM
import io.realm.RealmObject
import kotlinx.android.synthetic.main.fragment_brands.*
import kotlinx.android.synthetic.main.fragment_brands.back
import kotlinx.android.synthetic.main.fragment_brands.filter_btn
import kotlinx.android.synthetic.main.fragment_brands.serch_txt


class BrandsFragment : Fragment() {

    lateinit var binding: FragmentBrandsBinding
    private lateinit var viewModel: BrandsVM
    lateinit var categoryParent: Category
    private lateinit var simplifier: CategorySimplifier
    var childes: List<Category> = arrayListOf()
    private var brands: List<Brand> = arrayListOf()
    var list: List<RealmObject>? = arrayListOf()
    var item: DashBoardItem? = null
    var size = 0
    var indexExc = 0
    //var selectedIndex = 0
    //var oldIndex = 0
    val shared = Shared()
    var showEvery = 4
    private lateinit var selectedCategory: Category

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_brands, container, false)
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (::categoryParent.isInitialized)
        LogProgressRepository.logProgress("Brands_screen", category = categoryParent.id)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(BrandsVM::class.java)
        arguments?.let { bundle ->
            BrandsFragmentArgs.fromBundle(bundle).cat?.let {
                categoryParent = it.parentObj() ?: it
                val catChild = BrandsFragmentArgs.fromBundle(bundle).catChild
                simplifier = CategorySimplifier(categoryParent)
                binding.category = simplifier
                childes = simplifier.childes()
                if (catChild.isNull())
                    selectedIndex = if (selectedIndex < 0) 0 else selectedIndex
                else {
                    for (index in childes.indices) {
                        if (catChild?.id == childes[index].id) {
                            selectedIndex = index
                            break
                        }
                    }
                    selectedIndex = if (selectedIndex < 0) 0 else selectedIndex
                }
                if (childes.size > selectedIndex) {
                    childes[selectedIndex].selected = true
                    populate()
                }


            } ?: run {
                BrandsFragmentArgs.fromBundle(bundle).slug?.let {
                    viewModel.getCategoryBySlug(it) { category ->
                        category.parentObj()?.let{
                            categoryParent = it
                            simplifier = CategorySimplifier(categoryParent)
                            binding.category = simplifier
                            childes = simplifier.childes()
                                for (index in childes.indices) {
                                    if (category.id == childes[index].id) {
                                        selectedIndex = index
                                        break
                                    }
                                }
                                selectedIndex = if (selectedIndex < 0) 0 else selectedIndex
                        }?: run {
                            categoryParent = category
                            simplifier = CategorySimplifier(categoryParent)
                            binding.category = simplifier
                            childes = simplifier.childes()
                            childes[selectedIndex].selected = true
                            selectedIndex = if (selectedIndex < 0) 0 else selectedIndex
                        }
                        childes[selectedIndex].selected = true
                        populate()
                    }
                }
            }
        }
        binding.emptyDataTxt.container.isVisible = false
        clickListener()
        observeBrands()
    }

    fun populate(){
        DesignUtils.instance.setStatusBar(
            requireActivity() as MainActivity,
            false,
            simplifier.setBgColor()
        )
        if (childes.size < 2) {
            child_category_recycler.isVisible = false
        } else {
            child_category_recycler.apply {
                layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = categoryAdapter
            }
        }
        brands_recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = brandsAdapter
        }
        if (!::selectedCategory.isInitialized)
            selectedCategory = childes[selectedIndex]
        if (selectedCategory.isForAppraisal) {
            animateAppraisalView()
        } else {
            initAppraiseViewsVisibility()
            getBrandByCatId(selectedCategory.usedFor, selectedCategory.id)
        }
        (activity as MainActivity).checkForCategoryAds(selectedCategory)
    }

    private fun getBrandByCatId(usedFor: String?, id: String) {
        usedFor?.let { viewModel.refresh(it, id) }
    }

    private val categoryAdapter: BrandsCategoryAdapter by lazy {
        BrandsCategoryAdapter(childes) {
            selectedIndex = childes.indexOfFirst { s -> s.id == it.id }
            updateSelection(it)
            //Log.e("categoryAdapter", "" + it.translations?.en?.name)
            (activity as MainActivity).checkForCategoryAds(it)
        }
    }

    private val brandsAdapter: BrandsAdapter by lazy {
        BrandsAdapter(brands, item, size, indexExc, {
            if (!appraisal_view.isVisible) {
                val sim = BrandSimplifier(it)
                if (sim.hasModels!!) {
                    val action = BrandsFragmentDirections.actionShowModelsByBrandId(it, null)
                    val destination = findNavController().graph.findNode(R.id.brandsFragment2)
                    if (destination?.getAction(R.id.action_show_models_by_brand_id) != null) {
                        view?.let { it1 -> Navigation.findNavController(it1).navigate(action) }
                    }
                   // view?.let { it1 -> Navigation.findNavController(it1).navigate(action) }
                } else {
                    val action = BrandsFragmentDirections.actionShowLocation(sim.id, it, emptyArray())
                    view?.let { it1 -> Navigation.findNavController(it1).navigate(action) }
                }
            }
        }, {
            (activity as MainActivity).navigateFromAds(it)
        },{ ads, action ->
            (activity as MainActivity).callAction(ads,action)
        })
    }

    fun updateSelection(category: Category) {
        if (selectedIndex != oldIndex) {

            for (item in childes) {
                item.selected = category.id == item.id
            }
            if (category.isForAppraisal) {
                animateAppraisalView()
            } else {
                initAppraiseViewsVisibility()
                getBrandByCatId(category.usedFor, category.id)
            }
            selectedCategory = category
            categoryAdapter.refresh(childes)
            brands_recycler.smoothScrollToPosition(0)
        }

    }

    private fun observeBrands() {
        viewModel.noConectionError.observe(viewLifecycleOwner, Observer {
            if (it) {
                DesignUtils.instance.showErrorDialog(requireContext(), null, null) {

                }
            }
        })
        viewModel.empty.observe(viewLifecycleOwner, Observer {
            binding.emptyDataTxt.container.isVisible = it
            binding.brandsRecycler.isVisible = !it
        })
        viewModel.brands.observe(viewLifecycleOwner, Observer { brands ->

            indexExc = 0
            item = null
            list = brands
            size = list?.size ?: 0
            if (selectedCategory.showAdsOnTop) {
                val ads = (requireActivity() as MainActivity).getTopAds(selectedCategory, 10, 40)
                if (ads.size > 0) {
                    item = DashBoardItem("", null, ads)
                } else {
                    filterBrands()
                }
            } else {
                filterBrands()
            }
            if (selectedCategory.showAdsOnScroll){
                val ads = (requireActivity() as MainActivity).getTopAds(selectedCategory, 10, 50)
                if (ads.size > 0) {
                    val array:ArrayList<RealmObject> = arrayListOf()
                    list?.forEach {
                        array.add(it)
                    }
                    var y = 0
                    val x = array.size / showEvery
                    val total = array.size + x + showEvery
                    for (i in 1..total){
                        if (i % showEvery == 0){
                            if (i-1 < array.size) {
                                array.add(i-1, ads[y])
                            }else{
                                array.add(ads[y])
                            }
                            if (y == ads.size - 1) {
                                y = 0
                            }else{
                                y += 1
                            }
                        }
                    }

                    list = array
                    size = array.size
                }
            }
            brandsAdapter.refresh(item, list , size, indexExc)

            Handler().postDelayed({
                if (selectedIndex < oldIndex) {
                    child_category_recycler.smoothScrollToPosition(selectedIndex - (if (selectedIndex == 0) 0 else 1))
                } else {
                    child_category_recycler.smoothScrollToPosition(selectedIndex + (if (selectedIndex == childes.size - 1) 0 else 1))
                }
                oldIndex = selectedIndex
            }, 200)


        })
        viewModel.loading.observe(viewLifecycleOwner, Observer { isloading ->
            binding.progressCircular.isVisible = isloading

            brands_recycler.isVisible = !isloading
            if (isloading) {
                binding.emptyDataTxt.container.isVisible = false
                activity?.let { DesignUtils.instance.disableUserInteraction(it) }
            } else activity?.let { DesignUtils.instance.enableUserInteraction(it) }
            oldIndex = selectedIndex
        })

        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            print(error)
            binding.emptyDataTxt.container.isVisible = error.isNotEmpty()
        })
    }

    private fun filterBrands() {
        if (selectedCategory.showOffer) {
            size = 0
            list?.let {
                val bex = it.filter {
                    if (BrandSimplifier(it as Brand).showExclusiveOnTop == true) BrandSimplifier(it).isexclisive else BrandSimplifier(
                        it
                    ).isoffer
                }
                if (bex.size > 10) {
                    size += it.size
                    indexExc = bex.size
                } else {
                    if (bex.size in 1..10 && bex.size < it.size) {
                        size += 1
                        val title =
                            (BrandSimplifier(bex[0] as Brand).headerName) //if(BrandSimplifier(bex.get(0)).showExclusiveOnTop == true) (context?.getString(R.string.exclusive_offers)) else (BrandSimplifier(bex.get(0)).headerName)
                        item = title?.let { title -> DashBoardItem(title, bex as List<Brand>, null) }
                        list = it.filter {
                            !if (BrandSimplifier(it as Brand).showExclusiveOnTop == true) BrandSimplifier(
                                it
                            ).isexclisive else BrandSimplifier(
                                it
                            ).isoffer
                        }
                    } else if (bex.size == it.size) {
                        list = it
                    } else {
                        list = it.filter {
                            !if (BrandSimplifier(it as Brand).showExclusiveOnTop == true) BrandSimplifier(
                                it
                            ).isexclisive else BrandSimplifier(
                                it
                            ).isoffer
                        }
                    }
                    list?.let { listsize ->
                        size += listsize.size
                    }

                }
            }
        }
    }

    private fun clickListener() {
        binding.searchContainerTxt.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(BrandsFragmentDirections.actionNavigateToSearchFragment(selectedCategory))
        }
        appraise_btn.setOnClickListener {
           /* LogProgressRepository.logProgress(
                "Brands_screen_appraisal_btn_click",
                category = categoryParent.id
            )*/
            if (shared.existKey(USER_ID)) {
                Navigation.findNavController(requireView())
                    .navigate(BrandsFragmentDirections.actionNavigateToAppraisalFragment(null))
            } else {
                Navigation.findNavController(requireView())
                    .navigate(ModelDetailFragmentDirections.showLogin(R.id.yourCarDetailsFragment))
            }
        }
        back.setOnClickListener {
            if (Navigation.findNavController(it).previousBackStackEntry?.destination?.id == R.id.splashFragment) {
                val navHostFragment =
                    activity?.supportFragmentManager?.findFragmentById(R.id.fragment) as NavHostFragment
                val navController = navHostFragment.navController
                navController.navigate(R.id.dashboardFragment)
            } else {
                Navigation.findNavController(it).navigateUp()
            }
            // Navigation.findNavController(it).navigateUp()
            // .navigate(BrandsFragmentDirections.actionBackToDashbord())
        }

        serch_txt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel.search("$s")
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
        filter_btn.setOnClickListener {
            selectedCategory.let { cat ->
                Navigation.findNavController(it).navigate(
                    BrandsFragmentDirections.actionNavigateToFilterFragment(
                        cat,
                        selectedCategory
                    )
                )
            }
        }
    }

    private fun initAppraiseViewsVisibility() {
        title.isVisible = false
        sub.isVisible = false
        sub2.isVisible = false
        hand_with_key.isVisible = false
        car_img.isVisible = false
        hand_1.isVisible = false
        hand_2.isVisible = false
        hand_3.isVisible = false
        appraise_btn.isVisible = false
        appraisal_view.isVisible = false
        brands_recycler.isVisible = true
    }

    private fun animateAppraisalView() {
        initAppraiseViewsVisibility()
        appraisal_view.isVisible = true
        brands_recycler.isVisible = false
        val slideDown = AnimationUtils.loadAnimation(
            context,
            R.anim.slide_down
        )
        val slideDown2 = AnimationUtils.loadAnimation(
            context,
            R.anim.slide_down
        )
        val slideDown3 = AnimationUtils.loadAnimation(
            context,
            R.anim.slide_down
        )
        val slideUp = AnimationUtils.loadAnimation(
            context,
            R.anim.slide_up
        )
        val slideUp2 = AnimationUtils.loadAnimation(
            context,
            R.anim.slide_up
        )
        val slideLeft = AnimationUtils.loadAnimation(
            context,
            R.anim.slide_in_left
        )
        val slideRight = AnimationUtils.loadAnimation(
            context,
            R.anim.slide_in_right
        )
        val slideRight2 = AnimationUtils.loadAnimation(
            context,
            R.anim.slide_in_right
        )
        val slideRight3 = AnimationUtils.loadAnimation(
            context,
            R.anim.slide_in_right
        )
        Handler().postDelayed({
            if (selectedIndex < oldIndex) {
                child_category_recycler.smoothScrollToPosition(selectedIndex - (if (selectedIndex == 0) 0 else 1))
            } else {
                child_category_recycler.smoothScrollToPosition(selectedIndex + (if (selectedIndex == childes.size - 1) 0 else 1))
            }
            oldIndex = selectedIndex
        }, 100)
        title.isVisible = true
        title.startAnimation(slideDown)
        car_img.isVisible = true
        car_img.startAnimation(slideUp)
        slideDown.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
            }

            override fun onAnimationEnd(p0: Animation?) {
                hand_with_key.isVisible = true
                hand_with_key.startAnimation(slideLeft)
                slideLeft.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(p0: Animation?) {
                    }

                    override fun onAnimationEnd(p0: Animation?) {
                        sub.isVisible = true
                        sub.startAnimation(slideDown2)
                        slideDown2.setAnimationListener(object : Animation.AnimationListener {
                            override fun onAnimationStart(p0: Animation?) {
                            }

                            override fun onAnimationEnd(p0: Animation?) {
                                sub2.isVisible = true
                                sub2.startAnimation(slideDown3)
                                hand_2.isVisible = true
                                hand_2.startAnimation(slideRight)
                                slideRight.setAnimationListener(object :
                                    Animation.AnimationListener {
                                    override fun onAnimationStart(p0: Animation?) {
                                    }

                                    override fun onAnimationEnd(p0: Animation?) {
                                        // hand_1.isVisible = true
                                        hand_1.startAnimation(slideRight2)
                                        slideRight2.setAnimationListener(object :
                                            Animation.AnimationListener {
                                            override fun onAnimationStart(p0: Animation?) {
                                            }

                                            override fun onAnimationEnd(p0: Animation?) {
                                                hand_3.isVisible = true
                                                hand_3.startAnimation(slideRight3)
                                                slideRight3.setAnimationListener(object :
                                                    Animation.AnimationListener {
                                                    override fun onAnimationStart(p0: Animation?) {
                                                    }

                                                    override fun onAnimationEnd(p0: Animation?) {
                                                        appraise_btn.isVisible = true
                                                        appraise_btn.startAnimation(slideUp2)

                                                    }

                                                    override fun onAnimationRepeat(p0: Animation?) {
                                                    }

                                                })
                                            }

                                            override fun onAnimationRepeat(p0: Animation?) {
                                            }

                                        })
                                    }

                                    override fun onAnimationRepeat(p0: Animation?) {
                                    }

                                })
                            }

                            override fun onAnimationRepeat(p0: Animation?) {
                            }

                        })
                    }

                    override fun onAnimationRepeat(p0: Animation?) {
                    }

                })
            }

            override fun onAnimationRepeat(p0: Animation?) {
            }

        })

    }
}
