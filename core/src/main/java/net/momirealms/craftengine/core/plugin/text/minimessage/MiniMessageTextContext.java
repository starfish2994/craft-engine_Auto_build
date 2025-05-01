package net.momirealms.craftengine.core.plugin.text.minimessage;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.momirealms.craftengine.core.util.context.Context;
import net.momirealms.craftengine.core.util.context.ContextHolder;

public interface MiniMessageTextContext extends Context {

    TagResolver[] tagResolvers();
}
