package com.skaggsm.treechoppermod.mixin;

import com.skaggsm.treechoppermod.FabricTreeChopper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

import static org.spongepowered.asm.mixin.injection.callback.LocalCapture.CAPTURE_FAILSOFT;

@Pseudo
@Mixin(targets = {"net.minecraft.block.LeavesBlock", "me.thonk.croptopia.blocks.LeafCropBlock"})
public abstract class LeavesBlockMixin extends Block {
    public LeavesBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(at = @At("TAIL"), method = {"scheduledTick", "method_9588"}, locals = CAPTURE_FAILSOFT, remap = false)
    private void onScheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        var newState = world.getBlockState(pos);

        var dist = newState.get(Properties.DISTANCE_1_7);
        var persistent = newState.getOrEmpty(Properties.PERSISTENT).orElse(false);

        if (dist == 7 && !persistent && FabricTreeChopper.INSTANCE.getConfig().fastLeafDecay)
            this.randomTick(state, world, pos, random);
    }
}
