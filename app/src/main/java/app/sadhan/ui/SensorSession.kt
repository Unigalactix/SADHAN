package app.sadhan.ui

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun SensorSession(manager: SensorManager, sensors: List<Sensor>, onError: (String) -> Unit, onReading: (SensorEvent) -> Unit) {
    val owner = LocalLifecycleOwner.current
    val reading by rememberUpdatedState(onReading)
    val error by rememberUpdatedState(onError)
    DisposableEffect(manager, sensors, owner) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) = reading(event)
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        fun register() {
            sensors.forEach {
                if (!manager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)) error("Cannot start ${it.name}")
            }
        }
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> register()
                Lifecycle.Event.ON_STOP -> manager.unregisterListener(listener)
                else -> Unit
            }
        }
        owner.lifecycle.addObserver(observer)
        if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) register()
        onDispose { owner.lifecycle.removeObserver(observer); manager.unregisterListener(listener) }
    }
}
