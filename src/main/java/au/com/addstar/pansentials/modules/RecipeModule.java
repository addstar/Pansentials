package au.com.addstar.pansentials.modules;

import au.com.addstar.monolith.StringTranslator;
import au.com.addstar.monolith.lookup.MaterialDefinition;
import au.com.addstar.monolith.util.Parser;
import au.com.addstar.pansentials.CommandModule;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.*;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Map;

class RecipeModule extends CommandModule implements Listener
{
	private static final short WILDCARD = 32767;
    private final Map<Player, RecipeDisplay> mOpenInventories;
	
	
	public RecipeModule()
	{
		super("recipe");
		mOpenInventories = Maps.newHashMap();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (!(sender instanceof Player))
		{
			sender.sendMessage(ChatColor.RED + "Only players may use this");
			return true;
		}
		
		MaterialDefinition def;
		// Try item in hand
		if (args.length == 0)
		{

			Player player = (Player) sender;

			if (player.getInventory().getItemInMainHand() == null || player.getInventory().getItemInMainHand().getType() == Material.AIR) {
				sender.sendMessage(ChatColor.RED + "You are not holding an item, either hold one, or use /" + label + " <item>");
				return true;
			}

			def = MaterialDefinition.from(player.getInventory().getItemInMainHand());

		}
		// One is specified
		else
		{
			try
			{
				def = Parser.parseMaterialDefinition(args[0]);
				
				if (def == null)
				{
					sender.sendMessage(ChatColor.RED + "Unknown item " + args[0]);
					return true;
				}
			}
			catch (IllegalArgumentException e)
			{
				sender.sendMessage(ChatColor.RED + "Unknown item " + args[0]);
				return true;
			}
		}
		
		ItemStack item = def.asItemStack(1);
		List<Recipe> recipes = Bukkit.getRecipesFor(item);
		
		if (recipes.isEmpty())
		{
			sender.sendMessage(ChatColor.RED + StringTranslator.getName(item) + ChatColor.GOLD + " is not craftable");
			return true;
		}
		
		Player player = (Player)sender;
		RecipeDisplay display = new RecipeDisplay(recipes, player);
		display.showCurrent();
		
		mOpenInventories.put((Player)sender, display);
		
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args)
	{
		return null;
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	private void onInventoryClick(InventoryClickEvent event)
	{
		RecipeDisplay display = mOpenInventories.get(event.getWhoClicked());
		if (display == null)
			return;
		
		if (event.getClickedInventory() == display.inventory)
			event.setCancelled(true);
		else if (event.getAction() == InventoryAction.COLLECT_TO_CURSOR)
			event.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	private void onInventoryDrag(InventoryDragEvent event)
	{
		RecipeDisplay display = mOpenInventories.get(event.getWhoClicked());
		if (display == null)
			return;
		
		event.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	private void onInventoryClose(InventoryCloseEvent event)
	{
		RecipeDisplay display = mOpenInventories.get(event.getPlayer());
		if (display != null && !display.isChanging)
		{
			mOpenInventories.remove(event.getPlayer());
			display.close();
		}
	}
	
	@EventHandler
	private void onPlayerLeave(PlayerQuitEvent event)
	{
		RecipeDisplay display = mOpenInventories.remove(event.getPlayer());
		if (display != null)
			display.close();
	}
	
	private class RecipeDisplay
	{
        private final List<Recipe> mRecipes;
		private int mIndex;
        final Player player;
        Inventory inventory;
        boolean isChanging = false;
		private BukkitTask mChangeTask;

        RecipeDisplay(List<Recipe> recipes, Player player)
		{
			mRecipes = recipes;
			mIndex = 0;
			this.player = player;
			
			if (recipes.size() > 1)
			{
                mChangeTask = Bukkit.getScheduler().runTaskTimer(getPlugin(), this::next, 60, 60);
			}
		}

        void next()
		{
			++mIndex;
			if (mIndex >= mRecipes.size())
				mIndex = 0;
			isChanging = true;
			showCurrent();
			isChanging = false;
		}

        void showCurrent()
		{
			Inventory inv;
			Recipe raw = mRecipes.get(mIndex);
			
			if (raw instanceof ShapedRecipe)
			{
				inv = Bukkit.createInventory(null, InventoryType.WORKBENCH);
				ShapedRecipe recipe = (ShapedRecipe)raw;
				String[] shape = recipe.getShape();
				for (int line = 0; line < shape.length; ++line)
				{
					for (int c = 0; c < shape[line].length(); ++c)
					{
						Character ch = shape[line].charAt(c);
						ItemStack ingredient = recipe.getIngredientMap().get(ch);
						if (ingredient != null)
						{
							if (ingredient.getDurability() == WILDCARD)
							{
								ingredient = ingredient.clone();
								ingredient.setDurability((short)0);
							}
							
							int slot = line * 3 + c + 1;
							inv.setItem(slot, ingredient);
						}
					}
				}
				inv.setItem(0, recipe.getResult());
			}
			else if (raw instanceof ShapelessRecipe)
			{
				inv = Bukkit.createInventory(null, InventoryType.WORKBENCH);
				
				ShapelessRecipe recipe = (ShapelessRecipe)raw;
				int slot = 1;
				for (ItemStack ingredient : recipe.getIngredientList())
				{
					if (ingredient.getDurability() == WILDCARD)
					{
						ingredient = ingredient.clone();
						ingredient.setDurability((short)0);
					}
					
					inv.setItem(slot++, ingredient);
				}
				inv.setItem(0, recipe.getResult());
			}
			else if (raw instanceof FurnaceRecipe)
			{
				inv = Bukkit.createInventory(null, InventoryType.FURNACE);
				
				FurnaceRecipe recipe = (FurnaceRecipe)raw;
				ItemStack input = recipe.getInput();
				if (input.getDurability() == WILDCARD)
				{
					input = input.clone();
					input.setDurability((short)0);
				}
				
				inv.setItem(0, input);
				inv.setItem(2, recipe.getResult());
			}
			else
				return;
			
			// Show it
			player.openInventory(inv);
			inventory = inv;
		}

        void close()
		{
			if (mChangeTask != null)
				mChangeTask.cancel();
		}
	}
}
