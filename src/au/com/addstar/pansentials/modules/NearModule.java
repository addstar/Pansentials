package au.com.addstar.pansentials.modules;

import au.com.addstar.pansentials.MasterPlugin;
import au.com.addstar.pansentials.Module;
import au.com.addstar.pansentials.Utilities;
import org.bukkit.Location;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author benjamincharlton on 27/05/2015.
 */
public class NearModule implements Module, CommandExecutor {

    private MasterPlugin plugin;

    private int radius;

    private Player target;

    private Location location;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String cmd, String[] args) {
        if (sender instanceof Player) {
            Player s = (Player) sender;
            radius = plugin.getConfig().getInt("pansentials.near.default-radius");
            if (args.length == 0) {
                //check sender perms for the basic command with a default radius
                if (sender.hasPermission("pansentials.near.self")) {
                    Location loc = s.getLocation();
                    return doNear(s, radius, loc);
                } else {
                    sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "noPermission"));
                }
            } else {
                Double x = null;
                Double y = null;
                Double z = null;
                for (String arg : args) {
                    if (arg.startsWith("r:")) {
                        radius = Utilities.parseInt(arg.substring(2), "r: must be followed by an positive integer");
                    }
                    if (arg.startsWith("x:")) {
                        x = Utilities.parseDouble(arg.substring(2),
                                Utilities.format(plugin.getFormatConfig(), "near.location.error", "value:" + args[0], "coord:x"));
                    }
                    if (arg.startsWith("y:")) {
                        y = Utilities.parseDouble(arg.substring(2),
                                Utilities.format(plugin.getFormatConfig(), "near.location.error", "value:" + args[0], "coord:y"));
                    }
                    if (arg.startsWith("z:")) {
                        z = Utilities.parseDouble(arg.substring(2),
                                Utilities.format(plugin.getFormatConfig(), "near.location.error", "value:" + args[0], "coord:z"));
                    }
                    if (arg.startsWith("p:")) {
                        if (!sender.hasPermission("pansentials.near.other")) {
                            sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "noPermission"));
                            return true;
                        }
                        Player target = Bukkit.getPlayer(arg.substring(2));
                        if (target == null) {
                            s.sendMessage(Utilities.format(plugin.getFormatConfig(), "noPlayer", arg.substring(2)));
                            return true;
                        }
                        if (!s.canSee(target)) {
                            s.sendMessage(Utilities.format(plugin.getFormatConfig(), "noPlayer", arg.substring(2)));
                            return true;
                        }
                    }
                }//end arg loop
                if (target != null) {
                    if (sender.hasPermission("pansentials.near.other")) {
                        return doNear(s, radius, target.getLocation());
                    }
                } else if (x != null && y != null && z != null) {
                    if (sender.hasPermission("pansentials.near.coord")) {
                        location = new Location(s.getWorld(), x, y, z);
                        return doNear(s, radius, location);
                    } else {
                        sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "noPermission"));
                    }
                } else {
                    if (sender.hasPermission("pansentials.near.self")) {
                        Location loc = s.getLocation();
                        return doNear(s, radius, loc);
                    } else {
                        sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "noPermission"));
                        return true;
                    }

                }
            }
        } else {
            //Could add Console option here.
            sender.sendMessage("Command not available from Console");
            return true;
        }
        return true;
    }

    private boolean doNear(Player sender, Integer radius, Location location){
        return true;
    }

    @Override
    public void onEnable() {
        plugin.getCommand("near").setExecutor(this);
    }

    @Override
    public void onDisable() {
        plugin.getCommand("repair").setExecutor(null);
    }

    @Override
    public void setPandoraInstance(MasterPlugin plugin) {
        this.plugin = plugin;
    }
}
