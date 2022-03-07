package me.tepis.integratednbt;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.cyclops.integrateddynamics.api.block.IVariableContainer;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.network.INetworkCarrier;
import org.cyclops.integrateddynamics.api.network.INetworkElementProvider;
import org.cyclops.integrateddynamics.api.path.IPathElement;

public abstract class Capabilities {
    public static Capability<ICable> CABLE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    public static Capability<INetworkCarrier> NETWORK_CARRIER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    public static Capability<IPathElement> PATH_ELEMENT_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    public static Capability<IVariableContainer> VARIABLE_CONTAINER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    public static Capability<INetworkElementProvider> NETWORK_ELEMENT_PROVIDER = CapabilityManager.get(new CapabilityToken<>() {});
}
