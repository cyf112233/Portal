package byd.cxkcxkckx.Portal.func;
        
import byd.cxkcxkckx.Portal.Portal;
import org.bukkit.Bukkit;

public abstract class AbstractModule extends top.mrxiaom.pluginbase.func.AbstractModule<Portal> {
    public AbstractModule(Portal plugin) {
        super(plugin);
    }

    public void register() {
        if (this instanceof org.bukkit.event.Listener) {
            Bukkit.getPluginManager().registerEvents((org.bukkit.event.Listener) this, plugin);
        }
    }
}
