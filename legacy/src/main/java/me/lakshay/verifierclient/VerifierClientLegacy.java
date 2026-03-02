package me.lakshay.verifierclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.stream.Collectors;

public class VerifierClientLegacy implements ClientModInitializer {

    // OLD networking API (works for 1.16.5 → 1.20.4)
    private static final Identifier VERIFY_REQUEST  = new Identifier("lakshay", "verify_request");
    private static final Identifier VERIFY_RESPONSE = new Identifier("lakshay", "verify_response");

    @Override
    public void onInitializeClient() {

        ClientPlayNetworking.registerGlobalReceiver(VERIFY_REQUEST, (client, handler, buf, responseSender) -> {
            // server sends a small request string like "REQ"
            String msg;
            try {
                msg = buf.readString(32);
            } catch (Throwable ignored) {
                msg = "REQ";
            }

            if (!"REQ".equalsIgnoreCase(msg)) return;

            // build mod list
            String mods = FabricLoader.getInstance()
                    .getAllMods()
                    .stream()
                    .map(m -> m.getMetadata().getId())
                    .distinct()
                    .sorted()
                    .collect(Collectors.joining(","));

            PacketByteBuf out = PacketByteBufs.create();
            out.writeString(mods);

            ClientPlayNetworking.send(VERIFY_RESPONSE, out);
        });
    }
}
