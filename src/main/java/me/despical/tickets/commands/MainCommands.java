package me.despical.tickets.commands;

import me.despical.commandframework.Command;
import me.despical.commandframework.CommandArguments;
import me.despical.commandframework.Completer;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.util.Strings;
import me.despical.tickets.Main;
import me.despical.tickets.ticket.Ticket;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class MainCommands {

	private final Main plugin;

	public MainCommands(Main plugin) {
		this.plugin = plugin;
		this.plugin.getCommandFramework().registerCommands(this);
	}

	@Command(
		name = "omcticket",
		senderType = Command.SenderType.PLAYER
	)
	public void mainCommand(CommandArguments arguments) {
		sendMessage(arguments.getSender(), "commands.main-command-usage");
	}

	@Command(
		name = "omcticket.reload",
		permission = "omcticket.admin"
	)
	public void reloadCommand(CommandArguments arguments) {
		plugin.reloadConfig();

		arguments.sendMessage("Config reloaded.");
	}

	@Command(
		name = "omcticket.create",
		usage = "/omcticket create <message>",
		allowInfiniteArgs = true,
		senderType = Command.SenderType.PLAYER
	)
	public void createCommand(CommandArguments arguments) {
		Player player = arguments.getSender();

		if (arguments.isArgumentsEmpty()) {
			sendMessage(player, "commands.create-usage");
			return;
		}

		var ticketManager = plugin.getTicketManager();
		var ticket = new Ticket(player, String.join(" ", arguments.getArguments()), ticketManager.getRandomId(), ticketManager.getNextNumber(), ticketManager.getAvailableId());
		ticketManager.addTicket(ticket);
		ticketManager.saveTicket(ticket);

		sendMessage(player, "commands.created-ticket", ticket.getNumber());
	}

	@Command(
		name = "omcticket.reply",
		permission = "omcticket.admin",
		usage = "/omcticket reply <number> <message>",
		allowInfiniteArgs = true,
		min = 2,
		senderType = Command.SenderType.PLAYER
	)
	public void replyCommand(CommandArguments arguments) {
		Player player = arguments.getSender();

		var ticketNumber = arguments.getArgumentAsInt(0);
		var ticket = plugin.getTicketManager().getTicketFromId(ticketNumber);

		if (ticket == null) {
			sendMessage(player, "commands.no-ticket-found", ticketNumber);
			return;
		}

		var reply = String.join(" ", arguments.getArguments());
		reply = reply.concat(":" + player.getName());
		reply = reply.concat(":" + System.currentTimeMillis());
		ticket.getReplies().add(reply.substring(reply.indexOf(' ') + 1));

		var config = ConfigUtils.getConfig(plugin, "tickets");
		config.set("tickets.%d.replies".formatted(ticket.getId()), ticket.getReplies());
		ConfigUtils.saveConfig(plugin, config, "tickets");

		sendMessage(player, "commands.replied-to-ticket", ticket.getNumber());
	}

	@Command(
		name = "omcticket.close",
		permission = "omcticket.admin",
		usage = "/omcticket closeticket <number>",
		allowInfiniteArgs = true,
		min = 1,
		senderType = Command.SenderType.PLAYER
	)
	public void closeCommand(CommandArguments arguments) {
		Player player = arguments.getSender();

		var ticketNumber = arguments.getArgumentAsInt(0);
		var ticket = plugin.getTicketManager().getTicketFromId(ticketNumber);

		if (ticket == null) {
			sendMessage(player, "commands.no-ticket-found", ticketNumber);
			return;
		}

		ticket.setClosed(true);
		ticket.setClosingTime(System.currentTimeMillis());

		sendMessage(player, "commands.ticket-closed", ticket.getNumber());

		plugin.getTicketManager().removeTicketAndDownshift(ticket);

		var config = ConfigUtils.getConfig(plugin, "tickets");
		var path = "tickets.%d.".formatted(ticket.getId());
		config.set(path + "closed", true);
		config.set(path + "closingDate", ticket.getClosingTime());
		ConfigUtils.saveConfig(plugin, config, "tickets");
	}

	@Completer(
		name = "omcticket"
	)
	public List<String> tabCompleter(CommandArguments arguments) {
		if (!arguments.hasPermission("omcticket.admin")) return null;

		var arg = arguments.getArgument(0);

		if (arg == null) return null;

		if (arguments.getArgumentsLength() == 2) {
			if (arg.equalsIgnoreCase("reply"))
				return plugin.getTicketManager().getTickets().stream().map(ticket -> String.valueOf(ticket.getNumber())).toList();

			if (arg.equalsIgnoreCase("close"))
				return plugin.getTicketManager().getTickets().stream().filter(ticket -> !ticket.isClosed()).map(ticket -> String.valueOf(ticket.getNumber())).toList();
		}

		if (arguments.getArgumentsLength() == 1)
			return List.of("create", "reply", "close");

		return null;
	}

	private void sendMessage(CommandSender sender, String path, Object... objects) {
		var message = plugin.getConfig().getString(path);

		if (message.contains("%")) message = message.formatted(objects);

		sender.sendMessage(Strings.format(message));
	}
}