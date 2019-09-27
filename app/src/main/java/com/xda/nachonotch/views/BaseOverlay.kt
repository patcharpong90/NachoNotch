package com.xda.nachonotch.views

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.View
import android.view.WindowManager
import com.xda.nachonotch.services.BackgroundHandler

abstract class BaseOverlay(
        context: Context,
        backgroundResource: Int = 0,
        backgroundColor: Int = Int.MIN_VALUE
) : View(context) {
    var isAdded = false
        set(value) {
            field = value
            isWaitingToAdd = false
        }
    var isWaitingToAdd = false

    internal val service: BackgroundHandler
        get() = context as BackgroundHandler

    private var showAnimator: ValueAnimator? = null
    private var hideAnimator: ValueAnimator? = null

    open val params = WindowManager.LayoutParams().apply {
        flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or
                WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS

        type = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_PRIORITY_PHONE
        } else {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        }

        format = PixelFormat.TRANSLUCENT
    }

    init {
        if (backgroundResource != 0) {
            setBackgroundResource(backgroundResource)
        } else if (backgroundColor != Int.MIN_VALUE) {
            setBackgroundColor(backgroundColor)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            isForceDarkAllowed = false
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isAdded = true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isAdded = false
    }

    open fun update(wm: WindowManager) {
        try {
            wm.updateViewLayout(this, params)
        } catch (e: Exception) {}
    }

    open fun show(wm: WindowManager) {
        hideAnimator?.cancel()

        showAnimator = ObjectAnimator.ofFloat(params.alpha, 1f)
        showAnimator?.addUpdateListener {
            params.alpha = it.animatedValue.toString().toFloat()
            update(wm)
        }
        showAnimator?.start()
    }

    open fun hide(wm: WindowManager) {
        showAnimator?.cancel()

        hideAnimator = ObjectAnimator.ofFloat(params.alpha, 0f)
        hideAnimator?.addUpdateListener {
            params.alpha = it.animatedValue.toString().toFloat()
            update(wm)
        }
        hideAnimator?.start()
    }

    final override fun setBackgroundResource(resid: Int) {
        super.setBackgroundResource(resid)
    }

    final override fun setBackgroundColor(color: Int) {
        super.setBackgroundColor(color)
    }
}