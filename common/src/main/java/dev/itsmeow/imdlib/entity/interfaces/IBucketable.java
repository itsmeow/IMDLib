package dev.itsmeow.imdlib.entity.interfaces;

import net.minecraft.advancements.CriteriaTriggers;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IBucketable extends IContainable {

    @Override
    default void onPickupSuccess(Player player, InteractionHand hand, ItemStack stack) {
        this.getImplementation().playSound(SoundEvents.BUCKET_FILL_FISH, 1.0F, 1.0F);
        if (!this.getImplementation().level().isClientSide) {
            CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer) player, stack);
        }
    }

}
