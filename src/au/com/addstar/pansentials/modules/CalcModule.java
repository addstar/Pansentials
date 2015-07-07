package au.com.addstar.pansentials.modules;

import java.util.List;

import net.md_5.bungee.api.ChatColor;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import au.com.addstar.pansentials.CommandModule;

public class CalcModule extends CommandModule {
	public CalcModule() {
		super("calc");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String fullEquation = StringUtils.join(args);
		
		try {
			ExpressionBuilder builder = new ExpressionBuilder(fullEquation);
			Expression expr = builder.build();
			
			double value = expr.evaluate();
			sender.sendMessage(ChatColor.GRAY + fullEquation + " = " + ChatColor.WHITE + value);
		} catch (IllegalArgumentException e) {
			sender.sendMessage(ChatColor.RED + "Invalid equation");
		} catch (ArithmeticException e) {
			sender.sendMessage(ChatColor.RED + "Invalid equation");
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		return null;
	}

}
