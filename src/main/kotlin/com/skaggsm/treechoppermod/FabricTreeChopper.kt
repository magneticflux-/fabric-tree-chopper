package com.skaggsm.treechoppermod

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig
import me.sargunvohra.mcmods.autoconfig1u.ConfigHolder
import me.sargunvohra.mcmods.autoconfig1u.serializer.Toml4jConfigSerializer
import net.fabricmc.api.ModInitializer

/**
 * Created by Mitchell Skaggs on 7/30/2019.
 */
object FabricTreeChopper : ModInitializer {
    const val MODID = "fabric-tree-chopper"

    private lateinit var config_: ConfigHolder<FabricTreeChopperConfig>
    val config: FabricTreeChopperConfig
        get() = config_.config

    override fun onInitialize() {
        AutoConfig.register(FabricTreeChopperConfig::class.java, ::Toml4jConfigSerializer)
        config_ = AutoConfig.getConfigHolder(FabricTreeChopperConfig::class.java)
    }
}
