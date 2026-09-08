package uk.lobsterdoodle.namepicker.ui

import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Keeps the activity clear of the system bars.
 *
 * From Android 15 the system draws apps edge to edge, and from Android 16 (the target here) an app
 * can no longer opt out. This app has no inset handling of its own, so on 16 the decor is laid out
 * from y=0 and the content ends up behind the status bar and behind the navigation bar.
 *
 * Padding AppCompat's decor root restores the outer inset, since that view is the parent of both
 * the action bar and the content. Doing so replaces ActionBarOverlayLayout's own inset handling
 * though, and that is what normally offsets the content below the action bar — so the content is
 * offset here instead, by the same actionBarSize the theme gives the bar itself.
 *
 * Before 16 the system painted the status bar background. It no longer does, so that strip shows
 * the window background, which is light; hence dark status bar icons, without which the clock and
 * status icons are invisible.
 */
fun AppCompatActivity.applySystemBarInsets() {
    val decor: View? = findViewById(androidx.appcompat.R.id.decor_content_parent)
    val content = findViewById<View>(android.R.id.content)
    val root = decor ?: content

    WindowInsetsControllerCompat(window, root).isAppearanceLightStatusBars = true

    val actionBarHeight = TypedValue().let { value ->
        if (theme.resolveAttribute(android.R.attr.actionBarSize, value, true)) {
            TypedValue.complexToDimensionPixelSize(value.data, resources.displayMetrics)
        } else {
            0
        }
    }

    ViewCompat.setOnApplyWindowInsetsListener(root) { view, windowInsets ->
        val bars = windowInsets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
        if (decor != null) {
            // Only offset the content when this listener is actually doing the insetting. Before
            // Android 15 the decor is inset by the system and this never runs, so AppCompat keeps
            // placing the content below the action bar itself and must not be double counted.
            content.setPadding(0, if (bars.top > 0) actionBarHeight else 0, 0, 0)
        }
        WindowInsetsCompat.CONSUMED
    }
}
