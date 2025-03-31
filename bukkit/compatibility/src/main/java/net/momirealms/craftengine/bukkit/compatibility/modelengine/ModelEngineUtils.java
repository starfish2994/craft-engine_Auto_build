package net.momirealms.craftengine.bukkit.compatibility.modelengine;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import org.bukkit.entity.Entity;

public class ModelEngineUtils {

    public static void bindModel(Entity base, String id) {
        ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(base);
        ActiveModel activeModel = ModelEngineAPI.createActiveModel(id);
        modeledEntity.addModel(activeModel, true);
    }
}
