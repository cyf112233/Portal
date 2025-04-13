package byd.cxkcxkckx.Portal.func;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import byd.cxkcxkckx.Portal.Portal;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.List;

public class PortalListener extends AbstractModule implements Listener {
    private final Set<Material> frameMaterials = new HashSet<>();
    private int maxRange;
    private boolean showActivationMessage;
    private boolean debugMode;
    private int maxPortalBlocks;

    public PortalListener(Portal plugin) {
        super(plugin);
        reloadConfig();
    }

    public void reloadConfig() {
        frameMaterials.clear();
        List<String> materials = plugin.getConfig().getStringList("portal.frame-materials");
        for (String materialName : materials) {
            try {
                Material material = Material.valueOf(materialName);
                frameMaterials.add(material);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("无效的材料名称: " + materialName);
            }
        }
        maxRange = plugin.getConfig().getInt("portal.max-range", 20);
        showActivationMessage = plugin.getConfig().getBoolean("portal.show-activation-message", true);
        debugMode = plugin.getConfig().getBoolean("portal.debug-mode", false);
        maxPortalBlocks = plugin.getConfig().getInt("portal.max-portal-blocks", 100);
    }

    private void debug(String message) {
        if (debugMode) {
            plugin.getLogger().info("[DEBUG] " + message);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // 检查是否是右键点击
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();
        ItemStack item = event.getItem();

        // 检查是否使用打火石
        if (item != null && item.getType() == Material.FLINT_AND_STEEL) {
            debug("玩家 " + player.getName() + " 使用打火石点击了方块: " + clickedBlock.getType() + " 位置: " + 
                  clickedBlock.getX() + ", " + clickedBlock.getY() + ", " + clickedBlock.getZ());
            
            // 检查点击的方块是否可以作为传送门框架
            if (frameMaterials.contains(clickedBlock.getType())) {
                debug("检测到有效的框架方块");
                // 检查是否是封闭的框架
                if (isValidPortalFrame(clickedBlock)) {
                    debug("框架验证通过，开始创建传送门");
                    // 创建传送门
                    createPortal(clickedBlock);
                    if (showActivationMessage) {
                        player.sendMessage("§a成功激活自定义传送门！");
                    }
                    // 取消事件，防止重复触发
                    event.setCancelled(true);
                } else {
                    debug("框架验证失败，门框不完整");
                    if (showActivationMessage) {
                        player.sendMessage("§c门框不完整，无法激活传送门！");
                    }
                }
            } else {
                debug("点击的方块不是有效的框架材料");
            }
        }
    }

    private Set<Block> findValidFrame(Block start) {
        Set<Block> allFrameBlocks = new HashSet<>();
        Stack<Block> stack = new Stack<>();
        stack.push(start);
        allFrameBlocks.add(start);

        // 收集所有相连的框架方块
        while (!stack.isEmpty()) {
            Block current = stack.pop();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) != 1) continue;
                        Block neighbor = current.getRelative(dx, dy, dz);
                        if (!allFrameBlocks.contains(neighbor) && frameMaterials.contains(neighbor.getType())) {
                            stack.push(neighbor);
                            allFrameBlocks.add(neighbor);
                        }
                    }
                }
            }
        }

        debug("找到框架，包含 " + allFrameBlocks.size() + " 个方块");
        return allFrameBlocks;
    }

    private boolean isValidPortalFrame(Block start) {
        Set<Block> frameBlocks = findValidFrame(start);
        
        if (frameBlocks.isEmpty()) {
            debug("未找到有效的框架");
            return false;
        }

        // 检查框架是否形成闭合环
        boolean isClosed = isFrameClosed(frameBlocks);
        debug("框架闭合检查结果: " + isClosed);
        return isClosed;
    }

    private boolean isFrameClosed(Set<Block> frameBlocks) {
        // 检查每个框架方块是否至少有两个相邻的框架方块
        for (Block block : frameBlocks) {
            int adjacentCount = 0;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) != 1) continue;
                        Block neighbor = block.getRelative(dx, dy, dz);
                        if (frameBlocks.contains(neighbor)) {
                            adjacentCount++;
                        }
                    }
                }
            }
            if (adjacentCount < 2) {
                debug("发现不完整的框架方块: " + block.getX() + ", " + block.getY() + ", " + block.getZ() + 
                      "，相邻方块数量: " + adjacentCount);
                return false;
            }
        }
        return true;
    }

    private void createPortal(Block center) {
        // 获取所有框架方块
        Set<Block> frameBlocks = new HashSet<>();
        Stack<Block> stack = new Stack<>();
        stack.push(center);
        frameBlocks.add(center);

        debug("开始创建传送门，中心位置: " + center.getX() + ", " + center.getY() + ", " + center.getZ());

        while (!stack.isEmpty()) {
            Block current = stack.pop();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) != 1) continue;
                        Block neighbor = current.getRelative(dx, dy, dz);
                        if (!frameBlocks.contains(neighbor) && frameMaterials.contains(neighbor.getType())) {
                            stack.push(neighbor);
                            frameBlocks.add(neighbor);
                        }
                    }
                }
            }
        }

        // 找到框架的边界
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

        for (Block block : frameBlocks) {
            minX = Math.min(minX, block.getX());
            maxX = Math.max(maxX, block.getX());
            minY = Math.min(minY, block.getY());
            maxY = Math.max(maxY, block.getY());
            minZ = Math.min(minZ, block.getZ());
            maxZ = Math.max(maxZ, block.getZ());
        }

        // 计算需要生成的传送门方块数量
        int portalBlockCount = 0;
        for (int x = minX + 1; x < maxX; x++) {
            for (int y = minY + 1; y < maxY; y++) {
                for (int z = minZ + 1; z < maxZ; z++) {
                    Block block = center.getWorld().getBlockAt(x, y, z);
                    if (block.getType() == Material.AIR) {
                        portalBlockCount++;
                    }
                }
            }
        }

        debug("需要生成的传送门方块数量: " + portalBlockCount);

        // 检查是否超过最大限制
        if (maxPortalBlocks != -1 && portalBlockCount > maxPortalBlocks) {
            debug("传送门方块数量超过限制: " + portalBlockCount + " > " + maxPortalBlocks);
            if (showActivationMessage) {
                center.getWorld().getPlayers().forEach(player -> 
                    player.sendMessage("§c传送门过大，无法激活！最大允许方块数: " + maxPortalBlocks));
            }
            return;
        }

        // 判断传送门方向
        boolean isXAxis = false;
        boolean isZAxis = false;
        int firstX = -1;
        int firstZ = -1;

        for (Block block : frameBlocks) {
            if (firstX == -1) {
                firstX = block.getX();
                firstZ = block.getZ();
            } else {
                if (block.getX() != firstX) {
                    isXAxis = true;
                }
                if (block.getZ() != firstZ) {
                    isZAxis = true;
                }
            }
        }

        debug("传送门方向判断 - X轴: " + isXAxis + ", Z轴: " + isZAxis);

        // 根据传送门方向创建传送门方块
        if (isXAxis) {
            debug("创建与X轴平行的传送门");
            // 与X轴平行的传送门
            for (int x = minX + 1; x < maxX; x++) {
                for (int y = minY + 1; y < maxY; y++) {
                    Block block = center.getWorld().getBlockAt(x, y, firstZ);
                    if (block.getType() == Material.AIR) {
                        block.setType(Material.NETHER_PORTAL);
                        org.bukkit.block.data.Orientable data = (org.bukkit.block.data.Orientable) block.getBlockData();
                        data.setAxis(org.bukkit.Axis.X);
                        block.setBlockData(data);
                        debug("创建传送门方块: " + x + ", " + y + ", " + firstZ);
                    }
                }
            }
        } else if (isZAxis) {
            debug("创建与Z轴平行的传送门");
            // 与Z轴平行的传送门
            for (int z = minZ + 1; z < maxZ; z++) {
                for (int y = minY + 1; y < maxY; y++) {
                    Block block = center.getWorld().getBlockAt(firstX, y, z);
                    if (block.getType() == Material.AIR) {
                        block.setType(Material.NETHER_PORTAL);
                        org.bukkit.block.data.Orientable data = (org.bukkit.block.data.Orientable) block.getBlockData();
                        data.setAxis(org.bukkit.Axis.Z);
                        block.setBlockData(data);
                        debug("创建传送门方块: " + firstX + ", " + y + ", " + z);
                    }
                }
            }
        }
    }
} 