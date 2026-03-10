package io.github.androidpoet.nebula.protocol.engine

import kotlin.math.*

/**
 * Damped spring physics engine for smooth value transitions.
 *
 * Models a critically-damped or under-damped spring system matching
 * the AndroidX Compose Remote spring animation behavior.
 */
public class SpringStopEngine(
  private val stiffness: Float = 200f,
  private val damping: Float = 20f,
  private val mass: Float = 1f,
  private val threshold: Float = 0.001f,
) {

  private var position: Float = 0f
  private var velocity: Float = 0f
  private var target: Float = 0f

  public var currentValue: Float = 0f
    private set

  public var isFinished: Boolean = true
    private set

  public fun setTarget(newTarget: Float) {
    target = newTarget
    isFinished = false
  }

  public fun update(deltaSec: Float) {
    if (isFinished) return

    val dt = deltaSec.coerceAtMost(1f / 30f)
    val displacement = position - target
    val springForce = -stiffness * displacement
    val dampingForce = -damping * velocity
    val acceleration = (springForce + dampingForce) / mass

    velocity += acceleration * dt
    position += velocity * dt

    currentValue = position

    if (abs(velocity) < threshold && abs(position - target) < threshold) {
      position = target
      velocity = 0f
      currentValue = target
      isFinished = true
    }
  }
}
