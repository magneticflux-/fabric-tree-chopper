package com.skaggsm.treechoppermod.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Set;

import static com.skaggsm.treechoppermod.LogBlockUtilsKt.tryLogBreak;

/**
 * Created by Mitchell Skaggs on 8/3/2019.
 */
@Mixin(AxeItem.class)
public class AxeItemMixin extends MiningToolItem {
    protected AxeItemMixin(float float_1, float float_2, ToolMaterial toolMaterial_1, Set<Block> set_1, Settings item$Settings_1) {
        super(float_1, float_2, toolMaterial_1, set_1, item$Settings_1);
    }

    @Override
    public boolean postMine(ItemStack itemStack_1, World world_1, BlockState blockState_1, BlockPos blockPos_1, LivingEntity livingEntity_1) {
        boolean toReturn = super.postMine(itemStack_1, world_1, blockState_1, blockPos_1, livingEntity_1);
        tryLogBreak(itemStack_1, world_1, blockState_1, blockPos_1, livingEntity_1);
        return toReturn;
    }
}
