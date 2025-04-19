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

package dev.lyzev.hp.client.modmenu

import com.terraformersmc.modmenu.config.FileOnlyConfig
import com.terraformersmc.modmenu.config.option.BooleanConfigOption
import com.terraformersmc.modmenu.config.option.OptionConvertible
import net.minecraft.client.option.SimpleOption
import java.lang.reflect.Modifier

object HorsePowerConfig {

    val SHOW_INVENTORY = BooleanConfigOption("show_inventory", true)
    val SHOW_UNIT = BooleanConfigOption("show_unit", false)
    val SHOW_PERCENTAGE = BooleanConfigOption("show_percentage", true)
    val SHOW_AVERAGE = BooleanConfigOption("show_average", true)
    val SHOW_HUD = BooleanConfigOption("show_hud", true)

    var isSearchCommandAllowed = true

    fun asOptions(): Array<SimpleOption<*>> {
        val options = ArrayList<SimpleOption<*>>()
        for (field in HorsePowerConfig::class.java.declaredFields) {
            if (Modifier.isFinal(field.modifiers) && OptionConvertible::class.java.isAssignableFrom(field.type) && !field.isAnnotationPresent(FileOnlyConfig::class.java)) {
                try {
                    options.add((field[this] as OptionConvertible).asOption())
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }
            }
        }
        return options.toTypedArray()
    }
}
