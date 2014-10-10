package au.com.addstar.pansentials.modules;

import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.addstar.pansentials.MasterPlugin;
import au.com.addstar.pansentials.Module;
import au.com.addstar.pansentials.Utilities;

public class GamemodeModule implements Module, CommandExecutor{
	
	private MasterPlugin plugin;

	@Override
	public void onEnable() {
		plugin.getCommand("gamemode").setExecutor(this);
	}

	@Override
	public void onDisable() {
		plugin.getCommand("gamemode").setExecutor(null);
	}

	@Override
	public void setPandoraInstance(MasterPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String cmd,
			String[] args) {
		if(command.getName().equalsIgnoreCase("gamemode")){
			if(args.length == 0 && sender instanceof Player){
				GameMode gm = null;
				if(cmd.equalsIgnoreCase("gms"))
					gm = GameMode.SURVIVAL;
				else if(cmd.equalsIgnoreCase("gmc"))
					gm = GameMode.CREATIVE;
				else if(cmd.equalsIgnoreCase("gma"))
					gm = GameMode.ADVENTURE;
				
				if(gm != null && sender.hasPermission("pansentials.gamemode." + gm.toString().toLowerCase())){
					((Player) sender).setGameMode(gm);
					sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "gamemode.changed", "%gamemode%:" + gm.toString().toLowerCase()));
				}
				else{
					sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "noPermission"));
				}
			}
			else if(args.length == 1 && (cmd.equalsIgnoreCase("gms") || cmd.equalsIgnoreCase("gma") || cmd.equalsIgnoreCase("gmc"))){
				List<Player> plys = plugin.getServer().matchPlayer(args[0]);
				if(!plys.isEmpty()){
					Player ply = plys.get(0);
					GameMode gm = null;
					if(cmd.equalsIgnoreCase("gms"))
						gm = GameMode.SURVIVAL;
					else if(cmd.equalsIgnoreCase("gmc"))
						gm = GameMode.CREATIVE;
					else if(cmd.equalsIgnoreCase("gma"))
						gm = GameMode.ADVENTURE;
					
					if(gm != null && sender.hasPermission("pansentials.gamemode." + gm.toString().toLowerCase() + ".other")){
						ply.setGameMode(gm);
						ply.sendMessage(Utilities.format(plugin.getFormatConfig(), "gamemode.changed", 
								"%gamemode%:" + gm.toString().toLowerCase()));
						sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "gamemode.changedOther", "%player%:" + ply.getName(), 
								"%gamemode%:" + gm.toString().toLowerCase()));
					}
					else if(gm == null){
						sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "gamemode.noGamemode", "%name%:" + args[0]));
					}
					else{
						sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "noPermission"));
					}
				}
				else{
					sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "noPlayer", "%name%:" + args[1]));
				}
			}
			else if(args.length == 1 && sender instanceof Player){
				Player ply = (Player) sender;
				GameMode gm = getGamemode(args[0]);
				
				if(gm != null && ply.hasPermission("pansentials.gamemode." + gm.toString().toLowerCase())){
					ply.setGameMode(gm);
					ply.sendMessage(Utilities.format(plugin.getFormatConfig(), "gamemode.changed", "%gamemode%:" + gm.toString().toLowerCase()));
				}
				else if(gm == null){
					ply.sendMessage(Utilities.format(plugin.getFormatConfig(), "gamemode.noGamemode", "%name%:" + args[0]));
				}
				else{
					ply.sendMessage(Utilities.format(plugin.getFormatConfig(), "noPermission"));
				}
			}
			else if(args.length == 2){
				List<Player> plys = plugin.getServer().matchPlayer(args[1]);
				if(!plys.isEmpty()){
					Player ply = plys.get(0);
					GameMode gm = getGamemode(args[0]);
					if(gm != null && sender.hasPermission("pansentials.gamemode." + gm.toString().toLowerCase() + ".other")){
						ply.setGameMode(gm);
						ply.sendMessage(Utilities.format(plugin.getFormatConfig(), "gamemode.changed", 
								"%gamemode%:" + gm.toString().toLowerCase()));
						sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "gamemode.changedOther", "%player%:" + ply.getName(), 
								"%gamemode%:" + gm.toString().toLowerCase()));
					}
					else if(gm == null){
						sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "gamemode.noGamemode", "%name%:" + args[0]));
					}
					else{
						sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "noPermission"));
					}
				}
				else{
					sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "noPlayer", "%name%:" + args[1]));
				}
			}
			return true;
		}
		return false;
	}
	
	private GameMode getGamemode(String mode){
		if(mode.equalsIgnoreCase("survival") || mode.equalsIgnoreCase("s") || mode.equalsIgnoreCase("0")){
			return GameMode.SURVIVAL;
		}
		else if(mode.equalsIgnoreCase("creative") || mode.equalsIgnoreCase("c") || mode.equalsIgnoreCase("1")){
			return GameMode.CREATIVE;
		}
		else if(mode.equalsIgnoreCase("adventure") || mode.equalsIgnoreCase("a") || mode.equalsIgnoreCase("2")){
			return GameMode.ADVENTURE;
		}
		return null;
	}
}
