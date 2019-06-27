package com.github.qianniancc.residenceprefix;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import java.io.File;
import java.io.IOException;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
	public Player p;

	public void onEnable() {
		if (!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}
		File file = new File(getDataFolder(), "config.yml");
		if (!file.exists())
			saveDefaultConfig();
		getLogger().info("ResidencePrefix作者浅念！");
		reloadConfig();
		getServer().getPluginManager().registerEvents(this, this);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		try {
			p = (Player) sender;

		} catch (Exception e) {

			getLogger().info("这些指令需要在游戏中执行");
			return true;

		}

		Location loc = p.getLocation();
		ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(loc);
		ResidenceManager resMan = Residence.getInstance().getResidenceManager();
		File file = new File(getDataFolder(), "config.yml");
		if (cmd.getName().equalsIgnoreCase("rpreload")) {
			if ((!sender.isOp()) && (!sender.hasPermission("rp.reload")) && (!sender.hasPermission("rp.*"))) {
				sender.sendMessage("§c§l你没有权限");
				return false;
			}
			reloadConfig();
			sender.sendMessage("§a§l重置成功");
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("rplist")) {
			if ((!sender.isOp()) && (!sender.hasPermission("rp.list")) && (!sender.hasPermission("rp.*"))) {
				sender.sendMessage("§c§l你没有权限");
				return false;
			}
			if (args.length > 0) {
				Player isPlayer = getServer().getPlayer(args[0]);
				if (isPlayer != null) {
					p = isPlayer;
				}
			}
			int hasPrefixCount = 0;
			for (int i = 0; i < resMan.getOwnedZoneCount(p.getName()); i++) {
				String nowRes = (String) resMan.getResidenceList(p.getName(), true, true).get(i);
				String nowResPrefix = getConfig().getString(nowRes);

				if ((nowResPrefix != null) && (!nowResPrefix.isEmpty())) {
					nowResPrefix = ChatColor.translateAlternateColorCodes('&', nowResPrefix);
					sender.sendMessage("§e§l" + i + ":" + nowRes + "---[" + nowResPrefix + "§e§l]");
					hasPrefixCount++;
				} else {
					sender.sendMessage("§e§l" + i + ":" + nowRes + "---" + "无领地前缀");
				}
			}
			sender.sendMessage("§a§l我拥有" + resMan.getOwnedZoneCount(p.getName()) + "块领地");

			sender.sendMessage("§a§l拥有前缀" + hasPrefixCount + "块领地");
			int noSuffixCount = resMan.getOwnedZoneCount(p.getName()) - hasPrefixCount;
			sender.sendMessage("§a§l没有前缀" + noSuffixCount + "块领地");
			return true;
		}

		if (cmd.getName().equalsIgnoreCase("rpunset")) {
			if ((!sender.isOp()) && (!sender.hasPermission("rp.unset")) && (!sender.hasPermission("rp.*"))) {
				sender.sendMessage("§c§l你没有权限");
				return false;
			}
			if (res != null) {
				if ((res.getOwner() == p.getName()) || (p.isOp()) || (p.hasPermission("rp.admin"))
						|| (sender.hasPermission("rp.*"))) {
					getConfig().set(res.getName(), "");
					saveConfig();
					try {
						getConfig().save(file);
					} catch (IOException e) {
						sender.sendMessage(e.toString());
					}
					sender.sendMessage("§a§l清除领地前缀成功");
				} else {
					sender.sendMessage("§c§l这不是你的领地");
				}
			} else
				sender.sendMessage("§c§l你不在一个领地内");

			return true;
		}
		if (cmd.getName().equalsIgnoreCase("rpset")) {
			if ((!sender.isOp()) && (!sender.hasPermission("rp.set")) && (!sender.hasPermission("rp.*"))) {
				sender.sendMessage("§c§l你没有权限");
				return false;
			}
			if (args.length > 0) {
				if (res != null) {
					if ((res.getOwner() == p.getName()) || (p.isOp()) || (p.hasPermission("rp.admin"))
							|| (sender.hasPermission("rp.*"))) {
						String prefixArg = args[0];
						prefixArg = ChatColor.translateAlternateColorCodes('&', prefixArg);
						if (prefixArg.length() > getConfig().getInt("SettingLength")) {
							sender.sendMessage("§c§l你的领地前缀太长啦，尝试缩短些");
						} else {
							getConfig().set(res.getName(), prefixArg);
							saveConfig();
							try {
								getConfig().save(file);
							} catch (IOException e) {
								sender.sendMessage(e.toString());
							}
							sender.sendMessage("§a§l成功将当前领地前缀设置为§e§l" + prefixArg);
						}
					} else {
						sender.sendMessage("§c§l这不是你的领地");
					}
				} else
					sender.sendMessage("§c§l你不在一个领地内");

				return true;
			}
			sender.sendMessage("§e§l请使用/rpset <领地前缀>");
		}

		return false;
	}

	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		Location loc = e.getPlayer().getLocation();
		ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(loc);
		if (res != null) {
			if ((getConfig().getString(res.getName()) != null) && (getConfig().getString(res.getName()) != "")) {
				String prefix = getConfig().getString(res.getName());
				prefix = ChatColor.translateAlternateColorCodes('&', prefix);
				if (getConfig().getBoolean("TogglePrefix")) {
					e.setFormat(prefix + "§r" + e.getFormat());
				}
				if (getConfig().getBoolean("ToggleSuffix"))
					e.setFormat("§r" + e.getFormat() + prefix);
			} else {
				String DefaultPrefix = getConfig().getString("DefaultPrefix");
				DefaultPrefix = ChatColor.translateAlternateColorCodes('&', DefaultPrefix);
				if (getConfig().getBoolean("TogglePrefix")) {
					e.setFormat(DefaultPrefix + "§r" + e.getFormat());
				}
				if (getConfig().getBoolean("ToggleSuffix"))
					e.setFormat("§r" + e.getFormat() + DefaultPrefix);
			}
		}
	}
}