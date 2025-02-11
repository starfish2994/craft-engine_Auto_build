package net.momirealms.craftengine.core.plugin.locale;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.Translator;
import net.momirealms.craftengine.core.plugin.Reloadable;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public interface TranslationManager extends Reloadable {
    default String miniMessageTranslation(String key) {
        return miniMessageTranslation(key, null);
    }

    void forceLocale(Locale locale);

    String miniMessageTranslation(String key, @Nullable Locale locale);

    default Component render(Component component) {
        return render(component, null);
    }

    Component render(Component component, @Nullable Locale locale);

    static @Nullable Locale parseLocale(@Nullable String locale) {
        return locale == null || locale.isEmpty() ? null : Translator.parseLocale(locale);
    }
}
