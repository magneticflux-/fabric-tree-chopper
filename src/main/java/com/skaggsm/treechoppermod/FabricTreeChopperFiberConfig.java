package com.skaggsm.treechoppermod;

import me.shedaniel.fiber2cloth.api.ClothSetting;
import net.minecraft.item.AxeItem;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FabricTreeChopperFiberConfig {
    public boolean fastLeafDecay = true;
    @NotNull
    @ClothSetting.Tooltip("config.fabric-tree-chopper.fullChopDurabilityUsage@Tooltip")
    public FullChopDurabilityMode fullChopDurabilityUsage = FullChopDurabilityMode.BREAK_MID_CHOP;
    public SneakBehavior sneakBehavior = SneakBehavior.DISABLE_CHOPPING;
    public boolean requireLeavesToChop = true;
    @NotNull
    public ChopMode treeChopMode = ChopMode.SINGLE_CHOP;
    public int logSearchLimit = 1000;
    public boolean stopBeforeAxeBreak = true;
    public boolean chopInCreativeMode = false;
    @ClothSetting.RegistryInput("minecraft:item")
    public List<Identifier> axes = Registries.ITEM.stream().filter(AxeItem.class::isInstance).map(Registries.ITEM::getId).toList();
    public boolean variableBreakingTime = false;
}

