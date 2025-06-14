package net.momirealms.craftengine.bukkit.compatibility.model.freeminecraftmodels;

import net.momirealms.craftengine.core.entity.AbstractEntity;
import net.momirealms.craftengine.core.entity.furniture.AbstractExternalModel;

public class FreeMinecraftModelsModel extends AbstractExternalModel {


    public FreeMinecraftModelsModel(String id) {
        super(id);
    }

    @Override
    public String plugin() {
        return "FreeMinecraftModels";
    }

    @Override
    public void bindModel(AbstractEntity entity) {
        org.bukkit.entity.Entity bukkitEntity = (org.bukkit.entity.Entity) entity.literalObject();
        FreeMinecraftModelsUtils.bindModel(bukkitEntity, id());
    }
}
