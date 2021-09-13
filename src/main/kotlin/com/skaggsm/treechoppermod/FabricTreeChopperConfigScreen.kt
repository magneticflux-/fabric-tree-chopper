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
import me.shedaniel.fiber2cloth.api.DefaultTypes
import me.shedaniel.fiber2cloth.api.Fiber2Cloth
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier
import java.util.Optional
import java.util.function.Function

class FabricTreeChopperConfigScreen : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<Screen> {
        return ConfigScreenFactory { screen ->
            Fiber2Cloth.create(screen, MODID, configTree, "config.fabric-tree-chopper.title")
                .setSaveRunnable {
                    serialize()
                }
                .registerLeafEntryFunction(IDENTIFIER_LIST) { leaf: ConfigLeaf<List<String>>, type: ListSerializableType<String>, mirror: PropertyMirror<List<Identifier>>, defaultValue: List<Identifier>, errorSupplier: Function<List<Identifier>, Optional<Text>> ->
                    listOf(
                        configEntryBuilder
                            .startStrList(
                                TranslatableText("config.$MODID.${leaf.name}"),
                                IDENTIFIER_LIST.toPlatformType(mirror.value)
                            )
                            .setDefaultValue { IDENTIFIER_LIST.toPlatformType(defaultValue) }
                            .setExpanded(true)
                            .setSaveConsumer { value: List<String> ->
                                mirror.setValue(IDENTIFIER_LIST.toRuntimeType(value))
                            }
                            .setErrorSupplier {
                                try {
                                    errorSupplier.apply(IDENTIFIER_LIST.toRuntimeType(it))
                                } catch (e: FiberConversionException) {
                                    Optional.of(LiteralText(e.localizedMessage))
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
            ConfigTypes.makeList(DefaultTypes.IDENTIFIER_TYPE)
        val configEntryBuilder: ConfigEntryBuilder = ConfigEntryBuilder.create()
    }
}
