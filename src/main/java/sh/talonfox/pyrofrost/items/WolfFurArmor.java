package sh.talonfox.pyrofrost.items;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import sh.talonfox.pyrofrost.registry.ItemRegistry;

public class WolfFurArmor implements ArmorMaterial {
    private static final int[] BASE_DURABILITY = new int[] { 11, 16, 15, 13 };
    private static final int[] PROTECTION_AMOUNTS = new int[] { 2, 3, 2, 1 };

    @Override
    public int getDurability(ArmorItem.Type type) {
        return BASE_DURABILITY[type.ordinal()] * 7;
    }

    @Override
    public int getProtection(ArmorItem.Type type) {
        return PROTECTION_AMOUNTS[type.ordinal()];
    }

    @Override
    public int getEnchantability() {
        return 15;
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ITEM_ARMOR_EQUIP_LEATHER;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.ofItems(ItemRegistry.WOLF_PELT_ITEM);
    }

    @Override
    public String getName() {
        return "wolf_fur";
    }

    @Override
    public float getToughness() {
        return 0F;
    }

    @Override
    public float getKnockbackResistance() {
        return 0F;
    }
}
