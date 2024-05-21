package com.sammy.hungy;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@SuppressWarnings("unused")
@Mod(Hungy.mod_id)
public class Hungy {
    public static final String mod_id = "hungy";
    public static final Logger logger = LogUtils.getLogger();
    
    // This normally doesn't yell about being unused. I wonder if I even need this as a mod hehe.
    public Hungy() {
        MinecraftForge.EVENT_BUS.register(this);
    }
}
