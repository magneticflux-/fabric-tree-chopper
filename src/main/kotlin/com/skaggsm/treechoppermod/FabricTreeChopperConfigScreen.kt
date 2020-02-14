package com.skaggsm.treechoppermod

import io.github.prospector.modmenu.api.ConfigScreenFactory
import io.github.prospector.modmenu.api.ModMenuApi
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig
import net.minecraft.client.gui.screen.Screen

@Suppress("unused")
class FabricTreeChopperConfigScreen : ModMenuApi {
    override fun getModId(): String {
        return FabricTreeChopper.MODID
    }

    override fun getModConfigScreenFactory(): ConfigScreenFactory<Screen> {
        return ConfigScreenFactory { AutoConfig.getConfigScreen(FabricTreeChopperConfig::class.java, it).get() }
    }
}
