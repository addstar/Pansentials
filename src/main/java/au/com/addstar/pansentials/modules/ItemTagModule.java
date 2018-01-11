package au.com.addstar.pansentials.modules;

import au.com.addstar.monolith.util.nbtapi.NBTItem;
import au.com.addstar.pansentials.MasterPlugin;
import au.com.addstar.pansentials.Module;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Created for the AddstarMC Project.
 * Created by Narimm on 22/04/2016.
 */
public class ItemTagModule implements Module, CommandExecutor {

    private MasterPlugin plugin;

    @Override
    public void onEnable() {
        plugin.getCommand("itemnbt").setExecutor(this);
    }

    @Override
    public void onDisable() {
        plugin.getCommand("itemnbt").setExecutor(null);
    }

    @Override
    public void setPandoraInstance(MasterPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (command.getName().equalsIgnoreCase("itemnbt")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Player Command Only");
                return false;
            }
            Player player = (Player) sender;
            if (player.hasPermission("pansentials.showNBT")) {
                ItemStack item = player.getInventory().getItemInMainHand();
                NBTItem nItem = new NBTItem(item);
                if (args.length == 0) {
                    sender.sendMessage("Item: ");
                    sender.sendMessage(item.toString());
                    sender.sendMessage("NBT Data: ");
                    sender.sendMessage(nItem.asNBTString());
                }
                if (args.length > 0) {
                    if (args[0].equals("get")) {
                        String[] nbtTags = StringUtils.split(args[1], ",");
                        if (nbtTags.length == 0) {
                            sender.sendMessage("Key: " + args[1] + "Value:" + nItem.getString(args[1]));
                        } else {
                            for (String tag : nbtTags)
                                sender.sendMessage("Key: " + tag + "Value:" + nItem.getString(tag));
                        }
                    }
                    if (args[0].equals("set")) {
                        sender.sendMessage("Not yet available");
                    }
                }
                return true;
            } else {
                sender.sendMessage("No Permission for this command");
                return true;
            }

        }
        sender.sendMessage("Command Error");
        return false;
    }
}


