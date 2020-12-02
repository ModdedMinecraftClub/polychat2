package club.moddedminecraft.polychat.fabric116.mixins;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import club.moddedminecraft.polychat.fabric116.Polychat;

@Mixin(ServerPlayerEntity.class)
public abstract class DeathMessageMixin extends LivingEntity {
    protected DeathMessageMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("HEAD"),
            method = "onDeath")
    public void onDeath(DamageSource source, CallbackInfo ci) {
        String message = getDamageTracker().getDeathMessage().getString();
        Polychat.receiveDeathMessage(message);
    }
}
