/*
 * Copyright (c) 2022, Damen <gh: damencs>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:

 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.

 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.tobqol.rooms.maiden;

import net.runelite.client.plugins.tobqol.TheatreQOLConfig;
import net.runelite.client.plugins.tobqol.TheatreQOLPlugin;
import net.runelite.client.plugins.tobqol.rooms.RoomHandler;
import net.runelite.client.plugins.tobqol.rooms.maiden.commons.MaidenHealth;
import net.runelite.client.plugins.tobqol.rooms.maiden.commons.MaidenPhase;
import net.runelite.client.plugins.tobqol.rooms.maiden.commons.MaidenRedCrab;
import net.runelite.client.plugins.tobqol.rooms.maiden.commons.MaidenTable;
import net.runelite.client.plugins.tobqol.tracking.RoomDataHandler;
import net.runelite.client.plugins.tobqol.tracking.RoomDataItem;
import net.runelite.client.plugins.tobqol.tracking.RoomInfoBox;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Hitsplat;
import net.runelite.api.NPC;
import net.runelite.api.events.*;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import java.awt.*;
import java.util.List;
import java.util.*;

import static net.runelite.client.plugins.tobqol.api.game.Region.MAIDEN;
import static net.runelite.client.plugins.tobqol.api.game.Region.inRegion;
import static net.runelite.client.plugins.tobqol.rooms.maiden.commons.MaidenConstants.*;
import static net.runelite.client.plugins.tobqol.tracking.RoomInfoUtil.createInfoBox;
import static net.runelite.client.plugins.tobqol.tracking.RoomInfoUtil.formatTime;
import static lombok.AccessLevel.NONE;

@Getter
@Slf4j
public class MaidenHandler extends RoomHandler
{
	@Inject
	private MaidenSceneOverlay sceneOverlay;

	private RoomDataHandler dataHandler;

	@CheckForNull
	private NPC maidenNpc = null;

	private RoomInfoBox maidenInfoBox;

	@CheckForNull
	private MaidenHealth health = null;

	private MaidenPhase phase = MaidenPhase.OTHER;

	private final List<NPC> bloodSpawns = new ArrayList<>();

	private final Map<Integer, MaidenRedCrab> crabsMap = new HashMap<>();

	@Getter(NONE) // Omit Lombok's Getter
	private final List<MaidenRedCrab> crabs_buffer = new ArrayList<>();

	@Getter(NONE) // Omit Lombok's Getter
	private byte totalLeaks = 0;

	private boolean considerCrabs = true;

	@Inject
	protected MaidenHandler(TheatreQOLPlugin plugin, TheatreQOLConfig config)
	{
		super(plugin, config);
		setRoomRegion(MAIDEN);
	}

	@Override
	public void load()
	{
		dataHandler = plugin.getDataHandler();
		overlayManager.add(sceneOverlay);
	}

	@Override
	public void unload()
	{
		overlayManager.remove(sceneOverlay);
		reset();
	}

	@Override
	public void reset()
	{
		maidenNpc = null;
		phase = MaidenPhase.OTHER;

		bloodSpawns.clear();
		crabsMap.clear();
		crabs_buffer.clear();
		totalLeaks = 0;
		considerCrabs = true;

		if (instance.getRaidStatus() <= 1)
		{
			infoBoxManager.removeInfoBox(maidenInfoBox);
		}
	}

	@Override
	public boolean active()
	{
		return inRegion(client, MAIDEN) && maidenNpc != null && !maidenNpc.isDead();
	}

	@Subscribe
	private void onNpcSpawned(NpcSpawned e)
	{
		NPC npc = e.getNpc();

		if (active())
		{
			isNpcFromName(npc, MaidenTable.BLOOD_SPAWN_NAME, (n -> bloodSpawns.add(npc)));
			isNpcFromName(npc, MaidenTable.RED_CRAB_NAME, n ->
			{
				crabsMap.put(n.getIndex(), new MaidenRedCrab(client, instance, n, phase));

				if (!dataHandler.getData().isEmpty() & considerCrabs)
				{
					considerCrabs = false;

					if (!dataHandler.Find("70s").isPresent())
					{
						dataHandler.getData().add(new RoomDataItem("70s", dataHandler.getTime(), 1, !config.displayTimeSplits()));
						return;
					}
					else if (!dataHandler.Find("50s").isPresent() && dataHandler.Find("70s").isPresent())
					{
						dataHandler.getData().add(new RoomDataItem("50s", dataHandler.getTime(), 2, !config.displayTimeSplits(), "70s"));
						return;
					}
					else if (!dataHandler.Find("30s").isPresent() && dataHandler.Find("50s").isPresent())
					{
						dataHandler.getData().add(new RoomDataItem("30s", dataHandler.getTime(), 3, !config.displayTimeSplits(), "50s"));
						return;
					}
				}
			});
			return;
		}

		isNpcFromName(npc, BOSS_NAME, n ->
		{
			if (!dataHandler.Find("Starting Tick").isPresent())
			{
				dataHandler.getData().add(new RoomDataItem("Starting Tick", client.getTickCount(), true));
				dataHandler.setShouldTrack(true);

				dataHandler.getData().add(new RoomDataItem("Room", dataHandler.getTime(), 99, false, "30s"));
			}

			instance.lazySetMode(() -> MaidenTable.findMode(n.getId()));
			maidenNpc = n;
			phase = MaidenPhase.compose(n);
		});
	}

	@Subscribe
	private void onNpcChanged(NpcChanged e)
	{
		if (!active())
		{
			return;
		}

		isNpcFromName(e.getNpc(), BOSS_NAME, n ->
		{
			phase = MaidenPhase.compose(n);
		});
	}

	@Subscribe
	private void onNpcDespawned(NpcDespawned e)
	{
		if (!active())
		{
			return;
		}

		NPC npc = e.getNpc();

		if (isNpcFromName(npc, BOSS_NAME, $ -> reset()))
		{
			return;
		}

		isNpcFromName(npc, MaidenTable.BLOOD_SPAWN_NAME, n -> bloodSpawns.removeIf(s -> s != null && npc.getIndex() == s.getIndex()));

		isNpcFromName(npc, MaidenTable.RED_CRAB_NAME, n ->
		{
			crabsMap.remove(n.getIndex());
			crabs_buffer.removeIf(crab -> crab.test(n));
		});
	}

	@Subscribe
	private void onGameTick(GameTick e)
	{
		if (instance.isInRaid() && instance.getCurrentRegion().isMaiden())
		{
			if (instance.getRoomStatus() == 1 && !dataHandler.Find("Starting Tick").isPresent())
			{
				dataHandler.getData().add(new RoomDataItem("Starting Tick", client.getTickCount(), true, true));
				dataHandler.setShouldTrack(true);
			}

			if (dataHandler.isShouldTrack() && !dataHandler.getData().isEmpty())
			{
				dataHandler.updateTotalTime();
			}
		}

		if (!active())
		{
			return;
		}

		if (!considerCrabs)
		{
			considerCrabs = true;
		}

		if (!crabs_buffer.isEmpty())
		{
			crabs_buffer.removeIf(crab ->
			{
				if (crab.distance(maidenNpc) == 0 && !crab.health().zero())
				{
					if (config.displayMaidenLeaks())
					{
						enqueueChatMessage(ChatMessageType.FRIENDNOTIFICATION, b -> b
								.append(Color.BLUE, "[" + crab.phaseKey() + "]")
								.append(ChatColorType.NORMAL)
								.append(" - " + crab.spawnKey() + " leaked with ")
								.append(Color.RED, crab.health().truncatedPercent() + "%")
								.append(ChatColorType.NORMAL)
								.append(" hitpoints!"));
					}

					totalLeaks++;
				}

				return true;
			});
		}
	}

	@Subscribe
	private void onAnimationChanged(AnimationChanged e)
	{
		if (!active() || !(e.getActor() instanceof NPC))
		{
			return;
		}

		isNpcFromName((NPC) e.getActor(), BOSS_NAME, n ->
		{
			switch (n.getAnimation())
			{
				case -1: break;
				case MaidenTable.MAIDEN_DEATH_ANIM:
				{
					reset();
					break;
				}
			}
		});

		isNpcFromName((NPC) e.getActor(), RED_CRAB_NAME, n ->
		{
			if (n.getAnimation() == MaidenTable.RED_CRAB_DEATH_ANIM)
			{
				Optional.ofNullable(crabsMap.getOrDefault(n.getIndex(), null)).ifPresent(crabs_buffer::add);
			}
		});
	}

	@Subscribe
	private void onHitsplatApplied(HitsplatApplied e)
	{
		if (!active() || !(e.getActor() instanceof NPC))
		{
			return;
		}

		NPC npc = (NPC) e.getActor();
		Hitsplat hitsplat = e.getHitsplat();
		int amount = hitsplat.getAmount();

		isNpcFromName(npc, BOSS_NAME, n ->
		{
			if (hitsplat.isMine() || hitsplat.isOthers())
			{
				Optional.ofNullable(health).ifPresent(h -> h.removeHealth(amount));
				return;
			}
		});

		isNpcFromName(npc, RED_CRAB_NAME, n ->
		{
			MaidenRedCrab crab = crabsMap.getOrDefault(n.getIndex(), null);

			if (crab == null || (!hitsplat.isMine() && !hitsplat.isOthers()))
			{
				return;
			}

			crab.health().removeHealth(amount);
		});
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (instance.getCurrentRegion() != MAIDEN && event.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}

		String stripped = Text.removeTags(event.getMessage());

		if (MAIDEN_WAVE.matcher(stripped).find())
		{
			dataHandler.setShouldTrack(false);
			dataHandler.Find("Room").get().setValue(dataHandler.getTime());

			if (config.displayRoomTimes().isInfobox())
			{
				buildInfobox();
			}

			if (config.displayRoomTimes().isChat())
			{
				sendChatTimes();
			}
		}
	}

	private void buildInfobox()
	{
		if (!dataHandler.getData().isEmpty())
		{
			String tooltip;

			if (!dataHandler.Find("Starting Tick").get().isException())
			{
				tooltip = "70% - " + formatTime(dataHandler.FindValue("70s")) + "</br>" +
						"50% - " + formatTime(dataHandler.FindValue("50s")) + formatTime(dataHandler.FindValue("50s"), dataHandler.FindValue("70s")) + "</br>" +
						"30% - " + formatTime(dataHandler.FindValue("30s")) + formatTime(dataHandler.FindValue("30s"), dataHandler.FindValue("50s")) + "</br>" +
						"Complete - " + formatTime(dataHandler.FindValue("Room")) + formatTime(dataHandler.FindValue("Room"), dataHandler.FindValue("30s"));
			}
			else
			{
				tooltip = "Complete - " + formatTime(dataHandler.FindValue("Room")) + "*";
			}

			maidenInfoBox = createInfoBox(plugin, config, itemManager.getImage(BOSS_IMAGE), "Maiden", formatTime(dataHandler.FindValue("Room")), tooltip);
			infoBoxManager.addInfoBox(maidenInfoBox);
		}
	}

	private void sendChatTimes()
	{
		if (!dataHandler.getData().isEmpty())
		{
			if (!dataHandler.Find("Starting Tick").get().isException())
			{
				enqueueChatMessage(ChatMessageType.GAMEMESSAGE, b -> b
						.append(Color.RED, "70%")
						.append(ChatColorType.NORMAL)
						.append(" - " + formatTime(dataHandler.FindValue("70s")) + " - ")
						.append(Color.RED, "50%")
						.append(ChatColorType.NORMAL)
						.append(" - " + formatTime(dataHandler.FindValue("50s")) + formatTime(dataHandler.FindValue("50s"), dataHandler.FindValue("70s")) + " - ")
						.append(Color.RED, "30%")
						.append(ChatColorType.NORMAL)
						.append(" - " + formatTime(dataHandler.FindValue("30s")) + formatTime(dataHandler.FindValue("30s"), dataHandler.FindValue("50s"))));

				if (config.roomTimeValidation())
				{
					enqueueChatMessage(ChatMessageType.GAMEMESSAGE, b -> b
							.append(Color.RED, "Maiden - Room Complete")
							.append(ChatColorType.NORMAL)
							.append(" - " + formatTime(dataHandler.FindValue("Room")) + formatTime(dataHandler.FindValue("Room"), dataHandler.FindValue("30%"))));
				}
			}
			else
			{
				if (config.roomTimeValidation())
				{
					enqueueChatMessage(ChatMessageType.GAMEMESSAGE, b -> b
							.append(Color.RED, "Maiden - Room Complete")
							.append(ChatColorType.NORMAL)
							.append(" - " + formatTime(dataHandler.FindValue("Room")) + "*"));
				}
			}
		}
	}
}
