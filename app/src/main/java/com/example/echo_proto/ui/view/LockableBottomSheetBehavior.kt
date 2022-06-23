package com.example.echo_proto.ui.view
//
//import android.content.Context
//import android.util.AttributeSet
//import android.view.MotionEvent
//import android.view.View
//import androidx.coordinatorlayout.widget.CoordinatorLayout
////import com.google.android.material.bottomsheet.ViewPagerBottomSheetBehavior
//import com.google.android.material.bottomsheet.BottomSheetBehavior
//
//
//class LockableBottomSheetBehavior<V : View?> : BottomSheetBehavior<V>() {
//    private var isLocked = false
//
//    fun setLocked(locked: Boolean) {
//        isLocked = locked
//    }
//
//
//    fun onInterceptTouchEvent(parent: CoordinatorLayout?, child: V, event: MotionEvent?): Boolean {
//        var handled = false
//        if (!isLocked) {
//            handled = super.onInterceptTouchEvent(parent!!, child, event!!)
//        }
//        return handled
//    }
//
//    fun onTouchEvent(parent: CoordinatorLayout?, child: V, event: MotionEvent?): Boolean {
//        var handled = false
//        if (!isLocked) {
//            handled = super.onTouchEvent(parent, child, event)
//        }
//        return handled
//    }
//
//    fun onStartNestedScroll(
//        coordinatorLayout: CoordinatorLayout?, child: V, directTargetChild: View?,
//        target: View?, axes: Int, type: Int
//    ): Boolean {
//        var handled = false
//        if (!isLocked) {
//            handled = super.onStartNestedScroll(
//                coordinatorLayout,
//                child,
//                directTargetChild,
//                target,
//                axes,
//                type
//            )
//        }
//        return handled
//    }
//
//    fun onNestedPreScroll(
//        coordinatorLayout: CoordinatorLayout?, child: V, target: View?,
//        dx: Int, dy: Int, consumed: IntArray?, type: Int
//    ) {
//        if (!isLocked) {
//            super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
//        }
//    }
//
//    fun onStopNestedScroll(
//        coordinatorLayout: CoordinatorLayout?,
//        child: V,
//        target: View?,
//        type: Int
//    ) {
//        if (!isLocked) {
//            super.onStopNestedScroll(coordinatorLayout, child, target, type)
//        }
//    }
//
//    fun onNestedPreFling(
//        coordinatorLayout: CoordinatorLayout?, child: V, target: View?,
//        velocityX: Float, velocityY: Float
//    ): Boolean {
//        var handled = false
//        if (!isLocked) {
//            handled = super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY)
//        }
//        return handled
//    }
//}