package com.skaggsm.treechoppermod

import net.minecraft.block.BlockState
import net.minecraft.entity.ItemEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import net.minecraft.world.World
import java.util.*

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
        // Reversed so the top gets added to the output list last and it picked first. Makes log breaking look more "natural".
        .reversed()

/**
 * If there are other logs, finds the furthest one and swaps it into [blockPos].
 */
fun maybeSwapFurthestLog(originalBlockState: BlockState, world: World, blockPos: BlockPos) {
    val furthestLog = findFurthestLog(originalBlockState, world, blockPos)

    if (furthestLog != null) {
        world.clearBlockState(furthestLog, false)
        world.setBlockState(blockPos, originalBlockState)
    }
}

private fun findFurthestLog(originalBlockState: BlockState, world: World, blockPos: BlockPos): BlockPos? {
    val logs = findAllLogsAbove(originalBlockState, world, blockPos)
    return logs.lastOrNull()
}

private fun findAllLogsAbove(originalBlockState: BlockState, world: World, originalBlockPos: BlockPos, distance: Int = 1): Set<BlockPos> {
    val logQueue = linkedSetOf<BlockPos>()
    val foundLogs = linkedSetOf<BlockPos>()

    logQueue.push(originalBlockPos)

    while (logQueue.isNotEmpty()) {
        val log = logQueue.pop()
        directions.map { log + it }
                .filter { originalBlockState.block == world.getBlockState(it).block && it !in foundLogs }
                .forEach {
                    logQueue.push(it)
                }
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
fun maybeBreakAllLogs(originalBlockState: BlockState, world: World, blockPos: BlockPos, itemStack_1: ItemStack) {
    val logs = findAllLogsAbove(originalBlockState, world, blockPos)

    for (log in logs)
        world.clearBlockState(log, false)

    world.spawnEntity(ItemEntity(
            world, blockPos.x + .5, blockPos.y + .5, blockPos.z + .5,
            ItemStack(originalBlockState.block.asItem(), logs.size)
    ))

    // Damage needs the player entity, our ItemStack is a copy so it doesn't reflect in-inv.
    /*
    println("before: ${itemStack_1.damage}")
    val ret = itemStack_1.damage(1, world.random, null)
    println("$ret after: ${itemStack_1.damage}")
    itemStack_1.damage = 10
    */
}
