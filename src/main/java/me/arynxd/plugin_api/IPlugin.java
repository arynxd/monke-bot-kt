package me.arynxd.plugin_api;

import me.arynxd.monke.Monke;

public interface IPlugin {
    void onEnable(Monke monke);
    void onDisable();
}
