package me.despical.tickets;

import me.despical.commandframework.CommandFramework;
import me.despical.tickets.commands.MainCommands;
import me.despical.tickets.ticket.TicketManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Main extends JavaPlugin {

	private CommandFramework commandFramework;
	private TicketManager ticketManager;

	@Override
	public void onEnable() {
		setupFiles();

		commandFramework = new CommandFramework(this);
		ticketManager = new TicketManager(this);

		new MainCommands(this);
		new OpenTicketPlaceholders(this);
		new ClosedTicketPlaceholders(this);
	}

	private void setupFiles() {
		this.saveDefaultConfig();

		if (!new File(getDataFolder(), "tickets.yml").exists()) {
			saveResource("tickets.yml", false);
		}
	}

	public CommandFramework getCommandFramework() {
		return commandFramework;
	}

	public TicketManager getTicketManager() {
		return ticketManager;
	}
}