package za.org.grassroot2.view.widget;

import android.content.Context;
import android.os.Build;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class WrapContentViewPager extends ViewPager {


    private static final String TAG = WrapContentViewPager.class.getName();
    private boolean mAnimStarted;
    private AdjustAnimation adjustAnimation;
    private boolean scrollingEnabled = false;
    private boolean shouldPlayAnim = true;

    private class AdjustAnimation extends Animation {

        private int targetHeight;
        private float heightChange;
        private int currentHeight;

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            if (interpolatedTime >= 1) {
                getLayoutParams().height = targetHeight;
            } else {
                int stepHeight = (int) (heightChange * interpolatedTime);
                getLayoutParams().height = currentHeight + stepHeight;
            }
            requestLayout();
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }

        public void setParameters(int currentHeight, int targetHeight, int diff) {
            this.currentHeight = currentHeight;
            this.targetHeight = targetHeight;
            heightChange = diff;
        }
    }

    public WrapContentViewPager(Context context) {
        super(context);
        init();
    }

    private void init() {
        adjustAnimation = new AdjustAnimation();
        adjustAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mAnimStarted = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mAnimStarted = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    @Override
    public void setCurrentItem(int item) {
        shouldPlayAnim = true;
        super.setCurrentItem(item);
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        shouldPlayAnim = true;
        super.setCurrentItem(item, smoothScroll);
        requestLayout();
    }

    public WrapContentViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if(shouldPlayAnim && !mAnimStarted && null != getAdapter()) {
            int height = 0;
            View child = ((FragmentStatePagerAdapter) getAdapter()).getItem(getCurrentItem()).getView();
            if (child != null) {
                child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                height = child.getMeasuredHeight();
                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN && height < getMinimumHeight()) {
                    height = getMinimumHeight();
                }
            }
            int newHeight = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
            if (getLayoutParams().height != 0 && heightMeasureSpec != newHeight) {
                final int heightChange = height - getLayoutParams().height;
                adjustAnimation.setParameters(getLayoutParams().height, height, heightChange);
                adjustAnimation.setDuration(100);
                startAnimation(adjustAnimation);
                mAnimStarted = true;
                shouldPlayAnim = false;
            } else {
                heightMeasureSpec = newHeight;
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return scrollingEnabled && super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return scrollingEnabled && super.onTouchEvent(ev);
    }
}
