package com.skaggsm.treechoppermod

import com.skaggsm.treechoppermod.FabricTreeChopper.config
import com.skaggsm.treechoppermod.FullChopDurabilityMode.BREAK_AFTER_CHOP
import com.skaggsm.treechoppermod.FullChopDurabilityMode.BREAK_MID_CHOP
import net.minecraft.block.BlockState
import net.minecraft.block.LogBlock
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import net.minecraft.world.World

private val directions = linkedSetOf(
        // Above top
        Vec3i(0, 1, 0),

        // Above touching
        Vec3i(1, 1, 0),
        Vec3i(-1, 1, 0),
        Vec3i(0, 1, 1),
        Vec3i(0, 1, -1),

        // Above diagonal
        Vec3i(1, 1, 1),
        Vec3i(1, 1, -1),
        Vec3i(-1, 1, 1),
        Vec3i(-1, 1, -1),

        // Side touching
        Vec3i(1, 0, 0),
        Vec3i(-1, 0, 0),
        Vec3i(0, 0, 1),
        Vec3i(0, 0, -1),

        // Side diagonal
        Vec3i(1, 0, 1),
        Vec3i(1, 0, -1),
        Vec3i(-1, 0, 1),
        Vec3i(-1, 0, -1)
)
        // Reversed so that the top gets added to the output list last and gets picked first. Makes log breaking look more "natural".
        .reversed()

/**
 * If there are other logs, finds the furthest one and swaps it into [blockPos].
 */
fun maybeSwapFurthestLog(originalBlockState: BlockState, world: World, blockPos: BlockPos) {
    val furthestLog = findFurthestLog(originalBlockState, world, blockPos)

    if (furthestLog != null) {
        world.breakBlock(furthestLog, false)
        world.setBlockState(blockPos, originalBlockState)
    }
}

private fun findFurthestLog(originalBlockState: BlockState, world: World, blockPos: BlockPos): BlockPos? {
    val logs = findAllLogsAbove(originalBlockState, world, blockPos)
    return logs.lastOrNull()
}

private fun findAllLogsAbove(originalBlockState: BlockState, world: World, originalBlockPos: BlockPos): Set<BlockPos> {
    val logQueue = linkedSetOf<BlockPos>()
    val foundLogs = linkedSetOf<BlockPos>()

    logQueue.push(originalBlockPos)

    while (logQueue.isNotEmpty()) {
        val log = logQueue.pop()
        directions.map { log + it }
                .filter { originalBlockState.block == world.getBlockState(it).block && it !in foundLogs }
                .forEach { logQueue.push(it) }
        foundLogs += log
    }

    return foundLogs - originalBlockPos
}

private fun <E> LinkedHashSet<E>.pop(): E {
    val elem = first()
    remove(elem)
    return elem
}

private fun <E> LinkedHashSet<E>.push(elem: E) {
    this.add(elem)
}

private operator fun BlockPos.plus(it: Vec3i): BlockPos {
    return this.add(it)
}

/**
 * If there are other logs, breaks all of them and drops them at [blockPos].
 */
fun maybeBreakAllLogs(originalBlockState: BlockState, world: World, blockPos: BlockPos, itemStack_1: ItemStack, livingEntity: LivingEntity) {
    val logs = findAllLogsAbove(originalBlockState, world, blockPos)
    var logsBroken = 0

    for (log in logs) {
        if (config.fullChopDurabilityUsage == BREAK_MID_CHOP && itemStack_1.count == 0)
            break
        world.breakBlock(log, false)
        logsBroken++

        if (config.fullChopDurabilityUsage == BREAK_AFTER_CHOP || config.fullChopDurabilityUsage == BREAK_MID_CHOP)
            itemStack_1.damage(1, livingEntity, { it.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND) })
    }

    world.spawnEntity(ItemEntity(
            world, blockPos.x + .5, blockPos.y + .5, blockPos.z + .5,
            ItemStack(originalBlockState.block.asItem(), logsBroken)
    ))
}

fun tryLogBreak(itemStack_1: ItemStack, world_1: World, blockState_1: BlockState, blockPos_1: BlockPos, livingEntity_1: LivingEntity) {
    if (blockState_1.block is LogBlock) {
        when (config.treeChopMode) {
            ChopMode.FULL_CHOP -> maybeBreakAllLogs(blockState_1, world_1, blockPos_1, itemStack_1, livingEntity_1)
            ChopMode.SINGLE_CHOP -> maybeSwapFurthestLog(blockState_1, world_1, blockPos_1)
            ChopMode.VANILLA_CHOP -> {
            }
        }
    }
}
