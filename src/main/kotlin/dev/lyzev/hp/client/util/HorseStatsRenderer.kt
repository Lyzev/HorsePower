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

import dev.lyzev.hp.client.HorsePowerClient
import dev.lyzev.hp.client.modmenu.HorsePowerConfig
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.passive.AbstractHorseEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import kotlin.math.roundToInt

object HorseStatsRenderer {

    private val BACKGROUND_TEXTURE = Identifier.of(HorsePowerClient.MOD_ID, "textures/gui/container/background.png")
    private const val WHITE = 0xFFFFFFFF.toInt()
    private val FORMATTINGS = arrayOf(Formatting.DARK_RED, Formatting.RED, Formatting.GOLD, Formatting.YELLOW, Formatting.GREEN, Formatting.DARK_GREEN)

    fun render(drawContext: DrawContext, entity: AbstractHorseEntity, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        if (!HorsePowerConfig.SHOW_INVENTORY.value) return

        val speed = entity.getAttributeBaseValue(EntityAttributes.MOVEMENT_SPEED).toBPS().round(1)
        val jump = entity.getAttributeBaseValue(EntityAttributes.JUMP_STRENGTH).toJump().round(1)
        val health = entity.getAttributeBaseValue(EntityAttributes.MAX_HEALTH).round(1)

        val speedPercentage = entity.getAttributeBaseValue(EntityAttributes.MOVEMENT_SPEED).toPercentage(AbstractHorseEntity.MAX_MOVEMENT_SPEED_BONUS)
        val jumpPercentage = entity.getAttributeBaseValue(EntityAttributes.JUMP_STRENGTH).toPercentage(AbstractHorseEntity.MAX_JUMP_STRENGTH_BONUS)
        val healthPercentage = health.toPercentage(AbstractHorseEntity.MAX_HEALTH_BONUS)

        drawContext.drawBackgroundBox(x, y)

        drawContext.drawAttribute("↔ ", speed, speedPercentage, AbstractHorseEntity.MIN_MOVEMENT_SPEED_BONUS.toDouble().toBPS(), AbstractHorseEntity.MAX_MOVEMENT_SPEED_BONUS.toDouble().toBPS(), x, y, 0, mouseX, mouseY)
        drawContext.drawAttribute("↕ ", jump, jumpPercentage, AbstractHorseEntity.MIN_JUMP_STRENGTH_BONUS.toDouble().toJump(), AbstractHorseEntity.MAX_JUMP_STRENGTH_BONUS.toDouble().toJump(), x, y, 10, mouseX, mouseY)
        drawContext.drawAttribute("♥ ", health, healthPercentage, AbstractHorseEntity.MIN_HEALTH_BONUS.toDouble(), AbstractHorseEntity.MAX_HEALTH_BONUS.toDouble(), x, y, 20, mouseX, mouseY)

        if (HorsePowerConfig.SHOW_AVERAGE.value) {
            drawContext.drawAverage(speedPercentage, jumpPercentage, healthPercentage, x, y + 30)
        }
    }

    private fun DrawContext.drawBackgroundBox(x: Int, y: Int) {
        drawTexture(RenderLayer::getGuiTextured, BACKGROUND_TEXTURE, x - 5, y - 5, 0f, 0f, 126, 61, 256, 256)
    }

    private fun Double.toPercentage(maxValue: Float): Double = this / maxValue

    private fun DrawContext.drawAttribute(symbol: String, value: Double, percentage: Double, minValue: Double, maxValue: Double, x: Int, y: Int, offsetY: Int, mouseX: Int, mouseY: Int) {
        val text = buildAttributeText(symbol, value, percentage)
        val formatting = getFormatting(percentage)
        drawTextWithShadow(HorsePowerClient.mc.textRenderer, Text.literal(text).formatted(formatting), x, y + offsetY, WHITE)

        if (isMouseHovering(mouseX, mouseY, x, y + offsetY, text)) {
            drawTooltip(minValue, maxValue, mouseX, mouseY)
        }
    }

    private fun buildAttributeText(symbol: String, value: Double, percentage: Double): String {
        return buildString {
            append(symbol).append(value)
            if (HorsePowerConfig.SHOW_UNIT.value) {
                append(symbol.getUnit())
            }
            if (HorsePowerConfig.SHOW_PERCENTAGE.value) {
                append(" (").append((percentage * 100).roundToInt()).append("%)")
            }
        }
    }

    private fun String.getUnit(): String = when (this) {
        "↔ " -> " m/s"
        "↕ " -> " blocks"
        else -> " HP"
    }

    private fun getFormatting(percentage: Double): Formatting {
        return FORMATTINGS[(percentage * (FORMATTINGS.size - 1)).toInt().coerceIn(0, FORMATTINGS.size - 1)]
    }

    private fun isMouseHovering(mouseX: Int, mouseY: Int, x: Int, y: Int, text: String): Boolean {
        val textWidth = HorsePowerClient.mc.textRenderer.getWidth(text)
        return mouseX in x..(x + textWidth) && mouseY in y..(y + 9)
    }

    private fun DrawContext.drawTooltip(minValue: Double, maxValue: Double, mouseX: Int, mouseY: Int) {
        val hoverText = listOf(
            Text.literal("Min: ${minValue.round(1)}").formatted(Formatting.DARK_RED),
            Text.literal("Max: ${maxValue.round(1)}").formatted(Formatting.DARK_GREEN)
        )
        drawTooltip(HorsePowerClient.mc.textRenderer, hoverText, mouseX, mouseY)
    }

    private fun DrawContext.drawAverage(speedPercentage: Double, jumpPercentage: Double, healthPercentage: Double, x: Int, y: Int) {
        val average = (speedPercentage + jumpPercentage + healthPercentage) / 3
        val averageFormatting = getFormatting(average)
        drawTextWithShadow(HorsePowerClient.mc.textRenderer, Text.literal("Average: ${(average * 100).roundToInt()}%").formatted(averageFormatting), x, y, WHITE)
    }
}
