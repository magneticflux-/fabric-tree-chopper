package com.skaggsm.treechoppermod

import me.sargunvohra.mcmods.autoconfig1.AutoConfig
import me.sargunvohra.mcmods.autoconfig1.serializer.Toml4jConfigSerializer
import net.fabricmc.api.ModInitializer

/**
 * Created by Mitchell Skaggs on 7/30/2019.
 */
object FabricTreeChopper : ModInitializer {
    const val MODID = "fabric-tree-chopper"

    lateinit var config: FabricTreeChopperConfig

    override fun onInitialize() {
        AutoConfig.register(FabricTreeChopperConfig::class.java, ::Toml4jConfigSerializer)
        config = AutoConfig.getConfigHolder(FabricTreeChopperConfig::class.java).config
    }
}
