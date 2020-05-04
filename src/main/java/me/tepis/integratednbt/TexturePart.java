package me.tepis.integratednbt;

import net.minecraft.client.gui.GuiScreen;

/**
 * Represents a part in a texture; Offers help method for quick rendering
 */
public class TexturePart {
    private int x;
    private int y;
    private int width;
    private int height;

    public TexturePart(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void renderTo(GuiScreen gui, int x, int y) {
        gui.drawTexturedModalRect(x, y, this.x, this.y, this.width, this.height);
    }

    public void renderTo(ExtendedGuiContainer gui, int x, int y, int width, int height) {
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
