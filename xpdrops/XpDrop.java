package net.runelite.client.plugins.xpdrops;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.Actor;
import net.runelite.api.Skill;

@Data
@AllArgsConstructor
public class XpDrop
{
	private Skill skill;
	private int experience;
	private XpDropStyle style;
	private boolean fake;
	private Actor attachedActor;
}
