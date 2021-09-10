package com.skaggsm.treechoppermod;

public enum FullChopDurabilityMode {
    /**
     * No "extra" durability loss. The regular block breaking still happens and damages the axe once regardless of this mod's interference.
     */
    NO_DURABILITY_LOSS,
    BREAK_AFTER_CHOP,
    BREAK_MID_CHOP,
}
