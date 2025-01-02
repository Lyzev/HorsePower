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

package dev.lyzev.hp.util

import java.awt.Color

private val hsvA = FloatArray(3)
private val hsvB = FloatArray(3)

fun interpolateColor(a: Color, b: Color, percent: Double): Int {
    Color.RGBtoHSB(a.red, a.green, a.blue, hsvA)
    Color.RGBtoHSB(b.red, b.green, b.blue, hsvB)
    return Color.HSBtoRGB(
        hsvA[0] + ((hsvB[0] - hsvA[0]) * percent).toFloat(),
        hsvA[1] + ((hsvB[1] - hsvA[1]) * percent).toFloat(),
        hsvA[2] + ((hsvB[2] - hsvA[2]) * percent).toFloat()
    )
}
