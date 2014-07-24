package net.dmulloy2.ultimatearena.commands;

import net.dmulloy2.commands.Command;
import net.dmulloy2.ultimatearena.UltimateArena;
import net.dmulloy2.ultimatearena.arenas.Arena;

/**
 * @author dmulloy2
 */

public abstract class UltimateArenaCommand extends Command
{
	protected final UltimateArena plugin;
	public UltimateArenaCommand(UltimateArena plugin)
	{
		super(plugin);
		this.plugin = plugin;
		this.usesPrefix = true;
	}

	protected final Arena getArena(int arg)
	{
		if (args.length > arg)
		{
			return plugin.getArena(args[arg]);
		}

		if (isPlayer() && plugin.isInArena(player))
		{
			return plugin.getArena(player);
		}

		return null;
	}
}