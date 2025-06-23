package net.momirealms.craftengine.core.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SNBTDeserializer {

    private static final char COMPOUND_START = '{';
    private static final char COMPOUND_END = '}';
    private static final char LIST_START = '[';
    private static final char LIST_END = ']';
    private static final char STRING_DELIMITER = '"';
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

    // 布尔值常量
    private static final String TRUE_LITERAL = "true";
    private static final String FALSE_LITERAL = "false";
    private static final int TRUE_LENGTH = 4;
    private static final int FALSE_LENGTH = 5;

    // 使用 char[] 处理获得更高的性能
    private final char[] sourceContent;
    private final int length;
    private int position = 0;

    private SNBTDeserializer(String content) {
        this.sourceContent = content.toCharArray();
        this.length = sourceContent.length;
    }

    // 入口API
    public static Object parse(String input) throws IllegalArgumentException {
        SNBTDeserializer parser = new SNBTDeserializer(input);
        Object result = parser.parseValue();
        parser.skipWhitespace();

        if (parser.position != parser.length) {
            throw new IllegalArgumentException("Extra content at end: " +
                    new String(parser.sourceContent, parser.position,
                            parser.length - parser.position));
        }
        return result;
    }

    // 开始解析, 步进字符.
    private Object parseValue() {
        skipWhitespace();
        return switch (peekCurrentChar()) {
            case COMPOUND_START -> parseCompound();
            case LIST_START -> parseList();
            case STRING_DELIMITER -> parseString();
            default -> parsePrimitive();
        };
    }

    // 解析包小肠 {}
    private Map<String, Object> parseCompound() {
        position++; // 跳过 '{'
        skipWhitespace();

        Map<String, Object> compoundMap = new LinkedHashMap<>(16); // 避免一次扩容, 应该有一定的性能提升

        if (position < length && sourceContent[position] != COMPOUND_END) {
            do {
                String key = parseKey();
                if (position >= length || sourceContent[position] != KEY_VALUE_SEPARATOR) {
                    throw new IllegalArgumentException("Expected ':' at position " + position);
                }
                position++; // 跳过 ':'
                Object value = parseValue();
                compoundMap.put(key, value);
                skipWhitespace();
            } while (position < length && sourceContent[position] == ELEMENT_SEPARATOR && ++position > 0);
        }

        if (position >= length || sourceContent[position] != COMPOUND_END) {
            throw new IllegalArgumentException("Expected '}' at position " + position);
        }
        position++; // 跳过 '}'
        return compoundMap;
    }

    // 解析列表值 [1, 2, 3]
    private List<Object> parseList() {
        position++; // 跳过 '['
        skipWhitespace();
        List<Object> elementList = new ArrayList<>();

        if (position < length && sourceContent[position] != LIST_END) {
            do {
                elementList.add(parseValue());
                skipWhitespace();
            } while (position < length && sourceContent[position] == ELEMENT_SEPARATOR && ++position > 0);
        }

        if (position >= length || sourceContent[position] != LIST_END) {
            throw new IllegalArgumentException("Expected ']' at position " + position);
        }
        position++; // 跳过 ']'
        return elementList;
    }

    // 解析字符串
    private String parseString() {
        position++; // 跳过开始的引号
        int start = position;

        // 扫一次字符串, 如果没有发现转义就直接返回, 发现了就再走转义解析.
        // 这样可以避免创建一次 StringBuilder.
        while (position < length) {
            char c = sourceContent[position];
            if (c == STRING_DELIMITER) {
                String result = new String(sourceContent, start, position - start);
                position++; // 跳过结束引号
                return result; // 没有转义直接返回字符串.
            }
            // 如果发现转义字符，
            else if (c == ESCAPE_CHAR) {
                return parseStringWithEscape(start);
            }
            position++;
        }
        // 没有扫描到结束引号
        throw new IllegalArgumentException("Unterminated string at " + start);
    }

    // 处理含转义的字符串
    private String parseStringWithEscape(int start) {
        StringBuilder sb = new StringBuilder(position - start + 16);
        sb.append(sourceContent, start, position - start);

        while (position < length) {
            char c = sourceContent[position++];
            if (c == ESCAPE_CHAR && position < length) {
                sb.append(getEscapedChar(sourceContent[position++]));
            } else if (c == STRING_DELIMITER) { // 字符
                return sb.toString();
            } else {
                sb.append(c);
            }
        }
        // 没有扫描到结束引号
        throw new IllegalArgumentException("Unterminated string");
    }

    // 解析Key值
    private String parseKey() {
        skipWhitespace();
        // 如果有双引号就委托给string解析处理.
        if (position < length && sourceContent[position] == STRING_DELIMITER) {
            return parseString();
        }

        int start = position;
        while (position < length) {
            char c = sourceContent[position];
            if (Character.isJavaIdentifierPart(c)) {
                position++;
            } else {
                break;
            }
        }

        skipWhitespace();
        return new String(sourceContent, start, position - start);
    }

    // 解析原生值
    private Object parsePrimitive() {
        skipWhitespace();
        int tokenStart = position;

        // 先解析获取值的长度
        while (position < length) {
            char c = sourceContent[position];
            if (c <= ' ' || c == ',' || c == ']' || c == '}') break;
            position++;
        }
        int tokenLength = position - tokenStart;
        if (tokenLength == 0) {
            throw new IllegalArgumentException("Empty value at position " + tokenStart);
        }

        // 布尔值快速检查
        if (tokenLength == TRUE_LENGTH && matchesAt(tokenStart, TRUE_LITERAL)) {
            return Boolean.TRUE;
        }
        if (tokenLength == FALSE_LENGTH && matchesAt(tokenStart, FALSE_LITERAL)) {
            return Boolean.FALSE;
        }

        // 带后缀的值处理
        char lastChar = sourceContent[tokenStart + tokenLength - 1];
        if (tokenLength > 1 && isTypeSuffix(lastChar)) {
            String valueContent = new String(sourceContent, tokenStart, tokenLength - 1);
            return switch (lastChar) {
                case BYTE_SUFFIX -> parseByte(tokenStart, tokenLength - 1);
                case SHORT_SUFFIX -> parseShort(tokenStart, tokenLength - 1);
                case LONG_SUFFIX -> parseLong(tokenStart, tokenLength - 1);
                case FLOAT_SUFFIX -> Float.parseFloat(valueContent);
                case DOUBLE_SUFFIX -> Double.parseDouble(valueContent);
                case BOOLEAN_SUFFIX -> parseBoolean(valueContent);
                default -> Double.parseDouble(valueContent);
            };
        }

        // 手动解析Double数字
        return Double.parseDouble(new String(sourceContent, tokenStart, tokenLength));
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
            case STRING_DELIMITER -> '"';
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
        if (position >= length) throw new IllegalArgumentException("Unexpected end of input at position " + position);
        return sourceContent[position];
    }


    // 跳过空格 - 优化24：手动展开循环，处理常见情况
    private void skipWhitespace() {
        while (position < length) {
            char c = sourceContent[position];
            if (c > ' ') { break; } // 大于空格的字符都不是空白字符

            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                position++;
            } else {
                break;
            }
        }
    }

}
