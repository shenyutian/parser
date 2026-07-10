package org.apk.parser.app

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * 处理 Android 15+（targetSdk 35）强制 edge-to-edge：把系统栏（状态栏/导航栏）内边距
 * 应用到根视图，避免内容被状态栏遮挡导致按钮无法点击。
 */
fun View.applySystemBarsPadding() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        v.updatePadding(top = bars.top, bottom = bars.bottom, left = bars.left, right = bars.right)
        insets
    }
}
