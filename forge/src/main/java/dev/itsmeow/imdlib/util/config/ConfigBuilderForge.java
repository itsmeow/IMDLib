package dev.itsmeow.imdlib.util.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ConfigBuilderForge extends ConfigBuilder {

    private ForgeConfigSpec.Builder builder;
    private ForgeConfigSpec spec;

    public ConfigBuilderForge(CommonConfigAPI.ConfigType type, Consumer<ConfigBuilder> init, Runnable onLoad) {
        super(type, init, onLoad);
        final Pair<ConfigBuilder, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(builder -> {
            this.builder = builder;
            init.accept(this);
            return this;
        });
        this.spec = specPair.getRight();
        ModLoadingContext.get().registerConfig(type == CommonConfigAPI.ConfigType.CLIENT ? ModConfig.Type.CLIENT : (type == CommonConfigAPI.ConfigType.COMMON ? ModConfig.Type.COMMON : ModConfig.Type.SERVER), specPair.getRight());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onLoadForge);
    }

    private void onLoadForge(ModConfig.Loading configEvent) {
        LogManager.getLogger().debug("Loading {}", configEvent.getConfig().getFileName());
        if(configEvent.getConfig().getSpec() == spec) {
            this.onLoad();
        }
    }

    @Override
    public <T> Supplier<T> define(String path, T defaultValue) {
        return builder.worldRestart().define(path, defaultValue)::get;
    }

    @Override
    public <T extends Comparable<? super T>> Supplier<T> defineInRange(List<String> path, T defaultValue, T min, T max, Class<T> clazz) {
        return builder.worldRestart().defineInRange(path, defaultValue, min, max, clazz)::get;
    }

    @Override
    public Supplier<Double> defineInRange(String path, double defaultValue, double min, double max) {
        return builder.worldRestart().defineInRange(path, defaultValue, min, max)::get;
    }

    @Override
    public Supplier<Integer> defineInRange(String path, int defaultValue, int min, int max) {
        return builder.worldRestart().defineInRange(path, defaultValue, min, max)::get;
    }

    @Override
    public Supplier<Long> defineInRange(String path, long defaultValue, long min, long max) {
        return builder.worldRestart().defineInRange(path, defaultValue, min, max)::get;
    }

    @Override
    public <T> Supplier<List<? extends T>> defineList(String path, List<? extends T> defaultValue, Predicate<Object> elementValidator) {
        return builder.worldRestart().defineList(path, defaultValue, elementValidator)::get;
    }

    @Override
    public <T> Supplier<T> define(String path, String comment, T defaultValue) {
        return builder.comment(comment).worldRestart().define(path, defaultValue)::get;
    }

    @Override
    public <T extends Comparable<? super T>> Supplier<T> defineInRange(List<String> path, String comment, T defaultValue, T min, T max, Class<T> clazz) {
        return builder.comment(comment).worldRestart().defineInRange(path, defaultValue, min, max, clazz)::get;
    }

    @Override
    public Supplier<Double> defineInRange(String path, String comment, double defaultValue, double min, double max) {
        return builder.comment(comment).worldRestart().defineInRange(path, defaultValue, min, max)::get;
    }

    @Override
    public Supplier<Integer> defineInRange(String path, String comment, int defaultValue, int min, int max) {
        return builder.comment(comment).worldRestart().defineInRange(path, defaultValue, min, max)::get;
    }

    @Override
    public Supplier<Long> defineInRange(String path, String comment, long defaultValue, long min, long max) {
        return builder.comment(comment).worldRestart().defineInRange(path, defaultValue, min, max)::get;
    }

    @Override
    public <T> Supplier<List<? extends T>> defineList(String path, String comment, List<? extends T> defaultValue, Predicate<Object> elementValidator) {
        return builder.comment(comment).worldRestart().defineList(path, defaultValue, elementValidator)::get;
    }

    @Override
    public void push(String path) {
        builder.push(path);
    }

    @Override
    public void pop() {
        builder.pop();
    }
}
