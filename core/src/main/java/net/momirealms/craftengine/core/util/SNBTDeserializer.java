package net.momirealms.craftengine.core.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SNBTDeserializer {
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
    private static final char BOOLEAN_SUFFIX = 'B';

    // 使用 char[] 处理获得更高的性能
    private final char[] sourceContent;
    private final int length;
    private int cursor = 0;

    private SNBTDeserializer(String content) {
        this.sourceContent = content.toCharArray();
        this.length = sourceContent.length;
    }

    // 入口API
    public static Object deserializeAsJava(String input) throws IllegalArgumentException {
        SNBTDeserializer parser = new SNBTDeserializer(input);
        Object result = parser.parseValue();
        parser.skipWhitespace();

        if (parser.cursor != parser.length) {
            throw new IllegalArgumentException("Extra content at end: " +
                    new String(parser.sourceContent, parser.cursor,
                            parser.length - parser.cursor));
        }
        return result;
    }

    // 开始解析, 步进字符.
    private Object parseValue() {
        skipWhitespace();
        return switch (peekCurrentChar()) {
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

        if (cursor < length && sourceContent[cursor] != COMPOUND_END) {
            do {
                String key = parseKey();
                if (cursor >= length || sourceContent[cursor] != KEY_VALUE_SEPARATOR) {
                    throw new IllegalArgumentException("Expected ':' at position " + cursor);
                }
                cursor++; // 跳过 ':'
                Object value = parseValue();
                compoundMap.put(key, value);
                skipWhitespace();
            } while (cursor < length && sourceContent[cursor] == ELEMENT_SEPARATOR && ++cursor > 0);
        }

        if (cursor >= length || sourceContent[cursor] != COMPOUND_END) {
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

        if (cursor < length && sourceContent[cursor] != LIST_END) {
            do {
                elementList.add(parseValue());
                skipWhitespace();
            } while (cursor < length && sourceContent[cursor] == ELEMENT_SEPARATOR && ++cursor > 0);
        }

        if (cursor >= length || sourceContent[cursor] != LIST_END) {
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
            char c = sourceContent[cursor];
            if (c == delimiter) {
                String result = new String(sourceContent, start, cursor - start);
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
        sb.append(sourceContent, start, cursor - start);

        while (cursor < length) {
            char c = sourceContent[cursor++];
            if (c == ESCAPE_CHAR && cursor < length) {
                sb.append(getEscapedChar(sourceContent[cursor++])); // 解析转义.
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
            if (sourceContent[cursor] == STRING_DELIMITER) return parseString(STRING_DELIMITER);
            if (sourceContent[cursor] == SINGLE_QUOTES) return parseString(SINGLE_QUOTES);
        }

        int start = cursor;
        while (cursor < length) {
            char c = sourceContent[cursor];
            if (Character.isJavaIdentifierPart(c)) {
                cursor++;
            } else {
                break;
            }
        }

        skipWhitespace();
        return new String(sourceContent, start, cursor - start);
    }

    // 解析原生值
    private Object parsePrimitive() {
        // 先解析获取值的长度
        int tokenStart = cursor;
        while (cursor < length) {
            char c = sourceContent[cursor];
            if (c <= ' ' || c == ',' || c == ']' || c == '}') break;
            cursor++;
        }
        int tokenLength = cursor - tokenStart;
        if (tokenLength == 0) throw new IllegalArgumentException("Empty value at position " + tokenStart);

        // 布尔值快速检查
        if (tokenLength == 4 && matchesAt(tokenStart, "true")) return Boolean.TRUE;
        if (tokenLength == 5 && matchesAt(tokenStart, "false")) return Boolean.FALSE;

        // 无后缀数字处理
        if (isNumber(tokenStart, tokenStart + tokenLength) == 1) return Integer.parseInt(new String(sourceContent, tokenStart, tokenLength));
        if (isNumber(tokenStart, tokenStart + tokenLength) == 2) return Double.parseDouble(new String(sourceContent, tokenStart, tokenLength));

        // 带后缀的值处理
        char lastChar = sourceContent[tokenStart + tokenLength - 1];
        if (tokenLength > 1 &&                                      // 要求: 长度>1
            isTypeSuffix(lastChar) &&                               // 要求: 有效后缀
            isNumber(tokenStart, tokenStart + tokenLength - 1) > 0  // 要求: 除后缀外是合法数字
        ) {
            return switch (lastChar) {
                case BYTE_SUFFIX -> parseByte(tokenStart, tokenLength - 1);
                case SHORT_SUFFIX -> parseShort(tokenStart, tokenLength - 1);
                case LONG_SUFFIX -> parseLong(tokenStart, tokenLength - 1);
                case FLOAT_SUFFIX -> Float.parseFloat(new String(sourceContent, tokenStart, tokenLength - 1));
                case DOUBLE_SUFFIX -> Double.parseDouble(new String(sourceContent, tokenStart, tokenLength - 1));
                case BOOLEAN_SUFFIX -> parseBoolean(new String(sourceContent, tokenStart, tokenLength - 1));
                default -> new IllegalArgumentException("Parse Error with: " + new String(sourceContent, tokenStart, tokenLength - 1)); // 永远不应进入此 case.
            };
        }

        // 都无法匹配就默认为 String 喵~
        return new String(sourceContent, tokenStart, tokenLength);
    }

    // 工具函数: 快速检查布尔值字符串匹配, 忽略大小写.
    private boolean matchesAt(int start, String target) {
        for (int i = 0; i < target.length(); i++) {
            char c1 = sourceContent[start + i];
            char c2 = target.charAt(i);
            if (c1 != c2 && c1 != (c2 ^ 32)) return false; // 忽略大小写比较
        }
        return true;
    }

    // 工具函数: 合法后缀检查
    private boolean isTypeSuffix(char c) {
        return c == BYTE_SUFFIX || c == SHORT_SUFFIX || c == LONG_SUFFIX ||
                c == FLOAT_SUFFIX || c == DOUBLE_SUFFIX || c == BOOLEAN_SUFFIX;
    }


    // 手动解析值
    private byte parseByte(int start, int length) {
        int value = parseInteger(start, length);
        if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE)
            throw new IllegalArgumentException("Byte value out of range");
        return (byte) value;
    }

    private short parseShort(int start, int length) {
        int value = parseInteger(start, length);
        if (value < Short.MIN_VALUE || value > Short.MAX_VALUE)
            throw new NumberFormatException("Short value out of range");
        return (short) value;
    }

    private Boolean parseBoolean(String content) {
        if ("1".equals(content)) return Boolean.TRUE;
        if ("0".equals(content)) return Boolean.FALSE;
        throw new NumberFormatException("Invalid boolean value");
    }

    private int parseInteger(int start, int length) {
        int result = 0;
        boolean negative = false;
        int i = 0;

        if (sourceContent[start] == '-') {
            negative = true;
            i = 1;
        }

        for (; i < length; i++) {
            char c = sourceContent[start + i];
            if (c < '0' || c > '9') throw new NumberFormatException("Invalid integer");
            result = result * 10 + (c - '0');
        }

        return negative ? -result : result;
    }

    private long parseLong(int start, int length) {
        long result = 0;
        boolean negative = false;
        int i = 0;

        if (sourceContent[start] == '-') {
            negative = true;
            i = 1;
        }

        for (; i < length; i++) {
            char c = sourceContent[start + i];
            if (c < '0' || c > '9') throw new NumberFormatException("Invalid long");
            result = result * 10 + (c - '0');
        }

        return negative ? -result : result;
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

    // 获取当前字符
    private char peekCurrentChar() {
        if (cursor >= length) throw new IllegalArgumentException("Unexpected end of input at position " + cursor);
        return sourceContent[cursor];
    }

    // 跳过空格
    private void skipWhitespace() {
        while (cursor < length) {
            char c = sourceContent[cursor];
            if (c > ' ') { break; } // 大于空格的字符都不是空白字符
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                cursor++;
            } else {
                break;
            }
        }
    }

    // 检查是不是合法数字.
    // 返回0代表不合法, 1代表整数, 2代表小数.
    private int isNumber(int start, int end) {
        // 跳过负号
        if (sourceContent[start] == '-') start++;

        // 除负号外第一个字符必须是数字.
        char c1 = sourceContent[start];
        if (c1 < '0' || c1 > '9') return 0;

        // 检查剩余的数字, 只能有一个小数点
        boolean hasDecimal = false;
        for (; start < end; start++) {
            char c = sourceContent[start];
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
