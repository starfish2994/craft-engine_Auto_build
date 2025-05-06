package net.momirealms.craftengine.core.world.chunk;

import net.momirealms.craftengine.core.world.SectionPos;

public interface InjectedHolder {

    CESection ceSection();

    void ceSection(CESection section);

    CEChunk ceChunk();

    void ceChunk(CEChunk chunk);

    SectionPos cePos();

    void cePos(SectionPos pos);

    interface Section extends InjectedHolder {
    }

    interface Palette extends InjectedHolder {

        Object target();
    }
}
