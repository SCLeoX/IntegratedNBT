package me.tepis.integratednbt;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import java.util.List;
import java.util.stream.Collectors;

public class HoverTextImageButton extends ImageButton {
    private Screen gui;

    private List<Component> hoverText;

    public HoverTextImageButton(
        Screen gui,
        TexturePart textureNormal,
        TexturePart textureHover,
        int x,
        int y,
        Button.OnPress onPress
    ) {
        super(textureNormal, textureHover, x, y, onPress);
        this.gui = gui;
    }

    public HoverTextImageButton(Screen gui, int x, int y, Button.OnPress onPress) {
        super(x, y, onPress);
        this.gui = gui;
    }

    public void setHoverText(List<Component> hoverText) {
        this.hoverText = hoverText;
    }

    public void setHoverTextRaw(List<String> hoverText) {
        this.hoverText = hoverText.stream()
            .map(TextComponent::new)
            .collect(Collectors.toList());
    }

    /**
     * Draw hover text if is hovered
     */
    public void drawHover(PoseStack matrixStack, int mouseX, int mouseY) {
        if (this.isHovered) {
            this.gui.renderTooltip(
                matrixStack,
                FontHelper.wrap(this.hoverText, 200),
                mouseX,
                mouseY
            );
        }
    }
}
