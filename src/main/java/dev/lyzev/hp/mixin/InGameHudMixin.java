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
import dev.lyzev.hp.util.HorseStatsRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.passive.AbstractHorseEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(DrawContext drawContext, RenderTickCounter renderTickCounter, CallbackInfo ci) {
        if (HorsePowerConfig.INSTANCE.getSHOW_HUD().getValue()) {
            final var entity = client.targetedEntity;
            if (entity instanceof final AbstractHorseEntity horse) {
                HorseStatsRenderer.INSTANCE.render(drawContext, horse, client.getWindow().getScaledWidth() / 2 + 10, client.getWindow().getScaledHeight() / 2 + 10, 0, 0);
            }
        }
    }
}
