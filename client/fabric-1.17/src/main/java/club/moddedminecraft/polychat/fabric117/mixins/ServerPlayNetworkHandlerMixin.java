package club.moddedminecraft.polychat.fabric117.mixins;

import club.moddedminecraft.polychat.fabric117.Polychat;
import net.minecraft.server.filter.TextStream;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Shadow public ServerPlayerEntity player;

    @Inject(method="handleMessage", at=@At(value="HEAD"))
    private void polychat$handleMessage(TextStream.Message message, CallbackInfo ci) {
        if (!message.getFiltered().startsWith("/")) {
            Polychat.handleMessage(message, this.player);
        }
    }

}
