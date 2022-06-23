package com.example.echo_proto.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerFeedAdapter(
    val fragments: List<Fragment>,
    context: FragmentActivity
) : FragmentStateAdapter(context) {

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}