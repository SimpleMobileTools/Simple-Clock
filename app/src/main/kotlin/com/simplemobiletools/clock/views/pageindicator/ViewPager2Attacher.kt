package com.simplemobiletools.clock.views.pageindicator

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback

/**
 * ViewPager2 Attacher for [PagerIndicator]
) */
class ViewPager2Attacher : PagerIndicator.PagerAttacher<ViewPager2> {
    private var dataSetObserver: AdapterDataObserver? = null
    private var attachedAdapter: RecyclerView.Adapter<*>? = null
    private var onPageChangeListener: OnPageChangeCallback? = null
    private var pager: ViewPager2? = null

    override fun attachToPager(indicator: PagerIndicator, pager: ViewPager2) {
        attachedAdapter = pager.adapter
        checkNotNull(attachedAdapter) { "Set adapter before call attachToPager() method" }
        this.pager = pager
        updateIndicatorDotsAndPosition(indicator)
        dataSetObserver = object : AdapterDataObserver() {
            override fun onChanged() {
                indicator.reattach()
            }
        }
        attachedAdapter!!.registerAdapterDataObserver(dataSetObserver!!)
        onPageChangeListener = object : OnPageChangeCallback() {
            var idleState = true
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixel: Int
            ) {
                updateIndicatorOnPagerScrolled(indicator, position, positionOffset)
            }

            override fun onPageSelected(position: Int) {
                if (idleState) {
                    updateIndicatorDotsAndPosition(indicator)
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                idleState = state == ViewPager2.SCROLL_STATE_IDLE
            }
        }
        pager.registerOnPageChangeCallback(onPageChangeListener!!)
    }

    override fun detachFromPager() {
        attachedAdapter!!.unregisterAdapterDataObserver(dataSetObserver!!)
        pager!!.unregisterOnPageChangeCallback(onPageChangeListener!!)
    }

    private fun updateIndicatorDotsAndPosition(indicator: PagerIndicator) {
        indicator.dotCount = attachedAdapter!!.itemCount
        indicator.setCurrentPosition(pager!!.currentItem)
    }

    private fun updateIndicatorOnPagerScrolled(
        indicator: PagerIndicator,
        position: Int,
        positionOffset: Float
    ) {
        // ViewPager may emit negative positionOffset for very fast scrolling
        val offset: Float = when {
            positionOffset < 0 -> {
                0f
            }
            positionOffset > 1 -> {
                1f
            }
            else -> {
                positionOffset
            }
        }
        indicator.onPageScrolled(position, offset)
    }
}
