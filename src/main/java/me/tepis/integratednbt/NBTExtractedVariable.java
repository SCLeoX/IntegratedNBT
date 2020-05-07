package me.tepis.integratednbt;

import net.minecraft.nbt.NBTBase;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.expression.VariableAdapter;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeNbt.ValueNbt;

public class NBTExtractedVariable extends VariableAdapter<IValue> {
    private IVariable<ValueNbt> sourceNBTVariable;
    private NBTPath extractionPath;
    private NBTBase cachedValue;
    private byte defaultNBTId;

    public NBTExtractedVariable(
        IVariable<ValueNbt> sourceNBTVariable,
        NBTPath extractionPath,
        byte defaultNBTId
    ) {
        this.sourceNBTVariable = sourceNBTVariable;
        this.extractionPath = extractionPath;
        this.defaultNBTId = defaultNBTId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public IValueType<IValue> getType() {
        try {
            this.ensureCachedValue();
            if (this.cachedValue == null) {
                return NBTValueConverter.getDefaultValue(this.defaultNBTId).getType();
            }
            return (IValueType<IValue>) NBTValueConverter.mapNBTToValueType(this.cachedValue);
        } catch (EvaluationException ex) {
            return NBTValueConverter.getDefaultValue(this.defaultNBTId).getType();
        }
    }

    private void ensureCachedValue() throws EvaluationException {
        if (this.cachedValue == null) {
            this.sourceNBTVariable.addInvalidationListener(this);
            this.cachedValue =
                this.extractionPath.extract(this.sourceNBTVariable.getValue().getRawValue());
        }
    }

    @Override
    public IValue getValue() throws EvaluationException {
        this.ensureCachedValue();
        if (this.cachedValue == null) {
            return NBTValueConverter.getDefaultValue(this.defaultNBTId);
        }
        return NBTValueConverter.mapNBTToValue(this.cachedValue);
    }

    @Override
    public void invalidate() {
        this.cachedValue = null;
        super.invalidate();
    }
}
