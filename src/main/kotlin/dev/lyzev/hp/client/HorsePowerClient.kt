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

package dev.lyzev.hp.client

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import dev.lyzev.hp.client.modmenu.HorsePowerConfig
import dev.lyzev.hp.client.modmenu.HorsePowerConfigManager
import dev.lyzev.hp.client.util.HorseStatsRenderer.render
import dev.lyzev.hp.client.util.round
import dev.lyzev.hp.client.util.toBPS
import dev.lyzev.hp.client.util.toJump
import dev.lyzev.hp.main.payload.SearchAllowedPayload
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.passive.AbstractHorseEntity
import net.minecraft.entity.passive.DonkeyEntity
import net.minecraft.entity.passive.HorseEntity
import net.minecraft.entity.passive.MuleEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.apache.logging.log4j.LogManager


object HorsePowerClient : ClientModInitializer {

    const val MOD_ID = "horsepower"

    val mc = MinecraftClient.getInstance()
    private val logger = LogManager.getLogger(HorsePowerClient::class.java)

    var last = System.currentTimeMillis()
    val horses = mutableListOf<Entity>()

    override fun onInitializeClient() {
        logger.info("Initializing HorsePowerClient")

        HorsePowerConfigManager.initializeConfig()
        logger.info("Config initialized")

        ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { dispatcher, _ ->
            dispatcher.register(
                ClientCommandManager.literal("search")
                    .then(
                        ClientCommandManager.argument("criteria", StringArgumentType.word())
                            .suggests { _, builder ->
                                builder.suggest("health").suggest("speed").suggest("jump").suggest("average").buildFuture()
                            }
                            .then(
                                ClientCommandManager.argument("amount", IntegerArgumentType.integer(1, 100))
                                    .then(
                                        ClientCommandManager.argument("direction", StringArgumentType.word())
                                            .suggests { _, builder ->
                                                builder.suggest("best").suggest("worst").buildFuture()
                                            }
                                            .executes { context ->
                                                val criteria = StringArgumentType.getString(context, "criteria")
                                                val amount = IntegerArgumentType.getInteger(context, "amount")
                                                val dir = StringArgumentType.getString(context, "direction")
                                                    .equals("best", ignoreCase = true)

                                                executeSearch(context, criteria, amount, dir)
                                                1
                                            }
                                    )
                            )
                    )
                    .executes { context ->
                        val criteria = "average"
                        val amount = 2
                        val dir = true
                        executeSearch(context, criteria, amount, dir)
                        1
                    }
            )
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
        logger.info("Commands registered")

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
        logger.info("HudRenderCallback registered")

        ClientLoginConnectionEvents.INIT.register(ClientLoginConnectionEvents.Init { handler, client ->
            HorsePowerConfig.isSearchCommandAllowed = true
        })
        logger.info("ClientLoginConnectionEvents registered")

        PayloadTypeRegistry.configurationS2C().register(SearchAllowedPayload.ID, SearchAllowedPayload.CODEC)

        ClientConfigurationNetworking.registerGlobalReceiver(SearchAllowedPayload.ID) { payload: SearchAllowedPayload, context ->
            context.client().execute {
                HorsePowerConfig.isSearchCommandAllowed = payload.allowed
                if (!payload.allowed) {
                    mc.inGameHud.chatHud.addMessage(
                        Text.translatable("horsepower.search.disabled").formatted(Formatting.RED)
                    )
                }
            }
        }
        logger.info("SearchAllowedPayload registered")
    }

    private fun executeSearch(context: CommandContext<FabricClientCommandSource>, criteria: String, amount: Int, dir: Boolean): Int {
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
            HorsePowerClient.horses.clear()
            if (dir) {
                HorsePowerClient.horses += horses.takeLast(amount)
            }else{
                HorsePowerClient.horses += horses.take(amount)
            }
            context.source.sendFeedback(
                Text.translatable(
                    "horsepower.search.success",
                    HorsePowerClient.horses.size,
                    criteria,
                    if (dir) "best" else "worst"
                ).withColor(Formatting.GREEN.colorValue!!)
            )
            1
        }
    }
}
