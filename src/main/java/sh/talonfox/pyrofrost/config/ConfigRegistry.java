package sh.talonfox.pyrofrost.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import sh.talonfox.pyrofrost.Pyrofrost;

public class ConfigRegistry {
    public static void init() {
        AutoConfig.register(PyrofrostConfig.class, JanksonConfigSerializer::new);
        Pyrofrost.CONFIG = AutoConfig.getConfigHolder(PyrofrostConfig.class).getConfig();
    }
}
