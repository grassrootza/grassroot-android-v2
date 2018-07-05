package za.org.grassroot2.view.widget

import android.content.Context
import android.os.Build
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation

class WrapContentViewPager : ViewPager {
    private var mAnimStarted: Boolean = false
    private var adjustAnimation: AdjustAnimation? = null
    private val scrollingEnabled = false
    private var shouldPlayAnim = true

    private inner class AdjustAnimation : Animation() {

        private var targetHeight: Int = 0
        private var heightChange: Float = 0.toFloat()
        private var currentHeight: Int = 0

        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            if (interpolatedTime >= 1) {
                layoutParams.height = targetHeight
            } else {
                val stepHeight = (heightChange * interpolatedTime).toInt()
                layoutParams.height = currentHeight + stepHeight
            }
            requestLayout()
        }

        override fun willChangeBounds(): Boolean {
            return true
        }

        fun setParameters(currentHeight: Int, targetHeight: Int, diff: Int) {
            this.currentHeight = currentHeight
            this.targetHeight = targetHeight
            heightChange = diff.toFloat()
        }
    }

    constructor(context: Context) : super(context) {
        init()
    }

    private fun init() {
        adjustAnimation = AdjustAnimation()
        adjustAnimation!!.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                mAnimStarted = true
            }

            override fun onAnimationEnd(animation: Animation) {
                mAnimStarted = false
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
    }

    override fun setCurrentItem(item: Int) {
        shouldPlayAnim = true
        super.setCurrentItem(item)
    }

    override fun setCurrentItem(item: Int, smoothScroll: Boolean) {
        shouldPlayAnim = true
        super.setCurrentItem(item, smoothScroll)
        requestLayout()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpec = heightMeasureSpec
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (shouldPlayAnim && !mAnimStarted && null != adapter) {
            var height = 0
            val child = (adapter as FragmentStatePagerAdapter).getItem(currentItem).view
            if (child != null) {
                child.measure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
                height = child.measuredHeight
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && height < minimumHeight) {
                    height = minimumHeight
                }
            }
            val newHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
            if (layoutParams.height != 0 && heightMeasureSpec != newHeight) {
                val heightChange = height - layoutParams.height
                adjustAnimation!!.setParameters(layoutParams.height, height, heightChange)
                adjustAnimation!!.duration = 100
                startAnimation(adjustAnimation)
                mAnimStarted = true
                shouldPlayAnim = false
            } else {
                heightMeasureSpec = newHeight
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return scrollingEnabled && super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return scrollingEnabled && super.onTouchEvent(ev)
    }

    companion object {


        private val TAG = WrapContentViewPager::class.java.name
    }
}
