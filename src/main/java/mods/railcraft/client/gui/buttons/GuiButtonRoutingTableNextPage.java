/* 
 * Copyright (c) CovertJaguar, 2014 http://railcraft.info
 * 
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.client.gui.buttons;

import mods.railcraft.client.gui.GuiRoutingTable;
import mods.railcraft.client.render.tools.OpenGL;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiButtonRoutingTableNextPage extends GuiButton {

    /**
     * True for pointing right (next page), false for pointing left (previous
     * page).
     */
    private final boolean nextPage;

    public GuiButtonRoutingTableNextPage(int par1, int par2, int par3, boolean par4) {
        super(par1, par2, par3, 23, 13, "");
        this.nextPage = par4;
    }

    /**
     * Draws this button to the screen.
     */
    @Override
    public void drawButton(Minecraft par1Minecraft, int par2, int par3) {
        if (visible) {
            boolean flag = par2 >= xPosition && par3 >= yPosition && par2 < xPosition + width && par3 < yPosition + height;
            OpenGL.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            par1Minecraft.renderEngine.bindTexture(GuiRoutingTable.TEXTURE);
            int k = 0;
            int l = 192;

            if (flag) {
                k += 23;
            }

            if (!nextPage) {
                l += 13;
            }

            drawTexturedModalRect(xPosition, yPosition, k, l, 23, 13);
        }
    }
}
