package com.kuwait.showroomz.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.kuwait.showroomz.R
import com.kuwait.showroomz.model.data.TestDrive
import com.kuwait.showroomz.model.simplifier.TestDriveSimplifier
import com.kuwait.showroomz.view.fragment.DashboardFragmentDirections

class DashboardTestDriveAdapter(private var list: List<TestDrive>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class TestDriveViewHolder(var view: com.kuwait.showroomz.databinding.DashboardTestDriveItemBinding) :
        RecyclerView.ViewHolder(view.root)


    fun refresh(testDrives: List<TestDrive>) {
        this.list = testDrives
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

            val view =
                DataBindingUtil.inflate<com.kuwait.showroomz.databinding.DashboardTestDriveItemBinding>(
                    inflater,
                    R.layout.dashboard_test_drive_item,
                    parent,
                    false
                )
            return TestDriveViewHolder(view)



    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val testDrive = list[position]
        if (holder is TestDriveViewHolder){
            holder.view.testDrive = TestDriveSimplifier(testDrive)
            holder.itemView
                .setOnClickListener {
                    testDrive.let { it1 ->
                        DashboardFragmentDirections.showTestDriveDetails(
                            it1
                        )
                    }.let { it2 -> Navigation.findNavController(it).navigate(it2) }
                }
        }



    }


}