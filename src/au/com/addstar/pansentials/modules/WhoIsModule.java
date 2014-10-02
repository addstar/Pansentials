package au.com.addstar.pansentials.modules;

import java.util.List;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import au.com.addstar.pansentials.MasterPlugin;
import au.com.addstar.pansentials.Module;
import au.com.addstar.pansentials.Utilities;

public class WhoIsModule implements Module, CommandExecutor{
	
	private MasterPlugin plugin;
	private FileConfiguration config;
    private static Economy econ = null;

	@Override
	public void onEnable() {
		plugin.getCommand("whois").setExecutor(this);
		config = plugin.getFormatConfig();
		
		setupEconomy();
	}
	
	private boolean setupEconomy(){
        if(plugin.getServer().getPluginManager().getPlugin("Vault") == null){
        	plugin.getLogger().info("Vault Not Found!");
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if(rsp == null){
        	plugin.getLogger().info("No Economy Plugin Found!");
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

	@Override
	public void onDisable() {
		plugin.getCommand("whois").setExecutor(null);
		config = null;
	}

	@Override
	public void setPandoraInstance(MasterPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String cmd,
			String[] args) {
		if(args.length == 1){
			List<Player> plys = Bukkit.getServer().matchPlayer(args[0]);
			if(plys.size() > 0){
				Player ply = plys.get(0);
				sender.sendMessage(Utilities.format(config, "whois.header", "%player%:" + ply.getName()));
				sender.sendMessage(Utilities.format(config, "whois.nick", "%nick%:" + ply.getDisplayName()));
				Damageable dmg = (Damageable) ply;
				Double d = Double.valueOf(dmg.getHealth());
				sender.sendMessage(Utilities.format(config, "whois.health", "%health%:" + d.intValue() + "/20"));
				sender.sendMessage(Utilities.format(config, "whois.hunger", "%hunger%:" + ply.getFoodLevel() + "/20", 
						"%saturation%:" + Float.valueOf(ply.getSaturation()).intValue()));
				sender.sendMessage(Utilities.format(config, "whois.exp", "%exp%:" + ply.getTotalExperience(), "%level%:" + ply.getLevel()));
				sender.sendMessage(Utilities.format(config, "whois.pos", "%world%:" + ply.getLocation().getWorld().getName(), 
						"%x%:" + ply.getLocation().getBlockX(), "%y%:" + ply.getLocation().getBlockY(), "%z%:" + ply.getLocation().getBlockZ()));
				if(econ != null){
					sender.sendMessage(Utilities.format(config, "whois.money", "%money%:" + econ.getBalance(ply.getPlayer())));
				}
				sender.sendMessage(Utilities.format(config, "whois.ip", "%ip%:" + ply.getAddress().getHostName() + "/" + ply.getAddress().getAddress().getCanonicalHostName()));
				//Location
				sender.sendMessage(Utilities.format(config, "whois.gamemode", "%gamemode%:" + ply.getGameMode().toString().toLowerCase()));
				sender.sendMessage(Utilities.format(config, "whois.op", "%op%:" + ply.isOp()));
				String isFlying = "Not Flying";
				if(ply.isFlying())
					isFlying = "Flying";
				sender.sendMessage(Utilities.format(config, "whois.fly", "%flymode%:" + ply.getAllowFlight(), "%flying%:" + isFlying));
			}
			else{
				sender.sendMessage(Utilities.format(config, "noPlayer", args[0]));
			}
			
			return true;
		}
		return false;
	}

}
