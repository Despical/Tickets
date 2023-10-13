package me.despical.tickets.ticket;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.util.Strings;
import me.despical.tickets.Main;

import java.util.ArrayList;
import java.util.List;

public class TicketManager {

    private final Main plugin;
    private final int expirationTime;
    private final List<Ticket> tickets;

    public TicketManager(Main plugin) {
        this.plugin = plugin;
        this.expirationTime = plugin.getConfig().getInt("expiration-time");
        this.tickets = new ArrayList<>();
        this.loadTickets();
    }

    public void addTicket(Ticket ticket) {
        tickets.add(ticket);
    }

    public List<Ticket> getTickets() {
        return tickets;
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

    public int getAvailableId() {
        var config = ConfigUtils.getConfig(plugin, "tickets");
        var section = config.getConfigurationSection("tickets");

        if (section == null) return 1;

        var ticketList = section.getKeys(false).stream().map(Integer::parseInt).sorted().toList();

        return ticketList.get(ticketList.size() - 1) + 1;
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
            var number = config.getInt(path + "number");
            var id = Integer.parseInt(ticketName);
            var ticket = new Ticket(uuid, message, creationDate, number, id);

            ticket.setClosed(config.getBoolean(path + "closed"));
            ticket.setClosingTime(config.getLong(path + "closingDate"));
            ticket.setReply(config.getStringList(path + "replies"));
            this.tickets.add(ticket);
        }
    }
}