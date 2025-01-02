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
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.lyzev.hp.util.MathKt.unit2bps;
import static dev.lyzev.hp.util.MathKt.unit2jump;

@Mixin(HorseScreen.class)
public class HorseScreenMixin {

    private static final int WHITE = 0xFFFFFFFF;

    private static final Formatting[] FORMATTINGS = new Formatting[] { Formatting.DARK_RED, Formatting.RED, Formatting.GOLD, Formatting.YELLOW, Formatting.GREEN, Formatting.DARK_GREEN };

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

            double speedMovementPercentage = horse.getAttributeBaseValue(EntityAttributes.MOVEMENT_SPEED) / AbstractHorseEntity.MAX_MOVEMENT_SPEED_BONUS;
            double jumpHeightPercentage = horse.getAttributeBaseValue(EntityAttributes.JUMP_STRENGTH) / AbstractHorseEntity.MAX_JUMP_STRENGTH_BONUS;
            double healthPercentage = health / AbstractHorseEntity.MAX_HEALTH_BONUS;

            var speedMovementFormatting = FORMATTINGS[Math.min(FORMATTINGS.length - 1, (int) Math.round(speedMovementPercentage * (FORMATTINGS.length - 1)))];
            var jumpHeightFormatting = FORMATTINGS[Math.min(FORMATTINGS.length - 1, (int) Math.round(jumpHeightPercentage * (FORMATTINGS.length - 1)))];
            var healthFormatting = FORMATTINGS[Math.min(FORMATTINGS.length - 1, (int) Math.round(healthPercentage * (FORMATTINGS.length - 1)))];

            var speedMovementText = new StringBuilder();
            speedMovementText.append("↔ ").append(speedMovement);
            if (HorsePowerConfig.INSTANCE.getSHOW_UNIT().getValue()) {
                speedMovementText.append(" m/s");
            }
            if (HorsePowerConfig.INSTANCE.getSHOW_PERCENTAGE().getValue()) {
                speedMovementText.append(" (").append(Math.round(speedMovementPercentage * 100)).append("%)");
            }
            drawContext.drawTextWithShadow(HorsePower.INSTANCE.getMc().textRenderer, Text.literal(speedMovementText.toString()).formatted(speedMovementFormatting), x, y, WHITE);

            var jumpHeightText = new StringBuilder();
            jumpHeightText.append("↕ ").append(jumpHeight);
            if (HorsePowerConfig.INSTANCE.getSHOW_UNIT().getValue()) {
                jumpHeightText.append(" blocks");
            }
            if (HorsePowerConfig.INSTANCE.getSHOW_PERCENTAGE().getValue()) {
                jumpHeightText.append(" (").append(Math.round(jumpHeightPercentage * 100)).append("%)");
            }
            drawContext.drawTextWithShadow(HorsePower.INSTANCE.getMc().textRenderer, Text.literal(jumpHeightText.toString()).formatted(jumpHeightFormatting), x, y + 10, WHITE);

            var healthText = new StringBuilder();
            healthText.append("♥ ").append(health);
            if (HorsePowerConfig.INSTANCE.getSHOW_UNIT().getValue()) {
                healthText.append(" HP");
            }
            if (HorsePowerConfig.INSTANCE.getSHOW_PERCENTAGE().getValue()) {
                healthText.append(" (").append(Math.round(healthPercentage * 100)).append("%)");
            }
            drawContext.drawTextWithShadow(HorsePower.INSTANCE.getMc().textRenderer, Text.literal(healthText.toString()).formatted(healthFormatting), x, y + 20, WHITE);

            if (!HorsePowerConfig.INSTANCE.getSHOW_AVERAGE().getValue()) {
                return;
            }

            double total = MathHelper.clamp(speedMovementPercentage, 0, 1) + MathHelper.clamp(jumpHeightPercentage, 0, 1) + MathHelper.clamp(healthPercentage, 0, 1);
            double average = total / 3;

            var averageFormatting = FORMATTINGS[Math.min(FORMATTINGS.length - 1, (int) Math.round(average * (FORMATTINGS.length - 1)))];

            drawContext.drawTextWithShadow(HorsePower.INSTANCE.getMc().textRenderer, Text.literal("Average: " + Math.round(average * 100) + "%").formatted(averageFormatting), x, y + 30, WHITE);
        }
    }
}
