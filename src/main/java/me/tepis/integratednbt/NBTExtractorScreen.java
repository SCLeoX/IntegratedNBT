package me.tepis.integratednbt;

import me.tepis.integratednbt.network.PacketHandler;
import me.tepis.integratednbt.network.clientbound.NBTExtractorUpdateClientMessage.ErrorCode;
import me.tepis.integratednbt.network.serverbound.NBTExtractorUpdateAutoRefreshMessage;
import me.tepis.integratednbt.network.serverbound.NBTExtractorUpdateExtractionPathMessage;
import me.tepis.integratednbt.network.serverbound.NBTExtractorUpdateOutputModeMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.nbt.INBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Arrays;

import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glScaled;
import static org.lwjgl.opengl.GL11.glScissor;
import static org.lwjgl.opengl.GL11.glTranslated;

@OnlyIn(Dist.CLIENT)
public class NBTExtractorScreen extends ExtendedContainerScreen<NBTExtractorContainer> {
    public static final int SCREEN_EDGE = 4;
    public static final Texture GUI_TEXTURE = new Texture(
        "integratednbt",
        "textures/gui/nbt_extractor.png"
    );
    // Different parts of the texture; See texture file for definitions
    private static final TexturePart PART0 = GUI_TEXTURE.createPart(0, 0, 8, 24);
    private static final TexturePart PART1 = GUI_TEXTURE.createPart(12, 0, 4, 24);
    private static final TexturePart PART2 = GUI_TEXTURE.createPart(20, 0, 8, 24);
    private static final TexturePart PART3 = GUI_TEXTURE.createPart(0, 28, 8, 4);
    private static final TexturePart PART4 = GUI_TEXTURE.createPart(12, 28, 4, 4);
    private static final TexturePart PART5 = GUI_TEXTURE.createPart(20, 28, 8, 4);
    private static final TexturePart PART6 = GUI_TEXTURE.createPart(0, 36, 8, 8);
    private static final TexturePart PART7 = GUI_TEXTURE.createPart(12, 36, 4, 8);
    private static final TexturePart PART8 = GUI_TEXTURE.createPart(20, 36, 178, 110);
    private static final TexturePart PART9 = GUI_TEXTURE.createPart(202, 36, 8, 8);
    private static final int BUTTON_SIZE = 12;
    private static final TexturePart BUTTON_UNKNOWN = GUI_TEXTURE.createPart(
        78,
        0,
        BUTTON_SIZE,
        BUTTON_SIZE
    );
    private static final TexturePart BUTTON_UNKNOWN_HOVER = GUI_TEXTURE.createPart(
        78,
        12,
        BUTTON_SIZE,
        BUTTON_SIZE
    );
    private static final TexturePart BUTTON_REFERENCE_MODE = GUI_TEXTURE.createPart(
        90,
        0,
        BUTTON_SIZE,
        BUTTON_SIZE
    );
    private static final TexturePart BUTTON_REFERENCE_MODE_HOVER = GUI_TEXTURE.createPart(
        90,
        12,
        BUTTON_SIZE,
        BUTTON_SIZE
    );
    private static final TexturePart BUTTON_OPERATOR_MODE = GUI_TEXTURE.createPart(
        102,
        0,
        BUTTON_SIZE,
        BUTTON_SIZE
    );
    private static final TexturePart BUTTON_OPERATOR_MODE_HOVER = GUI_TEXTURE.createPart(
        102,
        12,
        BUTTON_SIZE,
        BUTTON_SIZE
    );
    private static final TexturePart BUTTON_VALUE_MODE = GUI_TEXTURE.createPart(
        114,
        0,
        BUTTON_SIZE,
        BUTTON_SIZE
    );
    private static final TexturePart BUTTON_VALUE_MODE_HOVER = GUI_TEXTURE.createPart(
        114,
        12,
        BUTTON_SIZE,
        BUTTON_SIZE
    );
    private static final TexturePart BUTTON_REFRESH_ON = GUI_TEXTURE.createPart(
        126,
        0,
        BUTTON_SIZE,
        BUTTON_SIZE
    );
    private static final TexturePart BUTTON_REFRESH_ON_HOVER = GUI_TEXTURE.createPart(
        126,
        12,
        BUTTON_SIZE,
        BUTTON_SIZE
    );
    private static final TexturePart BUTTON_REFRESH_OFF = GUI_TEXTURE.createPart(
        138,
        0,
        BUTTON_SIZE,
        BUTTON_SIZE
    );
    private static final TexturePart BUTTON_REFRESH_OFF_HOVER = GUI_TEXTURE.createPart(
        138,
        12,
        BUTTON_SIZE,
        BUTTON_SIZE
    );
    private static final int BASE_PADDING = 200;
    private static final int INVENTORY_WIDTH = 178;
    private static final int INVENTORY_HEIGHT = 110;
    private static final int TOP_BORDER_SIZE = 24;
    private static final int SIDE_BORDER_SIZE = 8;
    private static final double CENTERED_TEXT_MAX_RATIO = 0.8;
    private static final int BUTTON_SPACING = 2;

    // These are static because GUI sometimes after receiving the update packets.
    private static NBTExtractorScreen lastInstance = null;
    // Null signify that the first update packet has not arrived yet.
    private static ErrorCode errorCode = null;
    private static INBT nbt;
    private static NBTPath extractionPath = null;
    private static NBTExtractorOutputMode outputMode = null;
    private static ITextComponent errorMessage = null;
    private static Boolean autoRefresh = null;

    private NBTTreeViewer treeViewer;
    private NBTExtractorContainer nbtExtractorContainer;
    private FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
    /**
     * Padding outside the GUI; Responsive; Updated by updateCalculations
     */
    private int padding;
    /**
     * The width of NBT screen; Responsive; Updated by updateCalculations
     */
    private int screenWidth;
    /**
     * The height of NBT screen; Responsive; Updated by updateCalculations
     */
    private int screenHeight;
    /**
     * The scale factor of Minecraft; Updated by updateCalculations
     */
    private double scaleFactor;
    private HoverTextImageButton outputModeButton;
    private HoverTextImageButton autoRefreshButton;

    public NBTExtractorScreen(
        NBTExtractorContainer screenContainer,
        PlayerInventory inventory,
        ITextComponent title
    ) {
        super(screenContainer, inventory, title);
        NBTExtractorScreen.lastInstance = this;
        this.nbtExtractorContainer = screenContainer;
        NBTExtractorTileEntity tileEntity = this.nbtExtractorContainer.getNbtExtractorEntity();
        this.treeViewer = new NBTTreeViewer(
            this,
            tileEntity.getExpandedPaths(),
            tileEntity.getScrollTop()
        ) {
            @Override
            public void onUpdateSelectedPath(NBTPath newPath, INBT nbt) {
                PacketHandler.INSTANCE.send(
                    PacketDistributor.SERVER.noArg(),
                    new NBTExtractorUpdateExtractionPathMessage(
                        NBTExtractorScreen.this.nbtExtractorContainer.getNbtExtractorEntity()
                            .getPos(),
                        newPath,
                        nbt.getId()
                    )
                );
            }

            @Override
            public NBTPath getSelectedPath() {
                return extractionPath;
            }
        };
    }

    public static void updateError(ErrorCode errorCode) {
        NBTExtractorScreen.errorCode = errorCode;
    }

    public static void updateNBT(INBT nbt) {
        NBTExtractorScreen.nbt = nbt;
    }

    public static void updateExtractionPath(NBTPath extractionPath) {
        NBTExtractorScreen.extractionPath = extractionPath;
    }

    public static void updateOutputMode(NBTExtractorOutputMode outputMode) {
        NBTExtractorScreen.outputMode = outputMode;
        if (lastInstance != null) {
            lastInstance.updateOutputModeButton();
        }
    }

    private void updateOutputModeButton() {
        if (this.outputModeButton == null) {
            return;
        }
        ArrayList<String> messages = new ArrayList<>();
        if (outputMode == null) {
            this.outputModeButton.setTexture(
                BUTTON_UNKNOWN,
                BUTTON_UNKNOWN_HOVER
            );
            messages.add(I18n.format(
                "integratednbt:nbt_extractor.output_mode",
                I18n.format("integratednbt:nbt_extractor.loading")
            ));
        } else if (outputMode == NBTExtractorOutputMode.REFERENCE) {
            this.outputModeButton.setTexture(
                BUTTON_REFERENCE_MODE,
                BUTTON_REFERENCE_MODE_HOVER
            );
            messages.add(I18n.format(
                "integratednbt:nbt_extractor.output_mode",
                I18n.format("integratednbt:nbt_extractor.output_mode.reference")
            ));
        } else if (outputMode == NBTExtractorOutputMode.OPERATOR) {
            this.outputModeButton.setTexture(
                BUTTON_OPERATOR_MODE,
                BUTTON_OPERATOR_MODE_HOVER
            );
            messages.add(I18n.format(
                "integratednbt:nbt_extractor.output_mode",
                I18n.format("integratednbt:nbt_extractor.output_mode.operator")
            ));
        } else {
            this.outputModeButton.setTexture(
                BUTTON_VALUE_MODE,
                BUTTON_VALUE_MODE_HOVER
            );
            messages.add(I18n.format(
                "integratednbt:nbt_extractor.output_mode",
                I18n.format("integratednbt:nbt_extractor.output_mode.value")
            ));
        }
        messages.addAll(Arrays.asList(I18n.format(
            "integratednbt:nbt_extractor.output_mode.description").split("\\\\n")));
        this.outputModeButton.setHoverText(messages);
    }

    public static void updateErrorMessage(ITextComponent errorMessage) {
        NBTExtractorScreen.errorMessage = errorMessage;
    }

    public static void updateAutoRefresh(Boolean autoRefresh) {
        NBTExtractorScreen.autoRefresh = autoRefresh;
        if (lastInstance != null) {
            lastInstance.updateAutoRefreshButton();
        }
    }

    private void updateAutoRefreshButton() {
        if (this.autoRefreshButton == null) {
            return;
        }
        ArrayList<String> messages = new ArrayList<>();
        if (autoRefresh == null) {
            this.autoRefreshButton.setTexture(
                BUTTON_UNKNOWN,
                BUTTON_UNKNOWN_HOVER
            );
            messages.add(I18n.format(
                "integratednbt:nbt_extractor.auto_refresh",
                I18n.format("integratednbt:nbt_extractor.loading")
            ));
        } else if (autoRefresh) {
            this.autoRefreshButton.setTexture(
                BUTTON_REFRESH_ON,
                BUTTON_REFRESH_ON_HOVER
            );
            messages.add(I18n.format(
                "integratednbt:nbt_extractor.auto_refresh",
                I18n.format("integratednbt:nbt_extractor.auto_refresh.on")
            ));
        } else {
            this.autoRefreshButton.setTexture(
                BUTTON_REFRESH_OFF,
                BUTTON_REFRESH_OFF_HOVER
            );
            messages.add(I18n.format(
                "integratednbt:nbt_extractor.auto_refresh",
                I18n.format("integratednbt:nbt_extractor.auto_refresh.off")
            ));
        }
        messages.addAll(Arrays.asList(I18n.format(
            "integratednbt:nbt_extractor.auto_refresh.description").split("\\\\n")));
        this.autoRefreshButton.setHoverText(messages);
    }

    @Override
    public boolean mouseClicked(
        double mouseX,
        double mouseY,
        int mouseButton
    ) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.treeViewer.mouseClicked(mouseButton);
        return true;
    }

    @Override
    protected void init() {
        this.updateCalculations();
        this.xSize = this.width - 2 * this.padding;
        this.ySize = this.height - 2 * this.padding;
        super.init();
        this.nbtExtractorContainer.setSlotOffset(
            (this.xSize - INVENTORY_WIDTH) / 2,
            this.ySize - INVENTORY_HEIGHT
        );
        this.treeViewer.updateBounds(
            this.padding + SIDE_BORDER_SIZE,
            this.padding + TOP_BORDER_SIZE,
            this.screenWidth,
            this.screenHeight
        );
        this.outputModeButton = new HoverTextImageButton(
            this,
            this.width - this.padding - 7 - BUTTON_SIZE,
            this.padding + 7,
            this::onOutputModeButtonClick
        );
        this.updateOutputModeButton();
        this.addButton(this.outputModeButton);
        this.autoRefreshButton = new HoverTextImageButton(
            this,
            this.width - this.padding - 7 - BUTTON_SIZE * 2 - BUTTON_SPACING,
            this.padding + 7,
            this::onAutoRefreshButtonClick
        );
        this.updateAutoRefreshButton();
        this.addButton(this.autoRefreshButton);
    }

    /**
     * Update
     */
    private void updateCalculations() {
        this.scaleFactor = Minecraft.getInstance().getMainWindow().getGuiScaleFactor();
        this.padding = (int) Math.min(
            Math.max(BASE_PADDING / Math.pow(this.scaleFactor, 3), 4),
            Math.min(this.width, this.height) / 10.
        );
        this.screenWidth = this.width - 2 * this.padding - 2 * SIDE_BORDER_SIZE;
        this.screenHeight = this.height - 2 * this.padding - TOP_BORDER_SIZE - INVENTORY_HEIGHT;
    }

    public void onOutputModeButtonClick(Button ignored) {
        if (outputMode == null) {
            return;
        }
        PacketHandler.INSTANCE.send(
            PacketDistributor.SERVER.noArg(),
            new NBTExtractorUpdateOutputModeMessage(
                this.nbtExtractorContainer.getNbtExtractorEntity().getPos(),
                NBTExtractorOutputMode.values()[(outputMode.ordinal() + 1) %
                    NBTExtractorOutputMode.values().length]
            )
        );
    }

    public void onAutoRefreshButtonClick(Button ignored) {
        if (autoRefresh == null) {
            return;
        }
        PacketHandler.INSTANCE.send(
            PacketDistributor.SERVER.noArg(),
            new NBTExtractorUpdateAutoRefreshMessage(
                this.nbtExtractorContainer.getNbtExtractorEntity().getPos(),
                !autoRefresh
            )
        );
    }

    @Override
    public boolean mouseScrolled(
        double mouseX,
        double mouseY,
        double dWheel
    ) {
        super.mouseScrolled(mouseX, mouseY, dWheel);
        if (errorCode == ErrorCode.NO_ERROR && nbt != null) {
            this.treeViewer.mouseScrolled(dWheel);
        }
        return true;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.outputModeButton.drawHover(mouseX, mouseY);
        this.autoRefreshButton.drawHover(mouseX, mouseY);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        this.renderGuiParts();
        this.fontRenderer.drawString(
            I18n.format("block.integratednbt.nbt_extractor"),
            this.padding + 8,
            this.padding + 9,
            4210752
        );
        // Scissor test allows restricting rendering to a rectangular portion of the screen.
        // In this case, we only want to render in the screen area of the NBT Extractor.
        glEnable(GL_SCISSOR_TEST);
        glScissor(
            (int) this.scaleFactor * (this.padding + SIDE_BORDER_SIZE),
            (int) this.scaleFactor * (this.padding + INVENTORY_HEIGHT),
            (int) this.scaleFactor * this.screenWidth,
            (int) this.scaleFactor * this.screenHeight
        );
        Slot srcNBTSlot = this.nbtExtractorContainer.getSrcNBTSlot();
        if (!srcNBTSlot.getHasStack()) {
            errorCode = null;
            this.renderWelcome();
        } else if (errorCode == null) {
            this.renderLoading();
        } else if (!errorCode.equals(ErrorCode.NO_ERROR)) {
            this.renderError();
        } else {
            this.treeViewer.render(nbt, mouseX, mouseY);
        }
        glDisable(GL_SCISSOR_TEST);
    }

    @Override
    public void onClose() {
        lastInstance = null;
        errorCode = null;
        nbt = null;
        extractionPath = null;
        outputMode = null;
        errorMessage = null;
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void renderGuiParts() {
        int padding = this.padding;
        int screenWidth = this.screenWidth;
        int screenHeight = this.screenHeight;
        GUI_TEXTURE.bind();
        PART0.renderTo(this, padding, padding);
        PART1.renderTo(this, padding + SIDE_BORDER_SIZE, padding, screenWidth, -1);
        PART2.renderTo(this, this.width - padding - SIDE_BORDER_SIZE, padding);
        PART3.renderTo(this, padding, padding + TOP_BORDER_SIZE, -1, screenHeight);
        PART4.renderTo(
            this,
            padding + SIDE_BORDER_SIZE,
            padding + TOP_BORDER_SIZE,
            screenWidth,
            screenHeight
        );
        PART5.renderTo(
            this,
            this.width - padding - SIDE_BORDER_SIZE,
            padding + TOP_BORDER_SIZE,
            -1,
            screenHeight
        );
        int topOfPart6789 = this.height - padding - INVENTORY_HEIGHT;
        PART6.renderTo(this, padding, topOfPart6789);
        int part7Width2x = this.width - 2 * padding - 2 * SIDE_BORDER_SIZE - INVENTORY_WIDTH;
        int part7WidthFloor = (int) Math.floor(part7Width2x / 2.0);
        int part7WidthCeil = (int) Math.ceil(part7Width2x / 2.0);
        PART7.renderTo(
            this,
            padding + SIDE_BORDER_SIZE,
            topOfPart6789,
            part7WidthFloor,
            -1
        );
        PART8.renderTo(
            this,
            padding + SIDE_BORDER_SIZE + part7WidthFloor,
            topOfPart6789
        );
        PART7.renderTo(
            this,
            padding + SIDE_BORDER_SIZE + part7WidthFloor + INVENTORY_WIDTH,
            topOfPart6789,
            part7WidthCeil,
            -1
        );
        PART9.renderTo(
            this,
            this.width - padding - SIDE_BORDER_SIZE,
            topOfPart6789
        );
    }

    private void renderWelcome() {
        this.renderCenteredTextGroup(
            I18n.format("integratednbt:nbt_extractor.welcome"),
            0x00FFFF,
            I18n.format("integratednbt:nbt_extractor.welcome.description")
        );
    }

    private void renderLoading() {
        this.renderCenteredTextGroup(
            I18n.format("integratednbt:nbt_extractor.loading"),
            0xFFFF00,
            I18n.format("integratednbt:nbt_extractor.loading.description")
        );
    }

    private void renderError() {
        String message = "";
        if (errorMessage != null) {
            message = errorMessage.getFormattedText();
        } else {
            switch (errorCode) {
                case EVAL_ERROR:
                    message = I18n.format("integratednbt:nbt_extractor.error.eval");
                    break;
                case TYPE_ERROR:
                    message = I18n.format("integratednbt:nbt_extractor.error.type");
                    break;
                case UNEXPECTED_ERROR:
                    message = I18n.format("integratednbt:nbt_extractor.error.unexpected");
                    break;
            }
        }
        this.renderCenteredTextGroup(
            I18n.format("integratednbt:nbt_extractor.error"),
            0xFF5555,
            message
        );
    }

    private void renderCenteredTextGroup(String title, int titleColor, String description) {
        glPushMatrix();
        try {
            int x = this.screenCenterX();
            int y = this.screenCenterY();
            int titleWidth = this.fontRenderer.getStringWidth(title);
            glPushMatrix();
            try {
                this.scaleAt(x, y, 2);
                this.fontRenderer.drawString(
                    title,
                    -titleWidth / 2f,
                    -this.fontRenderer.FONT_HEIGHT - 1,
                    titleColor
                );
            } finally {
                glPopMatrix();
            }
            this.scaleAt(x, y, 1);
            int wrappingWidth = (int) (this.screenWidth * CENTERED_TEXT_MAX_RATIO);
            int descriptionWidth = this.fontRenderer.getStringWidth(description);
            if (descriptionWidth > wrappingWidth) {
                this.fontRenderer.drawSplitString(
                    description,
                    -wrappingWidth / 2,
                    4,
                    wrappingWidth,
                    0xFFFFFF
                );
            } else {
                this.fontRenderer.drawString(
                    description,
                    -descriptionWidth / 2f,
                    4,
                    0xFFFFFF
                );
            }
        } finally {
            glPopMatrix();
        }
    }

    private int screenCenterX() {
        return this.width / 2;
    }

    private int screenCenterY() {
        return this.padding + TOP_BORDER_SIZE + this.screenHeight / 2;
    }

    private void scaleAt(int x, int y, double scale) {
        glScaled(scale, scale, 1d);
        glTranslated(x / scale, y / scale, 0d);
    }
}
