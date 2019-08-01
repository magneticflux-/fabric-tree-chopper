package com.skaggsm.treechoppermod.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.LogBlock;
import net.minecraft.block.PillarBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LogBlock.class)
public class LogBlockMixin extends PillarBlock {

    public LogBlockMixin(Settings block$Settings_1) {
        super(block$Settings_1);
    }

    @Override
    public void onStacksDropped(BlockState blockState_1, World world_1, BlockPos blockPos_1, ItemStack itemStack_1) {
        super.onStacksDropped(blockState_1, world_1, blockPos_1, itemStack_1);
        int top = 1;
        if (blockState_1.equals(world_1.getBlockState(new BlockPos(blockPos_1.getX(), blockPos_1.getY() + top, blockPos_1.getZ())))) {
            while (blockState_1.equals(world_1.getBlockState(new BlockPos(blockPos_1.getX(), blockPos_1.getY() + top, blockPos_1.getZ())))) {
                top++;
            }
            top--;
            BlockState blockStateToMoveDown = world_1.getBlockState(new BlockPos(blockPos_1.getX(), blockPos_1.getY() + top, blockPos_1.getZ()));
            world_1.clearBlockState(new BlockPos(blockPos_1.getX(), blockPos_1.getY() + top, blockPos_1.getZ()), true);
            world_1.setBlockState(blockPos_1, blockStateToMoveDown, 0);
        }
    }
}
