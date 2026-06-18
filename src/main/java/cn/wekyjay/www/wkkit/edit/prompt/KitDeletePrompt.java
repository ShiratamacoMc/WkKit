package cn.wekyjay.www.wkkit.edit.prompt;

import cn.handyplus.lib.adapter.HandySchedulerUtil;
import cn.handyplus.lib.adapter.PlayerSchedulerUtil;
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
 * Folia 兼容的礼包删除确认系统
 * 替代 Conversation API（在 Folia 中不可用）
 */
public class KitDeletePrompt implements Listener {
	
	// 存储等待删除确认的玩家
	private static final Map<UUID, DeleteSession> waitingDeletes = new ConcurrentHashMap<>();
	
	private static class DeleteSession {
		private final String kitname;
		private final long startTime;
		
		public DeleteSession(String kitname) {
			this.kitname = kitname;
			this.startTime = System.currentTimeMillis();
		}
		
		public boolean isExpired() {
			return System.currentTimeMillis() - startTime > 60000; // 60秒超时
		}
	}
	
	public static void newConversation(Player player, String kitname) {
		DeleteSession session = new DeleteSession(kitname);
		waitingDeletes.put(player.getUniqueId(), session);
		player.sendMessage("你是否要删除§e" + kitname + "§f? (输入 Y 确认 / N 取消)");
		
		// 60秒后自动清理
		HandySchedulerUtil.runTaskLaterAsynchronously(() -> {
			DeleteSession current = waitingDeletes.get(player.getUniqueId());
			if (current != null && current.isExpired()) {
				waitingDeletes.remove(player.getUniqueId());
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
		DeleteSession session = waitingDeletes.get(player.getUniqueId());
		
		if (session == null) return; // 玩家不在删除确认中
		
		// 取消聊天事件
		e.setCancelled(true);
		
		String input = e.getMessage();
		
		if (!input.equalsIgnoreCase("Y") && !input.equalsIgnoreCase("N")) {
			player.sendMessage("§c输入无效！请输入 Y 或 N。");
			return;
		}
		
		if (input.equalsIgnoreCase("Y")) {
			// 执行删除命令
			PlayerSchedulerUtil.performCommand(player, "wk delete " + session.kitname);
			player.sendMessage("§a已成功删除礼包 - " + session.kitname);
		} else {
			player.sendMessage("§c你取消了礼包删除");
		}
		
		// 清理会话
		waitingDeletes.remove(player.getUniqueId());
	}
	
	/**
	 * 玩家退出时清理会话
	 */
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		waitingDeletes.remove(e.getPlayer().getUniqueId());
	}
}