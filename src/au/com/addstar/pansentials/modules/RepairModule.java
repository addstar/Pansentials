package au.com.addstar.pansentials.modules;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import au.com.addstar.pansentials.MasterPlugin;
import au.com.addstar.pansentials.Module;
import au.com.addstar.pansentials.Utilities;

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
			if (args.length == 0) {
				ItemStack stack = ply.getItemInHand();
				if (stack == null || stack.getType().isBlock() || stack.getDurability() == 0)
					return true;
				
				stack.setDurability((short) 0);
				ply.updateInventory();
				sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "repair.hand"));
			} else {
				if (args[0].equalsIgnoreCase("all")) {
					for (ItemStack stack : ply.getInventory().getContents()) {
						if (needsRepair(stack))
							stack.setDurability((short) 0);
					}
					for (ItemStack stack : ply.getInventory().getArmorContents()) {
						if (needsRepair(stack))
							stack.setDurability((short) 0);
					}
				}
				sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "repair.all"));
			}
		}
		return true;
	}
	
	private boolean needsRepair(ItemStack stack) {
		if (stack == null)
			return false;

		Material mat = stack.getType();
		if (stack.getDurability() == 0 || stack.getMaxStackSize() < 0 ||
				mat.isBlock() || mat.isRecord() || 
				mat == Material.MONSTER_EGG || mat == Material.MONSTER_EGGS ||
				mat == Material.SKULL || mat == Material.SKULL_ITEM)
					return false;

		return true;
	}
}
