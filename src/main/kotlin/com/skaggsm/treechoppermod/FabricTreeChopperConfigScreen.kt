package com.skaggsm.treechoppermod

import com.skaggsm.treechoppermod.FabricTreeChopper.MODID
import com.skaggsm.treechoppermod.FabricTreeChopper.configTree
import com.skaggsm.treechoppermod.FabricTreeChopper.serialize
import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import me.shedaniel.fiber2cloth.api.Fiber2Cloth
import net.minecraft.client.gui.screen.Screen

@Suppress("unused")
class FabricTreeChopperConfigScreen : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<Screen> {
        return ConfigScreenFactory {
            Fiber2Cloth.create(it, MODID, configTree, "config.fabric-tree-chopper.title")
                .setSaveRunnable {
                    serialize()
                }
                .build()
                .screen
        }
    }
}
