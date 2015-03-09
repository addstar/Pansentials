package au.com.addstar.pansentials;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;

public abstract class CommandModule extends AbstractModule implements CommandExecutor, TabCompleter
{
	private String[] mCommands;
	
	public CommandModule(String... commands)
	{
		mCommands = commands;
	}
	
	@Override
	public void onEnable()
	{
		for (String name : mCommands)
		{
			PluginCommand cmd = getPlugin().getCommand(name);
			if (cmd != null)
			{
				cmd.setExecutor(this);
				cmd.setTabCompleter(this);
			}
		}
	}
	
	@Override
	public void onDisable()
	{
		for (String name : mCommands)
		{
			PluginCommand cmd = getPlugin().getCommand(name);
			if (cmd != null)
			{
				cmd.setExecutor(null);
				cmd.setTabCompleter(null);
			}
		}
	}
}
