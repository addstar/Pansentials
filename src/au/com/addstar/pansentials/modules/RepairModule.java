package au.com.addstar.pansentials.modules;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import au.com.addstar.pansentials.MasterPlugin;
import au.com.addstar.pansentials.Module;

public class RepairModule implements Module, CommandExecutor{
	
	private MasterPlugin plugin;

	@Override
	public void onEnable() {
		plugin.getCommand("repair").setExecutor(this);
	}

	@Override
	public void onDisable() {
		plugin.getCommand("repair").setExecutor(null);
	}

	@Override
	public void setPandoraInstance(MasterPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String cmd,
			String[] args) {
		if(command.getName().equalsIgnoreCase("repair") && sender instanceof Player){
			Player ply = (Player) sender;
			ItemStack stack = ply.getItemInHand();
			if(stack != null){
				stack.setDurability(stack.getType().getMaxDurability());
			}
			ply.updateInventory();
		}
		return true;
	}

}
