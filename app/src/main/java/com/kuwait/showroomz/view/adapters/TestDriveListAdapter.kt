package com.kuwait.showroomz.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.R
import com.kuwait.showroomz.model.data.TestDrive
import com.kuwait.showroomz.model.simplifier.TestDriveSimplifier
import com.kuwait.showroomz.view.fragment.TestDriveListFragmentDirections

class TestDriveListAdapter(private var list: List<TestDrive>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class TestDriveViewHolder(var view: com.kuwait.showroomz.databinding.TestDriveListItemBinding) :
        RecyclerView.ViewHolder(view.root)


    fun refresh(testDrive: List<TestDrive>) {
        this.list = testDrive
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val view =
            DataBindingUtil.inflate<com.kuwait.showroomz.databinding.TestDriveListItemBinding>(
                inflater,
                R.layout.test_drive_list_item,
                parent,
                false
            )
        return TestDriveViewHolder(view)


    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val testDrive = list[position]
        if (holder is TestDriveViewHolder)
            holder.view.testDrive = TestDriveSimplifier(testDrive)


        holder.itemView.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(TestDriveListFragmentDirections.showTestDriveDetails(testDrive))
        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position].modelData?.id != null) 0 else 1
    }
}