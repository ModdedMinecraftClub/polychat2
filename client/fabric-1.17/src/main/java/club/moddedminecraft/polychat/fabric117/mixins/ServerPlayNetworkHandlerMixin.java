package club.moddedminecraft.polychat.fabric117.mixins;

import club.moddedminecraft.polychat.fabric117.Polychat;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Inject(method="onGameMessage",
            at = @At(value="HEAD",
                    target = "Lnet/minecraft/network/listener/ServerPlayPacketListener;onGameMessage(Lnet/minecraft/network/packet/c2s/play/ChatMessageC2SPacket;)V")
    )
    private void handleGameMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
        String withPrefix = Polychat.getClient().getFormattedServerId() + " " + packet.getChatMessage();
        packet = new ChatMessageC2SPacket(withPrefix);
        Polychat.getClient().getCallbacks().newChatMessage(packet.getChatMessage(), packet.getChatMessage());
    }



}
