/*
 * Copyright (c) 2025. Lyzev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.lyzev.hp.client.util

import net.minecraft.entity.LivingEntity
import kotlin.math.pow

/**
 * Constants for unit conversion.
 */
private const val UNIT_TO_BPS = 43.17

/**
 * Constants for jump height calculation.
 */
private const val FACTOR = 0.98 * 0.98

/**
 * Converts a unit to blocks per second.
 * See the following link for more information:
 * https://minecraft.wiki/w/Horse#Movement_speed
 */
fun Double.toBPS(): Double {
    return this * UNIT_TO_BPS
}

/**
 * Converts a unit to a jump height.
 *
 * The internal unit represents the initial vertical velocity when the horse jump is perfectly timed.
 * Then we can apply gravity to calculate the resulting jump height.
 * The gravity formula is based on the calculations found at:
 * https://www.reddit.com/r/GameTheorists/comments/dj8odm/i_calculated_minecrafts_true_gravity/
 *
 * @see net.minecraft.entity.passive.AbstractHorseEntity.jump
 *
 * @param unit The internal unit to convert to jump height.
 */
fun Double.toJump(): Double {
    var velocity = this
    var jumpHeight = 0.0
    while (velocity > 0) {
        jumpHeight += velocity
        velocity = (velocity - LivingEntity.GRAVITY) * FACTOR
    }
    return jumpHeight
}

fun Double.round(decimals: Int): Double {
    require(decimals >= 0)
    val multiplier = 10.0.pow(decimals)
    return kotlin.math.round(this * multiplier) / multiplier
}
