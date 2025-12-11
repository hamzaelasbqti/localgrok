package com.localgrok.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localgrok.R
import com.localgrok.data.local.entity.ChatEntity
import com.localgrok.ui.components.EmptyChatPlaceholder
import com.localgrok.ui.components.GrokTopBar
import com.localgrok.ui.components.chat.AssistantMessageBubble
import com.localgrok.ui.components.chat.DEFAULT_MODEL
import com.localgrok.ui.components.chat.UnifiedInputBar
import com.localgrok.ui.components.chat.UserMessageBubble
import com.localgrok.ui.theme.InterFont
import com.localgrok.ui.theme.LocalAppColors
import com.localgrok.ui.viewmodel.ChatViewModel
import com.localgrok.ui.viewmodel.ConnectionStatus
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val chats by viewModel.chats.collectAsState()
    val currentChat by viewModel.currentChat.collectAsState()
    val streamingState by viewModel.streamingState.collectAsState()
    val reasoningContent by viewModel.reasoningContent.collectAsState()
    val brainToggleEnabled by viewModel.brainToggleEnabled.collectAsState()

    // Theme-aware colors
    val colors = LocalAppColors.current

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // State for delete confirmation dialog
    var chatToDelete by remember { mutableStateOf<ChatEntity?>(null) }

    // State for delete all confirmation dialog
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    // State for selected model
    var selectedModel by remember { mutableStateOf(DEFAULT_MODEL) }

    // Debounce state to prevent rapid button clicks from causing issues
    var isNavigating by remember { mutableStateOf(false) }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Ensure drawer is closed when screen is shown (e.g., returning from settings)
    LaunchedEffect(Unit) {
        if (drawerState.isOpen) {
            drawerState.close()
        }
    }

    // Reset navigation lock after drawer animations complete
    LaunchedEffect(drawerState.currentValue, drawerState.targetValue) {
        // Only reset when drawer animation is complete (current == target)
        if (drawerState.currentValue == drawerState.targetValue) {
            isNavigating = false
        }
    }

    // Collapse keyboard when drawer starts opening (handles both gesture and programmatic open)
    LaunchedEffect(drawerState.targetValue) {
        if (drawerState.targetValue == DrawerValue.Open) {
            focusManager.clearFocus()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true, // Enable swipe-to-open and swipe-to-close gestures
        scrimColor = colors.background.copy(alpha = 0.7f),
        drawerContent = {
            GrokDrawerContent(
                colors = colors,
                chats = chats,
                currentChatId = currentChat?.id,
                onNewConversation = {
                    if (!isNavigating && !drawerState.isAnimationRunning) {
                        isNavigating = true
                        viewModel.createNewChat()
                        scope.launch {
                            drawerState.close()
                            // isNavigating will be reset by LaunchedEffect when animation completes
                        }
                    }
                },
                onSelectChat = { chatId ->
                    if (!isNavigating && !drawerState.isAnimationRunning) {
                        isNavigating = true
                        viewModel.selectChat(chatId)
                        scope.launch {
                            drawerState.close()
                            // isNavigating will be reset by LaunchedEffect when animation completes
                        }
                    }
                },
                onDeleteChat = { chat ->
                    if (!isNavigating && !drawerState.isAnimationRunning) {
                        chatToDelete = chat
                    }
                },
                onDeleteAllChats = {
                    if (!isNavigating && !drawerState.isAnimationRunning) {
                        showDeleteAllDialog = true
                    }
                },
                onSettingsClick = {
                    if (!isNavigating && !drawerState.isAnimationRunning) {
                        isNavigating = true
                        // Navigate immediately for instant transition - drawer closes in parallel
                        onNavigateToSettings()
                        // Close drawer asynchronously (will complete even after navigation)
                        scope.launch {
                            drawerState.close()
                        }
                    }
                }
            )
        }
    ) {
        // Main Chat Content - Column for top bar + overlay Box for content and input
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(colors.background)
                .statusBarsPadding()
        ) {
            // Top App Bar - Grok Style (stays fixed at top)
            GrokTopBar(
                colors = colors,
                onMenuClick = {
                    if (!isNavigating && !drawerState.isAnimationRunning) {
                        isNavigating = true
                        focusManager.clearFocus() // Collapse keyboard when opening sidebar
                        scope.launch {
                            drawerState.open()
                            // isNavigating will be reset by LaunchedEffect when animation completes
                        }
                    }
                },
                onNewConversationClick = {
                    // Only create new chat if current chat has messages
                    if (messages.isNotEmpty() && !isNavigating && !drawerState.isAnimationRunning) {
                        viewModel.createNewChat()
                    }
                }
            )

            // Overlay Box - Input bar floats over content with transparent corners
            val density = LocalDensity.current
            // Input bar height estimate: compact design matching Grok
            // Text field (24dp min) + row 2 (36dp) + internal padding (20dp) + outer padding (18dp) = ~98dp
            val inputBarHeight = 100.dp
            val imeBottomPadding = WindowInsets.ime.getBottom(density)
            val imePaddingDp = with(density) { imeBottomPadding.toDp() }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(colors.background)
            ) {
                // Child 1: Chat History (fills screen, with bottom padding for input bar)
                if (messages.isEmpty()) {
                    EmptyChatPlaceholder(
                        selectedModel = selectedModel,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            // Bottom padding: input bar height + IME padding + nav bar + safe area buffer
                            bottom = inputBarHeight + imePaddingDp + 56.dp
                        ),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = messages,
                            key = { it.id }
                        ) { message ->
                            when (message.role) {
                                "user" -> UserMessageBubble(
                                    message = message,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )

                                "assistant" -> {
                                    // Use streaming state for real-time updates on active message
                                    val isStreamingMessage = streamingState?.messageId == message.id
                                    val displayMessage =
                                        if (isStreamingMessage && streamingState != null) {
                                            // Override with streaming state for immediate updates
                                            message.copy(
                                                content = streamingState!!.content,
                                                isStreaming = streamingState!!.isStreaming,
                                                isThinking = streamingState!!.isThinking,
                                                toolUsed = streamingState!!.toolUsed,
                                                toolDisplayName = streamingState!!.toolDisplayName
                                                    ?: message.toolDisplayName
                                            )
                                        } else {
                                            message
                                        }

                                    // Check if currently executing a tool
                                    val isExecutingTool = isStreamingMessage &&
                                            streamingState?.isExecutingTool == true
                                    val toolDisplayName = if (isExecutingTool) {
                                        streamingState?.toolDisplayName
                                    } else null

                                    AssistantMessageBubble(
                                        message = displayMessage,
                                        reasoningContent = reasoningContent[message.id] ?: "",
                                        isReasoningEnabled = brainToggleEnabled,
                                        isExecutingTool = isExecutingTool,
                                        toolDisplayName = toolDisplayName,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Error message (positioned above the input bar)
                uiState.error?.let { error ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = inputBarHeight + 16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.darkGrey)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Error: $error",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.error
                        )
                    }
                }

                // Child 2: Floating Input Bar (aligned to bottom, with transparent outer container)
                UnifiedInputBar(
                    value = inputText,
                    onValueChange = { inputText = it },
                    onSend = {
                        if (uiState.connectionStatus != ConnectionStatus.Connected) {
                            viewModel.sendSetupReminder()
                            inputText = ""
                        } else if (inputText.isNotBlank()) {
                            viewModel.sendMessage(
                                content = inputText,
                                model = selectedModel.modelId
                            )
                            inputText = ""
                        }
                    },
                    onStop = { viewModel.stopGeneration() },
                    selectedModel = selectedModel,
                    onModelSelected = { model ->
                        selectedModel = model
                        viewModel.setModel(model.modelId)
                    },
                    brainToggleEnabled = brainToggleEnabled,
                    onBrainToggleChanged = { viewModel.toggleBrain() },
                    isGenerating = uiState.isGenerating,
                    enabled = !uiState.isGenerating,
                    allowEmptySend = uiState.connectionStatus != ConnectionStatus.Connected,
                    colors = colors,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .imePadding()
                        .navigationBarsPadding()
                )
            }
        }
    }

    // Delete confirmation dialog
    chatToDelete?.let { chat ->
        AlertDialog(
            onDismissRequest = { chatToDelete = null },
            containerColor = colors.darkGrey,
            title = {
                Text(
                    text = "Delete Chat",
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${chat.title}\"? This action cannot be undone.",
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteChat(chat.id)
                        chatToDelete = null
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = colors.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { chatToDelete = null }
                ) {
                    Text(
                        text = "Cancel",
                        color = colors.textPrimary
                    )
                }
            }
        )
    }

    // Delete all confirmation dialog
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            containerColor = colors.darkGrey,
            title = {
                Text(
                    text = "Delete all chats",
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete all chats? This action cannot be undone.",
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAllChats()
                        showDeleteAllDialog = false
                    }
                ) {
                    Text(
                        text = "Delete All",
                        color = colors.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteAllDialog = false }
                ) {
                    Text(
                        text = "Cancel",
                        color = colors.textPrimary
                    )
                }
            }
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// GROK-STYLE NAVIGATION DRAWER
// ═══════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GrokDrawerContent(
    colors: com.localgrok.ui.theme.LocalGrokColors,
    chats: List<ChatEntity>,
    currentChatId: Long?,
    onNewConversation: () -> Unit,
    onSelectChat: (Long) -> Unit,
    onDeleteChat: (ChatEntity) -> Unit,
    onDeleteAllChats: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    ModalDrawerSheet(
        drawerContainerColor = colors.background,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            // Search Bar Header
            SearchHistoryBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                colors = colors,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // New Chat Action
            DrawerActionItem(
                icon = Icons.Outlined.Edit,
                label = "New chat",
                onClick = onNewConversation,
                colors = colors
            )

            // Delete All Chats Action
            DrawerActionItem(
                icon = Icons.Outlined.Delete,
                label = "Delete all chats",
                onClick = onDeleteAllChats,
                colors = colors
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                color = colors.borderGrey,
                thickness = 0.5.dp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // CHATS Header
            Text(
                text = "CHATS",
                fontFamily = InterFont,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = colors.textSubtle,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Chat List
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                val filteredChats = if (searchQuery.isBlank()) {
                    chats
                } else {
                    chats.filter { it.title.contains(searchQuery, ignoreCase = true) }
                }

                items(
                    items = filteredChats,
                    key = { it.id }
                ) { chat ->
                    ConversationItem(
                        chat = chat,
                        isSelected = chat.id == currentChatId,
                        onClick = { onSelectChat(chat.id) },
                        onLongClick = { onDeleteChat(chat) },
                        colors = colors
                    )
                }
            }

            // Sticky Footer
            DrawerFooter(
                onSettingsClick = onSettingsClick,
                colors = colors
            )
        }
    }
}

@Composable
private fun SearchHistoryBar(
    query: String,
    onQueryChange: (String) -> Unit,
    colors: com.localgrok.ui.theme.LocalGrokColors,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.darkGrey)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = colors.textSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = colors.textPrimary,
                fontFamily = InterFont,
                fontSize = 15.sp
            ),
            cursorBrush = SolidColor(colors.textPrimary),
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                Box {
                    if (query.isEmpty()) {
                        Text(
                            text = "Search history",
                            fontFamily = InterFont,
                            fontSize = 15.sp,
                            color = colors.textSubtle
                        )
                    }
                    innerTextField()
                }
            }
        )
        // Chevron icon
        Text(
            text = "»",
            fontSize = 20.sp,
            color = colors.textSecondary
        )
    }
}

@Composable
private fun DrawerActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    colors: com.localgrok.ui.theme.LocalGrokColors,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.textPrimary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontFamily = InterFont,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            color = colors.textPrimary
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ConversationItem(
    chat: ChatEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    colors: com.localgrok.ui.theme.LocalGrokColors,
    modifier: Modifier = Modifier
) {
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val timeString = remember(chat.updatedAt) { timeFormat.format(chat.updatedAt) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 1.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) colors.darkGrey else Color.Transparent)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = chat.title,
                fontFamily = InterFont,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = timeString,
                fontFamily = InterFont,
                fontSize = 12.sp,
                color = colors.textSubtle
            )
        }

        // More options
        IconButton(
            onClick = { onLongClick() },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options",
                tint = colors.textSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun DrawerFooter(
    onSettingsClick: () -> Unit,
    colors: com.localgrok.ui.theme.LocalGrokColors,
    modifier: Modifier = Modifier
) {
    HorizontalDivider(
        color = colors.borderGrey,
        thickness = 0.5.dp
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Logo
        Image(
            painter = painterResource(id = R.drawable.localgrok_transparentlogo),
            contentDescription = "localgrok logo",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // App name and version
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "localgrok",
                fontFamily = InterFont,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = colors.textPrimary
            )
            Text(
                text = "dev build",
                fontFamily = InterFont,
                fontSize = 13.sp,
                color = colors.textSubtle
            )
        }

        // Settings gear
        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = colors.textSecondary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
