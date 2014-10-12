package au.com.addstar.pansentials.modules;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import au.com.addstar.pansentials.MasterPlugin;
import au.com.addstar.pansentials.Module;
import au.com.addstar.pansentials.Utilities;

public class GodModule implements Module, CommandExecutor, Listener{
	
	private MasterPlugin plugin;
	private Set<Player> gods;

	@Override
	public void onEnable() {
		plugin.getCommand("god").setExecutor(this);
		gods = new HashSet<Player>();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@Override
	public void onDisable() {
		plugin.getCommand("god").setExecutor(null);
		gods = null;
		HandlerList.unregisterAll(this);
	}

	@Override
	public void setPandoraInstance(MasterPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String cmd,
			String[] args) {
		if(command.getName().equalsIgnoreCase("god")){
			if(args.length == 0 && sender instanceof Player){
				Player ply = (Player)sender;
				
				if(gods.contains(ply) || cmd.equalsIgnoreCase("ungod")){
					gods.remove(ply);
					ply.sendMessage(Utilities.format(plugin.getFormatConfig(), "god.selfUngod"));
				}
				else{
					gods.add(ply);
					ply.sendMessage(Utilities.format(plugin.getFormatConfig(), "god.selfGod"));
				}
			}
			else if(args.length == 1){
				if(sender.hasPermission("pansentials.god.other")){
					List<Player> plys = Bukkit.matchPlayer(args[0]);
					if(!plys.isEmpty()){
						Player ply = plys.get(0);
						if(gods.contains(ply) || cmd.equalsIgnoreCase("ungod")){
							gods.remove(ply);
							ply.sendMessage(Utilities.format(plugin.getFormatConfig(), "god.selfUngod"));
							sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "god.otherUngod", "%player%:" + ply.getName()));
						}
						else{
							gods.add(ply);
							ply.sendMessage(Utilities.format(plugin.getFormatConfig(), "god.selfGod"));
							sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "god.otherGod", "%player%:" + ply.getName()));
						}
					}
					else{
						sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "noPlayer", "%name%:" + args[0]));
					}
				}
				else{
					sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "noPermission"));
				}
			}
			return true;
		}
		return false;
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onDamage(EntityDamageEvent event){
		if(event.getEntity() instanceof Player){
			Player ply = (Player)event.getEntity();
			if(gods.contains(ply)){
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	private void onDisconnect(PlayerQuitEvent event){
		if(gods.contains(event.getPlayer())){
			gods.remove(event.getPlayer());
		}
	}
	
	@EventHandler
	private void onHunger(FoodLevelChangeEvent event){
		Player ply = (Player) event.getEntity();
		if(gods.contains(ply)){
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	private void onTarget(EntityTargetEvent event){
		if(event.getTarget() instanceof Player){
			Player ply = (Player) event.getTarget();
			if(gods.contains(ply)){
				event.setCancelled(true);
			}
		}
	}
}
