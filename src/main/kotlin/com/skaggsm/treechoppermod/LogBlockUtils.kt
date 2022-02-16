package com.skaggsm.treechoppermod

import com.google.common.cache.CacheBuilder
import com.skaggsm.treechoppermod.FabricTreeChopper.config
import com.skaggsm.treechoppermod.FullChopDurabilityMode.BREAK_AFTER_CHOP
import com.skaggsm.treechoppermod.FullChopDurabilityMode.BREAK_MID_CHOP
import net.minecraft.SharedConstants
import net.minecraft.block.BlockState
import net.minecraft.block.LeavesBlock
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.stat.Stats
import net.minecraft.tag.BlockTags
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import java.time.Duration

private val BlockState.isNaturalLeaf: Boolean
    get() = (BlockTags.LEAVES.contains(this.block) || BlockTags.WART_BLOCKS.contains(this.block)) &&
        !this.getOrEmpty(LeavesBlock.PERSISTENT).orElse(false)
private val BlockState.isChoppable: Boolean
    get() = BlockTags.LOGS.contains(this.block)

private val Item.id: Identifier
    get() = Registry.ITEM.getId(this)

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
 * A cache of leaf positions to allow blocks that were recently touching leaves to be broken.
 *
 * Key: the [BlockPos] of the log being checked
 * Value: the tick that leaves were last checked for
 */
private val WAS_TOUCHING_NATURAL_LEAVES = CacheBuilder.newBuilder()
    .maximumSize(1024L * 64)
    .expireAfterAccess(Duration.ofMinutes(5))
    .build<BlockPos, Long>()

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

fun findAllLogsAbove(originalBlockState: BlockState, world: World, originalBlockPos: BlockPos): Set<BlockPos> {
    // Leaf cache check
    val currentTick = world.time
    val lastTouchingLeafTime =
        WAS_TOUCHING_NATURAL_LEAVES.getIfPresent(originalBlockPos) ?: Long.MIN_VALUE // Default is before everything
    // Must have been touching more recently than x seconds ago
    val wasRecentlyTouchingNaturalLeaves =
        lastTouchingLeafTime >= (currentTick - SharedConstants.TICKS_PER_SECOND * 10)

    val logQueue = linkedSetOf<BlockPos>()
    val foundLogs = linkedSetOf<BlockPos>()
    var foundNaturalLeaf = wasRecentlyTouchingNaturalLeaves

    logQueue.push(originalBlockPos)

    while (logQueue.isNotEmpty()) {
        val log = logQueue.pop()
        directions.map(log::plus)
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
    // Cache each log found to remember leaves later
    if (foundNaturalLeaf)
        for (log in foundLogs) {
            WAS_TOUCHING_NATURAL_LEAVES.put(log.toImmutable(), currentTick)
        }

    // The original block was already broken, skip returning it
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
 * If we should stop breaking logs (the axe in [stack] only has 2 durability left, one for the vanilla log break)
 */
private fun shouldStop(stack: ItemStack): Boolean {
    return config.stopBeforeAxeBreak && (stack.maxDamage - stack.damage - 1) < 2
}

/**
 * If there are other logs, breaks all of them and drops them at [pos].
 */
fun maybeBreakAllLogs(
    originalBlockState: BlockState,
    world: World,
    pos: BlockPos,
    miner: PlayerEntity
) {
    val stack = miner.mainHandStack
    val logs = findAllLogsAbove(originalBlockState, world, pos)
    var logsBroken = 0

    for (log in logs) {
        // Check if the axe has broken and abort if so
        if (stack.count == 0)
            break
        world.breakBlock(log, false, miner)
        logsBroken++

        miner.incrementStat(Stats.MINED.getOrCreateStat(originalBlockState.block))
        miner.addExhaustion(0.005f)

        // Do the damage incrementally
        if (config.fullChopDurabilityUsage == BREAK_MID_CHOP) {
            stack.damage(1, miner) { it.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND) }
            if (shouldStop(stack))
                break
        }
    }

    // Do all the damage at once after the whole tree is chopped
    if (config.fullChopDurabilityUsage == BREAK_AFTER_CHOP) {
        for (i in 0 until logsBroken) {
            stack.damage(1, miner) { it.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND) }
            if (shouldStop(stack))
                break
        }
    }

    world.spawnEntity(
        ItemEntity(
            world, pos.x + .5, pos.y + .5, pos.z + .5,
            ItemStack(originalBlockState.block.asItem(), logsBroken)
        )
    )
}

fun canBreakLog(player: PlayerEntity, state: BlockState): Boolean {
    return state.isChoppable &&
        config.sneakBehavior.shouldChop(player.isSneaking) &&
        !(player.isCreative && !config.chopInCreativeMode) &&
        player.mainHandStack.item.id in config.axes
}

fun tryLogBreak(world: World, player: PlayerEntity, pos: BlockPos, state: BlockState) {
    if (canBreakLog(player, state)) {
        when (config.treeChopMode) {
            ChopMode.FULL_CHOP -> maybeBreakAllLogs(state, world, pos, player)
            ChopMode.SINGLE_CHOP -> maybeSwapFurthestLog(state, world, pos)
            ChopMode.VANILLA_CHOP -> {
            }
        }
    }
}
