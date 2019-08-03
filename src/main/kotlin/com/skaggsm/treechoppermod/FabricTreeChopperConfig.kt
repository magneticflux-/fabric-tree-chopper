package com.skaggsm.treechoppermod

import me.sargunvohra.mcmods.autoconfig1.ConfigData
import me.sargunvohra.mcmods.autoconfig1.annotation.Config

@Config(name = FabricTreeChopper.MODID)
class FabricTreeChopperConfig : ConfigData {
    var fastLeafDecay: Boolean = false
    var treeChopMode: ChopMode = ChopMode.SINGLE_CHOP
}

enum class ChopMode {
    FULL_CHOP,
    SINGLE_CHOP,
    VANILLA_CHOP
}
