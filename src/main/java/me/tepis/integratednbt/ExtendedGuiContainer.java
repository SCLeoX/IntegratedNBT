package me.tepis.integratednbt;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.Container;

public abstract class ExtendedGuiContainer extends GuiContainer {
    public ExtendedGuiContainer(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }

    public void drawTexturedModalRectScalable(
        int destX, int destY,
        int destWidth, int destHeight,
        int srcX, int srcY,
        int srcWidth, int srcHeight
    ) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(destX, destY + destHeight, this.zLevel)
            .tex((float) (srcX) * 0.00390625F, (float) (srcY + srcHeight) * 0.00390625F)
            .endVertex();
        bufferbuilder.pos(destX + destWidth, destY + destHeight, this.zLevel)
            .tex((float) (srcX + srcWidth) * 0.00390625F, (float) (srcY + srcHeight) * 0.00390625F)
            .endVertex();
        bufferbuilder.pos(destX + destWidth, destY, this.zLevel)
            .tex((float) (srcX + srcWidth) * 0.00390625F, (float) (srcY) * 0.00390625F)
            .endVertex();
        bufferbuilder.pos(destX, destY, this.zLevel)
            .tex((float) (srcX) * 0.00390625F, (float) (srcY) * 0.00390625F)
            .endVertex();
        tessellator.draw();
    }
}
