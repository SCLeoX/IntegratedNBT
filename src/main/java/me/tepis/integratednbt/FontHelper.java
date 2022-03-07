package me.tepis.integratednbt;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.stream.Collectors;

public abstract class FontHelper {
    public static List<FormattedCharSequence> wrap(List<Component> components, int lineWidth) {
        Font font = Minecraft.getInstance().font;
        return components
            .stream()
            .flatMap(component -> font.split(component, lineWidth).stream())
            .collect(Collectors.toList());
    }
}
