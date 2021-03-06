/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2016
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.client.gui.buttons;

import mods.railcraft.client.render.tools.OpenGL;
import mods.railcraft.common.core.RailcraftConstants;
import mods.railcraft.common.gui.buttons.IButtonTextureSet;
import mods.railcraft.common.gui.tooltips.ToolTip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public abstract class GuiBetterButton<T extends GuiBetterButton<T>> extends GuiButton {

    private static final ResourceLocation TEXTURE = new ResourceLocation(RailcraftConstants.GUI_TEXTURE_FOLDER + "gui_basic.png");
    protected final IButtonTextureSet texture;
    private ToolTip toolTip;
    @Nullable
    private Consumer<T> clickConsumer;
    @Nullable
    private Consumer<T> statusUpdater;

    protected GuiBetterButton(int id, int x, int y, int width, IButtonTextureSet texture, String label) {
        super(id, x, y, width, texture.getHeight(), label);
        this.texture = texture;
    }

    public abstract T getThis();

    public T setClickConsumer(Consumer<T> clickConsumer) {
        this.clickConsumer = clickConsumer;
        return getThis();
    }

    public void consumeClick() {
        if (clickConsumer != null)
            clickConsumer.accept(getThis());
    }

    public T setStatusUpdater(Consumer<T> statusUpdater) {
        this.statusUpdater = statusUpdater;
        return getThis();
    }

    public void updateStatus() {
        if (statusUpdater != null)
            statusUpdater.accept(getThis());
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return texture.getHeight();
    }

    public int getTextColor(boolean mouseOver) {
        if (!enabled)
            return 0xffa0a0a0;
        else if (mouseOver)
            return 0xffffa0;
        else
            return 0xe0e0e0;
    }

    public boolean isMouseOverButton(int mouseX, int mouseY) {
        return mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + getHeight();
    }

    protected void bindButtonTextures(Minecraft minecraft) {
        minecraft.renderEngine.bindTexture(TEXTURE);
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
        if (!visible)
            return;
        FontRenderer fontrenderer = minecraft.fontRendererObj;
        bindButtonTextures(minecraft);
        OpenGL.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int xOffset = texture.getX();
        int yOffset = texture.getY();
        int h = texture.getHeight();
        int w = texture.getWidth();
        boolean mouseOver = isMouseOverButton(mouseX, mouseY);
        int hoverState = getHoverState(mouseOver);
        drawTexturedModalRect(xPosition, yPosition, xOffset, yOffset + hoverState * h, width / 2, h);
        drawTexturedModalRect(xPosition + width / 2, yPosition, xOffset + w - width / 2, yOffset + hoverState * h, width / 2, h);
        mouseDragged(minecraft, mouseX, mouseY);
        drawCenteredString(fontrenderer, displayString, xPosition + width / 2, yPosition + (h - 8) / 2, getTextColor(mouseOver));
    }

    @Nullable
    public ToolTip getToolTip() {
        return toolTip;
    }

    public void setToolTip(@Nullable ToolTip tips) {
        this.toolTip = tips;
    }

}
