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

package dev.lyzev.hp.server

import dev.lyzev.hp.client.HorsePowerClient
import dev.lyzev.hp.main.payload.SearchAllowedPayload
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking
import org.apache.logging.log4j.LogManager
import kotlin.math.log

object HorsePowerServer : DedicatedServerModInitializer {

    private val logger = LogManager.getLogger(HorsePowerClient::class.java)

    override fun onInitializeServer() {
        logger.info("Initializing HorsePower server")

        PayloadTypeRegistry.configurationS2C().register(SearchAllowedPayload.ID, SearchAllowedPayload.CODEC)
        logger.info("Registered SearchAllowedPayload")

        val payload = SearchAllowedPayload(false)

        ServerConfigurationConnectionEvents.CONFIGURE.register { handler, sender ->
            if (ServerConfigurationNetworking.canSend(handler, SearchAllowedPayload.ID)) {
                ServerConfigurationNetworking.send(handler, payload)
                logger.info("Disabled search command for player ${sender.name}")
            } else {
                logger.error("Failed to send SearchAllowedPayload to player ${sender.name}")
            }
        }
        logger.info("Registered ServerConfigurationConnectionEvents")
    }
}
