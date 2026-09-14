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
import dev.lyzev.hp.client.util.HorseStatsRenderer
import dev.lyzev.hp.client.util.round
import dev.lyzev.hp.client.util.toBPS
import dev.lyzev.hp.client.util.toJump
import dev.lyzev.hp.main.payload.SearchAllowedPayload
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.animal.equine.AbstractHorse
import net.minecraft.world.entity.animal.equine.Donkey
import net.minecraft.world.entity.animal.equine.Horse
import net.minecraft.world.entity.animal.equine.Mule
import org.apache.logging.log4j.LogManager


object HorsePowerClient : ClientModInitializer {

    const val MOD_ID = "horsepower"
    val minMovementSpeed = AbstractHorse.MIN_MOVEMENT_SPEED.toDouble()
    val maxMovementSpeed = AbstractHorse.MAX_MOVEMENT_SPEED.toDouble()
    val minJumpStrength = AbstractHorse.MIN_JUMP_STRENGTH.toDouble()
    val maxJumpStrength = AbstractHorse.MAX_JUMP_STRENGTH.toDouble()
    val minHealth = AbstractHorse.MIN_HEALTH.toDouble()
    val maxHealth = AbstractHorse.MAX_HEALTH.toDouble()

    val mc = Minecraft.getInstance()
    private val logger = LogManager.getLogger(HorsePowerClient::class.java)

    var last = System.currentTimeMillis()
    val horses = mutableListOf<Entity>()

    override fun onInitializeClient() {
        logger.info("Initializing HorsePowerClient")

        HorsePowerConfigManager.initializeConfig()
        logger.info("Config initialized")

        ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { dispatcher, _ ->
            dispatcher.register(
                ClientCommands.literal("search")
                    .then(
                        ClientCommands.argument("criteria", StringArgumentType.word())
                            .suggests { _, builder ->
                                builder.suggest("health").suggest("speed").suggest("jump").suggest("average").buildFuture()
                            }
                            .then(
                                ClientCommands.argument("amount", IntegerArgumentType.integer(1, 100))
                                    .then(
                                        ClientCommands.argument("direction", StringArgumentType.word())
                                            .suggests { _, builder ->
                                                builder.suggest("best").suggest("worst").buildFuture()
                                            }
                                            .executes { context ->
                                                val criteria = StringArgumentType.getString(context, "criteria").lowercase()
                                                val amount = IntegerArgumentType.getInteger(context, "amount")
                                                val searchDirection = StringArgumentType.getString(context, "direction")
                                                    .equals("best", ignoreCase = true)

                                                executeSearch(context, criteria, amount, searchDirection)
                                            }
                                    ).executes { context ->
                                        val criteria = StringArgumentType.getString(context, "criteria").lowercase()
                                        val amount = IntegerArgumentType.getInteger(context, "amount")
                                        val searchDirection = true
                                        executeSearch(context, criteria, amount, searchDirection)
                                    }
                            ).executes { context ->
                                val criteria = StringArgumentType.getString(context, "criteria").lowercase()
                                val amount = 2
                                val dir = true
                                executeSearch(context, criteria, amount, dir)
                            }
                    )
                    .executes { context ->
                        val criteria = "average"
                        val amount = 2
                        val dir = true
                        executeSearch(context, criteria, amount, dir)
                    }
            )
            dispatcher.register(
                ClientCommands.literal("stats").executes { context: CommandContext<FabricClientCommandSource> ->
                    val targetEntity = mc.crosshairPickEntity
                    if (targetEntity is AbstractHorse) {
                        val movementSpeed = targetEntity.getAttributeBaseValue(Attributes.MOVEMENT_SPEED)
                        val jumpStrength = targetEntity.getAttributeBaseValue(Attributes.JUMP_STRENGTH)
                        val health = targetEntity.getAttributeBaseValue(Attributes.MAX_HEALTH)
                        context.source.sendFeedback(
                            Component.translatable(
                                "horsepower.stats.success",
                                movementSpeed.toBPS().round(1),
                                jumpStrength.toJump().round(1),
                                health.round(1)
                            ).withStyle(ChatFormatting.GREEN)
                        )
                        1
                    } else {
                        context.source.sendError(
                            Component.translatable("horsepower.stats.error").withStyle(ChatFormatting.RED)
                        )
                        0
                    }
                })
        })
        logger.info("Commands registered")

        HudElementRegistry.attachElementAfter(
            VanillaHudElements.MISC_OVERLAYS,
            Identifier.fromNamespaceAndPath(MOD_ID, "hud"),
            HorseStatsRenderer
        )
        logger.info("HudElement registered")

        ClientLoginConnectionEvents.INIT.register(ClientLoginConnectionEvents.Init { _, _ ->
            HorsePowerConfig.isSearchCommandAllowed = true
        })
        logger.info("ClientLoginConnectionEvents registered")

        PayloadTypeRegistry.clientboundConfiguration().register(SearchAllowedPayload.TYPE, SearchAllowedPayload.CODEC)

        ClientConfigurationNetworking.registerGlobalReceiver(SearchAllowedPayload.TYPE) { payload: SearchAllowedPayload, context ->
            context.client().execute {
                HorsePowerConfig.isSearchCommandAllowed = payload.allowed
                if (!payload.allowed) {
                    mc.gui.hud.chat.addClientSystemMessage(
                        Component.translatable("horsepower.search.disabled").withStyle(ChatFormatting.RED)
                    )
                }
            }
        }
        logger.info("SearchAllowedPayload registered")
    }

    private fun executeSearch(context: CommandContext<FabricClientCommandSource>, criteria: String, amount: Int, searchDirection: Boolean): Int {
        var criteria = criteria
        if (!HorsePowerConfig.isSearchCommandAllowed) {
            context.source.sendError(Component.translatable("horsepower.search.disabled"))
            return 0
        }
        val horses =
            mc.level!!.entitiesForRendering().filter { it is Horse || it is Donkey || it is Mule }.sortedBy {
                val horse = it as AbstractHorse
                when (criteria) {
                    "health" -> horse.getAttributeBaseValue(Attributes.MAX_HEALTH)
                    "speed" -> horse.getAttributeBaseValue(Attributes.MOVEMENT_SPEED)
                    "jump" -> horse.getAttributeBaseValue(Attributes.JUMP_STRENGTH)
                    else -> {
                        criteria = "average"
                        val movementSpeed = horse.getAttributeBaseValue(Attributes.MOVEMENT_SPEED).coerceIn(
                            minMovementSpeed,
                            maxMovementSpeed
                        ) / maxMovementSpeed
                        val jumpStrength = horse.getAttributeBaseValue(Attributes.JUMP_STRENGTH).coerceIn(
                            minJumpStrength,
                            maxJumpStrength
                        ) / maxJumpStrength
                        val health = horse.getAttributeBaseValue(Attributes.MAX_HEALTH).coerceIn(
                            minHealth,
                            maxHealth
                        ) / maxHealth
                        movementSpeed + jumpStrength + health
                    }
                }
            }
        return if (horses.isEmpty()) {
            context.source.sendError(Component.translatable("horsepower.search.error"))
            0
        } else {
            last = System.currentTimeMillis()
            HorsePowerClient.horses.clear()
            if (searchDirection) {
                HorsePowerClient.horses += horses.takeLast(amount)
            } else {
                HorsePowerClient.horses += horses.take(amount)
            }
            context.source.sendFeedback(
                Component.translatable(
                    "horsepower.search.success",
                    HorsePowerClient.horses.size,
                    Component.translatable("horsepower.search.criteria.$criteria"),
                    Component.translatable(if (searchDirection) "horsepower.search.best" else "horsepower.search.worst")
                ).withStyle(ChatFormatting.GREEN)
            )
            1
        }
    }
}
