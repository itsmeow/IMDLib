package dev.itsmeow.imdlib;

import jdk.internal.reflect.Reflection;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class FabricMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return (!mixinClassName.equals("dev.itsmeow.imdlib.mixin.AbstractConfigEntryMixin") && !mixinClassName.equals("dev.itsmeow.imdlib.mixin.ClothConfigScreenAccessor")) || clothLoaded();
    }

    public static boolean clothLoaded() {
        try {
            Class c = Class.forName("me.shedaniel.clothconfig2.ClothConfigInitializer", false, Reflection.getCallerClass().getClassLoader());
            return c != null;
        } catch (ClassNotFoundException | LinkageError | SecurityException e) {
            return false;
        }
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
