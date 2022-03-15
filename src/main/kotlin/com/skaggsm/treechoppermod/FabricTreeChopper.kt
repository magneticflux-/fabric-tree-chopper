package com.skaggsm.treechoppermod

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.AnnotatedSettings
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigTypes
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.StringConfigType
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
import net.minecraft.util.Identifier
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.div

/**
 * Created by Mitchell Skaggs on 7/30/2019.
 */
object FabricTreeChopper : ModInitializer {
    const val MODID = "fabric-tree-chopper"
    private val IDENTIFIER_TYPE: StringConfigType<Identifier> = ConfigTypes.STRING
        .withPattern("(?>[a-z0-9_.-]+:)?[a-z0-9/._-]+")
        .derive(Identifier::class.java, ::Identifier, Identifier::toString)

    private val serializer = JanksonValueSerializer(false)
    private val configFile: Path = FabricLoader.getInstance().configDir / "fabric-tree-chopper.json"

    lateinit var config: FabricTreeChopperFiberConfig
    lateinit var configTree: ConfigBranch

    override fun onInitialize() {
        config = FabricTreeChopperFiberConfig()

        val settingsBuilder = AnnotatedSettings.builder()
        if (FabricLoader.getInstance().environmentType == EnvType.CLIENT) {
            Fiber2Cloth.configure(settingsBuilder)
            settingsBuilder.registerTypeMapping(Identifier::class.java, DefaultTypes.IDENTIFIER_TYPE)
        } else {
            settingsBuilder.registerTypeMapping(Identifier::class.java, IDENTIFIER_TYPE)
        }
        val settings = settingsBuilder.build()

        configTree = ConfigTree.builder().applyFromPojo(config, settings).build()

        if (Files.notExists(configFile)) {
            serialize()
        }

        deserialize()

        PlayerBlockBreakEvents.AFTER.register(
            PlayerBlockBreakEvents.After { world, player, pos, state, _ ->
                tryLogBreak(world, player, pos, state)
            }
        )
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
