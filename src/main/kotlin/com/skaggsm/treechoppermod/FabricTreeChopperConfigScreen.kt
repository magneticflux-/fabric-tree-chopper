package com.skaggsm.treechoppermod

import io.github.prospector.modmenu.api.ModMenuApi
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig
import net.minecraft.client.gui.screen.Screen
import java.util.function.Function

@Suppress("unused")
class FabricTreeChopperConfigScreen : ModMenuApi {
    override fun getModId(): String {
        return FabricTreeChopper.MODID
    }

    override fun getConfigScreenFactory(): Function<Screen, out Screen> {
        return Function { AutoConfig.getConfigScreen(FabricTreeChopperConfig::class.java, it).get() }
    }
}
