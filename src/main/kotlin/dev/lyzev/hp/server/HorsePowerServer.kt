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

import dev.lyzev.hp.main.payload.SearchAllowedPayload
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking

object HorsePowerServer : DedicatedServerModInitializer {

    override fun onInitializeServer() {
        PayloadTypeRegistry.configurationS2C().register(SearchAllowedPayload.ID, SearchAllowedPayload.CODEC)

        val payload = SearchAllowedPayload(false)

        ServerConfigurationConnectionEvents.CONFIGURE.register { handler, sender ->
            if (ServerConfigurationNetworking.canSend(handler, SearchAllowedPayload.ID)) {
                ServerConfigurationNetworking.send(handler, payload)
            }
        }
    }
}
