package com.kuwait.showroomz.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.kuwait.showroomz.R
import com.kuwait.showroomz.databinding.FragmentTrimItemBinding
import com.kuwait.showroomz.extras.DesignUtils
import com.kuwait.showroomz.model.data.Trim
import com.kuwait.showroomz.model.simplifier.ModelSimplifier
import com.kuwait.showroomz.model.simplifier.TrimSimplifier
import com.kuwait.showroomz.viewModel.ModelDetailVM


class TrimItemFragment : Fragment() {
    private lateinit var viewModel: ModelDetailVM
     lateinit var simplifier: ModelSimplifier
    private var trim: Trim? = null
    lateinit var binding: FragmentTrimItemBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*arguments?.let {
            trim = it.getSerializable(Companion.ARG_PARAM) as Trim?

        }*/
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_trim_item, container, false)
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ModelDetailVM::class.java)
        arguments?.let {
            trim = it.getSerializable(Companion.ARG_PARAM) as Trim?
             /*trim?.specs?.let {
                 trim?.specs = it.sortedBy { s -> s.position }
             }*/
        }
        binding.trim= trim?.let { TrimSimplifier(it) }
        binding.model=simplifier
        viewModel.noConectionError.observe(viewLifecycleOwner,  androidx.lifecycle.Observer {
            if (it){
                DesignUtils.instance.showErrorDialog(requireContext(), null,null){

                }
            }
        })
    }
    companion object {

        fun newInstance(trim: Trim, simplifier: ModelSimplifier) =
            TrimItemFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(Companion.ARG_PARAM, trim)

                }
                this.simplifier =simplifier
            }

        private const val ARG_PARAM = "TRIM"
    }
}