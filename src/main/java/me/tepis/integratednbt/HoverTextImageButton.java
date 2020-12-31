package me.tepis.integratednbt;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.util.List;
import java.util.stream.Collectors;

public class HoverTextImageButton extends ImageButton {
    private Screen gui;

    private List<ITextComponent> hoverText;

    public HoverTextImageButton(
        Screen gui,
        TexturePart textureNormal,
        TexturePart textureHover,
        int x,
        int y,
        Button.IPressable onPress
    ) {
        super(textureNormal, textureHover, x, y, onPress);
        this.gui = gui;
    }

    public HoverTextImageButton(Screen gui, int x, int y, Button.IPressable onPress) {
        super(x, y, onPress);
        this.gui = gui;
    }

    public void setHoverText(List<ITextComponent> hoverText) {
        this.hoverText = hoverText;
    }

    public void setHoverTextRaw(List<String> hoverText) {
        this.hoverText = hoverText.stream().map(StringTextComponent::new).collect(Collectors.toList());
    }

    /**
     * Draw hover text if is hovered
     */
    public void drawHover(MatrixStack matrixStack, int mouseX, int mouseY) {
        if (this.isHovered) {
            GuiUtils.drawHoveringText(
                matrixStack,
                this.hoverText,
                mouseX,
                mouseY,
                this.gui.width,
                this.gui.height,
                240,
                Minecraft.getInstance().fontRenderer // SensibleFontRenderer.get()
            );
        }
    }
}
