package uk.lobsterdoodle.namepicker.ui.draw

import android.icu.text.ListFormatter
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uk.lobsterdoodle.namepicker.R
import uk.lobsterdoodle.namepicker.navigation.isCompactHeight
import uk.lobsterdoodle.namepicker.ui.components.HatIllustration
import uk.lobsterdoodle.namepicker.ui.nameLabel

/** Hat shake: ±6°, 3 cycles (spec "Reveal" step 1). */
private const val SHAKE_MS = 600

/** Gap between cards; for more than 4 names it shrinks so the last card starts by 1.5 s. */
internal fun revealStaggerMs(count: Int): Float =
    if (count <= 4) 250f else minOf(250f, 900f / (count - 1))

/** Animator duration scale 0 = "Remove animations" (spec "Reduced motion"); there is no Compose API. */
private fun reducedMotion(context: android.content.Context): Boolean =
    Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f

/**
 * The draw moment (spec "Reveal", D10): a full-window takeover over every pane. The result is
 * already computed and saved when this opens; the motion only presents it.
 *
 * - Expressive: the hat shakes, the cards pop out one by one on the expressive spatial spring, then
 *   the buttons fade in. Reduced motion: no shake, no stagger, one crossfade.
 * - Plays once per result ([RevealUi.played]); rotation or restore shows the end layout.
 * - TalkBack hears the whole result at once ("Drawn: Ada, Sam and Tom"), not gated on the motion.
 * - Dismissed by Done, back (with a predictive-back preview) or Escape.
 */
@Composable
fun RevealOverlay(reveal: RevealUi, onDone: () -> Unit, onDrawMore: () -> Unit, onPlayed: () -> Unit) {
    Dialog(
        onDismissRequest = onDone,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        RevealWindowBars()
        RevealContent(reveal, onDone, onDrawMore, onPlayed)
    }
}

/** The backdrop is dark in both themes, so the dialog window's bar icons are light, and its bars transparent. */
@Composable
private fun RevealWindowBars() {
    val view = LocalView.current
    SideEffect {
        val window = (view.parent as? DialogWindowProvider)?.window ?: return@SideEffect
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            @Suppress("DEPRECATION")
            window.statusBarColor = Color.Transparent.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = Color.Transparent.toArgb()
        }
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
    }
}

private data class RevealColors(
    val backdrop: Color,
    val onBackdrop: Color,
    val sparkle: Color,
    val moreContainer: Color,
    val moreContent: Color,
)

@Composable
private fun revealColors(): RevealColors {
    val c = MaterialTheme.colorScheme
    return if (isSystemInDarkTheme()) {
        RevealColors(c.primaryContainer, c.onPrimaryContainer, c.tertiary, c.primary, c.onPrimary)
    } else {
        RevealColors(c.primary, c.onPrimary, c.tertiaryContainer, c.surfaceContainerLowest, c.primary)
    }
}

private enum class RevealLayout { PHONE, LANDSCAPE, LARGE }

@Composable
private fun RevealContent(reveal: RevealUi, onDone: () -> Unit, onDrawMore: () -> Unit, onPlayed: () -> Unit) {
    val context = LocalContext.current
    val colors = revealColors()
    val sizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
    val layout = when {
        isCompactHeight(sizeClass) -> RevealLayout.LANDSCAPE
        sizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND) -> RevealLayout.LARGE
        else -> RevealLayout.PHONE
    }
    val currentOnPlayed by rememberUpdatedState(onPlayed)
    val currentOnDone by rememberUpdatedState(onDone)

    // --- Motion: one set of animatables per result; at rest at the end layout once played. ---
    val reduced = remember { reducedMotion(context) }
    val start = if (reveal.played) 1f else 0f
    val shake = remember(reveal.names) { Animatable(0f) }
    val cards = remember(reveal.names) { reveal.names.map { Animatable(start) } }
    val buttons = remember(reveal.names) { Animatable(start) }
    val fade = remember(reveal.names) { Animatable(if (reveal.played || !reduced) 1f else 0f) }
    val spring = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    LaunchedEffect(reveal.names) {
        if (reveal.played) return@LaunchedEffect
        if (reduced) {
            cards.forEach { it.snapTo(1f) }
            buttons.snapTo(1f)
            fade.animateTo(1f, tween(300))
        } else {
            coroutineScope {
                launch {
                    shake.animateTo(0f, keyframes {
                        durationMillis = SHAKE_MS
                        -6f at 50
                        6f at 150
                        -6f at 250
                        6f at 350
                        -6f at 450
                        6f at 530
                        0f at SHAKE_MS
                    })
                }
                val stagger = revealStaggerMs(cards.size)
                cards.forEachIndexed { i, card ->
                    launch {
                        delay(SHAKE_MS + (i * stagger).toLong())
                        card.animateTo(1f, spring)
                    }
                }
            }
            buttons.animateTo(1f, tween(200))
        }
        currentOnPlayed()
    }

    // --- Accessibility: announce the whole result once, after the first frame (a live region only
    // speaks on a change). A restore doesn't announce again. ---
    val spokenNames = reveal.names.map { nameLabel(it.text) }
    val announcement = stringResource(R.string.reveal_a11y, ListFormatter.getInstance().format(spokenNames))
    var spoken by remember(reveal.names) { mutableStateOf(if (reveal.played) announcement else "") }
    LaunchedEffect(reveal.names) {
        if (!reveal.played) {
            withFrameNanos {}
            spoken = announcement
        }
    }

    // --- Predictive back: the takeover shrinks and fades with the gesture, then dismisses. ---
    var backProgress by remember { mutableFloatStateOf(0f) }
    PredictiveBackHandler { progress ->
        try {
            progress.collect { backProgress = it.progress }
            currentOnDone()
        } catch (e: CancellationException) {
            backProgress = 0f
            throw e
        }
    }

    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    val title = stringResource(R.string.reveal_title)

    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer {
                val s = 1f - 0.1f * backProgress
                scaleX = s
                scaleY = s
                alpha = fade.value * (1f - 0.5f * backProgress)
            }
            .background(colors.backdrop)
            .semantics { paneTitle = title }
            .focusRequester(focus)
            .focusable()
            .onPreviewKeyEvent {
                if (it.key == Key.Escape && it.type == KeyEventType.KeyUp) {
                    currentOnDone()
                    true
                } else {
                    false
                }
            },
    ) {
        // Hidden announcer: set once the window is up.
        Box(Modifier.size(1.dp).semantics { liveRegion = LiveRegionMode.Assertive; contentDescription = spoken })

        val hat: @Composable (Int) -> Unit = { size ->
            HatIllustration(
                size = size.dp,
                hat = colors.onBackdrop,
                sparkle = colors.sparkle,
                modifier = Modifier.graphicsLayer {
                    rotationZ = shake.value
                    transformOrigin = TransformOrigin(0.5f, 0.9f)
                },
            )
        }
        val titleText: @Composable (TextStyle) -> Unit = { style ->
            Text(title, style = style, color = colors.onBackdrop, textAlign = TextAlign.Center)
        }
        val cardsArea: @Composable (Modifier, Int) -> Unit = { modifier, columns ->
            RevealCards(reveal, cards, layout, columns, modifier)
        }
        val buttonsArea: @Composable (Boolean) -> Unit = { stacked ->
            RevealButtons(reveal, colors, stacked, onDone, onDrawMore, Modifier.graphicsLayer { alpha = buttons.value })
        }
        val single = reveal.names.size == 1

        when (layout) {
            RevealLayout.LANDSCAPE -> Row(
                Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(horizontal = 28.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    titleText(MaterialTheme.typography.headlineLarge)
                    hat(120)
                }
                cardsArea(Modifier.weight(1f), if (single) 1 else 2)
                Box(Modifier.width(200.dp)) { buttonsArea(true) }
            }
            else -> Column(
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .then(
                        if (layout == RevealLayout.LARGE) {
                            Modifier.padding(start = 120.dp, end = 120.dp, top = 56.dp, bottom = 48.dp)
                        } else {
                            Modifier.padding(start = 24.dp, end = 24.dp, top = 40.dp, bottom = 32.dp)
                        },
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (layout == RevealLayout.LARGE) 28.dp else 20.dp),
            ) {
                Column(
                    Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                ) {
                    titleText(MaterialTheme.typography.displaySmall.copy(textAlign = TextAlign.Center))
                    cardsArea(Modifier.fillMaxWidth().weight(1f, fill = false), if (single || layout == RevealLayout.PHONE) 1 else 3)
                }
                hat(if (layout == RevealLayout.LARGE) 140 else 112)
                buttonsArea(false)
            }
        }
    }
}

/** The name cards: a column on phones, a grid on larger windows; many names scroll. */
@Composable
private fun RevealCards(
    reveal: RevealUi,
    progress: List<Animatable<Float, *>>,
    layout: RevealLayout,
    columns: Int,
    modifier: Modifier,
) {
    val single = reveal.names.size == 1
    val style = when {
        single && layout == RevealLayout.LANDSCAPE -> MaterialTheme.typography.displayMedium
        single -> MaterialTheme.typography.displayLarge
        layout == RevealLayout.LANDSCAPE -> MaterialTheme.typography.headlineLarge
        else -> MaterialTheme.typography.displaySmall
    }
    val padding = when {
        single -> Modifier.padding(horizontal = 24.dp, vertical = 28.dp)
        layout == RevealLayout.LANDSCAPE -> Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        else -> Modifier.padding(horizontal = 24.dp, vertical = 18.dp)
    }
    val rise = with(LocalDensity.current) { 90.dp.toPx() }
    Column(
        modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        reveal.names.withIndex().chunked(columns).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { (i, name) ->
                    val p = progress.getOrNull(i)
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        shadowElevation = 4.dp,
                        modifier = Modifier.weight(1f).graphicsLayer {
                            val v = p?.value ?: 1f
                            alpha = v.coerceIn(0f, 1f)
                            translationY = (1f - v) * rise
                            val s = 0.55f + 0.45f * v
                            scaleX = s
                            scaleY = s
                        },
                    ) {
                        Text(
                            nameLabel(name.text),
                            style = style,
                            textAlign = TextAlign.Center,
                            modifier = padding.fillMaxWidth(),
                        )
                    }
                }
                repeat(columns - row.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}

/** Done (outlined) and Draw n more, or "Everyone's been drawn" when the hat is empty. */
@Composable
private fun RevealButtons(
    reveal: RevealUi,
    colors: RevealColors,
    stacked: Boolean,
    onDone: () -> Unit,
    onDrawMore: () -> Unit,
    modifier: Modifier,
) {
    val label = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
    val done: @Composable (Modifier) -> Unit = { m ->
        OutlinedButton(
            onClick = onDone,
            shape = CircleShape,
            border = BorderStroke(2.dp, colors.onBackdrop),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.onBackdrop),
            modifier = m.height(56.dp),
        ) { Text(stringResource(R.string.reveal_done), style = label) }
    }
    val more: @Composable (Modifier) -> Unit = { m ->
        if (reveal.noneLeft) {
            Text(
                stringResource(R.string.reveal_none_left),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = colors.onBackdrop,
                textAlign = TextAlign.Center,
                modifier = m,
            )
        } else {
            Button(
                onClick = onDrawMore,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = colors.moreContainer, contentColor = colors.moreContent),
                modifier = m.height(56.dp),
            ) { Text(pluralStringResource(R.plurals.reveal_draw_more, reveal.more, reveal.more), style = label) }
        }
    }
    if (stacked) {
        Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            done(Modifier.fillMaxWidth())
            more(Modifier.fillMaxWidth())
        }
    } else {
        Row(
            modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            done(Modifier)
            more(Modifier)
        }
    }
}
