package net.dmulloy2.ultimatearena.commands;

import net.dmulloy2.ultimatearena.UltimateArena;
import net.dmulloy2.ultimatearena.arenas.objects.ArenaZone;
import net.dmulloy2.ultimatearena.permissions.Permission;

public class CmdDislike extends UltimateArenaCommand
{	
	public CmdDislike(UltimateArena plugin)
	{
		super(plugin);
		this.name = "dislike";
		this.aliases.add("d");
		this.requiredArgs.add("arena");
		this.description = "dislike an arena";
		this.permission = Permission.DISLIKE;
		
		this.mustBePlayer = true;
	}
	
	@Override
	public void perform()
	{
		String arenaname = args[0];
		ArenaZone az = plugin.getArenaZone(arenaname);
		if (az != null)
		{
			if (az.canLike(player))
			{
				sendpMessage("&cYou have disliked: " + az.getArenaName());
				
				az.setDisliked(az.getDisliked() + 1);
				az.getVoted().add(player.getName());
			}
			else
			{
				err("You already voted for this arena!");
			}
		}
		else
		{
			err("This arena doesn't exist!");
		}
	}
}