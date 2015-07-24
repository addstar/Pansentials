package au.com.addstar.pansentials.modules;

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
import org.bukkit.entity.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

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

    private double radius;

    private Player target;

    private boolean verbose;

    //maxRadius set in doNear

    protected Class<?>[] testClasses;

    /**
     *
     * @param sender the CommandSender
     * @param command  the command we are running
     * @param cmd a string representation of the command
     * @param args the extra arguements
     * @return boolean true on success or false on failure.
     */

    @Override
    public boolean onCommand(CommandSender sender, Command command, String cmd, String[] args) {
        Map<Entity, Double> filteredEntities = new LinkedHashMap<>();
        radius = plugin.getConfig().getDouble("near.default-radius", 50);
        int arglength = args.length;
        String entityType = "Unknown";
        verbose = false;
        if (command.getName().equalsIgnoreCase("near")) {
            testClasses = new Class<?>[] {Player.class};
            entityType = "Players";
        } else if (command.getName().equalsIgnoreCase("animals")) {
            testClasses = new Class<?>[] {Animals.class, Golem.class};
            entityType = "Animals";
        } else if (command.getName().equalsIgnoreCase("monsters")) {
            testClasses = new Class<?>[] {Monster.class, Slime.class, Ghast.class};
            entityType = "Monsters";
        } else if(command.getName().equalsIgnoreCase("npcs")) {
            testClasses = new Class<?>[] {NPC.class};
            entityType = "NPCs";
        }

        if (sender instanceof ConsoleCommandSender) {
            if (arglength == 0) { // "near"
                sender.sendMessage("Running Command without params as console is not supported.");
                return true;
            } else if (arglength <= 3) { // "near player <opt r>"
                target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    sender.sendMessage(args[0] + " is not valid for radius or Player name");
                    return false;
                }
                if (arglength >= 2) {
                    try {
                        radius = Double.parseDouble(args[1]);
                    } catch (NumberFormatException exception) {
                        // not a Double
                        sender.sendMessage("The value for radius could not be determined");
                        sender.sendMessage(args[1] + " is not valid for radius using default value of " + radius);
                    }

                }
                if (arglength == 3) {
                    if(args[2].equals("-v") || args[2].equals("-verbose")) {
                        verbose = true;
                    } else {
                        sender.sendMessage(args[2] + " is not valid for 3rd param using " + Arrays.toString(args) + "did you want 'verbose or v'");
                    }
                }
                Map<Entity, Double> totalEntities = doNear(radius, target.getLocation());
                for (Map.Entry<Entity, Double> pair : totalEntities.entrySet()) {
                    for (Class<?> testClass : testClasses) {
                        if (testClass.isInstance(pair.getKey())) {
                            filteredEntities.put(pair.getKey(), pair.getValue());
                        }
                    }
                }
                return printMap(filteredEntities, sender, entityType,totalEntities.size(), verbose);
            } else if (args.length >= 4 || arglength <= 6) { // "near x y z world <optional r> <optional -v>"
                World world = Bukkit.getWorld(args[3]);
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
                if (arglength == 6) {
                    if(args[5].equals("-v") || args[5].equals("-verbose")) {
                        verbose = true;
                    } else {
                        sender.sendMessage(args[5] + " is not valid for 6th param using " + Arrays.toString(args) + "did you want '-verbose or -v'");
                    }
                }
                Map<Entity, Double> totalEntities = doNear(radius, location);
                for (Map.Entry<Entity, Double> pair : totalEntities.entrySet()) {
                    for (Class<?> testClass : testClasses) {
                        if (testClass.isInstance(pair.getKey())) {
                            filteredEntities.put(pair.getKey(), pair.getValue());
                        }
                    }
                }
                return printMap(filteredEntities, sender, entityType,totalEntities.size(), verbose);

            } else {
                sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "near.console.help"));
                return false;
            }
        } else {
            if (sender instanceof Player) {
                Player s = (Player) sender;
                if (arglength == 0) { // "near"
                    return doPlayerNear((Player)sender, entityType, (Player)sender, radius, verbose);
                } else if (arglength >= 1 || arglength <= 3) { // "near player <opt r>"
                    if(arglength == 1) {
                        if(args[0].equals("-v") || args[0].equals("-verbose")) {
                            verbose = true;
                            return doPlayerNear((Player) sender, entityType, (Player) sender, radius, true);
                        }
                    }
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
                            sender.sendMessage(args[0] + " is not valid for radius or Player name");
                            return false;
                        }
                    }
                    if (arglength >= 2) {
                        try {
                            radius = Double.parseDouble(args[1]);
                        } catch (NumberFormatException exception) {
                            //not a Double
                            if(args[1].equals("-v") || args[1].equals("-verbose")) {
                                verbose = true;
                            } else {
                                sender.sendMessage("The value for radius could not be determined");
                                sender.sendMessage(args[1] + " is not valid for radius using default value of " + radius);
                            }
                        }

                    }
                    if (arglength >= 3) {
                        if(args[2].equals("-v") || args[2].equals("-verbose")) {
                            verbose = true;
                        } else {
                            sender.sendMessage(args[2] + " is not valid for 3rd param using " + Arrays.toString(args) + "did you want '-verbose or -v'");
                        }
                    }
                    if (target == null) {
                        target = s;
                    }
                    if (target != s && !s.canSee(target)) {
                        sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "noPlayer", "%name%:" + args[0]));
                        return true;
                    }
                    return doPlayerNear((Player)sender, entityType, target, radius, verbose);
                } else {
                    sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "near.help"));
                    return false;
                }
            } else { // command running from anything other than a player or console not supported
                plugin.getServer().getConsoleSender().sendMessage("/near run from sender:" + sender.getClass().getSimpleName() + ": " + sender.getName());
                return false;
            }
        }
    }

    private boolean doPlayerNear(Player sender, String entityType, Player target, Double rad, Boolean v) {
        Map<Entity, Double> filteredEntities = new LinkedHashMap<>();
        if(target == null) {
            target = sender;
        }
        Map<Entity, Double> totalEntities = doNear(rad, target.getLocation());
        for (Map.Entry<Entity, Double> pair : totalEntities.entrySet()) {
            for (Class<?> testClass : testClasses) {
            	if (sender.equals(pair.getKey())) {
            		continue;
            	}
            	
                if (testClass.isInstance(pair.getKey())) {
                    boolean canSee;
                    if (pair.getKey() instanceof Player) {
                        Player p = (Player) pair.getKey();
                        canSee = (sender.hasPermission("vanish.see") || sender.canSee(p));
                    } else {
                        canSee = true;
                    }
                    
                    if (canSee) {
                        filteredEntities.put(pair.getKey(), pair.getValue());
                    }
                }
            }
        }
        if(!sender.hasPermission("pansentials.near.verbose")) {
            verbose = false;
            if (v) {
                sender.sendMessage(Utilities.format(plugin.getFormatConfig(), "noPermission") + "Cannot use -verbose");
            }
        } else {
            verbose = v;
        }
        return printMap(filteredEntities, sender, entityType, totalEntities.size(), verbose);
    }

    private boolean printMap(Map<Entity, Double> result, CommandSender sender, String entityType, int total, boolean verbose) {
        String formatCode;
        StringBuilder message =  new StringBuilder();
        List<String> vmessage = new ArrayList<>();
        result = Utilities.sortByValue(result);
        if (entityType.equals("Players")) {
            formatCode = "%-16s";
        } else {
            formatCode = "%-10s";
        }
        if (verbose) {
            vmessage.add(ChatColor.GOLD + "|" + String.format(formatCode, entityType) + " : DISTANCE @ Location (X,Y,Z)");
        } else {
            message.append(ChatColor.GOLD).append("Found ").append(entityType).append(": ");
        }
        if (result.isEmpty()) {
            sender.sendMessage(ChatColor.GREEN + "No entities found for that set of params.");
            return true;
        }
        message.append(ChatColor.GREEN);
        int i = 0;
        for (Map.Entry<Entity, Double> pair : result.entrySet()) {

            Entity entity = pair.getKey();
            Double distance = pair.getValue();

            String entityName;
            String entityDetail = "";
            String customName = "";

            if (entityType.equals("Players")) {
                if (entity.hasMetadata("NPC")) {
                    entityName = entity.getName() + "(NPC)";
                } else {
                    entityName = entity.getName();
                }
            } else {
                entityName = entity.getType().toString();
                customName = entity.getName();
                if (customName.equalsIgnoreCase(entityName)) {
                    customName = "";
                }
            }

            // Capitalize the word (initially all caps)
            entityName = entityName.substring(0, 1).toUpperCase() + entityName.substring(1).toLowerCase();
            if (customName.length() > 0) {
                entityDetail = ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + "(\"" + customName + "\")";
            }

            // Pad with spaces to the right for a width of 10
            String distanceText;
            if(verbose) {
                entityName = String.format(formatCode, entityName);
                distanceText = String.format("%2s", distance.intValue());
            } else {
                distanceText = String.valueOf(distance.intValue());
            }

            if (verbose) {
                Location loc = entity.getLocation();
                String locString =
                        Long.toString(Math.round(loc.getX())) + "," +
                                Long.toString(Math.round(loc.getY())) + "," +
                                Long.toString(Math.round(loc.getZ()));
                if (entityType.equals("NPCs")) { //should probably check instanceof here.
                    Villager v = (Villager) entity;
                    entityDetail = entityDetail + ChatColor.GREEN + " Prof:" + v.getProfession();
                }
                if (entity instanceof Ageable) {
                    Ageable ae = (Ageable) entity;
                    long age = TimeUnit.SECONDS.toHours(ae.getTicksLived() / 20);
                    String ageT;
                    if (age > 24) {
                        age = TimeUnit.HOURS.toDays(age);
                        ageT = age + "dys";
                    } else {
                        ageT = age + "hrs";
                    }

                    if (ae.isAdult()) {
                        entityDetail = entityDetail + ChatColor.GREEN + " Adult:" + ageT;
                    } else {
                        entityDetail = entityDetail + ChatColor.GREEN + " Juvenile:" + ageT;
                    }
                    if (ae.canBreed()) {
                        entityDetail = entityDetail + ChatColor.RED + "B";
                    }
                }
                if (entityType.equals("Animals") && entity instanceof Animals) { //should probably check instanceof here.
                    if (entity instanceof Tameable) {
                        Tameable t = (Tameable) entity;
                        if (t.isTamed()) {
                            entityDetail = entityDetail + ChatColor.AQUA + " owner:" + t.getOwner().getName();
                        } else {
                            entityDetail = entityDetail + ChatColor.AQUA + " wild";
                        }
                    }

                    Animals a = (Animals) entity;
                    if (a.isLeashed()) {
                        Location loc2 = a.getLeashHolder().getLocation();
                        String loc2String =
                                Long.toString(Math.round(loc2.getX())) + "," +
                                        Long.toString(Math.round(loc2.getY())) + "," +
                                        Long.toString(Math.round(loc2.getZ()));
                        entityDetail = entityDetail + ChatColor.GOLD + " Leashed:" + a.getLeashHolder().getName() + " @ " + loc2String;
                    }
                }
                vmessage.add(ChatColor.GREEN + "" + i + "." + entityName + " : " + ChatColor.YELLOW + distanceText + " @ " + locString + entityDetail);
            } else {
                message.append(ChatColor.GREEN).append(entityName).append(entityDetail).append(ChatColor.YELLOW).append("(")
                        .append(distanceText).append(")");
            }
            if (i < result.size()) {
                message.append(", ");
            }
            i++;
        }
        if(verbose) {
            sender.sendMessage(vmessage.toArray(new String[result.size()]));
            sender.sendMessage("Total found: " + ChatColor.YELLOW + result.size() + ChatColor.GREEN + " from " + total + " Entities");
        } else {
            sender.sendMessage(message.toString());
        }
        return true;
    }

    /**
     * Returns a map of entities sorted on the distance from the location
     * @param rad the Radius of the box;
     * @param location the location
     * @return a Map<Entity, Double>
     **/

    private Map<Entity, Double> doNear(double rad, Location location) {
        radius = rad;
        double maxRadius = 78;
        radius = (radius > maxRadius) ? maxRadius : radius;
        Collection<Entity> entities = location.getWorld().getNearbyEntities(location, radius, radius, radius);
        Map<Entity, Double> results = new HashMap<>();
        for (Entity ent : entities) {
            Double distance = location.distance(ent.getLocation());
            if (distance <= radius) {
                results.put(ent, distance);
            }
        }
        return results;
    }


    @Override
    public void onEnable() {
        plugin.getCommand("near").setExecutor(this);
        plugin.getCommand("animals").setExecutor(this);
        plugin.getCommand("monsters").setExecutor(this);
        plugin.getCommand("npcs").setExecutor(this);
        plugin.getConfig().addDefault("near.default-radius", 10);
    }

    @Override
    public void onDisable() {
        plugin.getCommand("near").setExecutor(null);
        plugin.getCommand("animals").setExecutor(null);
        plugin.getCommand("monsters").setExecutor(null);
        plugin.getCommand("npcs").setExecutor(null);
    }

    @Override
    public void setPandoraInstance(MasterPlugin plugin) {
        this.plugin = plugin;
    }
}
