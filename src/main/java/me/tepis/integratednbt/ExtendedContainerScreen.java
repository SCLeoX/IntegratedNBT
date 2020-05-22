package me.tepis.integratednbt;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.text.ITextComponent;

public abstract class ExtendedContainerScreen<T extends Container> extends ContainerScreen<T> {
    public ExtendedContainerScreen(
        T screenContainer,
        PlayerInventory inv,
        ITextComponent titleIn
    ) {
        super(screenContainer, inv, titleIn);
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
        bufferbuilder.pos(destX, destY + destHeight, 0)
            .tex((float) (srcX) * 0.00390625F, (float) (srcY + srcHeight) * 0.00390625F)
            .endVertex();
        bufferbuilder.pos(destX + destWidth, destY + destHeight, 0)
            .tex((float) (srcX + srcWidth) * 0.00390625F, (float) (srcY + srcHeight) * 0.00390625F)
            .endVertex();
        bufferbuilder.pos(destX + destWidth, destY, 0)
            .tex((float) (srcX + srcWidth) * 0.00390625F, (float) (srcY) * 0.00390625F)
            .endVertex();
        bufferbuilder.pos(destX, destY, 0)
            .tex((float) (srcX) * 0.00390625F, (float) (srcY) * 0.00390625F)
            .endVertex();
        tessellator.draw();
    }
}
