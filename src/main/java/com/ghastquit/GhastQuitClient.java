package com.ghastquit;

import java.util.Locale;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

/**
 * Client-side mod: only ghasts matter. Every other mob, player and
 * item is ignored.
 *
 * If your game has no ghast loaded for 60 seconds in a row, you are
 * disconnected from the server. A countdown is always shown above your hotbar.
 *
 * On multiplayer servers it kicks you. In singleplayer / LAN it only shows the
 * timer (marked "test mode") and never kicks, so you can test it safely.
 */
public class GhastQuitClient implements ClientModInitializer {

	/** How long you may go without a loaded ghast, in seconds. */
	private static final int SECONDS_WITHOUT_GHAST = 60;

	/** 20 ticks per second. */
	private static final int TICKS_LIMIT = SECONDS_WITHOUT_GHAST * 20;

	/** Consecutive ticks with zero ghasts loaded. */
	private int ticksWithout = 0;

	@Override
	public void onInitializeClient() {
		// Fresh timer every time you join or leave a server.
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ticksWithout = 0);
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ticksWithout = 0);

		ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
	}

	private void onTick(Minecraft mc) {
		if (mc.level == null || mc.player == null || mc.getConnection() == null) {
			ticksWithout = 0;
			return;
		}

		boolean multiplayer = !mc.hasSingleplayerServer() && mc.getCurrentServer() != null;

		// Count loaded ghasts, ignore everything else.
		int ghasts = 0;
		for (Entity entity : mc.level.entitiesForRendering()) {
			if (entity.getType() == EntityType.GHAST) {
				ghasts++;
			}
		}

		if (ghasts > 0) {
			ticksWithout = 0;
		} else if (ticksWithout < TICKS_LIMIT) {
			ticksWithout++;
		}

		// Timer above the hotbar, updated every tick.
		double secondsLeft = (TICKS_LIMIT - ticksWithout) / 20.0;
		String text = String.format(Locale.ROOT,
				"Ghasts loaded: %d | Kick in %.1fs", ghasts, secondsLeft);
		if (!multiplayer) {
			text += " (test mode, no kick)";
		}
		mc.player.displayClientMessage(Component.literal(text), true);

		if (multiplayer && ticksWithout >= TICKS_LIMIT) {
			ticksWithout = 0;
			mc.getConnection().getConnection().disconnect(
					Component.literal("Ghast Quit: no ghast loaded for "
							+ SECONDS_WITHOUT_GHAST + " seconds."));
		}
	}
}
