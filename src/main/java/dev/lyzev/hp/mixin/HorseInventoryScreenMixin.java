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
import kotlin.math.MathKt;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HorseInventoryScreen.class)
public class HorseInventoryScreenMixin {

    @Shadow
    @Final
    private int inventoryColumns;

    @Shadow
    @Final
    private AbstractHorse horse;

    @Inject(method = "renderBg", at = @At("RETURN"))
    private void renderBg(GuiGraphics guiGraphics, float f, int i, int j, CallbackInfo ci) {
        // TODO: Add indicator to the horse inventory screen to show the attributes of the horse
        if (inventoryColumns > 0) {
        } else {
            int imageWidth = 176;
            int imageHeight = 166;
            int x = HorsePower.INSTANCE.getMc().getWindow().getGuiScaledWidth() / 2 - imageWidth / 2 + 82;
            int y = HorsePower.INSTANCE.getMc().getWindow().getGuiScaledHeight() / 2 - imageHeight / 2 + 20;
            AbstractHorse horse = this.horse;

            double speedMovement = Math.round(horse.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) * 100) / 100.0;
            double jumpHeight = Math.round(horse.getAttributeBaseValue(Attributes.JUMP_STRENGTH) * 100) / 100.0;
            double health = Math.round(horse.getAttributeBaseValue(Attributes.MAX_HEALTH) * 100) / 100.0;
            guiGraphics.drawString(HorsePower.INSTANCE.getMc().font, "Speed: " + speedMovement, x, y, 0xFFFFFF);
            guiGraphics.drawString(HorsePower.INSTANCE.getMc().font, "Jump: " + jumpHeight, x, y + 10, 0xFFFFFF);
            guiGraphics.drawString(HorsePower.INSTANCE.getMc().font, "Health: " + health, x, y + 20, 0xFFFFFF);
        }
    }
}
