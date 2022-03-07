package me.tepis.integratednbt;

import com.mojang.blaze3d.vertex.PoseStack;
import me.tepis.integratednbt.network.PacketHandler;
import me.tepis.integratednbt.network.clientbound.NBTExtractorUpdateClientMessage.ErrorCode;
import me.tepis.integratednbt.network.serverbound.NBTExtractorUpdateAutoRefreshMessage;
import me.tepis.integratednbt.network.serverbound.NBTExtractorUpdateExtractionPathMessage;
import me.tepis.integratednbt.network.serverbound.NBTExtractorUpdateOutputModeMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Arrays;

import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glScissor;

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
    private static Tag nbt;
    private static NBTPath extractionPath = null;
    private static NBTExtractorOutputMode outputMode = null;
    private static Component errorMessage = null;
    private static Boolean autoRefresh = null;

    private NBTTreeViewer treeViewer;
    private NBTExtractorContainer nbtExtractorContainer;
    private Font fontRenderer = Minecraft.getInstance().font;
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
        Inventory inventory,
        Component title
    ) {
        super(screenContainer, inventory, title);
        NBTExtractorScreen.lastInstance = this;
        this.nbtExtractorContainer = screenContainer;
        NBTExtractorBE tileEntity = this.nbtExtractorContainer.getNbtExtractorEntity();
        this.treeViewer = new NBTTreeViewer(
            this,
            tileEntity.getExpandedPaths(),
            tileEntity.getScrollTop()
        ) {
            @Override
            public void onUpdateSelectedPath(NBTPath newPath, Tag nbt) {
                PacketHandler.INSTANCE.send(
                    PacketDistributor.SERVER.noArg(),
                    new NBTExtractorUpdateExtractionPathMessage(
                        NBTExtractorScreen.this.nbtExtractorContainer.getNbtExtractorEntity()
                            .getBlockPos(),
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

    public static void updateNBT(Tag nbt) {
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
        ArrayList<Component> messages = new ArrayList<>();
        if (outputMode == null) {
            this.outputModeButton.setTexture(
                BUTTON_UNKNOWN,
                BUTTON_UNKNOWN_HOVER
            );
            messages.add(new TranslatableComponent(
                "integratednbt:nbt_extractor.output_mode",
                new TranslatableComponent("integratednbt:nbt_extractor.loading")
            ));
        } else {
            this.outputModeButton.setTexture(
                outputMode.getButtonTextureNormal(),
                outputMode.getButtonTextureHover()
            );
            messages.add(new TranslatableComponent(
                "integratednbt:nbt_extractor.output_mode",
                outputMode.getName()
            ));
        }
        messages.add(new TranslatableComponent(
            "integratednbt:nbt_extractor.output_mode.description.begin").setStyle(Style.EMPTY.withColor(
            ChatFormatting.GRAY)));
        messages.add(new TextComponent(" "));
        Arrays.stream(NBTExtractorOutputMode.values())
            .forEach(describingOutputMode -> messages.add(describingOutputMode.getDescription(
                describingOutputMode.equals(outputMode))));
        messages.add(new TextComponent(" "));
        messages.add(new TranslatableComponent(
            "integratednbt:nbt_extractor.output_mode.description.end",
            NBTExtractorOutputMode.REFERENCE.getName()
        ).setStyle(Style.EMPTY.withColor(
            ChatFormatting.GRAY)));
        this.outputModeButton.setHoverText(messages);
    }

    public static void updateErrorMessage(Component errorMessage) {
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
            messages.add(I18n.get(
                "integratednbt:nbt_extractor.auto_refresh",
                I18n.get("integratednbt:nbt_extractor.loading")
            ));
        } else if (autoRefresh) {
            this.autoRefreshButton.setTexture(
                BUTTON_REFRESH_ON,
                BUTTON_REFRESH_ON_HOVER
            );
            messages.add(I18n.get(
                "integratednbt:nbt_extractor.auto_refresh",
                I18n.get("integratednbt:nbt_extractor.auto_refresh.on")
            ));
        } else {
            this.autoRefreshButton.setTexture(
                BUTTON_REFRESH_OFF,
                BUTTON_REFRESH_OFF_HOVER
            );
            messages.add(I18n.get(
                "integratednbt:nbt_extractor.auto_refresh",
                I18n.get("integratednbt:nbt_extractor.auto_refresh.off")
            ));
        }
        messages.addAll(Arrays.asList(I18n.get(
            "integratednbt:nbt_extractor.auto_refresh.description").split("\\\\n")));
        this.autoRefreshButton.setHoverTextRaw(messages);
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
        this.imageWidth = this.width - 2 * this.padding;
        this.imageHeight = this.height - 2 * this.padding;
        super.init();
        this.nbtExtractorContainer.setSlotOffset(
            (this.imageWidth - INVENTORY_WIDTH) / 2,
            this.imageHeight - INVENTORY_HEIGHT
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
        this.addWidget(this.outputModeButton);
        this.autoRefreshButton = new HoverTextImageButton(
            this,
            this.width - this.padding - 7 - BUTTON_SIZE * 2 - BUTTON_SPACING,
            this.padding + 7,
            this::onAutoRefreshButtonClick
        );
        this.updateAutoRefreshButton();
        this.addWidget(this.autoRefreshButton);
    }

    /**
     * Update
     */
    private void updateCalculations() {
        this.scaleFactor = Minecraft.getInstance().getWindow().getGuiScale();
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
                this.nbtExtractorContainer.getNbtExtractorEntity().getBlockPos(),
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
                this.nbtExtractorContainer.getNbtExtractorEntity().getBlockPos(),
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
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {
        this.outputModeButton.drawHover(matrixStack, mouseX, mouseY);
        this.autoRefreshButton.drawHover(matrixStack, mouseX, mouseY);
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.outputModeButton.render(matrixStack, mouseX, mouseY, partialTicks);
        this.autoRefreshButton.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        this.renderGuiParts(matrixStack);
        this.fontRenderer.draw(
            matrixStack,
            I18n.get("block.integratednbt.nbt_extractor"),
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
        if (!srcNBTSlot.hasItem()) {
            errorCode = null;
            this.renderWelcome(matrixStack);
        } else if (errorCode == null) {
            this.renderLoading(matrixStack);
        } else if (!errorCode.equals(ErrorCode.NO_ERROR)) {
            this.renderError(matrixStack);
        } else {
            this.treeViewer.render(matrixStack, nbt, mouseX, mouseY);
        }
        glDisable(GL_SCISSOR_TEST);
    }

    @Override
    public void removed() {
        if (lastInstance == this) {
            lastInstance = null;
            errorCode = null;
            nbt = null;
            extractionPath = null;
            outputMode = null;
            errorMessage = null;
        }
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void renderGuiParts(PoseStack matrixStack) {
        int padding = this.padding;
        int screenWidth = this.screenWidth;
        int screenHeight = this.screenHeight;
        GUI_TEXTURE.bind();
        PART0.renderTo(this, matrixStack, padding, padding);
        PART1.renderToScaled(this, matrixStack, padding + SIDE_BORDER_SIZE, padding, screenWidth, -1);
        PART2.renderTo(this, matrixStack, this.width - padding - SIDE_BORDER_SIZE, padding);
        PART3.renderToScaled(this, matrixStack, padding, padding + TOP_BORDER_SIZE, -1, screenHeight);
        PART4.renderToScaled(
            this,
            matrixStack,
            padding + SIDE_BORDER_SIZE,
            padding + TOP_BORDER_SIZE,
            screenWidth,
            screenHeight
        );
        PART5.renderToScaled(
            this,
            matrixStack,
            this.width - padding - SIDE_BORDER_SIZE,
            padding + TOP_BORDER_SIZE,
            -1,
            screenHeight
        );
        int topOfPart6789 = this.height - padding - INVENTORY_HEIGHT;
        PART6.renderTo(this, matrixStack, padding, topOfPart6789);
        int part7Width2x = this.width - 2 * padding - 2 * SIDE_BORDER_SIZE - INVENTORY_WIDTH;
        int part7WidthFloor = (int) Math.floor(part7Width2x / 2.0);
        int part7WidthCeil = (int) Math.ceil(part7Width2x / 2.0);
        PART7.renderToScaled(
            this,
            matrixStack,
            padding + SIDE_BORDER_SIZE,
            topOfPart6789,
            part7WidthFloor,
            -1
        );
        PART8.renderTo(
            this,
            matrixStack,
            padding + SIDE_BORDER_SIZE + part7WidthFloor,
            topOfPart6789
        );
        PART7.renderToScaled(
            this,
            matrixStack,
            padding + SIDE_BORDER_SIZE + part7WidthFloor + INVENTORY_WIDTH,
            topOfPart6789,
            part7WidthCeil,
            -1
        );
        PART9.renderTo(
            this,
            matrixStack,
            this.width - padding - SIDE_BORDER_SIZE,
            topOfPart6789
        );
    }

    private void renderWelcome(PoseStack matrixStack) {
        this.renderCenteredTextGroup(
            matrixStack,
            I18n.get("integratednbt:nbt_extractor.welcome"),
            0x00FFFF,
            I18n.get("integratednbt:nbt_extractor.welcome.description")
        );
    }

    private void renderLoading(PoseStack matrixStack) {
        this.renderCenteredTextGroup(
            matrixStack,
            I18n.get("integratednbt:nbt_extractor.loading"),
            0xFFFF00,
            I18n.get("integratednbt:nbt_extractor.loading.description")
        );
    }

    private void renderError(PoseStack matrixStack) {
        String message = "";
        if (errorMessage != null) {
            message = errorMessage.getString();
        } else {
            switch (errorCode) {
                case EVAL_ERROR:
                    message = I18n.get("integratednbt:nbt_extractor.error.eval");
                    break;
                case TYPE_ERROR:
                    message = I18n.get("integratednbt:nbt_extractor.error.type");
                    break;
                case UNEXPECTED_ERROR:
                    message = I18n.get("integratednbt:nbt_extractor.error.unexpected");
                    break;
            }
        }
        this.renderCenteredTextGroup(
            matrixStack,
            I18n.get("integratednbt:nbt_extractor.error"),
            0xFF5555,
            message
        );
    }

    private void renderCenteredTextGroup(PoseStack matrixStack, String title, int titleColor, String description) {
        matrixStack.pushPose();
        try {
            int x = this.screenCenterX();
            int y = this.screenCenterY();
            int titleWidth = this.fontRenderer.width(title);
            matrixStack.pushPose();
            try {
                this.scaleAt(matrixStack, x, y, 2);
                this.fontRenderer.draw(
                    matrixStack,
                    title,
                    -titleWidth / 2f,
                    -this.fontRenderer.lineHeight - 1,
                    titleColor
                );
            } finally {
                matrixStack.popPose();
            }
            this.scaleAt(matrixStack, x, y, 1);
            int wrappingWidth = (int) (this.screenWidth * CENTERED_TEXT_MAX_RATIO);
            int descriptionWidth = this.fontRenderer.width(description);
            if (descriptionWidth > wrappingWidth) {
                // this.fontRenderer.drawSplitString(
                this.fontRenderer.drawWordWrap(
                    new TextComponent(description),
                    -wrappingWidth / 2,
                    4,
                    wrappingWidth,
                    0xFFFFFF
                );
            } else {
                this.fontRenderer.draw(
                    matrixStack,
                    description,
                    -descriptionWidth / 2f,
                    4,
                    0xFFFFFF
                );
            }
        } finally {
            matrixStack.popPose();
        }
    }

    private int screenCenterX() {
        return this.width / 2;
    }

    private int screenCenterY() {
        return this.padding + TOP_BORDER_SIZE + this.screenHeight / 2;
    }

    private void scaleAt(PoseStack poseStack, int x, int y, float scale) {
        poseStack.scale(scale, scale, 1f);
        poseStack.translate(x / scale, y / scale, 0f);
    }
}
