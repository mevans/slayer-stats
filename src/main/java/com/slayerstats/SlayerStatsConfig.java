package com.slayerstats;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Units;

@ConfigGroup(SlayerStatsConfig.GROUP_NAME)
public interface SlayerStatsConfig extends Config
{
	String GROUP_NAME = "slayerstats";

	@ConfigItem(
		keyName = "showOverlay",
		name = "Show overlay",
		description = "Display slayer session stats in an on-screen overlay panel"
	)
	default boolean showOverlay()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showPointsPerHour",
		name = "Show points per hour",
		description = "Show session slayer points per hour in the overlay"
	)
	default boolean showPointsPerHour()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showSessionPoints",
		name = "Show session points",
		description = "Show slayer points gained this session in the overlay"
	)
	default boolean showSessionPoints()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showSuperiorsSpawned",
		name = "Show superiors spawned",
		description = "Show superior slayer monsters spawned this session in the overlay"
	)
	default boolean showSuperiorsSpawned()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showSuperiorsPerHour",
		name = "Show superiors per hour",
		description = "Show superior spawn rate this session in the overlay"
	)
	default boolean showSuperiorsPerHour()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showTasksCompleted",
		name = "Show tasks completed",
		description = "Show slayer tasks completed this session in the overlay"
	)
	default boolean showTasksCompleted()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showTasksPerHour",
		name = "Show tasks per hour",
		description = "Show slayer task completion rate this session in the overlay"
	)
	default boolean showTasksPerHour()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showExpeditiousProcs",
		name = "Show expeditious procs",
		description = "Show expeditious bracelet procs this session in the overlay"
	)
	default boolean showExpeditiousProcs()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showExpeditiousPerHour",
		name = "Show expeditious procs per hour",
		description = "Show expeditious bracelet proc rate this session in the overlay"
	)
	default boolean showExpeditiousPerHour()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showSlaughterProcs",
		name = "Show slaughter procs",
		description = "Show bracelet of slaughter procs this session in the overlay"
	)
	default boolean showSlaughterProcs()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showSlaughterPerHour",
		name = "Show slaughter procs per hour",
		description = "Show bracelet of slaughter proc rate this session in the overlay"
	)
	default boolean showSlaughterPerHour()
	{
		return true;
	}

	@ConfigItem(
		keyName = "sessionIdleTimeout",
		name = "Session idle timeout",
		description = "End the slayer session after this many minutes without slayer XP"
	)
	@Units(" mins")
	default int sessionIdleTimeout()
	{
		return 5;
	}
}
