package au.com.addstar.pansentials.modules;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import au.com.addstar.pansentials.MasterPlugin;
import au.com.addstar.pansentials.Module;
import au.com.addstar.pansentials.Utilities;

public class FlyModule implements Module, CommandExecutor, Listener{
	
	private MasterPlugin plugin;
	private FileConfiguration conf;

	@Override
	public void onEnable() {
		plugin.getCommand("fly").setExecutor(this);
		conf = plugin.getFormatConfig();
	}

	@Override
	public void onDisable() {
		plugin.getCommand("fly").setExecutor(null);
		conf = null;
	}

	@Override
	public void setPandoraInstance(MasterPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String cmd,
			String[] args) {
		if(command.getName().equalsIgnoreCase("fly")){
			if(sender instanceof Player && args.length == 0){
				Player ply = (Player) sender;
				if(ply.getAllowFlight()){
					ply.setAllowFlight(false);
					ply.sendMessage(Utilities.format(conf, "fly.selfDisable"));
				}
				else{
					ply.setAllowFlight(true);
					ply.sendMessage(Utilities.format(conf, "fly.selfEnable"));
				}
			}
			else if(args.length == 1 && sender.hasPermission("pansentials.fly.other")){
				List<Player> players = Bukkit.getServer().matchPlayer(args[0]);
				if(players.size() > 0){
					Player ply = players.get(0);
					if(ply.getAllowFlight()){
						ply.setAllowFlight(false);
						ply.sendMessage(Utilities.format(conf, "fly.selfDisable"));
						sender.sendMessage(Utilities.format(conf, "fly.other.execDisable", "%player%:" + ply.getName()));
					}
					else{
						ply.setAllowFlight(true);
						ply.sendMessage(Utilities.format(conf, "fly.selfEnable"));
						sender.sendMessage(Utilities.format(conf, "fly.other.execEnable", "%player%:" + ply.getName()));
					}
				}
			}
		}
		return true;
	}
	
	@EventHandler
	private void onJoin(PlayerJoinEvent event){
		if(event.getPlayer().getGameMode() != GameMode.CREATIVE){
			if(event.getPlayer().hasPermission("pansentials.fly.join"))
				event.getPlayer().setAllowFlight(true);
			else
				event.getPlayer().setAllowFlight(false);
		}
	}
	
	@EventHandler
	private void onSwitchWorld(PlayerChangedWorldEvent event){
		if(event.getPlayer().getGameMode() != GameMode.CREATIVE){
			if(event.getPlayer().hasPermission("pansentials.fly.changeworld"))
				event.getPlayer().setAllowFlight(true);
			else
				event.getPlayer().setAllowFlight(false);
		}
	}
}
