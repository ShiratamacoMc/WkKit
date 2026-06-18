package cn.wekyjay.www.wkkit.edit.prompt;

import cn.handyplus.lib.adapter.HandySchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Folia 兼容的礼包组删除确认系统
 * 替代 Conversation API（在 Folia 中不可用）
 */
public class GroupPrompt implements Listener {
	
	// 存储等待删除确认的玩家
	private static final Map<UUID, GroupSession> waitingGroups = new ConcurrentHashMap<>();
	
	private static class GroupSession {
		private final String groupname;
		private final GroupStep step;
		private final long startTime;
		
		public GroupSession(String groupname, GroupStep step) {
			this.groupname = groupname;
			this.step = step;
			this.startTime = System.currentTimeMillis();
		}
		
		public boolean isExpired() {
			return System.currentTimeMillis() - startTime > 60000; // 60秒超时
		}
	}
	
	private enum GroupStep {
		CONFIRM_DELETE,      // 确认是否删除
		CONFIRM_WITH_KITS    // 确认是否同时删除礼包
	}
	
	public static void newConversation(Player player, String groupname) {
		GroupSession session = new GroupSession(groupname, GroupStep.CONFIRM_DELETE);
		waitingGroups.put(player.getUniqueId(), session);
		player.sendMessage("你是否要删除 " + groupname + "? (输入 Y 确认 / N 取消)");
		
		// 60秒后自动清理
		HandySchedulerUtil.runTaskLaterAsynchronously(() -> {
			GroupSession current = waitingGroups.get(player.getUniqueId());
			if (current != null && current.isExpired()) {
				waitingGroups.remove(player.getUniqueId());
				player.sendMessage("§c确认超时，已取消删除。");
			}
		}, 20L * 60);
	}
	
	/**
	 * 监听玩家聊天输入
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerChat(AsyncPlayerChatEvent e) {
		Player player = e.getPlayer();
		GroupSession session = waitingGroups.get(player.getUniqueId());
		
		if (session == null) return; // 玩家不在删除确认中
		
		// 取消聊天事件
		e.setCancelled(true);
		
		String input = e.getMessage();
		
		if (!input.equalsIgnoreCase("Y") && !input.equalsIgnoreCase("N")) {
			player.sendMessage("§c输入无效！请输入 Y 或 N。");
			return;
		}
		
		if (session.step == GroupStep.CONFIRM_DELETE) {
			if (input.equalsIgnoreCase("N")) {
				player.sendMessage("§c你取消了礼包组删除");
				waitingGroups.remove(player.getUniqueId());
				return;
			}
			
			// 进入下一步：询问是否同时删除礼包
			GroupSession nextSession = new GroupSession(session.groupname, GroupStep.CONFIRM_WITH_KITS);
			waitingGroups.put(player.getUniqueId(), nextSession);
			player.sendMessage("是否也删除礼包组内的礼包? (输入 Y 确认 / N 取消)");
			
			// 60秒后自动清理
			HandySchedulerUtil.runTaskLaterAsynchronously(() -> {
				GroupSession current = waitingGroups.get(player.getUniqueId());
				if (current != null && current.isExpired()) {
					waitingGroups.remove(player.getUniqueId());
					player.sendMessage("§c确认超时，已取消删除。");
				}
			}, 20L * 60);
			
		} else if (session.step == GroupStep.CONFIRM_WITH_KITS) {
			// 执行删除命令
			if (input.equalsIgnoreCase("N")) {
				Bukkit.dispatchCommand(player, "wk group remove " + session.groupname + " true");
			} else {
				Bukkit.dispatchCommand(player, "wk group delete " + session.groupname);
			}
			
			// 清理会话
			waitingGroups.remove(player.getUniqueId());
		}
	}
	
	/**
	 * 玩家退出时清理会话
	 */
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		waitingGroups.remove(e.getPlayer().getUniqueId());
	}
}
