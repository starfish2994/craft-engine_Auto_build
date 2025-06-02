package net.momirealms.craftengine.bukkit.compatibility.mythicmobs;

import io.lumine.mythic.api.adapters.AbstractItemStack;
import io.lumine.mythic.api.adapters.AbstractPlayer;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.drops.DropMetadata;
import io.lumine.mythic.api.drops.IItemDrop;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.drops.droppables.ItemDrop;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.MCUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CraftEngineItemDrop extends ItemDrop implements IItemDrop {
    private final CustomItem<ItemStack> customItem;

    public CraftEngineItemDrop(String line, MythicLineConfig config, CustomItem<ItemStack> customItem) {
        super(line, config);
        this.customItem = customItem;
        CraftEngine.instance().debug(() -> "[MM调试] " + customItem.id() + " 注册成功");
    }

    @Override
    public AbstractItemStack getDrop(DropMetadata dropMetadata, double amount) {
        CraftEngine.instance().debug(() -> "[MM调试] getDrop() dropMetadata={" + dropMetadata + "}, amount={" + amount + "}");
        ItemBuildContext context = ItemBuildContext.EMPTY;
        SkillCaster caster = dropMetadata.getCaster();
        if (caster != null && caster.getEntity() instanceof AbstractPlayer abstractPlayer) {
            Entity bukkitEntity = abstractPlayer.getBukkitEntity();
            if (bukkitEntity instanceof Player bukkitPlayer) {
                var player = BukkitCraftEngine.instance().adapt(bukkitPlayer);
                context = ItemBuildContext.of(player);
            }
        }
        int amountInt = MCUtils.fastFloor(amount + 0.5F);
        ItemStack itemStack = this.customItem.buildItemStack(context, amountInt);
        return BukkitAdapter.adapt(itemStack).amount(amountInt);
    }
}
