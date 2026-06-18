package cn.wekyjay.www.wkkit.edit.prompt;

import cn.handyplus.lib.adapter.HandySchedulerUtil;
import cn.wekyjay.www.wkkit.WkKit;
import cn.wekyjay.www.wkkit.kit.Kit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Folia 兼容的提示输入系统
 * 替代 Conversation API（在 Folia 中不可用）
 */
public class KitFlagPrompt implements Listener {
	
	// 存储等待输入的玩家会话
	private static final Map<UUID, InputSession> waitingSessions = new ConcurrentHashMap<>();
	
	/**
	 * 输入会话类
	 */
	private static class InputSession {
		private final String kitname;
		private final String flag;
		private final SessionType type;
		private final long startTime;
		
		public InputSession(String kitname, String flag, SessionType type) {
			this.kitname = kitname;
			this.flag = flag;
			this.type = type;
			this.startTime = System.currentTimeMillis();
		}
		
		public boolean isExpired() {
			return System.currentTimeMillis() - startTime > 60000; // 60秒超时
		}
	}
	
	private enum SessionType {
		SET_FLAG,
		DELETE_FLAG
	}
	
	// 添加Flag
	public static void setFlag(Player player, String kitname, String flag) {
		InputSession session = new InputSession(kitname, flag, SessionType.SET_FLAG);
		waitingSessions.put(player.getUniqueId(), session);
		player.sendMessage("§a正在修改§e" + kitname + "§a的§e" + flag + "§a ,请输入你要修改的值(输入 Cancel 取消):");
		
		// 60秒后自动清理
		HandySchedulerUtil.runTaskLaterAsynchronously(() -> {
			InputSession current = waitingSessions.get(player.getUniqueId());
			if (current != null && current.isExpired()) {
				waitingSessions.remove(player.getUniqueId());
				player.sendMessage("§c输入超时，已取消修改。");
			}
		}, 20L * 60);
	}
	
	// 删除Flag
	public static void deFlag(Player player, String kitname, String flag) {
		InputSession session = new InputSession(kitname, flag, SessionType.DELETE_FLAG);
		waitingSessions.put(player.getUniqueId(), session);
		player.sendMessage("§c你确定要删除§e" + kitname + "§c的§e" + flag + "§c吗？(输入 Y 确认 / N 取消)");
		
		// 60秒后自动清理
		HandySchedulerUtil.runTaskLaterAsynchronously(() -> {
			InputSession current = waitingSessions.get(player.getUniqueId());
			if (current != null && current.isExpired()) {
				waitingSessions.remove(player.getUniqueId());
				player.sendMessage("§c输入超时，已取消操作。");
			}
		}, 20L * 60);
	}
	
	/**
	 * 监听玩家聊天输入
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerChat(AsyncPlayerChatEvent e) {
		Player player = e.getPlayer();
		InputSession session = waitingSessions.get(player.getUniqueId());
		
		if (session == null) return; // 玩家不在输入会话中
		
		// 取消聊天事件，防止消息广播
		e.setCancelled(true);
		
		String input = e.getMessage();
		
		// 处理输入
		if (session.type == SessionType.SET_FLAG) {
			handleSetFlagInput(player, session, input);
		} else if (session.type == SessionType.DELETE_FLAG) {
			handleDeleteFlagInput(player, session, input);
		}
		
		// 清理会话
		waitingSessions.remove(player.getUniqueId());
	}
	
	/**
	 * 处理设置 Flag 的输入
	 */
	private void handleSetFlagInput(Player player, InputSession session, String input) {
		// 取消操作
		if (input.equalsIgnoreCase("Cancel")) {
			player.sendMessage("§e你取消了修改！");
			return;
		}
		
		String flag = session.flag;
		String kitname = session.kitname;
		
		// 验证输入
		if (flag.equals("Delay") && !input.matches("[0-9]+")) {
			player.sendMessage("§c输入无效！Delay 必须是数字。");
			return;
		}
		if (flag.equals("Times") && !input.matches("[0-9]+")) {
			player.sendMessage("§c输入无效！Times 必须是数字。");
			return;
		}
		if (flag.equals("Vault") && !input.matches("[0-9]+")) {
			player.sendMessage("§c输入无效！Vault 必须是数字。");
			return;
		}
		
		input = input.replace("&", "§");
		
		Kit kit = Kit.getKit(kitname);
		if (kit == null) {
			player.sendMessage("§c礼包不存在！");
			return;
		}
		
		// 应用修改
		try {
			switch(flag) {
				case "DisplayName": kit.setDisplayName(input); break;
				case "Icon": kit.setIcon(input); break;
				case "Permission": kit.setPermission(input); break;
				case "Times": kit.setTimes(Integer.parseInt(input)); break;
				case "Delay": kit.setDelay(Integer.parseInt(input)); break;
				case "DoCron": kit.setDocron(input); break;
				case "Lore": kit.setLore(Arrays.asList(input.split(","))); break;
				case "MythicMobs": kit.setMythicMobs(Arrays.asList(input.split(","))); break;
				case "Drop": kit.setDrop(Arrays.asList(input.split(","))); break;
				case "Commands": kit.setCommands(Arrays.asList(input.split(","))); break;
				case "Vault": kit.setVault(Integer.parseInt(input)); break;
				case "NoRefreshFirst": kit.setNoRefreshFirst(Boolean.parseBoolean(input)); break;
			}
			kit.saveConfig();
			player.sendMessage("§a修改成功！");
		} catch (Exception ex) {
			player.sendMessage("§c修改失败：" + ex.getMessage());
		}
	}
	
	/**
	 * 处理删除 Flag 的输入
	 */
	private void handleDeleteFlagInput(Player player, InputSession session, String input) {
		if (!input.equalsIgnoreCase("Y") && !input.equalsIgnoreCase("N")) {
			player.sendMessage("§c输入无效！请输入 Y 或 N。");
			return;
		}
		
		if (input.equalsIgnoreCase("N")) {
			player.sendMessage("§e你取消了删除！");
			return;
		}
		
		String flag = session.flag;
		String kitname = session.kitname;
		
		Kit kit = Kit.getKit(kitname);
		if (kit == null) {
			player.sendMessage("§c礼包不存在！");
			return;
		}
		
		// 删除 Flag
		try {
			switch(flag) {
				case "DisplayName": kit.setDisplayName(WkKit.getWkKit().getConfig().getString("Default.Name")); break;
				case "Icon": kit.setIcon(WkKit.getWkKit().getConfig().getString("Default.Icon")); break;
				case "Permission": kit.setPermission(null); break;
				case "Times": kit.setTimes(null); break;
				case "Delay": kit.setDelay(null); break;
				case "DoCron": kit.setDocron(null); break;
				case "Lore": kit.setLore(null); break;
				case "MythicMobs": kit.setMythicMobs(null); break;
				case "Drop": kit.setDrop(null); break;
				case "Commands": kit.setCommands(null); break;
			}
			kit.saveConfig();
			player.sendMessage("§a删除成功！");
		} catch (Exception ex) {
			player.sendMessage("§c删除失败：" + ex.getMessage());
		}
	}
	
	/**
	 * 玩家退出时清理会话
	 */
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		waitingSessions.remove(e.getPlayer().getUniqueId());
	}
}