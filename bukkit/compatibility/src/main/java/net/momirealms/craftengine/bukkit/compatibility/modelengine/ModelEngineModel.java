package net.momirealms.craftengine.bukkit.compatibility.modelengine;

import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.entity.furniture.AbstractExternalModel;

public class ModelEngineModel extends AbstractExternalModel {

    public ModelEngineModel(String id) {
        super(id);
    }

    @Override
    public String plugin() {
        return "ModelEngine";
    }

    @Override
    public void bindModel(Entity entity) {
        org.bukkit.entity.Entity bukkitEntity = (org.bukkit.entity.Entity) entity.literalObject();
        ModelEngineUtils.bindModel(bukkitEntity, id());
    }
}
