package au.com.addstar.pansentials.modules;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import au.com.addstar.monolith.attributes.ItemAttributeModifier;
import au.com.addstar.monolith.MonoItemStack;
import au.com.addstar.monolith.util.Attributes;
import au.com.addstar.pansentials.CommandModule;
import net.md_5.bungee.api.ChatColor;

public class ItemAttributeModule extends CommandModule {
	public ItemAttributeModule() {
		super("itemattribute");
	}
	
	private void displayUsage(CommandSender sender, String label) {
		sender.sendMessage(ChatColor.GOLD + "Usage:");
		sender.sendMessage(ChatColor.WHITE + "/" + label + " list");
		sender.sendMessage(ChatColor.GRAY + " Lists all attributes and their values for the held item");
		
		sender.sendMessage(ChatColor.WHITE + "/" + label + " reset");
		sender.sendMessage(ChatColor.GRAY + " Resets all attributes on the held item");
		
		sender.sendMessage(ChatColor.WHITE + "/" + label + " <attributeName>");
		sender.sendMessage(ChatColor.GRAY + " Shows information for the specified attribute on the held item");
		
		sender.sendMessage(ChatColor.WHITE + "/" + label + " <attributeName> add <name> [uuid] <value> <operation> <slot>");
		sender.sendMessage(ChatColor.GRAY + " Adds a modifier for the attribute on the held item");
		
		sender.sendMessage(ChatColor.WHITE + "/" + label + " <attributeName> remove <name|uuid>");
		sender.sendMessage(ChatColor.GRAY + " Removes a modifier for the attribute on the held item");
		
		sender.sendMessage(ChatColor.WHITE + "/" + label + " <attributeName> clear");
		sender.sendMessage(ChatColor.GRAY + " Removes all modifiers for the attribute on the held item");
		
		sender.sendMessage(ChatColor.WHITE + "/" + label + " <attributeName> set <name|uuid> value <value>");
		sender.sendMessage(ChatColor.GRAY + " Changes the value of a modifier on the held item");
		
		sender.sendMessage(ChatColor.WHITE + "/" + label + " <attributeName> set <name|uuid> operation <operation>");
		sender.sendMessage(ChatColor.GRAY + " Changes the operation of a modifier on the held item");
		
		sender.sendMessage(ChatColor.WHITE + "/" + label + " <attributeName> set <name|uuid> slot <slot>");
		sender.sendMessage(ChatColor.GRAY + " Changes the applicable slot of a modifier on the held item");
		
		sender.sendMessage(ChatColor.WHITE + "/" + label + " <attributeName> set <name|uuid> name <name>");
		sender.sendMessage(ChatColor.GRAY + " Renames a modifier on the held item");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command can only be used by players");
			return true;
		}
		
		// Display usage
		if (args.length == 0) {
			displayUsage(sender, label);
			return true;
		}
		
		Player player = (Player)sender;
		ItemStack held = player.getInventory().getItemInMainHand();
		if (held == null || held.getType() == Material.AIR) {
			sender.sendMessage(ChatColor.RED + "You need to be holding an item to use this");
			return true;// List all attributes and their modifiers
		}
		
		MonoItemStack item = new MonoItemStack(held);
		
		if (args[0].equalsIgnoreCase("list")) {
			handleListAttributes(player, item);
		} else if (args[0].equalsIgnoreCase("reset")) {
			handleReset(player, item);
		} else {
			// It needs an attribute id
			Attribute attribute = Attributes.fromId(args[0]);
			if (attribute == null) {
				player.sendMessage(ChatColor.RED + "Unknown attribute: " + args[0]);
				return true;
			}
			
			if (args.length == 1) {
				handleDisplayAttribute(player, item, attribute);
			} else if (args[1].equalsIgnoreCase("add")) {
				handleAddModifier(player, item, attribute, label, Arrays.copyOfRange(args, 2, args.length));
			} else if (args[1].equalsIgnoreCase("remove")) {
				handleRemoveModifier(player, item, attribute, label, Arrays.copyOfRange(args, 2, args.length));
			} else if (args[1].equalsIgnoreCase("clear")) {
				handleClearModifiers(player, item, attribute);
			} else if (args[1].equalsIgnoreCase("set")) {
				handleSetModifier(player, item, attribute, label, Arrays.copyOfRange(args, 2, args.length));
			} else {
				player.sendMessage(ChatColor.RED + "Unknown action " + args[1]);
			}
		}
		
		return true;
	}
	
	private void handleListAttributes(Player player, MonoItemStack item) {
		// List all attributes and their modifiers
		boolean showedAny = false;
		for (Attribute attribute : Attribute.values()) {
			Collection<ItemAttributeModifier> modifiers = item.getAttributes().getModifiers(attribute);
			if (!modifiers.isEmpty()) {
				showedAny = true;
				player.sendMessage(ChatColor.YELLOW + attribute.name().toLowerCase());
				displayModifiers(player, modifiers);
			}
		}
		
		if (!showedAny) {
			player.sendMessage(ChatColor.GRAY + "This item has no modifiers applied");
		}
	}
	
	private void handleReset(Player player, MonoItemStack item) {
		item.getAttributes().clearModifiers();
		
		player.sendMessage(ChatColor.GOLD + "All modifiers have been cleared");
	}
	
	private void handleDisplayAttribute(Player player, MonoItemStack item, Attribute attribute) {
		Collection<ItemAttributeModifier> modifiers = item.getAttributes().getModifiers(attribute);
		
		player.sendMessage(ChatColor.YELLOW + attribute.name().toLowerCase());
		
		displayModifiers(player, modifiers);
	}
	
	private void displayModifiers(CommandSender sender, Collection<ItemAttributeModifier> modifiers) {
		sender.sendMessage(ChatColor.WHITE + "Modifiers:");
		
		for (ItemAttributeModifier modifier : modifiers) {
			String line = ChatColor.GRAY + " " + modifier.getName() + ": ";
			switch (modifier.getOperation()) {
			case ADD_NUMBER:
				if (modifier.getAmount() >= 0) {
					line += " +" + modifier.getAmount();
				} else {
					line += " " + modifier.getAmount();
				}
				break;
			case ADD_SCALAR:
				line += String.format(" +%.1f%%", (modifier.getAmount()) * 100);
				break;
			case MULTIPLY_SCALAR_1:
				line += String.format(" x%.1f%%", (1 + modifier.getAmount()) * 100);
				break;
			}
			
			if (modifier.isSlotSpecific()) {
				line += " in " + modifier.getSlot().name().toLowerCase();
			}
			
			sender.sendMessage(line);
		}
	}
	
	private void handleAddModifier(Player player, MonoItemStack item, Attribute attribute, String label, String[] args) {
		if (args.length < 4) {
			player.sendMessage(ChatColor.RED + "You have not supplied enough arguments to add a modifier:");
			player.sendMessage("Usage: /" + label + " " + Attributes.getId(attribute) + " add <name> [uuid] <value> <operation> <slot>");
			return;
		}
		
		String name = args[0];
		
		// Parse UUID if available
		int next;
		UUID id;
		if (args.length >= 5) {
			// Try as UUID first
			try {
				id = UUID.fromString(args[1]);
			} catch (IllegalArgumentException e) {
				// Try as a long
				try {
					long val = Long.parseLong(args[1]);
					id = new UUID(0, val);
				} catch (NumberFormatException e2) {
					// Dont know
					player.sendMessage(ChatColor.RED + "You have supplied an incorrect value for [uuid]. You provided " + args[1]);
					player.sendMessage("Usage: /" + label + " " + Attributes.getId(attribute) + " add <name> [uuid] <value> <operation> <slot>");
					return;
				}
			}
			
			next = 2;
		} else {
			id = UUID.randomUUID();
			next = 1;
		}
		
		// Parse value
		double value;
		try {
			value = Double.parseDouble(args[next]);
		} catch (NumberFormatException e) {
			player.sendMessage(ChatColor.RED + "You have supplied an incorrect value for <value>. You provided " + args[next] + ", expected a number");
			player.sendMessage("Usage: /" + label + " " + Attributes.getId(attribute) + " add <name> [uuid] <value> <operation> <slot>");
			return;
		}
		
		// Parse operation
		Operation operation;
		switch (args[next+1].toLowerCase()) {
		case "add":
			operation = Operation.ADD_NUMBER;
			break;
		case "add_scale":
			operation = Operation.ADD_SCALAR;
			break;
		case "scale":
			operation = Operation.MULTIPLY_SCALAR_1;
			break;
		default:
			// Try as a number
			try {
				int op = Integer.parseInt(args[next+1]);
				if (op < 0 || op > 2) {
					player.sendMessage(ChatColor.RED + "You have supplied an incorrect value for <operation>. You provided " + args[next+1] + ", expected \"add\", \"add_scale\", \"scale\" or 0-2");
					player.sendMessage("Usage: /" + label + " " + Attributes.getId(attribute) + " add <name> [uuid] <value> <operation> <slot>");
					return;
				}
				
				operation = Operation.values()[op];
			} catch (NumberFormatException e) {
				player.sendMessage(ChatColor.RED + "You have supplied an incorrect value for <operation>. You provided " + args[next+1] + ", expected \"add\", \"add_scale\", \"scale\" or 0-2");
				player.sendMessage("Usage: /" + label + " " + Attributes.getId(attribute) + " add <name> [uuid] <value> <operation> <slot>");
				return;
			}
		}
		
		// Parse slot
		EquipmentSlot slot;
		switch (args[next+2].toLowerCase()) {
		case "head":
			slot = EquipmentSlot.HEAD;
			break;
		case "chest":
			slot = EquipmentSlot.CHEST;
			break;
		case "legs":
			slot = EquipmentSlot.LEGS;
			break;
		case "feet":
			slot = EquipmentSlot.FEET;
			break;
		case "hand":
		case "mainhand":
			slot = EquipmentSlot.HAND;
			break;
		case "offhand":
		case "off_hand":
			slot = EquipmentSlot.OFF_HAND;
			break;
		case "all":
		case "any":
			slot = null;
			break;
		default:
			player.sendMessage(ChatColor.RED + "You have supplied an incorrect value for <slot>. You provided " + args[next+2] + ", expected \"head\", \"chest\", \"legs\", \"feet\", \"hand\", \"offhand\", \"any\"");
			player.sendMessage("Usage: /" + label + " " + Attributes.getId(attribute) + " add <name> [uuid] <value> <operation> <slot>");
			return;
		}
		
		// Create the modifier
		ItemAttributeModifier modifier = new ItemAttributeModifier(id, name, value, operation, slot);
		item.getAttributes().addModifier(attribute, modifier);
		
		player.sendMessage(ChatColor.GREEN + "Added the modifier \"" + name + "\" to that item");
	}
	
	private void handleRemoveModifier(Player player, MonoItemStack item, Attribute attribute, String label, String[] args) {
		if (args.length == 0) {
			player.sendMessage(ChatColor.RED + "You have not supplied enough arguments to remove a modifier:");
			player.sendMessage("Usage: /" + label + " " + Attributes.getId(attribute) + " remove <name|uud>");
			return;
		}
		
		// Try as UUID first
		UUID id = null;
		try {
			id = UUID.fromString(args[0]);
		} catch (IllegalArgumentException e) {
			// Try as a long
			try {
				long val = Long.parseLong(args[0]);
				id = new UUID(0, val);
			} catch (NumberFormatException e2) {
				// Not an id?
			}
		}
		
		// Try and find it
		Collection<ItemAttributeModifier> modifiers = item.getAttributes().getModifiers(attribute);
		
		ItemAttributeModifier matching = null;
		
		for (ItemAttributeModifier modifier : modifiers) {
			if (id != null) {
				if (modifier.getUniqueId().equals(id)) {
					matching = modifier;
					break;
				}
			} else {
				if (modifier.getName().replace(' ', '_').equals(args[0])) {
					matching = modifier;
					break;
				}
			}
		}
		
		if (matching != null) {
			item.getAttributes().removeModifier(matching);
			player.sendMessage(ChatColor.GOLD + "Removed modifier \"" + matching.getName() + "\"");
		} else {
			player.sendMessage(ChatColor.RED + "Unable to find matching modifier " + args[0]);
		}
	}
	
	private void handleClearModifiers(Player player, MonoItemStack item, Attribute attribute) {
		item.getAttributes().clearModifiers(attribute);
		
		player.sendMessage(ChatColor.GOLD + "Cleared modifiers for " + Attributes.getId(attribute));
	}
	
	private void handleSetModifier(Player player, MonoItemStack item, Attribute attribute, String label, String[] args) {
		if (args.length < 2) {
			player.sendMessage(ChatColor.RED + "You have not supplied enough arguments to set a modifier:");
			player.sendMessage("Usage: /" + label + " " + Attributes.getId(attribute) + " set <name|uuid> value <value>");
			player.sendMessage("Usage: /" + label + " " + Attributes.getId(attribute) + " set <name|uuid> operation <operation>");
			player.sendMessage("Usage: /" + label + " " + Attributes.getId(attribute) + " set <name|uuid> slot <slot>");
			player.sendMessage("Usage: /" + label + " " + Attributes.getId(attribute) + " set <name|uuid> name <name>");
			return;
		}
		
		label += " " + Attributes.getId(attribute);
		
		// Try as UUID first
		UUID id = null;
		try {
			id = UUID.fromString(args[0]);
		} catch (IllegalArgumentException e) {
			// Try as a long
			try {
				long val = Long.parseLong(args[0]);
				id = new UUID(0, val);
			} catch (NumberFormatException e2) {
				// Not an id?
			}
		}
		
		// Try and find it
		Collection<ItemAttributeModifier> modifiers = item.getAttributes().getModifiers(attribute);
		
		ItemAttributeModifier matching = null;
		
		for (ItemAttributeModifier modifier : modifiers) {
			if (id != null) {
				if (modifier.getUniqueId().equals(id)) {
					matching = modifier;
					break;
				}
			} else {
				if (modifier.getName().replace(' ', '_').equals(args[0])) {
					matching = modifier;
					break;
				}
			}
		}
		
		if (matching == null) {
			player.sendMessage(ChatColor.RED + "Could not find modifier \"" + args[0] + "\"");
			return;
		}
		
		if (args[1].equalsIgnoreCase("value")) {
			handleSetValue(player, item, attribute, matching, label, Arrays.copyOfRange(args, 2, args.length));
		} else if (args[1].equalsIgnoreCase("operation")) {
			handleSetOperation(player, item, attribute, matching, label, Arrays.copyOfRange(args, 2, args.length));
		} else if (args[1].equalsIgnoreCase("slot")) {
			handleSetSlot(player, item, attribute, matching, label, Arrays.copyOfRange(args, 2, args.length));
		} else if (args[1].equalsIgnoreCase("name")) {
			handleSetName(player, item, attribute, matching, label, Arrays.copyOfRange(args, 2, args.length));
		} else {
			player.sendMessage(ChatColor.RED + "Unknown action " + args[1]);
			return;
		}
	}
	
	public void handleSetValue(Player player, MonoItemStack item, Attribute attribute, ItemAttributeModifier modifier, String label, String[] args) {
		if (args.length < 1) {
			player.sendMessage(ChatColor.RED + "You have not supplied enough arguments to set a modifier:");
			player.sendMessage("Usage: /" + label + " set <name|uuid> value <value>");
			return;
		}
		
		// Parse value
		double value;
		try {
			value = Double.parseDouble(args[0]);
		} catch (NumberFormatException e) {
			player.sendMessage(ChatColor.RED + "You have supplied an incorrect value for <value>. You provided " + args[0] + ", expected a number");
			player.sendMessage("Usage: /" + label + " set <name|uuid> value <value>");
			return;
		}
		
		ItemAttributeModifier newModifier = new ItemAttributeModifier(
			modifier.getUniqueId(), 
			modifier.getName(), 
			value, 
			modifier.getOperation(), 
			modifier.getSlot()
		);
		
		// Replace it
		item.getAttributes().removeModifier(modifier);
		item.getAttributes().addModifier(attribute, newModifier);
		
		player.sendMessage(ChatColor.GREEN + "Set the value to " + args[0]);
	}
	
	public void handleSetOperation(Player player, MonoItemStack item, Attribute attribute, ItemAttributeModifier modifier, String label, String[] args) {
		if (args.length < 1) {
			player.sendMessage(ChatColor.RED + "You have not supplied enough arguments to set a modifier:");
			player.sendMessage("Usage: /" + label + " set <name|uuid> operation <operation>");
			return;
		}
		
		// Parse operation
		Operation operation;
		switch (args[0].toLowerCase()) {
		case "add":
			operation = Operation.ADD_NUMBER;
			break;
		case "add_scale":
			operation = Operation.ADD_SCALAR;
			break;
		case "scale":
			operation = Operation.MULTIPLY_SCALAR_1;
			break;
		default:
			// Try as a number
			try {
				int op = Integer.parseInt(args[0]);
				if (op < 0 || op > 2) {
					player.sendMessage(ChatColor.RED + "You have supplied an incorrect value for <operation>. You provided " + args[0] + ", expected \"add\", \"add_scale\", \"scale\" or 0-2");
					player.sendMessage("Usage: /" + label + " set <name|uuid> operation <operation>");
					return;
				}
				
				operation = Operation.values()[op];
			} catch (NumberFormatException e) {
				player.sendMessage(ChatColor.RED + "You have supplied an incorrect value for <operation>. You provided " + args[0] + ", expected \"add\", \"add_scale\", \"scale\" or 0-2");
				player.sendMessage("Usage: /" + label + " set <name|uuid> operation <operation>");
				return;
			}
		}
		
		ItemAttributeModifier newModifier = new ItemAttributeModifier(
			modifier.getUniqueId(), 
			modifier.getName(), 
			modifier.getAmount(), 
			operation, 
			modifier.getSlot()
		);
		
		// Replace it
		item.getAttributes().removeModifier(modifier);
		item.getAttributes().addModifier(attribute, newModifier);
		
		player.sendMessage(ChatColor.GREEN + "Set the operation to " + operation.name().toLowerCase());
	}
	
	public void handleSetSlot(Player player, MonoItemStack item, Attribute attribute, ItemAttributeModifier modifier, String label, String[] args) {
		if (args.length < 1) {
			player.sendMessage(ChatColor.RED + "You have not supplied enough arguments to set a modifier:");
			player.sendMessage("Usage: /" + label + " set <name|uuid> slot <slot>");
			return;
		}
		
		// Parse slot
		EquipmentSlot slot;
		switch (args[0].toLowerCase()) {
		case "head":
			slot = EquipmentSlot.HEAD;
			break;
		case "chest":
			slot = EquipmentSlot.CHEST;
			break;
		case "legs":
			slot = EquipmentSlot.LEGS;
			break;
		case "feet":
			slot = EquipmentSlot.FEET;
			break;
		case "hand":
		case "mainhand":
			slot = EquipmentSlot.HAND;
			break;
		case "offhand":
		case "off_hand":
			slot = EquipmentSlot.OFF_HAND;
			break;
		case "all":
		case "any":
			slot = null;
			break;
		default:
			player.sendMessage(ChatColor.RED + "You have supplied an incorrect value for <slot>. You provided " + args[0] + ", expected \"head\", \"chest\", \"legs\", \"feet\", \"hand\", \"offhand\", \"any\"");
			player.sendMessage("Usage: /" + label + " set <name|uuid> slot <slot>");
			return;
		}
		
		ItemAttributeModifier newModifier = new ItemAttributeModifier(
			modifier.getUniqueId(), 
			modifier.getName(), 
			modifier.getAmount(), 
			modifier.getOperation(), 
			slot
		);
		
		// Replace it
		item.getAttributes().removeModifier(modifier);
		item.getAttributes().addModifier(attribute, newModifier);

		player.sendMessage(ChatColor.GREEN + "Set the slot to " + args[0]);
	}
	
	public void handleSetName(Player player, MonoItemStack item, Attribute attribute, ItemAttributeModifier modifier, String label, String[] args) {
		if (args.length < 1) {
			player.sendMessage(ChatColor.RED + "You have not supplied enough arguments to set a modifier:");
			player.sendMessage("Usage: /" + label + " set <name|uuid> name <name>");
			return;
		}
		
		String name = args[0];
		
		ItemAttributeModifier newModifier = new ItemAttributeModifier(
			modifier.getUniqueId(), 
			name, 
			modifier.getAmount(), 
			modifier.getOperation(), 
			modifier.getSlot()
		);
		
		// Replace it
		item.getAttributes().removeModifier(modifier);
		item.getAttributes().addModifier(attribute, newModifier);
		
		player.sendMessage(ChatColor.GREEN + "Renamed the modifier to " + name);
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (!(sender instanceof Player)) {
			return null;
		}
		
		Player player = (Player)sender;
		ItemStack held = player.getInventory().getItemInMainHand();
		if (held == null || held.getType() == Material.AIR) {
			return null;
		}
		
		MonoItemStack item = new MonoItemStack(held);
		
		List<String> source = Lists.newArrayList();
		if (args.length == 1) {
			source.add("list");
			source.add("reset");
			
			// Attribute names
			for (Attribute attribute : Attribute.values()) {
				source.add(Attributes.getId(attribute));
			}
		} else {
			// Further commands need the attribute
			Attribute attribute = Attributes.fromId(args[0]);
			if (attribute == null) {
				return null;
			}
			
			if (args.length == 2) {
				source.add("add");
				source.add("remove");
				source.add("clear");
				source.add("set");
			} else if (args[1].equalsIgnoreCase("remove")) {
				if (args.length == 3) {
					buildModifierList(source, item.getAttributes().getModifiers(attribute));
				}
			} else if (args[1].equalsIgnoreCase("add")) {
				if (args.length == 5 || args.length == 6) {
					source.add("add");
					source.add("add_scale");
					source.add("scale");
				}
				
				if (args.length == 6 || args.length == 7) {
					source.add("head");
					source.add("chest");
					source.add("legs");
					source.add("feet");
					source.add("hand");
					source.add("mainhand");
					source.add("offhand");
				}
			} else if (args[1].equalsIgnoreCase("set")) {
				if (args.length == 4) {
					source.add("value");
					source.add("slot");
					source.add("operation");
					source.add("name");
				} else if (args.length == 5) {
					if (args[3].equalsIgnoreCase("slot")) {
						source.add("head");
						source.add("chest");
						source.add("legs");
						source.add("feet");
						source.add("hand");
						source.add("mainhand");
						source.add("offhand");
					} else if (args[3].equalsIgnoreCase("operation")) {
						source.add("add");
						source.add("add_scale");
						source.add("scale");
					}
				}
			}
		}
		
		// Remove non matching elements
		String input = args[args.length-1].toLowerCase();
		Iterator<String> it = source.iterator();
		while (it.hasNext()) {
			String value = it.next();
			if (!value.toLowerCase().startsWith(input)) {
				it.remove();
			}
		}
		
		return source;
	}
	
	private void buildModifierList(List<String> dest, Collection<ItemAttributeModifier> modifiers) {
		for (ItemAttributeModifier modifier : modifiers) {
			dest.add(modifier.getName().replace(' ', '_'));
		}
	}

}
