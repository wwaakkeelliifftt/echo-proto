package com.example.echo_proto.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.echo_proto.databinding.ViewpagerFeedHostBinding
import com.example.echo_proto.ui.adapters.ViewPagerFeedAdapter
import com.example.echo_proto.ui.adapters.ZoomOutPageTransformer
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HostFeedPager: Fragment() {

    private var _binding: ViewpagerFeedHostBinding? = null
    private val binding get() = _binding!!

    private lateinit var feedHostPagerAdapter: ViewPagerFeedAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = ViewpagerFeedHostBinding.inflate(layoutInflater)
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
        updateMenuForCurrentPage(binding.viewPagerFeedHost.currentItem)
    }

    private fun setupViewPager() {
        val fragments = listOf(
            FeedFragment(),
            FeedPersonalFragment()
        )
        feedHostPagerAdapter = ViewPagerFeedAdapter(fragments, requireActivity())
        binding.viewPagerFeedHost.apply {
            adapter = feedHostPagerAdapter
            setPageTransformer(ZoomOutPageTransformer())
            offscreenPageLimit = fragments.size
        }
        TabLayoutMediator(binding.tabLayoutFeedHost, binding.viewPagerFeedHost) { tab, position ->
            tab.text = feedHostPagerAdapter.fragments[position]::class.java.simpleName
            tab.badge // todo: new episode counter??
        }.attach()
    }

    private fun setupMenuUpdateListener() {
        binding.viewPagerFeedHost.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateMenuForCurrentPage(position)
            }
        })
    }

    private fun updateMenuForCurrentPage(position: Int) {
        // Убираем setHasOptionsMenu для всех фрагментов сначала
        feedHostPagerAdapter.fragments.forEachIndexed { index, fragment ->
            if (index != position) {
                fragment.setHasOptionsMenu(false)
            }
        }
        
        // Устанавливаем setHasOptionsMenu для текущего фрагмента
        val currentFragment = feedHostPagerAdapter.fragments.getOrNull(position)
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