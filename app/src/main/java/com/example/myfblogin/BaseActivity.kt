package com.example.myfblogin

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import android.widget.RelativeLayout.LayoutParams
import com.example.myfblogin.component.PagerSlidingTabStrip
import kotlinx.android.synthetic.main.header_activity.view.*
import kotlinx.android.synthetic.main.toolbar.*


abstract class BaseActivity : AppCompatActivity() {

    var mTabs: PagerSlidingTabStrip? = null
    var mToolbar: Toolbar? = null
    var tabEnabled = true
    private var mActionViewIds = ArrayList<Int>()
    private var flMainLayout: FrameLayout? = null
    private var mMainLayout: LinearLayout? = null
    private var rlHeaderLayout: RelativeLayout? = null
    private var mLayoutBack: LinearLayout? = null
    private var mLayoutTitle: LinearLayout? = null
    private var mImgIcon: ImageView? = null
    private var mIconPadding:Float? = null

    private val clickListener = View.OnClickListener { v ->
        if (v.id == R.id.layoutHome) {
            onBackMenuClick(v)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            mIconPadding = resources.displayMetrics.density * 10
            setTheme(R.style.MyTheme)
            setupActionBar()
            mLayoutBack!!.imgMenu.visibility = View.GONE
        } catch (e: OutOfMemoryError) {
            finish()
            System.gc()
            return
        }
    }

    override fun onTitleChanged(title: CharSequence, color: Int) {
        super.onTitleChanged(title, color)
        setHeaderTitle(title.toString())
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        initToolbar()
        setHeaderColor(ContextCompat.getColor(this@BaseActivity, R.color.colorPrimary))
    }

    fun initToolbar() {
        if (flMainLayout == null) {
            return
        }
        try {
            // Set a toolbar to replace the action bar.
            mToolbar = toolbar
            mToolbar!!.setContentInsetsAbsolute(0, 0)
            setSupportActionBar(mToolbar)

            if (flMainLayout?.parent != null) {
                (flMainLayout?.parent as ViewGroup).removeView(flMainLayout)
            }
            updateTabs()

            supportActionBar?.setDisplayShowCustomEnabled(true)
            supportActionBar?.customView = flMainLayout
            var layoutParams: ViewGroup.LayoutParams = mToolbar!!.layoutParams

            if (tabEnabled) {
                layoutParams.height = resources.getDimensionPixelSize(R.dimen.toolbar_with_tab_height)
                mTabs!!.visibility = View.VISIBLE
            } else {
                layoutParams.height = resources.getDimensionPixelSize(R.dimen.toolbar_no_tab_height)
                mTabs!!.visibility = View.GONE
            }

            mToolbar!!.layoutParams = layoutParams

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateTabs() {
        if (flMainLayout == null) {
            return
        }

        mTabs = PagerSlidingTabStrip(this@BaseActivity, null)
        mTabs!!.shouldFit(true)
        mTabs!!.setShouldExpand(true)
        mTabs!!.setTabColorResource(R.color.colorPrimary)
//        mTabs!!.setTypeface(FontFactory.getInstance(this).getFont(FontFactory.getFontUltraBold(this)), Typeface.NORMAL)
        mTabs!!.setTabPaddingUpDown(resources.getDimensionPixelSize(R.dimen.activity_vertical_margin))
        mTabs!!.setTextColorResource(R.color.colorWhiteTrans)
        mTabs!!.setTextSelectedColorResource(android.R.color.white)
        mTabs!!.setIndicatorColorResource(android.R.color.white)
        mTabs!!.setIndicatorHeight(3)
    }

    private fun setHeaderColor(color: Int) {
        try {
            mMainLayout!!.setBackgroundColor(color)
            mToolbar!!.setBackgroundColor(color)
            setStatusBarColor(color)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setStatusBarColor(aColor: Int) {
        var color = aColor
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[2] *= 0.70f // value component
        color = Color.HSVToColor(hsv)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = color
        }
    }

    fun onBackMenuClick(view: View) {
        //if clickable, finish the activity, otherwise do nothing
        if (view.tag as Boolean) {
            finish()
        }
    }

    fun addAction(view: View) {
        var params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
        if (getActionCount() > 0) {
            params.addRule(RelativeLayout.LEFT_OF, mActionViewIds[mActionViewIds.size - 1])
        } else {
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        }

        view.layoutParams = params
        rlHeaderLayout!!.addView(view)
        mActionViewIds.add(view.id)

        params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
        params.addRule(RelativeLayout.LEFT_OF, view.id)
        params.addRule(RelativeLayout.RIGHT_OF, R.id.layoutHome)
        mLayoutTitle!!.layoutParams = params
    }

    fun setHeaderTitle(title: String) {
        if (title === "") {
            mLayoutTitle!!.visibility = View.GONE
            rlHeaderLayout!!.imgLogo.visibility = View.VISIBLE
        } else {
            (mLayoutTitle!!.txtTitle as TextView).text = title
            val face = Typeface.createFromAsset(assets, String.format("fonts/%s", getString(R.string.font_normal)))
            (mLayoutTitle!!.txtTitle as TextView).typeface = face
            (mLayoutTitle!!.txtTitle as TextView).setTextColor(ContextCompat.getColor(this@BaseActivity, android.R.color.white))
            mLayoutTitle!!.visibility = View.VISIBLE
            rlHeaderLayout!!.imgLogo.visibility = View.GONE
        }
    }

    fun onCreateTab(tabs: PagerSlidingTabStrip): View {
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.HORIZONTAL
        layout.layoutParams = params
//        val tabColor = Color.parseColor(if (JBabeArticleManager.PrefMgr().getViewMode() === JUserPreferencesManager.EViewModeNight) JFlavourPreferencesManager.COLOR_BABE_THEME_DARK else JFlavourPreferencesManager.COLOR_BABE_THEME_LIGHT)
//        layout.setBackgroundColor(tabColor)

        val tabParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
        tabParams.weight = 1f
        tabs.layoutParams = tabParams

        tabs.overScrollMode = View.OVER_SCROLL_NEVER
        tabs.setDividerColor(0x00000000)
        tabs.setTextSelectedColorResource(android.R.color.white)
        tabs.setIndicatorColorResource(android.R.color.white)
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            tabs.setUnderlineHeight(1)
            tabs.setUnderlineColorResource(android.R.color.white)
        }
        layout.addView(tabs)
        return layout
    }

    fun hideHeader() {
        supportActionBar!!.hide()
    }

    fun setLeftMenuIcon(resId: Int, clickableBackground: Boolean) {
        if (mImgIcon == null) {
            mImgIcon = ImageView(this)
        }
        if (!clickableBackground) {
            mIconPadding = 0f
        }
        mImgIcon!!.setImageResource(resId)
        mImgIcon!!.setPadding((mIconPadding!! / 2).toInt(), 0, mIconPadding!!.toInt(), 0)
        mLayoutBack!!.removeAllViews()
        mLayoutBack!!.addView(mImgIcon)

        if (!clickableBackground) {
            mLayoutBack!!.setBackgroundColor(Color.TRANSPARENT)
        } else {
            mLayoutBack!!.setBackgroundResource(R.drawable.bg_transparent_selectable)
        }
        mLayoutBack!!.tag = clickableBackground
    }

    @SuppressLint("InflateParams")
    private fun setupActionBar() {
        try {
            val mLayoutInflater: LayoutInflater = this@BaseActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            flMainLayout = mLayoutInflater.inflate(R.layout.header_activity, null) as FrameLayout?

            mMainLayout = flMainLayout!!.llHeaderLayout
            rlHeaderLayout = flMainLayout!!.rlHeaderLayout

            mLayoutTitle = rlHeaderLayout!!.layoutTitle
            mLayoutBack = rlHeaderLayout!!.layoutHome
            mLayoutBack!!.setOnClickListener(clickListener)
            mLayoutBack!!.tag = true
        } catch (e: OutOfMemoryError) {
            System.gc()
            setupActionBar()
        }

        checkActions()
        onCreateHeaderAction()
    }

    @SuppressLint("ResourceType")
    private fun checkActions() {
        val view = FrameLayout(this)
        val params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        view.layoutParams = params
        view.id = 127
        addAction(view)
        mActionViewIds.add(view.id)
    }

    private fun getActionCount(): Int {
        return mActionViewIds.size - 1
    }

    protected abstract fun onCreateHeaderAction()
}
