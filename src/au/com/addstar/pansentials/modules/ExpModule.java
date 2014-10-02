package au.com.addstar.pansentials.modules;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import au.com.addstar.pansentials.MasterPlugin;
import au.com.addstar.pansentials.Module;
import au.com.addstar.pansentials.Utilities;

public class ExpModule implements Module, CommandExecutor, TabCompleter{
	
	private MasterPlugin plugin;
	private FileConfiguration config;

	@Override
	public void onEnable() {
		config = plugin.getFormatConfig();
		
		plugin.getCommand("exp").setExecutor(this);
	}

	@Override
	public void onDisable() {
		config = null;
		
		plugin.getCommand("exp").setExecutor(null);
	}

	@Override
	public void setPandoraInstance(MasterPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String arg2,
			String[] args) {
		if(args.length == 0 && sender instanceof Player){
			Player ply = (Player) sender;
			sender.sendMessage(Utilities.format(config, "exp.info", "%player%:" + ply.getDisplayName(), 
					"%exp%:" + ply.getTotalExperience(), "%level%:" + ply.getLevel(), "%amount%:" + ply.getExpToLevel()));
		}
		else if(args.length == 1){
			List<Player> plys = Bukkit.getServer().matchPlayer(args[0]);
			if(!plys.isEmpty()){
				Player ply = plys.get(0);
				sender.sendMessage(Utilities.format(config, "exp.info", "%player%:" + ply.getDisplayName(), 
						"%exp%:" + ply.getTotalExperience(), "%level%:" + ply.getLevel(), "%amount%:" + ply.getExpToLevel()));
			}
			else{
				sender.sendMessage(Utilities.format(config, "noPlayer", "%name%:" + args[0]));
			}
		}
		else if(args.length == 3 && (args[1].equalsIgnoreCase("set") || args[1].equalsIgnoreCase("add"))){
			List<Player> plys = Bukkit.getServer().matchPlayer(args[0]);
			if(!plys.isEmpty()){
				Player ply = plys.get(0);
				int level = -1;
				int exp = -1;
				if(args[2].matches("L[0-9]+")){
					level = Integer.valueOf(args[2].replace("L", ""));
				}
				else if(args[2].matches("[0-9]+")){
					exp = Integer.valueOf(args[2]);
				}
				else{
					sender.sendMessage(Utilities.format(config, "exp.invalidExp", "%amount%:" + args[2]));
					return true;
				}
				
				if(args[1].equalsIgnoreCase("set")){
					if(level != -1){
						ply.setLevel(level);
						sender.sendMessage(Utilities.format(config, "exp.setLevel", "%player%:" + ply.getDisplayName(), 
								"%amount%:" + level));
					}
					else{
						ply.setLevel(0);
						ply.setExp(0);
						ply.setTotalExperience(0);
						ply.giveExp(exp);
						sender.sendMessage(Utilities.format(config, "exp.setExp", "%player%:" + ply.getDisplayName(), 
								"%amount%:" + exp, "%level%:" + ply.getLevel()));
					}
				}
				else{
					if(level != -1){
						ply.setLevel(ply.getLevel() + level);
						sender.sendMessage(Utilities.format(config, "exp.addLevel", "%player%:" + ply.getDisplayName(), 
								"%amount%:" + level, "%level%:" + ply.getLevel()));
					}
					else{
						ply.giveExp(exp);
						sender.sendMessage(Utilities.format(config, "exp.addExp", "%player%:" + ply.getDisplayName(), 
								"%amount%:" + exp, "%level%:" + ply.getLevel(), "%exp%:" + ply.getTotalExperience()));
					}
				}
			}
			else{
				sender.sendMessage(Utilities.format(config, "noPlayer", "%name%:" + args[0]));
			}
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command,
			String cmd, String[] args) {
		if(args.length == 2){
			if("set".startsWith(args[1]))
				return Arrays.asList("set");
			else if("add".startsWith(args[1]))
				return Arrays.asList("add");
			return Arrays.asList("add", "set");
		}
		return null;
	}

}
