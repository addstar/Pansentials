package au.com.addstar.pansentials.modules;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import au.com.addstar.pansentials.MasterPlugin;
import au.com.addstar.pansentials.Module;

public class WhoModule implements Module, CommandExecutor{
	
	private MasterPlugin plugin;

	@Override
	public void onEnable() {
		plugin.getCommand("who").setExecutor(this);
	}

	@Override
	public void onDisable() {
		plugin.getCommand("who").setExecutor(null);
	}

	@Override
	public void setPandoraInstance(MasterPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String cmd,
			String[] args) {
		if(command.getName().equalsIgnoreCase("who")){
			
		}
		return true;
	}

}
