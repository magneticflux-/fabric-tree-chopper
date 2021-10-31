package com.skaggsm.treechoppermod;

public enum SneakBehavior {
    DISABLE_CHOPPING {
        @Override
        public boolean shouldChop(boolean sneaking) {
            return !sneaking;
        }
    },
    ENABLE_CHOPPING {
        @Override
        public boolean shouldChop(boolean sneaking) {
            return sneaking;
        }
    },
    IGNORE {
        @Override
        public boolean shouldChop(boolean sneaking) {
            return true;
        }
    };

    public abstract boolean shouldChop(boolean sneaking);
}
