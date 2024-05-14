package com.almostreliable.kubeio.enderio.conduit;

import com.enderio.api.conduit.ConduitType;
import com.enderio.api.misc.ColorControl;
import com.enderio.conduits.common.types.energy.EnergyConduitTicker;
import com.enderio.conduits.common.types.energy.EnergyExtendedData;
import dev.gigaherz.graph3.Graph;
import dev.gigaherz.graph3.Mergeable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.energy.IEnergyStorage;
import org.apache.commons.lang3.function.TriFunction;

import java.util.List;

public class CustomEnergyConduitTicker extends EnergyConduitTicker {

    private final int transferRate;

    public CustomEnergyConduitTicker(int transferRate) {
        this.transferRate = transferRate;
    }

    @Override
    public void tickCapabilityGraph(
        ConduitType<?> type, List<CapabilityConnection> inserts, List<CapabilityConnection> extracts,
        ServerLevel level,
        Graph<Mergeable.Dummy> graph, TriFunction<ServerLevel, BlockPos, ColorControl, Boolean> isRedstoneActive
    ) {
        for (CapabilityConnection extract : extracts) {
            IEnergyStorage extractHandler = extract.cap;
            int availableForExtraction = extractHandler.extractEnergy(transferRate, true);
            if (availableForExtraction <= 0) continue;

            EnergyExtendedData.EnergySidedData sidedExtractData = extract.data.castTo(EnergyExtendedData.class)
                .compute(extract.direction);

            if (inserts.size() <= sidedExtractData.rotatingIndex) {
                sidedExtractData.rotatingIndex = 0;
            }

            for (int j = sidedExtractData.rotatingIndex; j < sidedExtractData.rotatingIndex + inserts.size(); j++) {
                int insertIndex = j % inserts.size();
                CapabilityConnection insert = inserts.get(insertIndex);

                int inserted = insert.cap.receiveEnergy(availableForExtraction, false);
                extractHandler.extractEnergy(inserted, false);

                if (inserted == availableForExtraction) {
                    sidedExtractData.rotatingIndex += insertIndex + 1;
                    break;
                }

                availableForExtraction -= inserted;
            }
        }
    }
}
