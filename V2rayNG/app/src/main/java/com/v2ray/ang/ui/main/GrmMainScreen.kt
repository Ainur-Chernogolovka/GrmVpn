package com.v2ray.ang.ui.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.v2ray.ang.R

/**
 * GRAMESTOS VPN main screen: one big power ring, the servers of the current
 * subscription and a sheet for adding a subscription.
 */
@Composable
fun GrmMainScreen(
    mainViewModel: MainViewModel,
    onAction: (MainAction) -> Unit,
) {
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val groupId = uiState.selectedGroupId
    val groupState by remember(groupId) { mainViewModel.serverGroupState(groupId) }
        .collectAsStateWithLifecycle()
    val rows = groupState.rows
    val connected = uiState.isRunning
    val hasServers = rows.isNotEmpty()
    val accent by animateColorAsState(
        targetValue = if (connected) GrmColors.Green else GrmColors.Pink,
        animationSpec = tween(durationMillis = 500),
        label = "screenAccent",
    )
    var showAdd by rememberSaveable { mutableStateOf(false) }
    val detail = mainViewModel.formatStatus(uiState.status)

    // Right after an import no delay is known yet: measure once so the pings show up.
    LaunchedEffect(groupId, rows.size) {
        if (rows.isNotEmpty() && rows.all { it.testDelayMillis == 0L }) {
            onAction(MainAction.TestAllServers)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(GrmColors.GradientTop, GrmColors.GradientMid, GrmColors.GradientBottom)
                )
            ),
    ) {
        GrmBackdrop(accent = accent, modifier = Modifier.align(Alignment.TopCenter))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            GrmHeader(
                showMenu = hasServers,
                onUpdate = { onAction(MainAction.UpdateSubscriptions) },
                onPing = { onAction(MainAction.TestRealAllServers) },
                onRemoveAll = { onAction(MainAction.RemoveAllServers) },
            )
            Text(
                text = stringResource(if (connected) R.string.grm_tagline_on else R.string.grm_tagline),
                color = if (connected) GrmColors.Green else GrmColors.Cyan.copy(alpha = 0.85f),
                fontFamily = GrmFonts.Mono,
                fontSize = 11.sp,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 4.dp),
            )

            PowerButton(
                connected = connected,
                accent = accent,
                stateText = stringResource(if (connected) R.string.grm_status_on else R.string.grm_status_off),
                onClick = { onAction(MainAction.ToggleService) },
            )

            StatusBlock(
                connected = connected,
                accent = accent,
                detail = detail,
                onDetailClick = { onAction(MainAction.TestCurrentServer) },
            )

            if (hasServers) {
                Spacer(Modifier.height(20.dp))
                val protocol = rows.firstOrNull { it.guid == uiState.selectedGuid }?.typeDescription
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    StatTile(
                        label = stringResource(R.string.grm_ip_label),
                        value = stringResource(if (connected) R.string.grm_ip_hidden else R.string.grm_ip_visible),
                        valueColor = if (connected) GrmColors.Green else GrmColors.PinkText,
                        modifier = Modifier.weight(1f),
                    )
                    StatTile(
                        label = stringResource(R.string.grm_state_label),
                        value = stringResource(if (connected) R.string.grm_state_on else R.string.grm_state_off),
                        valueColor = GrmColors.TextPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    StatTile(
                        label = stringResource(R.string.grm_protocol_label),
                        value = protocol ?: stringResource(R.string.grm_ping_unknown),
                        valueColor = GrmColors.TextPrimary,
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(Modifier.height(20.dp))
                ServersHeader(
                    title = uiState.groups.firstOrNull { it.id == groupId }?.remarks.orEmpty(),
                    count = rows.size,
                    onUpdate = { onAction(MainAction.UpdateSubscriptions) },
                )
                if (uiState.groups.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        uiState.groups.forEach { group ->
                            key(group.id) {
                                GroupChip(
                                    text = group.remarks,
                                    selected = group.id == groupId,
                                    onClick = { onAction(MainAction.SelectGroup(group.id)) },
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    rows.forEach { row ->
                        key(row.guid) {
                            ServerRowItem(
                                row = row,
                                selected = row.guid == uiState.selectedGuid,
                                onClick = { onAction(MainAction.SelectServer(row.guid)) },
                            )
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
                GrmOutlinedButton(
                    text = stringResource(R.string.grm_add_subscription),
                    iconRes = R.drawable.ic_grm_link,
                    onClick = { showAdd = true },
                )
            } else {
                Spacer(Modifier.height(20.dp))
                Text(
                    text = stringResource(R.string.grm_empty_title),
                    color = GrmColors.TextPrimary,
                    fontFamily = GrmFonts.Body,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = stringResource(R.string.grm_empty_text),
                    color = GrmColors.TextSecondary,
                    fontFamily = GrmFonts.Body,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                )
                Spacer(Modifier.height(20.dp))
                GrmPrimaryButton(
                    text = stringResource(R.string.grm_add_subscription),
                    iconRes = R.drawable.ic_grm_link,
                    onClick = { showAdd = true },
                )
                Text(
                    text = stringResource(R.string.grm_add_hint),
                    color = GrmColors.TextSecondary,
                    fontFamily = GrmFonts.Mono,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                )
            }
            Spacer(
                Modifier
                    .navigationBarsPadding()
                    .height(28.dp)
            )
        }

        if (showAdd) {
            AddSubscriptionSheet(
                onDismiss = { showAdd = false },
                onAction = onAction,
            )
        }
    }
}

@Composable
private fun GrmHeader(
    showMenu: Boolean,
    onUpdate: () -> Unit,
    onPing: () -> Unit,
    onRemoveAll: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_grm_foreground),
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .offset(x = (-12).dp),
            )
            Text(
                text = buildAnnotatedString {
                    append(stringResource(R.string.grm_title_prefix))
                    withStyle(SpanStyle(color = GrmColors.Green)) {
                        append(stringResource(R.string.grm_title_suffix))
                    }
                },
                color = Color.White,
                fontFamily = GrmFonts.Display,
                fontSize = 21.sp,
                letterSpacing = 1.sp,
                modifier = Modifier.offset(x = (-14).dp),
            )
        }
        if (showMenu) {
            Box {
                val menuLabel = stringResource(R.string.grm_menu)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(GrmColors.Card)
                        .border(1.dp, GrmColors.CardBorder, RoundedCornerShape(14.dp))
                        .clickable(role = Role.Button, onClickLabel = menuLabel) { menuOpen = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_grm_menu),
                        contentDescription = menuLabel,
                        tint = GrmColors.TextPrimary,
                    )
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.grm_update_subscription)) },
                        onClick = { menuOpen = false; onUpdate() },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.grm_test_ping)) },
                        onClick = { menuOpen = false; onPing() },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.grm_remove_all)) },
                        onClick = { menuOpen = false; onRemoveAll() },
                    )
                }
            }
        } else {
            Spacer(Modifier.size(44.dp))
        }
    }
}

@Composable
private fun PowerButton(connected: Boolean, accent: Color, stateText: String, onClick: () -> Unit) {
    val description = stringResource(if (connected) R.string.grm_power_off_desc else R.string.grm_power_on_desc)
    Box(
        modifier = Modifier
            .padding(top = 6.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(320.dp)
                .drawBehind {
                    val radius = size.width * 0.72f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(accent.copy(alpha = 0.38f), Color.Transparent),
                            center = center,
                            radius = radius,
                        ),
                        radius = radius,
                        center = center,
                    )
                }
                .semantics {
                    contentDescription = description
                    stateDescription = stateText
                }
                .clickable(role = Role.Button, onClickLabel = description, onClick = onClick),
        ) {
            GrmPowerRing(connected = connected, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun StatusBlock(
    connected: Boolean,
    accent: Color,
    detail: String,
    onDetailClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(if (connected) R.string.grm_status_on else R.string.grm_status_off),
            color = if (connected) GrmColors.Green else GrmColors.PinkText,
            fontFamily = GrmFonts.Display,
            fontSize = 22.sp,
            letterSpacing = 3.sp,
        )
        Text(
            text = detail,
            color = GrmColors.TextSecondary,
            fontFamily = GrmFonts.Mono,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 8.dp)
                .clickable(onClick = onDetailClick)
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(GrmColors.Card)
            .border(1.dp, GrmColors.CardBorder, RoundedCornerShape(16.dp))
            .padding(12.dp),
    ) {
        Text(
            text = label,
            color = GrmColors.TextSecondary,
            fontFamily = GrmFonts.Mono,
            fontSize = 10.sp,
            letterSpacing = 1.6.sp,
            maxLines = 1,
        )
        Text(
            text = value,
            color = valueColor,
            fontFamily = GrmFonts.Body,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Composable
private fun ServersHeader(title: String, count: Int, onUpdate: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (title.isBlank()) stringResource(R.string.grm_servers) else title,
                color = GrmColors.TextPrimary,
                fontFamily = GrmFonts.Display,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(R.string.grm_servers_count, count),
                color = GrmColors.TextSecondary,
                fontFamily = GrmFonts.Mono,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        val label = stringResource(R.string.grm_update_subscription)
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable(role = Role.Button, onClickLabel = label, onClick = onUpdate),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_grm_refresh),
                contentDescription = label,
                tint = GrmColors.Cyan,
            )
        }
    }
}

@Composable
private fun GroupChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = text,
        color = if (selected) GrmColors.Ink else GrmColors.TextPrimary,
        fontFamily = GrmFonts.Body,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) GrmColors.Green else GrmColors.Card)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    )
}

@Composable
private fun ServerRowItem(row: ServerRowUiModel, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    val ping = row.testDelayMillis
    val pingText = when {
        ping > 0 -> stringResource(R.string.grm_ping_ms, ping)
        ping < 0 -> stringResource(R.string.grm_ping_failed)
        else -> stringResource(R.string.grm_ping_unknown)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) GrmColors.Green.copy(alpha = 0.12f) else GrmColors.Card)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) GrmColors.Green else GrmColors.CardBorder,
                shape = shape,
            )
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = row.remarks,
                color = GrmColors.TextPrimary,
                fontFamily = GrmFonts.Body,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = row.typeDescription,
                color = GrmColors.TextSecondary,
                fontFamily = GrmFonts.Mono,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
        Text(
            text = pingText,
            color = when {
                ping > 0 -> GrmColors.Green
                ping < 0 -> GrmColors.PinkText
                else -> GrmColors.TextSecondary
            },
            fontFamily = GrmFonts.Mono,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 10.dp),
        )
    }
}

@Composable
private fun GrmPrimaryButton(text: String, iconRes: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(32.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(
                elevation = 14.dp,
                shape = shape,
                ambientColor = GrmColors.Cyan,
                spotColor = GrmColors.Cyan,
            )
            .clip(shape)
            .background(Brush.horizontalGradient(listOf(Color(0xFF1FE4A6), Color(0xFF2BC7FF))))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 12.dp, end = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(21.dp))
                .background(Color(0xFF07173F)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = GrmColors.Green,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = text,
            color = GrmColors.Ink,
            fontFamily = GrmFonts.Display,
            fontSize = 17.sp,
            letterSpacing = 1.4.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun GrmOutlinedButton(text: String, iconRes: Int, onClick: () -> Unit) {
    val shape = RoundedCornerShape(28.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(shape)
            .background(GrmColors.Card)
            .border(1.5.dp, GrmColors.Green.copy(alpha = 0.8f), shape)
            .clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = GrmColors.Green,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = text,
            color = GrmColors.Green,
            fontFamily = GrmFonts.Display,
            fontSize = 15.sp,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 10.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSubscriptionSheet(
    onDismiss: () -> Unit,
    onAction: (MainAction) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var link by rememberSaveable { mutableStateOf("") }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = GrmColors.Sheet,
        contentColor = GrmColors.TextPrimary,
        dragHandle = { BottomSheetDefaults.DragHandle(color = GrmColors.TextSecondary) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        ) {
            Text(
                text = stringResource(R.string.grm_sheet_title),
                color = Color.White,
                fontFamily = GrmFonts.Display,
                fontSize = 22.sp,
            )
            Text(
                text = stringResource(R.string.grm_sheet_sub),
                color = GrmColors.Cyan.copy(alpha = 0.9f),
                fontFamily = GrmFonts.Mono,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = stringResource(R.string.grm_link_label),
                color = GrmColors.TextSecondary,
                fontFamily = GrmFonts.Mono,
                fontSize = 10.sp,
                letterSpacing = 1.8.sp,
                modifier = Modifier.padding(top = 20.dp),
            )
            OutlinedTextField(
                value = link,
                onValueChange = { link = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                placeholder = {
                    Text(
                        text = stringResource(R.string.grm_link_placeholder),
                        fontFamily = GrmFonts.Mono,
                        fontSize = 13.sp,
                    )
                },
                textStyle = TextStyle(
                    fontFamily = GrmFonts.Mono,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                ),
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = GrmColors.Green,
                    unfocusedTextColor = GrmColors.Green,
                    focusedBorderColor = GrmColors.Cyan,
                    unfocusedBorderColor = GrmColors.Cyan.copy(alpha = 0.6f),
                    cursorColor = GrmColors.Green,
                    focusedContainerColor = GrmColors.Field,
                    unfocusedContainerColor = GrmColors.Field,
                    focusedPlaceholderColor = GrmColors.TextSecondary,
                    unfocusedPlaceholderColor = GrmColors.TextSecondary,
                ),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SheetSecondaryButton(
                    text = stringResource(R.string.grm_from_clipboard),
                    iconRes = R.drawable.ic_grm_clipboard,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onDismiss()
                        onAction(MainAction.ImportClipboard)
                    },
                )
                SheetSecondaryButton(
                    text = stringResource(R.string.grm_scan_qr),
                    iconRes = R.drawable.ic_grm_qr,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onDismiss()
                        onAction(MainAction.ImportQRcode)
                    },
                )
            }
            GrmPrimaryButton(
                text = stringResource(R.string.grm_add),
                iconRes = R.drawable.ic_grm_link,
                modifier = Modifier.padding(top = 16.dp),
                onClick = {
                    val text = link.trim()
                    if (text.isNotEmpty()) {
                        onDismiss()
                        onAction(MainAction.ImportBatchConfig(text))
                    }
                },
            )
            Text(
                text = stringResource(R.string.grm_sheet_note),
                color = GrmColors.TextSecondary,
                fontFamily = GrmFonts.Mono,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
            )
        }
    }
}

@Composable
private fun SheetSecondaryButton(
    text: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .height(52.dp)
            .clip(shape)
            .background(GrmColors.Card)
            .border(1.dp, GrmColors.CardBorder, shape)
            .clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = GrmColors.Cyan,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = text,
            color = GrmColors.TextPrimary,
            fontFamily = GrmFonts.Body,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}
