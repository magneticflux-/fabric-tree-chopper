package com.skaggsm.treechoppermod.mixin;

import com.skaggsm.treechoppermod.FabricTreeChopper;
import net.minecraft.block.BlockState;
import net.minecraft.block.LogBlock;
import net.minecraft.block.PillarBlock;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;

@Mixin(LogBlock.class)
public class LogBlockMixin extends PillarBlock {

    public LogBlockMixin(Settings block$Settings_1) {
        super(block$Settings_1);
    }

    @Override
    public void onStacksDropped(BlockState blockState_1, World world_1, BlockPos blockPos_1, ItemStack itemStack_1) {
        super.onStacksDropped(blockState_1, world_1, blockPos_1, itemStack_1);
        ArrayList<BlockPos> trackedLogs = getTreePositions(blockPos_1, world_1, new ArrayList<>(), blockState_1);
        removeDuplicates(trackedLogs);
        if (!trackedLogs.isEmpty()) {
            if (FabricTreeChopper.config.getFullTreeChop()) {
                for (BlockPos log : trackedLogs) {
                    world_1.clearBlockState(log, true);
                }
                world_1.spawnEntity(new ItemEntity(world_1, blockPos_1.getX(), blockPos_1.getY(), blockPos_1.getZ(), new ItemStack(this.asItem(), trackedLogs.size() - 1)));
            } else {
                int greatestDistance = 0;
                BlockPos farthestLog = blockPos_1;
                for (BlockPos log : trackedLogs) {
                    if (greatestDistance < log.getManhattanDistance(blockPos_1)) {
                        greatestDistance = log.getManhattanDistance(blockPos_1);
                        farthestLog = log;
                    }
                }
                world_1.setBlockState(blockPos_1, world_1.getBlockState(farthestLog));
                world_1.clearBlockState(farthestLog, true);
            }
        }
    }

    private ArrayList<BlockPos> getNewAdjacentLogs(ArrayList<BlockPos> alreadyLoggedLogs, World world, BlockPos blockPos, BlockState treeType) {
        ArrayList<BlockPos> newLogs = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (!(x == 0 && y == 0 && z == 0) && treeType.getBlock().equals(world.getBlockState(new BlockPos(blockPos.getX() + x, blockPos.getY() + y, blockPos.getZ() + z)).getBlock())) {
                        newLogs.add(new BlockPos(blockPos.getX() + x, blockPos.getY() + y, blockPos.getZ() + z));
                    }
                }
            }
        }
        removeAll(newLogs, alreadyLoggedLogs);
        removeDuplicates(newLogs);
        return newLogs;
    }

    private ArrayList<BlockPos> getTreePositions(BlockPos basePosition, World world, ArrayList<BlockPos> excluding, BlockState treeType) {
        excluding.add(basePosition);
        ArrayList<BlockPos> nextLogs = getNewAdjacentLogs(excluding, world, basePosition, treeType);
        excluding.addAll(getNewAdjacentLogs(excluding, world, basePosition, treeType));
        for (BlockPos nextLog : nextLogs) {
            excluding.addAll(getTreePositions(nextLog, world, excluding, treeType));
        }
        removeDuplicates(excluding);
        return excluding;
    }

    private void removeAll(ArrayList<BlockPos> base, ArrayList<BlockPos> positionsToRemove) {
        for (BlockPos blockPos : positionsToRemove) {
            for (int j = 0; j < base.size(); j++) {
                if (blockPos.getX() == base.get(j).getX() && blockPos.getY() == base.get(j).getY() && blockPos.getZ() == base.get(j).getZ()) {
                    base.remove(j);
                }
            }
        }
    }

    private void removeDuplicates(ArrayList<BlockPos> list) {
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                if (list.get(i).getX() == list.get(j).getX() && list.get(i).getY() == list.get(j).getY() && list.get(i).getZ() == list.get(j).getZ()) {
                    list.remove(j);
                    j++;
                }
            }
        }
    }
}
