package me.tepis.integratednbt;


import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.item.IValueTypeVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry.IVariableFacadeFactory;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator.ValueOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.item.ValueTypeVariableFacade;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public enum NBTExtractorOutputMode {
    REFERENCE(
        "reference",
        TextFormatting.YELLOW,
        Nest.GUI_TEXTURE.createPart(90, 0, Nest.BUTTON_SIZE, Nest.BUTTON_SIZE),
        Nest.GUI_TEXTURE.createPart(90, 12, Nest.BUTTON_SIZE, Nest.BUTTON_SIZE)
    ) {
        @Override
        public ItemStack writeItemStack(
            Supplier<IVariableFacade> sourceVariableFacadeSupplier,
            ItemStack outputVariableItemStack,
            INBT currentNBT,
            NBTPath extractionPath,
            byte defaultNBTId,
            BlockState blockState
        ) {
            IVariableFacadeHandlerRegistry registry =
                IntegratedDynamics._instance.getRegistryManager()
                    .getRegistry(IVariableFacadeHandlerRegistry.class);
            IVariableFacade variableFacade = sourceVariableFacadeSupplier.get();
            if (variableFacade != null) {
                int sourceNBTId = variableFacade.getId();
                IVariableFacadeFactory<NBTExtractedVariableFacade> factory =
                    new IVariableFacadeFactory<NBTExtractedVariableFacade>() {
                        @Override
                        public NBTExtractedVariableFacade create(boolean generateId) {
                            return new NBTExtractedVariableFacade(
                                generateId,
                                sourceNBTId,
                                extractionPath,
                                defaultNBTId
                            );
                        }

                        @Override
                        public NBTExtractedVariableFacade create(int id) {
                            return new NBTExtractedVariableFacade(
                                id,
                                sourceNBTId,
                                extractionPath,
                                defaultNBTId
                            );
                        }
                    };
                return registry.writeVariableFacadeItem(
                    true,
                    outputVariableItemStack,
                    NBTExtractedVariableFacadeHandler.getInstance(),
                    factory,
                    null,
                    blockState
                );
            } else {
                return null;
            }
        }
    },
    OPERATOR(
        "operator",
        TextFormatting.DARK_GREEN,
        Nest.GUI_TEXTURE.createPart(102, 0, Nest.BUTTON_SIZE, Nest.BUTTON_SIZE),
        Nest.GUI_TEXTURE.createPart(102, 12, Nest.BUTTON_SIZE, Nest.BUTTON_SIZE)
    ) {
        @Override
        public ItemStack writeItemStack(
            Supplier<IVariableFacade> sourceVariableFacadeSupplier,
            ItemStack outputVariableItemStack,
            INBT currentNBT,
            NBTPath extractionPath,
            byte defaultNBTId,
            BlockState blockState
        ) {
            return getVariableUsingValue(ValueOperator.of(new NBTExtractionOperator(
                extractionPath,
                defaultNBTId
            )), outputVariableItemStack, blockState);
        }
    },
    VALUE(
        "value",
        TextFormatting.GOLD,
        Nest.GUI_TEXTURE.createPart(114, 0, Nest.BUTTON_SIZE, Nest.BUTTON_SIZE),
        Nest.GUI_TEXTURE.createPart(114, 12, Nest.BUTTON_SIZE, Nest.BUTTON_SIZE)
    ) {
        @Override
        public ItemStack writeItemStack(
            Supplier<IVariableFacade> sourceVariableFacadeSupplier,
            ItemStack outputVariableItemStack,
            INBT currentNBT,
            NBTPath extractionPath,
            byte defaultNBTId,
            BlockState blockState
        ) {
            sourceVariableFacadeSupplier.get(); // Refresh variable
            INBT extractedNBT = extractionPath.extract(currentNBT);
            IValue value = extractedNBT == null
                ? NBTValueConverter.getDefaultValue(defaultNBTId)
                : NBTValueConverter.mapNBTToValue(extractedNBT);
            return getVariableUsingValue(value, outputVariableItemStack, blockState);
        }
    };

    private static class Nest {
        private static final Texture GUI_TEXTURE = new Texture(
            "integratednbt",
            "textures/gui/nbt_extractor.png"
        );
        private static final int BUTTON_SIZE = 12;
    }

    private String translationId;
    private TextFormatting color;
    private TexturePart buttonTextureNormal;
    private TexturePart buttonTextureHover;

    NBTExtractorOutputMode(
        String translationId,
        TextFormatting color,
        TexturePart buttonTextureNormal,
        TexturePart buttonTextureHover
    ) {
        this.translationId = translationId;
        this.color = color;
        this.buttonTextureNormal = buttonTextureNormal;
        this.buttonTextureHover = buttonTextureHover;
    }

    @Nullable
    @SuppressWarnings( {"rawtypes", "unchecked"})
    private static ItemStack getVariableUsingValue(
        IValue value,
        ItemStack outputVariableItemStack,
        BlockState blockState
    ) {
        IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager()
            .getRegistry(IVariableFacadeHandlerRegistry.class);
        if (value == null) {
            return null;
        }
        return registry.writeVariableFacadeItem(
            true,
            outputVariableItemStack,
            ValueTypes.REGISTRY,
            new IVariableFacadeHandlerRegistry.IVariableFacadeFactory<IValueTypeVariableFacade>() {
                @Override
                public IValueTypeVariableFacade create(boolean generateId) {
                    return new ValueTypeVariableFacade(generateId, value.getType(), value);
                }

                @Override
                public IValueTypeVariableFacade create(int id) {
                    return new ValueTypeVariableFacade(id, value.getType(), value);
                }
            },
            null,
            blockState
        );
    }

    public TexturePart getButtonTextureNormal() {
        return this.buttonTextureNormal;
    }

    public TexturePart getButtonTextureHover() {
        return this.buttonTextureHover;
    }

    public abstract ItemStack writeItemStack(
        Supplier<IVariableFacade> sourceVariableFacadeSupplier,
        ItemStack outputVariableItemStack,
        INBT currentNBT,
        NBTPath extractionPath,
        byte defaultNBTId,
        BlockState blockState
    );

    public ITextComponent getDescription(boolean highlighted) {
        return new TranslationTextComponent(
            "integratednbt:nbt_extractor.output_mode." + this.translationId + ".description",
            this.getName()
        ).setStyle(highlighted
            ? new Style().setColor(TextFormatting.GRAY)
            : new Style().setColor(TextFormatting.DARK_GRAY));
    }

    public ITextComponent getName() {
        return new TranslationTextComponent(
            "integratednbt:nbt_extractor.output_mode." + this.translationId + ".name"
        ).setStyle(new Style().setColor(this.color));
    }
}
