package uk.lobsterdoodle.namepicker.ui.draw

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import uk.lobsterdoodle.namepicker.R
import uk.lobsterdoodle.namepicker.shared.draw.DrawBarState
import uk.lobsterdoodle.namepicker.shared.draw.DrawState
import uk.lobsterdoodle.namepicker.shared.draw.SelectAllBox
import uk.lobsterdoodle.namepicker.ui.components.HatIllustration
import uk.lobsterdoodle.namepicker.ui.nameLabel

// The Draw screen's parts (spec "Draw", "Select-all header", "Chip states", "Draw bar", "States").
// Geometry and colour roles follow docs/design/3.0-mockups/project/DrawScreen.dc.html.

/** "8 of 30 in the hat · 4 drawn": in the hat out of every name, plus the drawn count once there is one. */
@Composable
fun counterText(draw: DrawState): String {
    val counter = pluralStringResource(R.plurals.draw_counter, draw.total, draw.inHat, draw.total)
    if (draw.drawnCount == 0) return counter
    return counter + " " + pluralStringResource(R.plurals.draw_counter_drawn, draw.drawnCount, draw.drawnCount)
}

/**
 * The sticky header: one tri-state checkbox labelled by the counter (D18). The box reflects the
 * not-drawn names only; tapping it selects or clears every name.
 */
@Composable
fun SelectAllHeader(draw: DrawState, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val counter = counterText(draw)
    val enabled = draw.box != SelectAllBox.DISABLED
    val state = when (draw.box) {
        SelectAllBox.ALL -> ToggleableState.On
        SelectAllBox.SOME -> ToggleableState.Indeterminate
        SelectAllBox.NONE, SelectAllBox.DISABLED -> ToggleableState.Off
    }
    val action = stringResource(if (draw.box == SelectAllBox.ALL) R.string.draw_clear_all else R.string.draw_select_all)
    val label = stringResource(R.string.draw_select_all_a11y, stringResource(R.string.draw_select_all), counter)
    Column(modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
        Row(
            Modifier.height(56.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                Modifier
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .triStateToggleable(state = state, enabled = enabled, role = Role.Checkbox, onClick = onToggle)
                    .semantics(mergeDescendants = true) {
                        contentDescription = label
                        onClick(label = action) { onToggle(); true }
                    }
                    .padding(end = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SelectAllBoxIcon(draw.box)
                // The row's label already carries the counter; don't read it twice.
                Text(counter, style = MaterialTheme.typography.titleSmall, modifier = Modifier.clearAndSetSemantics {})
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

/** The 22 dp box. "Some" is the tonal dash: primaryContainer fill, 2 dp primary border, primary dash. */
@Composable
private fun SelectAllBoxIcon(box: SelectAllBox) {
    val colors = MaterialTheme.colorScheme
    val (fill, edge) = when (box) {
        SelectAllBox.ALL -> colors.primary to colors.primary
        SelectAllBox.SOME -> colors.primaryContainer to colors.primary
        SelectAllBox.NONE -> Color.Transparent to colors.onSurfaceVariant
        SelectAllBox.DISABLED -> Color.Transparent to colors.onSurface.copy(alpha = 0.38f)
    }
    val shape = RoundedCornerShape(4.dp)
    Box(
        Modifier.size(22.dp).clip(shape).background(fill).border(2.dp, edge, shape),
        contentAlignment = Alignment.Center,
    ) {
        when (box) {
            SelectAllBox.ALL -> Icon(painterResource(R.drawable.ic_check), null, Modifier.size(18.dp), tint = colors.onPrimary)
            SelectAllBox.SOME -> Box(Modifier.size(width = 12.dp, height = 3.dp).clip(CircleShape).background(colors.primary))
            else -> Unit
        }
    }
}

/** The names, wrapping, in the current sort order, keyed by id. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NameChips(chips: List<ChipUi>, onToggle: (Long) -> Unit, onPutBack: (Long) -> Unit, modifier: Modifier = Modifier) {
    FlowRow(
        modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        chips.forEach { chip -> androidx.compose.runtime.key(chip.id) { NameChip(chip, onToggle, onPutBack) } }
    }
}

/**
 * One name (spec "Chip states"): In, Out or Drawn. In and Out toggle with a fast spatial spring on a
 * 0.96 press scale and a fast effects crossfade. Drawn is never colour alone: it has the ticket
 * icon, the strikethrough and its state description. Tapping a drawn chip opens a menu anchored to it
 * with "Put back in the hat", which is also a custom accessibility action (NAM-12).
 */
@Composable
internal fun NameChip(chip: ChipUi, onToggle: (Long) -> Unit, onPutBack: (Long) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val text = nameLabel(chip.text)
    val stateText = stringResource(
        when (chip.visual) {
            ChipVisual.IN -> R.string.chip_in
            ChipVisual.OUT -> R.string.chip_out
            ChipVisual.DRAWN -> R.string.chip_drawn
        },
    )
    val fill by animateColorAsState(
        when (chip.visual) {
            ChipVisual.IN -> colors.secondaryContainer
            ChipVisual.OUT -> Color.Transparent
            ChipVisual.DRAWN -> colors.surfaceContainerHigh
        },
        motion.fastEffectsSpec(),
    )
    val content = when (chip.visual) {
        ChipVisual.IN -> colors.onSecondaryContainer
        else -> colors.onSurfaceVariant
    }
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, motion.fastSpatialSpec())
    var menu by remember { mutableStateOf(false) }
    val putBackLabel = stringResource(R.string.chip_put_back)

    val toggle = when (chip.visual) {
        ChipVisual.DRAWN -> Modifier.clickable(
            interactionSource = interaction,
            indication = androidx.compose.material3.ripple(),
            role = Role.Button,
            onClick = { menu = true },
        )
        else -> Modifier.toggleable(
            value = chip.visual == ChipVisual.IN,
            interactionSource = interaction,
            indication = androidx.compose.material3.ripple(),
            role = Role.Checkbox,
            onValueChange = { onToggle(chip.id) },
        )
    }
    Box(Modifier.minimumInteractiveComponentSize(), contentAlignment = Alignment.Center) {
        Surface(
            shape = CircleShape,
            color = fill,
            contentColor = content,
            border = if (chip.visual == ChipVisual.OUT) BorderStroke(1.dp, colors.outline) else null,
            modifier = Modifier
                .scale(scale)
                .clip(CircleShape)
                .then(toggle)
                .semantics(mergeDescendants = true) {
                    stateDescription = stateText
                    if (chip.visual == ChipVisual.DRAWN) {
                        customActions = listOf(
                            CustomAccessibilityAction(putBackLabel) {
                                onPutBack(chip.id)
                                true
                            },
                        )
                    }
                },
        ) {
            Row(
                Modifier.heightIn(min = 40.dp).padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                when (chip.visual) {
                    ChipVisual.IN -> Icon(painterResource(R.drawable.ic_check), null, Modifier.size(16.dp))
                    ChipVisual.DRAWN -> Icon(painterResource(R.drawable.ic_ticket), null, Modifier.size(16.dp))
                    ChipVisual.OUT -> Unit
                }
                Text(
                    text,
                    style = MaterialTheme.typography.labelLarge.copy(
                        textDecoration = if (chip.visual == ChipVisual.DRAWN) TextDecoration.LineThrough else null,
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (chip.visual == ChipVisual.DRAWN) {
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(
                    text = { Text(putBackLabel) },
                    leadingIcon = { Icon(painterResource(R.drawable.ic_put_back), contentDescription = null) },
                    onClick = {
                        menu = false
                        onPutBack(chip.id)
                    },
                )
            }
        }
    }
}

/**
 * The draw bar (D6): the stepper and **Draw n**, or a message when nothing is in the hat. Docked at
 * the bottom ([vertical] false) or a trailing panel in compact height ([vertical] true).
 */
@Composable
fun DrawBar(
    draw: DrawState,
    count: Int,
    vertical: Boolean,
    onFewer: () -> Unit,
    onMore: () -> Unit,
    onDraw: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spring = MaterialTheme.motionScheme.defaultSpatialSpec<androidx.compose.ui.unit.IntSize>()
    val bar = draw.bar
    val drawButton: @Composable (Modifier) -> Unit = { m ->
        // Everyone's been drawn: Draw morphs into a filled Reset (spec "Draw bar", "Motion").
        val allDrawn = bar is DrawBarState.AllDrawn
        Button(
            onClick = if (allDrawn) onReset else onDraw,
            enabled = bar is DrawBarState.Stepper || allDrawn,
            shape = CircleShape,
            contentPadding = ButtonDefaults.ContentPadding,
            modifier = m.height(56.dp),
        ) {
            Text(
                when (bar) {
                    is DrawBarState.Stepper -> stringResource(R.string.draw_button, count)
                    DrawBarState.AllDrawn -> stringResource(R.string.draw_reset)
                    DrawBarState.NothingInHat -> stringResource(R.string.draw_button_plain)
                },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
    val message: @Composable (Modifier) -> Unit = { m ->
        val text = when (bar) {
            DrawBarState.AllDrawn -> R.string.draw_all_drawn
            else -> R.string.draw_nothing_in_hat
        }
        Text(
            stringResource(text),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            textAlign = if (vertical) TextAlign.Center else TextAlign.Start,
            modifier = m,
        )
    }
    if (vertical) {
        Column(
            modifier.width(200.dp).padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp).animateContentSize(spring),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Bottom),
        ) {
            if (bar is DrawBarState.Stepper) {
                Stepper(count, bar.max, vertical = true, onFewer, onMore)
            } else {
                message(Modifier.fillMaxWidth())
            }
            drawButton(Modifier.fillMaxWidth())
        }
    } else {
        Row(
            modifier.fillMaxWidth().padding(12.dp).padding(horizontal = 4.dp).animateContentSize(spring),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (bar is DrawBarState.Stepper) {
                Stepper(count, bar.max, vertical = false, onFewer, onMore)
                drawButton(Modifier.weight(1f))
            } else {
                message(Modifier.weight(1f))
                drawButton(Modifier)
            }
        }
    }
}

/**
 * The connected [− n +] stepper (48×56 dp segments; [+ n −] stacked in compact height, as in
 * `LandScreen.dc.html:116-124`). − is disabled at 1 and + at [max]; the value is announced politely.
 */
@Composable
private fun Stepper(count: Int, max: Int, vertical: Boolean, onFewer: () -> Unit, onMore: () -> Unit) {
    val group = stringResource(R.string.draw_count_group)
    val outer = 28.dp
    val inner = 6.dp
    val fewer: @Composable () -> Unit = {
        StepButton(
            R.drawable.ic_remove, R.string.draw_fewer, enabled = count > 1, onClick = onFewer,
            shape = if (vertical) RoundedCornerShape(inner, inner, outer, outer) else RoundedCornerShape(outer, inner, inner, outer),
            vertical = vertical,
        )
    }
    val more: @Composable () -> Unit = {
        StepButton(
            R.drawable.ic_add, R.string.draw_more_a11y, enabled = count < max, onClick = onMore,
            shape = if (vertical) RoundedCornerShape(outer, outer, inner, inner) else RoundedCornerShape(inner, outer, outer, inner),
            vertical = vertical,
        )
    }
    val value: @Composable () -> Unit = {
        Box(
            Modifier
                .then(if (vertical) Modifier.size(width = 72.dp, height = 44.dp) else Modifier.size(width = 44.dp, height = 56.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            // The live region is on the node whose text changes, so TalkBack reads each new value.
            Text(
                count.toString(),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
    }
    val groupModifier = Modifier.semantics { contentDescription = group }
    if (vertical) {
        Column(groupModifier, verticalArrangement = Arrangement.spacedBy(2.dp)) { more(); value(); fewer() }
    } else {
        Row(groupModifier, horizontalArrangement = Arrangement.spacedBy(2.dp)) { fewer(); value(); more() }
    }
}

@Composable
private fun StepButton(
    @DrawableRes icon: Int,
    @StringRes description: Int,
    enabled: Boolean,
    onClick: () -> Unit,
    shape: Shape,
    vertical: Boolean,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.38f),
        modifier = if (vertical) Modifier.size(width = 72.dp, height = 48.dp) else Modifier.size(width = 48.dp, height = 56.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(painterResource(icon), contentDescription = stringResource(description), modifier = Modifier.size(22.dp))
        }
    }
}

/** A list with no names (spec "States", NAM-9 AC6): the hat and **Add names** → Edit list. */
@Composable
fun DrawEmptyState(onAddNames: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        HatIllustration(size = 132.dp)
        Text(
            stringResource(R.string.draw_empty_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(R.string.draw_empty_body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onAddNames,
            shape = CircleShape,
            modifier = Modifier.padding(top = 12.dp).height(56.dp),
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        ) {
            Icon(painterResource(R.drawable.ic_add), contentDescription = null, modifier = Modifier.size(20.dp))
            Text(
                stringResource(R.string.draw_empty_action),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}
