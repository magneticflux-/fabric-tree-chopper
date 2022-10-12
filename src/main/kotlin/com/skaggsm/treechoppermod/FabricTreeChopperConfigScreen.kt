package com.skaggsm.treechoppermod

import com.skaggsm.treechoppermod.FabricTreeChopper.MODID
import com.skaggsm.treechoppermod.FabricTreeChopper.configTree
import com.skaggsm.treechoppermod.FabricTreeChopper.serialize
import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import io.github.fablabsmc.fablabs.api.fiber.v1.exception.FiberConversionException
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.ListSerializableType
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigTypes
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ListConfigType
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigLeaf
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.PropertyMirror
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import me.shedaniel.fiber2cloth.api.ClothAttributes
import me.shedaniel.fiber2cloth.api.DefaultTypes.IDENTIFIER_TYPE
import me.shedaniel.fiber2cloth.api.Fiber2Cloth
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import java.util.Optional
import java.util.function.Function

class FabricTreeChopperConfigScreen : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<Screen> {
        return ConfigScreenFactory { screen ->
            Fiber2Cloth.create(screen, MODID, configTree, Text.translatable("config.fabric-tree-chopper.title"))
                .setSaveRunnable {
                    serialize()
                }
                .registerLeafEntryFunction(IDENTIFIER_LIST) { leaf: ConfigLeaf<List<String>>, _: ListSerializableType<String>, mirror: PropertyMirror<List<Identifier>>, defaultValue: List<Identifier>, _: Function<List<Identifier>, Optional<Text>> ->
                    val registry = leaf.getAttributeValue(ClothAttributes.REGISTRY_INPUT, IDENTIFIER_TYPE)
                        .flatMap(Registry.REGISTRIES::getOrEmpty).orElse(null)
                    listOf(
                        configEntryBuilder
                            .startStrList(
                                Text.translatable("config.$MODID.${leaf.name}"),
                                IDENTIFIER_LIST.toPlatformType(mirror.value)
                            )
                            .setDefaultValue { IDENTIFIER_LIST.toPlatformType(defaultValue) }
                            .setExpanded(true)
                            .setSaveConsumer { value: List<String> ->
                                mirror.setValue(IDENTIFIER_LIST.toRuntimeType(value))
                            }
                            .setCellErrorSupplier {
                                try {
                                    val id = IDENTIFIER_TYPE.toRuntimeType(it)
                                    if (registry != null && !registry.containsId(id)) {
                                        return@setCellErrorSupplier Optional.of(
                                            Text.translatable(
                                                "config.error.fabric-tree-chopper.identifierNotInRegistry",
                                                id,
                                                registry.key.value
                                            )
                                        )
                                    }
                                    Optional.empty()
                                } catch (e: FiberConversionException) {
                                    Optional.of(
                                        Text.translatable(
                                            "config.error.fabric-tree-chopper.identifierInvalid",
                                            it
                                        )
                                    )
                                }
                            }
                            .build()
                    )
                }
                .build()
                .screen
        }
    }

    companion object {
        val IDENTIFIER_LIST: ListConfigType<List<Identifier>, String> =
            ConfigTypes.makeList(IDENTIFIER_TYPE)
        val configEntryBuilder: ConfigEntryBuilder = ConfigEntryBuilder.create()
    }
}
