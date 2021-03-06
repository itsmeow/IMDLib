package dev.itsmeow.imdlib.item;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import dev.itsmeow.imdlib.entity.EntityTypeContainer;
import dev.itsmeow.imdlib.entity.util.EntityTypeContainerContainable;
import dev.itsmeow.imdlib.entity.interfaces.IContainable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemModFishBucket<T extends MobEntity & IContainable> extends BucketItem implements IContainerItem<T> {
    private final EntityTypeContainerContainable<T, ItemModFishBucket<T>> typeContainer;
    private final ITooltipFunction tooltip;

    public static <T extends MobEntity & IContainable> BiFunction<EntityTypeContainerContainable<T, ItemModFishBucket<T>>, ITooltipFunction, ItemModFishBucket<T>> waterBucket(ItemGroup group) {
        return (container, tooltip) -> new ItemModFishBucket<>(container, () -> Fluids.WATER, tooltip, group);
    }

    public ItemModFishBucket(EntityTypeContainerContainable<T, ItemModFishBucket<T>> typeContainer, Supplier<? extends Fluid> fluid, ItemGroup group) {
        this(typeContainer, fluid, IContainerItem.VARIANT_TOOLTIP, group);
    }

    public ItemModFishBucket(EntityTypeContainerContainable<T, ItemModFishBucket<T>> typeContainer, Supplier<? extends Fluid> fluid, ITooltipFunction tooltip, ItemGroup group) {
        super(fluid, new Item.Properties().maxStackSize(1).group(group));
        this.typeContainer = typeContainer;
        this.setRegistryName(typeContainer.getEntityName() + "_bucket");
        this.tooltip = tooltip;
    }

    @Override
    public void onLiquidPlaced(World worldIn, ItemStack stack, BlockPos pos) {
        if(!worldIn.isRemote && worldIn instanceof ServerWorld) {
            this.placeEntity((ServerWorld) worldIn, stack, pos);
        }
    }

    @Override
    protected void playEmptySound(@Nullable PlayerEntity player, IWorld worldIn, BlockPos pos) {
        worldIn.playSound(player, pos, SoundEvents.ITEM_BUCKET_EMPTY_FISH, SoundCategory.NEUTRAL, 1.0F, 1.0F);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        this.tooltip.addInformation(this.typeContainer, stack, worldIn, tooltip);
    }

    @Override
    public EntityTypeContainer<T> getContainer() {
        return typeContainer;
    }

}
