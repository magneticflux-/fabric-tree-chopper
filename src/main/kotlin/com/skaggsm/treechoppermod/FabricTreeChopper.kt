package com.skaggsm.treechoppermod

import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.FiberSerialization
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.JanksonValueSerializer
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigBranch
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigTree
import net.fabricmc.api.ModInitializer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*

/**
 * Created by Mitchell Skaggs on 7/30/2019.
 */
object FabricTreeChopper : ModInitializer {
    const val MODID = "fabric-tree-chopper"

    val serializer = JanksonValueSerializer(false)
    val configFile = Paths.get("config", "fabric-tree-chopper.json")
    val oldConfigFile = Paths.get("config", "fabric-tree-chopper.toml")

    lateinit var config: FabricTreeChopperFiberConfig
    lateinit var configTree: ConfigBranch

    override fun onInitialize() {
        config = FabricTreeChopperFiberConfig()

        if (Files.exists(oldConfigFile)) {
            updateConfigTree()
        }

        configTree = ConfigTree.builder().applyFromPojo(config).build()

        if (Files.notExists(configFile)) {
            serialize()
        }

        deserialize()
    }

    fun updateConfigTree() {
        val scan = Scanner(oldConfigFile)
        val regex = Regex("(.+) = \"?(\\w+)\"?")
        var line = ""
        var matchResult: MatchResult
        while (scan.hasNextLine()) {
            line = scan.nextLine()
            matchResult = regex.find(line)!!
            when (matchResult.groupValues[1]) {
                "fastLeafDecay" -> {config.fastLeafDecay = matchResult.groupValues[2].toBoolean()}
                "treeChopMode" -> {config.treeChopMode = ChopMode.valueOf(matchResult.groupValues[2])}
                "fullChopDurabilityUsage" -> {config.fullChopDurabilityUsage = FullChopDurabilityMode.valueOf(matchResult.groupValues[2])}
                "sneakToDisable" -> {config.sneakToDisable = matchResult.groupValues[2].toBoolean()}
                "requireLeavesToChop" -> {config.requireLeavesToChop = matchResult.groupValues[2].toBoolean()}
            }
        }
        Files.delete(oldConfigFile)
    }

    fun serialize() {
        FiberSerialization.serialize(configTree, Files.newOutputStream(configFile, StandardOpenOption.WRITE, StandardOpenOption.CREATE), serializer)
    }

    fun deserialize() {
        FiberSerialization.deserialize(configTree, Files.newInputStream(configFile, StandardOpenOption.READ), serializer)
    }
}
