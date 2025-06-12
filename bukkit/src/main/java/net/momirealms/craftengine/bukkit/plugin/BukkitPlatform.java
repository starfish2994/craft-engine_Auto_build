package net.momirealms.craftengine.bukkit.plugin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MRegistryOps;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.Platform;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import org.bukkit.Bukkit;

import java.util.Map;

public class BukkitPlatform implements Platform {

    @Override
    public void dispatchCommand(String command) {
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object nbt2Java(String nbt) {
        try {
            Object tag = FastNMS.INSTANCE.method$TagParser$parseCompoundFully("{\"root\":" + nbt + "}");
            Map<String, Object> map = (Map<String, Object>) MRegistryOps.NBT.convertTo(MRegistryOps.JAVA, tag);
            return map.get("root");
        } catch (CommandSyntaxException e) {
            CraftEngine.instance().debug(e::getMessage);
            throw new LocalizedResourceConfigException("warning.config.template.argument.default_value.invalid_syntax", e, nbt);
        }
    }
}
