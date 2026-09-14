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

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.Identifier

@JvmRecord
data class SearchAllowedPayload(val allowed: Boolean) : CustomPacketPayload {

    override fun type() = TYPE

    companion object {
        val TYPE: CustomPacketPayload.Type<SearchAllowedPayload> =
            CustomPacketPayload.Type(Identifier.fromNamespaceAndPath("horsepower", "search"))

        val CODEC: StreamCodec<FriendlyByteBuf, SearchAllowedPayload> =
            StreamCodec.composite(
                ByteBufCodecs.BOOL, SearchAllowedPayload::allowed
            ) { allowed: Boolean -> SearchAllowedPayload(allowed) }
    }
}
