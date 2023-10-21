package me.despical.tickets.ticket;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.util.Strings;
import me.despical.tickets.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class TicketManager {

	private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	private final Main plugin;
	private final int expirationTime;
	private final List<Ticket> tickets, closedTickets;

	public TicketManager(Main plugin) {
		this.plugin = plugin;
		this.expirationTime = plugin.getConfig().getInt("expiration-time");
		this.tickets = new ArrayList<>();
		this.closedTickets = new ArrayList<>();
		this.loadTickets();
	}

	public void addTicket(Ticket ticket) {
		tickets.add(ticket);
	}

	public void removeTicketAndDownshift(Ticket ticket) {
		if (!tickets.remove(ticket)) return;

		var config = ConfigUtils.getConfig(plugin, "tickets");
		ticket.setNumber(getNextClosedTicketNumber());
		closedTickets.add(ticket);

		config.set("tickets.%d.number".formatted(ticket.getId()), ticket.getNumber());

		for (int i = 0; i < closedTickets.size() / 2; i++) {
			var indexTicket = closedTickets.get(i);
			var j = indexTicket.getNumber();

			indexTicket.setNumber(closedTickets.get(closedTickets.size() - i - 1).getNumber());

			var swappedTicket = closedTickets.get(closedTickets.size() - i - 1);
			swappedTicket.setNumber(j);

			config.set("tickets.%d.number".formatted(indexTicket.getId()), indexTicket.getNumber());
			config.set("tickets.%d.number".formatted(swappedTicket.getId()), j);
		}

		int missingNumber = findMissingNumber();

		for (var entryTicket : this.tickets) {
			if (missingNumber == -1) break;
			if (entryTicket.getNumber() == 1) continue;
			if (entryTicket.getNumber() < missingNumber) continue;

			missingNumber = findMissingNumber();

			var number = entryTicket.getNumber() - 1;

			config.set("tickets.%d.number".formatted(entryTicket.getId()), number);

			entryTicket.setNumber(number);
		}

		ConfigUtils.saveConfig(plugin, config, "tickets");
	}

	public List<Ticket> getTickets() {
		var allTickets = new ArrayList<>(this.tickets);
		allTickets.addAll(this.closedTickets);

		return allTickets;
	}

	public List<Ticket> getClosedTickets() {
		return closedTickets;
	}

	public List<Ticket> getOpenedTickets() {
		return tickets;
	}

	public Ticket getClosedTicketFromId(int id) {
		return closedTickets.stream().filter(ticket -> ticket.getNumber() == id).findFirst().orElse(null);
	}

	public Ticket getTicketFromId(int id) {
		return tickets.stream().filter(ticket -> ticket.getNumber() == id).findFirst().orElse(null);
	}

	public String getTicketStatus(Ticket ticket) {
		var config = plugin.getConfig();

		class ConfigReader {

			String read(String path) {
				return Strings.format(config.getString("ticket-status." + path));
			}
		}

		var reader = new ConfigReader();

		if (ticket.isClosed()) {
			return reader.read("closed");
		}

		var isNew = expirationTime >= (int) ((System.currentTimeMillis() - ticket.getCreationTime()) / 1000) % 60;

		return reader.read(isNew ? "new" : "open");
	}

	public void saveTicket(Ticket ticket) {
		var config = ConfigUtils.getConfig(plugin, "tickets");
		var path = "tickets.%d.".formatted(ticket.getId());

		config.set(path + "creationDate", ticket.getCreationTime());
		config.set(path + "message", ticket.getMessage());
		config.set(path + "uuid", ticket.getUUID().toString());
		config.set(path + "randomId", ticket.getRandomId());
		config.set(path + "number", ticket.getNumber());
		config.set(path + "closed", ticket.isClosed());
		config.set(path + "closingDate", ticket.getClosingTime());
		config.set(path + "replies", ticket.getReplies());

		ConfigUtils.saveConfig(plugin, config, "tickets");
	}

	public int getNextNumber() {
		if (tickets.isEmpty()) return 1;

		return tickets.get(tickets.size() - 1).getNumber() + 1;
	}

	public int getNextClosedTicketNumber() {
		if (closedTickets.isEmpty()) return 1;

		return closedTickets.get(closedTickets.size() - 1).getNumber() + 1;
	}

	public int getAvailableId() {
		var config = ConfigUtils.getConfig(plugin, "tickets");
		var section = config.getConfigurationSection("tickets");

		if (section == null) return 1;

		var ticketList = section.getKeys(false).stream().map(Integer::parseInt).sorted().toList();

		return ticketList.get(ticketList.size() - 1) + 1;
	}

	public String getRandomId() {
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i++ < 6;) {
			builder.append(CHARS.charAt(ThreadLocalRandom.current().nextInt(36)));
		}

		if (getTickets().stream().map(Ticket::getRandomId).toList().contains(builder.toString())) {
			return getRandomId();
		}

		return builder.toString();
	}

	private void loadTickets() {
		var config = ConfigUtils.getConfig(plugin, "tickets");
		var section = config.getConfigurationSection("tickets");

		if (section == null) return;

		for (var ticketName : section.getKeys(false)) {
			var path = "tickets.%s.".formatted(ticketName);
			var creationDate = config.getLong(path + "creationDate");
			var message = config.getString(path + "message");
			var uuid = config.getString(path + "uuid");
			var randomId = config.getString(path + "randomId");
			var number = config.getInt(path + "number");
			var id = Integer.parseInt(ticketName);
			var ticket = new Ticket(uuid, message, randomId, creationDate, number, id);

			ticket.setClosed(config.getBoolean(path + "closed"));
			ticket.setClosingTime(config.getLong(path + "closingDate"));
			ticket.setReply(config.getStringList(path + "replies"));

			if (ticket.isClosed()) {
				closedTickets.add(ticket);
				continue;
			}

			tickets.add(ticket);
		}
	}

	private int findMissingNumber() {
		var list = this.tickets.stream().map(Ticket::getNumber).toList();

		for (int i = 1; i <= list.size(); i++) {
			if (!list.contains(i)) return i;
		}

		return -1;
	}
}