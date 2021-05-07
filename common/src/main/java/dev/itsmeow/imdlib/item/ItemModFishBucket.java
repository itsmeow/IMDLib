package dev.itsmeow.imdlib.item;


import dev.itsmeow.imdlib.entity.EntityTypeContainer;
import dev.itsmeow.imdlib.entity.interfaces.IContainable;
import dev.itsmeow.imdlib.entity.util.EntityTypeContainerContainable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class ItemModFishBucket<T extends Mob & IContainable> extends BucketItem implements IContainerItem<T> {
    private final EntityTypeContainerContainable<T, ItemModFishBucket<T>> typeContainer;
    private final ITooltipFunction tooltip;

    public ItemModFishBucket(EntityTypeContainerContainable<T, ItemModFishBucket<T>> typeContainer, Supplier<? extends Fluid> fluid, CreativeModeTab group) {
        this(typeContainer, fluid, IContainerItem.VARIANT_TOOLTIP, group);
    }

    public ItemModFishBucket(EntityTypeContainerContainable<T, ItemModFishBucket<T>> typeContainer, Supplier<? extends Fluid> fluid, ITooltipFunction tooltip, CreativeModeTab group) {
        super(fluid.get(), new Item.Properties().stacksTo(1).tab(group));
        this.typeContainer = typeContainer;
        //TODO this.setRegistryName(typeContainer.getEntityName() + "_bucket");
        this.tooltip = tooltip;
    }

    public static <T extends Mob & IContainable> BiFunction<EntityTypeContainerContainable<T, ItemModFishBucket<T>>, ITooltipFunction, ItemModFishBucket<T>> waterBucket(CreativeModeTab group) {
        return (container, tooltip) -> new ItemModFishBucket<>(container, () -> Fluids.WATER, tooltip, group);
    }

    @Override
    public void checkExtraContent(Level worldIn, ItemStack stack, BlockPos pos) {
        if (!worldIn.isClientSide && worldIn instanceof ServerLevel) {
            this.placeEntity((ServerLevel) worldIn, stack, pos);
        }
    }

    @Override
    protected void playEmptySound(@Nullable Player player, LevelAccessor worldIn, BlockPos pos) {
        worldIn.playSound(player, pos, SoundEvents.BUCKET_EMPTY_FISH, SoundSource.NEUTRAL, 1.0F, 1.0F);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        this.tooltip.addInformation(this.typeContainer, stack, level, list);
    }

    @Override
    public EntityTypeContainer<T> getContainer() {
        return typeContainer;
    }

}