package com.skaggsm.treechoppermod

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.AnnotatedSettings
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.FiberSerialization
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.JanksonValueSerializer
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigBranch
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigTree
import me.shedaniel.fiber2cloth.api.DefaultTypes
import me.shedaniel.fiber2cloth.api.Fiber2Cloth
import net.fabricmc.api.EnvType
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.item.Item
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

private val Item.id: Identifier
    get() = Registry.ITEM.getId(this)

/**
 * Created by Mitchell Skaggs on 7/30/2019.
 */
object FabricTreeChopper : ModInitializer {
    const val MODID = "fabric-tree-chopper"

    private val serializer = JanksonValueSerializer(false)
    private val configFile: Path = Paths.get("config", "fabric-tree-chopper.json")

    lateinit var config: FabricTreeChopperFiberConfig
    lateinit var configTree: ConfigBranch

    override fun onInitialize() {
        config = FabricTreeChopperFiberConfig()

        val settingsBuilder = AnnotatedSettings.builder()
        if (FabricLoader.getInstance().environmentType == EnvType.CLIENT)
            Fiber2Cloth.configure(settingsBuilder)
        settingsBuilder.registerTypeMapping(Identifier::class.java, DefaultTypes.IDENTIFIER_TYPE)
        val settings = settingsBuilder.build()

        configTree = ConfigTree.builder().applyFromPojo(config, settings).build()

        if (Files.notExists(configFile)) {
            serialize()
        }

        deserialize()

        PlayerBlockBreakEvents.AFTER.register(PlayerBlockBreakEvents.After { world, player, pos, state, _ ->
            if (config.chopInCreativeMode || !player.isCreative) {
                val breakingStack = player.mainHandStack
                if (breakingStack.item.id in config.axes)
                    tryLogBreak(breakingStack, world, state, pos, player)
            }
        })
    }

    fun serialize() {
        FiberSerialization.serialize(
            configTree,
            Files.newOutputStream(configFile, StandardOpenOption.WRITE, StandardOpenOption.CREATE),
            serializer
        )
    }

    fun deserialize() {
        FiberSerialization.deserialize(
            configTree,
            Files.newInputStream(configFile, StandardOpenOption.READ),
            serializer
        )
    }
}
