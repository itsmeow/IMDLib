package dev.itsmeow.imdlib.item;

import dev.architectury.registry.registries.DeferredSupplier;
import dev.itsmeow.imdlib.entity.EntityTypeContainer;
import dev.itsmeow.imdlib.entity.interfaces.IContainable;
import dev.itsmeow.imdlib.entity.util.EntityTypeContainerContainable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;

public class ItemModEntityContainer<T extends Mob & IContainable> extends Item implements IContainerItem<T> {

    protected final EntityTypeContainerContainable<T, ItemModEntityContainer<T>> typeContainer;
    protected final ITooltipFunction tooltip;

    public ItemModEntityContainer(EntityTypeContainerContainable<T, ItemModEntityContainer<T>> typeContainer, DeferredSupplier<CreativeModeTab> group) {
        this(typeContainer, IContainerItem.VARIANT_TOOLTIP, group);
    }

    public ItemModEntityContainer(EntityTypeContainerContainable<T, ItemModEntityContainer<T>> typeContainer, ITooltipFunction tooltip, DeferredSupplier<CreativeModeTab> group) {
        super(new Item.Properties().stacksTo(1).arch$tab(group));
        this.typeContainer = typeContainer;
        this.tooltip = tooltip;
    }

    public ItemModEntityContainer(EntityTypeContainerContainable<T, ItemModEntityContainer<T>> typeContainer, CreativeModeTab group) {
        this(typeContainer, IContainerItem.VARIANT_TOOLTIP, group);
    }

    public ItemModEntityContainer(EntityTypeContainerContainable<T, ItemModEntityContainer<T>> typeContainer, ITooltipFunction tooltip, CreativeModeTab group) {
        super(new Item.Properties().stacksTo(1).arch$tab(group));
        this.typeContainer = typeContainer;
        this.tooltip = tooltip;
    }

    public static <T extends Mob & IContainable> BiFunction<EntityTypeContainerContainable<T, ItemModEntityContainer<T>>, ITooltipFunction, ItemModEntityContainer<T>> get(CreativeModeTab group) {
        return (container, tooltip) -> new ItemModEntityContainer<>(container, tooltip, group);
    }

    public static <T extends Mob & IContainable> BiFunction<EntityTypeContainerContainable<T, ItemModEntityContainer<T>>, ITooltipFunction, ItemModEntityContainer<T>> get(DeferredSupplier<CreativeModeTab> group) {
        return (container, tooltip) -> new ItemModEntityContainer<>(container, tooltip, group);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        if (!worldIn.isClientSide) {
            ItemStack itemstack = playerIn.getItemInHand(handIn);
            BlockHitResult raytraceresult = getPlayerPOVHitResult(worldIn, playerIn, ClipContext.Fluid.NONE);
            if (raytraceresult.getType() == BlockHitResult.Type.MISS) {
                return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
            } else if (raytraceresult.getType() != BlockHitResult.Type.BLOCK) {
                return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
            } else {
                BlockPos blockpos = raytraceresult.getBlockPos();
                if (worldIn instanceof ServerLevel) {
                    this.placeEntity((ServerLevel) worldIn, playerIn.getItemInHand(handIn), blockpos.relative(raytraceresult.getDirection()));
                }
                if (!playerIn.isCreative()) {
                    playerIn.setItemSlot(handIn == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND, new ItemStack(this.typeContainer.getEmptyContainerItem()));
                }
                return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemstack);
            }
        }
        return super.use(worldIn, playerIn, handIn);
    }


    @Environment(EnvType.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        if (list != null) {
            this.tooltip.addInformation(this.typeContainer, stack, level, list);
        }
    }

    @Override
    public EntityTypeContainer<T> getContainer() {
        return typeContainer;
    }

}