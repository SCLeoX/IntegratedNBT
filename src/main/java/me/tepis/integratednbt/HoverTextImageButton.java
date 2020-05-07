package me.tepis.integratednbt;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiUtils;

import java.util.List;

public class HoverTextImageButton extends ImageButton {
    private GuiScreen gui;
    private List<String> hoverText;

    public HoverTextImageButton(
        GuiScreen gui,
        int buttonId,
        TexturePart textureNormal,
        TexturePart textureHover,
        int x,
        int y
    ) {
        super(buttonId, textureNormal, textureHover, x, y);
        this.gui = gui;
    }

    public HoverTextImageButton(GuiScreen gui, int buttonId, int x, int y) {
        super(buttonId, x, y);
        this.gui = gui;
    }

    public void setHoverText(List<String> hoverText) {
        this.hoverText = hoverText;
    }

    /**
     * Draw hover text if is hovered
     */
    public void drawHover(int mouseX, int mouseY) {
        if (this.hovered) {
            GuiUtils.drawHoveringText(
                this.hoverText,
                mouseX,
                mouseY,
                this.gui.width,
                this.gui.height,
                200,
                Minecraft.getMinecraft().fontRenderer
            );
        }
    }
}
