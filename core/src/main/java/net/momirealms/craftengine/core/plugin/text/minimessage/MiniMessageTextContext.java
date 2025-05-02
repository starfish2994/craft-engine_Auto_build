package net.momirealms.craftengine.core.plugin.text.minimessage;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.momirealms.craftengine.core.util.context.Context;

public interface MiniMessageTextContext extends Context {

    TagResolver[] tagResolvers();
}
