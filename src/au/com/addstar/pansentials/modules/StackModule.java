package au.com.addstar.pansentials.modules;

import au.com.addstar.pansentials.MasterPlugin;
import au.com.addstar.pansentials.Module;
import au.com.addstar.pansentials.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class StackModule implements Module, CommandExecutor{
	
	private MasterPlugin plugin;

	@Override
	public void onEnable() {
		plugin.getCommand("stack").setExecutor(this);
	}

	@Override
	public void onDisable() {
		plugin.getCommand("stack").setExecutor(null);
	}

	@Override
	public void setPandoraInstance(MasterPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String cmd,
			String[] args) {
		if(command.getName().equalsIgnoreCase("stack")){
			if(args.length == 0 && sender instanceof Player){
				Player ply = (Player)sender;
				stack(ply);
				ply.sendMessage(Utilities.format(plugin.getFormatConfig(), "stack.self"));
			}
			else{
				if(args.length > 0) { //correct Array out of bounds exception for /stack
					List<Player> plys = Bukkit.matchPlayer(args[0]);
					if (!plys.isEmpty()) {
						stack(plys.get(0));
						plys.get(0).sendMessage(Utilities.format(plugin.getFormatConfig(), "stack.self"));
						sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "stack.other", "%player%:" + plys.get(0).getName()));
					} else {
						sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "noPlayer", "%name%:" + args[0]));
					}
				}else{
					sender.sendMessage("Cannot parse no arguments from console.");

				}
			}
			return true;
		}
		return false;
	}
	
	private void stack(Player ply){
		List<ItemStack> items = new ArrayList<ItemStack>();
		for(int c = 0; c < ply.getInventory().getContents().length; c++){
			ItemStack i = ply.getInventory().getItem(c);
			if(i != null && i.getAmount() < 64){
				boolean add = true;
				for(ItemStack d : new ArrayList<ItemStack>(items)){
					if(i.getType() == d.getType() && i.getDurability() == d.getDurability()){
						int amt = i.getAmount() + d.getAmount();
						int over = 0;
						if(amt > 64){
							over = amt - 64;
							amt = 64;
						}
						d.setAmount(amt);
						if(over != 0)
							i.setAmount(over);
						else{
							ply.getInventory().clear(c);
							add = false;
						}
						
						if(amt == 64)
							items.remove(d);
						
						if(over == 0)
							break;
					}
				}
				if(add){
					items.add(i);
				}
			}
		}
	}

}
