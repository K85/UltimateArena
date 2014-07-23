package net.dmulloy2.ultimatearena.types;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import lombok.Getter;
import lombok.NonNull;
import net.dmulloy2.types.EnchantmentType;
import net.dmulloy2.types.MyMaterial;
import net.dmulloy2.types.Reloadable;
import net.dmulloy2.ultimatearena.UltimateArena;
import net.dmulloy2.util.FormatUtil;
import net.dmulloy2.util.ItemUtil;
import net.dmulloy2.util.NumberUtil;
import net.dmulloy2.util.Util;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * @author dmulloy2
 */

@Getter
public final class ArenaClass implements Reloadable
{
	private boolean needsPermission;
	private String permissionNode;

	private List<ItemStack> armor = new ArrayList<>();
	private List<ItemStack> tools = new ArrayList<>();

	private boolean useHelmet = true;

	// Essentials Integration
	private String essKitName;
	private boolean useEssentials;
	private Map<String, Object> essentialsKit = new HashMap<>();

	// Potion Effects
	private boolean hasPotionEffects;
	private List<PotionEffect> potionEffects = new ArrayList<>();

	// ---- Transient
	private transient File file;
	private transient String name;
	private transient boolean loaded;

	private transient final UltimateArena plugin;

	public ArenaClass(@NonNull UltimateArena plugin, @NonNull File file)
	{
		this.plugin = plugin;
		this.file = file;
		this.name = FormatUtil.trimFileExtension(file, ".yml");

		// Load
		this.loaded = load();
	}

	/**
	 * Attempts to load this class
	 *
	 * @return Whether or not loading was successful
	 */
	public final boolean load()
	{
		Validate.isTrue(! loaded, "Class has already been loaded!");

		try
		{
			boolean changes = false;
			YamlConfiguration fc = YamlConfiguration.loadConfiguration(file);

			if (fc.isSet("armor"))
			{
				Map<String, Object> values = fc.getConfigurationSection("armor").getValues(false);
				for (Entry<String, Object> entry : values.entrySet())
				{
					String value = entry.getValue().toString();
					ItemStack item = null;

					try
					{
						// Attempt to parse regularly
						item = ItemUtil.readItem(value);
					} catch (Throwable ex) { }

					if (item != null)
					{
						armor.add(item);
						continue;
					}

					// Read legacy
					Material material = null;
					short data = 0;

					Map<Enchantment, Integer> enchants = new HashMap<>();

					value = value.replaceAll(" ", "");
					if (value.contains(","))
					{
						String str = value.substring(0, value.indexOf(","));
						MyMaterial myMat = MyMaterial.fromString(str);
						material = myMat.getMaterial();
						data = myMat.isIgnoreData() ? 0 : myMat.getData();
						enchants = readArmorEnchantments(value.substring(value.indexOf(",") + 1));
					}
					else
					{
						MyMaterial myMat = MyMaterial.fromString(value);
						material = myMat.getMaterial();
						data = myMat.isIgnoreData() ? 0 : myMat.getData();
					}

					item = new ItemStack(material, 1, data);

					if (! enchants.isEmpty())
						item.addUnsafeEnchantments(enchants);

					armor.add(item);

					// Convert
					fc.set("armor." + entry.getKey().toLowerCase(), ItemUtil.serialize(item));
					changes = true;
				}
			}

			if (fc.isSet("tools"))
			{
				Map<String, Object> values = fc.getConfigurationSection("tools").getValues(false);
				for (Entry<String, Object> entry : values.entrySet())
				{
					String value = entry.getValue().toString();

					try
					{
						ItemStack stack = ItemUtil.readItem(value);
						if (stack != null)
							tools.add(stack);
					}
					catch (Throwable ex)
					{
						plugin.outConsole(Level.SEVERE, Util.getUsefulStack(ex, "parsing item \"" + value + "\""));
					}
				}
			}

			useEssentials = fc.getBoolean("useEssentials", false);
			if (useEssentials && plugin.getEssentialsHandler().useEssentials())
			{
				essKitName = fc.getString("essentialsKit", "");
				if (! essKitName.isEmpty())
				{
					essentialsKit = plugin.getEssentialsHandler().readEssentialsKit(essKitName);
				}
			}

			hasPotionEffects = fc.getBoolean("hasPotionEffects", false);
			if (hasPotionEffects)
			{
				potionEffects = readPotionEffects(fc.getString("potionEffects"));
			}

			useHelmet = fc.getBoolean("useHelmet", true);

			if (fc.isSet("permissionNode"))
			{
				if (! fc.getString("permissionNode").isEmpty())
					fc.set("needsPermission", true);

				fc.set("permissionNode", null);
				changes = true;
			}

			needsPermission = fc.getBoolean("needsPermission", false);
			permissionNode = "ultimatearena.class." + name.toLowerCase();

			try
			{
				if (changes)
					fc.save(file);
			}
			catch (Throwable ex)
			{
				plugin.outConsole(Level.WARNING, Util.getUsefulStack(ex, "saving changes for class \"" + name + "\""));
			}
		}
		catch (Throwable ex)
		{
			plugin.outConsole(Level.SEVERE, Util.getUsefulStack(ex, "loading class \"" + name + "\""));
			return false;
		}

		plugin.debug("Successfully loaded class {0}!", name);
		return true;
	}

	private final List<PotionEffect> readPotionEffects(String str) throws Throwable
	{
		List<PotionEffect> ret = new ArrayList<>();

		str = str.replaceAll(" ", "");
		String[] split = str.split(",");
		for (String s : split)
		{
			if (s.contains(":"))
			{
				String[] split1 = s.split(":");
				PotionEffectType type = PotionEffectType.getByName(split1[0].toUpperCase());
				int strength = NumberUtil.toInt(split1[1]);

				if (type != null && strength >= 0)
				{
					ret.add(new PotionEffect(type, Integer.MAX_VALUE, strength));
				}
			}
		}

		return ret;
	}

	private final Map<Enchantment, Integer> readArmorEnchantments(String string) throws Throwable
	{
		Map<Enchantment, Integer> enchants = new HashMap<Enchantment, Integer>();

		string = string.replaceAll(" ", "");
		String[] split = string.split(",");
		for (String s : split)
		{
			if (s.contains(":"))
			{
				String[] split2 = s.split(":");
				Enchantment enchantment = EnchantmentType.toEnchantment(split2[0]);
				int level = NumberUtil.toInt(split2[1]);

				if (enchantment != null && level > 0)
				{
					enchants.put(enchantment, level);
				}
			}
		}

		return enchants;
	}

	public final boolean checkPermission(Player player)
	{
		return ! needsPermission || plugin.getPermissionHandler().hasPermission(player, permissionNode);
	}

	public final ItemStack getArmor(int index)
	{
		if (armor.size() >= index)
		{
			return armor.get(index);
		}

		return null;
	}

	@Override
	public void reload()
	{
		// Boolean defaults
		this.hasPotionEffects = false;
		this.needsPermission = false;
		this.useEssentials = false;
		this.useHelmet = true;

		// Clear lists and maps
		this.essentialsKit.clear();
		this.potionEffects.clear();
		this.armor.clear();
		this.tools.clear();

		// Empty strings
		this.permissionNode = "";
		this.essKitName = "";

		// Load the class again
		this.loaded = false;
		this.loaded = load();
	}
}