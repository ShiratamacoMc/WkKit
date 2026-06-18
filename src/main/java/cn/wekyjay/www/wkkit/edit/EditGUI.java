package cn.wekyjay.www.wkkit.edit;

import cn.wekyjay.www.wkkit.config.LangConfigLoader;
import cn.wekyjay.www.wkkit.invholder.GUISessionManager;
import cn.wekyjay.www.wkkit.invholder.GUISessionManager.GUIType;
import cn.wekyjay.www.wkkit.tool.WKTool;
import cn.wekyjay.www.wkkit.tool.items.Barrier;
import cn.wekyjay.www.wkkit.tool.items.GlassPane;
import cn.wekyjay.www.wkkit.tool.items.PlayerHead;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static org.bukkit.event.inventory.InventoryAction.NOTHING;
import static org.bukkit.event.inventory.InventoryAction.UNKNOWN;

public class EditGUI implements Listener{
	private String titile;
	private Inventory inv;
	private static EditGUI instance = null;
	

	private EditGUI() {
		titile = LangConfigLoader.getString("EDIT_TITLE");
		inv = Bukkit.createInventory(null, 3*9, titile);
		for(int i = 0; i <= 26; i++) {
			if(i == 10 || i == 12 || i == 14 || i == 16) {
				ItemStack is = GlassPane.LIGHT_BLUE.getItemStack();
				ItemMeta im = is.getItemMeta();
				im.setDisplayName(LangConfigLoader.getString("DO_NOT_TOUCH"));
				is.setItemMeta(im);
				inv.setItem(i, is);
				continue;
			}
			if(i == 11 || i == 13 || i == 15) {
				ItemStack is = Barrier.DEFAULT.getItemStack();
				ItemStack playerhead = new ItemStack(PlayerHead.PRESENT_RED.getItemStack());
				ItemMeta im = is.getItemMeta();
				im.setDisplayName(LangConfigLoader.getString("DEVELOPING"));
				is.setItemMeta(im);
				if(i == 11)  inv.setItem(i,is);
				im = playerhead.getItemMeta();
				im.setDisplayName(LangConfigLoader.getString("EDIT_KIT"));
				playerhead.setItemMeta(im);
				if(i == 13) inv.setItem(i,playerhead);
				if(i == 15) inv.setItem(i,is);
				continue;
			}
			ItemStack is = GlassPane.DEFAULT.getItemStack();
			ItemMeta im = is.getItemMeta();
			im.setDisplayName(LangConfigLoader.getString("DO_NOT_TOUCH"));
			is.setItemMeta(im);
			inv.setItem(i, is);
		}
	}
	
	public String getTitile() {
		return titile;
	}
	
	public static EditGUI getEditGUI() {//静态使用实例方法
		return instance == null ? instance = new EditGUI():instance;
	}
	
	/**
	 * 获得一个插件管理器
	 * @return
	 */
	public Inventory getEditInv() {
		return inv;
	}
	
	/**
	 * 打开编辑主界面（带会话注册）
	 * @param player 玩家
	 */
	public void openEditInv(Player player) {
		GUISessionManager.registerGUI(player, GUIType.EDIT_GUI);
		player.openInventory(inv);
	}

	@EventHandler
	public void editEvent(InventoryClickEvent e) {
		// 只处理玩家点击事件
		if (!(e.getWhoClicked() instanceof Player)) return;
		Player player = (Player) e.getWhoClicked();
		
		// 获取玩家的 GUI 会话（完全避免使用 getHolder()）
		if (!GUISessionManager.hasGUI(player, GUIType.EDIT_GUI)) return;
		
		// 只处理顶部 inventory 的点击
		if (e.getClickedInventory() == null || !e.getClickedInventory().equals(e.getInventory())) {
			return;
		}
		
		e.setCancelled(true);
		if(e.getAction().equals(NOTHING) || e.getAction().equals(UNKNOWN)) {
	        return;
		}
		if(e.getRawSlot() == 13) {
			new EditKit().openInventory(player);
			return;
		}
	}

	
}

