package RewardedIvan.asteroidclient.mixin;

import RewardedIvan.asteroidclient.events.entity.player.*;
import RewardedIvan.asteroidclient.utils.world.BlockUtils;
import RewardedIvan.asteroidclient.AsteroidClient;
import RewardedIvan.asteroidclient.events.entity.DropItemsEvent;
import RewardedIvan.asteroidclient.mixininterface.IClientPlayerInteractionManager;
import RewardedIvan.asteroidclient.systems.modules.Modules;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin implements IClientPlayerInteractionManager {
    @Shadow private int blockBreakingCooldown;

    @Shadow protected abstract void syncSelectedSlot();

    @Inject(method = "clickSlot", at = @At("HEAD"), cancellable = true)
    private void onClickSlot(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo info) {
        if (actionType == SlotActionType.THROW && slotId >= 0 && slotId < player.currentScreenHandler.slots.size()) {
            if (AsteroidClient.EVENT_BUS.post(DropItemsEvent.get(player.currentScreenHandler.slots.get(slotId).getStack())).isCancelled()) info.cancel();
        }
        else if (slotId == -999) {
            // Clicking outside of inventory
            if (AsteroidClient.EVENT_BUS.post(DropItemsEvent.get(player.currentScreenHandler.getCursorStack())).isCancelled()) info.cancel();
        }
    }

    @Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
    private void onAttackBlock(BlockPos blockPos, Direction direction, CallbackInfoReturnable<Boolean> info) {
        if (AsteroidClient.EVENT_BUS.post(StartBreakingBlockEvent.get(blockPos, direction)).isCancelled()) info.cancel();
    }

    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    public void interactBlock(ClientPlayerEntity player, ClientWorld world, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (AsteroidClient.EVENT_BUS.post(InteractBlockEvent.get(player.getMainHandStack().isEmpty() ? Hand.OFF_HAND : hand, hitResult)).isCancelled()) cir.setReturnValue(ActionResult.FAIL);
    }

    @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    private void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo info) {
        if (AsteroidClient.EVENT_BUS.post(AttackEntityEvent.get(target)).isCancelled()) info.cancel();
    }

    @Inject(method = "interactEntity", at = @At("HEAD"), cancellable = true)
    private void onInteractEntity(PlayerEntity player, Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> info) {
        if (AsteroidClient.EVENT_BUS.post(InteractEntityEvent.get(entity, hand)).isCancelled()) info.setReturnValue(ActionResult.FAIL);
    }

    @Inject(method = "dropCreativeStack", at = @At("HEAD"), cancellable = true)
    private void onDropCreativeStack(ItemStack stack, CallbackInfo info) {
        if (AsteroidClient.EVENT_BUS.post(DropItemsEvent.get(stack)).isCancelled()) info.cancel();
    }

    @Inject(method = "breakBlock", at = @At("HEAD"))
    private void onBreakBlock(BlockPos blockPos, CallbackInfoReturnable<Boolean> info) {
        AsteroidClient.EVENT_BUS.post(BreakBlockEvent.get(blockPos));
    }

    @Inject(method = "interactItem", at = @At("HEAD"), cancellable = true)
    private void onInteractItem(PlayerEntity player, World world, Hand hand, CallbackInfoReturnable<ActionResult> info) {
        InteractItemEvent event = AsteroidClient.EVENT_BUS.post(InteractItemEvent.get(hand));
        if (event.toReturn != null) info.setReturnValue(event.toReturn);
    }

    @Inject(method = "cancelBlockBreaking", at = @At("HEAD"), cancellable = true)
    private void onCancelBlockBreaking(CallbackInfo info) {
        if (BlockUtils.breaking) info.cancel();
    }

    @Override
    public void syncSelected() {
        syncSelectedSlot();
    }
}
