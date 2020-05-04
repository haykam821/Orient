package io.github.haykam821.orient.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CartographyTableBlock;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;

@Mixin(Block.class)
public abstract class BlockMixin {
	@Shadow
	private StateManager<Block, BlockState> stateManager;

	@Shadow
	protected abstract void setDefaultState(BlockState blockState);

	@Unique
	private static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

	@Unique
	private boolean shouldMakeRotatable() {
		Block block = (Block) (Object) this;
		return block instanceof CraftingTableBlock || block instanceof CartographyTableBlock;
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	public void setDefaultDirection(Block.Settings settings, CallbackInfo ci) {
		if (!this.shouldMakeRotatable()) return;
		this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
		
	}

	@Inject(method = "appendProperties", at = @At("TAIL"))
	private void appendDirectionProperty(StateManager.Builder<Block, BlockState> stateManager, CallbackInfo ci) {
		if (!this.shouldMakeRotatable()) return;
		stateManager.add(FACING);
	}

	@Inject(method = "getPlacementState", at = @At("RETURN"), cancellable = true)
	public void getPlacementDirection(ItemPlacementContext context, CallbackInfoReturnable<BlockState> ci) {
		if (!this.shouldMakeRotatable()) return;
		ci.setReturnValue(ci.getReturnValue().with(FACING, context.getPlayerFacing()));
	}
}