package cn.wekyjay.www.wkkit.edit;

import cn.wekyjay.www.wkkit.WkKit;
import cn.wekyjay.www.wkkit.config.LangConfigLoader;
import cn.wekyjay.www.wkkit.edit.prompt.KitDeletePrompt;
import cn.wekyjay.www.wkkit.edit.prompt.KitFlagPrompt;
import cn.wekyjay.www.wkkit.invholder.GUISessionManager;
import cn.wekyjay.www.wkkit.invholder.GUISessionManager.GUISession;
import cn.wekyjay.www.wkkit.invholder.GUISessionManager.GUIType;
import cn.wekyjay.www.wkkit.kit.Kit;
import cn.wekyjay.www.wkkit.kit.KitGroupManager;
import cn.wekyjay.www.wkkit.tool.ItemEditer;
import cn.wekyjay.www.wkkit.tool.WKTool;
import cn.wekyjay.www.wkkit.tool.items.GlassPane;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.bukkit.event.inventory.InventoryAction.NOTHING;
import static org.bukkit.event.inventory.InventoryAction.UNKNOWN;

public class EditKit implements Listener {
	private String editTitle;
	private Inventory[] kitGroupInvs; // 礼包组管理界面
	private List<ItemStack> kitGroupItemList;
	/**
	 * 礼包管理主页
	 */
	public EditKit() {
		int groupNum = KitGroupManager.getGroups().size();
		kitGroupItemList = new ArrayList<>();
		editTitle = LangConfigLoader.getString("EDIT_KIT_TITLE");
		//判断可创建的gui个数
		int guinum = 0;
		if (groupNum % 45 == 0 && groupNum != 0) guinum = groupNum / 45;
	    else guinum = groupNum / 45 + 1;
		
		kitGroupInvs = new Inventory[guinum];
		
		//添加物品到itemlist

		for(String kitGroupName : KitGroupManager.getGroups()) {
			ItemStack item = new ItemEditer(new ItemStack(Material.BOOK)).setDisplayName(kitGroupName).getItemStack();
			NBT.modify(item, nbti->{
			nbti.setString("wkkit", kitGroupName);
			});
			kitGroupItemList.add(item);
		}
		
		//创建gui到linv
		for(int i = 1; i <= guinum; i++) {
			Inventory inv;
			if(guinum == 1) {//如果只有一页就不加页数
				inv = Bukkit.createInventory(null, 6*9, editTitle);
			}else {
				String pagetitle = WKTool.replacePlaceholder("page", i+"", LangConfigLoader.getString("GUI_PAGETITLE"));
				inv = Bukkit.createInventory(null, 6*9, editTitle + " - " + pagetitle);
			}

			
			//添加物品：功能区
			ItemStack item_mn;
			if(WkKit.getWkKit().getConfig().getString("GUI.MenuMaterial").equalsIgnoreCase("Default")){
				item_mn = GlassPane.DEFAULT.getItemStack();
			}else {
				item_mn = new ItemStack(Material.getMaterial(WkKit.getWkKit().getConfig().getString("GUI.MenuMaterial")));
			}
			ItemMeta im = item_mn.getItemMeta();
			im.setDisplayName(LangConfigLoader.getString("DO_NOT_TOUCH"));
			item_mn.setItemMeta(im);
			//添加功能性物品：上一页
			ItemStack item_pre = new ItemStack(Material.getMaterial(WkKit.getWkKit().getConfig().getString("GUI.TurnPageMaterial")));
			ItemMeta ip = item_pre.getItemMeta();
			ip.setDisplayName(LangConfigLoader.getString("PREVIOUS_PAGE"));
			item_pre.setItemMeta(ip);
			//添加功能性物品：下一页
			ItemStack item_next = new ItemStack(Material.getMaterial(WkKit.getWkKit().getConfig().getString("GUI.TurnPageMaterial")));
			ItemMeta in = item_next.getItemMeta();
			in.setDisplayName(LangConfigLoader.getString("NEXT_PAGE"));
			item_next.setItemMeta(in);
			
			for(int i1 = 54 - 9; i1 < 54; i1++) {//最下一排
				inv.setItem(i1, item_mn);
			}

			kitGroupInvs[i-1] = inv;
		}
		//添加物品到指定的inv
		int num = 0;
		for(int invnum = 0; invnum < guinum; invnum++) {
			for(int i2 = 0; i2 < 45; i2++) {
				if(num >= groupNum || kitGroupItemList.size() == 0 ) {
					break;
				}
				kitGroupInvs[invnum].setItem(i2, kitGroupItemList.get(num));
				num += 1;
			}
		}
	}

	/**
	 * 获取礼包组展示界面的首页
	 * @return
	 */
	public Inventory getInventory() {
		return kitGroupInvs[0];
	}
	
	/**
	 * 打开礼包组展示界面（带会话注册）
	 * @param player 玩家
	 */
	public void openInventory(Player player) {
		GUISessionManager.registerGUI(player, GUIType.EDIT_MAIN);
		player.openInventory(kitGroupInvs[0]);
	}

	/**
	 * 编辑礼包组
	 * @param groupname
	 * @return
	 */
	public Inventory editGroup(String groupname, int page) {
		List<String> kitsname = KitGroupManager.getGroupKits(groupname);
		int kitnum = kitsname.size();
		List<ItemStack> itemlist = new ArrayList<>();
		String title = LangConfigLoader.getString("EDIT_KIT_GROUP_TITLE") + " - " + groupname;
		List<Integer> slot = Arrays.asList(10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43);
		//判断可创建的gui个数
		int guinum = 0;
		if(kitnum % 28 == 0 && !(kitnum == 0)) guinum = kitnum / 28;
		else guinum = (kitnum / 28) + 1;
		Inventory[] invlist = new Inventory[guinum];
		
		//添加物品到itemlist
		for(String kitname : kitsname) {
			Kit kit = Kit.getKit(kitname);
			ItemStack is = kit.getKitItem();
			ItemMeta im = is.getItemMeta();
			List<String> lore = new ArrayList<String>();
			lore.add(LangConfigLoader.getString("EDIT_CLICK_EDITKIT"));
			im.setLore(lore);
			is.setItemMeta(im);
			itemlist.add(is);
		}
		
		//创建gui到linv
		for(int i = 1; i <= guinum; i++) {
			Inventory inv;
			ItemStack item_mn;
		      if (guinum == 1) {
		    	  	inv = Bukkit.createInventory(null, 54, title);
		        } else {
		        	String pagetitle = WKTool.replacePlaceholder("page", i + "", LangConfigLoader.getString("GUI_PAGETITLE"));
		          	inv = Bukkit.createInventory(null, 54, title + " - " + pagetitle);
		        } 
		        if (WkKit.getWkKit().getConfig().getString("GUI.MenuMaterial").equalsIgnoreCase("Default")) {
		        	item_mn = GlassPane.DEFAULT.getItemStack();
		        } else {
		        	item_mn = new ItemStack(Material.getMaterial(WkKit.getWkKit().getConfig().getString("GUI.MenuMaterial")));
		        } 
		        // 设置按钮名称
		        item_mn = new ItemEditer(item_mn,LangConfigLoader.getString("DO_NOT_TOUCH")).getItemStack();
		        // 填入物品
		        for (int j = 0; j < 54; j++) {
		          if (!slot.contains(Integer.valueOf(j))) {
		        	  if(j == 1) {
	    				ItemStack is = GlassPane.BLACK.getItemStack();
	    				inv.setItem(j, new ItemEditer(is, LangConfigLoader.getString("EDIT_BACK")).getItemStack());
	    				continue;
		        	  }
		        	 inv.setItem(j, item_mn);  
		          }

		        } 
		        // 如果页数大于1则添加翻页按钮
		      if (guinum > 1) {
		          ItemStack item_pre = new ItemStack(Material.getMaterial(WkKit.getWkKit().getConfig().getString("GUI.TurnPageMaterial")));
		          item_pre = new ItemEditer(item_pre, LangConfigLoader.getString("PREVIOUS_PAGE")).getItemStack();
		          ReadWriteNBT nbti = WKTool.getItemNBT(item_pre);
		          nbti.setInteger("page", Integer.valueOf(i));
		          nbti.setString("groupname", groupname);
		          item_pre = NBT.itemStackFromNBT(nbti);
		          ItemStack item_next = new ItemStack(Material.getMaterial(WkKit.getWkKit().getConfig().getString("GUI.TurnPageMaterial")));
		          item_next = new ItemEditer(item_next, LangConfigLoader.getString("NEXT_PAGE")).getItemStack();
		          nbti = WKTool.getItemNBT(item_next);
		          nbti.setInteger("page", Integer.valueOf(i));
		          nbti.setString("groupname", groupname);
		          item_next = NBT.itemStackFromNBT(nbti);
		          if (i == 1) {
		            inv.setItem(50, item_next);
		          } else if (i == guinum) {
		            inv.setItem(48, item_pre);
		          } else {
		            inv.setItem(48, item_pre);
		            inv.setItem(50, item_next);
		          } 
		        } 
		        invlist[i - 1] = inv;
		}
		//添加物品到指定的inv
	    int counter = 0;
	    for (int invnum = 0; invnum < guinum; invnum++) {
	      for (int si = 0; si < slot.size() && counter != itemlist.size() && itemlist.size() != 0; si++) {
	        invlist[invnum].setItem(slot.get(si), itemlist.get(counter));
	        counter++;
	      } 
	    } 
	    return invlist[page - 1];
	}
	
	/**
	 * 打开礼包组编辑界面（带会话注册）
	 * @param player 玩家
	 * @param groupname 礼包组名
	 * @param page 页码
	 */
	public void openEditGroup(Player player, String groupname, int page) {
		GUISession session = GUISessionManager.registerGUI(player, GUIType.EDIT_GROUP);
		session.setData("groupname", groupname);
		session.setData("page", page);
		player.openInventory(editGroup(groupname, page));
	}
	
	/**
	 * 单个礼包编辑页面
	 * @param kitname
	 * @return
	 */
	public Inventory editKit(String kitname) {
		Kit kit = Kit.getKit(kitname);
		Inventory kitinv = Bukkit.createInventory(null, 4*9, LangConfigLoader.getString("EDIT_KIT_TITLE") + " - " + kitname);
		List<Integer> slot = Arrays.asList(0,2,3,5,6,8,13,22,31);
		List<Integer> hasflags = Arrays.asList(9,10,11,12,18,19,20,21,27,28,29,30);
		List<Integer> nonflags = Arrays.asList(14,15,16,17,23,24,25,26,32,33,34,35);
		// 填充物品
		for(int i = 0; i < 36; i++) {
			if(slot.contains(i)) {
				ItemStack is = GlassPane.DEFAULT.getItemStack();
				ItemMeta im = is.getItemMeta();
                assert im != null;
                im.setDisplayName(LangConfigLoader.getString("DO_NOT_TOUCH"));
				is.setItemMeta(im);
				kitinv.setItem(i, is);
				continue;
			}
			if(i == 1) {
				ItemStack is = GlassPane.BLACK.getItemStack();
				ItemMeta im = is.getItemMeta();
                assert im != null;
                im.setDisplayName(LangConfigLoader.getString("EDIT_BACK"));
				is.setItemMeta(im);
				NBT.modify(is, nbt -> {
					nbt.setString("wkkit", kitname);
				});
				kitinv.setItem(i, is);
				continue;
			}
			if(i == 4) {
				kitinv.setItem(i, Kit.getKit(kitname).getKitItem());
				continue;
			}
			if(i == 7) {
				ItemStack is = GlassPane.RED.getItemStack();
				ItemMeta im = is.getItemMeta();
                assert im != null;
                im.setDisplayName(LangConfigLoader.getString("EDIT_DELETE_KIT"));
				is.setItemMeta(im);
				NBT.modify(is, nbt -> {
					nbt.setString("wkkit", kitname);
				});
				kitinv.setItem(i, is);
			}
		}
		// 添加flag
		int hascount = 0;
		int noncount = 0;
		for(String key : kit.getFlags().keySet()) {
			if(kit.getFlags().get(key) != null) {
				ItemStack is = new ItemStack(Material.NAME_TAG); // 可能会报错
				ItemMeta im = is.getItemMeta();
				Object obj = kit.getFlags().get(key);
				im.setDisplayName("§e§l[√]§f§l " + key);
				if(obj instanceof String)im.setLore(Arrays.asList((String)obj));
				if(obj instanceof Integer) im.setLore(Arrays.asList(String.valueOf((int)obj)));
				if(obj instanceof Boolean) im.setLore(Arrays.asList(String.valueOf((boolean) obj)));
				if(obj instanceof List) im.setLore((List<String>)obj);
				if(obj instanceof ItemStack[]) im.setLore(Arrays.asList(LangConfigLoader.getString("EDIT_CLICK_EDIT")));
				is.setItemMeta(im);
				NBT.modify(is, nbt -> {
					nbt.setString("wkkit", key);
				});
				kitinv.setItem(hasflags.get(hascount),is);
				hascount++;
			}else {
				ItemStack is = new ItemStack(Material.NAME_TAG); // 可能会报错
				ItemMeta im = is.getItemMeta();
				im.setDisplayName("§e§l[§a§l+§e§l]§f§l " + key);
				is.setItemMeta(im);
				is.setItemMeta(im);
				NBT.modify(is, nbt -> {
					nbt.setString("wkkit", kitname);
				});
				kitinv.setItem(hasflags.get(hascount),is);
				noncount++;
			}
			
		}
		return kitinv;
	}
	
	/**
	 * 打开单个礼包编辑页面（带会话注册）
	 * @param player 玩家
	 * @param kitname 礼包名
	 */
	public void openEditKit(Player player, String kitname) {
		GUISession session = GUISessionManager.registerGUI(player, GUIType.EDIT_KIT);
		session.setData("kitname", kitname);
		player.openInventory(editKit(kitname));
	}
	/**
	 * 管理礼包的Item内容
	 * @param kitname
	 * @return
	 */
	public Inventory editKitItem(String kitname) {
		Kit kit = Kit.getKit(kitname);
		Inventory kitinv = Bukkit.createInventory(null, 5*9, LangConfigLoader.getString("EDIT_KIT_ITEM_TITLE") + " - " + kitname);
		// 填充物品
		kitinv.addItem(kit.getItemStacks());
		for(int i = 36; i < 45; i++) {
			if(i == 40) {
				ItemStack is = GlassPane.GREEN.getItemStack();
				ItemMeta im = is.getItemMeta();
				im.setDisplayName(LangConfigLoader.getString("EDIT_SAVE"));
				is.setItemMeta(im);
				NBT.modify(is, nbt -> {
					nbt.setString("wkkit", kitname);
				});
				kitinv.setItem(i, is);
				continue;
			}else {
				ItemStack is = GlassPane.DEFAULT.getItemStack();
				ItemMeta im = is.getItemMeta();
				im.setDisplayName(LangConfigLoader.getString("DO_NOT_TOUCH"));
				is.setItemMeta(im);
				kitinv.setItem(i, is);
				continue;
			}
		}
		return kitinv;
	}
	
	/**
	 * 打开礼包物品编辑界面（带会话注册）
	 * @param player 玩家
	 * @param kitname 礼包名
	 */
	public void openEditKitItem(Player player, String kitname) {
		GUISession session = GUISessionManager.registerGUI(player, GUIType.EDIT_KIT_ITEM);
		session.setData("kitname", kitname);
		player.openInventory(editKitItem(kitname));
	}
	
	@EventHandler
	public void onInvClick(InventoryClickEvent e) {
		// 只处理玩家点击事件
		if (!(e.getWhoClicked() instanceof Player)) return;
		Player player = (Player) e.getWhoClicked();
		
		// 获取玩家的 GUI 会话（完全避免使用 getHolder()）
		GUISession session = GUISessionManager.getSession(player);
		if (session == null) return; // 不是自定义 GUI，直接返回
		
		// 只处理顶部 inventory 的点击
		if (e.getClickedInventory() == null || !e.getClickedInventory().equals(e.getInventory())) {
			return;
		}
		
		GUIType guiType = session.getType();
		
		// ========== 礼包管理主界面 ==========
		if (guiType == GUIType.EDIT_MAIN) {
			e.setCancelled(true);
			if(e.getAction().equals(NOTHING) || e.getAction().equals(UNKNOWN)) return;
			if (ItemEditer.hasWkKitTag(e.getCurrentItem())) {
				String groupname = e.getCurrentItem().getItemMeta().getDisplayName();
				openEditGroup(player, groupname, 1);
				return;
			}
			return;
		}
		
		// ========== 礼包组编辑界面 ==========
		if (guiType == GUIType.EDIT_GROUP) {
			e.setCancelled(true);
			if (e.getAction().equals(NOTHING) || e.getAction().equals(UNKNOWN)) return;
			
			if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) {
				return;
			}
			
			String groupname = session.getString("groupname");
			
			// 如果是返回按钮（槽位1，优先检查）
			if(e.getRawSlot() == 1) {
				new EditKit().openInventory(player);
				return;
			}
			
			// 上一页按钮（槽位48）
			if (e.getRawSlot() == 48) {
				if (ItemEditer.hasWkKitTag(e.getCurrentItem())) {
					ReadWriteNBT nbt = WKTool.getItemNBT(e.getCurrentItem());
					int page = nbt.getInteger("page");
					String group = nbt.getString("groupname");
					if (group != null && page > 1) {
						openEditGroup(player, group, page - 1);
					}
				}
				return;
			}
			
			// 下一页按钮（槽位50）
			if (e.getRawSlot() == 50) {
				if (ItemEditer.hasWkKitTag(e.getCurrentItem())) {
					ReadWriteNBT nbt = WKTool.getItemNBT(e.getCurrentItem());
					int page = nbt.getInteger("page");
					String group = nbt.getString("groupname");
					if (group != null) {
						openEditGroup(player, group, page + 1);
					}
				}
				return;
			}
			
			// 点击礼包物品 - 打开礼包编辑界面（只在有效槽位中）
			List<Integer> validSlots = Arrays.asList(10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43);
			if (validSlots.contains(e.getRawSlot()) && ItemEditer.hasWkKitTag(e.getCurrentItem())) {
				String kitname = ItemEditer.getWkKitTagValue(e.getCurrentItem());
				if (kitname != null && !kitname.isEmpty()) {
					openEditKit(player, kitname);
				}
				return;
			}
			return;
		}
		
		// ========== 单个礼包编辑界面 ==========
		if (guiType == GUIType.EDIT_KIT) {
			e.setCancelled(true);
			if(e.getAction().equals(NOTHING) || e.getAction().equals(UNKNOWN)) return;
			
			String kitName = session.getString("kitname");
			if (kitName == null) return;
			
			// 左键点击
			if(ItemEditer.hasWkKitTag(e.getCurrentItem()) && e.getClick().equals(ClickType.LEFT)) {
				String key = ItemEditer.getWkKitTagValue(e.getCurrentItem());
				if (key != null) {
					// 返回按钮
					if(e.getRawSlot() == 1) {
						String groupName = KitGroupManager.getContainName(Kit.getKit(kitName));
						if (groupName != null) {
							openEditGroup(player, groupName, 1);
							return;
						}
					}
					
					// 删除礼包按钮
					if(e.getRawSlot() == 7) {
						player.closeInventory();
						KitDeletePrompt.newConversation(player, kitName);
						return;
					}
					
					// 各种 Flag 编辑
					if(key.equals("DisplayName")) {player.closeInventory();KitFlagPrompt.setFlag(player, kitName, "DisplayName");return;}
					if(key.equals("Icon")) {player.closeInventory();KitFlagPrompt.setFlag(player, kitName, "Icon");return;}
					if(key.equals("Times")) {player.closeInventory();KitFlagPrompt.setFlag(player, kitName, "Times");return;}
					if(key.equals("Delay")) {player.closeInventory();KitFlagPrompt.setFlag(player, kitName, "Delay");return;}
					if(key.equals("Permission")) {player.closeInventory();KitFlagPrompt.setFlag(player, kitName, "Permission");return;}
					if(key.equals("DoCron")) {player.closeInventory();KitFlagPrompt.setFlag(player, kitName, "DoCron");return;}
					if(key.equals("Commands")) {player.closeInventory();KitFlagPrompt.setFlag(player, kitName, "Commands");return;}
					if(key.equals("Lore")) {player.closeInventory();KitFlagPrompt.setFlag(player, kitName, "Lore");return;}
					if(key.equals("Drop")) {player.closeInventory();KitFlagPrompt.setFlag(player, kitName, "Drop");return;}
					if(key.equals("Vault")) {player.closeInventory();KitFlagPrompt.setFlag(player, kitName, "Vault");return;}
					if(key.equals("NoRefreshFirst")) {player.closeInventory();KitFlagPrompt.setFlag(player, kitName, "NoRefreshFirst");return;}
					if(key.equals("MythicMobs")) {player.closeInventory();KitFlagPrompt.setFlag(player, kitName, "MythicMobs");return;}
					if(key.equals("Item")) {openEditKitItem(player, kitName);return;}
				}
			}
			
			// 右键点击 - 删除 Flag
			if(ItemEditer.hasWkKitTag(e.getCurrentItem()) && e.getClick().equals(ClickType.RIGHT)) {
				String key = ItemEditer.getWkKitTagValue(e.getCurrentItem());
				if (key != null) {
					List<Integer> hasflags = Arrays.asList(9,10,11,12,18,19,20,21,27,28,29,30);
					if(hasflags.contains(e.getRawSlot())) {
						if(key.equals("DisplayName")) {player.closeInventory();KitFlagPrompt.deFlag(player, kitName, "DisplayName");return;}
						if(key.equals("Icon")) {player.closeInventory();KitFlagPrompt.deFlag(player, kitName, "Icon");return;}
						if(key.equals("Times")) {player.closeInventory();KitFlagPrompt.deFlag(player, kitName, "Times");return;}
						if(key.equals("Delay")) {player.closeInventory();KitFlagPrompt.deFlag(player, kitName, "Delay");return;}
						if(key.equals("Permission")) {player.closeInventory();KitFlagPrompt.deFlag(player, kitName, "Permission");return;}
						if(key.equals("DoCron")) {player.closeInventory();KitFlagPrompt.deFlag(player, kitName, "DoCron");return;}
						if(key.equals("Commands")) {player.closeInventory();KitFlagPrompt.deFlag(player, kitName, "Commands");return;}
						if(key.equals("Lore")) {player.closeInventory();KitFlagPrompt.deFlag(player, kitName, "Lore");return;}
						if(key.equals("Drop")) {player.closeInventory();KitFlagPrompt.deFlag(player, kitName, "Drop");return;}
						if(key.equals("Vault")) {player.closeInventory();KitFlagPrompt.deFlag(player, kitName, "Vault");return;}
						if(key.equals("NoRefreshFirst")) {player.closeInventory();KitFlagPrompt.deFlag(player, kitName, "NoRefreshFirst");return;}
						if(key.equals("MythicMobs")) {player.closeInventory();KitFlagPrompt.deFlag(player, kitName, "MythicMobs");return;}
					}
				}
			}
		}
		
		// ========== 礼包物品编辑界面 ==========
		if (guiType == GUIType.EDIT_KIT_ITEM) {
			if(e.getAction().equals(NOTHING) || e.getAction().equals(UNKNOWN)) return;
			
			// 只拦截底部功能栏的点击（36-44）
			if(e.getRawSlot() >= 36 && e.getRawSlot() < 45) {
				e.setCancelled(true);
				
				// 保存按钮（槽位40）
				if(e.getRawSlot() == 40) {
					String kitName = session.getString("kitname");
					if (kitName != null) {
						Kit kit = Kit.getKit(kitName);
						if (kit != null) {
							List<ItemStack> list = new ArrayList<ItemStack>();
							for(int i = 0; i < 36; i++) {
								if(e.getInventory().getItem(i) == null) continue;
								list.add(e.getInventory().getItem(i));
							}
							kit.setItemStack(list.toArray(new ItemStack[list.size()]));
							kit.saveConfig();
							openEditKit(player, kit.getKitname());
							player.sendMessage(LangConfigLoader.getStringWithPrefix("SAVE_SUCCESS", ChatColor.GREEN));
						}
					}
				}
			}
			return;
		}
	}
	
}
