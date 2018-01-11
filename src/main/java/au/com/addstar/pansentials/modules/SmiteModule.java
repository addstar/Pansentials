package au.com.addstar.pansentials.modules;

import au.com.addstar.pansentials.MasterPlugin;
import au.com.addstar.pansentials.Module;
import au.com.addstar.pansentials.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
			if(args.length >= 1 && !args[0].equalsIgnoreCase("-e")){
				List<Player> plys = Bukkit.matchPlayer(args[0]);
				if(!plys.isEmpty()){
					Player ply = plys.get(0);
					boolean isSchmite = false;
					if(sender instanceof Player){
						if (sender.getName().equalsIgnoreCase("Schmoller")) {
							isSchmite = true;
						}
					}
					
					if(cmd.equalsIgnoreCase("schmite")){
						isSchmite = true;
					}
					String effect = "lightning";
					if(args.length == 2){
						effect = args[1];
					}
					
					smite(effect, ply.getLocation());
					
					if(isSchmite){
						sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "smite.selfSchmite", "%player%:" + plys.get(0).getDisplayName()));
						ply.sendMessage(Utilities.format(plugin.getFormatConfig(), "smite.otherSchmite"));
					}
					else{
						sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "smite.self", "%player%:" + plys.get(0).getDisplayName()));
						ply.sendMessage(Utilities.format(plugin.getFormatConfig(), "smite.other"));
					}
					return true;
				}
			}
			else if(sender instanceof Player){
				String effect = "lightning";
				if(args.length == 2 && args[0].equalsIgnoreCase("-e"))
					effect = args[1];
				
				Player p = (Player)sender;

				smite(effect, p.getTargetBlock(null, 80).getLocation());
				return true;
			}
		}
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command,
			String cmd, String[] args) {
		if(command.getName().equalsIgnoreCase("smite")){
			if(args.length == 1){
				List<String> plys = new ArrayList<>();
				for(Player p : Bukkit.matchPlayer(args[0])){
					plys.add(p.getName());
				}
				plys.add("-e");
				return plys;
			}
			if(args.length == 2){
				if(!args[1].equalsIgnoreCase("")){
					if("explode".startsWith(args[1]))
						return Collections.singletonList("explode");
					else if("lightning".startsWith(args[1]))
						return Collections.singletonList("lightning");
				}
				else
					return Arrays.asList("lightning", "explode");
			}
		}
		return null;
	}
	
	private void smite(String effect, Location loc){
		if(!effect.equalsIgnoreCase("lightning")){
			if(effect.equalsIgnoreCase("explode")){
				loc.getWorld().createExplosion(loc.getX(), loc.getY() + 1, 
						loc.getZ(), 1.5f, false, false);
			}
		}
		else{
			LightningStrike strike = loc.getWorld().strikeLightningEffect(loc);
			List<Entity> ents = strike.getNearbyEntities(2d, 2d, 2d);
			for(Entity ent : ents){
				if(ent instanceof Player){
					((Player) ent).damage(5d);
				}
			}
		}
	}

}
