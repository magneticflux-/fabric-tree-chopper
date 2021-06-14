package com.skaggsm.treechoppermod.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.tag.Tag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import static com.skaggsm.treechoppermod.LogBlockUtilsKt.tryLogBreak;

/**
 * Created by Mitchell Skaggs on 8/3/2019.
 */
@Mixin(AxeItem.class)
public class AxeItemMixin extends MiningToolItem {
    protected AxeItemMixin(float attackDamage, float attackSpeed, ToolMaterial material, Tag<Block> effectiveBlocks, Settings settings) {
        super(attackDamage, attackSpeed, material, effectiveBlocks, settings);
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        boolean toReturn = super.postMine(stack, world, state, pos, miner);
        tryLogBreak(stack, world, state, pos, miner);
        return toReturn;
    }
}
