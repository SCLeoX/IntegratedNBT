package me.tepis.integratednbt;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Basically net.minecraft.client.gui.ImageButton, but more dynamic
 */
@OnlyIn(Dist.CLIENT)
public class ImageButton extends Button {
    private TexturePart textureNormal;
    private TexturePart textureHover;

    public ImageButton(
        TexturePart textureNormal,
        TexturePart textureHover,
        int x,
        int y,
        Button.OnPress onPress
    ) {
        super(x, y, textureNormal.getWidth(), textureNormal.getHeight(), Component.literal(""), onPress, DEFAULT_NARRATION);
        this.textureNormal = textureNormal;
        this.textureHover = textureHover;
    }

    /**
     * For lazy initialization of textures.
     */
    public ImageButton(int x, int y, Button.OnPress onPress) {
        super(x, y, 1, 1, Component.literal(""), onPress, DEFAULT_NARRATION);
    }

    public void setTexture(TexturePart textureNormal, TexturePart textureHover) {
        this.textureNormal = textureNormal;
        this.textureHover = textureHover;
        this.width = textureNormal.getWidth();
        this.height = textureNormal.getHeight();
    }

    /**
     * Draws this button to the screen.
     */
    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float wtf) {
        if (this.visible) {
            this.isHovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width &&
                mouseY < this.getY() + this.height;
            TexturePart texturePart = this.isHovered
                ? this.textureHover
                : this.textureNormal;
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
//            RenderSystem.disableDepthTest();
            texturePart.renderTo(guiGraphics, this.getX(), this.getY(), 0xffffff);
//            GlStateManager._enableDepthTest();
        }
    }
}
