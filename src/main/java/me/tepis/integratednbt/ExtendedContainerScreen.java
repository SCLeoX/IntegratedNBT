package me.tepis.integratednbt;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

public abstract class ExtendedContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    public ExtendedContainerScreen(
        T screenContainer,
        Inventory inv,
        Component titleIn
    ) {
        super(screenContainer, inv, titleIn);
    }

    public void drawTexturedModalRectScalable(
        Matrix4f matrix,
        int destX, int destY,
        int destWidth, int destHeight,
        int srcX, int srcY,
        int srcWidth, int srcHeight
    ) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(matrix, destX, destY + destHeight, 0)
            .uv((float) (srcX) * 0.00390625F, (float) (srcY + srcHeight) * 0.00390625F)
            .endVertex();
        bufferbuilder.vertex(matrix, destX + destWidth, destY + destHeight, 0)
            .uv((float) (srcX + srcWidth) * 0.00390625F, (float) (srcY + srcHeight) * 0.00390625F)
            .endVertex();
        bufferbuilder.vertex(matrix, destX + destWidth, destY, 0)
            .uv((float) (srcX + srcWidth) * 0.00390625F, (float) (srcY) * 0.00390625F)
            .endVertex();
        bufferbuilder.vertex(matrix, destX, destY, 0)
            .uv((float) (srcX) * 0.00390625F, (float) (srcY) * 0.00390625F)
            .endVertex();
        tesselator.end();
    }

    public void drawSplitString(PoseStack matrixStack, Font fontRenderer, FormattedText text, int x, int y, int maxLength, int color) {
        for(FormattedCharSequence ireorderingprocessor : fontRenderer.split(text, maxLength)) {
            fontRenderer.drawShadow(matrixStack, ireorderingprocessor, (float) x, (float) y, color);
            y += 9;
        }

    }
}
