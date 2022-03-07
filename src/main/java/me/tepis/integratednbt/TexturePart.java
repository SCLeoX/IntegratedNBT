package me.tepis.integratednbt;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;

/**
 * Represents a part in a texture; Offers help method for quick rendering
 */
public class TexturePart {
    private Texture texture;
    private int x;
    private int y;
    private int width;
    private int height;

    public TexturePart(Texture texture, int x, int y, int width, int height) {
        this.texture = texture;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void renderTo(GuiComponent gui, PoseStack matrixStack, int x, int y) {
        this.texture.bind();
        gui.blit(matrixStack, x, y, this.x, this.y, this.width, this.height);
    }

    private void setColorInt(int color) {
        RenderSystem.setShaderColor(
            (float) (color >> 16 & 255) / 255.0f,
            (float) (color >> 8 & 255) / 255.0f,
            (float) (color & 255) / 255.0F,
            1
        );
    }

    public void renderTo(GuiComponent gui, PoseStack matrixStack, int x, int y, int color) {
        this.setColorInt(color);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        this.renderTo(gui, matrixStack, x, y);
    }

    public void renderToScaled(ExtendedContainerScreen<?> gui, PoseStack matrixStack, int x, int y, int width, int height) {
        this.texture.bind();
        Matrix4f matrix = matrixStack.last().pose();
        gui.drawTexturedModalRectScalable(
            matrix,
            x,
            y,
            width == -1 ? this.width : width,
            height == -1 ? this.height : height,
            this.x,
            this.y,
            this.width,
            this.height
        );
    }

    public void renderToScaled(ExtendedContainerScreen<?> gui, PoseStack matrixStack, int x, int y, int width, int height, int color) {
        this.setColorInt(color);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        this.renderToScaled(gui, matrixStack, x, y, width, height);
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }
}
