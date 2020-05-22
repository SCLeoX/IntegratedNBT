package me.tepis.integratednbt;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.widget.button.Button;
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
        Button.IPressable onPress
    ) {
        super(x, y, textureNormal.getWidth(), textureNormal.getHeight(), "", onPress);
        this.textureNormal = textureNormal;
        this.textureHover = textureHover;
    }

    /**
     * For lazy initialization of textures.
     */
    public ImageButton(int x, int y, Button.IPressable onPress) {
        super(x, y, 1, 1, "", onPress);
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
    public void renderButton(int mouseX, int mouseY, float wtf) {
        if (this.visible) {
            RenderSystem.color3f(255, 255, 255);
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width &&
                mouseY < this.y + this.height;
            TexturePart texturePart = this.isHovered
                ? this.textureHover
                : this.textureNormal;
            GlStateManager.disableDepthTest();
            texturePart.renderTo(this, this.x, this.y);
            GlStateManager.enableDepthTest();
        }
    }
}
