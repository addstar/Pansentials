package au.com.addstar.pansentials.modules;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import au.com.addstar.pansentials.MasterPlugin;
import au.com.addstar.pansentials.Module;
import au.com.addstar.pansentials.Utilities;

public class SpeedModule implements Module, CommandExecutor, TabCompleter{
	
	private MasterPlugin plugin;

	@Override
	public void onEnable() {
		plugin.getCommand("speed").setExecutor(this);
		plugin.getCommand("speed").setTabCompleter(this);
	}

	@Override
	public void onDisable() {
		plugin.getCommand("speed").setExecutor(null);
		plugin.getCommand("speed").setTabCompleter(null);
	}

	@Override
	public void setPandoraInstance(MasterPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String cmd,
			String[] args) {
		if(command.getName().equalsIgnoreCase("speed")){
			if(args.length == 1 && sender instanceof Player){
				Player ply = (Player) sender;
				if(args[0].matches("[0-9]+(.[0-9]+)?") && Float.valueOf(args[0]) >= 0 && Float.valueOf(args[0]) <= 10){
					float speed = Float.valueOf(args[0]);
					
					if(plugin.getFormatConfig().contains("speed.maxSpeed") && !ply.isOp()){
						for(String key : plugin.getFormatConfig().getConfigurationSection("speed.maxSpeed").getKeys(false)){
							if(ply.hasPermission("pansentials.maxspeed." + key)){
								if(speed > plugin.getFormatConfig().getInt("speed.maxSpeed." + key)){
									speed = Double.valueOf(plugin.getFormatConfig().getDouble("speed.maxSpeed." + key)).floatValue();
								}
								break;
							}
						}
					}
					
					String ss = String.valueOf(speed);
					if(ss.endsWith(".0")){
						ss = ss.replace(".0", "");
					}
					
					if(ply.isFlying()){
						ply.setFlySpeed(0.1f * speed);
						ply.sendMessage(Utilities.format(plugin.getFormatConfig(), "speed.selfFly", "%speed%:" + ss));
					}
					else{
						if(speed > 5)
							speed = 5;
						ply.setWalkSpeed(0.2f * speed);
						ply.sendMessage(Utilities.format(plugin.getFormatConfig(), "speed.selfWalk", "%speed%:" + ss));
					}
				}
				else{
					ply.sendMessage(Utilities.format(plugin.getFormatConfig(), "speed.invalidSpeed", "%speed%:" + args[0]));
				}
			}
			else if(args.length == 2){
				if(sender instanceof Player && (args[0].equalsIgnoreCase("walk") || args[0].equalsIgnoreCase("fly")) &&
						args[1].matches("[0-9]+(.[0-9]+)?") && Float.valueOf(args[1]) >= 0 && Float.valueOf(args[1]) <= 10){
					Player ply = (Player) sender;
					float speed = Float.valueOf(args[1]);
					
					String ss = String.valueOf(speed);
					if(ss.endsWith(".0")){
						ss = ss.replace(".0", "");
					}
					
					if(args[0].equalsIgnoreCase("walk")){
						if(speed > 5)
							speed = 5;
						ply.setWalkSpeed(0.2f * speed);
						ply.sendMessage(Utilities.format(plugin.getFormatConfig(), "speed.selfWalk", "%speed%:" + ss));
					}
					else{
						ply.setFlySpeed(0.1f * speed);
						ply.sendMessage(Utilities.format(plugin.getFormatConfig(), "speed.selfFly", "%speed%:" + ss));
					}
				}
				else if(!args[1].matches("[0-9]+(.[0-9]+)?")){
					sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "speed.invalidSpeed", "%speed%:" + args[1]));
				}
				else if(!args[0].equalsIgnoreCase("walk") && !args[0].equalsIgnoreCase("fly")){
					sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "speed.invalidParameter", "%parameter%:" + args[0]));
				}
			}
			else if(args.length == 3){
				if(sender.hasPermission("pansentials.fly.other")){
					List<Player> plys = Bukkit.getServer().matchPlayer(args[0]);
					if(!plys.isEmpty()){
						if((args[1].equalsIgnoreCase("walk") || args[1].equalsIgnoreCase("fly") && args[2].matches("[0-9]+(.[0-9]+)?")) &&
								Float.valueOf(args[2]) >= 0 && Float.valueOf(args[2]) <= 10){
							Player ply = plys.get(0);
							float speed = Float.valueOf(args[2]);
							
							String ss = String.valueOf(speed);
							if(ss.endsWith(".0")){
								ss = ss.replace(".0", "");
							}
							
							if(args[1].equalsIgnoreCase("walk")){
								if(speed > 5)
									speed = 5;
								ply.setWalkSpeed(0.2f * speed);
								ply.sendMessage(Utilities.format(plugin.getFormatConfig(), "speed.selfWalk", "%speed%:" + ss));
								sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "speed.otherWalk", "%player%:" + ply.getName(), 
										"%speed%:" + ss));
							}
							else{
								ply.setFlySpeed(0.1f * speed);
								ply.sendMessage(Utilities.format(plugin.getFormatConfig(), "speed.selfFly", "%speed%:" + ss));
								sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "speed.otherFly", "%player%:" + ply.getName(), 
										"%speed%:" + ss));
							}
						}
						else if(!args[2].matches("[0-9]+(.[0-9]+)?")){
							sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "speed.invalidSpeed", "%speed%:" + args[2]));
						}
						else if(!args[1].equalsIgnoreCase("walk") && !args[1].equalsIgnoreCase("fly")){
							sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "speed.invalidParameter", "%parameter%:" + args[1]));
						}
					}
					else{
						sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "noPlayer", "%player%:" + args[0]));
					}
				}
			}
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command,
			String cmd, String[] args) {
		if(args.length == 1){
			if("fly".startsWith(args[0]))
				return Arrays.asList("fly");
			else if("walk".startsWith(args[0]))
				return Arrays.asList("walk");
			return Arrays.asList("fly", "walk");
		}
		else if(args.length == 2 && !(args[0].equalsIgnoreCase("fly") || args[0].equalsIgnoreCase("walk"))){
			if("fly".startsWith(args[1]))
				return Arrays.asList("fly");
			else if("walk".startsWith(args[1]))
				return Arrays.asList("walk");
			return Arrays.asList("fly", "walk");
		}
		return null;
	}

}
