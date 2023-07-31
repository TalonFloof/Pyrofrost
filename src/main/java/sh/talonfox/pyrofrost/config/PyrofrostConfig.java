package sh.talonfox.pyrofrost.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = "pyrofrost")
public class PyrofrostConfig implements ConfigData {
    @ConfigEntry.Category("client")
    @Comment("Relative X Position of the Temperature Icon")
    public int Client_IconX = 0;
    @ConfigEntry.Category("client")
    @Comment("Relative Y Position of the Temperature Icon")
    public int Client_IconY = 0;
    @ConfigEntry.Category("client")
    @Comment("Use Fahrenheit instead of Celsius")
    public boolean Client_UseFahrenheit = false;
    @ConfigEntry.Category("server")
    @Comment("For fuzzy folk :3 (or to make things easier)")
    public boolean Server_AddBaseHeadInsulation = false;
    @ConfigEntry.Category("server")
    @Comment("Sets the air temperature modifier when using an ice pack")
    @ConfigEntry.BoundedDiscrete(min=0L,max=100L)
    public long Server_IcePackModifier = 60L;
    @ConfigEntry.Category("server")
    @Comment("Sets a piece of Wolf Fur Armor's Insulation Modifier")
    public float Server_WolfFurArmor_Insulation = 4.0F;
    @ConfigEntry.Category("server")
    @Comment("Sets a piece of Netherite Armor's Insulation Modifier")
    public float Server_NetheriteArmor_Insulation = 8.0F;
    @ConfigEntry.Category("server")
    @Comment("Sets a piece of Netherite Armor's Radiation Resistance Modifier")
    @ConfigEntry.BoundedDiscrete(min=0L,max=100L)
    public long Server_NetheriteArmor_Resistance = 20L;
}
