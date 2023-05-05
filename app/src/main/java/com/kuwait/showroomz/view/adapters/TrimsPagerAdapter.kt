package com.kuwait.showroomz.view.adapters

import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.kuwait.showroomz.extras.customViews.WrapContentHeightViewPager
import com.kuwait.showroomz.model.data.Trim
import com.kuwait.showroomz.model.simplifier.ModelSimplifier
import com.kuwait.showroomz.model.simplifier.TrimSimplifier
import com.kuwait.showroomz.view.fragment.TrimItemFragment


class TrimsPagerAdapter(
    var trims: List<Trim>,
    var fragmentManager: FragmentManager,
    var simplifier: ModelSimplifier
) :
    FragmentPagerAdapter(fragmentManager, trims.size) {
    private var mCurrentPosition = -1
    override fun getItem(position: Int): Fragment {
        return TrimItemFragment.newInstance(trims[position],simplifier)
    }

    override fun getCount(): Int {
        return trims.size
    }
    override fun getPageTitle(position: Int): CharSequence? {
        return TrimSimplifier(trims[position]).name
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        super.setPrimaryItem(container, position, `object`)
        if (position !== mCurrentPosition && container is WrapContentHeightViewPager) {
            val fragment = `object` as Fragment
            val pager: WrapContentHeightViewPager = container
            if (fragment.view != null) {
                mCurrentPosition = position
                pager.measureCurrentView(fragment.requireView())
            }
        }
    }

}

 class ViewStateAdapter(
    var trims: List<Trim>,
    var fragmentManager: FragmentManager,
    var simplifier: ModelSimplifier,
    @NonNull lifecycle: Lifecycle
) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    @NonNull
    override fun createFragment(position: Int): Fragment {
         return TrimItemFragment.newInstance(trims[position], simplifier)
    }

    override fun getItemCount(): Int {
        // Hardcoded, use lists
        return trims.size
    }
}