package club.moddedminecraft.polychat.fabric116.mixins;

import club.moddedminecraft.polychat.fabric116.Polychat;
import net.minecraft.network.MessageType;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.UUID;

@Mixin(PlayerManager.class)
public class ChatMessageMixin {
    @Inject(method = "broadcastChatMessage",
            at = @At(value = "TAIL"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
    )
    public void onChat(Text message, MessageType type, UUID senderUuid, CallbackInfo ci) {
        if (type != MessageType.CHAT)
            return;
        Polychat.receiveChatMessage(message.getString(), senderUuid);
    }
}
