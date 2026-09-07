package uk.lobsterdoodle.namepicker.ui

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Pads the activity's root view clear of the system bars.
 *
 * From Android 15 the system draws apps edge to edge, and from Android 16 (the target here) an app
 * can no longer opt out. Without this the action bar sits underneath the status bar and the bottom
 * of each screen sits underneath the navigation bar.
 *
 * The padding goes on AppCompat's decor root where one exists, because that view is the parent of
 * both the action bar and the content; padding only the content would leave the action bar behind
 * the status bar. Activities with no action bar fall back to the content view.
 */
fun AppCompatActivity.applySystemBarInsets() {
    val root = findViewById<View>(androidx.appcompat.R.id.decor_content_parent)
        ?: findViewById(android.R.id.content)

    ViewCompat.setOnApplyWindowInsetsListener(root) { view, windowInsets ->
        val bars = windowInsets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
        WindowInsetsCompat.CONSUMED
    }
}
