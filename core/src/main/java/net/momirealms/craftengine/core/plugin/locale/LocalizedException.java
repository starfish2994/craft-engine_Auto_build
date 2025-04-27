package net.momirealms.craftengine.core.plugin.locale;

import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.craftengine.core.util.ArrayUtils;
import org.jetbrains.annotations.Nullable;

public class LocalizedException extends RuntimeException {
    private final String node;
    private final Exception originalException;
    private String[] arguments;

    public LocalizedException(String node, Exception originalException, String... arguments) {
        super(node);
        this.node = node;
        this.arguments = arguments;
        this.originalException = originalException;
    }

    public LocalizedException(String node, String... arguments) {
        this(node, (Exception) null, arguments);
    }

    public LocalizedException(String node, String[] arguments1, String... arguments2) {
        this(node, (Exception) null, ArrayUtils.merge(arguments1, arguments2));
    }

    @Nullable
    public Exception originalException() {
        return originalException;
    }

    public String[] arguments() {
        return arguments;
    }

    public String node() {
        return node;
    }

    public void setArgument(int index, String argument) {
        this.arguments[index] = argument;
    }

    public void appendHeadArgument(String argument) {
        this.arguments = ArrayUtils.appendElementToArrayHead(this.arguments, argument);
    }

    public void appendTailArgument(String argument) {
        this.arguments = ArrayUtils.appendElementToArrayTail(this.arguments, argument);
    }

    // we should not call this method directly, as it's inaccurate
    @Override
    public String getMessage() {
        if (originalException != null) {
            return originalException.getMessage();
        }
        String text = AdventureHelper.miniMessage().stripTags(TranslationManager.instance().miniMessageTranslation(this.node));
        for (int i = 0; i < arguments.length; i++) {
            text = text.replace("<arg:" + i + ">", arguments[i]);
        }
        return text;
//        return  "\n" + "Node: '" + this.node + "'" +
//                "\n" + "Translation: '" + AdventureHelper.miniMessage().stripTags(TranslationManager.instance().miniMessageTranslation(this.node)) + "'" +
//                "\n" + "Arguments: " + Arrays.toString(this.arguments);
    }
}
