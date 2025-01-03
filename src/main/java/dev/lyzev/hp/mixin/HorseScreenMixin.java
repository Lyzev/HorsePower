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
import dev.lyzev.hp.util.MathKt;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HorseScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

import static dev.lyzev.hp.util.MathKt.unit2bps;
import static dev.lyzev.hp.util.MathKt.unit2jump;

@Mixin(HorseScreen.class)
public class HorseScreenMixin {

    @Unique
    private static final Identifier BACKGROUND_TEXTURE = Identifier.of(HorsePower.MOD_ID, "textures/gui/container/background.png");

    @Unique
    private static final int WHITE = 0xFFFFFFFF;

    @Unique
    private static final Formatting[] FORMATTINGS = new Formatting[] { Formatting.DARK_RED, Formatting.RED, Formatting.GOLD, Formatting.YELLOW, Formatting.GREEN, Formatting.DARK_GREEN };

    @Shadow
    @Final
    private AbstractHorseEntity entity;

    @Inject(method = "drawBackground", at = @At("RETURN"))
    private void onDrawBackground(DrawContext drawContext, float f, int mouseX, int mouseY, CallbackInfo ci) {
        if (!HorsePowerConfig.INSTANCE.getSHOW_INVENTORY().getValue()) return;

        int imageWidth = 176;
        int imageHeight = 166;
        int x = HorsePower.INSTANCE.getMc().getWindow().getScaledWidth() / 2 + imageWidth / 2;
        int y = (HorsePower.INSTANCE.getMc().getWindow().getScaledHeight() - imageHeight) / 2;

        drawHorseAttributes(drawContext, x + 10, y + 5, mouseX, mouseY);
    }

    private void drawHorseAttributes(DrawContext drawContext, int x, int y, int mouseX, int mouseY) {
        double speed = MathKt.round(unit2bps(entity.getAttributeBaseValue(EntityAttributes.MOVEMENT_SPEED)), 1);
        double jump = MathKt.round(unit2jump(entity.getAttributeBaseValue(EntityAttributes.JUMP_STRENGTH)), 1);
        double health = MathKt.round(entity.getAttributeBaseValue(EntityAttributes.MAX_HEALTH), 1);

        double speedPercentage = getPercentage(entity.getAttributeBaseValue(EntityAttributes.MOVEMENT_SPEED), AbstractHorseEntity.MAX_MOVEMENT_SPEED_BONUS);
        double jumpPercentage = getPercentage(entity.getAttributeBaseValue(EntityAttributes.JUMP_STRENGTH), AbstractHorseEntity.MAX_JUMP_STRENGTH_BONUS);
        double healthPercentage = getPercentage(health, AbstractHorseEntity.MAX_HEALTH_BONUS);

        drawBackgroundBox(drawContext, x, y);

        drawAttribute(drawContext, "↔ ", speed, speedPercentage, unit2bps(AbstractHorseEntity.MIN_MOVEMENT_SPEED_BONUS), unit2bps(AbstractHorseEntity.MAX_MOVEMENT_SPEED_BONUS), x, y, 0, mouseX, mouseY);
        drawAttribute(drawContext, "↕ ", jump, jumpPercentage, unit2jump(AbstractHorseEntity.MIN_JUMP_STRENGTH_BONUS), unit2jump(AbstractHorseEntity.MAX_JUMP_STRENGTH_BONUS), x, y, 10, mouseX, mouseY);
        drawAttribute(drawContext, "♥ ", health, healthPercentage, AbstractHorseEntity.MIN_HEALTH_BONUS, AbstractHorseEntity.MAX_HEALTH_BONUS, x, y, 20, mouseX, mouseY);

        if (HorsePowerConfig.INSTANCE.getSHOW_AVERAGE().getValue()) {
            drawAverage(drawContext, speedPercentage, jumpPercentage, healthPercentage, x, y + 30);
        }
    }

    private void drawBackgroundBox(DrawContext drawContext, int x, int y) {
        drawContext.drawTexture(RenderLayer::getGuiTextured, BACKGROUND_TEXTURE, x - 5, y - 5, 0, 0, 126, 61, 256, 256);
    }

    private double getPercentage(double value, double maxValue) {
        return value / maxValue;
    }

    private void drawAttribute(DrawContext drawContext, String symbol, double value, double percentage, double minValue, double maxValue, int x, int y, int offsetY, int mouseX, int mouseY) {
        String text = buildAttributeText(symbol, value, percentage);
        Formatting formatting = getFormatting(percentage);
        drawContext.drawTextWithShadow(HorsePower.INSTANCE.getMc().textRenderer, Text.literal(text).formatted(formatting), x, y + offsetY, WHITE);

        if (isMouseHovering(mouseX, mouseY, x, y + offsetY, text)) {
            drawTooltip(drawContext, minValue, maxValue, mouseX, mouseY);
        }
    }

    private String buildAttributeText(String symbol, double value, double percentage) {
        StringBuilder text = new StringBuilder();
        text.append(symbol).append(value);
        if (HorsePowerConfig.INSTANCE.getSHOW_UNIT().getValue()) {
            text.append(getUnit(symbol));
        }
        if (HorsePowerConfig.INSTANCE.getSHOW_PERCENTAGE().getValue()) {
            text.append(" (").append(Math.round(percentage * 100)).append("%)");
        }
        return text.toString();
    }

    private String getUnit(String symbol) {
        switch (symbol) {
            case "↔ ":
                return " m/s";
            case "↕ ":
                return " blocks";
            default:
                return " HP";
        }
    }

    private Formatting getFormatting(double percentage) {
        return FORMATTINGS[Math.min(FORMATTINGS.length - 1, (int) Math.round(percentage * (FORMATTINGS.length - 1)))];
    }

    private boolean isMouseHovering(int mouseX, int mouseY, int x, int y, String text) {
        int textWidth = HorsePower.INSTANCE.getMc().textRenderer.getWidth(text);
        return mouseX >= x && mouseX <= x + textWidth && mouseY >= y && mouseY <= y + 9;
    }

    private void drawTooltip(DrawContext drawContext, double minValue, double maxValue, int mouseX, int mouseY) {
        var hoverText = new ArrayList<Text>();
        hoverText.add(Text.literal("Min: " + Math.round(minValue * 10) / 10.0).formatted(Formatting.DARK_RED));
        hoverText.add(Text.literal("Max: " + Math.round(maxValue * 10) / 10.0).formatted(Formatting.DARK_GREEN));
        drawContext.drawTooltip(HorsePower.INSTANCE.getMc().textRenderer, hoverText, mouseX, mouseY);
    }

    private void drawAverage(DrawContext drawContext, double speedPercentage, double jumpPercentage, double healthPercentage, int x, int y) {
        double average = calculateAverage(speedPercentage, jumpPercentage, healthPercentage);
        Formatting averageFormatting = getFormatting(average);
        drawContext.drawTextWithShadow(HorsePower.INSTANCE.getMc().textRenderer, Text.literal("Average: " + Math.round(average * 100) + "%").formatted(averageFormatting), x, y, WHITE);
    }

    private double calculateAverage(double speedPercentage, double jumpPercentage, double healthPercentage) {
        double total = MathHelper.clamp(speedPercentage, 0, 1) + MathHelper.clamp(jumpPercentage, 0, 1) + MathHelper.clamp(healthPercentage, 0, 1);
        return total / 3;
    }
}
