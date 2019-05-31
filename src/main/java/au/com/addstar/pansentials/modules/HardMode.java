package au.com.addstar.pansentials.modules;

import au.com.addstar.pansentials.MasterPlugin;
import au.com.addstar.pansentials.Module;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created for the Charlton IT Project.
 * Created by benjicharlton on 31/05/2019.
 */
public class HardMode implements Module, Listener, CommandExecutor, TabCompleter {

    private Map<Player, Boolean> enabledPlayers = new HashMap<>();
    private List<World> enabledWorlds = new ArrayList<>();
    private int radius = 10;
    private MasterPlugin plugin;
    private BukkitTask mainTask = null;
    private List<BukkitTask> tasks = new ArrayList<>();

    @Override
    public void onEnable() {
        plugin.getCommand("hardmode").setExecutor(this);

    }

    @Override
    public void onDisable() {
        if (mainTask != null && !mainTask.isCancelled()) {
            mainTask.cancel();
        }
        tasks.forEach(bukkitTask -> {
            if (!bukkitTask.isCancelled()) bukkitTask.cancel();
        });
        plugin.getCommand("hardmode").setExecutor(null);
    }

    @Override
    public void setPandoraInstance(MasterPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerSwitchWorld(PlayerChangedWorldEvent event) {
        if (enabledWorlds.contains(event.getPlayer().getLocation().getWorld())) {
            enabledPlayers.put(event.getPlayer(), true);
            startMainTask();
        } else {
            enabledPlayers.put(event.getPlayer(), false);
        }
        checkandStopTask();
    }

    private void checkandStopTask() {
        if (enabledWorlds.isEmpty()) {
            if (mainTask != null) {
                mainTask.cancel();
                return;
            }
        }
        if (!enabledPlayers.containsValue(true)) {
            if (mainTask != null) {
                mainTask.cancel();
            }
        }
    }

    private void startMainTask() {
        if (mainTask == null || mainTask.isCancelled())
            mainTask = createMainTask();
    }
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (enabledWorlds.contains(event.getPlayer().getLocation().getWorld())) {
            enabledPlayers.put(event.getPlayer(), true);
            startMainTask();
        } else {
            enabledPlayers.put(event.getPlayer(), false);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getType().equals(EntityType.ZOMBIE)) {
            if (event.getEntity().hasMetadata("fireworkTaskID")) {
                event.getEntity().getMetadata("fireworkTaskID")
                        .stream()
                        .filter(metadataValue -> metadataValue.getOwningPlugin().getName().equals(plugin.getName()))
                        .findFirst()
                        .ifPresent(metadataValue -> tasks.forEach(bukkitTask -> {
                                    if (bukkitTask.getTaskId() == metadataValue.asInt()) {
                                        bukkitTask.cancel();
                                    }
                                })
                        );
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        checkandStopTask();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        World world = null;
        if (commandSender instanceof Player) {
            world = ((Player) commandSender).getLocation().getWorld();
        } else {
            if (args.length == 2) {
                world = Bukkit.getWorld(args[1]);
            }
        }
        if (world == null)
            return false;
        if (args.length > 1) {
            switch (args[0].toLowerCase()) {
                case "enabled":
                    enabledWorlds.add(world);
                    commandSender.sendMessage("Hardmode enabled for " + world.getName());
                    world.getPlayers().forEach(player -> enabledPlayers.put(player, true));
                    mainTask = createMainTask();
                    return true;
                case "disabled":
                    enabledWorlds.remove(world);
                    world.getPlayers().forEach(player -> enabledPlayers.put(player, false));
                    commandSender.sendMessage("Hardmode disabled for " + world.getName());
                    checkandStopTask();
                    return true;
            }
        }
        return false;

    }

    private BukkitTask createMainTask() {
        return Bukkit.getScheduler().runTaskTimer(plugin, () -> enabledPlayers.forEach((player, aBoolean) -> {
            if (aBoolean) {
                createEvents(player);
            }
        }), 200, 100);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        List<String> result = new ArrayList<>();
        if (commandSender instanceof Player) {
            if (args.length == 0) {
                result.add("enabled");
                result.add("disabled");
                return result;
            }
        } else {
            switch (args.length) {
                case 0:
                    result.add("enabled");
                    result.add("disabled");
                    return result;
                case 1:
                    List<String> names = Bukkit.getWorlds().stream().map(World::getName).distinct().collect(Collectors.toList());
                    result.addAll(names);
                    return result;
            }
        }
        return result;

    }

    private Location getRandomLocation(Location loc, int radius) {
        int xpos = new Random().nextInt(1);
        int zpos = new Random().nextInt(1);
        int xran = new Random().nextInt(radius);
        int zran = new Random().nextInt(radius);
        if (xpos == 1) {
            if (zpos == 1) {
                return new Location(loc.getWorld(), loc.getX() + xran, loc.getY(), loc.getZ() + zran);
            } else {
                return new Location(loc.getWorld(), loc.getX() + xran, loc.getY(), loc.getZ() - zran);
            }
        } else if (zpos == 1) {
            return new Location(loc.getWorld(), loc.getX() - xran, loc.getY(), loc.getZ() + zran);
        } else {
            return new Location(loc.getWorld(), loc.getX() - xran, loc.getY(), loc.getZ() - zran);
        }

    }

    void createEvents(Player p) {
        int num = new Random().nextInt(10);
        for (int i = 0; i < num; i++) {
            Location loc = getRandomLocation(p.getLocation(), radius);
            int event = new Random().nextInt(5);
            if (loc.getWorld() == null) return;
            switch (event) {
                case 0:
                    return;
                case 1:
                    loc.getWorld().strikeLightning(loc);
                    return;
                case 2:
                    Entity e = loc.getWorld().spawnEntity(loc, EntityType.CREEPER);
                    ((Creeper) e).setPowered(true);
                    ((Creeper) e).setTarget(p);
                    ((Creeper) e).setExplosionRadius(10);
                    return;
                case 3:
                    Zombie z = (Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE_VILLAGER);
                    z.setCustomName(ChatColor.RED + "Zombie King" + ChatColor.RESET);
                    z.setTarget(p);
                    z.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 300, 3, true, true, true));
                    AttributeInstance armor = z.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS);
                    armor.setBaseValue(armor.getValue() * 3);
                    AttributeInstance attackSpeed = z.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
                    attackSpeed.setBaseValue(attackSpeed.getValue() * 2);
                    AttributeInstance attackForce = z.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
                    attackForce.setBaseValue(attackForce.getValue() * 3);
                    z.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(300);
                    z.getAttribute(Attribute.ZOMBIE_SPAWN_REINFORCEMENTS).setBaseValue(1D);
                    AttributeInstance health = z.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                    health.setBaseValue(health.getBaseValue() * 3);
                    BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                        if (z.isDead())
                            return;
                        Firework firewrk = (Firework) z.getLocation().getWorld().spawnEntity(z.getLocation(), EntityType.FIREWORK);
                        firewrk.getFireworkMeta().addEffect(FireworkEffect.builder().withColor(Color.RED).with(FireworkEffect.Type.BALL).build());

                    }, 20, 40);
                    tasks.add(task);
                    z.setCustomNameVisible(true);
                    z.setMetadata("fireworkTaskID",
                            new FixedMetadataValue(plugin, task.getTaskId()));
                    return;
                case 4:
                    Monster vex = (Monster) loc.getWorld().spawnEntity(loc, EntityType.VEX);
                    vex.setTarget(p);
                    return;
                case 5:
                    loc.getWorld().strikeLightning(loc);
                    return;
            }
        }
    }
}
