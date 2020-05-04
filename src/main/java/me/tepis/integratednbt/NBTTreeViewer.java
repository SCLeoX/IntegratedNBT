package me.tepis.integratednbt;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

import static me.tepis.integratednbt.NBTExtractorGui.NBT_EXTRACTOR_GUI_TEXTURES;
import static me.tepis.integratednbt.NBTExtractorGui.SCREEN_EDGE;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTranslated;

public abstract class NBTTreeViewer {
    public static final ResourceLocation ONE_PIXEL_TEXTURE = new ResourceLocation(
        "integratednbt",
        "textures/gui/1x1.png"
    );
    private static final long SMOOTH_SCROLLING_TRANSITION_TIME_MS = 75;
    private static final double SCROLL_SPEED = 1d / 4;
    private static final int LINE_SPACE = 1;
    private static final int EXPAND_BUTTON_RIGHT_MARGIN = 3;
    private static final int INDENTATION = 10;
    private static final int HIGHLIGHT_COLOR = 0x505050; // Dark gray
    private static final int SELECTED_COLOR = 0x506850; // Lighter Dark gray
    private static final int LABEL_COLOR = 0xFFFF55; // Yellow
    private static final int EXPAND_COLOR = 0xFFBB11; // Orange
    private static final int NUMBER_COLOR = 0x66FFFF; // Cyan
    private static final int STRING_COLOR = 0x55FF55; // Green
    private static final int COMPLEX_COLOR = 0xAAAAAA; // Gray
    private static final int EMPTY_COLOR = 0x777777; // Gray
    private static final int SCREEN_BACKGROUND_COLOR = 0x303030;
    private static final int SCROLL_BAR_COLOR = 0xCCCCCC;
    private static final int SCROLL_BAR_PADDING = 2;
    private static final int SCROLL_BAR_WIDTH = 3;

    private static final int EXPAND_BUTTON_SIZE = 7;
    private static final TexturePart PLUS_BUTTON = new TexturePart(
        48,
        0,
        EXPAND_BUTTON_SIZE,
        EXPAND_BUTTON_SIZE
    );
    private static final TexturePart PLUS_BUTTON_HOVER = new TexturePart(
        48,
        9,
        EXPAND_BUTTON_SIZE,
        EXPAND_BUTTON_SIZE
    );
    private static final TexturePart MINUS_BUTTON = new TexturePart(
        57,
        0,
        EXPAND_BUTTON_SIZE,
        EXPAND_BUTTON_SIZE
    );
    private static final TexturePart MINUS_BUTTON_HOVER = new TexturePart(
        57,
        9,
        EXPAND_BUTTON_SIZE,
        EXPAND_BUTTON_SIZE
    );
    private static final TexturePart PURE_COLOR = new TexturePart(0, 0, 1, 1);

    private final Set<NBTPath> expandedPaths;
    private final Wrapper<Integer> scrollTop;
    private final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
    private ExtendedGuiContainer gui;
    private int left;
    private int top;
    private int width;
    private int height;
    private double renderScroll;
    private long renderScrollTransitionStartTime;
    private double renderScrollTransitionStartLocation;
    private int maxScroll = 0;
    private int currentY;
    private int currentX;
    private NBTPath currentPath;
    private NBTPath hoveringPath;
    private NBTBase hoveringNBTNode;
    private NBTPath selecting;
    /**
     * X coordinate of mouse in the screen
     */
    private int mouseX;
    /**
     * Y coordinate of mouse in the screen
     */
    private int mouseY;
    private NBTPath hoveringExpandableButton;

    public NBTTreeViewer(
        ExtendedGuiContainer gui,
        Set<NBTPath> expandedPaths,
        Wrapper<Integer> scrollTop
    ) {
        this.gui = gui;
        this.expandedPaths = expandedPaths;
        this.scrollTop = scrollTop;
        this.renderScroll = scrollTop.get();
    }

    public void updateBounds(int left, int top, int width, int height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
    }

    public void handleMouseInput() {
        int dWheel = Mouse.getDWheel();
        int newValue = (int) (this.scrollTop.get() - dWheel * SCROLL_SPEED);
        if (newValue < 0) {
            newValue = 0;
        } else if (newValue > this.maxScroll) {
            newValue = this.maxScroll;
        }
        if (this.scrollTop.get() != newValue) {
            this.scrollTop.set(newValue);
            this.startScrollTransition();
        }
    }

    private void startScrollTransition() {
        this.renderScrollTransitionStartLocation = this.renderScroll;
        this.renderScrollTransitionStartTime = System.currentTimeMillis();
    }

    private boolean renderKVPair(
        String label,
        String value,
        int valueColor
    ) {
        if (label.isEmpty()) {
            return false;
        }
        boolean isSelected = this.getSelectedPath().equals(this.currentPath);
        boolean isHovering = false;
        int bottomBoundary = this.currentY + this.fontRenderer.FONT_HEIGHT;
        boolean isThreeSideInBounds = ((this.mouseX >= 0) && (this.mouseX < this.width) &&
            (this.mouseY >= this.renderScroll) && (this.mouseY < this.renderScroll + this.height) &&
            (this.mouseY >= this.currentY) && (this.mouseY < bottomBoundary + LINE_SPACE) &&
            (this.mouseX >= this.currentX));
        if (isThreeSideInBounds || isSelected) {
            int rightBoundary =
                this.currentX + this.fontRenderer.getStringWidth(label + (
                    value.isEmpty()
                        ? ""
                        : (": " + value)
                ));
            if (isSelected || this.mouseX < rightBoundary) {
                Minecraft.getMinecraft()
                    .getTextureManager()
                    .bindTexture(ONE_PIXEL_TEXTURE);
                GlHelper.colorInt(isSelected ? SELECTED_COLOR : HIGHLIGHT_COLOR);
                if (isThreeSideInBounds && this.mouseX < rightBoundary) {
                    isHovering = true;
                }
                PURE_COLOR.renderTo(
                    this.gui,
                    this.currentX - 1,
                    this.currentY - 1,
                    rightBoundary - this.currentX + 1,
                    bottomBoundary - this.currentY + 1
                );
            }
        }
        if (value.isEmpty()) {
            this.fontRenderer.drawString(label, this.currentX, this.currentY, LABEL_COLOR);
        } else {
            int valueX = this.fontRenderer.drawString(
                label + ": ",
                this.currentX,
                this.currentY,
                LABEL_COLOR
            );
            this.fontRenderer.drawString(value, valueX, this.currentY, valueColor);
        }
        return isHovering;
    }

    private void renderExpandableButton(boolean expanded) {
        boolean hovering = (
            this.mouseX >= this.currentX &&
                this.mouseX < (this.currentX + EXPAND_BUTTON_SIZE) &&
                this.mouseY >= this.currentY &&
                this.mouseY < (this.currentY + PLUS_BUTTON.getHeight())
        );
        TexturePart part = expanded
            ? (hovering ? MINUS_BUTTON_HOVER : MINUS_BUTTON)
            : (hovering ? PLUS_BUTTON_HOVER : PLUS_BUTTON);
        if (hovering) {
            this.hoveringExpandableButton = this.currentPath.copy();
        }
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(NBT_EXTRACTOR_GUI_TEXTURES);
        GlHelper.colorInt(EXPAND_COLOR);
        part.renderTo(this.gui, this.currentX, this.currentY);
        this.currentX += EXPAND_BUTTON_SIZE + EXPAND_BUTTON_RIGHT_MARGIN;
    }

    public void render(NBTTagCompound nbt, int absMouseX, int absMouseY) {
        if (nbt == null) {
            return;
        }
        this.hoveringPath = null;
        this.hoveringNBTNode = null;
        this.hoveringExpandableButton = null;
        this.currentPath = new NBTPath();
        this.currentY = SCREEN_EDGE;
        this.updateScroll();
        this.mouseX = absMouseX - this.left;
        this.mouseY = (int) (absMouseY - this.top + this.renderScroll);
        glPushMatrix();
        try {
            glTranslated(this.left, this.top - this.renderScroll, 0);
            this.renderNode(I18n.format("integratednbt:nbt_extractor.root"), nbt);
            int totalHeight = this.currentY + SCREEN_EDGE;
            glTranslated(0, this.renderScroll, 0);
            this.maxScroll = Math.max(totalHeight - this.height, 0);
            if (this.maxScroll != 0) {
                Minecraft.getMinecraft()
                    .getTextureManager()
                    .bindTexture(ONE_PIXEL_TEXTURE);
                GlHelper.colorInt(SCREEN_BACKGROUND_COLOR);
                PURE_COLOR.renderTo(
                    this.gui,
                    this.width - SCROLL_BAR_PADDING * 2 - SCROLL_BAR_WIDTH,
                    0,
                    SCROLL_BAR_PADDING * 2 + SCROLL_BAR_WIDTH,
                    this.height
                );
                GlHelper.colorInt(SCROLL_BAR_COLOR);
                PURE_COLOR.renderTo(
                    this.gui,
                    this.width - SCROLL_BAR_PADDING - SCROLL_BAR_WIDTH,
                    SCROLL_BAR_PADDING + (int) (
                        this.renderScroll / totalHeight * (
                            this.height - SCROLL_BAR_PADDING * 2)),
                    SCROLL_BAR_WIDTH,
                    (int) (
                        Math.ceil(
                            (double) this.height / totalHeight * (
                                this.height - SCROLL_BAR_PADDING * 2)))
                );
            }
            if (this.hoveringPath != null) {
                IValueType<? extends IValue> hoveringValueType =
                    NBTValueConverter.mapNBTToValueType(this.hoveringNBTNode);
                ArrayList<String> list = new ArrayList<>(5);
                list.add(this.hoveringPath.getDisplayText());
                list.add(I18n.format(
                    "integratednbt:nbt_extractor.tooltip.nbt_type",
                    NBTBase.getTypeName(this.hoveringNBTNode.getId())
                ));
                list.add(I18n.format(
                    "integratednbt:nbt_extractor.tooltip.converted_type",
                    hoveringValueType.getDisplayColorFormat()
                        + I18n.format(hoveringValueType.getTranslationKey())
                ));
                list.add(I18n.format(
                    "integratednbt:nbt_extractor.tooltip.default_value",
                    NBTValueConverter.getDefaultValueDisplayText(this.hoveringNBTNode.getId())
                ));
                if (Objects.equals(this.getSelectedPath(), this.hoveringPath)) {
                    list.add(I18n.format("integratednbt:nbt_extractor.tooltip.selected"));
                } else if (Objects.equals(this.selecting, this.hoveringPath)) {
                    list.add(I18n.format("integratednbt:nbt_extractor.tooltip.selecting"));
                } else {
                    list.add(I18n.format("integratednbt:nbt_extractor.tooltip.left_click"));
                }
                if (isNodeExpandable(this.hoveringNBTNode)) {
                    if (this.expandedPaths.contains(this.hoveringPath)) {
                        list.add(I18n.format("integratednbt:nbt_extractor.tooltip"
                            + ".right_click_collapse"));
                    } else {
                        list.add(I18n.format(
                            "integratednbt:nbt_extractor.tooltip.right_click_expand"));
                    }
                }
                GuiUtils.drawHoveringText(
                    list,
                    this.mouseX,
                    (int) (this.mouseY - this.renderScroll),
                    this.width,
                    this.height,
                    200,
                    this.fontRenderer
                );
            }
        } finally {
            glPopMatrix();
        }
    }

    private void updateScroll() {
        long transitionTime = System.currentTimeMillis() - this.renderScrollTransitionStartTime;
        if (transitionTime > SMOOTH_SCROLLING_TRANSITION_TIME_MS) {
            // Transition ended
            this.renderScroll = this.scrollTop.get();
            return;
        }
        double ratio = (double) transitionTime / SMOOTH_SCROLLING_TRANSITION_TIME_MS;
        this.renderScroll = this.renderScrollTransitionStartLocation
            + (this.scrollTop.get() - this.renderScrollTransitionStartLocation) * ratio;
    }

    public void mouseClicked(int mouseButton) {
        if (mouseButton == 0) { // Left click
            if (this.hoveringExpandableButton != null) {
                this.toggleExpanded(this.hoveringExpandableButton);
            }
            if (this.hoveringPath != null) {
                this.selecting = this.hoveringPath;
                this.onUpdateSelectedPath(this.hoveringPath, this.hoveringNBTNode);
            }
        } else if (mouseButton == 1) { // Right click
            if (this.hoveringPath != null && isNodeExpandable(this.hoveringNBTNode)) {
                this.toggleExpanded(this.hoveringPath);
            }
        }
    }

    private void toggleExpanded(NBTPath path) {
        if (this.expandedPaths.contains(path)) {
            this.expandedPaths.remove(path);
        } else {
            this.expandedPaths.add(path.copy());
        }
    }

    public abstract void onUpdateSelectedPath(NBTPath newPath, NBTBase nbt);

    private static boolean isNodeExpandable(NBTBase nbt) {
        int nbtId = nbt.getId();
        return nbtId == 9 || nbtId == 10;
    }

    private void renderEmpty() {
        this.currentX = (this.currentPath.getDepth() + 1) * INDENTATION + SCREEN_EDGE
            + EXPAND_BUTTON_RIGHT_MARGIN + EXPAND_BUTTON_SIZE;
        this.fontRenderer.drawString(
            I18n.format("integratednbt:nbt_extractor.empty"),
            this.currentX,
            this.currentY,
            EMPTY_COLOR
        );
        this.currentY += this.fontRenderer.FONT_HEIGHT + LINE_SPACE;
    }

    private void renderNode(String label, NBTBase node) {
        this.currentX = this.currentPath.getDepth() * INDENTATION + SCREEN_EDGE;
        boolean isExpandedIfExpandable = false;
        if (isNodeExpandable(node)) {
            isExpandedIfExpandable = this.expandedPaths.contains(this.currentPath);
        } else {
            this.currentX += EXPAND_BUTTON_RIGHT_MARGIN + EXPAND_BUTTON_SIZE;
        }

        boolean isHoveringText;

        // Render Value
        switch (node.getId()) {
            case 1: // Byte
                isHoveringText = this.renderKVPair(
                    label,
                    String.valueOf(((NBTTagByte) node).getByte()),
                    NUMBER_COLOR
                );
                break;
            case 2: // Short
                isHoveringText = this.renderKVPair(
                    label,
                    String.valueOf(((NBTTagShort) node).getShort()),
                    NUMBER_COLOR
                );
                break;
            case 3: // Int
                isHoveringText = this.renderKVPair(
                    label,
                    String.valueOf(((NBTTagInt) node).getInt()),
                    NUMBER_COLOR
                );
                break;
            case 4: // Long
                isHoveringText = this.renderKVPair(
                    label,
                    String.valueOf(((NBTTagLong) node).getLong()),
                    NUMBER_COLOR
                );
                break;
            case 5: // Float
                isHoveringText = this.renderKVPair(
                    label,
                    String.valueOf(((NBTTagFloat) node).getFloat()),
                    NUMBER_COLOR
                );
                break;
            case 6: // Double
                isHoveringText = this.renderKVPair(
                    label,
                    String.valueOf(((NBTTagDouble) node).getDouble()),
                    NUMBER_COLOR
                );
                break;
            case 7: // Byte Array
            case 11: // Int Array
            case 12: // Long Array
                isHoveringText = this.renderKVPair(
                    label,
                    "[]",
                    NUMBER_COLOR
                );
                break;
            case 8: // String
                isHoveringText = this.renderKVPair(
                    label,
                    // Yes, I understand technically we should escape double quotes in the string.
                    // However, I think that will just make it confusing.
                    '"' + ((NBTTagString) node).getString() + '"',
                    STRING_COLOR
                );
                break;
            case 9: // List
            case 10: { // Compound
                this.renderExpandableButton(isExpandedIfExpandable);
                isHoveringText = this.renderKVPair(
                    label,
                    (isExpandedIfExpandable ? "" : node.toString()),
                    COMPLEX_COLOR
                );
                break;
            }
            default:
                throw new RuntimeException("Unexpected NBT id:" + node.getId());
        }
        if (isHoveringText) {
            this.hoveringPath = this.currentPath.copy();
            this.hoveringNBTNode = node;
        }
        this.currentY += this.fontRenderer.FONT_HEIGHT + LINE_SPACE;
        if (isExpandedIfExpandable) {
            // Recursive calls
            switch (node.getId()) {
                case 9: { // List
                    if (((NBTTagList) node).tagCount() == 0) {
                        this.renderEmpty();
                        break;
                    }
                    int i = 0;
                    for (NBTBase item : (NBTTagList) node) {
                        this.currentPath.pushIndex(i);
                        this.renderNode("#" + i, item);
                        this.currentPath.pop();
                        i++;
                    }
                    break;
                }
                case 10: { // Compound
                    if (((NBTTagCompound) node).getSize() == 0) {
                        this.renderEmpty();
                        break;
                    }
                    NBTTagCompound compound = (NBTTagCompound) node;
                    for (String key : compound.getKeySet()) {
                        this.currentPath.pushKey(key);
                        this.renderNode(key, compound.getTag(key));
                        this.currentPath.pop();
                    }
                    break;
                }
            }
        }
    }

    public abstract NBTPath getSelectedPath();
}
