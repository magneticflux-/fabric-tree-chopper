package com.skaggsm.treechoppermod

import com.skaggsm.treechoppermod.FabricTreeChopper.configTree
import com.skaggsm.treechoppermod.FabricTreeChopper.serialize
import io.github.prospector.modmenu.api.ConfigScreenFactory
import io.github.prospector.modmenu.api.ModMenuApi
import me.shedaniel.fiber2cloth.api.Fiber2Cloth
import net.minecraft.client.gui.screen.Screen

@Suppress("unused")
class FabricTreeChopperConfigScreen : ModMenuApi {
    override fun getModId(): String {
        return FabricTreeChopper.MODID
    }

    override fun getModConfigScreenFactory(): ConfigScreenFactory<Screen> {
        return ConfigScreenFactory {
            Fiber2Cloth.create(it, modId, configTree, "")
                    .setSaveRunnable {
                        serialize()
                    }
                    .build()
                    .screen
        }
    }
}
