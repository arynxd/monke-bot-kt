package me.arynxd.plugin_api;

import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KClass;

public class PluginUtils {
    private PluginUtils() { }

    public static <T> KClass<T> getKClass(Class<T> clazz) {
        return JvmClassMappingKt.getKotlinClass(clazz);
    }
}
