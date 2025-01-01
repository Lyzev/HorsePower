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

package dev.lyzev.hp.mixin;

import dev.lyzev.hp.HorsePower;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HorseScreen;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.AbstractHorseEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HorseScreen.class)
public class HorseScreenMixin {

    @Shadow
    @Final
    private AbstractHorseEntity entity;

    @Inject(method = "drawBackground", at = @At("RETURN"))
    private void onDrawBackground(DrawContext drawContext, float f, int i, int j, CallbackInfo ci) {
        // TODO: Add indicator to the horse inventory screen to show the attributes of the horse
        if (entity.getInventoryColumns() > 0) {
        } else {
            int imageWidth = 176;
            int imageHeight = 166;

            int x = HorsePower.INSTANCE.getMc().getWindow().getScaledWidth() / 2 - imageWidth / 2 + 82;
            int y = HorsePower.INSTANCE.getMc().getWindow().getScaledHeight() / 2 - imageHeight / 2 + 20;

            AbstractHorseEntity horse = this.entity;

            double speedMovement = Math.round(horse.getAttributeBaseValue(EntityAttributes.MOVEMENT_SPEED) * 100) / 100.0;
            double jumpHeight = Math.round(horse.getAttributeBaseValue(EntityAttributes.JUMP_STRENGTH) * 100) / 100.0;
            double health = Math.round(horse.getAttributeBaseValue(EntityAttributes.MAX_HEALTH) * 100) / 100.0;

            drawContext.drawText(HorsePower.INSTANCE.getMc().textRenderer, "Speed: " + speedMovement, x, y, 0xFFFFFF, true);
            drawContext.drawText(HorsePower.INSTANCE.getMc().textRenderer, "Jump: " + jumpHeight, x, y + 10, 0xFFFFFF, true);
            drawContext.drawText(HorsePower.INSTANCE.getMc().textRenderer, "Health: " + health, x, y + 20, 0xFFFFFF, true);
        }
    }
}
