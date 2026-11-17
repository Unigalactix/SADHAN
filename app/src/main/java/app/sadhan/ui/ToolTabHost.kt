package app.sadhan.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.invisibleToUser
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner

val LocalAppLifecycleOwner = staticCompositionLocalOf<LifecycleOwner?> { null }
val LocalToolVisible = staticCompositionLocalOf { true }

@Composable
fun ToolTabHost(active: Boolean, content: @Composable () -> Unit) {
    val appOwner = LocalLifecycleOwner.current
    val visible by rememberUpdatedState(active)
    val owner = remember {
        object : LifecycleOwner {
            val registry = LifecycleRegistry(this).apply { currentState = Lifecycle.State.CREATED }
            override val lifecycle: Lifecycle get() = registry
        }
    }
    fun update() {
        owner.registry.currentState = when {
            active && appOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) -> Lifecycle.State.RESUMED
            active && appOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) -> Lifecycle.State.STARTED
            else -> Lifecycle.State.CREATED
        }
    }
    SideEffect { update() }
    DisposableEffect(owner, appOwner) {
        val observer = LifecycleEventObserver { _, _ ->
            owner.registry.currentState = when {
                visible && appOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) -> Lifecycle.State.RESUMED
                visible && appOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) -> Lifecycle.State.STARTED
                else -> Lifecycle.State.CREATED
            }
        }
        appOwner.lifecycle.addObserver(observer)
        onDispose { appOwner.lifecycle.removeObserver(observer); owner.registry.currentState = Lifecycle.State.DESTROYED }
    }
    val opacity = remember { Animatable(0f) }
    LaunchedEffect(active) { if (active) opacity.animateTo(1f, tween(180)) else opacity.snapTo(0f) }
    val hidden = if (active) Modifier else Modifier.clearAndSetSemantics { invisibleToUser() }
    Box(Modifier.fillMaxSize().graphicsLayer {
        alpha = opacity.value
        scaleX = .985f + .015f * opacity.value
        scaleY = .985f + .015f * opacity.value
        translationX = if (active) 0f else size.width * 2
        clip = true
    }.then(hidden)) {
        CompositionLocalProvider(
            LocalLifecycleOwner provides owner,
            LocalAppLifecycleOwner provides appOwner,
            LocalToolVisible provides active,
            content = content,
        )
    }
}
