package com.skaggsm.treechoppermod;

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Settings;
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.convention.SnakeCaseConvention;
import org.jetbrains.annotations.NotNull;

public class FabricTreeChopperFiberConfig {

    public boolean fastLeafDecay = true;
    public FullChopDurabilityMode fullChopDurabilityUsage = FullChopDurabilityMode.BREAK_MID_CHOP;
    public boolean sneakToDisable = true;
    public boolean invertSneakToDisable = false;
    public boolean requireLeavesToChop = true;
    @NotNull
    ChopMode treeChopMode = ChopMode.SINGLE_CHOP;
}

