package me.arynxd.monke

import me.arynxd.monke.launch.Monke
import me.arynxd.monke.launch.launcher


fun main() {
    val config = launcher {
        configPath = DEFAULT_CONFIG_PATH
    }
    Monke(config)
}


