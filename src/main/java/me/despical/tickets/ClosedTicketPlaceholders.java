package me.despical.tickets;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.despical.commons.util.Strings;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ClosedTicketPlaceholders extends PlaceholderExpansion {

	private final Main plugin;

	public ClosedTicketPlaceholders(Main plugin) {
		this.plugin = plugin;

		register();
	}

	@Override
	public boolean persist() {
		return true;
	}

	@NotNull
	@Override
	public String getIdentifier() {
		return "omcticketclosed";
	}

	@NotNull
	@Override
	public String getAuthor() {
		return "Despical";
	}

	@NotNull
	@Override
	public String getVersion() {
		return "1.0.0";
	}

	@Override
	public String onPlaceholderRequest(Player player, @NotNull String id) {
		if (player == null) return null;

		var split = id.split(":");
		var ticket = plugin.getTicketManager().getClosedTicketFromId(Integer.parseInt(split[0]));

		if (ticket == null || !ticket.isClosed()) return "";

		return switch (split[1].toLowerCase()) {
			case "ticketowner" -> plugin.getServer().getOfflinePlayer(ticket.getUUID()).getName();
			case "ticketstatus" -> plugin.getTicketManager().getTicketStatus(ticket);
			case "ticket" -> Strings.format(ticket.getMessage());
			case "id" -> ticket.getRandomId();
			case "mmddyyy" -> ticket.getClosingDate();
			case "closedcount" -> Integer.toString(plugin.getTicketManager().getClosedTickets().size());
			case "reply" -> {
				var replyId = Integer.parseInt(split[2]);

				if (split.length == 3) {

					yield Strings.format(replyId <= ticket.getReplies().size() ? ticket.getReplies().get(replyId - 1).split(":")[0] : plugin.getConfig().getString("commands.no-reply-with-that-id"));
				}

				var _3rd = split[3];

				if (_3rd.equalsIgnoreCase("name")) {
					yield Strings.format(replyId <= ticket.getReplies().size() ? ticket.getReplies().get(replyId - 1).split(":")[1] : plugin.getConfig().getString("commands.no-replier-with-that-id"));
				}

				yield null;
			}

			default -> null;
		};
	}
}