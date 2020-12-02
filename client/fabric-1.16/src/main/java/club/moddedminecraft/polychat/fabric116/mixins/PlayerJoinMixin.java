package club.moddedminecraft.polychat.fabric116.mixins;

import club.moddedminecraft.polychat.fabric116.Polychat;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PlayerManager.class)
public class PlayerJoinMixin {
    @Inject(method = "onPlayerConnect",
            at = @At(value = "TAIL"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
    )
    public void onPlayerJoin(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        Polychat.onJoin(player);
    }
}
