package net.dmulloy2.ultimatearena.commands;

import net.dmulloy2.ultimatearena.UltimateArena;
import net.dmulloy2.ultimatearena.permissions.PermissionType;

public class CmdCreate extends UltimateArenaCommand
{
	public CmdCreate(UltimateArena plugin) 
	{
		super(plugin);
		this.name = "create";
		this.aliases.add("c");
		this.requiredArgs.add("name");
		this.requiredArgs.add("type");
		this.mode = "build";
		this.description = "create an UltimateArena";
		this.permission = PermissionType.CMD_CREATE.permission;
		
		this.mustBePlayer = true;
	}
	
	@Override
	public void perform()
	{
		plugin.createField(player, args[0], args[1]);
	}
}