package au.com.addstar.pansentials.modules;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import au.com.addstar.pansentials.MasterPlugin;
import au.com.addstar.pansentials.Module;

public class MoreModule implements Module, CommandExecutor{
	
	private MasterPlugin plugin;

	@Override
	public void onEnable() {
		plugin.getCommand("more").setExecutor(this);
	}

	@Override
	public void onDisable() {
		plugin.getCommand("more").setExecutor(null);
	}

	@Override
	public void setPandoraInstance(MasterPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String cmd,
			String[] args) {
		if(command.getName().equalsIgnoreCase("more") && sender instanceof Player){
			Player ply = (Player) sender;
			ItemStack stack = ply.getItemInHand();
			if(stack != null){
				int size = stack.getMaxStackSize();
				if (args.length > 0) {
					size = Integer.valueOf(args[0]);
				}
				stack.setAmount(size);
			}
			ply.updateInventory();
		}
		return true;
	}

}
