package com.skaggsm.treechoppermod.mixin;

import com.skaggsm.treechoppermod.FabricTreeChopper;
import net.minecraft.block.BlockState;
import net.minecraft.block.LogBlock;
import net.minecraft.block.PillarBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import static com.skaggsm.treechoppermod.LogBlockUtilsKt.maybeBreakAllLogs;
import static com.skaggsm.treechoppermod.LogBlockUtilsKt.maybeSwapFurthestLog;

@Mixin(LogBlock.class)
public class LogBlockMixin extends PillarBlock {

    public LogBlockMixin(Settings block$Settings_1) {
        super(block$Settings_1);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStacksDropped(BlockState blockState_1, World world_1, BlockPos blockPos_1, ItemStack itemStack_1) {
        super.onStacksDropped(blockState_1, world_1, blockPos_1, itemStack_1);

        if (FabricTreeChopper.config.getFullTreeChop()) {
            maybeBreakAllLogs(blockState_1, world_1, blockPos_1, itemStack_1);
        } else {
            maybeSwapFurthestLog(blockState_1, world_1, blockPos_1);
        }
    }
}
