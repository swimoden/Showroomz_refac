package com.kuwait.showroomz.view.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.FragmentModelBinding
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.extras.Utils
import com.kuwait.showroomz.extras.WebViewLocaleHelper
import com.kuwait.showroomz.model.data.*
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.model.simplifier.BrandSimplifier
import com.kuwait.showroomz.model.simplifier.ModelSimplifier
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.view.adapters.ModelAdapter
import com.kuwait.showroomz.viewModel.ModelVM
import com.kuwait.showroomz.viewModel.ProfileVM
import io.realm.RealmObject
import kotlinx.android.synthetic.main.fragment_model.*


class ModelFragment : Fragment() {

    private var models: List<Model> = arrayListOf()
    private val viewModel by viewModels<ModelVM>()
    private lateinit var binding: FragmentModelBinding
    private lateinit var simplifier: BrandSimplifier
    //private lateinit var brand: Brand
    var list: List<RealmObject>? = listOf()
    var item: ModelDashBoardItem? = null
    var size = 0
    var indexExc = 0
    var showEvery = 4

    private val modelAdapter: ModelAdapter by lazy {
        ModelAdapter(models, item, size, indexExc, {
            val action = ModelFragmentDirections.aactionShowModelDetail(it,
                null, null)
            val destination = findNavController().graph.findNode(R.id.modelFragment)
            if (destination?.getAction(R.id.aactionShowModelDetail) != null) {
                view?.let { it1 -> Navigation.findNavController(it1).navigate(action) }
            }

        }, {
            (activity as MainActivity).navigateFromAds(it)
        },{ ads, action ->
            (activity as MainActivity).callAction(ads,action)
        })
    }
    override fun onResume() {
        super.onResume()
        if (::simplifier.isInitialized)
        simplifier.cat?.let { LogProgressRepository.logProgress("Models_screen",category = it.id,dealerData = simplifier.id ) }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //viewModel = ViewModelProviders.of(this).get(ModelVM::class.java)
        val helper = context?.let { WebViewLocaleHelper(it) }
        helper?.implementWorkaround()
        container?.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_model, container, false)
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
     //  Handler(Looper.getMainLooper()).postDelayed ({
           arguments?.let {
               ModelFragmentArgs.fromBundle(it).slug?.let {
                   viewModel.getBrandBySlug(it) { brand ->
                       populate(brand)
                   }
               } ?: run {
                   val brand = ModelFragmentArgs.fromBundle(it).brand
                   brand?.let {
                       populate(brand)
                   }
               }
           }
      // }, 225)



    }
    fun populate(brand:Brand){
        simplifier = BrandSimplifier(brand)
        binding.brand = simplifier
        simplifier.cat?.let {
            activity?.let { act ->
                (act as MainActivity).checkForBrandAds(it)
            }
        }
        model_recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = modelAdapter
        }
        viewModel.getModels(simplifier.id)
       // simplifier.cat?.usedFor?.let { viewModel.getModels(it, simplifier.id) }
        DesignUtils.instance.setStatusBar(requireActivity() as MainActivity,true,ContextCompat.getColor(requireContext(),R.color.off_white))
        clickListener()
        observeModels()
    }

    private fun observeModels() {
        viewModel.empty.observe(viewLifecycleOwner, Observer {
            binding.emptyDataTxt.container.isVisible = it
            model_recycler.isVisible = !it
        })
        viewModel.models.observe(viewLifecycleOwner, Observer { models ->


            indexExc = 0
            item = null
            list = models
            size = list?.size ?: 0
            if (simplifier.showAdsOnTop == true){
                val ads = ( requireActivity() as MainActivity).getTopAds( simplifier.cat, 20, 40)
                if (ads.size > 0){
                    item = ModelDashBoardItem("", null, ads)
                }else{
                    filterModels()
                }
            }else{
                filterModels()
            }
            if (simplifier.showAdsOnScroll == true){
                val ads = (requireActivity() as MainActivity).getTopAds(simplifier.cat, 20, 50)
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

            if (size > 0) {
                model_recycler.isVisible = true
                modelAdapter.refresh(item, list, size, indexExc)
            }else {
                model_recycler.isVisible = false
                binding.emptyDataTxt.container.isVisible = true
            }


        })
        viewModel.loading.observe(viewLifecycleOwner, Observer { isloading ->
            binding.progressCircular.isVisible = isloading
            if (isloading) {
                binding.emptyDataTxt.container.isVisible = false
                activity?.let { DesignUtils.instance.disableUserInteraction(it) }
            } else activity?.let { DesignUtils.instance.enableUserInteraction(it) }

        })

        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            print(error)
            binding.emptyDataTxt.container.isVisible = error.isNotEmpty()
        })
        viewModel.noConectionError.observe(viewLifecycleOwner,  androidx.lifecycle.Observer {
            if (it){
                DesignUtils.instance.showErrorDialog(requireContext(), null,null){

                }
            }
        })
    }
private fun filterModels() {
    if (simplifier.showOffers) {
        list?.let {
            size = 0
            val bex = it.filter {
                if (ModelSimplifier(it as Model).brand?.showExclusiveOnTop == true) ModelSimplifier(it).isexclisive else ModelSimplifier(
                    it
                ).isoffer
            }

            if (bex.size > 10) {
                size += it.size
                //list = models
                indexExc = bex.size
            } else {
                if (bex.size in 1..10 && bex.size < it.size) {
                    size += 1
                    val title = (ModelSimplifier(bex[0] as Model).brand?.headerName) //if(ModelSimplifier(bex.get(0)).brand?.showExclusiveOnTop == true) (context?.getString(R.string.exclusive_offers)) else (ModelSimplifier(bex.get(0)).brand?.headerName)
                    item = title?.let { title -> ModelDashBoardItem(title, bex as List<Model>, null) }
                    list = it.filter {
                        !if (ModelSimplifier(it as Model).brand?.showExclusiveOnTop == true) ModelSimplifier(
                            it
                        ).isexclisive else ModelSimplifier(it).isoffer
                    }
                } else if (bex.size == it.size) {
                    list = it
                } else {
                    list = it.filter {
                        !if (ModelSimplifier(it as Model).brand?.showExclusiveOnTop == true) ModelSimplifier(
                            it
                        ).isexclisive else ModelSimplifier(it).isoffer
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
            simplifier.cat?.let { it1 ->
                ModelFragmentDirections.actionNavigateToSearchFragment(
                    it1
                )
            }?.let { it2 -> Navigation.findNavController(it).navigate(it2) }
        }
        back.setOnClickListener {
            if (Navigation.findNavController(it).previousBackStackEntry?.destination?.id == R.id.splashFragment){
                val navHostFragment = activity?.supportFragmentManager?.findFragmentById(R.id.fragment) as NavHostFragment
                val navController = navHostFragment.navController
                navController.navigate(R.id.dashboardFragment)
            }else{
                Navigation.findNavController(it).navigateUp()
            }
        }
        show_locations.setOnClickListener {
            val action = ModelFragmentDirections.acttionShowLocationsByBrandId(simplifier.id, simplifier.brand, emptyArray())
            action.let { it1 -> Navigation.findNavController(it).navigate(it1) }

        }
        binding.filterBtn.setOnClickListener {
            val cat  = simplifier.cat
            cat?.let { it1 ->
                it1.parentObj()?.let { it2 ->
                    ModelFragmentDirections.actionNavigateToFilterFragment(
                        it1,
                        it2
                    )
                }
            }?.let { it2 ->
                Navigation.findNavController(it).navigate(
                    it2
                )
            }

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
    }

}