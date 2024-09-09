package dev.supergooey.caloriesnap

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.supergooey.caloriesnap.ui.theme.CalorieSnapTheme
import java.util.UUID


val TestMessages = listOf(
  Message(
    role = "system",
    content = listOf(
      MessageContent.Text(
        text = "Hello Human"
      )
    )
  ),
  Message(
    role = "user",
    content = listOf(
      MessageContent.Text(
        text = "Hello Pical"
      )
    )
  ),
  Message(
    role = "system",
    content = listOf(
      MessageContent.Text(
        text = "Hello Human"
      )
    )
  ),
  Message(
    role = "user",
    content = listOf(
      MessageContent.Text(
        text = "Hello Pical"
      )
    )
  ),
  Message(
    role = "system",
    content = listOf(
      MessageContent.Text(
        text = "Hello Human"
      )
    )
  ),
  Message(
    role = "user",
    content = listOf(
      MessageContent.Text(
        text = "Hello Pical"
      )
    )
  ),
  Message(
    role = "system",
    content = listOf(
      MessageContent.Text(
        text = "Hello Human"
      )
    )
  ),
  Message(
    role = "user",
    content = listOf(
      MessageContent.Text(
        text = "Hello Pical"
      )
    )
  ),
  Message(
    role = "system",
    content = listOf(
      MessageContent.Text(
        text = "Hello Human"
      )
    )
  ),
  Message(
    role = "user",
    content = listOf(
      MessageContent.Text(
        text = "Hello Pical"
      )
    )
  ),
  Message(
    role = "system",
    content = listOf(
      MessageContent.Text(
        text = "Hello Human"
      )
    )
  ),
  Message(
    role = "user",
    content = listOf(
      MessageContent.Text(
        text = "Hello Pical"
      )
    )
  ),
  Message(
    role = "system",
    content = listOf(
      MessageContent.Text(
        text = "Hello Human"
      )
    )
  ),
  Message(
    role = "user",
    content = listOf(
      MessageContent.Text(
        text = "Hello Pical"
      )
    )
  ),
  Message(
    role = "system",
    content = listOf(
      MessageContent.Text(
        text = "Hello Human"
      )
    )
  ),
  Message(
    role = "user",
    content = listOf(
      MessageContent.Text(
        text = "Hello Pical"
      )
    )
  ),
)

@Preview
@Composable
fun MessageComposerDemo() {
  val messages = remember { TestMessages.toMutableStateList() }
  var contextMessage by remember { mutableStateOf("") }
  var isSending by remember { mutableStateOf(false) }
  var id = remember { UUID.randomUUID().toString() }

  CalorieSnapTheme {
    SharedTransitionLayout {
      AnimatedContent(
        targetState = isSending,
        label = "isSending",
        transitionSpec =  {
            (fadeIn(animationSpec = tween(220, delayMillis = 0)))
              .togetherWith(fadeOut(animationSpec = tween(90)))
        },
      ) { state ->
        Scaffold(
          modifier = Modifier.fillMaxSize(),
          topBar = {
            TopAppBar(
              modifier = Modifier
                .renderInSharedTransitionScopeOverlay(zIndexInOverlay = 10f),
              title = { Text("Jane Doe") },
              // We cannot use Color.Transparent, since the TopAppBar is visible during
              // the transition, which causes the messages to be visible during the transition
              // So we use the same color as the Scaffold's containerColor.
              colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
              ),
              navigationIcon = {
                FilledIconButton(
                  onClick = { /* no-op */ },
                  colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.background
                  )
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = null
                  )
                }
              }
            )
          },
          bottomBar = {
            ComposerContainer(
              sharedTransitionScope = this@SharedTransitionLayout,
              animatedVisibilityScope = this@AnimatedContent,
              messageId = id,
              value = contextMessage,
              onValueChanged = { contextMessage = it},
              onSend = {
                isSending = true
                messages.add(
                  index = 0,
                  Message(
                    id = id,
                    role = "user",
                    content = listOf(
                      MessageContent.Text(
                        text = contextMessage
                      )
                    )
                  )
                )
              }
            )
          }
        ) { paddingValues ->
          val listState = rememberLazyListState()

          LaunchedEffect(messages.size) {
            isSending = false
            id = UUID.randomUUID().toString()
            contextMessage = ""
            listState.scrollToItem(0)
          }
          Surface(
            modifier = Modifier.padding(paddingValues),
          ) {
            Column(
              modifier = Modifier.fillMaxSize(),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              LazyColumn(
                modifier = Modifier
                  .fillMaxWidth()
                  .weight(1f),
                state = listState,
                reverseLayout = true
              ) {
                item {
                  Spacer(Modifier.height(8.dp))
                }
                itemsIndexed(items = messages) { index, message ->
                  val isUser = message.role == "user"
                  val alignment = if (isUser) Alignment.TopEnd else Alignment.TopStart
                  val body = (message.content[0] as MessageContent.Text).text
                  Box(modifier = Modifier.fillParentMaxWidth(), contentAlignment = alignment) {
                    // Message Bubble
                    Surface(
                      modifier = Modifier
                        .widthIn(max = 280.dp)
                        .sharedBounds(
                          sharedContentState = rememberSharedContentState(message.id),
                          animatedVisibilityScope = this@AnimatedContent,
                          boundsTransform = { _, _ -> spring(stiffness = Spring.StiffnessLow) },
                          resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                          zIndexInOverlay = if (index == 0) 1f else 0f
                        ),
                      color = messageBubbleColor(isUser),
                      shape = RoundedCornerShape(
                        topStartPercent = 50,
                        topEndPercent = 50,
                        bottomStartPercent = if (isUser) 50 else 10,
                        bottomEndPercent = if (isUser) 10 else 50
                      )
                    ) {
                      Text(
                        modifier = Modifier
                          .padding(vertical = 8.dp, horizontal = 16.dp)
                          .skipToLookaheadSize(),
                        text = body,
                        color = messageTextColor(isUser)
                      )
                    }
                  }
                }
                item {
                  Image(
                    modifier = Modifier.size(200.dp).clip(RoundedCornerShape(16.dp)),
                    painter = painterResource(R.drawable.bibimbap),
                    contentScale = ContentScale.Crop,
                    contentDescription = ""
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun ComposerContainer(
  modifier: Modifier = Modifier,
  sharedTransitionScope: SharedTransitionScope,
  animatedVisibilityScope: AnimatedVisibilityScope,
  messageId: String,
  value: String,
  onValueChanged: (String) -> Unit,
  onSend: () -> Unit,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = MaterialTheme.colorScheme.surfaceContainer,
    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
  ) {
    Row(
      modifier = Modifier
        .windowInsetsPadding(WindowInsets.navigationBars)
        .imePadding()
        .height(88.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(modifier = Modifier.padding(horizontal = 16.dp)) {
        with(sharedTransitionScope) {
          Composer(
            modifier = Modifier.sharedBounds(
              placeHolderSize = SharedTransitionScope.PlaceHolderSize.animatedSize,
              sharedContentState = rememberSharedContentState(messageId),
              animatedVisibilityScope = animatedVisibilityScope
            ),
            value = value,
            onValueChange = onValueChanged,
            onSend = onSend
          )
        }
      }
    }
  }
}

@Composable
fun messageBubbleColor(isUser: Boolean): Color {
  return if (isUser) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceContainerHighest
}

@Composable
fun messageTextColor(isUser: Boolean): Color {
  return if (isUser) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
}
