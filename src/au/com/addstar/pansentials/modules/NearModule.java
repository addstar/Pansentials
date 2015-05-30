package au.com.addstar.pansentials.modules;

import au.com.addstar.monolith.command.CommandSenderType;
import au.com.addstar.pansentials.MasterPlugin;
import au.com.addstar.pansentials.Module;
import au.com.addstar.pansentials.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author benjamincharlton on 27/05/2015.
 */
public class NearModule implements Module, CommandExecutor {

    private MasterPlugin plugin;

    int radius;

    Player target;

    Location location;

    private Boolean isConsole;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String cmd, String[] args) {
        isConsole = false;
        Player s = null;
        ConsoleCommandSender c = null;
        if (sender instanceof Player) {
            s = (Player) sender;
            if(!s.hasPermission("pansentials.near")){
                sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "noPermission"));
                return true;
            }
        } else if (sender instanceof ConsoleCommandSender) {
            isConsole = true;
        } else {
            sender.sendMessage("Must be player or console to run.");
            return true;
        }
        FileConfiguration config = plugin.getConfig();
        radius = config.getInt("near.default-radius", 10);
        if (args.length == 0) {
            if (isConsole) {
                sender.sendMessage("Running Command without params as console is not supported.");
                return true;
            }
            //check sender perms for the basic command with a default radius
            if (sender.hasPermission("pansentials.near.self")) {
                sender.sendMessage("Running Command around self.");
                Location loc = null;
                if (s != null) {
                    loc = s.getLocation();
                }
                return doNear(s, radius, loc);
            } else {
                sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "noPermission"));
            }
        } else {
            Double x = null;
            Double y = null;
            Double z = null;
            World world = null;
            target = null;

            for (String arg : args) {
                switch (arg.substring(0, 2)) {
                    case "r:":
                        radius = Utilities.parseInt(arg.substring(2), "r: must be followed by an positive integer");
                        break;
                    case "x:":
                        x = Utilities.parseDouble(arg.substring(2),
                                Utilities.format(plugin.getFormatConfig(), "near.location.error", "%value%:" + arg, "%coord%:x"));
                        break;
                    case "y:":
                        y = Utilities.parseDouble(arg.substring(2),
                                Utilities.format(plugin.getFormatConfig(), "near.location.error", "%value%:" + arg, "%coord%:y"));
                        break;
                    case "z:":
                        z = Utilities.parseDouble(arg.substring(2),
                                Utilities.format(plugin.getFormatConfig(), "near.location.error", "%value%:" + arg, "%coord%:z"));
                        break;
                    case "p:":
                        target = Bukkit.getPlayer(arg.substring(2));
                        if (target == null) {
                            sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "noPlayer", "%name%:" + arg.substring(2)));
                            return true;
                        }
                        break;
                    case "w:":
                        world = plugin.getServer().getWorld(arg.substring(2));
                        if (world == null) {
                            sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "noWorld", arg.substring(2)));
                        }
                        break;
                    default:
                        sender.sendMessage("The param " + arg + " is not valid for this command");
                        break;
                }

            }//end arg loop
            if (target != null) {//if a player target is set use that overrides all other params
                if (isConsole) {//console
                    sender.sendMessage("Performing /near with radius:" + radius + " around Target:" + target.getPlayerListName());
                    return doNear(sender, radius, target.getLocation());
                } else {//player

                    if (sender.hasPermission("pansentials.near.other")) {
                        if (s != null) {
                            if (!s.canSee(target)) {

                                sender.sendMessage("Performing /near with radius:" + radius + " around Target:" + target.getPlayerListName());
                                return doNear(sender, radius, target.getLocation());
                            } else {
                                sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "noPlayer"));
                                return true;
                            }
                        }
                    } else {
                        sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "noPermission"));
                        return true;
                    }
                }
            }
            //Process Location based
            if (isConsole) {
                if (x != null && y != null && z != null && world != null) { //handle x:y:z:w for console and player and xyz for player
                    location = new Location(world, x, y, z);
                    sender.sendMessage("Performing /near with radius:" + radius + " around Location x:" + x + "y:" + y + "z:" + z + " in World:" + world.getName());
                    return doNear(sender, radius, location);
                } else {
                    sender.sendMessage("Insufficient Params to process command");
                    return true;
                }
            } else {
                if (sender.hasPermission("pansentials.near.coord")) {
                    if (x != null && y != null && z != null) {
                        if (world == null) {
                            world = s != null ? s.getWorld() : null;
                        }
                        location = new Location(world, x, y, z);
                        sender.sendMessage("Performing /near with radius:" + radius + " around Location x:" + x + "y:" + y + "z:" + z + " in World:" + (world != null ? world.getName() : "No World Found"));
                        return doNear(s, radius, location);
                    } else {
                        return doNear(sender, radius, s != null ? s.getLocation() : null);
                    }
                } else {
                    sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "near.coord.noPermission"));
                }
            }

        }

        return false;
}

    private boolean doNear(CommandSender sender, Integer radius, Location location) {
        if (location == null) {
            sender.sendMessage("Location not found");
            return false;
        }
        Double rad = radius.doubleValue();
        Collection<Entity> entities = location.getWorld().getNearbyEntities(location,rad,rad,rad);
        Iterator<Entity> iter = entities.iterator();
        Map<Player, Double> results = new HashMap<>();
        while(iter.hasNext()){
            Entity ent = iter.next();
            if(ent instanceof Player ){
                results.put((Player)ent,location.distance(ent.getLocation()));
            }
        }
        if (results.size()==0){
            sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "near.empty"));
            return true;
        }
        results =  Utilities.sortByValue(results);
        for (Map.Entry pair : results.entrySet()) {
            Player player = (Player) pair.getKey();
            Double distance = (Double) pair.getValue();
            sender.sendMessage(ChatColor.GOLD + "|PLAYER : DISTANCE");
            sender.sendMessage(ChatColor.GREEN + "|" + player.getName() + " : " + distance.intValue());
        }
        return true;
    }

    @Override
    public void onEnable() {
        plugin.getCommand("near").setExecutor(this);
        FileConfiguration config = plugin.getConfig();
        config.addDefault("near.default-radius", 10);
    }

    @Override
    public void onDisable() {
        plugin.getCommand("near").setExecutor(null);
    }

    @Override
    public void setPandoraInstance(MasterPlugin plugin) {
        this.plugin = plugin;
    }
}
