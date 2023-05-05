package com.kuwait.showroomz.view.fragment

import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.FragmentSearchToCampareBinding
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.view.MainActivity
import com.kuwait.showroomz.view.adapters.SearchToCompareResultAdapter
import com.kuwait.showroomz.viewModel.SearchModelToCompareVM

class SearchToCompareFragment : Fragment() {
    lateinit var binding: FragmentSearchToCampareBinding
    private lateinit var viewModel: SearchModelToCompareVM
    private val searchResultAdapter = SearchToCompareResultAdapter(listOf())
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_search_to_campare, container, false)
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root


    }
    var delay: Long = 500 // 1 seconds after user stops typing
    var searchTxt = ""
    var last_text_edit: Long = 0
    var handler: Handler = Handler()

    private val input_finish_checker = Runnable {
        if (System.currentTimeMillis() > last_text_edit + delay - 500) {
            viewModel.searchByName(searchTxt)
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SearchModelToCompareVM::class.java)
        arguments.let {
            viewModel.category = it?.let { it1 -> SearchFragmentArgs.fromBundle(it1).cat }
            viewModel.filterInit()
            binding.viemModel = viewModel
        }
        binding.resultRecycler.apply {
            adapter = searchResultAdapter
            layoutManager = LinearLayoutManager(context)
        }
        DesignUtils.instance.setStatusBar(
            requireActivity() as MainActivity, true,
            ContextCompat.getColor(requireContext(), R.color.off_white)
        )
        observeData()
        onclickListener()
    }

    private fun onclickListener() {
        binding.closeButton.setOnClickListener {
            Navigation.findNavController(it).popBackStack()
        }
        binding.filterEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                handler.removeCallbacks(input_finish_checker)
            }

            override fun afterTextChanged(s: Editable?) {
                viewModel.searchByName(s.toString())
               /*/ searchTxt = s.toString()
                last_text_edit = System.currentTimeMillis();
                handler.postDelayed(input_finish_checker, delay)*/
            }

        })

    }

    private fun observeData() {

        viewModel.list.observe(viewLifecycleOwner, Observer {
            searchResultAdapter.refresh(it)
        })
    }


}