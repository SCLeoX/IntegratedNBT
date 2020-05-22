package me.tepis.integratednbt;

import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeNbt.ValueNbt;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import java.util.List;

public class NBTExtractionOperator implements IOperator {
    public static ResourceLocation UNIQUE_NAME = new ResourceLocation(
        IntegratedNBT.MODID,
        "nbt_extraction"
    );
    private NBTPath extractionPath;
    private byte defaultNBTId;

    public NBTExtractionOperator(NBTPath extractionPath, byte defaultNBTId) {
        this.extractionPath = extractionPath;
        this.defaultNBTId = defaultNBTId;
    }

    public NBTPath getExtractionPath() {
        return this.extractionPath;
    }

    public byte getDefaultNBTId() {
        return this.defaultNBTId;
    }

    @Override
    public ResourceLocation getUniqueName() {
        return UNIQUE_NAME;
    }

    @Override
    public ITextComponent getLocalizedNameFull() {
        return new TranslationTextComponent("integratednbt:nbt_extraction_operator.full_name");
    }

    @Override
    public void loadTooltip(List<ITextComponent> lines, boolean appendOptionalInfo) {
        String operatorName = L10NHelpers.localize(this.getTranslationKey());
        String categoryName = L10NHelpers.localize(this.getUnlocalizedCategoryName());
        String symbol = this.getSymbol();
        String outputTypeName = L10NHelpers.localize(this.getOutputType().getTranslationKey());
        lines.add(new TranslationTextComponent(
            L10NValues.OPERATOR_TOOLTIP_OPERATORNAME,
            operatorName,
            symbol
        ));
        lines.add(new TranslationTextComponent(
            L10NValues.OPERATOR_TOOLTIP_OPERATORCATEGORY,
            categoryName
        ));
        lines.add(new TranslationTextComponent(
            L10NValues.OPERATOR_TOOLTIP_INPUTTYPENAME,
            1,
            new TranslationTextComponent(ValueTypes.NBT.getTranslationKey()).setStyle(
                new Style().setColor(
                    ValueTypes.NBT.getDisplayColorFormat()))
        ));
        lines.add(new TranslationTextComponent(
            L10NValues.OPERATOR_TOOLTIP_OUTPUTTYPENAME,
            this.getOutputType().getDisplayColorFormat() + outputTypeName
        ));
    }

    @Override
    public String getTranslationKey() {
        return "integratednbt:nbt_extraction_operator.name";
    }

    @Override
    public String getUnlocalizedCategoryName() {
        return "integratednbt:nbt_extraction_operator.category_name";
    }

    @Override
    public String getSymbol() {
        return this.extractionPath.getCompactDisplayText();
    }

    @Override
    public IValueType<?> getOutputType() {
        return ValueTypes.CATEGORY_ANY;
    }

    @Override
    public IValueType<?>[] getInputTypes() {
        return new IValueType[] {ValueTypes.NBT};
    }

    @Override
    public IValueType<?> getConditionalOutputType(IVariable[] input) {
        try {
            if (input.length == 1) {
                IValue value = input[0].getValue();
                if (value instanceof ValueNbt) {
                    INBT extracted =
                        this.extractionPath.extract(((ValueNbt) value).getRawValue().orElse(null));
                    if (extracted != null) {
                        return NBTValueConverter.mapNBTToValueType(extracted);
                    }
                }
            }
        } catch (EvaluationException ignored) {
        }
        return NBTValueConverter.mapNBTIDToValueType(this.defaultNBTId);
    }

    @Override
    public IValue evaluate(IVariable... input) throws EvaluationException {
        if (input.length == 1) {
            IValue value = input[0].getValue();
            if (value instanceof ValueNbt) {
                INBT extracted =
                    this.extractionPath.extract(((ValueNbt) value).getRawValue().orElse(null));
                if (extracted != null) {
                    return NBTValueConverter.mapNBTToValue(extracted);
                }
            }
        }
        return NBTValueConverter.getDefaultValue(this.defaultNBTId);
    }

    @Override
    public int getRequiredInputLength() {
        return 1;
    }

    @Override
    public ITextComponent validateTypes(IValueType[] input) {
        if (input.length != 1) {
            return new TranslationTextComponent(
                L10NValues.OPERATOR_ERROR_WRONGINPUTLENGTH,
                new TranslationTextComponent("integratednbt:nbt_extraction_operator.full_name"),
                input.length,
                1
            );
        }
        IValueType<?> inputType = input[0];
        if (inputType == null) {
            return new TranslationTextComponent(
                L10NValues.OPERATOR_ERROR_NULLTYPE,
                new TranslationTextComponent("integratednbt:nbt_extraction_operator.full_name"),
                "0"
            );
        }
        if (!ValueHelpers.correspondsTo(ValueTypes.NBT, inputType)) {
            return new TranslationTextComponent(
                L10NValues.OPERATOR_ERROR_WRONGTYPE,
                new TranslationTextComponent("integratednbt:nbt_extraction_operator.full_name"),
                new TranslationTextComponent(inputType.getTranslationKey()),
                "1",
                new TranslationTextComponent(ValueTypes.NBT.getTranslationKey())
            );
        }
        return null;
    }

    @Override
    public IConfigRenderPattern getRenderPattern() {
        return null;
    }

    @Override
    public IOperator materialize() {
        return this;
    }
}
