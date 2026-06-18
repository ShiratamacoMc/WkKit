package cn.wekyjay.www.wkkit.listeners;

import cn.wekyjay.www.wkkit.WkKit;
import cn.wekyjay.www.wkkit.command.KitInfo;
import cn.wekyjay.www.wkkit.invholder.GUISessionManager;
import cn.wekyjay.www.wkkit.invholder.GUISessionManager.GUISession;
import cn.wekyjay.www.wkkit.invholder.GUISessionManager.GUIType;
import cn.wekyjay.www.wkkit.kit.Kit;
import cn.wekyjay.www.wkkit.kit.KitGetter;
import cn.wekyjay.www.wkkit.menu.MenuOpenner;
import cn.wekyjay.www.wkkit.tool.ItemEditer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.event.inventory.InventoryAction.NOTHING;
import static org.bukkit.event.inventory.InventoryAction.UNKNOWN;

public class KitMenuListener implements Listener{

	public static List<String> menutitles = new ArrayList<>();
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerClick(InventoryClickEvent e) {
		// 只处理玩家点击事件
		if (!(e.getWhoClicked() instanceof Player)) return;
		Player player = (Player) e.getWhoClicked();
		
		// 获取玩家的 GUI 会话（完全避免使用 getHolder()）
		GUISession session = GUISessionManager.getSession(player);
		if (session == null) return; // 不是自定义 GUI
		if (session.getType() != GUIType.KIT_MENU) return; // 不是礼包菜单
		
		// 只处理顶部 inventory 的点击
		if (e.getClickedInventory() == null || !e.getClickedInventory().equals(e.getInventory())) {
			return;
		}

		e.setCancelled(true);// 取消物品的拿取
		if(e.getAction().equals(NOTHING) || e.getAction().equals(UNKNOWN)) {
			return;
		}
		
		//如果点击是空格子就取消事件
		try {
			if(ItemEditer.hasWkKitTag(e.getCurrentItem())) {
				String kitname = ItemEditer.getWkKitTagValue(e.getCurrentItem());
				Kit kit = Kit.getKit(kitname);
				
				// 如果是右键则预览礼包
				if(e.getClick().isRightClick()) {
					new KitInfo().getKitInfo(kitname, player);
					return;
				}
				
				// 尝试领取礼包
				String menuname = session.getString("menuname");
				if (menuname != null) {
					new KitGetter().getKit(kit, player, menuname);
				}
			}
		} catch(NullPointerException e1) {
			return;
		}
		
		//领取物品后是否关闭GUI
		if(WkKit.getWkKit().getConfig().getBoolean("GUI.ClickClose")) {
			player.closeInventory();
		}else {
			String menuname = session.getString("menuname");
			if (menuname != null) {
				player.closeInventory();
				new MenuOpenner().openMenu(menuname, player);
			}
		}
	}
}
