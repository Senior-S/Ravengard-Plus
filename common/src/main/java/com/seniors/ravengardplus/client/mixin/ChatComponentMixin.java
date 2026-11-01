package com.seniors.ravengardplus.client.mixin;

import com.seniors.ravengardplus.client.ping.PingMarkers;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {
	// Every chat line passes through here, whether the server sends it as player chat or system text
	@ModifyVariable(method = "addMessage", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private Component ravengardPlus$readPing(Component message) {
		return PingMarkers.onChatMessage(message);
	}
}
