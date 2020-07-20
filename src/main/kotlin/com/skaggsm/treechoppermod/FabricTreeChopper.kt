package com.skaggsm.treechoppermod

import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.FiberSerialization
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.JanksonValueSerializer
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigBranch
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigTree
import net.fabricmc.api.ModInitializer
import java.nio.file.Files
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.StandardOpenOption

/**
 * Created by Mitchell Skaggs on 7/30/2019.
 */
object FabricTreeChopper : ModInitializer {
    const val MODID = "fabric-tree-chopper"

    val serializer = JanksonValueSerializer(false)
    val configFile = Path.of("config","testconfig.json")

    lateinit var config: FabricTreeChopperFiberConfig
    lateinit var configTree: ConfigBranch

    override fun onInitialize() {
        config = FabricTreeChopperFiberConfig()
        configTree = ConfigTree.builder().applyFromPojo(config).build()

        if (Files.notExists(configFile)) {
            serialize()
        }

        deserialize()
    }

    fun serialize() {
        FiberSerialization.serialize(configTree, Files.newOutputStream(configFile, StandardOpenOption.WRITE, StandardOpenOption.CREATE), serializer)
    }

    fun deserialize() {
        FiberSerialization.deserialize(configTree, Files.newInputStream(configFile, StandardOpenOption.READ), serializer)
    }
}
