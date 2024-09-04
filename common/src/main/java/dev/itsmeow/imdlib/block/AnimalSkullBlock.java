package dev.itsmeow.imdlib.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

public abstract class AnimalSkullBlock extends Block implements SimpleWaterloggedBlock {

    public static final DirectionProperty FACING_EXCEPT_DOWN = DirectionProperty.create("facing", Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP);
    public static final DirectionProperty TOP_FACING = DirectionProperty.create("top", Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
    private static final Map<Direction, VoxelShape> SHAPES = Maps.newEnumMap(ImmutableMap.of(
            Direction.NORTH, Block.box(4.0D, 4.0D, 8.0D, 12.0D, 12.0D, 16.0D),
            Direction.SOUTH, Block.box(4.0D, 4.0D, 0.0D, 12.0D, 12.0D, 8.0D),
            Direction.EAST, Block.box(0.0D, 4.0D, 4.0D, 8.0D, 12.0D, 12.0D),
            Direction.WEST, Block.box(8.0D, 4.0D, 4.0D, 16.0D, 12.0D, 12.0D),
            Direction.UP, Block.box(4.0D, 0.0D, 4.0D, 12.0D, 8.0D, 12.0D)));

    public AnimalSkullBlock() {
        super(Block.Properties.of().mapColor(MapColor.WOOL).sound(SoundType.STONE).strength(0.8F));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING_EXCEPT_DOWN, Direction.NORTH).setValue(BlockStateProperties.WATERLOGGED, false).setValue(TOP_FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
        return SHAPES.get(state.getValue(FACING_EXCEPT_DOWN));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = this.defaultBlockState();
        BlockPos pos = context.getClickedPos();
        Direction[] directions = context.getNearestLookingDirections();

        for (Direction dir : directions) {
            Direction direction = dir.getOpposite();
            if (direction == Direction.DOWN) {
                return null;
            } else if (direction == Direction.UP) {
                state = state.setValue(TOP_FACING, Direction.fromYRot(context.getPlayer().getYHeadRot()));
            }
            state = state.setValue(FACING_EXCEPT_DOWN, direction);
            if (!context.getLevel().getBlockState(pos.relative(dir)).canBeReplaced(context)) {
                return state.setValue(BlockStateProperties.WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
            }
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING_EXCEPT_DOWN, BlockStateProperties.WATERLOGGED, TOP_FACING);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING_EXCEPT_DOWN, rotation.rotate(state.getValue(FACING_EXCEPT_DOWN)));
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING_EXCEPT_DOWN)));
    }

}
