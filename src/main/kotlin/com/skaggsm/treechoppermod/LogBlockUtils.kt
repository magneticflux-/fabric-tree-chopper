package com.skaggsm.treechoppermod

import com.skaggsm.treechoppermod.FabricTreeChopper.config
import com.skaggsm.treechoppermod.FullChopDurabilityMode.BREAK_AFTER_CHOP
import com.skaggsm.treechoppermod.FullChopDurabilityMode.BREAK_MID_CHOP
import net.minecraft.block.BlockState
import net.minecraft.block.FungusBlock
import net.minecraft.block.LeavesBlock
import net.minecraft.block.Material
import net.minecraft.block.PillarBlock
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.stat.Stats
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import net.minecraft.world.World

private val BlockState.isNaturalLeaf: Boolean
    get() {
        return (this.contains(LeavesBlock.PERSISTENT) && !this.get(LeavesBlock.PERSISTENT)) || this.block is FungusBlock
    }
private val BlockState.isChoppable: Boolean
    get() {
        return this.block is PillarBlock && (this.material == Material.WOOD || this.material == Material.NETHER_WOOD)
    }

private val directions = linkedSetOf(
    // Above touching face
    Vec3i(0, 1, 0),

    // Above touching edge
    Vec3i(1, 1, 0),
    Vec3i(-1, 1, 0),
    Vec3i(0, 1, 1),
    Vec3i(0, 1, -1),

    // Above touching corner
    Vec3i(1, 1, 1),
    Vec3i(1, 1, -1),
    Vec3i(-1, 1, 1),
    Vec3i(-1, 1, -1),

    // Side touching face
    Vec3i(1, 0, 0),
    Vec3i(-1, 0, 0),
    Vec3i(0, 0, 1),
    Vec3i(0, 0, -1),

    // Side touching edge
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
    var foundNaturalLeaf = false

    logQueue.push(originalBlockPos)

    while (logQueue.isNotEmpty()) {
        val log = logQueue.pop()
        directions.map { log + it }
            .forEach {
                val state = world.getBlockState(it)
                if (originalBlockState.block == state.block && it !in foundLogs)
                    logQueue.push(it)
                else if (state.isNaturalLeaf) {
                    foundNaturalLeaf = true
                }
            }
        foundLogs += log
        if (config.logSearchLimit >= 0 && foundLogs.size > config.logSearchLimit)
        // We've found enough logs, stop now to prevent lag when breaking huge modded trees
            break
    }

    foundLogs -= originalBlockPos

    return if (config.requireLeavesToChop && !foundNaturalLeaf)
        emptySet()
    else
        foundLogs
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
 * If there are other logs, breaks all of them and drops them at [pos].
 */
fun maybeBreakAllLogs(
    originalBlockState: BlockState,
    world: World,
    pos: BlockPos,
    stack: ItemStack,
    miner: LivingEntity
) {
    val logs = findAllLogsAbove(originalBlockState, world, pos)
    var logsBroken = 0

    for (log in logs) {
        if (config.fullChopDurabilityUsage == BREAK_MID_CHOP && stack.count == 0)
            break
        world.breakBlock(log, false, miner)
        logsBroken++

        if (config.fullChopDurabilityUsage == BREAK_AFTER_CHOP || config.fullChopDurabilityUsage == BREAK_MID_CHOP)
            stack.damage(1, miner) { it.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND) }

        if (miner is PlayerEntity) {
            miner.incrementStat(Stats.MINED.getOrCreateStat(originalBlockState.block))
            miner.addExhaustion(0.005f)
        }
    }

    world.spawnEntity(
        ItemEntity(
            world, pos.x + .5, pos.y + .5, pos.z + .5,
            ItemStack(originalBlockState.block.asItem(), logsBroken)
        )
    )
}

fun tryLogBreak(stack: ItemStack, world: World, state: BlockState, pos: BlockPos, miner: LivingEntity) {
    if (state.isChoppable && !(miner.isSneaking && config.sneakToDisable)) {
        when (config.treeChopMode) {
            ChopMode.FULL_CHOP -> maybeBreakAllLogs(state, world, pos, stack, miner)
            ChopMode.SINGLE_CHOP -> maybeSwapFurthestLog(state, world, pos)
            ChopMode.VANILLA_CHOP -> {
            }
        }
    }
}
