package net.momirealms.craftengine.core.plugin.config.template;

import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.Manageable;
import net.momirealms.craftengine.core.plugin.config.ConfigParser;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.Key;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface TemplateManager extends Manageable {

    ConfigParser parser();

    Object applyTemplates(Key id, Object input);

    interface ArgumentString {

        String rawValue();

        Object get(Map<String, TemplateArgument> arguments);
    }

    final class Literal implements ArgumentString {
        private final String value;

        public Literal(String value) {
            this.value = value;
        }

        public static Literal literal(String value) {
            return new Literal(value);
        }

        @Override
        public String rawValue() {
            return this.value;
        }

        @Override
        public Object get(Map<String, TemplateArgument> arguments) {
            return this.value;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Literal literal)) return false;
            return this.value.equals(literal.value);
        }

        @Override
        public int hashCode() {
            return this.value.hashCode();
        }

        @Override
        public String toString() {
            return "Literal(" + this.value + ")";
        }
    }

    final class Placeholder implements ArgumentString {
        private final String placeholder;
        private final String rawText;
        private final Object defaultValue;

        public Placeholder(String placeholder) {
            this.rawText = "{" + placeholder + "}";
            int first = placeholder.indexOf(':');
            if (first == -1) {
                this.placeholder = placeholder;
                this.defaultValue = this.rawText;
            } else {
                this.placeholder = placeholder.substring(0, first);
                try {
                    this.defaultValue = CraftEngine.instance().platform().nbt2Java(placeholder.substring(first + 1));
                } catch (LocalizedResourceConfigException e) {
                    e.appendTailArgument(this.placeholder);
                    throw e;
                }
            }
        }

        public static Placeholder placeholder(String placeholder) {
            return new Placeholder(placeholder);
        }

        @Override
        public Object get(Map<String, TemplateArgument> arguments) {
            TemplateArgument replacement = arguments.get(this.placeholder);
            if (replacement != null) {
                return replacement.get(arguments);
            }
            return this.defaultValue;
        }

        @Override
        public String rawValue() {
            return this.rawText;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Placeholder that)) return false;
            return this.placeholder.equals(that.placeholder);
        }

        @Override
        public int hashCode() {
            return this.placeholder.hashCode();
        }

        @Override
        public String toString() {
            return "Placeholder(" + this.placeholder + ")";
        }
    }

    final class Complex2 implements ArgumentString {
        private final String rawText;
        private final ArgumentString arg1;
        private final ArgumentString arg2;

        public Complex2(String rawText, ArgumentString arg1, ArgumentString arg2) {
            this.arg1 = arg1;
            this.arg2 = arg2;
            this.rawText = rawText;
        }

        @Override
        public Object get(Map<String, TemplateArgument> arguments) {
            Object arg1 = this.arg1.get(arguments);
            Object arg2 = this.arg2.get(arguments);
            if (arg1 == null && arg2 == null) return null;
            if (arg1 == null) return String.valueOf(arg2);
            if (arg2 == null) return String.valueOf(arg1);
            return String.valueOf(arg1) + arg2;
        }

        @Override
        public String rawValue() {
            return this.rawText;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Complex that)) return false;
            return this.rawText.equals(that.rawText);
        }

        @Override
        public int hashCode() {
            return this.rawText.hashCode();
        }

        @Override
        public String toString() {
            return "Complex2(" + this.rawText + ")";
        }
    }

    final class Complex implements ArgumentString {
        private final List<ArgumentString> parts;
        private final String rawText;

        public Complex(String rawText, List<ArgumentString> parts) {
            this.parts = parts;
            this.rawText = rawText;
        }

        @Override
        public Object get(Map<String, TemplateArgument> arguments) {
            StringBuilder result = new StringBuilder();
            boolean hasValue = false;
            for (ArgumentString part : this.parts) {
                Object arg = part.get(arguments);
                if (arg != null) {
                    result.append(arg);
                    hasValue = true;
                }
            }
            if (!hasValue) return null;
            return result.toString();
        }

        @Override
        public String rawValue() {
            return this.rawText;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Complex that)) return false;
            return this.rawText.equals(that.rawText);
        }

        @Override
        public int hashCode() {
            return this.rawText.hashCode();
        }

        @Override
        public String toString() {
            return "Complex(" + this.rawText + ")";
        }
    }

    static ArgumentString preParse(String input) {
        if (input == null || input.isEmpty()) {
            return Literal.literal("");
        }

        List<ArgumentString> arguments = new ArrayList<>();
        StringBuilder currentLiteral = new StringBuilder();
        final int n = input.length();
        int i = 0;

        while (i < n) {
            char c = input.charAt(i);

            // --- 1. 处理转义字符 ---
            if (c == '\\') {
                // 只在'\'后面是'{'或'}'时才作为转义处理
                if (i + 1 < n && (input.charAt(i + 1) == '{' || input.charAt(i + 1) == '}')) {
                    currentLiteral.append(input.charAt(i + 1)); // 添加花括号
                    i += 2; // 跳过'\'和花括号
                } else {
                    // 对于所有其他情况 (如 \\, \n), 将'\'视为普通字符
                    currentLiteral.append(c);
                    i++;
                }
                continue;
            }

            // --- 2. 处理占位符 {key} ---
            if (c == '{') {
                // 如果在占位符之前有普通文本，先提交它
                if (!currentLiteral.isEmpty()) {
                    arguments.add(Literal.literal(currentLiteral.toString()));
                    currentLiteral.setLength(0); // 清空
                }

                // 开始解析占位符内部
                StringBuilder keyBuilder = new StringBuilder();
                int depth = 1;
                int j = i + 1;
                boolean foundMatch = false;

                while (j < n) {
                    char innerChar = input.charAt(j);
                    if (innerChar == '\\') { // 处理占位符内部的转义
                        if (j + 1 < n && (input.charAt(j + 1) == '{' || input.charAt(j + 1) == '}')) {
                            keyBuilder.append(input.charAt(j + 1));
                            j += 2;
                        } else {
                            keyBuilder.append(innerChar); // 将'\'视为普通字符
                            j++;
                        }
                    } else if (innerChar == '{') {
                        depth++;
                        keyBuilder.append(innerChar);
                        j++;
                    } else if (innerChar == '}') {
                        depth--;
                        if (depth == 0) { // 找到匹配的闭合括号
                            arguments.add(Placeholder.placeholder(keyBuilder.toString()));
                            i = j + 1; // 将主循环的索引跳到占位符之后
                            foundMatch = true;
                            break;
                        }
                        keyBuilder.append(innerChar); // 嵌套的 '}'
                        j++;
                    } else {
                        keyBuilder.append(innerChar);
                        j++;
                    }
                }

                if (foundMatch) {
                    continue; // 成功解析占位符，继续主循环
                } else {
                    // 没有找到匹配的 '}'，将起始的 '{' 视为普通文本
                    currentLiteral.append(c);
                    i++;
                }
            } else {
                // --- 3. 处理普通字符 ---
                currentLiteral.append(c);
                i++;
            }
        }

        // 添加循环结束后剩余的任何普通文本
        if (!currentLiteral.isEmpty()) {
            arguments.add(Literal.literal(currentLiteral.toString()));
        }

        // 根据解析出的参数数量返回最终结果
        return switch (arguments.size()) {
            case 0 -> Literal.literal(""); // 处理 input = "{}" 等情况
            case 1 -> arguments.getFirst();
            case 2 -> new Complex2(input, arguments.get(0), arguments.get(1));
            default -> new Complex(input, arguments);
        };
    }
}
