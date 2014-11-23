package au.com.addstar.pansentials.modules;

import java.util.ArrayList;
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

public class SmiteModule implements Module, CommandExecutor, TabCompleter{
	
	private MasterPlugin plugin;

	@Override
	public void onEnable() {
		plugin.getCommand("smite").setExecutor(this);
	}

	@Override
	public void onDisable() {
		plugin.getCommand("smite").setExecutor(null);
	}

	@Override
	public void setPandoraInstance(MasterPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String cmd,
			String[] args) {
		if(command.getName().equalsIgnoreCase("smite")){
			if(args.length >= 1){
				List<Player> plys = Bukkit.matchPlayer(args[0]);
				if(!plys.isEmpty()){
					boolean isSchmite = false;
					if(sender instanceof Player){
						if(((Player)sender).getName().equalsIgnoreCase("Schmoller")){
							isSchmite = true;
						}
					}
					
					if(cmd.equalsIgnoreCase("schmite")){
						isSchmite = true;
					}
					
					if(args.length == 2){
						if(args[1].equalsIgnoreCase("explode")){
							plys.get(0).getWorld().createExplosion(plys.get(0).getLocation(), 0.0f);
						}
					}
					else{
						plys.get(0).getWorld().strikeLightningEffect(plys.get(0).getLocation());
					}

					plys.get(0).setHealth(0d);
					
					if(isSchmite){
						sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "smite.selfSchmite", "%player%:" + plys.get(0).getDisplayName()));
						plys.get(0).sendMessage(Utilities.format(plugin.getFormatConfig(), "smite.otherSchmite"));
					}
					else{
						sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "smite.self", "%player%:" + plys.get(0).getDisplayName()));
						plys.get(0).sendMessage(Utilities.format(plugin.getFormatConfig(), "smite.other"));
					}
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command,
			String cmd, String[] args) {
		if(command.getName().equalsIgnoreCase("smite")){
			if(args.length == 1){
				List<String> plys = new ArrayList<String>();
				for(Player p : Bukkit.matchPlayer(args[0])){
					plys.add(p.getName());
				}
				return plys;
			}
			if(args.length == 2){
				if(!args[1].equalsIgnoreCase("")){
					if("explode".startsWith(args[1]))
						return Arrays.asList("explode");
					else if("lightning".startsWith(args[1]))
						return Arrays.asList("lightning");
					else if("none".startsWith(args[1]))
						return Arrays.asList("none");
				}
				else
					return Arrays.asList("lightning", "explode", "none");
			}
		}
		return null;
	}

}
