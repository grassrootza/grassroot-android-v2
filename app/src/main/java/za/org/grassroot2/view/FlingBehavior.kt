package za.org.grassroot2.view

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.v4.view.ViewCompat
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View

class FlingBehavior(context: Context, attrs: AttributeSet) : AppBarLayout.Behavior(context, attrs) {

    private var isPositive: Boolean = false

    override fun onNestedFling(coordinatorLayout: CoordinatorLayout, child: AppBarLayout, target: View, velocityX: Float, passedVelocityY: Float, passedConsumed: Boolean): Boolean {
        var velocityY = passedVelocityY
        var consumed = passedConsumed
        if (velocityY > 0 && !isPositive || velocityY < 0 && isPositive) {
            velocityY *= -1
        }
        if (target is RecyclerView && velocityY < 0) {
            val firstChild = target.getChildAt(0)
            val childAdapterPosition = target.getChildAdapterPosition(firstChild)
            consumed = childAdapterPosition > TOP_CHILD_FLING_THRESHOLD
        }
        return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed)
    }

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: AppBarLayout, target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
        isPositive = dy > 0
    }

    companion object {
        private val TOP_CHILD_FLING_THRESHOLD = 3
    }
}
