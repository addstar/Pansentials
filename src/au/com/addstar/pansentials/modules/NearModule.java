package au.com.addstar.pansentials.modules;

import au.com.addstar.pansentials.MasterPlugin;
import au.com.addstar.pansentials.Module;
import au.com.addstar.pansentials.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
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

    private int radius;

    private Player target;

    private Location location;

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
            c = (ConsoleCommandSender) sender;
            isConsole = true;
        } else {
            sender.sendMessage("Must be player or console to run.");
            return true;
        }

        radius = plugin.getConfig().getInt("near.default-radius");
        if (args.length == 0) {
            if (isConsole) {
                sender.sendMessage("Running Command without params as console is not supported.");
                return true;
            }
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
            World world = null;
            for (String arg : args) {
                switch (arg.substring(0, 1)) {
                    case ("r:"):
                        radius = Utilities.parseInt(arg.substring(2), "r: must be followed by an positive integer");
                        break;
                    case ("x:"):
                        x = Utilities.parseDouble(arg.substring(2),
                                Utilities.format(plugin.getFormatConfig(), "near.location.error", "%value%:" + args[0], "%coord%:x"));
                        break;
                    case ("y:"):
                        y = Utilities.parseDouble(arg.substring(2),
                                Utilities.format(plugin.getFormatConfig(), "near.location.error", "%value%:" + args[0], "%coord%:y"));
                        break;
                    case ("z:"):
                        z = Utilities.parseDouble(arg.substring(2),
                                Utilities.format(plugin.getFormatConfig(), "near.location.error", "%value%:" + args[0], "%coord%:z"));
                        break;
                    case ("p:"):
                        Player target = Bukkit.getPlayer(arg.substring(2));
                        if (target == null) {
                            sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "noPlayer", arg.substring(2)));
                            return true;
                        }
                        break;
                    case ("w:"):
                        world = plugin.getServer().getWorld(arg.substring(2));
                        if (world == null) {
                            sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "noWorld", arg.substring(2)));
                        }

                }

            }//end arg loop
            if (target != null) {//if a player target is set use that overrides all other params
                if (isConsole) {//console
                    sender.sendMessage("Performing /near with radius:" + radius + " around Target:" + target.getPlayerListName());
                    return doNear(c, radius, target.getLocation());
                } else {//player

                    if (sender.hasPermission("pansentials.near.other")) {
                        if (!s.canSee(target)) {

                            sender.sendMessage("Performing /near with radius:" + radius + " around Target:" + target.getPlayerListName());
                            return doNear(s, radius, target.getLocation());
                        } else {
                            sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "noPlayer"));
                            return true;
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
                    return doNear(c, radius, location);
                }
            } else {
                if (sender.hasPermission("pansentials.near.coord")) {
                    if (x != null && y != null && z != null) {
                        if (world == null) {
                            world = s.getWorld();
                        }
                        location = new Location(world, x, y, z);
                        sender.sendMessage("Performing /near with radius:" + radius + " around Location x:" + x + "y:" + y + "z:" + z + " in World:" + world.getName());
                        return doNear(s, radius, location);
                    }
                } else {
                    sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "near.coord.noPermission"));
                }
            }

        }

    return true;
}

    private boolean doNear(ConsoleCommandSender c, Integer radius, Location location) {
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
            c.sendMessage(Utilities.format(plugin.getFormatConfig(), "near.empty"));
            return true;
        }
        results =  Utilities.sortByValue(results);
        Iterator iterator = results.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry pair = (Map.Entry)iterator.next();
            Player player = (Player) pair.getKey();
            Double distance = (Double) pair.getValue();
            c.sendMessage(Utilities.format(plugin.getFormatConfig(), "near.info", "%player%:" + player.getName()) + "%distance%:" + distance.toString());
        }
        return true;
    }

    private boolean doNearSpigot(Player sender, Integer radius, Location location){
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
        Iterator iterator = results.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry pair = (Map.Entry)iterator.next();
            Player player = (Player) pair.getKey();
            Double distance = (Double) pair.getValue();
            sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "near.info", "%player%:" + player.getName()) + "%distance%:" + distance.toString());
        }
        return true;

    }
    private boolean doNear(Player sender, Integer radius, Location location){
        final long radiusSquared = radius * radius;
        Map<Player, Double> results = new HashMap<>();
        boolean showHidden = sender.hasPermission("vanish.see");//check this is the correct pex or that we have a permission to see vanished players
        for(Player player : location.getWorld().getPlayers()){ //pretty crap we have to get all players in the world and parse through them
            //todo Do we need a world check here to ensure this isn't been run on a player in hardcore
            if (!player.equals(sender) && sender.canSee(player) || showHidden){ //do we need to check if the player isHidden from the sender or is canSee enough
                final Location playerLoc = player.getLocation();
                final long delta = (long)playerLoc.distanceSquared(location);
                if (delta < radiusSquared){
                    results.put(player, Math.sqrt(delta));
                }
            }
        }
        if (results.size()==0){
            sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "near.empty"));
            return true;
        }

        results =  Utilities.sortByValue(results);
        Iterator iter = results.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry pair = (Map.Entry)iter.next();
            Player player = (Player) pair.getKey();
            Double distance = (Double) pair.getValue();
            sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "near.info", "%player%:" + player.getName()) + "%distance%:" + distance.toString());
        }
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
