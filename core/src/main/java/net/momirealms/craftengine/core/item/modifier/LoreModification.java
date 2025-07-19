package net.momirealms.craftengine.core.item.modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.util.AdventureHelper;
public record LoreModification(Action action, int priority, boolean split, List<String> content) implements Comparable<LoreModification> {
    public LoreModification(List<String> content) {
        this(Action.SET, 0, false, content);
    }
    public LoreModification(Action action, int priority, boolean split, List<String> content) {
        this.action = action;
        this.priority = priority;
        this.split = split;
        if (Config.addNonItalicTag()) {
            List<String> processed = new ArrayList<>(content.size());
            for (String arg : content) {
                processed.add(arg.startsWith("<!i>") ? arg : "<!i>" + arg);
            }
            this.content = processed;
        } else {
            this.content = content;
        }
    }
    public Stream<Component> apply(Stream<Component> lore, ItemBuildContext context) {
        return switch (action) {
            case PREPEND -> Stream.concat(parseContent(context), lore);
            case APPEND -> Stream.concat(lore, parseContent(context));
            default -> parseContent(context);
        };
    }
    private Stream<Component> parseContent(ItemBuildContext context) {
        Stream<Component> parsed = content.stream().map(string -> AdventureHelper.miniMessage().deserialize(string, context.tagResolvers()));
        return split ? parsed.map(AdventureHelper::splitLines).flatMap(List::stream) : parsed;
    }
    @Override
    public int compareTo(LoreModification other) {
        return Integer.compare(priority, other.priority);
    }
    public enum Action {
        SET, PREPEND, APPEND
    }
}
