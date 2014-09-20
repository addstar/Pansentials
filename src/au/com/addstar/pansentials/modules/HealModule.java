package au.com.addstar.pansentials.modules;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import au.com.addstar.pansentials.MasterPlugin;
import au.com.addstar.pansentials.Module;
import au.com.addstar.pansentials.Utilities;

public class HealModule implements Module, CommandExecutor{
	
	private MasterPlugin plugin;
	private FileConfiguration config;

	@Override
	public void onEnable() {
		config = plugin.getFormatConfig();
		
		plugin.getCommand("heal").setExecutor(this);
		plugin.getCommand("feed").setExecutor(this);
	}

	@Override
	public void onDisable() {
		config = null;
		
		plugin.getCommand("heal").setExecutor(null);
		plugin.getCommand("feed").setExecutor(null);
	}

	@Override
	public void setPandoraInstance(MasterPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String cmd,
			String[] args) {
		if(command.getName().equalsIgnoreCase("heal")){
			if(args.length == 0){
				if(sender instanceof Player){
					Player ply = (Player) sender;
					ply.setHealth(20d);
					ply.sendMessage(Utilities.format(config, "heal.self"));
				}
			}
			else{
				if(sender.hasPermission("pansentials.heal.other")){
					List<Player> players = plugin.getServer().matchPlayer(args[0]);
					if(players.size() > 0){
						players.get(0).setHealth(20d);
						players.get(0).sendMessage(Utilities.format(config, "heal.self"));
						sender.sendMessage(Utilities.format(config, "heal.other", "%player%:" + players.get(0).getName()));
					}
					else{
						sender.sendMessage(Utilities.format(config, "heal.noPlayer", "%name%:" + args[0]));
					}
				}
				else{
					sender.sendMessage(Utilities.format(config, "noPermission"));
				}
			}
		}
		else if(command.getName().equalsIgnoreCase("feed")){
			if(args.length == 0){
				if(sender instanceof Player){
					Player ply = (Player) sender;
					ply.setFoodLevel(20);
					ply.setSaturation(20f);
					ply.sendMessage(Utilities.format(config, "feed.self"));
				}
			}
			else{
				if(sender.hasPermission("pansentials.feed.other")){
					List<Player> players = plugin.getServer().matchPlayer(args[0]);
					if(players.size() > 0){
						players.get(0).setFoodLevel(20);
						players.get(0).setSaturation(20f);
						players.get(0).sendMessage(Utilities.format(config, "feed.self"));
						sender.sendMessage(Utilities.format(config, "feed.other", "%player%:" + players.get(0).getName()));
					}
					else{
						sender.sendMessage(Utilities.format(config, "feed.noPlayer", "%name%:" + args[0]));
					}
				}
				else{
					sender.sendMessage(Utilities.format(config, "noPermission"));
				}
			}
		}
		return false;
	}

}
