package net.momirealms.craftengine.core.plugin;

import net.momirealms.craftengine.core.block.BlockManager;
import net.momirealms.craftengine.core.entity.furniture.FurnitureManager;
import net.momirealms.craftengine.core.font.FontManager;
import net.momirealms.craftengine.core.item.ItemManager;
import net.momirealms.craftengine.core.item.recipe.RecipeManager;
import net.momirealms.craftengine.core.loot.VanillaLootManager;
import net.momirealms.craftengine.core.pack.PackManager;
import net.momirealms.craftengine.core.pack.host.ResourcePackHost;
import net.momirealms.craftengine.core.plugin.classpath.ClassPathAppender;
import net.momirealms.craftengine.core.plugin.command.CraftEngineCommandManager;
import net.momirealms.craftengine.core.plugin.command.sender.SenderFactory;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.config.template.TemplateManager;
import net.momirealms.craftengine.core.plugin.config.template.TemplateManagerImpl;
import net.momirealms.craftengine.core.plugin.dependency.Dependencies;
import net.momirealms.craftengine.core.plugin.dependency.Dependency;
import net.momirealms.craftengine.core.plugin.dependency.DependencyManager;
import net.momirealms.craftengine.core.plugin.dependency.DependencyManagerImpl;
import net.momirealms.craftengine.core.plugin.gui.GuiManager;
import net.momirealms.craftengine.core.plugin.gui.category.ItemBrowserManager;
import net.momirealms.craftengine.core.plugin.gui.category.ItemBrowserManagerImpl;
import net.momirealms.craftengine.core.plugin.locale.TranslationManager;
import net.momirealms.craftengine.core.plugin.locale.TranslationManagerImpl;
import net.momirealms.craftengine.core.plugin.logger.PluginLogger;
import net.momirealms.craftengine.core.plugin.logger.filter.DisconnectLogFilter;
import net.momirealms.craftengine.core.plugin.logger.filter.LogFilter;
import net.momirealms.craftengine.core.plugin.network.NetworkManager;
import net.momirealms.craftengine.core.plugin.scheduler.SchedulerAdapter;
import net.momirealms.craftengine.core.sound.SoundManager;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.WorldManager;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class CraftEngine implements Plugin {
    public static final String MOD_CLASS = "net.momirealms.craftengine.mod.CraftEnginePlugin";
    public static final String NAMESPACE = "craftengine";
    private static CraftEngine instance;
    protected PluginLogger logger;
    protected Consumer<Supplier<String>> debugger = (s) -> {};
    protected Config config;
    protected ClassPathAppender classPathAppender;
    protected DependencyManager dependencyManager;
    protected SchedulerAdapter<?> scheduler;
    protected NetworkManager networkManager;
    protected FontManager fontManager;
    protected PackManager packManager;
    protected ItemManager<?> itemManager;
    protected RecipeManager<?> recipeManager;
    protected BlockManager blockManager;
    protected TranslationManager translationManager;
    protected WorldManager worldManager;
    protected FurnitureManager furnitureManager;
    protected CraftEngineCommandManager<?> commandManager;
    protected SenderFactory<? extends Plugin, ?> senderFactory;
    protected TemplateManager templateManager;
    protected ItemBrowserManager itemBrowserManager;
    protected GuiManager guiManager;
    protected SoundManager soundManager;
    protected VanillaLootManager vanillaLootManager;

    private final Consumer<CraftEngine> reloadEventDispatcher;
    private boolean isReloading;

    private String buildByBit = "%%__BUILTBYBIT__%%";
    private String polymart = "%%__POLYMART__%%";
    private String time = "%%__TIMESTAMP__%%";
    private String user = "%%__USER__%%";
    private String username = "%%__USERNAME__%%";

    protected CraftEngine(Consumer<CraftEngine> reloadEventDispatcher) {
        instance = this;
        this.reloadEventDispatcher = reloadEventDispatcher;
        VersionHelper.init(serverVersion());
    }

    public static CraftEngine instance() {
        if (instance == null) {
            throw new IllegalStateException("CraftEngine has not been initialized");
        }
        return instance;
    }

    public void onPluginLoad() {
        ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addFilter(new LogFilter());
        ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addFilter(new DisconnectLogFilter());
        this.dependencyManager = new DependencyManagerImpl(this);
        ArrayList<Dependency> dependenciesToLoad = new ArrayList<>();
        dependenciesToLoad.addAll(commonDependencies());
        dependenciesToLoad.addAll(platformDependencies());
        this.dependencyManager.loadDependencies(dependenciesToLoad);
        this.translationManager = new TranslationManagerImpl(this);
        this.config = new Config(this);
    }

    public record ReloadResult(boolean success, long asyncTime, long syncTime) {

        static ReloadResult failure() {
            return new ReloadResult(false, -1L, -1L);
        }

        static ReloadResult success(long asyncTime, long syncTime) {
            return new ReloadResult(true, asyncTime, syncTime);
        }
    }

    public CompletableFuture<ReloadResult> reloadPlugin(Executor asyncExecutor, Executor syncExecutor, boolean reloadRecipe) {
        CompletableFuture<ReloadResult> future = new CompletableFuture<>();
        asyncExecutor.execute(() -> {
            if (this.isReloading) {
                future.complete(ReloadResult.failure());
                return;
            }
            this.isReloading = true;
            long time1 = System.currentTimeMillis();
            // firstly reload main config
            this.config.load();
            // reset debugger
            this.debugger = Config.debug() ? (s) -> logger.info("[Debug] " + s.get()) : (s) -> {};
            // now we reload the translations
            this.translationManager.reload();
            // clear the outdated cache by reloading the managers
            this.templateManager.reload();
            this.furnitureManager.reload();
            this.fontManager.reload();
            this.itemManager.reload();
            this.soundManager.reload();
            this.itemBrowserManager.reload();
            this.blockManager.reload();
            this.worldManager.reload();
            this.vanillaLootManager.reload();
            this.guiManager.reload();
            this.packManager.reload();
            if (reloadRecipe) {
                this.recipeManager.reload();
            }
            // now we load resources
            this.packManager.loadResources(reloadRecipe);
            // handle some special client lang for instance block_name
            this.translationManager.delayedLoad();
            // init suggestions and packet mapper
            this.blockManager.delayedLoad();
            // init suggestions
            this.furnitureManager.delayedLoad();
            // sort the categories
            this.itemBrowserManager.delayedLoad();
            // collect illegal characters from minecraft:default font
            this.fontManager.delayedLoad();
            if (reloadRecipe) {
                // convert data pack recipes
                this.recipeManager.delayedLoad();
            }
            long time2 = System.currentTimeMillis();
            long asyncTime = time2 - time1;
            syncExecutor.execute(() -> {
                try {
                    long time3 = System.currentTimeMillis();
                    // register songs
                    this.soundManager.runDelayedSyncTasks();
                    // register recipes
                    if (reloadRecipe) {
                        this.recipeManager.runDelayedSyncTasks();
                    }
                    long time4 = System.currentTimeMillis();
                    long syncTime = time4 - time3;
                    this.reloadEventDispatcher.accept(this);
                    future.complete(ReloadResult.success(asyncTime, syncTime));
                } finally {
                    this.isReloading = false;
                }
            });
        });
        return future;
    }

    public void onPluginEnable() {
        this.networkManager.init();
        this.templateManager = new TemplateManagerImpl();
        this.itemBrowserManager = new ItemBrowserManagerImpl(this);
        this.commandManager.registerDefaultFeatures();
        // delay the reload so other plugins can register some custom parsers
        this.scheduler.sync().runDelayed(() -> {
            this.registerDefaultParsers();
            // hook external item plugins
            this.itemManager.delayedInit();
            // hook worldedit
            this.blockManager.delayedInit();
            // register listeners and tasks
            this.guiManager.delayedInit();
            this.recipeManager.delayedInit();
            this.packManager.delayedInit();
            this.fontManager.delayedInit();
            this.vanillaLootManager.delayedInit();
            // reload the plugin
            try {
                this.reloadPlugin(Runnable::run, Runnable::run, true);
            } catch (Exception e) {
                this.logger.warn("Failed to reload plugin on enable stage", e);
            }
            // must be after reloading because this process loads furniture
            this.worldManager.delayedInit();
            this.furnitureManager.delayedInit();
            // set up some platform extra tasks
            this.platformDelayedEnable();
        });
    }

    public void onPluginDisable() {
        if (this.networkManager != null) this.networkManager.disable();
        if (this.fontManager != null) this.fontManager.disable();
        if (this.packManager != null) this.packManager.disable();
        if (this.itemManager != null) this.itemManager.disable();
        if (this.blockManager != null) this.blockManager.disable();
        if (this.furnitureManager != null) this.furnitureManager.disable();
        if (this.templateManager != null) this.templateManager.disable();
        if (this.worldManager != null) this.worldManager.disable();
        if (this.recipeManager != null) this.recipeManager.disable();
        if (this.itemBrowserManager != null) this.itemBrowserManager.disable();
        if (this.guiManager != null) this.guiManager.disable();
        if (this.soundManager != null) this.soundManager.disable();
        if (this.vanillaLootManager != null) this.vanillaLootManager.disable();
        if (this.translationManager != null) this.translationManager.disable();
        if (this.scheduler != null) this.scheduler.shutdownScheduler();
        if (this.scheduler != null) this.scheduler.shutdownExecutor();
        if (this.commandManager != null) this.commandManager.unregisterFeatures();
        if (this.senderFactory != null) this.senderFactory.close();
        ResourcePackHost.instance().disable();
    }

    protected void registerDefaultParsers() {
        // register template parser
        this.packManager.registerConfigSectionParser(this.templateManager.parser());
        // register font parser
        this.packManager.registerConfigSectionParsers(this.fontManager.parsers());
        // register item parser
        this.packManager.registerConfigSectionParser(this.itemManager.parser());
        // register furniture parser
        this.packManager.registerConfigSectionParser(this.furnitureManager.parser());
        // register block parser
        this.packManager.registerConfigSectionParser(this.blockManager.parser());
        // register recipe parser
        this.packManager.registerConfigSectionParser(this.recipeManager.parser());
        // register category parser
        this.packManager.registerConfigSectionParser(this.itemBrowserManager.parser());
        // register translation parser
        this.packManager.registerConfigSectionParsers(this.translationManager.parsers());
        // register sound parser
        this.packManager.registerConfigSectionParsers(this.soundManager.parsers());
        // register vanilla loot parser
        this.packManager.registerConfigSectionParser(this.vanillaLootManager.parser());
    }

    protected abstract void platformDelayedEnable();

    protected abstract List<Dependency> platformDependencies();

    protected List<Dependency> commonDependencies() {
        return List.of(
                Dependencies.BSTATS_BASE,
                Dependencies.CAFFEINE,
                Dependencies.GEANTY_REF,
                Dependencies.NETTY_HTTP,
                Dependencies.CLOUD_CORE, Dependencies.CLOUD_SERVICES,
                Dependencies.GSON,
                Dependencies.SLF4J_API, Dependencies.SLF4J_SIMPLE,
                Dependencies.COMMONS_IO,
                Dependencies.ZSTD,
                Dependencies.BYTE_BUDDY,
                Dependencies.SNAKE_YAML,
                Dependencies.BOOSTED_YAML,
                Dependencies.MINIMESSAGE,
                Dependencies.TEXT_SERIALIZER_GSON,
                Dependencies.TEXT_SERIALIZER_JSON,
                Dependencies.AHO_CORASICK,
                Dependencies.LZ4
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public <W> SchedulerAdapter<W> scheduler() {
        return (SchedulerAdapter<W>) scheduler;
    }

    @Override
    public ClassPathAppender classPathAppender() {
        return classPathAppender;
    }

    @Override
    public Config config() {
        return config;
    }

    @Override
    public PluginLogger logger() {
        return logger;
    }

    @Override
    public void debug(Supplier<String> message) {
        debugger.accept(message);
    }

    @Override
    public boolean isReloading() {
        return isReloading;
    }

    public abstract boolean hasPlaceholderAPI();

    @Override
    public DependencyManager dependencyManager() {
        return dependencyManager;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> ItemManager<T> itemManager() {
        return (ItemManager<T>) itemManager;
    }

    @Override
    public BlockManager blockManager() {
        return blockManager;
    }

    @Override
    public NetworkManager networkManager() {
        return networkManager;
    }

    @Override
    public FontManager imageManager() {
        return fontManager;
    }

    @Override
    public TranslationManager translationManager() {
        return translationManager;
    }

    @Override
    public TemplateManager templateManager() {
        return templateManager;
    }

    @Override
    public FurnitureManager furnitureManager() {
        return furnitureManager;
    }

    @Override
    public PackManager packManager() {
        return packManager;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> RecipeManager<T> recipeManager() {
        return (RecipeManager<T>) recipeManager;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <P extends Plugin, C> SenderFactory<P, C> senderFactory() {
        return (SenderFactory<P, C>) senderFactory;
    }

    @Override
    public WorldManager worldManager() {
        return worldManager;
    }

    @Override
    public ItemBrowserManager itemBrowserManager() {
        return itemBrowserManager;
    }

    @Override
    public GuiManager guiManager() {
        return guiManager;
    }

    @Override
    public SoundManager soundManager() {
        return soundManager;
    }

    @Override
    public VanillaLootManager vanillaLootManager() {
        return vanillaLootManager;
    }
}
