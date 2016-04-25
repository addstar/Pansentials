package au.com.addstar.pansentials.modules;

import au.com.addstar.monolith.lookup.Lookup;
import au.com.addstar.pansentials.MasterPlugin;
import au.com.addstar.pansentials.Module;
import au.com.addstar.pansentials.Utilities;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

public class EnchantModule implements Module, CommandExecutor{
	
	private MasterPlugin plugin;

	@Override
	public void onEnable() {
		plugin.getCommand("enchant").setExecutor(this);
	}

	@Override
	public void onDisable() {
		plugin.getCommand("enchant").setExecutor(null);
	}

	@Override
	public void setPandoraInstance(MasterPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String cmd,
			String[] args) {
		if(sender instanceof Player && command.getName().equalsIgnoreCase("enchant") && args.length >= 2){
			Player player = (Player) sender;
			Enchantment ench = Lookup.findEnchantmentByName(args[0]);
			if (ench == null) {
				ench = Enchantment.getByName(args[0].toUpperCase());
			}
			if (ench != null) {
				if (player.getInventory().getItemInMainHand().getType() != Material.AIR) {
					boolean unsafe = false;
					if(args.length == 3 && args[2].equalsIgnoreCase("unsafe")){
						unsafe = true;
					}
					int level = 1;
					if(args[1].matches("[0-9]+"))
						level = Integer.valueOf(args[1]);
					else{
						player.sendMessage(Utilities.format(plugin.getFormatConfig(), "enchant.invalidValue", "%value%:" + args[1]));
						return true;
					}
					if ((unsafe) && (!player.hasPermission("pansentials.enchant.unsafe"))) {
						player.sendMessage(Utilities.format(plugin.getFormatConfig(), "enchant.illegalEnchant",
								"%enchant%:" + ench.getName().toLowerCase(),
								"%level%:" + level,
								"%item%:" + player.getInventory().getItemInMainHand().getType().toString().toLowerCase()));
						return true;
					}
					if(level != 0){
						if(!unsafe){
							try{
								player.getInventory().getItemInMainHand().addEnchantment(ench, level);
								player.sendMessage(Utilities.format(plugin.getFormatConfig(), "enchant.success",
										"%item%:" + player.getInventory().getItemInMainHand().getType().toString().toLowerCase(),
										"%enchant%:" + ench.getName().toLowerCase(),
										"%level%:" + level));
							}
							catch (IllegalArgumentException e){
								player.sendMessage(Utilities.format(plugin.getFormatConfig(), "enchant.illegalEnchant", 
										"%enchant%:" + ench.getName().toLowerCase(), 
										"%level%:" + level,
										"%item%:" + player.getInventory().getItemInMainHand().getType().toString().toLowerCase()));
							}
						}
						else{
							player.getInventory().getItemInMainHand().addUnsafeEnchantment(ench, level);
							player.sendMessage(Utilities.format(plugin.getFormatConfig(), "enchant.success",
									"%item%:" + player.getInventory().getItemInMainHand().getType().toString().toLowerCase(),
									"%enchant%:" + ench.getName().toLowerCase(),
									"%level%:" + level));
						}
					}
					else{
						player.getInventory().getItemInMainHand().removeEnchantment(ench);
						player.sendMessage(Utilities.format(plugin.getFormatConfig(), "enchant.removed", 
								"%enchant%:" + ench.getName().toLowerCase(),
								"%item%:" + player.getInventory().getItemInMainHand().getType().toString().toLowerCase()));
					}
				}
				else{
					player.sendMessage(Utilities.format(plugin.getFormatConfig(), "enchant.noItem"));
				}
			}
			else{
				player.sendMessage(Utilities.format(plugin.getFormatConfig(), "enchant.invalidEnchant", "%name%:" + args[0]));
			}
			
			return true;
		}
		return false;
	}
}
