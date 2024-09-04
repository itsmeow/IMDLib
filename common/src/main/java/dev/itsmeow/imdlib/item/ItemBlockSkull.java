package dev.itsmeow.imdlib.item;

import dev.architectury.registry.registries.DeferredSupplier;
import dev.itsmeow.imdlib.block.AnimalSkullBlock;
import dev.itsmeow.imdlib.entity.util.variant.IVariant;
import dev.itsmeow.imdlib.util.HeadType.PlacementType;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ItemBlockSkull extends StandingAndWallBlockItem {

    public final PlacementType placement;
    public final String id;
    public final IVariant variant;

    public ItemBlockSkull(Block block, PlacementType placement, String id, IVariant variant, DeferredSupplier<CreativeModeTab> group) {
        this(block, placement, id, variant, new Item.Properties().arch$tab(group));
    }

    public ItemBlockSkull(Block block, PlacementType placement, String id, IVariant variant, CreativeModeTab group) {
        this(block, placement, id, variant, new Item.Properties().arch$tab(group));
    }

    public ItemBlockSkull(Block block, PlacementType placement, String id, IVariant variant, Properties prop) {
        super(block, block, prop, Direction.DOWN);
        this.placement = placement;
        this.id = id;
        this.variant = variant;
    }

    @Override
    @Nullable
    protected BlockState getPlacementState(BlockPlaceContext ctx) {
        BlockState returnedState = null;
        Level world = ctx.getLevel();
        BlockPos clickPos = ctx.getClickedPos();
        for (Direction side : ctx.getNearestLookingDirections()) {
            BlockState newState;
            if (side == Direction.DOWN && placement != PlacementType.FLOOR_AND_WALL) {
                return null;
            }
            newState = this.getBlock().getStateForPlacement(ctx);
            if (newState == null || !newState.canSurvive(world, clickPos) || (newState.getValue(AnimalSkullBlock.FACING_EXCEPT_DOWN) == Direction.UP && placement != PlacementType.FLOOR_AND_WALL))
                continue;
            returnedState = newState;
            break;
        }
        return returnedState;
    }

    @Override
    public InteractionResult place(BlockPlaceContext ctx) {
        if (!ctx.canPlace()) {
            return InteractionResult.FAIL;
        } else {
            if (ctx.getNearestLookingDirection() == Direction.DOWN || (ctx.getNearestLookingDirection() == Direction.UP && placement != PlacementType.FLOOR_AND_WALL)) {
                return InteractionResult.FAIL;
            }
            BlockState placementState = this.getPlacementState(ctx);
            if (placementState == null) {
                return InteractionResult.FAIL;
            } else if (!this.placeBlock(ctx, placementState)) {
                return InteractionResult.FAIL;
            } else {
                BlockPos blockpos = ctx.getClickedPos();
                Level world = ctx.getLevel();
                Player player = ctx.getPlayer();
                ItemStack stack = ctx.getItemInHand();
                BlockState newState = world.getBlockState(blockpos);
                Block block = newState.getBlock();
                if (block == placementState.getBlock()) {
                    this.updateCustomBlockEntityTag(blockpos, world, player, stack, newState);
                    block.setPlacedBy(world, blockpos, newState, player, stack);
                    if (player instanceof ServerPlayer) {
                        CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) player, blockpos, stack);
                    }
                }

                SoundType soundtype = newState.getSoundType();
                world.playSound(player, blockpos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                stack.shrink(1);
                return InteractionResult.SUCCESS;
            }
        }
    }
}
