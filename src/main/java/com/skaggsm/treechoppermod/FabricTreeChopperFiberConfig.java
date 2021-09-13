package com.skaggsm.treechoppermod;

import me.shedaniel.fiber2cloth.api.ClothSetting;
import net.minecraft.item.AxeItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FabricTreeChopperFiberConfig {
    public boolean fastLeafDecay = true;
    @NotNull
    @ClothSetting.Tooltip("config.fabric-tree-chopper.fullChopDurabilityUsage@Tooltip")
    public FullChopDurabilityMode fullChopDurabilityUsage = FullChopDurabilityMode.BREAK_MID_CHOP;
    public boolean sneakToDisable = true;
    public boolean requireLeavesToChop = true;
    @NotNull
    public ChopMode treeChopMode = ChopMode.SINGLE_CHOP;
    public int logSearchLimit = -1;
    public boolean stopBeforeAxeBreak = true;
    public List<Identifier> axes = Registry.ITEM.stream().filter(AxeItem.class::isInstance).map(Registry.ITEM::getId).toList();
}
