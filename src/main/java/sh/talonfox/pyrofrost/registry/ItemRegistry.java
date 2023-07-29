package sh.talonfox.pyrofrost.registry;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import sh.talonfox.pyrofrost.items.Thermometor;

public class ItemRegistry {
    public static final Thermometor THERMOMETOR_ITEM = new Thermometor(new FabricItemSettings());

    public static void init() {
        Registry.register(Registries.ITEM,new Identifier("pyrofrost","thermometor"),THERMOMETOR_ITEM);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(content -> {
            content.add(THERMOMETOR_ITEM);
        });
    }
}
