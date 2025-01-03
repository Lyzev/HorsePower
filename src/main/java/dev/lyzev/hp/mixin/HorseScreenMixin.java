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

import dev.lyzev.hp.client.HorsePowerClient;
import dev.lyzev.hp.client.modmenu.HorsePowerConfig;
import dev.lyzev.hp.client.util.HorseStatsRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HorseScreen;
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
    private void onDrawBackground(DrawContext drawContext, float f, int mouseX, int mouseY, CallbackInfo ci) {
        if (!HorsePowerConfig.INSTANCE.getSHOW_INVENTORY().getValue()) return;

        var imageWidth = 176;
        var imageHeight = 166;
        var x = HorsePowerClient.INSTANCE.getMc().getWindow().getScaledWidth() / 2 + imageWidth / 2;
        var y = (HorsePowerClient.INSTANCE.getMc().getWindow().getScaledHeight() - imageHeight) / 2;

        HorseStatsRenderer.INSTANCE.render(drawContext, entity, x + 10, y + 5, mouseX, mouseY);
    }
}
