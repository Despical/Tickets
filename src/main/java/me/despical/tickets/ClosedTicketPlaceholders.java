package me.despical.tickets;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.despical.commons.util.Strings;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ClosedTicketPlaceholders  extends PlaceholderExpansion {

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
        var ticket = plugin.getTicketManager().getTicketFromId(Integer.parseInt(split[0]));

        return switch (split[1].toLowerCase()) {
            case "ticketowner" -> plugin.getServer().getOfflinePlayer(ticket.getUUID()).getName();
            case "ticketstatus" -> plugin.getTicketManager().getTicketStatus(ticket);
            case "ticket" -> Strings.format(ticket.getMessage());
            case "mmddyyy" -> ticket.getClosingDate();
            case "reply" -> {
                var replyId = Integer.parseInt(split[2]);

                yield Strings.format(replyId <= ticket.getReplies().size() ? ticket.getReplies().get(replyId - 1) : plugin.getConfig().getString("commands.no-reply-with-that-id"));
            }

            default -> null;
        };
    }
}