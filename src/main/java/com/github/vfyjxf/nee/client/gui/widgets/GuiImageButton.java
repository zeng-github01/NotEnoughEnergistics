package com.github.vfyjxf.nee.client.gui.widgets;

import com.github.vfyjxf.nee.utils.Globals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public abstract class GuiImageButton extends GuiButton {
    public static final ResourceLocation STATES = new ResourceLocation(Globals.MOD_ID, "textures/gui/states.png");

    private final boolean halfSize;

    public GuiImageButton(int x, int y, int width, int height, boolean halfSize) {
        super(0, x, y, width, height, "");
        this.halfSize = halfSize;
    }

    @Override
    public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            GlStateManager.pushMatrix();
            GlStateManager.translate(this.x, this.y, 0.0F);
            if (halfSize) {
                GlStateManager.scale(0.5f, 0.5f, 0.5f);
            }
            if (this.enabled) {
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            } else {
                GlStateManager.color(0.5f, 0.5f, 0.5f, 1.0f);
            }
            GlStateManager.enableAlpha();
            this.drawImage(mc);
            this.mouseDragged(mc, mouseX, mouseY);
            GlStateManager.popMatrix();
        }
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public abstract void drawImage(Minecraft mc);

    public void drawTooltip(Minecraft mc, int mouseX, int mouseY) {

    }

    @Override
    public boolean mousePressed(@Nonnull Minecraft mc, int mouseX, int mouseY) {
        if (this.hovered) {
            return true;
        }
        return false;
    }
}
