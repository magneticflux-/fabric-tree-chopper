package com.skaggsm.treechoppermod.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

import static org.spongepowered.asm.mixin.injection.callback.LocalCapture.CAPTURE_FAILSOFT;

@Mixin(LeavesBlock.class)
public abstract class LeavesBlockMixin {
    @Shadow
    public abstract void onRandomTick(BlockState blockState_1, World world_1, BlockPos blockPos_1, Random random_1);

    @Inject(at = @At("TAIL"), method = "onScheduledTick", locals = CAPTURE_FAILSOFT)
    private void onScheduledTick(BlockState blockState_1, World world_1, BlockPos blockPos_1, Random random_1, CallbackInfo ci) {
        this.onRandomTick(blockState_1, world_1, blockPos_1, random_1);
    }
}
