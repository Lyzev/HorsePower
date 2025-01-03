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

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import dev.lyzev.hp.modmenu.HorsePowerConfig
import dev.lyzev.hp.modmenu.HorsePowerConfigManager
import dev.lyzev.hp.payload.SearchAllowedPayload
import dev.lyzev.hp.util.HorseStatsRenderer.render
import dev.lyzev.hp.util.round
import dev.lyzev.hp.util.toBPS
import dev.lyzev.hp.util.toJump
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.networking.v1.C2SPlayChannelEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.passive.AbstractHorseEntity
import net.minecraft.entity.passive.DonkeyEntity
import net.minecraft.entity.passive.HorseEntity
import net.minecraft.entity.passive.MuleEntity
import net.minecraft.network.packet.CustomPayload
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
                ClientCommandManager.literal("search")
                    .then(
                        ClientCommandManager.argument("criteria", StringArgumentType.word()).suggests { _, builder ->
                        builder.suggest("health").suggest("speed").suggest("jump").suggest("average").buildFuture()
                    }.then(
                        ClientCommandManager.argument("amount", IntegerArgumentType.integer(1, 10))
                            .executes { context ->
                                val criteria = StringArgumentType.getString(context, "criteria")
                                val amount = IntegerArgumentType.getInteger(context, "amount")
                                executeSearch(context, criteria, amount)
                            })).executes { context: CommandContext<FabricClientCommandSource> ->
                        val criteria = "average"
                        val amount = 2
                        executeSearch(context, criteria, amount)
                    })
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
                                movementSpeed.toBPS().round(1),
                                jumpStrength.toJump().round(1),
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

        HudRenderCallback.EVENT.register(HudRenderCallback { drawContext, _ ->
            if (HorsePowerConfig.SHOW_HUD.value) {
                val entity = mc.targetedEntity
                if (entity is AbstractHorseEntity) {
                    render(
                        drawContext,
                        entity,
                        mc.window.scaledWidth / 2 + 10,
                        mc.window.scaledHeight / 2 + 10,
                        0,
                        0
                    )
                }
            }
        })

        C2SPlayChannelEvents.REGISTER.register(C2SPlayChannelEvents.Register { handler, sender, client, channels ->
            HorsePowerConfig.isSearchCommandAllowed = true
            println("Search command is allowed!!!!!!!!")
        })

        PayloadTypeRegistry.configurationS2C().register(SearchAllowedPayload.ID, SearchAllowedPayload.CODEC)

        ClientConfigurationNetworking.registerGlobalReceiver(CustomPayload.Id(HorsePowerConfig.SEARCH_ALLOWED_PACKET_ID)) { payload: SearchAllowedPayload, context ->
            context.client().execute {
                HorsePowerConfig.isSearchCommandAllowed = payload.allowed
                if (!payload.allowed) {
                    mc.inGameHud.chatHud.addMessage(
                        Text.translatable("horsepower.search.disabled").formatted(Formatting.RED)
                    )
                }
                println("Search command is ${if (payload.allowed) "allowed" else "disabled"}!!!!!!!!!")
            }
        }
    }

    private fun executeSearch(context: CommandContext<FabricClientCommandSource>, criteria: String, amount: Int): Int {
        if (!HorsePowerConfig.isSearchCommandAllowed) {
            context.source.sendError(Text.translatable("horsepower.search.disabled"))
            return 0
        }
        val horses =
            mc.world!!.entities.filter { it is HorseEntity || it is DonkeyEntity || it is MuleEntity }.sortedBy {
                    val horse = it as AbstractHorseEntity
                    when (criteria) {
                        "health" -> horse.getAttributeBaseValue(EntityAttributes.MAX_HEALTH)
                        "speed" -> horse.getAttributeBaseValue(EntityAttributes.MOVEMENT_SPEED)
                        "jump" -> horse.getAttributeBaseValue(EntityAttributes.JUMP_STRENGTH)
                        else -> {
                            val movementSpeed = horse.getAttributeBaseValue(EntityAttributes.MOVEMENT_SPEED).coerceIn(
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
                    }
                }
        return if (horses.isEmpty()) {
            context.source.sendError(Text.translatable("horsepower.search.error"))
            0
        } else {
            last = System.currentTimeMillis()
            this.horses.clear()
            this.horses += horses.takeLast(amount)
            context.source.sendFeedback(
                Text.translatable(
                    "horsepower.search.success", this.horses.size, criteria
                ).withColor(Formatting.GREEN.colorValue!!)
            )
            1
        }
    }
}
