package au.com.addstar.pansentials.modules;

import au.com.addstar.pansentials.MasterPlugin;
import au.com.addstar.pansentials.Module;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.*;

public class PlayerListModule implements Module, CommandExecutor {
	
	private MasterPlugin plugin;
	private static Chat chat = null;

	// Class to store player display name and suffix
	private static class PlayerInfo {
		private String displayName;
		private String suffix;

		public PlayerInfo(String displayName, String suffix) {
			this.displayName = displayName;
			this.suffix = suffix;
		}

		public String getDisplayName() {
			return displayName;
		}

		public String getSuffix() {
			return suffix;
		}

		public String getPlayerColour() {
			if (getSuffix() != null && !getSuffix().isEmpty()) {
				return ChatColor.translateAlternateColorCodes('&', getSuffix());
			}
			return ChatColor.WHITE.toString();
		}
	}

	private static void setupVault() {
		RegisteredServiceProvider<Chat> rsp = Bukkit.getServer().getServicesManager().getRegistration(Chat.class);
		chat = rsp.getProvider();
	}
	
	@Override
	public void onEnable() {
		setupVault();
		plugin.getCommand("mvw").setExecutor(this);
	}
	
	@Override
	public void onDisable() {
		plugin.getCommand("mvw").setExecutor(null);
	}
	
	@Override
	public void setPandoraInstance(MasterPlugin plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String cmd,
							 String[] args) {
		// Get all players online
		Collection<? extends Player> players = Bukkit.getOnlinePlayers();

		// Create a map to hold worlds and their players
		Map<World, List<PlayerInfo>> worldPlayersMap = new HashMap<>();

		// Iterate over all online players
		for (Player player : players) {
			// Check if player is hidden from the command invoker
			boolean vanished = isVanished(player);
			if (!vanished || (vanished && canSeeVanished(sender))) {
				// Get the player's display name
				PlayerInfo pinfo = new PlayerInfo(player.getDisplayName(), chat.getPlayerSuffix(player));

				// Add the player to their world's list in the map
				worldPlayersMap.computeIfAbsent(player.getWorld(), k -> new ArrayList<>()).add(pinfo);
			}
		}

		// Output total number of players on the server
		sender.sendMessage(ChatColor.AQUA + "--- Worlds and players (" + players.size() + "): ---");

		// Convert the map's entry set to a list
		List<Map.Entry<World, List<PlayerInfo>>> worldEntries = new ArrayList<>(worldPlayersMap.entrySet());

		// Sort the list by world name
		worldEntries.sort(Comparator.comparing(e -> e.getKey().getName(), String.CASE_INSENSITIVE_ORDER));

		// Iterate over each entry in the sorted list
		for (Map.Entry<World, List<PlayerInfo>> entry : worldEntries) {
			// Get the world and list of player display names
			World world = entry.getKey();
			List<PlayerInfo> playerInfos = entry.getValue();

			// Sort the list of player display names alphabetically
			playerInfos.sort(Comparator.comparing(pi -> pi.getDisplayName(), String.CASE_INSENSITIVE_ORDER));

			// Use a StringBuilder to build the line
			StringBuilder line = new StringBuilder(ChatColor.GREEN + world.getName() + " "
					+ ChatColor.YELLOW + "(" + playerInfos.size() + ")"
					+ ChatColor.WHITE + ": ");

			// Add each player name to the line
			for (PlayerInfo playerInfo : playerInfos) {
				line.append(playerInfo.getPlayerColour())
						.append(playerInfo.displayName)
						.append(ChatColor.WHITE)
						.append(", ");
			}

			// Remove the trailing comma and space
			if (line.length() > 0) {
				line.setLength(line.length() - 4);  // Chopping off trailing comma, space and colour bytes
			}

			// Output the line
			sender.sendMessage(line.toString());
		}
		return true;
	}

	private boolean isVanished(Player player) {
		for (MetadataValue meta : player.getMetadata("vanished")) {
			if (meta.asBoolean()) return true;
		}
		return false;
	}

	private boolean canSeeVanished(CommandSender sender) {
		if (sender instanceof Player) {
			if (sender.hasPermission("pv.see") || sender.hasPermission("vanish.see")) {
				return true; // Allowed to see vanished
			} else {
				return false; // No perm to see vanished
			}
		}
		// Console can always see vanished
		return true;
	}
}
