package com.kuwait.showroomz.view

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.text.InputType
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.tasks.Task
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.kuwait.showroomz.BuildConfig
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.*
import com.kuwait.showroomz.extras.*
import com.kuwait.showroomz.extras.MyApplication.Key.LANG
import com.kuwait.showroomz.extras.customViews.CircleProgressBar
import com.kuwait.showroomz.model.data.*
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.model.simplifier.AdsSimplifier
import com.kuwait.showroomz.model.simplifier.NotificationData
import com.kuwait.showroomz.view.adapters.AdsImageSliderAdapter
import com.kuwait.showroomz.view.adapters.ProgramActionsAdapter
import com.kuwait.showroomz.view.fragment.BrandsFragment
import com.kuwait.showroomz.view.fragment.SimpleDialog
import com.kuwait.showroomz.viewModel.MainVM
import com.yariksoffice.lingver.Lingver
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.circle_progress
import kotlinx.android.synthetic.main.activity_main.count_txt
import kotlinx.android.synthetic.main.ads_dialog.*
import kotlinx.android.synthetic.main.ads_dialog.program_actions
import kotlinx.android.synthetic.main.ads_fullscreen_dialog.*
import kotlinx.android.synthetic.main.email_update_dialog.*
import kotlinx.android.synthetic.main.fragment_edit_profile.view.*
import kotlinx.android.synthetic.main.fragment_request_amount.*
import kotlinx.android.synthetic.main.terms_and_services_dialog.*
import kotlinx.android.synthetic.main.terms_and_services_dialog.exit_btn
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.floor
import kotlin.text.isNullOrEmpty


class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {

    /* companion object {
         val dialogSms: BottomSheetDialog by lazy {
             BottomSheetDialog(
                 context,
                 R.style.AppTheme
             )
         }


     }*/

    private lateinit var setting: Setting
    private val TAG = "MainActivity"
    private val UPDATE_REQUEST_CODE = 1000
    private lateinit var navHostFragment: NavHostFragment
    private val adsDialog: Dialog by lazy { Dialog(this, R.style.AppTheme_FullScreenDialog) }
    private lateinit var fullScreenDialog: Dialog
    private lateinit var fullScreenVideoDialog: Dialog
    var selectedCategory: Category? = null
    private var imageList: List<Image> = listOf()
    private lateinit var shared: Shared
    private var advertisement: Advertisement? = null


    lateinit var viewModel: MainVM
    private var adsList: List<Advertisement> = arrayListOf<Advertisement>()

    private val imageSliderAdapter: AdsImageSliderAdapter by lazy {
        AdsImageSliderAdapter(imageList, this)
    }
    val successCallbackDialog: BottomSheetDialog by lazy {
        BottomSheetDialog(
            this,
            R.style.BottomSheetDialog
        )
    }


    val dialogCallback: BottomSheetDialog by lazy {
        BottomSheetDialog(
            this,
            R.style.BottomSheetDialog
        )
    }

    var dialoge = SimpleDialog(null, null)

    override fun onStop() {
        super.onStop()
        //viewModel.getDeviceActivityLogStop()
        Log.e(TAG, "onStop: 0")
    }


    fun check() {
        val (appUpdateManager, _) = pair
        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (::setting.isInitialized) {
                    if (getUpdateType(setting) == 0) {
                        if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                            popupSnackbarForCompleteUpdate()
                        }
                    }
                    if (getUpdateType(setting) == 1) {
                        if (appUpdateInfo.updateAvailability()
                            == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                        ) {
                            // If an in-app update is already running, resume the update.
                            appUpdateManager.startUpdateFlowForResult(
                                appUpdateInfo,
                                AppUpdateType.IMMEDIATE,
                                this,
                                UPDATE_REQUEST_CODE
                            )
                        }
                        if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) appUpdateManager.completeUpdate()
                    }
                }
            }

        val extras = intent!!.extras
        if (extras != null) {
            val notification = Utils.instance.buildNotificationData(extras)
            Log.e(TAG, "onNewIntent: ${notification.toString()}")
            navigateFromNotification(notification)
        }
        intent?.let { handleIntent(it) }
    }

    private fun handleIntent(intent: Intent) {
        val appLinkAction = intent.action
        val appLinkData: Uri? = intent.data
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment) as NavHostFragment
        val navController = navHostFragment.navController
        if (Intent.ACTION_VIEW == appLinkAction) {
            Log.e(TAG, "handleIntent: ${appLinkData?.path.toString()}")
            appLinkData?.path
            if (appLinkData?.path!!.startsWith("/Vehicle")) {

                val slug = appLinkData.query?.split("=")?.get(1)
                val bundle = Bundle()
                val model = viewModel.getModel(slug)
                model?.let { model ->
                    bundle.putParcelable("model", model)
                    bundle.putString("id", null)
                    bundle.putString("slug", null)
                } ?: run {
                    bundle.putParcelable("model", null)
                    bundle.putString("id", slug)
                    bundle.putString("slug", null)
                }
                navController.navigate(R.id.modelDetailsFragment, bundle)


                return
            }
            if (appLinkData.path!!.startsWith("/dd-")) {
                val slug = appLinkData.lastPathSegment?.split("dd-")?.get(1)
                val brandToGo = viewModel.getBrandBySlug(slug)
                val bundle = Bundle()
                brandToGo?.let { brandToGo ->
                    bundle.putParcelable("brand", brandToGo)
                    bundle.putString("slug", null)
                } ?: run {
                    bundle.putParcelable("brand", null)
                    bundle.putString("slug", slug)
                }
                navController.navigate(R.id.modelFragment, bundle)
                return
            }
            if (appLinkData.path!!.startsWith("/ca-")) {
                val slug = appLinkData.lastPathSegment?.split("ca-")?.get(1)
                val category = viewModel.getCategoryBySlug(slug)
                val bundle = Bundle()
                category?.let {
                    if (category.parent != null) {
                        bundle.putParcelable("cat", category.parentObj())
                        bundle.putParcelable("catChild", category)
                        bundle.putString("slug", null)
                    } else {
                        bundle.putParcelable("cat", category)
                        bundle.putParcelable("catChild", null)
                        bundle.putString("slug", null)
                    }
                } ?: run {
                    bundle.putParcelable("cat", null)
                    bundle.putParcelable("catChild", null)
                    bundle.putString("slug", slug)
                }
                Shared.selectedIndex = 0
                Shared.oldIndex = 0
                navController.navigate(R.id.brandsFragment2, bundle)
                return
            }
        }
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)

        val extras = intent!!.extras
        if (extras != null) {
            val notification = Utils.instance.buildNotificationData(extras)
            Log.e(TAG, "onNewIntent: ${notification.toString()}")
            navigateFromNotification(notification)

        }
        intent.let { handleIntent(it) }
    }

    private fun navigateFromNotification(notification: NotificationData?) {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment) as NavHostFragment
        val navController = navHostFragment.navController
        notification?.navigateToLink?.let {
            if (it == "true") {
                notification.link?.let {
                    var url = it
                    if (!url.startsWith("http://") && !url.startsWith("https://"))
                        url = "http://$url"
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(browserIntent)
                }
                return
            }
        }
        notification?.locationInApp?.let {
            when (it) {
                "10" -> {
                    Shared.selectedIndex = 0
                    Shared.oldIndex = 0
                    val parent = viewModel.getCategory(notification.parent_id)
                    val category = viewModel.getCategory(notification.id)
                    val bundle = Bundle()
                    bundle.putParcelable("cat", parent)
                    bundle.putParcelable("catChild", category)
                    bundle.putString("slug", null)
                    navController.navigate(R.id.brandsFragment2, bundle)
                }
                "20" -> {
                    val brandToGo = viewModel.getBrand(notification.dealerData)
                    val bundle = Bundle()
                    bundle.putParcelable("brand", brandToGo)
                    bundle.putString("slug", null)
                    navController
                        .navigate(R.id.modelFragment, bundle)

                }
                "30" -> {
                    val model = viewModel.getModel(notification.modelData)
                    val bundle = Bundle()
                    bundle.putParcelable("model", model)
                    bundle.putString("id", null)
                    bundle.putString("slug", null)
                    navController
                        .navigate(R.id.modelDetailsFragment, bundle)
                }
                "40" -> {
                    val bundle = Bundle()
                    val model = viewModel.getModel(notification.modelData)
                    val bank = viewModel.getBank(notification.bank)
                    bundle.putParcelable("model", model)
                    bundle.putParcelable("bank", bank)
                    bundle.putParcelable("trim", null)
                    navController
                        .navigate(R.id.financeFragment, bundle)
                }
                "50" -> {
                    val bundle = Bundle()
                    val model = viewModel.getModel(notification.modelData)
                    bundle.putParcelable("model", model)
                    bundle.putParcelable("bank", null)
                    bundle.putParcelable("trim", null)
                    navController
                        .navigate(R.id.financeFragment, bundle)
                }
                "60" -> {
                    navController.navigate(R.id.dashboardFragment)
                }
                else -> {
                    navController.navigate(R.id.dashboardFragment)
                }
            }
        } ?: run {
            notification?.callback_id?.let {
                notification.bank_id?.let { ite1 ->
                    val bundle = Bundle()
                    bundle.putString("callbackId", it)
                    bundle.putParcelable("callback", null)
                    navController.navigate(R.id.financeRequestDetailFragment, bundle)
                } ?: run {
                    val bundle = Bundle()
                    bundle.putString("appraisal_id", it)
                    bundle.putParcelable("callback", null)
                    navController.navigate(R.id.appraisalCarItemDetails, bundle)
                }
            } ?: run {
                navController.navigate(R.id.dashboardFragment)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.fade_in, android.R.anim.fade_out)
        setContentView(R.layout.activity_main)

        shared = Shared(this)
        shared.setString(LAST_SHOW_TIME_ADS, "0")
        viewModel = ViewModelProviders.of(this).get(MainVM::class.java)
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            LogProgressRepository.registerDeviceId(it.token)
        }
        val connectionLiveData = ConnectionLiveData(this)
        connectionLiveData.observe(this, Observer { isConnected ->
            isConnected?.let {
                NetworkUtils.instance.connected = isConnected
            }
        })

        /* if (shared.existKey(DISPLAYED_ADS)) {
             shared.removeKey(DISPLAYED_ADS)
         }*/
        fullScreenDialog = Dialog(this, R.style.AppTheme)
        fullScreenVideoDialog = Dialog(this, R.style.AppTheme)
        handleIntent(intent)

        adsDialog.setContentView(R.layout.ads_dialog)

        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment) as NavHostFragment


        val inflater = navHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.navigation)
        if (!NetworkUtils.instance.isNetworkAvailable(this)) {
            //graph.startDestination = R.id.splashFragment
            graph.setStartDestination(R.id.networkFragment)
        }

        navHostFragment.navController.graph = graph
        navHostFragment.navController.addOnDestinationChangedListener(this)

        if (!shared.string(LANG).isNullOrEmpty())
            setLocale(shared.string(LANG))

        // lifecycleScope.launch {
        //delay(500)
        observeData()
        viewModel.getAllData()
        check()
        // }

    }

    private fun exist(
        advertisement: Advertisement,
        historyAds: List<DisplayedAdvertisement>
    ): Boolean {

        /*historyAds.forEach {
            if (it.id == advertisement.id) {
                return true
            }
        }*/
        return false
    }


    private fun showAds(selectedCategory: String?, locationinapp: Int) {
        if (shared.existKey("show_ads")) {
            if (!shared.bool("show_ads")) {
                return
            }
        }
        if (!shared.existKey(LAST_SHOW_TIME_ADS)) {
            shared.setString(LAST_SHOW_TIME_ADS, Date().time.toString())
            return
        }
        val settings = viewModel.settings.value
        settings?.let{
            if (it.isEmpty()){
                return
            }
            val setting = it.first()
            val interval = if (setting?.adsTimeInterval == null) 120.0 else setting.adsTimeInterval!!.toDouble()
            val timing = if (shared.string(LAST_SHOW_TIME_ADS) == null) "0" else shared.string(LAST_SHOW_TIME_ADS)!!
            if (((Date().time - timing.toLong()) / 1000 ) > interval) {
                val filteredAds: ArrayList<Advertisement> = arrayListOf()
                val adss = viewModel.ads.value
                adss?.forEach { advertisement ->
                    selectedCategory?.let {
                        if (advertisement.locationInApp == locationinapp && advertisement.categoryToDisplay?.id == selectedCategory && advertisement.isEnabled == true && advertisement.mediaSize != 50 && advertisement.mediaSize != 40)
                            filteredAds.add(advertisement)
                    } ?: run {
                        if (advertisement.locationInApp == locationinapp && advertisement.isEnabled == true && advertisement.mediaSize != 50 && advertisement.mediaSize != 40)
                            filteredAds.add(advertisement)
                    }
                }
                if (filteredAds.size > 0) {
                    val ads = Utils.instance.getRandomAds(filteredAds)
                    displayAds(ads)
                }
            } else {
                viewModel.user?.let {
                    getTargetAds(it, selectedCategory, locationinapp)
                } ?: run {
                    if (shared.existKey(USER_ID)) {
                        shared.string(USER_ID)?.let {
                            val local = LocalRepo()
                            local.getOne<User>(it)?.let {
                                viewModel.user = it
                                getTargetAds(it, selectedCategory, locationinapp)
                            }
                        }
                    }
                }
            }
        }

    }

    private fun getTargetAds(it: User, selectedCategory: String?, locationinapp: Int) {
        val setting = viewModel.settings.value?.first()
        val filteredAds: ArrayList<Advertisement> = arrayListOf()
        val adss = viewModel.ads.value
        if (it.gender == 0) {
            val interval = if (setting?.frequencyMan == null) 60.0 else setting.frequencyMan!!.toDouble()
            val timing = if (shared.string(LAST_SHOW_TIME_ADS) == null) "0" else shared.string(LAST_SHOW_TIME_ADS)!!
            if ((Date().time - timing.toLong()) / 1000 > interval) {
                adss?.forEach { advertisement ->
                    selectedCategory?.let {
                        if (advertisement.locationInApp == locationinapp && advertisement.categoryToDisplay?.id == selectedCategory
                            && advertisement.isEnabled == true && advertisement.forMen == true && advertisement.mediaSize != 50 && advertisement.mediaSize != 40
                        )
                            filteredAds.add(advertisement)
                    } ?: run {
                        if (advertisement.locationInApp == locationinapp && advertisement.isEnabled == true && advertisement.forMen == true && advertisement.mediaSize != 50 && advertisement.mediaSize != 40)
                            filteredAds.add(advertisement)
                    }
                }
            }
        } else {
            val interval = if (setting?.frequencyWomen == null) 60.0 else setting.frequencyWomen!!.toDouble()
            val timing = if (shared.string(LAST_SHOW_TIME_ADS) == null) "0" else shared.string(LAST_SHOW_TIME_ADS)!!
            if ((Date().time - timing.toLong()) / 1000 > interval) {
          //  if ((Date().time - shared.string(LAST_SHOW_TIME_ADS)?.toLong()!!) / 1000 > setting?.frequencyWomen!!.toDouble() ) {
                adss?.forEach { advertisement ->
                    selectedCategory?.let {
                        if (advertisement.locationInApp == locationinapp && advertisement.categoryToDisplay?.id == selectedCategory
                            && advertisement.isEnabled == true && advertisement.forWomen == true && advertisement.mediaSize != 50 && advertisement.mediaSize != 40
                        )
                            filteredAds.add(advertisement)
                    } ?: run {
                        if (advertisement.locationInApp == locationinapp && advertisement.isEnabled == true && advertisement.forWomen == true && advertisement.mediaSize != 50 && advertisement.mediaSize != 40)
                            filteredAds.add(advertisement)
                    }
                }
            }
        }
        if (filteredAds.size > 0) {
            val ads = Utils.instance.getRandomAds(filteredAds)
            displayAds(ads)
        }
    }

    fun displayAds(ads: Advertisement) {
        when (ads.mediaSize) {
            10 -> {
                if (ads.imageGallery.isNotEmpty()) {
                    initFullScreenDialog(ads)
                } else
                    initVideoAdsDialog(ads)
                viewModel.incrementNbView(ads)
                if (shared.existKey(LAST_SHOW_TIME_ADS)) {
                    shared.removeKey(LAST_SHOW_TIME_ADS)
                    shared.setString(LAST_SHOW_TIME_ADS, Date().time.toString())
                } else {
                    shared.setString(LAST_SHOW_TIME_ADS, Date().time.toString())
                }
            }
            20 -> {
                initDialog(ads)
                viewModel.incrementNbView(ads)
                if (shared.existKey(LAST_SHOW_TIME_ADS)) {
                    shared.removeKey(LAST_SHOW_TIME_ADS)
                    shared.setString(LAST_SHOW_TIME_ADS, Date().time.toString())
                } else {
                    shared.setString(LAST_SHOW_TIME_ADS, Date().time.toString())
                }
            }
            30 -> {
                initBannerAds(ads)
                viewModel.incrementNbView(ads)
                if (shared.existKey(LAST_SHOW_TIME_ADS)) {
                    shared.removeKey(LAST_SHOW_TIME_ADS)
                    shared.setString(LAST_SHOW_TIME_ADS, Date().time.toString())
                } else {
                    shared.setString(LAST_SHOW_TIME_ADS, Date().time.toString())
                }
            }
        }

    }

    private var actionList: List<Action> = arrayListOf()
    private fun initDialog(ads: Advertisement) {
        val simplifier = AdsSimplifier(ads)
        val programsActionAdapter = ProgramActionsAdapter(actionList)
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        adsDialog.window?.setLayout(width, height)
        adsDialog.setCancelable(false)
        val videoView = adsDialog.findViewById<VideoView>(R.id.video_view)
        val videoContainer = adsDialog.findViewById<ConstraintLayout>(R.id.videoContainer)
        val pagerContainer = adsDialog.findViewById<ConstraintLayout>(R.id.imagePager)
        val title = adsDialog.findViewById<TextView>(R.id.title)
        title.text = simplifier.headline
        adsDialog.program_actions.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = programsActionAdapter
        }
        adsDialog.program_actions.isVisible = simplifier.actions.isNotEmpty()
        manipulateAction(simplifier, programsActionAdapter)
        // val actionbtn = adsDialog.findViewById<Button>(R.id.ads_action_btn)
        if (ads.mediaType == "video") {
            pagerContainer.visibility = View.GONE
            videoContainer.visibility = View.VISIBLE
            videoView.setVideoPath(ads.video)
            videoView.start()

            videoContainer.setOnClickListener {
                videoView.resume()
                navigateFromAds(ads)
            }
        } else {
            pagerContainer.visibility = View.VISIBLE
            videoContainer.visibility = View.GONE
            val pager = adsDialog.findViewById<ViewPager>(R.id.pager)
            val tablayout = adsDialog.findViewById<TabLayout>(R.id.tab_layout)
            pager.adapter = imageSliderAdapter
            imageSliderAdapter.refreshData(ads.imageGallery)
            tablayout.isVisible = ads.imageGallery.size > 1
            tablayout.setupWithViewPager(pager)
            imageSliderAdapter.setOnItemCLickListener(object :
                AdsImageSliderAdapter.OnItemClickListener {
                override fun onItemClick() {
                    navigateFromAds(ads)
                }
            })
        }
        val progressBar = adsDialog.findViewById<CircleProgressBar>(R.id.circle_progress)
        val countTxt = adsDialog.findViewById<TextView>(R.id.count_txt)
        progressBar.isEnabled = ads.allowSkip!!


        val countDownTimer = object : CountDownTimer(((ads.duration?.toLong()!! * 1000)), 10) {
            override fun onFinish() {
                progressBar.progress = 1f
                countTxt.text = "X"
                progressBar.isEnabled = true
            }

            override fun onTick(millisUntilFinished: Long) {
                val prog = ((ads.duration?.times(1000)
                    ?: 0) - millisUntilFinished).toFloat() / ads.duration?.times(1000)!!
                progressBar.progress =
                    prog
                countTxt.text =
                    ((millisUntilFinished.toFloat() / 1000) + 1).toInt().toString()

            }

        }
        countDownTimer.start()
        progressBar
            .setOnClickListener { adsDialog.dismiss() }
        adsDialog.show()
        val windowManager = window.attributes
        windowManager.dimAmount = 0.2f
        /* actionbtn.setOnClickListener {
             showCallbackBottomSheet(ads)
         }*/


    }

    private fun manipulateAction(simplifier: AdsSimplifier, adapter: ProgramActionsAdapter) {

        adapter.refreshActions(simplifier.actions.sortedBy { action -> action.position })
        adapter.setOnItemCLickListener(object :
            ProgramActionsAdapter.OnItemClickListener {
            override fun onItemClick(action: Action?) {
                callAction(simplifier, action)
            }
        })
    }

    fun callAction(simplifier: AdsSimplifier, action: Action?) {

        simplifier.id?.let {
            LogProgressRepository.logProgress(
                "Actions_${action?.identifier}", "", "", "", "",
                it, ""
            )
        }
        if (action?.identifier.equals("navigate_to_link")){
            if (simplifier.navigateToLink()){
                simplifier.link?.let {
                    var url = it
                    if (!url.startsWith("http://") && !url.startsWith("https://"))
                        url = "http://$url"
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(browserIntent)
                }
            }
        }else if (action?.identifier == "callback") {
            showCallbackBottomSheet(simplifier.advertisement)
        } else if (action?.identifier.equals("call")) {
            if (simplifier.link == null)
                return

            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:${simplifier.link}")
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.CALL_PHONE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startActivity(callIntent)
            } else {

                if (PermissionUtils.getNBR(
                        applicationContext,
                        Manifest.permission.CALL_PHONE
                    ) > 1
                ) {
                    displayNeverAskAgainDialog()

                } else {
                    requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), 1)
                    PermissionUtils.setNBR(
                        applicationContext,
                        Manifest.permission.CALL_PHONE
                    )
                }
            }
        } else if (action?.identifier == "lead_generation") {
            showCallbackBottomSheetLead(simplifier.advertisement)
        } else if (action?.identifier == "test_drive") {
            val bundle = Bundle()
            bundle.putParcelable("model", simplifier.fetchModel())
            bundle.putInt("atShowroom", 0)
            findNavController(this@MainActivity, R.id.fragment).navigate(
                R.id.testDriveDateFragment,
                bundle
            )
        } else {
            Toast.makeText(applicationContext, "not available", Toast.LENGTH_LONG).show()
        }
    }

    private fun displayNeverAskAgainDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(applicationContext)
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
                val uri: Uri = Uri.fromParts("package", applicationContext.packageName, null)
                intent.data = uri
                startActivity(intent)
            })
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }


    private fun showCallbackBottomSheet(ads: Advertisement) {
        viewModel.verifyPhone.value = true
        val bindingCallbackBottomSheet: AdsCallbackBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this),
            R.layout.ads_callback_bottom_sheet,
            null,
            false
        )
        if (!ads.modelToGo.isNull()) {

            viewModel.modelId = ads.modelToGo?.id.toString()
            val simplifier = AdsSimplifier(ads)
            val cat = simplifier.categorVm
            cat?.isCivilIdMandatory?.let {
                viewModel.isCivilIdMandatory.set(it)
                if (!it) {
                    bindingCallbackBottomSheet.civilId.hint =
                        resources.getString(R.string.civil_id_optional)
                }
            }
            cat?.isKFh?.let {
                viewModel.isKfh.set(it)
            }
            bindingCallbackBottomSheet.civilId.setText(viewModel.user?.civilID)

        }

        bindingCallbackBottomSheet.viewModel = viewModel

        dialogCallback.setContentView(bindingCallbackBottomSheet.root)
        dialogCallback.show()
        bindingCallbackBottomSheet.exitBtn.setOnClickListener {
            //viewModel.verifyPhone.value = true

            dialogCallback.dismiss()
        }
    }


    private fun showCallbackBottomSheetLead(ads: Advertisement) {

        val bindingCallbackBottomSheet: AdsCallbackBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this),
            R.layout.ads_callback_bottom_sheet,
            null,
            false
        )
        viewModel.callUserData()
        viewModel.isChangeToEmail.set(true)
        viewModel.adsId = ads.id.toString()

        //bindingCallbackBottomSheet.civilId.isVisible = true
        bindingCallbackBottomSheet.civilId.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        bindingCallbackBottomSheet.civilId.hint = getString(R.string.email)

        bindingCallbackBottomSheet.civilId.setText(viewModel.user?.email)
        viewModel.civilId.set(viewModel.user?.email)
        bindingCallbackBottomSheet.viewModel = viewModel
        dialogCallback.setContentView(bindingCallbackBottomSheet.root)
        dialogCallback.show()
        bindingCallbackBottomSheet.exitBtn.setOnClickListener {

            dialogCallback.dismiss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Log.d(this, "onActivityResult: [From Activity]:  " + requestCode + ", " + resultCode)
        val navHostFragment = supportFragmentManager.fragments.first() as? NavHostFragment
        if (navHostFragment != null) {
            val childFragments = navHostFragment.childFragmentManager.fragments
            childFragments.forEach { fragment ->
                fragment.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    /*private fun addADsToHistory(ads: Advertisement) {
        if (shared.existKey(DISPLAYED_ADS)) {
            var recently = shared.getList<DisplayedAdvertisement>(DISPLAYED_ADS)
            if ((recently.filter { advertisement -> advertisement.id == ads.id }).isEmpty()) {
                recently.add(DisplayedAdvertisement(ads.id))
                shared.removeKey(DISPLAYED_ADS)
                shared.setList(DISPLAYED_ADS, recently)
            }


        } else {
            var recently = arrayListOf<DisplayedAdvertisement>()
            recently.add(DisplayedAdvertisement(ads.id))
            shared.setList(DISPLAYED_ADS, recently)
        }
        if (shared.existKey(LAST_SHOW_TIME_ADS)) {
            shared.removeKey(LAST_SHOW_TIME_ADS)
            shared.setString(LAST_SHOW_TIME_ADS, Date().time.toString())
        } else shared.setString(LAST_SHOW_TIME_ADS, Date().time.toString())
    }*/

    private fun initVideoAdsDialog(ads: Advertisement) {
        fullScreenVideoDialog.setContentView(R.layout.ads_fullscreen_video_dialog)
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.MATCH_PARENT
        fullScreenVideoDialog.window?.setLayout(width, height)
        fullScreenVideoDialog.setCancelable(false)

        var videoView = fullScreenVideoDialog.findViewById<VideoView>(R.id.video_view)
        var progressBar =
            fullScreenVideoDialog.findViewById<CircleProgressBar>(R.id.circle_progress)
        var countTxt = fullScreenVideoDialog.findViewById<TextView>(R.id.count_txt)
        videoView.setVideoURI(Uri.parse( ads.video))
        progressBar.isEnabled = ads.allowSkip!!
        fullScreenVideoDialog.findViewById<ConstraintLayout>(R.id.videoContainer)
            .setOnClickListener {
                navigateFromAds(ads)
            }
        videoView.start()
        var countDownTimer = object : CountDownTimer(((ads.duration?.toLong()!! * 1000)), 10) {
            override fun onFinish() {
                progressBar.progress = 1f
                countTxt.text = "X"
                progressBar.isEnabled = true
            }

            override fun onTick(millisUntilFinished: Long) {
                var prog = ((ads.duration?.times(1000)
                    ?: 0) - millisUntilFinished).toFloat() / ads.duration?.times(1000)!!
                progressBar.progress =
                    prog
                countTxt.text =
                    ((millisUntilFinished.toFloat() / 1000) + 1).toInt().toString()

            }

        }
        countDownTimer.start()
        progressBar
            .setOnClickListener { fullScreenVideoDialog.dismiss() }
        fullScreenVideoDialog.show()
        val programsActionAdapter = ProgramActionsAdapter(actionList)
        fullScreenVideoDialog.program_actions.isVisible = actionList.isEmpty()
        fullScreenVideoDialog.program_actions.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = programsActionAdapter
        }
        fullScreenVideoDialog.program_actions.isVisible = ads.actionsBasic.isNotEmpty()
        manipulateAction(AdsSimplifier(ads), programsActionAdapter)
        val windowManager = window.attributes
        windowManager.dimAmount = 0.2f
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }


    private fun observeData() {
        viewModel.localSettings.observe(this, Observer {
            if (it.isNotEmpty()) {
                this.setting = it.first()
                checkForUpdate(it.first())
                checkForRemoteDataVersion(it.first())
            }

        })
        viewModel.ads.observe(this, Observer {
            adsList = it
        })
        viewModel.successCallback.observe(this, Observer {

            if (it) {
                viewModel.successCallback.value = false
                showSuccessCallbackBottomSheet()

            }
        })
        viewModel.civilIdError.observe(this, Observer {
            if (it) {
                if (viewModel.isChangeToEmail.get()) {
                    showErrorDialog(getString(R.string.invalid_email))
                } else {
                    showErrorDialog(getString(R.string.invalid_civil_id))
                }

            }
        })
        viewModel.phoneError.observe(this, Observer {
            if (it) {
                showErrorDialog(getString(R.string.invalid_phone))
            }
        })
        viewModel.nameError.observe(this, Observer {
            if (it) {
                showErrorDialog(getString(R.string.empty_name))
            }
        })
        viewModel.callbackLoading.observe(this, Observer {

            if (it) {
                dialogCallback.dismiss()
                adsDialog.findViewById<ProgressBar>(R.id.progress_circular).isVisible = true
                DesignUtils.instance.disableUserInteraction(this)
            } else {
                adsDialog.findViewById<ProgressBar>(R.id.progress_circular).isVisible = false
                DesignUtils.instance.enableUserInteraction(this)
            }

        })
        /*viewModel.verifyPhone.observe(this, Observer {
            if (it) {
                viewModel.phoneNumber.get()?.let { it1 ->
                    dialoge = SimpleDialog.emptyInstance(it1, {
                        viewModel.verifyPhone.value = true
                        viewModel.requestCallback()
                        dialoge.dismiss()
                    }, {
                        viewModel.verifyPhone.value = false
                    })

                } ?: kotlin.run { return@Observer }

                //dialoge.show(supportFragmentManager, SimpleDialog.TAG)
            }

        })*/
    }

    private fun showSuccessCallbackBottomSheet() {
        val bindingSuccess: CallbackSuccessBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this),
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
            adsDialog.dismiss()
            dialogCallback.dismiss()
            successCallbackDialog.dismiss()

        }
    }

    private fun showErrorDialog(message: String) {
        val errorDialog =
            BottomSheetDialog(this, R.style.BottomSheetDialog)
        val bindingError: ErrorBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this),
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
            viewModel.verifyPhone.value = true
            errorDialog.dismiss()


        }

    }

    private fun checkForRemoteDataVersion(setting: Setting) {

    }

    private val pair: Pair<AppUpdateManager, Task<AppUpdateInfo>>
        get() {
            val appUpdateManager = AppUpdateManagerFactory.create(this)
            val appUpdateInfoTask = appUpdateManager.appUpdateInfo
            return Pair(appUpdateManager, appUpdateInfoTask)
        }

    private fun checkForUpdate(setting: Setting) {
        val (appUpdateManager, appUpdateInfoTask) = pair
        val updateType = getUpdateType(setting)
        if (updateType == -1) return
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (
                appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(updateType)
            ) {
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    if (updateType == 1)
                        appUpdateManager.completeUpdate()
                    else if (updateType == 0) {
                        popupSnackbarForCompleteUpdate()
                    }
                }
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    updateType,
                    this,
                    UPDATE_REQUEST_CODE
                )

            }
        }

        /* if (viewModel.localSettings.value?.first() != null) {
             if (Gson().toJson(viewModel.settings.value?.first()?.androidVersion?.split('.'))!! > Gson().toJson(BuildConfig.VERSION_NAME.split('.')) && viewModel.settings.value?.first()?.forceUpdate!!) {
                 showUpdateBottomSheet()
                 return
             }
             if (Gson().toJson(viewModel.settings.value?.first()?.androidVersion?.split('.'))!! > Gson().toJson(BuildConfig.VERSION_NAME.split('.'))  && viewModel.settings.value?.first()?.optionalUpdate!!) {
                 showOptionUpdateBottomSheet()
                 return
             }
         }*/

    }

    private fun popupSnackbarForCompleteUpdate() {
        val (appUpdateManager, appUpdateInfoTask) = pair
        Snackbar.make(
            findViewById(android.R.id.content),
            getString(R.string.update_downloaded),
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction(getString(R.string.restart)) { appUpdateManager.completeUpdate() }
            setActionTextColor(ContextCompat.getColor(this@MainActivity, R.color.colorWhite))
            show()
        }
    }

    private fun getUpdateType(setting: Setting): Int {
        var updateType = -1
        if (setting.forceUpdate!!)
            updateType = 1
        else if (setting.optionalUpdate!!) updateType = 0
        return updateType
    }

    /* override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         super.onActivityResult(requestCode, resultCode, data)
         if (requestCode == UPDATE_REQUEST_CODE) {
             if (resultCode != RESULT_OK) {
                 Log.e(TAG, "onActivityResult: ")
                 // If the update is cancelled or fails,
                 // you can request to start the update again.
             }
         }

     }*/

    private fun showUpdateBottomSheet() {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialog)
        val binding: UpdateForceBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this),
            R.layout.update_force_bottom_sheet,
            null,
            false
        )

        dialog.setContentView(binding.root)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        dialog.setCancelable(false)
        binding.okBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data =
                Uri.parse("https://play.google.com/store/apps/details?id=com.kuwait.showroomz&hl=en")
            startActivity(intent)
        }
    }

    private fun showOptionUpdateBottomSheet() {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialog)
        val binding: UpdateOptionalBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this),
            R.layout.update_optional_bottom_sheet,
            null,
            false
        )

        dialog.setContentView(binding.root)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        binding.exitBtn.setOnClickListener {
            dialog.dismiss()

        }
        binding.okBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data =
                Uri.parse("https://play.google.com/store/apps/details?id=com.kuwait.showroomz&hl=en")
            startActivity(intent)
        }
    }

    private fun initBannerAds(ads: Advertisement) {
        banner_layout.visibility = View.VISIBLE
        imageSliderAdapter.refreshData(ads.imageGallery)
        ads_pager.apply {
            adapter = imageSliderAdapter
        }
        click_container.setOnClickListener {
            navigateFromAds(ads)
        }

        ads_tab_layout.setupWithViewPager(ads_pager)
        ads_tab_layout.isVisible = ads.imageGallery.size > 1
        circle_progress.isEnabled = ads.allowSkip == true
        val countDownTimer = object : CountDownTimer((ads.duration?.toLong()!! * 1000), 10) {
            override fun onFinish() {
                circle_progress.progress = 1f
                count_txt.text = "X"
                circle_progress.isEnabled = true
            }

            override fun onTick(millisUntilFinished: Long) {
                val prog = ((ads.duration?.times(1000)
                    ?: 0) - millisUntilFinished).toFloat() / ads.duration?.times(1000)!!
                circle_progress.progress = prog
                count_txt.text = ((millisUntilFinished.toFloat() / 1000) + 1).toInt().toString()

            }

        }
        countDownTimer.start()
        circle_progress.setOnClickListener {
            banner_layout.visibility = View.GONE
        }
    }

    fun hideBannerLayout() {
        banner_layout.visibility = View.GONE
    }

    private fun initFullScreenDialog(ads: Advertisement) {

        val metrics = this.resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val programsActionAdapter = ProgramActionsAdapter(actionList)
        val density: Float = metrics.density
        fullScreenDialog = Dialog(this, R.style.AppTheme)
        fullScreenDialog.setContentView(R.layout.ads_fullscreen_dialog)
        imageSliderAdapter.refreshData(ads.imageGallery)
        val w = 337.5 * density //(width * 0.95f)
        val h = 600 * density //(height * 0.95f)
        var param = fullScreenDialog.container.layoutParams
        param.width = w.toInt()
        param.height = h.toInt()
        fullScreenDialog.container.layoutParams = param
        //fullScreenDialog.window?.setLayout(w.toInt(), h.toInt())
        fullScreenDialog.setCancelable(false)
        fullScreenDialog.findViewById<ViewPager>(R.id.intro_pager).apply {
            adapter = imageSliderAdapter
        }
        imageSliderAdapter.setOnItemCLickListener(object :
            AdsImageSliderAdapter.OnItemClickListener {
            override fun onItemClick() {
                navigateFromAds(ads)
            }
        })
        val tab = fullScreenDialog.findViewById<TabLayout>(R.id.into_tab_layout)
        tab.isVisible = ads.imageGallery.size > 1
        tab.setupWithViewPager(fullScreenDialog.findViewById<ViewPager>(R.id.intro_pager), true)
        fullScreenDialog.findViewById<CircleProgressBar>(R.id.circle_progress).isEnabled =
            ads.allowSkip == true

        val countDownTimer = object : CountDownTimer(((ads.duration?.toLong()!! * 1000)), 10) {
            override fun onFinish() {
                fullScreenDialog.findViewById<CircleProgressBar>(R.id.circle_progress).progress = 1f
                fullScreenDialog.findViewById<TextView>(R.id.count_txt).text = "X"
                fullScreenDialog.findViewById<CircleProgressBar>(R.id.circle_progress).isEnabled =
                    true
            }

            override fun onTick(millisUntilFinished: Long) {
                var prog = ((ads.duration?.times(1000)
                    ?: 0) - millisUntilFinished).toFloat() / ads.duration?.times(1000)!!
                fullScreenDialog.findViewById<CircleProgressBar>(R.id.circle_progress).progress =
                    prog
                fullScreenDialog.findViewById<TextView>(R.id.count_txt).text =
                    ((millisUntilFinished.toFloat() / 1000) + 1).toInt().toString()

            }
        }
        countDownTimer.start()
        fullScreenDialog.program_actions.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = programsActionAdapter
        }
        val simplifier = AdsSimplifier(ads)
        fullScreenDialog.program_actions.isVisible = simplifier.actions.isNotEmpty()
        manipulateAction(AdsSimplifier(ads), programsActionAdapter)
        fullScreenDialog.findViewById<CircleProgressBar>(R.id.circle_progress)
            .setOnClickListener { fullScreenDialog.dismiss() }
        fullScreenDialog.show()
        if (simplifier.actions.isEmpty()) {

        }
        fullScreenDialog.window?.setBackgroundDrawableResource(R.color.transparent)



        val windowManager = window.attributes
        windowManager.dimAmount = 0.2f
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

    fun showTermsPopUps(identifier: String, action: (pos: Int) -> Unit) {
        var bottomReached = false
        val metrics = this.resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        val dialog = Dialog(this, R.style.AppTheme)
        dialog.setContentView(R.layout.terms_and_services_dialog)
        val url =  BuildConfig.BASE_URL + (if (isEnglish) "en/" else "ar/") +  TERM_CONDITION_URL//}lang=${if (isEnglish) "en" else "ar"}"
        dialog.webView.loadUrl(url)
        dialog.exit_btn.setOnClickListener {
            action.invoke(0)
            dialog.dismiss()
        }

        dialog.next_btn.setOnClickListener {
            if (bottomReached)
                dialog.dismiss()
            else
                dialog.webView.scrollBy(0, dialog.webView.measuredHeight)
        }

        dialog.webView.setOnScrollChangeListener { p0, newLeft, newTop, oldLeft, oldTop ->
            val readerViewHeight: Int = dialog.webView.measuredHeight

            val contentHeight: Int =
                floor((dialog.webView.contentHeight * resources.displayMetrics.density).toDouble()).toInt()//dialog.webView.contentHeight
            when {

                newTop + readerViewHeight >= contentHeight - 10 -> {
                    dialog.next_btn.text = resources.getString(R.string.accept)
                    bottomReached = true;

                }
                else -> {
                    dialog.next_btn.text = resources.getString(R.string.next)
                    bottomReached = false
                }
            }


        }

        val w = (width * 0.95f)
        val h = (height * 0.95f)
        dialog.window?.setLayout(w.toInt(), h.toInt())
        dialog.setCancelable(false)
        dialog.show()
        val windowManager = window.attributes
        windowManager.dimAmount = 0.2f
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

    fun navigateFromAds(ads: Advertisement) {
        val simp = AdsSimplifier(ads)
        val modetogo = simp.fetchModel()
        val brandTogo = simp.fetchBrand()
        val catdisplay = simp.fetchCategoryDisplay()
        val catTogo = simp.fetchCategory()
        viewModel.incrementNbClick(ads)
        if (simp.navigateToLink()){
            simp.link?.let {
                var url = it
                if (!url.startsWith("http://") && !url.startsWith("https://"))
                    url = "http://$url"
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(browserIntent)
            }
            return
        }
        if (ads.banks.isNotEmpty()) {
            val bundle = Bundle()
            if (modetogo != null) {
                bundle.putParcelable("model", modetogo)
                bundle.putParcelable("bank", ads.banks.first())
                bundle.putParcelable("trim", null)
                Navigation.findNavController(this@MainActivity, R.id.fragment)
                    .navigate(R.id.financeFragment, bundle)
            } else {
                bundle.putParcelable("bank", ads.banks.first())
                Navigation.findNavController(this@MainActivity, R.id.fragment)
                    .navigate(R.id.request_amountFragment, bundle)
            }
            hideAds()
            return
        }
        if (modetogo != null && ads.banks.isNotEmpty()) {
            val bundle = Bundle()
            bundle.putParcelable("model", modetogo)
            bundle.putParcelable("bank", ads.banks.first())
            bundle.putParcelable("trim", null)
            Navigation.findNavController(this@MainActivity, R.id.fragment)
                .navigate(R.id.financeFragment, bundle)
            hideAds()
            return
        }
        if (modetogo != null) {
            val bundle = Bundle()
            bundle.putParcelable("model", modetogo)
            bundle.putString("id", null)
            bundle.putString("slug", null)
            Navigation.findNavController(this@MainActivity, R.id.fragment)
                .navigate(R.id.modelDetailsFragment, bundle)
            hideAds()
            return
        }
        if (brandTogo != null) {
            val bundle = Bundle()
            bundle.putParcelable("brand", brandTogo)
            bundle.putString("slug", null)
            Navigation.findNavController(this@MainActivity, R.id.fragment)
                .navigate(R.id.modelFragment, bundle)
            hideAds()
            return
        }
        if (catdisplay != null && catTogo != null) {
            val navController = findNavController(this, R.id.fragment)
            val simplifier = AdsSimplifier(ads)

            if (navController.currentDestination?.id == R.id.brandsFragment2 && simplifier.categorVm?.parent?.id == simplifier.categorVmdisplay?.parent?.id) {
                val navHostFragment: NavHostFragment =
                    supportFragmentManager.findFragmentById(R.id.fragment) as NavHostFragment
                val fragment: BrandsFragment =
                    navHostFragment.childFragmentManager.fragments[0] as BrandsFragment
                Shared.oldIndex = 0
                Shared.selectedIndex =
                    fragment.childes.indexOfFirst { s -> s.id == simplifier.categorVm?.id }
                simplifier.fetchCategory()?.let { fragment.updateSelection(category = it) }
            } else {
                Shared.selectedIndex = 0
                Shared.oldIndex = 0
                val bundle = Bundle()
                bundle.putParcelable("cat", simplifier.fetchCategory())
                bundle.putParcelable("catChild", catTogo)
                bundle.putString("slug", null)
                Navigation.findNavController(this@MainActivity, R.id.fragment)
                    .navigate(R.id.brandsFragment2, bundle)

            }
            hideAds()
            return
        }


    }

    private fun hideAds() {
        fullScreenDialog.dismiss()
        fullScreenVideoDialog.dismiss()
        adsDialog.dismiss()
        banner_layout.visibility = View.GONE
    }

    fun setLocale(lang: String?) {
//        LanguageManager.instance.setLocale(this@MainActivity, lang)
        lang?.let { Lingver.getInstance().setLocale(this, it) }
        if (lang == "en") {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(BuildConfig.ar_topic)
            FirebaseMessaging.getInstance().subscribeToTopic(BuildConfig.en_topic)

        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(BuildConfig.en_topic)
            FirebaseMessaging.getInstance().subscribeToTopic(BuildConfig.ar_topic)
        }

    }

    fun restartActivity() {
        val i = Intent(this, MainActivity::class.java)
        startActivity(i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
    }


    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        currentFocus?.hideKeyboard()
        hideAds()
        var ads = viewModel.ads.value ?: ArrayList()
        var filteredAds: ArrayList<Advertisement> = arrayListOf()

        /*var historyAds: List<DisplayedAdvertisement> = arrayListOf()
        if (shared.existKey(DISPLAYED_ADS)) {
            historyAds = shared.getList<DisplayedAdvertisement>(DISPLAYED_ADS)
        }
        if (historyAds.isNotEmpty() && ads.size == historyAds.size) {
            shared.removeKey(DISPLAYED_ADS)
        }*/



        when (destination.label.toString()) {

            "DashboardFragment" -> {
                showAds(null, 60)
/*                if (viewModel.localSettings.value?.isNotEmpty()!!) {
//                    checkForUpdate()
                }*/
                /*if (shared.existKey("show_ads")) {
                    if (!shared.bool("show_ads")) {
                        return
                    }
                }
                ads.forEach {
                    if ((it.locationInApp == 64 && it.isEnabled == true)
                    /*&& !exist(
                        it,
                        historyAds
                    )*/
                    ) {
                        filteredAds.add(it)
                    }
                }

                if (filteredAds.size > 0) {
                    advertisement = Utils.instance.getRandomAds(filteredAds)
                    showAds(advertisement!!)
                }*/

            }
            /* "BrandsFragment" -> {


                 ads.forEach {
                     if ((it.locationInApp == 2 || it.isRandom!!) && !exist(
                             it,
                             historyAds
                         ) && (selectedCategory?.id == it.categoryToDisplay?.parent?.id)
                     ) {


                         filteredAds.add(it)
                     }
                 }
                 if (filteredAds?.size!! > 0) {
                     advertisement = Utils.instance.getRandomAds(filteredAds)

                     showAds(advertisement!!)
                 }

             }
             "ModelFragment" -> {
                 ads.forEach {
                     if ((it.locationInApp == 4 ) && !exist(
                             it,
                             historyAds
                         )
                     ) {
                         filteredAds.add(it)
                     }
                 }
                 if (filteredAds?.size!! > 0) {
                     advertisement = Utils.instance.getRandomAds(filteredAds)
                     showAds(advertisement!!)
                 }


             }
             "fragment_model_details" -> {
                 ads.forEach {
                     if ((it.locationInApp == 8) && !exist(
                             it,
                             historyAds
                         )
                     ) {

                         filteredAds.add(it)
                     }
                 }

                 if (filteredAds?.size!! > 0) {
                     advertisement = Utils.instance.getRandomAds(filteredAds)
                     showAds(advertisement!!)
                 }


             }*/
            "financeFragment" -> {
                showAds(null, 50)
                /*if (shared.existKey("show_ads")) {
                    if (!shared.bool("show_ads")) {
                        return
                    }
                }
                ads.forEach {
                    if ((it.locationInApp == 32 && it.isEnabled == true) /*&& !exist(
                            it,
                            historyAds
                        )*/
                    ) {
                        filteredAds.add(it)
                    }
                }

                if (filteredAds.size > 0) {
                    advertisement = Utils.instance.getRandomAds(filteredAds)
                    showAds(advertisement!!)
                }*/

            }
        }
        if (selectedCategory != null)
            Log.e(
                "BrandsFragmentToDisplay",
                "selectedCategory : " + selectedCategory?.translations?.en?.name
            )

    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    fun checkForCategoryAds(selectedCategory: Category) {
        showAds(selectedCategory.id, 10)

        /* if (shared.existKey("show_ads")) {
             if (!shared.bool("show_ads")) {
                 return
             }
         }
         // Log.e(viewModel.TAG, "checkForCategoryAds: ${selectedCategory.translations?.en?.name}")
         // var historyAds: List<DisplayedAdvertisement> = arrayListOf()
         var filteredAds: ArrayList<Advertisement> = arrayListOf()
         var ads = viewModel.ads.value
         /* if (shared.existKey(DISPLAYED_ADS)) {
              historyAds = shared.getList<DisplayedAdvertisement>(DISPLAYED_ADS)
          }
          if (historyAds.isNotEmpty() && ads?.size == historyAds.size) {
              shared.removeKey(DISPLAYED_ADS)
          }*/
         ads?.forEach { advertisement ->

             if (advertisement.locationInApp == 2 && advertisement.categoryToDisplay?.id == selectedCategory.id
                 && advertisement.isEnabled == true)
                 filteredAds.add(advertisement)
         }

         if (filteredAds.size > 0) {
             advertisement = Utils.instance.getRandomAds(filteredAds)
             showAds(advertisement!!)
         }*/
    }

    fun checkForBrandAds(selectedCategory: Category) {
        showAds(selectedCategory.id, 20)
        /*if (shared.existKey("show_ads")) {
            if (!shared.bool("show_ads")) {
                return
            }
        }
        Log.e(viewModel.TAG, "checkForBrandAds: ${selectedCategory.translations?.en?.name}")
        var historyAds: List<DisplayedAdvertisement> = arrayListOf()
        val filteredAds: ArrayList<Advertisement> = arrayListOf()
        val ads = viewModel.ads.value
        /*if (shared.existKey(DISPLAYED_ADS)) {
            historyAds = shared.getList<DisplayedAdvertisement>(DISPLAYED_ADS)
        }
        if (historyAds.isNotEmpty() && ads?.size == historyAds.size) {
            shared.removeKey(DISPLAYED_ADS)
        }*/
        ads?.forEach { advertisement ->
            //val simp = AdsSimplifier(advertisement)
            if (advertisement.locationInApp == 4 && advertisement.categoryToDisplay?.id == selectedCategory.id
                && advertisement.isEnabled == true)
                filteredAds.add(advertisement)
        }

        if (filteredAds.size > 0) {
            advertisement = Utils.instance.getRandomAds(filteredAds)
            showAds(advertisement!!)
        }*/
    }

    fun checkForModelAds(selectedCategory: String) {
        showAds(selectedCategory, 30)
        /*if (shared.existKey("show_ads")) {
            if (!shared.bool("show_ads")) {
                return
            }
        }
        //Log.e(viewModel.TAG, "checkForModelAds: ${selectedCategory.translations?.en?.name}")
        // var historyAds: List<DisplayedAdvertisement> = arrayListOf()
        val filteredAds: ArrayList<Advertisement> = arrayListOf()
        val ads = viewModel.ads.value
        /*if (shared.existKey(DISPLAYED_ADS)) {
            historyAds = shared.getList<DisplayedAdvertisement>(DISPLAYED_ADS)
        }
        if (historyAds.isNotEmpty() && ads?.size == historyAds.size) {
            shared.removeKey(DISPLAYED_ADS)
        }*/
        ads?.forEach { advertisement ->
            // val simp = AdsSimplifier(advertisement)
            if (advertisement.locationInApp == 8 && advertisement.categoryToDisplay?.id == selectedCategory
                && advertisement.isEnabled == true
            /*&& !exist(
                advertisement,
                historyAds
            )*/
            )
                filteredAds.add(advertisement)
        }

        if (filteredAds.size > 0) {
            advertisement = Utils.instance.getRandomAds(filteredAds)
            advertisement?.let {
                showAds(it)
            }

        }*/
    }

    fun getTopAds(
        selectedCategory: Category?,
        locInApp: Int,
        mediaSize: Int
    ): ArrayList<Advertisement> {
        if (shared.existKey("show_ads")) {
            if (!shared.bool("show_ads")) {
                return arrayListOf()
            }
        }
        val filteredAds: ArrayList<Advertisement> = arrayListOf()
        val ads = viewModel.ads.value
        ads?.forEach { advertisement ->
            if (advertisement.isEnabled == true) {
                selectedCategory?.let {
                    if (advertisement.locationInApp == locInApp && advertisement.mediaSize == mediaSize && advertisement.categoryToDisplay?.id == selectedCategory.id)
                        filteredAds.add(advertisement)
                } ?: run {
                    if (advertisement.locationInApp == locInApp && advertisement.mediaSize == mediaSize)
                        filteredAds.add(advertisement)
                }
            }
        }
       filteredAds.sortByDescending { s -> s.position }
        return  filteredAds
    }

    override fun onSaveInstanceState(oldInstanceState: Bundle) {
        super.onSaveInstanceState(oldInstanceState)
        oldInstanceState.clear()
    }

}
