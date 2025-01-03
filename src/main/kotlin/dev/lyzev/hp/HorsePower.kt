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

package dev.lyzev.hp

import com.mojang.brigadier.context.CommandContext
import dev.lyzev.hp.modmenu.HorsePowerConfigManager
import dev.lyzev.hp.util.round
import dev.lyzev.hp.util.unit2bps
import dev.lyzev.hp.util.unit2jump
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.passive.AbstractHorseEntity
import net.minecraft.entity.passive.DonkeyEntity
import net.minecraft.entity.passive.HorseEntity
import net.minecraft.entity.passive.MuleEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting


object HorsePower : ClientModInitializer {

    const val MOD_ID = "horsepower"

    val mc = MinecraftClient.getInstance()

    var last = System.currentTimeMillis()
    val horses = mutableListOf<Entity>()

    override fun onInitializeClient() {
        HorsePowerConfigManager.initializeConfig()

        ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { dispatcher, _ ->
            dispatcher.register(
                ClientCommandManager.literal("search").executes { context: CommandContext<FabricClientCommandSource> ->
                    val horses =
                        mc.world!!.entities.filter { it is HorseEntity || it is DonkeyEntity || it is MuleEntity }
                            .sortedBy {
                                val horse = it as AbstractHorseEntity
                                val movementSpeed =
                                    horse.getAttributeBaseValue(EntityAttributes.MOVEMENT_SPEED).coerceIn(
                                        AbstractHorseEntity.MIN_MOVEMENT_SPEED_BONUS.toDouble(),
                                        AbstractHorseEntity.MAX_MOVEMENT_SPEED_BONUS.toDouble()
                                    ) / AbstractHorseEntity.MAX_MOVEMENT_SPEED_BONUS.toDouble()
                                val jumpStrength = horse.getAttributeBaseValue(EntityAttributes.JUMP_STRENGTH).coerceIn(
                                    AbstractHorseEntity.MIN_JUMP_STRENGTH_BONUS.toDouble(),
                                    AbstractHorseEntity.MAX_JUMP_STRENGTH_BONUS.toDouble()
                                ) / AbstractHorseEntity.MAX_JUMP_STRENGTH_BONUS.toDouble()
                                val health = horse.getAttributeBaseValue(EntityAttributes.MAX_HEALTH).coerceIn(
                                    AbstractHorseEntity.MIN_HEALTH_BONUS.toDouble(),
                                    AbstractHorseEntity.MAX_HEALTH_BONUS.toDouble()
                                ) / AbstractHorseEntity.MAX_HEALTH_BONUS.toDouble()
                                movementSpeed + jumpStrength + health
                            }
                    if (horses.isEmpty()) {
                        context.source.sendError(Text.translatable("horsepower.search.error"))
                        0
                    } else {
                        last = System.currentTimeMillis()
                        this.horses.clear()
                        this.horses += horses.take(2)
                        context.source.sendFeedback(
                            Text.translatable(if (horses.count() == 1) "horsepower.search.success.one" else "horsepower.search.success.two")
                                .withColor(Formatting.GREEN.colorValue!!)
                        )
                        1
                    }
                })
            // TODO: Add /stats command that gets crosshair entity check if it's a horse and then display stats
            dispatcher.register(
                ClientCommandManager.literal("stats").executes { context: CommandContext<FabricClientCommandSource> ->
                    val targetEntity = mc.targetedEntity
                    if (targetEntity is AbstractHorseEntity) {
                        val movementSpeed = targetEntity.getAttributeBaseValue(EntityAttributes.MOVEMENT_SPEED)
                        val jumpStrength = targetEntity.getAttributeBaseValue(EntityAttributes.JUMP_STRENGTH)
                        val health = targetEntity.getAttributeBaseValue(EntityAttributes.MAX_HEALTH)
                        context.source.sendFeedback(
                            Text.translatable(
                                "horsepower.stats.success",
                                unit2bps(movementSpeed).round(1),
                                unit2jump(jumpStrength).round(1),
                                health.round(1)
                            ).formatted(Formatting.GREEN)
                        )
                        1
                    } else {
                        context.source.sendError(
                            Text.translatable("horsepower.stats.error").formatted(Formatting.RED)
                        )
                        0
                    }
                })
        })
    }
}
