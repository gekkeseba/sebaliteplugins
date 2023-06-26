package net.runelite.client.plugins.xpdrops.overlay;

import net.runelite.client.plugins.xpdrops.XpDropStyle;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.Actor;

@Data
@AllArgsConstructor
public class XpDropInFlight
{
	private int icons;
	private int amount;
	private XpDropStyle style;
	private float yOffset;
	private float xOffset;
	private float alpha;
	private float frame;
	private int hit;
	private Actor attachTo;
	// Can include predicted hit but is not 'just' a predicted hit.
	boolean isPredictedHit;
}
