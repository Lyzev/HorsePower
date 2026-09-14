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

import dev.lyzev.hp.client.modmenu.HorsePowerConfig;
import dev.lyzev.hp.client.util.HorseStatsRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractMountInventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMountInventoryScreen.class)
public final class AbstractMountInventoryScreenMixin {
    @Shadow
    @Final
    protected LivingEntity mount;

    @Inject(method = "extractBackground", at = @At("RETURN"))
    private void onExtractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (HorsePowerConfig.INSTANCE.getSHOW_INVENTORY().getValue() && this.mount instanceof AbstractHorse horse) {
            int imageWidth = 176;
            int imageHeight = 166;
            int x = graphics.guiWidth() / 2 + imageWidth / 2;
            int y = (graphics.guiHeight() - imageHeight) / 2;
            HorseStatsRenderer.INSTANCE.render(graphics, horse, x + 10, y + 5, mouseX, mouseY);
        }
    }
}
