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
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.animal.horse.AbstractHorse
import net.minecraft.world.entity.animal.horse.Donkey
import net.minecraft.world.entity.animal.horse.Horse
import net.minecraft.world.entity.animal.horse.Mule


object HorsePower : ClientModInitializer {

    val mc = Minecraft.getInstance()

    var last = System.currentTimeMillis()
    val horses = mutableListOf<Entity>()

    override fun onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { dispatcher, _ ->
            dispatcher.register(
                ClientCommandManager.literal("search")
                    .executes { context: CommandContext<FabricClientCommandSource> ->
                        val horses = mc.level!!.entitiesForRendering().filter { it is Horse || it is Donkey || it is Mule }.sortedBy {
                            val horse = it as AbstractHorse
                            val movementSpeed = horse.getAttributeBaseValue(Attributes.MOVEMENT_SPEED).coerceIn(AbstractHorse.MIN_MOVEMENT_SPEED.toDouble(), AbstractHorse.MAX_MOVEMENT_SPEED.toDouble()) / AbstractHorse.MAX_MOVEMENT_SPEED.toDouble()
                            val jumpStrength = horse.getAttributeBaseValue(Attributes.JUMP_STRENGTH).coerceIn(AbstractHorse.MIN_JUMP_STRENGTH.toDouble(), AbstractHorse.MAX_JUMP_STRENGTH.toDouble()) / AbstractHorse.MAX_JUMP_STRENGTH.toDouble()
                            val health = horse.getAttributeBaseValue(Attributes.MAX_HEALTH).coerceIn(AbstractHorse.MIN_HEALTH.toDouble(), AbstractHorse.MAX_HEALTH.toDouble()) / AbstractHorse.MAX_HEALTH.toDouble()
                            movementSpeed + jumpStrength + health
                        }
                        if (horses.isEmpty()) {
                            context.source.sendError(Component.translatable("hp.search.error"))
                            0
                        } else {
                            last = System.currentTimeMillis()
                            this.horses.clear()
                            this.horses += horses.take(2)
                            context.source.sendFeedback(Component.translatable(if (horses.count() == 1) "hp.search.success.one" else "hp.search.success.two").withColor(ChatFormatting.GREEN.color!!))
                            1
                        }
                    }
            )
        })
    }
}
