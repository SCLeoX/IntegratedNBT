package me.tepis.integratednbt;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Represents a part in a texture; Offers help method for quick rendering
 */
@SideOnly(Side.CLIENT)
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

    public void renderTo(Gui gui, int x, int y) {
        this.texture.bind();
        gui.drawTexturedModalRect(x, y, this.x, this.y, this.width, this.height);
    }

    public void renderTo(ExtendedGuiContainer gui, int x, int y, int width, int height) {
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
