package au.com.addstar.pansentials.modules;

import au.com.addstar.pansentials.MasterPlugin;
import au.com.addstar.pansentials.Module;
import au.com.addstar.pansentials.Utilities;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A note regarding permissions
 * A player will require
 * pansentials.near.other to perform any of the commands against another player target
 * Players currently do not have an option to /near against a location defined by x y z
 *
 *  @author benjamincharlton on 27/05/2015.
 */
public class NearModule implements Module, CommandExecutor {

    private MasterPlugin plugin;

    private Double radius;

    private Player target;

    private Double maxRadius = (double) 78;

    protected  static Class<? extends Entity> test; //the Class to test against


    @Override
    @SuppressWarnings("unchecked")
    public boolean onCommand(CommandSender sender, Command command, String cmd, String[] args) {
        Map<Entity, Double> result = new LinkedHashMap();
        radius = plugin.getConfig().getDouble("near.default-radius", 10);
        Integer arglength = args.length;
        String entityType = "UNKNOWN";
        if (command.getName().equalsIgnoreCase("near")) {
            test = Player.class;
            entityType = "PLAYER";
        } else if (command.getName().equalsIgnoreCase("animals")) {
            test = Animals.class;
            entityType = "ANIMAL";
        } else if (command.getName().equalsIgnoreCase("monsters")) {
            test = Monster.class;
            entityType = "MONSTER";
        }
        if (sender instanceof ConsoleCommandSender) {
            boolean showCoords = true;
            if (arglength == 0) { // "near"
                sender.sendMessage("Running Command without params as console is not supported.");
                return true;
            } else if (arglength == 1 || arglength == 2) { // "near player <opt r>"
                target = plugin.getServer().getPlayer(args[0]);
                if (target == null) {
                    sender.sendMessage(args[1] + " is not valid for radius or Player name");
                    return true;
                }
                if (arglength == 2) {
                    try {
                        radius = Double.parseDouble(args[1]);
                    } catch (NumberFormatException exception) {
                        //not a Double
                        sender.sendMessage("The value for radius could not be determined");
                        sender.sendMessage(args[1] + " is not valid for radius using default value of " + radius);
                    }

                }
                for (Map.Entry pair : doNear(radius, target.getLocation()).entrySet()) {
                    if (test.isInstance(pair.getKey())) {
                        result.put((Entity) pair.getKey(), (Double) pair.getValue());
                    }
                }
                return printMap(result, sender, entityType, showCoords);
            } else if (args.length == 4 || arglength == 5) {// "near x y z world <optional r>"
                World world = plugin.getServer().getWorld(args[3]);
                Location location;
                try {
                    location = new Location(world, Double.parseDouble(args[0]), Double.parseDouble(args[1]), Double.parseDouble(args[2]));
                } catch (NumberFormatException error) {
                    sender.sendMessage("One of the 3 values for x y z could not be converted to a coordinate value.");
                    return false;
                }
                if (world == null) {
                    sender.sendMessage(args[3] + " does not reference an available world");
                    return false;
                }
                if (arglength == 5) {
                    try {
                        radius = Double.parseDouble(args[4]);
                    } catch (NumberFormatException exception) {
                        sender.sendMessage("The value for radius could not be determined");
                        sender.sendMessage(args[4] + " is not valid for radius using default value of " + radius);
                    }
                }
                for (Map.Entry pair : doNear(radius, location).entrySet()) {
                    if (test.isInstance(pair.getKey())) {
                        result.put((Entity) pair.getKey(), (Double) pair.getValue());
                    }
                }
                return printMap(result, sender, entityType, showCoords);

            } else {
                sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "near.console.help"));
                return false;
            }
        } else {
            if (sender instanceof Player) {
                Player s = (Player) sender;
                boolean showCoords = true;
                if (arglength == 0) { // "near"
                    for (Map.Entry pair : doNear(radius, s.getLocation()).entrySet()) {
                        if (test.isInstance(pair.getKey())) {
                            Boolean canSee;
                            try {
                                Player p = (Player) pair.getKey();
                                canSee = (s.hasPermission("vanish.see") || s.canSee(p));
                            } catch (ClassCastException e) {//supress if not a player
                                canSee = true;
                            }
                            if (canSee) {
                                result.put((Entity) pair.getKey(), (Double) pair.getValue());
                            }
                        }
                    }
                    return printMap(result, sender, entityType, showCoords);
                } else if (arglength == 1 || arglength == 2) { // "near player <opt r>"
                    if (!s.hasPermission("Pansentials.near.other")) {
                        sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "noPermission"));
                        return true;
                    }
                    target = plugin.getServer().getPlayer(args[0]);
                    if (target == null) {
                        try {
                            radius = Double.parseDouble(args[0]);
                        } catch (NumberFormatException exception) {
                            //not a Double
                            sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "noPlayer", "%name%:" + args[0]));
                            sender.sendMessage("The value for radius could not be determined");
                            sender.sendMessage(args[1] + " is not valid for radius or Player name");
                            return true;
                        }
                    }
                    if (arglength == 2) {
                        try {
                            radius = Double.parseDouble(args[1]);
                        } catch (NumberFormatException exception) {
                            //not a Double
                            sender.sendMessage("The value for radius could not be determined");
                            sender.sendMessage(args[1] + " is not valid for radius using default value of " + radius);
                        }

                    }
                    if (target == null) {
                        target = s;
                    }
                    for (Map.Entry pair : doNear(radius, target.getLocation()).entrySet()) {
                        if (test.isInstance(pair.getKey())) {
                            Boolean canSee;
                            try {
                                Player p = (Player) pair.getKey();
                                canSee = (s.hasPermission("vanish.see") || s.canSee(p));
                            } catch (ClassCastException e) {//supress if not a player
                                canSee = true;
                            }
                            if (canSee) {
                                result.put((Entity) pair.getKey(), (Double) pair.getValue());
                            }
                        }
                    }
                    return printMap(result, sender, entityType, showCoords);
                } else {
                    sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "near.help"));
                    return false;
                }
            } else {//command running from anything other than a player or console not supported
                plugin.getServer().getConsoleSender().sendMessage("/near run from sender:" + sender.getClass().getSimpleName() + ": " + sender.getName());
                return false;
            }
        }
    }

    private boolean printMap(Map<Entity, Double> result, CommandSender sender, String entityType, boolean showCoords) {
        String formatCode;

        if (entityType == "PLAYER") {
            formatCode = "%-15s";
        }
        else {
            formatCode = "%-10s";
        }

        sender.sendMessage(ChatColor.GOLD + "|" + String.format(formatCode, entityType) + " : DISTANCE");
        if (result.isEmpty()) {
            sender.sendMessage((ChatColor.GREEN + "No entities found for that set of params."));
        }
        for (Map.Entry pair : result.entrySet()) {
            Entity entity = (Entity) pair.getKey();
            Double distance = (Double) pair.getValue();

            // Capitalize the word (initially all caps)
            // Pad with spaces to the right for a width of 10
            String entityName;

            if (entityType == "PLAYER") {
                entityName = entity.getName();
            }
            else {
                entityName = entity.getType().toString();
            }

            entityName = String.format(formatCode, entityName.substring(0, 1).toUpperCase() + entityName.substring(1).toLowerCase());
            String distanceText = String.format("%2s", distance.intValue());

            if (showCoords) {
                Location loc = entity.getLocation();
                String locString =
                        Long.toString(Math.round(loc.getX())) + "," +
                        Long.toString(Math.round(loc.getY())) + "," +
                        Long.toString(Math.round(loc.getZ()));

                sender.sendMessage(ChatColor.GREEN + "|" + entityName + " : " + distanceText + " @ " + locString);
            }
            else
            {
                sender.sendMessage(ChatColor.GREEN + "|" + entityName + " : " + distanceText);
            }

        }
        return true;
    }


    /**
     * Returns a map of entities sorted on the distance from the location
     * @param rad the Radius of the box;
     * @param location the location
     * @return a Map<Entity, Double>
     **/

    private Map<Entity, Double> doNear(Double rad, Location location) {
        if (rad == null) {
            rad = radius;
        }
        if (rad > maxRadius) {
            rad = maxRadius;
        }
        Collection<Entity> entities = location.getWorld().getNearbyEntities(location, rad, rad, rad);
        Map<Entity, Double> results = new HashMap<>();
        for (Entity ent : entities) {
            Double distance = location.distance(ent.getLocation());
            if(distance<=rad) {
                results.put(ent, distance);
            }
        }
        results = Utilities.sortByValue(results);
        return results;
    }


    @Override
    public void onEnable() {
        plugin.getCommand("near").setExecutor(this);
        plugin.getCommand("animals").setExecutor(this);
        plugin.getCommand("monsters").setExecutor(this);
        plugin.getConfig().addDefault("near.default-radius", 10);

    }

    @Override
    public void onDisable() {
        plugin.getCommand("near").setExecutor(null);
        plugin.getCommand("animals").setExecutor(null);
        plugin.getCommand("monsters").setExecutor(null);
    }

    @Override
    public void setPandoraInstance(MasterPlugin plugin) {
        this.plugin = plugin;
    }
}
