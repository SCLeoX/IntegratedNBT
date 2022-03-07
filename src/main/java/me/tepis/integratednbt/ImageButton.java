package me.tepis.integratednbt;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

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
        super(x, y, textureNormal.getWidth(), textureNormal.getHeight(), new TextComponent(""), onPress);
        this.textureNormal = textureNormal;
        this.textureHover = textureHover;
    }

    /**
     * For lazy initialization of textures.
     */
    public ImageButton(int x, int y, Button.OnPress onPress) {
        super(x, y, 1, 1, new TextComponent(""), onPress);
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
    public void renderButton(PoseStack matrixStack, int mouseX, int mouseY, float wtf) {
        if (this.visible) {
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width &&
                mouseY < this.y + this.height;
            TexturePart texturePart = this.isHovered
                ? this.textureHover
                : this.textureNormal;
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
//            RenderSystem.disableDepthTest();
            texturePart.renderTo(this, matrixStack, this.x, this.y, 0xffffff);
//            GlStateManager._enableDepthTest();
        }
    }
}
