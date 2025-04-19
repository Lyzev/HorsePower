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
package dev.lyzev.hp.mixin

import dev.lyzev.hp.client.HorsePowerClient.horses
import dev.lyzev.hp.client.HorsePowerClient.last
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.passive.AbstractHorseEntity
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@Mixin(MinecraftClient::class)
class MinecraftClientMixin {
    @Inject(method = ["hasOutline"], at = [At("HEAD")], cancellable = true)
    private fun onHasOutline(entity: Entity?, cir: CallbackInfoReturnable<Boolean?>) {
        if (entity is AbstractHorseEntity) {
            if (System.currentTimeMillis() - last <= 5000 && horses.contains(entity)) {
                cir.returnValue = true
                cir.cancel()
            }
        }
    }
}
