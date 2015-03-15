package au.com.addstar.pansentials.modules;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import au.com.addstar.monolith.lookup.EntityDefinition;
import au.com.addstar.monolith.lookup.Lookup;
import au.com.addstar.monolith.template.EntitySettings;
import au.com.addstar.monolith.template.EntityTemplate;
import au.com.addstar.monolith.template.internal.EntityTemplateSetting;
import au.com.addstar.monolith.util.Parser;
import au.com.addstar.monolith.util.Stringifier;
import au.com.addstar.pansentials.CommandModule;

public class SpawnMobModule extends CommandModule
{
	public SpawnMobModule()
	{
		super("spawnmob");
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private EntityTemplate createEntity(String definition)
	{
		String[] options = definition.split("\\|");
		
		EntityDefinition def = Lookup.findEntityByName(options[0]);
		if (def == null)
			throw new IllegalArgumentException("Unknown entity type " + options[0]);
		
		if (!def.isSpawnable())
			throw new IllegalArgumentException(options[0] + " is not spawnable");
		
		EntityTemplate template = new EntityTemplate(def);
		
		// Try options
		if (options.length > 1)
		{
			for (int i = 1; i < options.length; ++i)
			{
				String option = options[i];
				String optionName;
				String optionValue;
				
				if (option.contains("="))
				{
					String[] pair = option.split("=");
					optionName = pair[0];
					optionValue = pair[1];
				}
				else
				{
					optionName = option;
					optionValue = null;
				}
				
				boolean handled = false;
				for (EntityTemplateSetting setting : EntitySettings.values())
				{
					if (!setting.appliesTo(def))
						continue;
					
					for (String name : setting.getNames())
					{
						if (name.equalsIgnoreCase(optionName))
						{
							if (template.isSet(setting))
								throw new IllegalArgumentException(optionName + " is already set");
						}
						else
							continue;
						
						if (optionValue == null)
						{
							if (setting.getDefault() instanceof Boolean)
							{
								Boolean bool = (Boolean)setting.getDefault();
								template.set(setting, !bool);
								handled = true;
							}
							else
								throw new IllegalArgumentException("Option " + name + " requires an argument");
						}
						else
						{
							try
							{
								Object value = Parser.parse(setting.getType(), optionValue);
								template.set(setting, value);
								handled = true;
							}
							catch (UnsupportedOperationException e)
							{
								throw new IllegalArgumentException("Unable to set " + optionName);
							}
						}
					}
				}
				
				if (!handled)
					throw new IllegalArgumentException("Unknown setting " + optionName);
			}
		}
		
		return template;
	}
	
	@SuppressWarnings("rawtypes")
	private void doHelp(CommandSender sender, String entityType)
	{
		EntityDefinition def = Lookup.findEntityByName(entityType);
		if (def == null)
		{
			sender.sendMessage(ChatColor.RED + "Unknown entity type " + entityType);
			return;
		}
		
		sender.sendMessage(ChatColor.WHITE + "Availble options for " + ChatColor.YELLOW + entityType + ChatColor.GRAY + ":");
		for (EntityTemplateSetting setting : EntitySettings.values())
		{
			if (!setting.appliesTo(def))
				continue;
			
			StringBuilder builder = new StringBuilder();
			builder.append(' ');
			builder.append(ChatColor.YELLOW);
			builder.append(setting.getNames()[0]);
			
			builder.append(ChatColor.GRAY);
			builder.append(':');
			builder.append(ChatColor.GOLD);
			builder.append(setting.getType().getSimpleName());
			
			if (setting.getDefault() != null)
			{
				builder.append(ChatColor.GRAY);
				builder.append(" default=");
				builder.append(ChatColor.WHITE);
				builder.append(Stringifier.toString(setting.getDefault()));
			}
			
			if (setting.getNames().length > 1)
			{
				builder.append("\n  ");
				builder.append(ChatColor.GRAY);
				builder.append("aliases: ");
				builder.append(ChatColor.WHITE);
				builder.append(StringUtils.join(setting.getNames(), ", ", 1, setting.getNames().length));
			}
			
			sender.sendMessage(builder.toString());
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (!(sender instanceof Player))
		{
			sender.sendMessage("Only players can use this");
			return true;
		}
		
		if (args.length == 0)
			return false;
		
		if (args[0].equalsIgnoreCase("help"))
		{
			if (args.length < 2)
				sender.sendMessage("Usage /" + label + " help <entity>");
			else
				doHelp(sender, args[1]);
			
			return true;
		}
		
		int count = 1;
		int end = args.length;
		if (args[args.length-1].matches("[0-9]+"))
		{
			count = Integer.parseInt(args[args.length-1]);
			--end;
		}
		
		String line = StringUtils.join(args, ' ', 0, end);
		String[] mobs = line.split(",");
		
		Player player = (Player)sender;
		Location location = player.getLocation();
		
		try
		{
			List<EntityTemplate> templates = Lists.newArrayList();
			
			// Create the templates
			for (String mob : mobs)
			{
				if (mob.startsWith("^"))
				{
					mob = mob.substring(1);
					int index;
					if (mob.isEmpty())
						index = 1;
					else
					{
						try
						{
							index = Integer.parseInt(mob);
							if (index <= 0)
								throw new IllegalArgumentException("Illegal value '^" + mob + "'");
						}
						catch (NumberFormatException e)
						{
							throw new IllegalArgumentException("Illegal value '^" + mob + "'");
						}
					}
					
					if (index > templates.size())
						throw new IllegalArgumentException("Illegal value '^" + mob + "'. There is not enough definitions");
					templates.add(templates.get(templates.size()-index));
				}
				else
					templates.add(createEntity(mob));
			}
			
			// Spawn the entities
			int spawned = 0;
			for (int i = 0; i < count; ++i)
			{
				Entity last = null;
				for (EntityTemplate template : templates)
				{
					Entity ent = template.createAt(location);
					if (ent == null)
						break;
					
					++spawned;
					if (last != null)
						last.setPassenger(ent);
					last = ent;
				}
			}
			
			sender.sendMessage(ChatColor.GREEN + "Spawned " + spawned + " entities");
		}
		catch (IllegalArgumentException e)
		{
			sender.sendMessage(ChatColor.RED + e.getMessage());
		}
		
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args)
	{
		return null;
	}
}
