package com.example.myfblogin.component

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.*
import android.widget.*
import com.example.myfblogin.R
import kotlinx.android.synthetic.main.tab_title.view.*
import java.util.*

/*
 * Copyright (C) 2013 Andreas Stuetz <andreas.stuetz@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@SuppressLint("InflateParams")
class PagerSlidingTabStrip constructor(context: Context) : HorizontalScrollView(context) {
    private val allCaps: Boolean = false

    interface IconTabProvider {
        fun getPageIconDefResId(position: Int): Int

        fun getPageIconSelResId(position: Int): Int
    }

    interface TextTabProvider {
        fun getTabType(position: Int): Byte
    }
    // @formatter:on

    private val defaultTabLayoutParams: LinearLayout.LayoutParams
    private val expandedTabLayoutParams: LinearLayout.LayoutParams

    private val pageListener = PageListener()
    private var delegatePageListener: ViewPager.OnPageChangeListener? = null

    private val tabsContainer: LinearLayout = LinearLayout(context)
    private var pager: ViewPager? = null

    private val dm: DisplayMetrics = resources.displayMetrics

    private var tabCount: Int = 0

    private var currentPosition = 0
    private var currentPositionOffset = 0f

    private val rectPaint: Paint = Paint()
    private val dividerPaint: Paint = Paint()

    private var tabColor = ContextCompat.getColor(getContext(), R.color.colorPrimary)
    var indicatorColor = ContextCompat.getColor(getContext(), android.R.color.white)
    var indicatorType : Byte = INDICATOR_WRAP_CONTENT
    private var underlineColor = ContextCompat.getColor(getContext(), android.R.color.transparent)
    private var dividerColor = ContextCompat.getColor(getContext(), android.R.color.transparent)

    private var shouldExpand = false
    var isTextAllCaps = true
        private set

    private var scrollOffset = 52
    private var indicatorHeight = 4
    private var underlineHeight = 0
    private var dividerPadding = 10
    private var tabPaddingLeftRight = 5
    private var tabPaddingUpDown = 5
    private var dividerWidth = 10

    private var tabTextSize = 12
    private var tabTextColor = ContextCompat.getColor(getContext(), android.R.color.white)
    private var tabTextSelectedColor = ContextCompat.getColor(getContext(), android.R.color.white)
    private var tabTypeface: Typeface? = null
    private var tabTypefaceStyle = Typeface.BOLD

    private var lastScrollX = 0

    var tabBackground = R.drawable.background_tab
    private var shouldFit = false

    private var locale: Locale? = null

    override fun onDetachedFromWindow() {
        delegatePageListener = null
        super.onDetachedFromWindow()
    }

    constructor(context: Context, attrs: AttributeSet?):this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int = 0):this(context){
        setAllCaps(true)
        isFillViewport = true
        setWillNotDraw(false)

        tabsContainer.orientation = LinearLayout.HORIZONTAL
        tabsContainer.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        addView(tabsContainer)
        setBackgroundResource(android.R.color.transparent)
        tabsContainer.setBackgroundResource(android.R.color.transparent)

        scrollOffset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, scrollOffset.toFloat(), dm).toInt()
        indicatorHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, indicatorHeight.toFloat(), dm).toInt()
        underlineHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, underlineHeight.toFloat(), dm).toInt()
        dividerPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dividerPadding.toFloat(), dm).toInt()
        tabPaddingLeftRight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, tabPaddingLeftRight.toFloat(), dm).toInt()
        tabPaddingUpDown = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, tabPaddingUpDown.toFloat(), dm).toInt()
        dividerWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dividerWidth.toFloat(), dm).toInt()
        tabTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, tabTextSize.toFloat(), dm).toInt()

        // get system attrs (android:textSize and android:textColor)

        var a = context.obtainStyledAttributes(attrs, ATTRS)

        tabTextSize = a.getDimensionPixelSize(0, tabTextSize)
        tabTextColor = a.getColor(0, tabTextColor)

        a.recycle()

        // get custom attrs

        a = context.obtainStyledAttributes(attrs, R.styleable.PagerSlidingTabStrip)

        indicatorColor = a.getColor(R.styleable.PagerSlidingTabStrip_pstsIndicatorColor, indicatorColor)
        underlineColor = a.getColor(R.styleable.PagerSlidingTabStrip_pstsUnderlineColor, underlineColor)
        dividerColor = a.getColor(R.styleable.PagerSlidingTabStrip_pstsDividerColor, dividerColor)
        indicatorHeight = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsIndicatorHeight, indicatorHeight)
        underlineHeight = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsUnderlineHeight, underlineHeight)
        dividerPadding = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsDividerPadding, dividerPadding)
        tabPaddingLeftRight = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsTabPaddingLeftRight, tabPaddingLeftRight)
        tabPaddingUpDown = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsTabPaddingUpDown, tabPaddingUpDown)
        tabBackground = a.getResourceId(R.styleable.PagerSlidingTabStrip_pstsTabBackground, tabBackground)
        shouldExpand = a.getBoolean(R.styleable.PagerSlidingTabStrip_pstsShouldExpand, shouldExpand)
        scrollOffset = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsScrollOffset, scrollOffset)
        isTextAllCaps = a.getBoolean(R.styleable.PagerSlidingTabStrip_pstsTextAllCaps, isTextAllCaps)

        a.recycle()

        rectPaint.isAntiAlias = true
        rectPaint.style = Paint.Style.FILL

        dividerPaint.isAntiAlias = true
        dividerPaint.strokeWidth = dividerWidth.toFloat()

        if (locale == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locale = resources.configuration.locales.get(0)
            } else {
                locale = resources.configuration.locale
            }
        }
    }

    init {
        defaultTabLayoutParams = LinearLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT)
        expandedTabLayoutParams = LinearLayout.LayoutParams(0, FrameLayout.LayoutParams.MATCH_PARENT, 1.0f)
    }

    fun setViewPager(pager: ViewPager) {
        this.pager = pager

        if (pager.adapter == null) {
            throw IllegalStateException("ViewPager does not have adapter instance.")
        }

        pager.addOnPageChangeListener(pageListener)

        notifyDataSetChanged()
    }

    fun setOnPageChangeListener(listener: ViewPager.OnPageChangeListener) {
        this.delegatePageListener = listener
    }

    fun notifyDataSetChanged() {
        if (pager != null && pager!!.adapter != null) {
            tabsContainer.removeAllViews()

            tabCount = pager!!.adapter.count

            for (i in 0..tabCount - 1) {

                if (pager!!.adapter is IconTabProvider) {
                    if (pager!!.currentItem == i) {
                        addIconTab(i, (pager!!.adapter as IconTabProvider).getPageIconSelResId(i))
                    } else {
                        addIconTab(i, (pager!!.adapter as IconTabProvider).getPageIconDefResId(i))
                    }
                } else if (pager!!.adapter is TextTabProvider) {
                    addTextTab(i, pager!!.adapter.getPageTitle(i).toString(), (pager!!.adapter as TextTabProvider).getTabType(i))
                } else {
                    addTextTab(i, pager!!.adapter.getPageTitle(i).toString(), TEXT_DECORATION_NONE)
                }

            }

            updateTabStyles()

            if (tabsContainer.width > width) {
                val params = layoutParams as LinearLayout.LayoutParams
                params.width = FrameLayout.LayoutParams.WRAP_CONTENT
                layoutParams = params
            }

            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {

                @SuppressLint("NewApi")
                override fun onGlobalLayout() {

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        viewTreeObserver.removeGlobalOnLayoutListener(this)
                    } else {
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }

                    currentPosition = pager!!.currentItem
                    scrollToChild(currentPosition, 0)
                }
            })
        }
    }

    fun shouldFit(should: Boolean) {
        shouldFit = should
        val params = tabsContainer.layoutParams as FrameLayout.LayoutParams
        params.width = LinearLayout.LayoutParams.MATCH_PARENT
        tabsContainer.layoutParams = params
    }

    private fun addTextTab(position: Int, title: String, textDecoration: Byte) {
        val mInflater = LayoutInflater.from(context)
        val tab = mInflater.inflate(R.layout.tab_title, null) as FrameLayout
        if (shouldFit) {
            val tabParam = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            tabParam.width = 0
            tabParam.weight = 1f
            tabParam.gravity = Gravity.CENTER
            tab.layoutParams = tabParam
        }

        val text = tab.txtTitle
        val textParam = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        textParam.gravity = Gravity.CENTER
        text.layoutParams = textParam
        text.text = title
        text.gravity = Gravity.CENTER
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        text.setSingleLine()
        text.setTypeface(tabTypeface, Typeface.NORMAL)

        //add text decoration here
        if (textDecoration == TEXT_DECORATION_BG_ROUNDED) {
//            text.setBackgroundResource(R.drawable.bg_red_rounded)
        }

        addTab(position, tab)
    }

    private fun addIconTab(position: Int, resId: Int) {
        val tab = ImageButton(context)
        tab.setImageResource(resId)
        addTab(position, tab)
    }

    private fun addTab(position: Int, tab: View) {
        tab.isFocusable = true
        tab.setOnClickListener { pager!!.currentItem = position }

        if (tab is ImageButton) {
            tab.setPadding(tabPaddingLeftRight, tabPaddingUpDown / 5, tabPaddingLeftRight, tabPaddingUpDown / 5)
        } else {
            tab.setPadding(tabPaddingLeftRight, tabPaddingUpDown / 5, tabPaddingLeftRight, tabPaddingUpDown * 2 / 5)
        }

        tabsContainer.addView(tab, position, if (shouldExpand) expandedTabLayoutParams else defaultTabLayoutParams)
    }

    fun updateTabStyles() {

        for (i in 0..tabCount - 1) {

            val v = tabsContainer.getChildAt(i)

            v.setBackgroundResource(tabBackground)

            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
            params.weight = 1f
            v.layoutParams = params

            if (v is FrameLayout) {
                val tab = v.getChildAt(0) as TextView
                //				tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTextSize);
                if (pager!!.currentItem == i) {
                    tab.setTypeface(tabTypeface, tabTypefaceStyle)
                    tab.setTextColor(tabTextSelectedColor)
                } else {
                    tab.setTypeface(tabTypeface, Typeface.NORMAL)
                    tab.setTextColor(tabTextColor)
                }

                var text = tab.text.toString()
                text = if (text.length < 15) text else text.substring(0, 12) + "..."
                tab.text = text

                // setAllCaps() is only available from API 14, so the upper case
                // is made manually if we are on a
                // pre-ICS-build
                if (isTextAllCaps) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        tab.setAllCaps(true)
                    } else {
                        tab.text = text.toUpperCase(locale!!)
                    }
                }
            }
        }

    }

    private fun scrollToChild(position: Int, offset: Int) {

        if (tabCount == 0) {
            return
        }

        var newScrollX = tabsContainer.getChildAt(position).left + offset

        if (position > 0 || offset > 0) {
            newScrollX -= scrollOffset
        }

        if (newScrollX != lastScrollX) {
            lastScrollX = newScrollX
            smoothScrollTo(newScrollX, 0)
        }

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (isInEditMode || tabCount == 0) {
            return
        }

        val height = height

        // draw underline
        rectPaint.color = underlineColor
        canvas.drawRect(0f, (height - underlineHeight).toFloat(), tabsContainer.width.toFloat(), height.toFloat(), rectPaint)

        // draw background
        rectPaint.color = tabColor
        canvas.drawRect(0f, 0f, tabsContainer.width.toFloat(), (height - underlineHeight).toFloat(), rectPaint)

        // draw indicator line
        rectPaint.color = indicatorColor

        // default: line below current tab
        val currentTab = tabsContainer.getChildAt(currentPosition)
        var lineLeft = currentTab.left.toFloat()
        var lineRight = currentTab.right.toFloat()

        // if there is an offset, start interpolating left and right coordinates
        // between current and next tab
        if (currentTab is FrameLayout) {
            val tabContent = currentTab.getChildAt(0) //get content of current tab
            if (currentPositionOffset > 0f && currentPosition < tabCount - 1) {
                val nextTab = tabsContainer.getChildAt(currentPosition + 1)
                var nextContent = nextTab
                if (nextTab is FrameLayout) {
                    nextContent = nextTab.getChildAt(0) //get content of next tab
                }

                val nextTabLeft = nextTab.left.toFloat()
                val nextTabRight = nextTab.right.toFloat()

                lineLeft = currentPositionOffset * nextTabLeft + (1f - currentPositionOffset) * lineLeft
                lineRight = currentPositionOffset * nextTabRight + (1f - currentPositionOffset) * lineRight

                if (indicatorType == INDICATOR_WRAP_CONTENT) {
                    lineLeft += nextContent.left
                    lineRight -= (nextTab.width - nextContent.right)

                    if (lineLeft < currentTab.getLeft() + tabContent.left) {
                        lineLeft = (currentTab.getLeft() + tabContent.left).toFloat()
                        lineRight = (currentTab.getRight() - (currentTab.getWidth() - tabContent.right)).toFloat()
                    }
                }
            } else {
                if (indicatorType == INDICATOR_WRAP_CONTENT) {
                    lineLeft += tabContent.left
                    lineRight -= (currentTab.getWidth() - tabContent.right)
                }
            }
//            lineLeft = lineLeft - (tabPaddingLeftRight*2)
//            lineRight = lineRight + (tabPaddingLeftRight*2)

            canvas.drawRect(lineLeft, (height - indicatorHeight).toFloat(), lineRight, height.toFloat() /*- underlineHeight*/, rectPaint)
        } else {
            if (currentPositionOffset > 0f && currentPosition < tabCount - 1) {

                val nextTab = tabsContainer.getChildAt(currentPosition + 1)
                val nextTabLeft = nextTab.left.toFloat()
                val nextTabRight = nextTab.right.toFloat()

                if (indicatorType == INDICATOR_MATCH_PARENT) {
                    lineLeft = currentPositionOffset * nextTabLeft/* + (1f - currentPositionOffset) * lineLeft*/
                    lineRight = currentPositionOffset * nextTabRight /*+ (1f - currentPositionOffset) * lineRight*/
                } else {
                    lineLeft = currentPositionOffset * nextTabLeft + (1f - currentPositionOffset) * lineLeft
                    lineRight = currentPositionOffset * nextTabRight + (1f - currentPositionOffset) * lineRight
                }
            }
            canvas.drawRect(lineLeft, 0f, lineRight, height.toFloat(), rectPaint)
        }

        // draw divider

        dividerPaint.color = dividerColor
        for (i in 0..tabCount - 1 - 1) {
            val tab = tabsContainer.getChildAt(i)
            canvas.drawLine(tab.right.toFloat(), dividerPadding.toFloat(), tab.right.toFloat(), (height - dividerPadding).toFloat(), dividerPaint)
        }
    }

    private inner class PageListener : ViewPager.OnPageChangeListener {

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            currentPosition = position
            currentPositionOffset = positionOffset

            scrollToChild(position, (positionOffset * tabsContainer.getChildAt(position).width).toInt())

            invalidate()

            if (delegatePageListener != null) {
                delegatePageListener!!.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                scrollToChild(pager!!.currentItem, 0)
            }

            if (delegatePageListener != null) {
                delegatePageListener!!.onPageScrollStateChanged(state)
            }
        }

        override fun onPageSelected(position: Int) {
            if (delegatePageListener != null) {
                delegatePageListener!!.onPageSelected(position)
            }
        }

    }

    fun setIndicatorColorResource(resId: Int) {
        this.indicatorColor = ContextCompat.getColor(context, resId)
        invalidate()
    }

    fun setTabIndicatorType(type:Byte) {
        this.indicatorType = type
        invalidate()
    }

    fun setTabColor(tabColor: Int) {
        this.tabColor = tabColor
        invalidate()
    }

    fun setTabColorResource(resId: Int) {
        this.tabColor = ContextCompat.getColor(context, resId)
        invalidate()
    }

    fun getTabColor(): Int {
        return this.tabColor
    }

    fun setIndicatorHeight(indicatorLineHeightPx: Int) {
        this.indicatorHeight = indicatorLineHeightPx
        indicatorHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, indicatorHeight.toFloat(), dm).toInt()
        invalidate()
    }

    fun getIndicatorHeight(): Int {
        return indicatorHeight
    }

    fun setUnderlineColor(underlineColor: Int) {
        this.underlineColor = underlineColor
        invalidate()
    }

    fun setUnderlineColorResource(resId: Int) {
        this.underlineColor = ContextCompat.getColor(context, resId)
        invalidate()
    }

    fun getUnderlineColor(): Int {
        return underlineColor
    }

    fun setDividerColor(dividerColor: Int) {
        this.dividerColor = dividerColor
        invalidate()
    }

    fun setDividerColorResource(resId: Int) {
        this.dividerColor = ContextCompat.getColor(context, resId)
        invalidate()
    }

    fun getDividerColor(): Int {
        return dividerColor
    }

    fun setUnderlineHeight(underlineHeightPx: Int) {
        this.underlineHeight = underlineHeightPx
        underlineHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, underlineHeight.toFloat(), dm).toInt()
        invalidate()
    }

    fun getUnderlineHeight(): Int {
        return underlineHeight
    }

    fun setDividerPadding(dividerPaddingPx: Int) {
        this.dividerPadding = dividerPaddingPx
        invalidate()
    }

    fun getDividerPadding(): Int {
        return dividerPadding
    }

    fun setScrollOffset(scrollOffsetPx: Int) {
        this.scrollOffset = scrollOffsetPx
        invalidate()
    }

    fun getScrollOffset(): Int {
        return scrollOffset
    }

    fun setShouldExpand(shouldExpand: Boolean) {
        this.shouldExpand = shouldExpand
        requestLayout()
    }

    fun getShouldExpand(): Boolean {
        return shouldExpand
    }

    fun setAllCaps(textAllCaps: Boolean) {
        this.isTextAllCaps = textAllCaps
    }

    var textSize: Int
        get() = tabTextSize
        set(textSizePx) {
            this.tabTextSize = textSizePx
            updateTabStyles()
        }

    fun setTextColorResource(resId: Int) {
        this.tabTextColor = ContextCompat.getColor(context, resId)
        updateTabStyles()
    }

    fun setTextSelectedColor(textColor: Int) {
        this.tabTextSelectedColor = textColor
        updateTabStyles()
    }

    fun setTextSelectedColorResource(resId: Int) {
        this.tabTextSelectedColor = ContextCompat.getColor(context, resId)
        updateTabStyles()
    }

    var textColor: Int
        get() = tabTextColor
        set(textColor) {
            this.tabTextColor = textColor
            updateTabStyles()
        }

    fun setTypeface(typeface: Typeface, style: Int) {
        this.tabTypeface = typeface
        this.tabTypefaceStyle = style
        updateTabStyles()
    }

    fun setTabPaddingUpDown(paddingPx: Int) {
        this.tabPaddingUpDown = paddingPx
        updateTabStyles()
    }

    fun getTabPaddingUpDown(): Int {
        return tabPaddingUpDown
    }

    fun setTabPaddingLeftRight(paddingPx: Int) {
        this.tabPaddingLeftRight = paddingPx
        updateTabStyles()
    }

    fun getTabPaddingLeftRight(): Int {
        return tabPaddingLeftRight
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)
        currentPosition = savedState.currentPosition
        requestLayout()
    }

    public override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        savedState.currentPosition = currentPosition
        return savedState
    }

    internal class SavedState : View.BaseSavedState {
        var currentPosition: Int = 0

        constructor(superState: Parcelable) : super(superState) {}

        private constructor(`in`: Parcel) : super(`in`) {
            currentPosition = `in`.readInt()
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeInt(currentPosition)
        }

        companion object {

            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(`in`: Parcel): SavedState {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    companion object {

        val TEXT_DECORATION_NONE: Byte = 0x0
        private val TEXT_DECORATION_BG_ROUNDED: Byte = 0x1
        val INDICATOR_WRAP_CONTENT: Byte = 0x0
        val INDICATOR_MATCH_PARENT: Byte = 0x1

        // @formatter:off
        private val ATTRS = intArrayOf(android.R.attr.textSize, android.R.attr.textColor)
    }

}
