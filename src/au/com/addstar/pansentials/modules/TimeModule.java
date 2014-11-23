package au.com.addstar.pansentials.modules;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.addstar.pansentials.DescParseTickFormat;
import au.com.addstar.pansentials.MasterPlugin;
import au.com.addstar.pansentials.Module;
import au.com.addstar.pansentials.Utilities;

public class TimeModule implements Module, CommandExecutor{
	
	private MasterPlugin plugin;

	@Override
	public void onEnable() {
		plugin.getCommand("time").setExecutor(this);
	}

	@Override
	public void onDisable() {
		plugin.getCommand("time").setExecutor(null);
	}

	@Override
	public void setPandoraInstance(MasterPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String cmd, String[] args) {
		final Set<World> worlds = new TreeSet<World>(new WorldNameComparator());
		if (command.getName().equalsIgnoreCase("time")) {
			// If theres no arguments to /time we just display current time(s)
			if (args.length == 0) {
				if (sender instanceof Player) {
					Player p = (Player) sender;
					worlds.add(p.getWorld());
				} else {
					for (World w : Bukkit.getWorlds()) {
						worlds.add(w);
					}
				}
				getWorldsTime(sender, worlds);
			} else {
				if (args[0].equalsIgnoreCase("help"))
					return false;

				if (sender instanceof Player) {
					// Check perms of player
					if (!sender.hasPermission("pansentials.time.set")) {
						sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "noPermission"));
						return true;
					}
				} else {
					// Console must provide world + time
					if (args.length == 1) {
						sender.sendMessage(ChatColor.RED + "You must specify a world name to set the time from console.");
						return true;
					}
				}

				// Get the target world for the time change
				String time;
				if (args.length == 1) {
					Player p = (Player) sender;
					worlds.add(p.getWorld());
					time = args[0];
				} else {
					World w = Bukkit.getWorld(args[0]);
					if (w == null) {
						sender.sendMessage(ChatColor.RED + "Invalid world name.");
						return true;
					}
					worlds.add(w);
					time = args[1];                          
				}

				// Parse the specified time value and set the world time(s)
				long ticks;
				try {
					ticks = DescParseTickFormat.parse(time);
				}
				catch (NumberFormatException e) {
					sender.sendMessage(ChatColor.RED + "Invalid time value.");
					return true;
				}
				setWorldsTime(sender, worlds, ticks);
			}
		}
		return true;
	}

	/**
	 * Used to set the time and inform of the change
	 */
	private void setWorldsTime(final CommandSender sender, final Collection<World> worlds, final long ticks)
	{
		final StringBuilder output = new StringBuilder();
		for (World world : worlds) {
			world.setTime(ticks);

			if (output.length() > 0) {
				output.append(", ");
			}
			output.append(world.getName());
		}
		sender.sendMessage(Utilities.format(
				plugin.getFormatConfig(),
				"time.setTime",
				"%world%:" + output.toString(),
				"%time12%:" + DescParseTickFormat.format12(ticks),
				"%time24%:" + DescParseTickFormat.format24(ticks),
				"%ticks%:" + ticks
		));
	}

	private void getWorldsTime(final CommandSender sender, final Collection<World> worlds) {
		for (World world : worlds) {
			sender.sendMessage(Utilities.format(
					plugin.getFormatConfig(),
					"time.timeFormat",
					"%world%:" + world.getName(),
					"%time12%:" + DescParseTickFormat.format12(world.getTime()),
					"%time24%:" + DescParseTickFormat.format24(world.getTime()),
					"%ticks%:" + world.getTime()
			));
		}
	}

	class WorldNameComparator implements Comparator<World>
	{
		@Override
		public int compare(final World a, final World b)
		{
			return a.getName().compareTo(b.getName());
		}
	}
}