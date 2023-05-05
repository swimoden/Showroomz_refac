package com.kuwait.showroomz.view.fragment

import android.Manifest.permission.*
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.FragmentDashboardBinding
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.extras.Shared
import com.kuwait.showroomz.extras.TOKEN
import com.kuwait.showroomz.extras.USER_ID
import com.kuwait.showroomz.model.data.User
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.model.simplifier.BrandSimplifier
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.view.adapters.*
import com.kuwait.showroomz.viewModel.CategoryVM
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlin.system.exitProcess


class DashboardFragment : Fragment() {
   // private lateinit var viewModel: CategoryVM
   private val viewModel by viewModels<CategoryVM>()
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var listener: NavController.OnDestinationChangedListener
    lateinit var binding: FragmentDashboardBinding
    private lateinit var shared: Shared
    private lateinit var categoryAdapter: DashboardCategoryAdapter
    private lateinit var itemAdapter: NavigationDrawerAdapter
    private val callbacksAdapter: DashboardCallbackAdapter by lazy {
        DashboardCallbackAdapter(listOf())
    }
    private val testDriveAdapter: DashboardTestDriveAdapter by lazy {
        DashboardTestDriveAdapter(listOf())
    }
    private val favoriteAdapter: DashboardFavoriteAdapter by lazy {
        DashboardFavoriteAdapter(listOf())

    }
    private val financeCallback: DashboardFinanceCallbackAdapter by lazy {
        DashboardFinanceCallbackAdapter(listOf())

    }
    private val financeRequest: DashboardFinanceCallbackAdapter by lazy {
        DashboardFinanceCallbackAdapter(listOf())

    }
    private val brandsAdapter: ExclusiveBrandAdapter by lazy {
        ExclusiveBrandAdapter(listOf()) {
            val action = DashboardFragmentDirections.actionShowModelsByBrandId(it, null)
            view?.let { it1 -> Navigation.findNavController(it1).navigate(action) }
        }
    }
    private val brandsOffersAdapter: ExclusiveBrandAdapter by lazy {
        ExclusiveBrandAdapter(listOf()) {
            val action = DashboardFragmentDirections.actionShowModelsByBrandId(it, null)
            view?.let { it1 -> Navigation.findNavController(it1).navigate(action) }
        }
    }
    private val trendingAdapter: TrendingModelAdapter by lazy {
        TrendingModelAdapter(listOf()) {
            val action = DashboardFragmentDirections.aactionShowModelDetail(it,
                null, null)
            view?.let { it1 -> Navigation.findNavController(it1).navigate(action) }
        }
    }
    private val recently: RecentlyViewedModelAdapter by lazy {
        RecentlyViewedModelAdapter(listOf()) {
            val action = DashboardFragmentDirections.aactionShowModelDetail(it,
                null, null)
            view?.let { it1 -> Navigation.findNavController(it1).navigate(action) }
        }
    }
    private val topAdsAdapter: TopAdsAdapter by lazy {
        TopAdsAdapter(listOf()) {

            (activity as MainActivity).navigateFromAds(it)
        }
    }
    override fun onResume() {
        super.onResume()
        viewModel.getRecentlyViewed()



        if (shared.existKey(USER_ID)) {

            viewModel.fetchFinanceCallbacks()
            viewModel.fetchCallbacks()
            viewModel.getTestDrive()
        }
        LogProgressRepository.logProgress("Dashbord_screen")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard, container, false)
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        initNavigationViewAdapter()
        categoryAdapter = DashboardCategoryAdapter(arrayListOf(), activity)
        navController = findNavController()


        binding.menuButton.setOnClickListener {

            if (binding.drawer.isOpen) {
                binding.drawer.close()
            } else {
                initNavigationViewAdapter()
                binding.drawer.open()
            }
        }
        /*navController.addOnDestinationChangedListener { controller, destination, arguments ->
            Log.e("OnDestinationListener", destination.label.toString())

        }*/
        DesignUtils.instance.setStatusBar(
            requireActivity() as MainActivity,
            false,
            ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark)
        )

        return binding.root
    }



    private fun initNavigationViewAdapter() {
        itemAdapter = NavigationDrawerAdapter(
            activity, this@DashboardFragment, listOf(
                DrawerItem(R.id.profileFragment, getString(R.string.profile)),
                DrawerItem(R.id.request_amountFragment, getString(R.string.finance)),
                DrawerItem(R.id.favoriteFragment, getString(R.string.favorites)),
                DrawerItem(R.id.languageFragment, getString(R.string.language)),
                DrawerItem(R.id.contactUsFragment, getString(R.string.contact_us)),
                DrawerItem(R.id.termsConditionsFragment, getString(R.string.terms_conditions)),
                DrawerItem(R.id.aboutAppFragment, getString(R.string.aboutApp))

            )
        )

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        shared = activity?.let { Shared(it) }!!
        view.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
       // viewModel = ViewModelProviders.of(this).get(CategoryVM::class.java)


        username.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        if (shared.existKey(USER_ID)) {
            shared.string(USER_ID)?.let {
                val local = LocalRepo()
                local.getOne<User>(it)?.let{
                    username.text = it.fullName
                }
            }

        }
        viewModel.initRecyclersStatus()
        binding.fbBtn.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/showroomzkw/"))
            startActivity(browserIntent)
        }
        binding.instaBtn.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/showroomzkw/"))
            startActivity(browserIntent)
        }
        binding.twiterBtn.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/showroomz"))
            startActivity(browserIntent)
        }
        if (::itemAdapter.isInitialized) {
            binding.itemsRecycler.apply {
                layoutManager = LinearLayoutManager(this.context)
                adapter = itemAdapter
            }
        }
       /* binding.navigationView.rootView.findViewById<RecyclerView>(R.id.itemsRecycler).apply {
            adapter = itemAdapter
            layoutManager = LinearLayoutManager(this.context)
        }*/
        ads_recycler.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = topAdsAdapter
        }


        category_recycler.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
            smoothScrollToPosition(0)
        }
        binding.dashboardRecyclerExclusive.apply {
            adapter = brandsAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            smoothScrollToPosition(0)
        }
          binding.offersRecycler.apply {
              adapter = brandsOffersAdapter
              layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
              smoothScrollToPosition(0)
          }
        binding.dashboardRecyclerTrending.apply {
            adapter = trendingAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            smoothScrollToPosition(0)
        }
        itemAdapter.selectedItem = -1
        binding.recentlyRecycler.apply {
            adapter = recently
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            smoothScrollToPosition(0)
        }
        binding.dashboardRecyclerCallback.apply {
            adapter = callbacksAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            smoothScrollToPosition(0)
        }
        binding.dashboardRecyclerFinanceCallback.apply {
            adapter = financeCallback
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            smoothScrollToPosition(0)
        }
        binding.dashboardRecyclerFinanceRequest.apply {
            adapter = financeRequest
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            smoothScrollToPosition(0)
        }
        binding.testDriveRecycler.apply {
            adapter = testDriveAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            smoothScrollToPosition(0)
        }
        binding.favoriteRecycler.apply {
            adapter = favoriteAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            smoothScrollToPosition(0)
        }
        val ads = ( requireActivity() as MainActivity).getTopAds(null, 60, 40)
        if (ads.size > 0){
            ads_recycler.isVisible = true
            topAdsAdapter.refresh(ads)
            ads.forEach{
                (activity as MainActivity).viewModel.incrementNbView(it)
            }
        }
        observeCategories()
        onClickListener()
    }

    private fun onClickListener() {
        binding.customizeBtn.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(DashboardFragmentDirections.actionShowCustomizeDashboard())
        }
    }


    private fun observeCategories() {
        viewModel.noConnectionError.observe(viewLifecycleOwner, Observer {
            if (it){
                DesignUtils.instance.showErrorDialog(requireContext(), null,null){

                }
            }
        })
        viewModel.categories.observe(viewLifecycleOwner, Observer { categories ->
            if (categories.isNotEmpty())
                categoryAdapter.refresh(categories)
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer { isloading ->
            if (isloading) print("Loading") else print("not loading")
        })

        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            print(error)
        })
        viewModel.brands.observe(viewLifecycleOwner, Observer { brands ->
            if (brands.isNotEmpty() && viewModel.isEnableExclusive.get()) {
                exclusive_offers_textView.visibility = View.VISIBLE
                brandsAdapter.refresh(brands)
            }
        })
        viewModel.brandsHasOffer.observe(viewLifecycleOwner, Observer { brands ->
            if (brands.isNotEmpty() ) {
                offers_textView.apply {
                    visibility = View.VISIBLE
                    text=BrandSimplifier(brands.get(0)).headerName
                }
                brandsOffersAdapter.refresh(brands)
            }
        })
        viewModel.trendingModels.observe(viewLifecycleOwner, Observer { models ->
            if (models.isNotEmpty() && viewModel.isEnableTrending.get()) {
                trendingAdapter.refresh(models)
                binding.trendingContainer.visibility = View.VISIBLE
            }

        })
        viewModel.recentlyModel.observe(viewLifecycleOwner, Observer { models ->
            if (models.isNotEmpty() && viewModel.isEnableRecently.get()) {
                binding.recentlyContainer.visibility = View.VISIBLE
                recently.refresh(models)
            }

        })
        viewModel.callbacks.observe(viewLifecycleOwner, Observer { models ->
            if (models.isNotEmpty() && viewModel.isEnableCallback.get()) {
                binding.callbackContainer.visibility = View.VISIBLE
                callbacksAdapter.refresh(models)
            }

        })
        viewModel.financeCallbacks.observe(viewLifecycleOwner, Observer { models ->
            if (models.isNotEmpty() && viewModel.isEnableFinanceCallback.get()) {
                binding.financeCallbackContainer.visibility = View.VISIBLE
                financeCallback.refresh(models)
            }

        })
        viewModel.financeRequest.observe(viewLifecycleOwner, Observer { models ->
            if (models.isNotEmpty() && viewModel.isEnableFinanceRequest.get()) {
                binding.financeRequestContainer.visibility = View.VISIBLE
                financeRequest.refresh(models)
            }

        })
        viewModel.testDrive.observe(viewLifecycleOwner, Observer { models ->
            if (models.isNotEmpty() && viewModel.isEnableTestDrive.get()) {
                binding.testDriveContainer.visibility = View.VISIBLE
                testDriveAdapter.refresh(models)
            }

        })
        viewModel.favoriteList.observe(viewLifecycleOwner, Observer { list ->
            if (list.isNotEmpty()) {
                binding.favoriteContainer.visibility = View.VISIBLE
                favoriteAdapter.refresh(list)
            } else {
                binding.favoriteContainer.visibility = View.GONE
                favoriteAdapter.refresh(list)
            }

        })
        itemAdapter.setOnItemCLickListener(object : NavigationDrawerAdapter.OnItemClickListener {
            override fun onItemClick(item: DrawerItem) {
                when (item.id) {
                    R.id.profileFragment -> {
                        if (shared.existKey(TOKEN)) {
                            binding.drawer.close()
                            navController.navigate(DashboardFragmentDirections.actionShowProfile())
                        } else {
                            binding.drawer.close()

                            navController.navigate(
                                DashboardFragmentDirections.actionShowLoginFragment(
                                    R.id.profileFragment
                                )
                            )
                        }

                    }
                    R.id.favoriteFragment -> {
                        binding.drawer.close()
                        navController.navigate(DashboardFragmentDirections.showFavoriteList())
                    }
                    R.id.contactUsFragment -> {
                        binding.drawer.close()
                        navController.navigate(DashboardFragmentDirections.showContactUs())
                    }
                    R.id.termsConditionsFragment -> {
                        binding.drawer.close()
                        navController.navigate(DashboardFragmentDirections.showTerms())
                    }
                    R.id.aboutAppFragment -> {
                        binding.drawer.close()
                        navController.navigate(DashboardFragmentDirections.showAboutApp())
                    }
                    R.id.request_amountFragment -> {
                    binding.drawer.close()
                    navController.navigate(DashboardFragmentDirections.showRequestAmount(null))
                }
                }
            }

        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                exitProcess(2)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    fun reloadFragment() {
        binding.drawer.close()
        //requireFragmentManager().beginTransaction().detach(this).attach(this).commit()
        val action = DashboardFragmentDirections.reloadFragment()
        view?.let { it1 -> Navigation.findNavController(it1).navigate(action) }
       /* itemAdapter = NavigationDrawerAdapter(
            activity, this@DashboardFragment, listOf(
                DrawerItem(R.id.profileFragment, getString(R.string.profile)),
                DrawerItem(R.id.financeFragment, getString(R.string.finance)),
                DrawerItem(R.id.favoriteFragment, getString(R.string.favorites)),
                DrawerItem(R.id.languageFragment, getString(R.string.language)),
                DrawerItem(R.id.contactUsFragment, getString(R.string.contact_us)),
                DrawerItem(R.id.termsConditionsFragment, getString(R.string.terms_conditions)),
                DrawerItem(R.id.aboutAppFragment, getString(R.string.aboutApp))

            )
        )
        itemAdapter.notifyDataSetChanged()*/
    }

}
