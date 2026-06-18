package cn.wekyjay.www.wkkit.invholder;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * GUI 会话管理器
 * 用于追踪玩家当前打开的自定义 GUI，避免使用 getHolder() 导致的性能问题
 * 特别是在 Folia 环境中，getHolder() 会导致同步加载区块和 NBT 数据
 */
public class GUISessionManager implements Listener {
    
    private static final Map<UUID, GUISession> sessions = new HashMap<>();
    
    /**
     * GUI 会话数据类
     */
    public static class GUISession {
        private final GUIType type;
        private final Map<String, Object> data;
        
        public GUISession(GUIType type) {
            this.type = type;
            this.data = new HashMap<>();
        }
        
        public GUIType getType() {
            return type;
        }
        
        public void setData(String key, Object value) {
            data.put(key, value);
        }
        
        public Object getData(String key) {
            return data.get(key);
        }
        
        public String getString(String key) {
            Object obj = data.get(key);
            return obj != null ? obj.toString() : null;
        }
        
        public Integer getInt(String key) {
            Object obj = data.get(key);
            return obj instanceof Integer ? (Integer) obj : null;
        }
    }
    
    /**
     * GUI 类型枚举
     */
    public enum GUIType {
        EDIT_MAIN,           // 礼包管理主页
        EDIT_GROUP,          // 礼包组编辑
        EDIT_KIT,            // 单个礼包编辑
        EDIT_KIT_ITEM,       // 礼包物品编辑
        KIT_MENU,            // 礼包菜单
        KIT_PREVIEW,         // 礼包预览
        EDIT_GUI             // 编辑主界面
    }
    
    /**
     * 注册玩家打开的 GUI
     * @param player 玩家
     * @param type GUI 类型
     * @return 创建的会话对象
     */
    public static GUISession registerGUI(Player player, GUIType type) {
        GUISession session = new GUISession(type);
        sessions.put(player.getUniqueId(), session);
        return session;
    }
    
    /**
     * 注册玩家打开的 GUI（带数据）
     * @param player 玩家
     * @param type GUI 类型
     * @param dataKey 数据键
     * @param dataValue 数据值
     * @return 创建的会话对象
     */
    public static GUISession registerGUI(Player player, GUIType type, String dataKey, Object dataValue) {
        GUISession session = new GUISession(type);
        session.setData(dataKey, dataValue);
        sessions.put(player.getUniqueId(), session);
        return session;
    }
    
    /**
     * 获取玩家当前的 GUI 会话
     * @param player 玩家
     * @return GUI 会话，如果不存在返回 null
     */
    public static GUISession getSession(Player player) {
        return sessions.get(player.getUniqueId());
    }
    
    /**
     * 检查玩家是否打开了指定类型的 GUI
     * @param player 玩家
     * @param type GUI 类型
     * @return 是否打开了该类型的 GUI
     */
    public static boolean hasGUI(Player player, GUIType type) {
        GUISession session = sessions.get(player.getUniqueId());
        return session != null && session.getType() == type;
    }
    
    /**
     * 更新会话数据
     * @param player 玩家
     * @param key 数据键
     * @param value 数据值
     */
    public static void updateSessionData(Player player, String key, Object value) {
        GUISession session = sessions.get(player.getUniqueId());
        if (session != null) {
            session.setData(key, value);
        }
    }
    
    /**
     * 注销玩家的 GUI 会话
     * @param player 玩家
     */
    public static void unregisterGUI(Player player) {
        sessions.remove(player.getUniqueId());
    }
    
    /**
     * 清理所有会话（用于插件重载）
     */
    public static void clearAll() {
        sessions.clear();
    }
    
    /**
     * 获取当前会话数量（调试用）
     */
    public static int getSessionCount() {
        return sessions.size();
    }
    
    // ========== 事件监听器 ==========
    
    /**
     * 玩家关闭背包时的处理
     * 策略：完全不清理会话
     * 理由：
     * 1. 新会话会自动覆盖旧会话（registerGUI 使用 put 操作）
     * 2. 避免在 GUI 切换过程中意外清理会话
     * 3. 真正的清理只在玩家离线时进行（onPlayerQuit）
     * 4. 完全避免使用 getHolder() 方法
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        // 不做任何操作，会话只在玩家退出时清理
        // 这样可以避免 GUI 切换过程中的时序问题
    }
    
    /**
     * 玩家退出游戏时清理会话
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        unregisterGUI(e.getPlayer());
    }
}
