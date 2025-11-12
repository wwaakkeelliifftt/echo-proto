package com.example.echo_proto.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.echo_proto.data.remote.FeedChannel
import com.example.echo_proto.databinding.ViewpagerChannelsHostBinding
import com.example.echo_proto.ui.adapters.ViewPagerFeedAdapter
import com.example.echo_proto.ui.adapters.ZoomOutPageTransformer
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class HostChannelsPager : Fragment() {

    private var _binding: ViewpagerChannelsHostBinding? = null
    private val binding get() = _binding!!

    private lateinit var channelsHostPagerAdapter: ViewPagerFeedAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = ViewpagerChannelsHostBinding.inflate(layoutInflater)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViewPager()
        setupMenuUpdateListener()
        
        // Настраиваем toolbar после создания ViewPager
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        
        // Устанавливаем меню для первой страницы
        updateMenuForCurrentPage(0)
    }
    
    override fun onResume() {
        super.onResume()
        // Убеждаемся, что toolbar установлен при возврате на фрагмент
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        // Обновляем меню для текущей страницы
        updateMenuForCurrentPage(binding.viewPagerChannelsHost.currentItem)
    }

    private fun setupViewPager() {
        val fragments = mutableListOf<Fragment>()
        FeedChannel.listOfChannels.forEach { feedChannel ->
            val fragment = ChannelFragment.newInstance(feedChannel.id)
            fragments.add(fragment)
        }
        channelsHostPagerAdapter = ViewPagerFeedAdapter(fragments = fragments, requireActivity())
        binding.viewPagerChannelsHost.apply {
            adapter = channelsHostPagerAdapter
            setPageTransformer(ZoomOutPageTransformer())
            offscreenPageLimit = fragments.size
        }
        TabLayoutMediator(binding.tabLayoutChannelsHost, binding.viewPagerChannelsHost) { tab, position ->
            tab.text = "${position + 1}.${FeedChannel.listOfChannels[position].tabBadgeName}"
            tab.badge // todo: new episode counter??
        }.attach()
    }

    private fun setupMenuUpdateListener() {
        binding.viewPagerChannelsHost.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateMenuForCurrentPage(position)
            }
        })
    }

    private fun updateMenuForCurrentPage(position: Int) {
        // Убираем setHasOptionsMenu для всех фрагментов сначала
        channelsHostPagerAdapter.fragments.forEachIndexed { index, fragment ->
            if (index != position) {
                fragment.setHasOptionsMenu(false)
            }
        }
        
        // Устанавливаем setHasOptionsMenu для текущего фрагмента
        val currentFragment = channelsHostPagerAdapter.fragments.getOrNull(position)
        currentFragment?.setHasOptionsMenu(true)
        
        // Инвалидируем меню, чтобы текущий фрагмент мог обновить его
        // Используем post для гарантии, что setHasOptionsMenu уже применен
        view?.post {
            activity?.invalidateOptionsMenu()
        }
    }

    // Не переопределяем onPrepareOptionsMenu - дочерние фрагменты полностью управляют меню
    // Дочерние фрагменты должны вызывать menu.clear() в начале своего onPrepareOptionsMenu

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}