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
package net.runelite.client.plugins.tobqol.rooms.xarpus.commons;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import net.runelite.client.plugins.tobqol.api.game.Instance;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.runelite.api.NpcID;

import javax.annotation.Nullable;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public enum XarpusTable implements XarpusConstants
{
	XARPUS_INACTIVE(NpcID.XARPUS_10766, NpcID.XARPUS, NpcID.XARPUS_10770),
	XARPUS_P1(NpcID.XARPUS_10767, NpcID.XARPUS_8339, NpcID.XARPUS_10771),
	XARPUS_P23(NpcID.XARPUS_10768, NpcID.XARPUS_8340, NpcID.XARPUS_10772),
	XARPUS_DEAD(NpcID.XARPUS_10769, NpcID.XARPUS_8341, NpcID.XARPUS_10773);


	private final int sm;
	private final int rg;
	private final int hm;

	private static final Table<Instance.Mode, Integer, XarpusTable> TABLE;

	static
	{
		ImmutableTable.Builder<Instance.Mode, Integer, XarpusTable> builder = ImmutableTable.builder();

		for (XarpusTable table : values())
		{
			builder.put(Instance.Mode.STORY, table.sm, table);
			builder.put(Instance.Mode.REGULAR, table.rg, table);
			builder.put(Instance.Mode.HARD, table.hm, table);
		}

		TABLE = builder.build();
	}

	@Nullable
	public static Instance.Mode findMode(int npcId)
	{
		return Instance.findFirstMode(mode -> TABLE.contains(mode, npcId));
	}

	public static boolean anyMatch(XarpusTable table, int npcId)
	{
		return table != null && (table.sm == npcId || table.rg == npcId || table.hm == npcId);
	}
}
