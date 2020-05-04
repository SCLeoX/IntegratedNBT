package me.tepis.integratednbt;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.cyclops.commoncapabilities.api.capability.wrench.IWrench;
import org.cyclops.integrateddynamics.api.block.IVariableContainer;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.network.INetworkCarrier;
import org.cyclops.integrateddynamics.api.network.INetworkElementProvider;
import org.cyclops.integrateddynamics.api.path.IPathElement;

public abstract class Capabilities {
    @CapabilityInject(IWrench.class)
    public static Capability<IWrench> WRENCH_CAPABILITY = null;
    @CapabilityInject(ICable.class)
    static Capability<ICable> CABLE_CAPABILITY;
    @CapabilityInject(INetworkCarrier.class)
    static Capability<INetworkCarrier> NETWORK_CARRIER_CAPABILITY;
    @CapabilityInject(IPathElement.class)
    static Capability<IPathElement> PATH_ELEMENT_CAPABILITY;
    @CapabilityInject(IVariableContainer.class)
    static Capability<IVariableContainer> VARIABLE_CONTAINER_CAPABILITY;
    @CapabilityInject(INetworkElementProvider.class)
    static Capability<INetworkElementProvider> NETWORK_ELEMENT_PROVIDER;
}
