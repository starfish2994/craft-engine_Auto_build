package net.momirealms.craftengine.core.plugin.config.template;

import net.momirealms.craftengine.core.pack.LoadingSequence;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.config.ConfigParser;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.GsonHelper;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;

@SuppressWarnings("DuplicatedCode")
public class TemplateManagerImpl implements TemplateManager {
    /*
     * 此类仍需要一次重构，对模板预解析，避免每次调用时候重新判断值是否含有参数
     */
    private final Map<Key, Object> templates = new HashMap<>();
    private final static Set<String> NON_TEMPLATE_KEY = new HashSet<>(Set.of(TEMPLATE, ARGUMENTS, OVERRIDES, MERGES));
    private final TemplateParser templateParser;

    public TemplateManagerImpl() {
        this.templateParser = new TemplateParser();
    }

    public class TemplateParser implements ConfigParser {
        public static final String[] CONFIG_SECTION_NAME = new String[] {"templates", "template"};

        @Override
        public String[] sectionId() {
            return CONFIG_SECTION_NAME;
        }

        @Override
        public int loadingSequence() {
            return LoadingSequence.TEMPLATE;
        }

        @Override
        public boolean supportsParsingObject() {
            return true;
        }

        @Override
        public void parseObject(Pack pack, Path path, Key id, Object obj) {
            if (templates.containsKey(id)) {
                throw new LocalizedResourceConfigException("warning.config.template.duplicate", path.toString(), id.toString());
            }
            templates.put(id, obj);
        }
    }

    @Override
    public void unload() {
        this.templates.clear();
    }

    @Override
    public ConfigParser parser() {
        return this.templateParser;
    }

    @Override
    public Map<String, Object> applyTemplates(Key id, Map<String, Object> input) {
        Objects.requireNonNull(input, "Input must not be null");
        Map<String, Object> result = new LinkedHashMap<>();
        processMap(input,
                Map.of("__ID__", PlainStringTemplateArgument.plain(id.value()),
                "__NAMESPACE__", PlainStringTemplateArgument.plain(id.namespace())),
                (obj) -> {
                    // 当前位于根节点下，如果下一级就是模板，则应把模板结果与当前map合并
                    // 如果模板结果不是map，则为非法值，因为不可能出现类似于下方的配置
                    // items:
                    //   test:invalid: 111
                    if (obj instanceof Map<?,?> mapResult) {
                        result.putAll(MiscUtils.castToMap(mapResult, false));
                    } else {
                        throw new IllegalArgumentException("Invalid template used. Input: " + GsonHelper.get().toJson(input) + ". Template: " + GsonHelper.get().toJson(obj));
                    }
        });
        return result;
    }

    // 对于处理map，只有input是已知map，而返回值可能并不是
    private void processMap(Map<String, Object> input,
                            Map<String, TemplateArgument> parentArguments,
                            // 只有当前为模板的时候，才会调用callback
                            Consumer<Object> processCallBack) {
        // 传入的input是否含有template，这种情况下，返回值有可能是非map
        if (input.containsKey(TEMPLATE)) {
            TemplateProcessingResult processingResult = processTemplates(input, parentArguments);
            List<Object> templates = processingResult.templates();
            // 你敢保证template里没有template吗？
            List<Object> processedTemplates = new ArrayList<>();
            // 先递归处理后再合并
            for (Object template : templates) {
                processUnknownTypeMember(template, processingResult.arguments(), processedTemplates::add);
            }
            if (processedTemplates.isEmpty()) {
                return;
            }
            Object firstTemplate = processedTemplates.get(0);
            // 如果是map，应当深度合并
            if (firstTemplate instanceof Map<?,?>) {
                Map<String, Object> results = new LinkedHashMap<>();
                for (Object processedTemplate : processedTemplates) {
                    if (processedTemplate instanceof Map<?, ?> anotherMap) {
                        deepMergeMaps(results, MiscUtils.castToMap(anotherMap, false));
                    }
                }
                if (processingResult.overrides() instanceof Map<?, ?> overrides) {
                    results.putAll(MiscUtils.castToMap(overrides, false));
                }
                if (processingResult.merges() instanceof Map<?, ?> merges) {
                    deepMergeMaps(results, MiscUtils.castToMap(merges, false));
                }
                processCallBack.accept(results);
            } else if (firstTemplate instanceof List<?>) {
                List<Object> results = new ArrayList<>();
                // 仅仅合并list
                for (Object processedTemplate : processedTemplates) {
                    if (processedTemplate instanceof List<?> anotherList) {
                        results.addAll(anotherList);
                    }
                }
                if (processingResult.overrides() instanceof List<?> overrides) {
                    results.clear();
                    results.addAll(overrides);
                }
                if (processingResult.merges() instanceof List<?> merges) {
                    results.addAll(merges);
                }
                processCallBack.accept(results);
            } else {
                Object overrides = processingResult.overrides();
                if (overrides != null) {
                    processCallBack.accept(overrides);
                } else {
                    // 其他情况下应当忽略其他的template
                    processCallBack.accept(firstTemplate);
                }
            }
        } else {
            // 如果不是模板，则返回值一定是map
            // 依次处理map下的每个参数
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<String, Object> inputEntry : input.entrySet()) {
                String key = applyArgument(inputEntry.getKey(), parentArguments).toString();
                processUnknownTypeMember(inputEntry.getValue(), parentArguments, (processed) -> result.put(key, processed));
            }
            processCallBack.accept(result);
        }
    }

    // 处理一个类型未知的值，本方法只管将member处理好后，传递回调用者
    private void processUnknownTypeMember(Object member,
                                          Map<String, TemplateArgument> parentArguments,
                                          Consumer<Object> processCallback) {
        if (member instanceof Map<?,?> innerMap) {
            // map下面还是个map吗？这并不一定
            // 比如
            // a:
            //   template: xxx
            // 这时候a并不一定是map，最终类型取决于template，那么应当根据template的结果进行调整，所以我们继续交给上方方法处理
            processMap(MiscUtils.castToMap(innerMap, false), parentArguments, processCallback);
        } else if (member instanceof List<?> innerList) {
            // map 下面是个list，那么对下面的每个成员再次处理
            List<Object> result = new ArrayList<>();
            for (Object item : innerList) {
                // 处理完以后，加入到list内
                processUnknownTypeMember(item, parentArguments, result::add);
            }
            processCallback.accept(result);
        } else if (member instanceof String possibleArgument) {
            // 如果是个string，其可能是 {xxx} 的参数，那么就尝试应用参数后再返回
            processCallback.accept(applyArgument(possibleArgument, parentArguments));
        } else {
            // 对于其他值，直接处理
            processCallback.accept(member);
        }
    }

    private TemplateProcessingResult processTemplates(Map<String, Object> input,
                                                      Map<String, TemplateArgument> parentArguments) {
        // 先获取template节点下所有的模板
        List<String> templateIds = MiscUtils.getAsStringList(input.get(TEMPLATE));
        List<Object> templateList = new ArrayList<>(templateIds.size());

        for (String templateId : templateIds) {
            // 如果模板id被用了参数，则应先应用参数后再查询模板
            Object actualTemplate = applyArgument(templateId, parentArguments);
            if (actualTemplate == null) continue; // 忽略被null掉的模板
            Object template = Optional.ofNullable(this.templates.get(Key.of(actualTemplate.toString())))
                    .orElseThrow(() -> new IllegalArgumentException("Template not found: " + actualTemplate));
            templateList.add(template);
        }

        Object argument = input.get(ARGUMENTS);
        boolean hasArgument = argument != null;

        // 将本节点下的参数与父参数合并
        Map<String, TemplateArgument> arguments = !hasArgument ? parentArguments : mergeArguments(
                MiscUtils.castToMap(argument, false),
                parentArguments
        );

        Object override = input.get(OVERRIDES);
        if (override instanceof Map<?, ?> rawOverrides) {
            // 对overrides参数应用 本节点 + 父节点 参数
            Map<String, Object> overrides = new LinkedHashMap<>();
            processMap(MiscUtils.castToMap(rawOverrides, false), arguments, (obj) -> {
                // 如果overrides的下一级就是一个模板，则模板必须为map类型
                if (obj instanceof Map<?,?> mapResult) {
                    overrides.putAll(MiscUtils.castToMap(mapResult, false));
                } else {
                    throw new IllegalArgumentException("Invalid template used. Input: " + GsonHelper.get().toJson(input) + ". Template: " + GsonHelper.get().toJson(obj));
                }
            });
            // overrides是map了，merges也只能是map
            if (input.get(MERGES) instanceof Map<?, ?> rawMerges) {
                Map<String, Object> merges = new LinkedHashMap<>();
                processMap(MiscUtils.castToMap(rawMerges, false), arguments, (obj) -> {
                    // 如果merges的下一级就是一个模板，则模板必须为map类型
                    if (obj instanceof Map<?,?> mapResult) {
                        merges.putAll(MiscUtils.castToMap(mapResult, false));
                    } else {
                        throw new IllegalArgumentException("Invalid template used. Input: " + GsonHelper.get().toJson(input) + ". Template: " + GsonHelper.get().toJson(obj));
                    }
                });
                // 已有template，merges，overrides 和可选的arguments
                if (input.size() > (hasArgument ? 4 : 3)) {
                    // 会不会有一种可能，有笨比用户把模板和普通配置混合在了一起？再次遍历input后处理
                    for (Map.Entry<String, Object> inputEntry : input.entrySet()) {
                        String inputKey = inputEntry.getKey();
                        if (NON_TEMPLATE_KEY.contains(inputKey)) continue;
                        processUnknownTypeMember(inputEntry.getValue(), arguments, (processed) -> merges.put(inputKey, processed));
                    }
                }
                // 返回处理结果
                return new TemplateProcessingResult(
                        templateList,
                        overrides,
                        merges,
                        arguments
                );
            } else {
                // 已有template，overrides 和可选的arguments
                if (input.size() > (hasArgument ? 3 : 2)) {
                    Map<String, Object> merges = new LinkedHashMap<>();
                    // 会不会有一种可能，有笨比用户把模板和普通配置混合在了一起？再次遍历input后处理
                    for (Map.Entry<String, Object> inputEntry : input.entrySet()) {
                        String inputKey = inputEntry.getKey();
                        if (NON_TEMPLATE_KEY.contains(inputKey)) continue;
                        processUnknownTypeMember(inputEntry.getValue(), arguments, (processed) -> merges.put(inputKey, processed));
                    }
                    return new TemplateProcessingResult(
                            templateList,
                            overrides,
                            merges,
                            arguments
                    );
                } else {
                    return new TemplateProcessingResult(
                            templateList,
                            overrides,
                            null,
                            arguments
                    );
                }
            }
        } else if (override instanceof List<?> overrides) {
            // overrides不为空，且不是map
            List<Object> processedOverrides = new ArrayList<>(overrides.size());
            for (Object item : overrides) {
                processUnknownTypeMember(item, arguments, processedOverrides::add);
            }
            if (input.get(MERGES) instanceof List<?> rawMerges) {
                List<Object> merges = new ArrayList<>(rawMerges.size());
                for (Object item : rawMerges) {
                    processUnknownTypeMember(item, arguments, merges::add);
                }
                return new TemplateProcessingResult(
                        templateList,
                        processedOverrides,
                        merges,
                        arguments
                );
            } else {
                return new TemplateProcessingResult(
                        templateList,
                        processedOverrides,
                        null,
                        arguments
                );
            }
        } else if (override instanceof String rawOverride) {
            return new TemplateProcessingResult(
                    templateList,
                    applyArgument(rawOverride, arguments),
                    null,
                    arguments
            );
        } else if (override != null) {
            // overrides不为空，且不是map,list。此情况不用再考虑merge了
            return new TemplateProcessingResult(
                    templateList,
                    override,
                    null,
                    arguments
            );
        } else {
            // 获取merges
            Object merge = input.get(MERGES);
            if (merge instanceof Map<?, ?> rawMerges) {
                Map<String, Object> merges = new LinkedHashMap<>();
                processMap(MiscUtils.castToMap(rawMerges, false), arguments, (obj) -> {
                    // 如果merges的下一级就是一个模板，则模板必须为map类型
                    if (obj instanceof Map<?,?> mapResult) {
                        merges.putAll(MiscUtils.castToMap(mapResult, false));
                    } else {
                        throw new IllegalArgumentException("Invalid template used. Input: " + GsonHelper.get().toJson(input) + ". Template: " + GsonHelper.get().toJson(obj));
                    }
                });
                // 已有template和merges 和可选的arguments
                if (input.size() > (hasArgument ? 3 : 2)) {
                    // 会不会有一种可能，有笨比用户把模板和普通配置混合在了一起？再次遍历input后处理
                    for (Map.Entry<String, Object> inputEntry : input.entrySet()) {
                        String inputKey = inputEntry.getKey();
                        if (NON_TEMPLATE_KEY.contains(inputKey)) continue;
                        processUnknownTypeMember(inputEntry.getValue(), arguments, (processed) -> merges.put(inputKey, processed));
                    }
                }
                return new TemplateProcessingResult(
                        templateList,
                        null,
                        merges,
                        arguments
                );
            } else if (merge instanceof List<?> rawMerges) {
                List<Object> merges = new ArrayList<>(rawMerges.size());
                for (Object item : rawMerges) {
                    processUnknownTypeMember(item, arguments, merges::add);
                }
                return new TemplateProcessingResult(
                        templateList,
                        null,
                        merges,
                        arguments
                );
            } else if (merge instanceof String rawMerge) {
                // merge是个string
                return new TemplateProcessingResult(
                        templateList,
                        null,
                        applyArgument(rawMerge, arguments),
                        arguments
                );
            } else if (merge != null) {
                // merge是个普通的类型
                return new TemplateProcessingResult(
                        templateList,
                        null,
                        merge,
                        arguments
                );
            } else {
                // 无overrides和merges
                // 会不会有一种可能，有笨比用户不会使用merges，把模板和普通配置混合在了一起？再次遍历input后处理
                if (input.size() > (hasArgument ? 2 : 1)) {
                    Map<String, Object> merges = new LinkedHashMap<>();
                    for (Map.Entry<String, Object> inputEntry : input.entrySet()) {
                        String inputKey = inputEntry.getKey();
                        if (NON_TEMPLATE_KEY.contains(inputKey)) continue;
                        processUnknownTypeMember(inputEntry.getValue(), arguments, (processed) -> merges.put(inputKey, processed));
                    }
                    return new TemplateProcessingResult(
                            templateList,
                            null,
                            merges,
                            arguments
                    );
                } else {
                    return new TemplateProcessingResult(
                            templateList,
                            null,
                            null,
                            arguments
                    );
                }
            }
        }
    }

    // 合并参数
    private Map<String, TemplateArgument> mergeArguments(@NotNull Map<String, Object> rawChildArguments,
                                                         @NotNull Map<String, TemplateArgument> parentArguments) {
        Map<String, TemplateArgument> result = new HashMap<>(parentArguments);
        // 我们遍历一下当前节点下的所有参数，这些参数可能含有内嵌参数。所以需要对参数map先处理一次后再合并
        // arguments:
        //   argument_1: "{parent_argument}"
        for (Map.Entry<String, Object> argumentEntry : rawChildArguments.entrySet()) {
            // 获取最终的string形式参数
            String placeholder = applyArgument(argumentEntry.getKey(), parentArguments).toString();
            // 父亲参数最大
            if (result.containsKey(placeholder)) continue;
            Object rawArgument = argumentEntry.getValue();
            if (rawArgument instanceof Map<?,?> mapArgument) {
                // 此参数是一个map，那么对map应用模板，然后再根据map是否含有type等参数，判别其是否为带名特殊参数
                Map<String, Object> nestedResult = new LinkedHashMap<>();
                processMap(MiscUtils.castToMap(mapArgument, false), parentArguments, (obj) -> {
                    // 如果有人往arguments下塞了一个模板，则模板类型应为map
                    if (obj instanceof Map<?,?> mapResult) {
                        nestedResult.putAll(MiscUtils.castToMap(mapResult, false));
                    } else {
                        throw new IllegalArgumentException("Invalid template used. Input: " + GsonHelper.get().toJson(mapArgument) + ". Template: " + GsonHelper.get().toJson(obj));
                    }
                });
                result.put(placeholder, TemplateArguments.fromMap(nestedResult));
            } else if (rawArgument instanceof List<?> listArgument) {
                // 此参数是一个list，那么只需要应用模板即可
                List<Object> nestedResult = new ArrayList<>();
                for (Object item : listArgument) {
                    processUnknownTypeMember(item, parentArguments, nestedResult::add);
                }
                result.put(placeholder, new ListTemplateArgument(nestedResult));
            } else if (rawArgument == null) {
                // 使用 null 覆写其父参数内容
                result.put(placeholder, NullTemplateArgument.INSTANCE);
            } else if (rawArgument instanceof Number number) {
                result.put(placeholder, new ObjectTemplateArgument(number));
            } else if (rawArgument instanceof Boolean booleanValue) {
                result.put(placeholder, new ObjectTemplateArgument(booleanValue));
            } else {
                // 将参数字符串化后，应用参数再放入
                Object applied = applyArgument(rawArgument.toString(), parentArguments);
                result.put(placeholder, new ObjectTemplateArgument(applied));
            }
        }
        return result;
    }

    // 将某个输入变成最终的结果，可以是string->string，也可以是string->map/list
    private Object applyArgument(String input, Map<String, TemplateArgument> arguments) {
        // 如果字符串长度连3都没有，那么肯定没有{}啊
        if (input.length() < 3) return input;
        if (input.charAt(0) == '{' && input.charAt(input.length() - 1) == '}') {
            String key = input.substring(1, input.length() - 1);
            return Optional.ofNullable(arguments.get(key))
                    .map(TemplateArgument::get)
                    .orElseGet(() -> replacePlaceholders(input, arguments));
        }
        return replacePlaceholders(input, arguments);
    }

    private record TemplateProcessingResult(
            List<Object> templates,
            Object overrides,
            Object merges,
            Map<String, TemplateArgument> arguments
    ) {}

    @SuppressWarnings("unchecked")
    private void deepMergeMaps(Map<String, Object> baseMap, Map<String, Object> mapToMerge) {
        for (Map.Entry<String, Object> entry : mapToMerge.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (baseMap.containsKey(key)) {
                Object existingValue = baseMap.get(key);
                if (existingValue instanceof Map && value instanceof Map) {
                    Map<String, Object> existingMap = (Map<String, Object>) existingValue;
                    Map<String, Object> newMap = (Map<String, Object>) value;
                    deepMergeMaps(existingMap, newMap);
                } else if (existingValue instanceof List && value instanceof List) {
                    List<Object> existingList = (List<Object>) existingValue;
                    List<Object> newList = (List<Object>) value;
                    existingList.addAll(newList);
                } else {
                    baseMap.put(key, value);
                }
            } else {
                baseMap.put(key, value);
            }
        }
    }

    public static String replacePlaceholders(String input, Map<String, TemplateArgument> replacements) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        StringBuilder finalResult = new StringBuilder();
        int n = input.length();
        int lastAppendPosition = 0; // 追踪上一次追加操作结束的位置
        int i = 0;
        while (i < n) {
            // 检查当前字符是否为未转义的 '{'
            int backslashes = 0;
            int temp_i = i - 1;
            while (temp_i >= 0 && input.charAt(temp_i) == '\\') {
                backslashes++;
                temp_i--;
            }
            if (input.charAt(i) == '{' && backslashes % 2 == 0) {
                // 发现占位符起点
                int placeholderStartIndex = i;
                // 追加从上一个位置到当前占位符之前的文本
                finalResult.append(input, lastAppendPosition, placeholderStartIndex);
                // --- 开始解析占位符内部 ---
                StringBuilder keyBuilder = new StringBuilder();
                int depth = 1;
                int j = i + 1;
                boolean foundMatch = false;
                while (j < n) {
                    char c = input.charAt(j);
                    if (c == '\\') { // 处理转义
                        if (j + 1 < n) {
                            keyBuilder.append(input.charAt(j + 1));
                            j += 2;
                        } else {
                            keyBuilder.append(c);
                            j++;
                        }
                    } else if (c == '{') {
                        depth++;
                        keyBuilder.append(c);
                        j++;
                    } else if (c == '}') {
                        depth--;
                        if (depth == 0) { // 找到匹配的结束括号
                            String key = keyBuilder.toString();
                            TemplateArgument value = replacements.get(key);
                            if (value != null) {
                                // 如果在 Map 中找到值，则进行替换
                                finalResult.append(value.get());
                            } else {
                                // 否则，保留原始占位符（包括 '{}'）
                                finalResult.append(input, placeholderStartIndex, j + 1);
                            }
                            // 更新位置指针
                            i = j + 1;
                            lastAppendPosition = i;
                            foundMatch = true;
                            break;
                        }
                        keyBuilder.append(c); // 嵌套的 '}'
                        j++;
                    } else {
                        keyBuilder.append(c);
                        j++;
                    }
                }
                // --- 占位符解析结束 ---
                if (!foundMatch) {
                    // 如果内层循环结束仍未找到匹配的 '}'，则不进行任何特殊处理
                    // 外层循环的 i 会自然递增
                    i++;
                }
            } else {
                i++;
            }
        }
        // 追加最后一个占位符之后的所有剩余文本
        if (lastAppendPosition < n) {
            finalResult.append(input, lastAppendPosition, n);
        }
        return finalResult.toString();
    }
}
