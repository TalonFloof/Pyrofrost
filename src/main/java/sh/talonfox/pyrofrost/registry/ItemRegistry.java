package sh.talonfox.pyrofrost.registry;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import sh.talonfox.pyrofrost.items.Thermometor;
import sh.talonfox.pyrofrost.items.WolfFurArmor;
import sh.talonfox.pyrofrost.items.WolfPelt;

public class ItemRegistry {
    public static final Thermometor THERMOMETOR_ITEM = new Thermometor(new FabricItemSettings());
    public static final WolfPelt WOLF_PELT_ITEM = new WolfPelt(new FabricItemSettings());
    public static final ArmorMaterial WOLF_FUR_ARMOR = new WolfFurArmor();
    public static final Item WOLF_FUR_HELMET = new ArmorItem(WOLF_FUR_ARMOR, ArmorItem.Type.HELMET, new Item.Settings());
    public static final Item WOLF_FUR_CHESTPLATE = new ArmorItem(WOLF_FUR_ARMOR, ArmorItem.Type.CHESTPLATE, new Item.Settings());
    public static final Item WOLF_FUR_LEGGINGS = new ArmorItem(WOLF_FUR_ARMOR, ArmorItem.Type.LEGGINGS, new Item.Settings());
    public static final Item WOLF_FUR_PAWS = new ArmorItem(WOLF_FUR_ARMOR, ArmorItem.Type.BOOTS, new Item.Settings());

    public static void init() {
        Registry.register(Registries.ITEM,new Identifier("pyrofrost","thermometor"),THERMOMETOR_ITEM);
        Registry.register(Registries.ITEM,new Identifier("pyrofrost","wolf_pelt"),WOLF_PELT_ITEM);
        Registry.register(Registries.ITEM,new Identifier("pyrofrost","wolf_fur_helmet"),WOLF_FUR_HELMET);
        Registry.register(Registries.ITEM,new Identifier("pyrofrost","wolf_fur_chestplate"),WOLF_FUR_CHESTPLATE);
        Registry.register(Registries.ITEM,new Identifier("pyrofrost","wolf_fur_leggings"),WOLF_FUR_LEGGINGS);
        Registry.register(Registries.ITEM,new Identifier("pyrofrost","wolf_fur_paws"),WOLF_FUR_PAWS);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(content -> {
            content.add(THERMOMETOR_ITEM);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(content -> {
            content.add(WOLF_PELT_ITEM);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(content -> {
            content.add(WOLF_FUR_HELMET);
            content.add(WOLF_FUR_CHESTPLATE);
            content.add(WOLF_FUR_LEGGINGS);
            content.add(WOLF_FUR_PAWS);
        });
    }
}
