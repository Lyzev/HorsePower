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
package dev.lyzev.hp.main.payload

import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

@JvmRecord
data class SearchAllowedPayload(val allowed: Boolean) : CustomPayload {

    override fun getId() = ID

    companion object {
        private val PACKET_ID = Identifier.of("horsepower", "search")

        val ID = CustomPayload.Id<SearchAllowedPayload>(PACKET_ID)
        val CODEC: PacketCodec<PacketByteBuf, SearchAllowedPayload> =
            PacketCodec.tuple(
                PacketCodecs.BOOLEAN, SearchAllowedPayload::allowed
            ) { allowed: Boolean -> SearchAllowedPayload(allowed) }
    }
}
