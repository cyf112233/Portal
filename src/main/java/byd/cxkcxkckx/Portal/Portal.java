package byd.cxkcxkckx.Portal;
        
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.EconomyHolder;
import byd.cxkcxkckx.Portal.func.PortalListener;

public class Portal extends BukkitPlugin {
    public static Portal getInstance() {
        return (Portal) BukkitPlugin.getInstance();
    }

    public Portal() {
        super(options()
                .bungee(false)
                .adventure(false)
                .database(false)
                .reconnectDatabaseWhenReloadConfig(false)
                .vaultEconomy(false)
                .scanIgnore("top.mrxiaom.example.libs")
        );
    }

    @Override
    protected void afterEnable() {
        getLogger().info("Portal 加载完毕");
        // 注册监听器
        new PortalListener(this).register();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        // 重新加载监听器配置
        PortalListener listener = new PortalListener(this);
        listener.reloadConfig();
    }
}
