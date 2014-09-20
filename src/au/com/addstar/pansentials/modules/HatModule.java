package au.com.addstar.pansentials.modules;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import au.com.addstar.pansentials.MasterPlugin;
import au.com.addstar.pansentials.Module;
import au.com.addstar.pansentials.Utilities;

public class HatModule implements Module, CommandExecutor{
	
	private MasterPlugin plugin;
	private FileConfiguration config;

	@Override
	public void onEnable() {
		plugin.getCommand("hat").setExecutor(this);
		config = plugin.getFormatConfig();
	}

	@Override
	public void onDisable() {
		plugin.getCommand("hat").setExecutor(null);
		config = null;
	}

	@Override
	public void setPandoraInstance(MasterPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String cmd,
			String[] args) {
		if(command.getName().equalsIgnoreCase("hat") && sender instanceof Player){
			Player ply = (Player) sender;
			ItemStack hat = ply.getInventory().getHelmet();
			
			ply.getInventory().setHelmet(ply.getItemInHand());
			ply.setItemInHand(hat);
			ply.sendMessage(Utilities.format(config, "hat"));
		}
		return true;
	}

}
