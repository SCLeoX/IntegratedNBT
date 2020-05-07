package me.tepis.integratednbt;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator.ValueOperator;

import javax.annotation.Nonnull;

/**
 * Basically net.minecraft.client.gui.ImageButton, but more dynamic
 */
@SideOnly(Side.CLIENT)
public class ImageButton extends GuiButton {
    private TexturePart textureNormal;
    private TexturePart textureHover;

    public ImageButton(
        int buttonId,
        TexturePart textureNormal,
        TexturePart textureHover,
        int x,
        int y
    ) {
        super(buttonId, x, y, "");
        this.textureNormal = textureNormal;
        this.textureHover = textureHover;
        this.width = textureNormal.getWidth();
        this.height = textureNormal.getHeight();
    }

    /**
     * For lazy initialization of textures.
     */
    public ImageButton(int buttonId, int x, int y) {
        super(buttonId, x, y, "");
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
    public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            GlStateManager.color(255, 255, 255);
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width &&
                mouseY < this.y + this.height;
            TexturePart texturePart = this.hovered
                ? this.textureHover
                : this.textureNormal;
            GlStateManager.disableDepth();
            texturePart.renderTo(this, this.x, this.y);
            GlStateManager.enableDepth();
        }
    }
}
