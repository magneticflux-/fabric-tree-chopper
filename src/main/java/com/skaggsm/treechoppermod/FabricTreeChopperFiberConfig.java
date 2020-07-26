package com.skaggsm.treechoppermod;

import me.shedaniel.fiber2cloth.api.ClothSetting;
import org.jetbrains.annotations.NotNull;

public class FabricTreeChopperFiberConfig {

    public boolean fastLeafDecay = true;
    @NotNull
    @ClothSetting.Tooltip
    public FullChopDurabilityMode fullChopDurabilityUsage = FullChopDurabilityMode.BREAK_MID_CHOP;
    public boolean sneakToDisable = true;
    public boolean requireLeavesToChop = true;
    @NotNull
    public ChopMode treeChopMode = ChopMode.SINGLE_CHOP;
}

