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
import dev.lyzev.hp.client.HorsePowerClient.mc
import dev.lyzev.hp.client.modmenu.HorsePowerConfig
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement
import net.minecraft.ChatFormatting
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.animal.equine.AbstractHorse

object HorseStatsRenderer : HudElement {

    private val BACKGROUND_TEXTURE = Identifier.fromNamespaceAndPath(HorsePowerClient.MOD_ID, "textures/gui/container/background.png")
    private const val WHITE = 0xFFFFFFFF.toInt()
    private val FORMATTINGS = arrayOf(ChatFormatting.DARK_RED, ChatFormatting.RED, ChatFormatting.GOLD, ChatFormatting.YELLOW, ChatFormatting.GREEN, ChatFormatting.DARK_GREEN)

    override fun extractRenderState(extractor: GuiGraphicsExtractor, deltaTracker: DeltaTracker) {
        if (!HorsePowerConfig.SHOW_HUD.value) return
        val entity = mc.crosshairPickEntity
        if (entity is AbstractHorse) {
            val x = extractor.guiWidth() / 2 + 10
            val y = extractor.guiHeight() / 2 + 10

            render(extractor, entity, x, y, -1, -1)
        }
    }

    fun render(extractor: GuiGraphicsExtractor, entity: AbstractHorse, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        val speed = entity.getAttributeBaseValue(Attributes.MOVEMENT_SPEED).toBPS().round(3)
        val jump = entity.getAttributeBaseValue(Attributes.JUMP_STRENGTH).toJump().round(3)
        val health = entity.getAttributeBaseValue(Attributes.MAX_HEALTH).round(3)

        val speedPercentage = entity.getAttributeBaseValue(Attributes.MOVEMENT_SPEED).toPercentage(HorsePowerClient.maxMovementSpeed)
        val jumpPercentage = entity.getAttributeBaseValue(Attributes.JUMP_STRENGTH).toPercentage(HorsePowerClient.maxJumpStrength)
        val healthPercentage = health.toPercentage(HorsePowerClient.maxHealth)

        extractor.drawBackgroundBox(x, y)

        extractor.drawAttribute("→ ", speed, speedPercentage, HorsePowerClient.minMovementSpeed.toBPS(), HorsePowerClient.maxMovementSpeed.toBPS(), x, y, 0, mouseX, mouseY)
        extractor.drawAttribute("↑ ", jump, jumpPercentage, HorsePowerClient.minJumpStrength.toJump(), HorsePowerClient.maxJumpStrength.toJump(), x, y, 10, mouseX, mouseY)
        extractor.drawAttribute("♥ ", health, healthPercentage, HorsePowerClient.minHealth, HorsePowerClient.maxHealth, x, y, 20, mouseX, mouseY)

        if (HorsePowerConfig.SHOW_AVERAGE.value) {
            extractor.drawAverage(speedPercentage, jumpPercentage, healthPercentage, x, y + 30)
        }
    }

    private fun GuiGraphicsExtractor.drawBackgroundBox(x: Int, y: Int) {
        blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, x - 5, y - 5, 0f, 0f, 126, 61, 256, 256)
    }

    private fun Double.toPercentage(maxValue: Double): Double = this / maxValue

    private fun GuiGraphicsExtractor.drawAttribute(symbol: String, value: Double, percentage: Double, minValue: Double, maxValue: Double, x: Int, y: Int, offsetY: Int, mouseX: Int, mouseY: Int) {
        val text = buildAttributeText(symbol, value, percentage)
        val formatting = getFormatting(percentage)
        text(mc.font, Component.literal(text).withStyle(formatting), x, y + offsetY, WHITE, true)

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
                append(" (").append((percentage * 100).round(2)).append("%)")
            }
        }
    }

    private fun String.getUnit(): String = when (this) {
        "→ " -> Component.translatable("horsepower.unit.speed").string
        "↑ " -> Component.translatable("horsepower.unit.jump").string
        else -> Component.translatable("horsepower.unit.health").string
    }

    private fun getFormatting(percentage: Double): ChatFormatting {
        return FORMATTINGS[(percentage * (FORMATTINGS.size - 1)).toInt().coerceIn(0, FORMATTINGS.size - 1)]
    }

    private fun isMouseHovering(mouseX: Int, mouseY: Int, x: Int, y: Int, text: String): Boolean {
        val textWidth = mc.font.width(text)
        return mouseX in x..(x + textWidth) && mouseY in y..(y + 9)
    }

    private fun GuiGraphicsExtractor.drawTooltip(minValue: Double, maxValue: Double, mouseX: Int, mouseY: Int) {
        val hoverText = listOf(
            Component.translatable("horsepower.hud.tooltip.min", minValue.round(2)).withStyle(ChatFormatting.DARK_RED),
            Component.translatable("horsepower.hud.tooltip.max", maxValue.round(2)).withStyle(ChatFormatting.DARK_GREEN)
        )
        setComponentTooltipForNextFrame(mc.font, hoverText, mouseX, mouseY)
    }

    private fun GuiGraphicsExtractor.drawAverage(speedPercentage: Double, jumpPercentage: Double, healthPercentage: Double, x: Int, y: Int) {
        val average = (speedPercentage + jumpPercentage + healthPercentage) / 3
        val averageFormatting = getFormatting(average)
        text(mc.font, Component.translatable("horsepower.hud.average", (average * 100).round(2)).withStyle(averageFormatting), x, y, WHITE, true)
    }
}
