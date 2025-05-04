package net.momirealms.craftengine.core.plugin.context;

import net.momirealms.craftengine.core.plugin.context.parameter.CommonParameterProvider;

import java.util.List;
import java.util.Optional;

public abstract class AbstractAdditionalCommonContext extends AbstractCommonContext {
    private final List<LazyContextParameterProvider> providers;

    public AbstractAdditionalCommonContext(ContextHolder contexts, List<LazyContextParameterProvider> providers) {
        super(contexts);
        this.providers = providers;
    }

    public AbstractAdditionalCommonContext(ContextHolder contexts) {
        super(contexts);
        this.providers = List.of(new CommonParameterProvider());
    }

    @Override
    public <T> Optional<T> getOptionalParameter(ContextKey<T> parameter) {
        for (LazyContextParameterProvider provider : providers) {
            Optional<T> result = provider.getOptionalParameter(parameter);
            if (result.isPresent()) {
                return result;
            }
        }
        return super.getOptionalParameter(parameter);
    }

    @Override
    public <T> T getParameterOrThrow(ContextKey<T> parameter) {
        for (LazyContextParameterProvider provider : providers) {
            Optional<T> result = provider.getOptionalParameter(parameter);
            if (result.isPresent()) {
                return result.get();
            }
        }
        return super.getParameterOrThrow(parameter);
    }
}
