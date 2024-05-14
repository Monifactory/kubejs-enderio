package com.almostreliable.kubeio.enderio.conduit;

import com.enderio.EnderIO;
import com.enderio.api.conduit.ClientConduitData;
import com.enderio.api.misc.Vector2i;
import com.enderio.conduits.common.types.energy.EnergyExtendedData;
import net.minecraft.resources.ResourceLocation;

public class CustomEnergyClientData extends ClientConduitData.Simple<EnergyExtendedData> {

    private static final int SIZE = 64;

    public CustomEnergyClientData(ResourceLocation textureLocation) {
        super(EnderIO.loc("textures/" + textureLocation.getPath() + ".png"), new Vector2i(40, 40));
    }

    @Override
    public Vector2i getTextureSize() {
        return new Vector2i(SIZE, SIZE);
    }
}
