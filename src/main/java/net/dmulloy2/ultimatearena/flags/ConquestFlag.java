package net.dmulloy2.ultimatearena.flags;

import java.util.ArrayList;
import java.util.List;

import net.dmulloy2.ultimatearena.UltimateArena;
import net.dmulloy2.ultimatearena.arenas.Arena;
import net.dmulloy2.ultimatearena.types.ArenaLocation;
import net.dmulloy2.ultimatearena.types.ArenaPlayer;
import net.dmulloy2.util.Util;

import org.bukkit.entity.Player;

/**
 * @author dmulloy2
 */

public class ConquestFlag extends ArenaFlag
{
	public ConquestFlag(Arena arena, ArenaLocation location, UltimateArena plugin)
	{
		super(arena, location, plugin);
	}

	@Override
	public void checkNear(List<ArenaPlayer> arenaPlayers)
	{
		int team1 = 0;
		int team2 = 0;

		List<ArenaPlayer> players = new ArrayList<>();
		for (ArenaPlayer ap : arenaPlayers)
		{
			Player player = ap.getPlayer();
			if (player.getHealth() > 0.0D && player.getWorld().getUID().equals(location.getWorld())
					&& player.getLocation().distance(location) < 4.5D)
			{
				players.add(ap);

				if (ap.getTeam() == 1)
					team1++;
				else
					team2++;
			}
		}

		// Remove any duplicates
		players = Util.removeDuplicates(players);
		cappingTeam = team1 > team2 ? 1 : 2;

		// The other team is trying to cap
		if (cappingTeam != owningTeam)
		{
			if (cappingTeam == 1)
			{
				int added = (team1 - team2) * 5;
				if (power - added < 0)
					power = 0;
				else
					power -= added;

				for (ArenaPlayer apl : players)
				{
					apl.sendMessage("&3Capping! &e{0}&3%", power);
				}

				if (power == 0)
				{
					setOwningTeam(1);
				}
			}
			else
			{
				int added = (team2 - team1) * 5;
				if (power - added < 0)
					power = 0;
				else
					power -= added;

				for (ArenaPlayer apl : players)
				{
					apl.sendMessage("&3Capping! &e{0}&3%", power);
				}

				if (power == 0)
				{
					setOwningTeam(2);
				}
			}
		}
		else
		{
			if (owningTeam == 1 && ! capped)
			{
				int added = (team1 - team2) * 5;
				if (power + added > 100)
					power = 100;
				else
					power += added;

				for (ArenaPlayer apl : players)
				{
					apl.sendMessage("&3Capping! &e{0}&3%", power);
				}

				if (power == 100)
				{
					for (ArenaPlayer apl : players)
					{
						apl.sendMessage("&3Capped!");
					}

					capped = true;
					setOwningTeam(1);
				}
			}
			else if (owningTeam == 2 && ! capped)
			{
				int added = (team2 - team1) * 5;
				if (power + added > 100)
					power = 100;
				else
					power += added;

				for (ArenaPlayer apl : players)
				{
					apl.sendMessage("&3Capping! &e{0}&3%", power);
				}

				if (power == 100)
				{
					for (ArenaPlayer apl : players)
					{
						apl.sendMessage("&3Capped!");
					}

					capped = true;
					setOwningTeam(1);
				}
			}
		}
	}
}