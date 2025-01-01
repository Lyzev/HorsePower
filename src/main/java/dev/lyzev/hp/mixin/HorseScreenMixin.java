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
import dev.lyzev.hp.modmenu.HorsePowerConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HorseScreen;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

import static dev.lyzev.hp.util.MathKt.unit2bps;
import static dev.lyzev.hp.util.MathKt.unit2jump;

@Mixin(HorseScreen.class)
public class HorseScreenMixin {

    @Shadow
    @Final
    private AbstractHorseEntity entity;

    @Inject(method = "drawBackground", at = @At("RETURN"))
    private void onDrawBackground(DrawContext drawContext, float f, int i, int j, CallbackInfo ci) {
        if (!HorsePowerConfig.INSTANCE.getSHOW_INVENTORY().getValue())
            return;
        // TODO: Add indicator to the horse inventory screen to show the attributes of the horse
        if (entity.getInventoryColumns() > 0) {
        } else {
            int imageWidth = 176;
            int imageHeight = 166;

            int x = HorsePower.INSTANCE.getMc().getWindow().getScaledWidth() / 2 - imageWidth / 2 + 82;
            int y = HorsePower.INSTANCE.getMc().getWindow().getScaledHeight() / 2 - imageHeight / 2 + 20;

            AbstractHorseEntity horse = this.entity;

            double speedMovement = Math.round(unit2bps(horse.getAttributeBaseValue(EntityAttributes.MOVEMENT_SPEED)) * 100) / 100.0;
            double jumpHeight = Math.round(unit2jump(horse.getAttributeBaseValue(EntityAttributes.JUMP_STRENGTH)) * 100) / 100.0;
            int health = (int) horse.getAttributeBaseValue(EntityAttributes.MAX_HEALTH);

            int color1 = 0xB71C1C;
            int color2 = 0x1B5E20;

            float[] hsv1 = new float[3];
            float[] hsv2 = new float[3];
            Color.RGBtoHSB((color1 >> 16) & 0xFF, (color1 >> 8) & 0xFF, color1 & 0xFF, hsv1);
            Color.RGBtoHSB((color2 >> 16) & 0xFF, (color2 >> 8) & 0xFF, color2 & 0xFF, hsv2);
            float[] diff = new float[]{
                hsv2[0] - hsv1[0],
                hsv2[1] - hsv1[1],
                hsv2[2] - hsv1[2]
            };

            double speedMovementPercentage = horse.getAttributeBaseValue(EntityAttributes.MOVEMENT_SPEED) / AbstractHorseEntity.MAX_MOVEMENT_SPEED_BONUS;
            double jumpHeightPercentage = horse.getAttributeBaseValue(EntityAttributes.JUMP_STRENGTH) / AbstractHorseEntity.MAX_JUMP_STRENGTH_BONUS;
            double healthPercentage = health / AbstractHorseEntity.MAX_HEALTH_BONUS;

            int interpolatedColorSpeed = Color.HSBtoRGB(
                hsv1[0] + (float) (diff[0] * speedMovementPercentage),
                1f,
                hsv1[2] + (float) (diff[2] * speedMovementPercentage)
            );
            int interpolatedColorJump = Color.HSBtoRGB(
                hsv1[0] + (float) (diff[0] * jumpHeightPercentage),
                1f,
                hsv1[2] + (float) (diff[2] * jumpHeightPercentage)
            );
            int interpolatedColorHealth = Color.HSBtoRGB(
                hsv1[0] + (float) (diff[0] * healthPercentage),
                1f,
                hsv1[2] + (float) (diff[2] * healthPercentage)
            );

            drawContext.drawText(HorsePower.INSTANCE.getMc().textRenderer, "↔ " + speedMovement + " m/s (" + Math.round(speedMovementPercentage * 100) + "%)", x, y, interpolatedColorSpeed, true);
            drawContext.drawText(HorsePower.INSTANCE.getMc().textRenderer, "↕ " + jumpHeight + " blocks (" + Math.round(jumpHeightPercentage * 100) + "%)", x, y + 10, interpolatedColorJump, true);
            drawContext.drawText(HorsePower.INSTANCE.getMc().textRenderer, "♥ " + health + " HP (" + Math.round(healthPercentage * 100) + "%)", x, y + 20, interpolatedColorHealth, true);

            double total = MathHelper.clamp(speedMovementPercentage, 0, 1) + MathHelper.clamp(jumpHeightPercentage, 0, 1) + MathHelper.clamp(healthPercentage, 0, 1);
            double average = total / 3;

            int interpolatedColorAverage = Color.HSBtoRGB(
                hsv1[0] + (float) (diff[0] * average),
                1f,
                hsv1[2] + (float) (diff[2] * average)
            );

            drawContext.drawText(HorsePower.INSTANCE.getMc().textRenderer, "Average: " + Math.round(average * 100) + "%", x, y + 30, interpolatedColorAverage, true);
        }
    }
}
