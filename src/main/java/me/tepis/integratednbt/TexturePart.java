package me.tepis.integratednbt;

import net.minecraft.client.gui.AbstractGui;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

/**
 * Represents a part in a texture; Offers help method for quick rendering
 */
@OnlyIn(Dist.CLIENT)
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

    public void renderTo(AbstractGui gui, int x, int y) {
        this.texture.bind();
        gui.blit(x, y, this.x, this.y, this.width, this.height);
    }

    public void renderTo(ExtendedContainerScreen<?> gui, int x, int y, int width, int height) {
        this.texture.bind();
        gui.drawTexturedModalRectScalable(
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

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }
}
