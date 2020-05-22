package me.tepis.integratednbt;

import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;
import org.cyclops.cyclopscore.datastructure.Wrapper;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelBaked;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeNbt.ValueNbt;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.item.VariableFacadeBase;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class NBTExtractedVariableFacade extends VariableFacadeBase {
    private int sourceNBTId;
    private NBTPath extractionPath;
    private byte defaultNBTId;
    private boolean validating;
    private boolean gettingVariable;
    private int lastNetworkHash;
    private NBTExtractedVariable variable;

    public NBTExtractedVariableFacade(
        boolean generateId,
        int sourceNBTId,
        @Nullable NBTPath extractionPath,
        byte defaultNBTId
    ) {
        super(generateId);
        this.sourceNBTId = sourceNBTId;
        this.extractionPath = extractionPath;
        this.defaultNBTId = defaultNBTId;
    }

    public NBTExtractedVariableFacade(
        int id,
        int sourceNBTId,
        @Nullable NBTPath extractionPath,
        byte defaultNBTId
    ) {
        super(id);
        this.sourceNBTId = sourceNBTId;
        this.extractionPath = extractionPath;
        this.defaultNBTId = defaultNBTId;
    }

    public byte getDefaultNBTId() {
        return this.defaultNBTId;
    }

    public int getSourceNBTId() {
        return this.sourceNBTId;
    }

    public NBTPath getExtractionPath() {
        return this.extractionPath;
    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(List<ITextComponent> list, World world) {
        if (!this.isValid()) {
            return;
        }
        list.add(new TranslationTextComponent(
            "integratednbt:nbt_extracted_variable.tooltip.source_nbt_id",
            this.sourceNBTId
        ));
        list.add(new TranslationTextComponent(
            "integratednbt:nbt_extracted_variable.tooltip.path",
            this.extractionPath.getDisplayText()
        ));
        list.add(new TranslationTextComponent(
            "integratednbt:nbt_extracted_variable.tooltip.default_value",
            NBTValueConverter.getDefaultValueDisplayText(this.defaultNBTId)
        ));
        super.addInformation(list, world);
    }

    @Override
    public boolean isValid() {
        return this.extractionPath != null;
    }

    @Override
    public void addModelOverlay(
        IVariableModelBaked variableModelBaked,
        List<BakedQuad> quads,
        Random random,
        IModelData modelData
    ) {

    }

    @Override
    @SuppressWarnings("unchecked")
    public <V extends IValue> IVariable<V> getVariable(
        IPartNetwork network
    ) {
        if (!this.isValid()) {
            return null;
        }
        int newNetworkHash = network != null ? network.hashCode() : -1;
        if (this.variable == null || newNetworkHash != this.lastNetworkHash) {
            this.lastNetworkHash = newNetworkHash;
            if (network == null || !network.hasVariableFacade(this.sourceNBTId)) {
                return null;
            }
            IVariableFacade sourceNbtVariableFacade = network.getVariableFacade(this.sourceNBTId);
            if (!sourceNbtVariableFacade.isValid() || sourceNbtVariableFacade == this) {
                return null;
            }
            if (this.gettingVariable) {
                return null;
            }
            this.gettingVariable = true;
            IVariable<ValueNbt> sourceNbtVariable = sourceNbtVariableFacade.getVariable(network);
            this.gettingVariable = false;
            if (sourceNbtVariable == null) {
                return null;
            }
            this.variable = new NBTExtractedVariable(
                sourceNbtVariable,
                this.extractionPath,
                this.defaultNBTId
            );
        }
        return (IVariable<V>) this.variable;
    }

    @Override
    public void validate(
        IPartNetwork network, IValidator validator, IValueType containingValueType
    ) {
        if (!this.isValid()) {
            return;
        }
        if (this.sourceNBTId < 0) {
            validator.addError(new TranslationTextComponent(L10NValues.VARIABLE_ERROR_INVALIDITEM));
        } else if (!network.hasVariableFacade(this.sourceNBTId)) {
            validator.addError(new TranslationTextComponent(
                L10NValues.OPERATOR_ERROR_VARIABLENOTINNETWORK,
                Integer.toString(this.sourceNBTId)
            ));
        } else {
            IVariableFacade sourceVariableFacade = network.getVariableFacade(this.sourceNBTId);
            if (sourceVariableFacade == this) {
                validator.addError(new TranslationTextComponent(
                    L10NValues.OPERATOR_ERROR_CYCLICREFERENCE,
                    Integer.toString(this.sourceNBTId)
                ));
            } else if (sourceVariableFacade != null) {
                final Wrapper<Boolean> isValid = new Wrapper<>(true);
                if (this.validating) {
                    validator.addError(new TranslationTextComponent(
                        L10NValues.OPERATOR_ERROR_CYCLICREFERENCE,
                        this.getId()
                    ));
                }
                this.validating = true;
                sourceVariableFacade.validate(network, error -> {
                    validator.addError(error);
                    isValid.set(false);
                }, ValueTypes.NBT);
                this.validating = false;
            }
        }
    }

    @Override
    public IValueType<?> getOutputType() {
        return ValueTypes.CATEGORY_ANY;
    }
}
