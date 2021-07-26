package me.arynxd.monke.launch

import me.arynxd.monke.objects.exception.LaunchException

fun launcher(fn: LauncherBuilder.() -> Unit): LaunchConfig {
    val launcher = LauncherBuilder()
    fn(launcher)
    launcher.validate()
    return LaunchConfig(
        launcher.configPath!!,
        launcher.isService,
        launcher.isTesting
    )
}

class LauncherBuilder {
    var configPath: String? = null
    var isService = false
    var isTesting = false

    fun validate() {
        if (configPath == null) {
            throw LaunchException("Config path is required")
        }
    }
}

data class LaunchConfig(
    val configPath: String,
    val isService: Boolean,
    val isTesting: Boolean
)

