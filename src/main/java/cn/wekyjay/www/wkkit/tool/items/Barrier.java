package cn.wekyjay.www.wkkit.tool.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum Barrier {
	DEFAULT;
	
	private ItemStack item;
	
	Barrier() {
		this.item = new ItemStack(Material.BARRIER, 1);
	}
	
	public ItemStack getItemStack() {
		return item.clone();
	}
}
