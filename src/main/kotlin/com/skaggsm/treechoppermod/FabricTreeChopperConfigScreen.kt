package com.skaggsm.treechoppermod

import io.github.prospector.modmenu.api.ModMenuApi
import me.sargunvohra.mcmods.autoconfig1.AutoConfig
import net.minecraft.client.gui.screen.Screen
import java.util.function.Function

class FabricTreeChopperConfigScreen : ModMenuApi {
    override fun getModId(): String {
        return FabricTreeChopper.MODID
    }

    override fun getConfigScreenFactory(): Function<Screen, out Screen> {
        return Function<Screen, Screen> { screen -> AutoConfig.getConfigScreen(FabricTreeChopperConfig::class.java, screen).get() }
    }
}