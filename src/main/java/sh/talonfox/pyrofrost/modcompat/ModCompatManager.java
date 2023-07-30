package sh.talonfox.pyrofrost.modcompat;

import net.fabricmc.loader.api.FabricLoader;
import sh.talonfox.pyrofrost.Pyrofrost;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class ModCompatManager {
    public static HashMap<String, String> modules = new HashMap<>();
    public static ArrayList<String> availableMods = new ArrayList<>();
    public static void init() {
        modules.put("dehydration","sh.talonfox.pyrofrost.modcompat.DehydrationCompat");
        modules.put("seasons","sh.talonfox.pyrofrost.modcompat.FabricSeasonsCompat");
        for(String i : modules.keySet()) {
            if (FabricLoader.getInstance().isModLoaded(i)) {
                availableMods.add(i);
                runMethod(i,"init");
            }
        }
    }
    public static Object runMethod(String name, String method, Object... args) {
        if (FabricLoader.getInstance().isModLoaded(name)) {
            Class<?> clazz = null;
            try {
                clazz = Pyrofrost.class.getClassLoader().loadClass(modules.get(name));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            for(Method i : clazz.getDeclaredMethods()) {
                if(i.getName().equals(method)) {
                    try {
                        return i.invoke(null, args);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return null;
        }
        return null;
    }
    public static boolean isModAvailable(String mod) {
        return availableMods.contains(mod);
    }
}
