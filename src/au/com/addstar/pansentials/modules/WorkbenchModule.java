package au.com.addstar.pansentials.modules;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.addstar.pansentials.MasterPlugin;
import au.com.addstar.pansentials.Module;

public class WorkbenchModule implements Module, CommandExecutor{
	
	private MasterPlugin plugin;

	@Override
	public void onEnable() {
		plugin.getCommand("workbench").setExecutor(this);
	}

	@Override
	public void onDisable() {
		plugin.getCommand("workbench").setExecutor(null);
	}

	@Override
	public void setPandoraInstance(MasterPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String cmd,
			String[] args) {
		if(command.getName().equalsIgnoreCase("workbench") && sender instanceof Player){
			Player ply = (Player) sender;
			ply.openWorkbench(ply.getLocation(), true);
		}
		return true;
	}

}
