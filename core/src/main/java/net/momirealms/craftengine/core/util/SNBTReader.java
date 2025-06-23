package net.momirealms.craftengine.core.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SNBTReader extends DefaultStringReader {
    private static final char COMPOUND_START = '{';
    private static final char COMPOUND_END = '}';
    private static final char LIST_START = '[';
    private static final char LIST_END = ']';
    private static final char STRING_DELIMITER = '"';
    private static final char SINGLE_QUOTES = '\'';
    private static final char DOUBLE_QUOTES = '"';
    private static final char KEY_VALUE_SEPARATOR = ':';
    private static final char ELEMENT_SEPARATOR = ',';
    private static final char ESCAPE_CHAR = '\\';

    // 数字类型后缀
    private static final char BYTE_SUFFIX = 'b';
    private static final char SHORT_SUFFIX = 's';
    private static final char LONG_SUFFIX = 'l';
    private static final char FLOAT_SUFFIX = 'f';
    private static final char DOUBLE_SUFFIX = 'd';

    private final int length;
    private SNBTReader(String content) {
        super(content);
        this.length = this.string.length();
    }

    // 入口API
    public static Object deserializeAsJava(String input) throws IllegalArgumentException {
        SNBTReader parser = new SNBTReader(input);
        Object result = parser.parseValue();
        parser.skipWhitespace();

        if (parser.cursor != parser.length)
            throw new IllegalArgumentException("Extra content at end: " + parser.string.substring(parser.cursor, parser.length - parser.cursor));

        return result;
    }

    // 开始解析, 步进字符.
    private Object parseValue() {
        skipWhitespace();
        return switch (peek()) {
            case COMPOUND_START -> parseCompound();
            case LIST_START -> parseList();
            case DOUBLE_QUOTES -> parseString(DOUBLE_QUOTES);
            case SINGLE_QUOTES -> parseString(SINGLE_QUOTES);
            default -> parsePrimitive();
        };
    }

    // 解析包小肠 {}
    private Map<String, Object> parseCompound() {
        cursor++; // 跳过 '{'
        skipWhitespace();

        Map<String, Object> compoundMap = new LinkedHashMap<>(16); // 避免一次扩容, 应该有一定的性能提升

        if (cursor < length && peek() != COMPOUND_END) {
            do {
                String key = parseKey();
                if (cursor >= length || peek() != KEY_VALUE_SEPARATOR) {
                    throw new IllegalArgumentException("Expected ':' at position " + cursor);
                }
                cursor++; // 跳过 ':'
                Object value = parseValue();
                compoundMap.put(key, value);
                skipWhitespace();
            } while (cursor < length && peek() == ELEMENT_SEPARATOR && ++cursor > 0);
        }

        if (cursor >= length || peek() != COMPOUND_END) {
            throw new IllegalArgumentException("Expected '}' at position " + cursor);
        }
        cursor++; // 跳过 '}'
        return compoundMap;
    }

    // 解析列表值 [1, 2, 3]
    private List<Object> parseList() {
        cursor++; // 跳过 '['
        skipWhitespace();
        List<Object> elementList = new ArrayList<>();

        if (cursor < length && peek() != LIST_END) {
            do {
                elementList.add(parseValue());
                skipWhitespace();
            } while (cursor < length && peek() == ELEMENT_SEPARATOR && ++cursor > 0);
        }

        if (cursor >= length || peek() != LIST_END) {
            throw new IllegalArgumentException("Expected ']' at position " + cursor);
        }
        cursor++; // 跳过 ']'
        return elementList;
    }

    // 解析字符串
    private String parseString(char delimiter) {
        cursor++; // 跳过开始的引号
        int start = cursor;

        // 扫一次字符串, 如果没有发现转义就直接返回, 发现了就再走转义解析.
        // 这样可以避免创建一次 StringBuilder.
        while (cursor < length) {
            char c = peek();
            if (c == delimiter) {
                String result = string.substring(start, cursor);
                cursor++; // 跳过结束引号
                return result; // 没有转义直接返回字符串.
            }
            // 如果发现转义字符，进行转义处理.
            else if (c == ESCAPE_CHAR) {
                return parseStringWithEscape(start, delimiter);
            }
            cursor++;
        }
        // 没有扫描到结束引号
        throw new IllegalArgumentException("Unterminated string at " + start);
    }

    // 处理含转义的字符串
    private String parseStringWithEscape(int start, char delimiter) {
        // 先把之前遍历没有包含转义的字符保存
        StringBuilder sb = new StringBuilder(cursor - start + 16);
        sb.append(string, start, cursor);

        while (cursor < length) {
            char c = read();
            if (c == ESCAPE_CHAR && cursor < length) {
                sb.append(getEscapedChar(read())); // 解析转义.
            } else if (c == delimiter) { // 发现结束分隔字符, 返回.
                return sb.toString();
            } else {
                sb.append(c);
            }
        }
        // 没有扫描到结束引号
        throw new IllegalArgumentException("Unterminated string at " + start);
    }

    // 解析Key值
    private String parseKey() {
        skipWhitespace();
        // 如果有双引号就委托给string解析处理.
        if (cursor < length) {
            if (peek() == STRING_DELIMITER) return parseString(STRING_DELIMITER);
            if (peek() == SINGLE_QUOTES) return parseString(SINGLE_QUOTES);
        }

        int start = cursor;
        while (cursor < length) {
            if (Character.isJavaIdentifierPart(peek())) cursor++; else break;
        }

        skipWhitespace();
        return string.substring(start, cursor);
    }

    // 解析原生值
    private Object parsePrimitive() {
        // 先解析获取值的长度
        int tokenStart = cursor;
        while (cursor < length) {
            char c = peek();
            if (c <= ' ' || c == ',' || c == ']' || c == '}') break;
            cursor++;
        }
        int tokenLength = cursor - tokenStart;
        if (tokenLength == 0) throw new IllegalArgumentException("Empty value at position " + tokenStart);
        String fullContent = string.substring(tokenStart, tokenStart + tokenLength);

        // 布尔值快速检查
        if ("1B".equals(fullContent) || (tokenLength == 4 && matchesAt(tokenStart, "true"))) return Boolean.TRUE;
        if ("0B".equals(fullContent) || (tokenLength == 5 && matchesAt(tokenStart, "false"))) return Boolean.FALSE;

        // 无后缀数字处理
        if (isNumber(tokenStart, tokenStart + tokenLength) == 1) return Integer.parseInt(fullContent);
        if (isNumber(tokenStart, tokenStart + tokenLength) == 2) return Double.parseDouble(fullContent);

        // 带后缀的值处理
        char lastChar = string.charAt(tokenStart + tokenLength - 1);
        if (tokenLength > 1 &&                                      // 要求: 长度>1
            isTypeSuffix(lastChar) &&                               // 要求: 有效后缀
            isNumber(tokenStart, tokenStart + tokenLength - 1) > 0  // 要求: 除后缀外是合法数字
        ) {
            final String content = string.substring(tokenStart, tokenStart + tokenLength - 1);
            try {
                return switch (lastChar) {
                    case BYTE_SUFFIX -> Byte.parseByte(content);
                    case SHORT_SUFFIX -> Short.parseShort(content);
                    case LONG_SUFFIX -> Long.parseLong(content);
                    case FLOAT_SUFFIX -> Float.parseFloat(content);
                    case DOUBLE_SUFFIX -> Double.parseDouble(content);
                    default -> new IllegalArgumentException("Parse Error with: " + content); // 永远不应进入此 case.
                };
            } catch (NumberFormatException e) {
                return fullContent; // 如果有神人写了 128b 一类的超范围值, 就当做字符串返回好了.
            }
        }

        // 都无法匹配就默认为 String 喵~
        return fullContent;
    }

    // 工具函数: 快速检查布尔值字符串匹配, 忽略大小写.
    private boolean matchesAt(int start, String target) {
        for (int i = 0; i < target.length(); i++) {
            char c1 = string.charAt(start + i);
            char c2 = target.charAt(i);
            if (c1 != c2 && c1 != (c2 ^ 32)) return false; // 忽略大小写比较
        }
        return true;
    }

    // 工具函数: 合法后缀检查
    private boolean isTypeSuffix(char c) {
        return c == BYTE_SUFFIX || c == SHORT_SUFFIX || c == LONG_SUFFIX || c == FLOAT_SUFFIX || c == DOUBLE_SUFFIX;
    }

    // 转义字符处理
    private char getEscapedChar(char escapedChar) {
        return switch (escapedChar) {
            case DOUBLE_QUOTES -> '"';
            case SINGLE_QUOTES -> '\'';
            case ESCAPE_CHAR -> '\\';
            case COMPOUND_START -> '{';
            case COMPOUND_END -> '}';
            case LIST_START -> '[';
            case LIST_END -> ']';
            case KEY_VALUE_SEPARATOR -> ':';
            case ELEMENT_SEPARATOR -> ',';
            default -> escapedChar;
        };
    }

    // 检查是不是合法数字.
    // 返回0代表不合法, 1代表整数, 2代表小数.
    private int isNumber(int start, int end) {
        // 跳过负号
        if (string.charAt(start) == '-') start++;

        // 除负号外第一个字符必须是数字.
        char c1 = string.charAt(start);
        if (c1 < '0' || c1 > '9') return 0;

        // 检查剩余的数字, 只能有一个小数点
        boolean hasDecimal = false;
        for (; start < end; start++) {
            char c = string.charAt(start);
            if (c < '0' || c > '9') {
                if (c == '.') {
                    if (hasDecimal) return 0; // bro 不能有2个小数点.
                    hasDecimal = true;
                } else {
                    return 0;
                }
            }
        }

        return hasDecimal ? 2 : 1;
    }

}
