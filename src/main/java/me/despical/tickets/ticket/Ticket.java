package me.despical.tickets.ticket;

import me.despical.tickets.Main;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Ticket {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);
	public static final DateFormat FORMATTER = new SimpleDateFormat(plugin.getConfig().getString("date-format"));

	private final String uuid, message, randomId;
	private final long creationDate;
	private final int id;

	private int number;
	private long closingTime;
	private boolean closed;
	private List<String> replies;

	public Ticket(Player owner, String message, String randomId, int number, int id) {
		this.uuid = owner.getUniqueId().toString();
		this.message = message;
		this.randomId = randomId;
		this.creationDate = System.currentTimeMillis();
		this.number = number;
		this.replies = new ArrayList<>();
		this.id = id;
	}

	public Ticket(String uuid, String message, String randomId, long creationDate, int number, int id) {
		this.uuid = uuid;
		this.message = message;
		this.randomId = randomId;
		this.creationDate = creationDate;
		this.number = number;
		this.id = id;
		this.replies = new ArrayList<>();
	}

	public String getRandomId() {
		return randomId;
	}

	public UUID getUUID() {
		return UUID.fromString(uuid);
	}

	public String getMessage() {
		return message;
	}

	public long getCreationTime() {
		return creationDate;
	}

	public String getCreationDate() {
		var date = new Date(creationDate);
		return FORMATTER.format(date);
	}

	public String getClosingDate() {
		var date = new Date(closingTime);
		return FORMATTER.format(date);
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public List<String> getReplies() {
		return replies;
	}

	public void setReply(List<String> replies) {
		this.replies = replies;
	}

	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	public int getId() {
		return id;
	}

	public long getClosingTime() {
		return closingTime;
	}

	public void setClosingTime(long closingTime) {
		this.closingTime = closingTime;
	}
}