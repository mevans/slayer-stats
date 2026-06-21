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
		keyName = "showPointsPerHourInfobox",
		name = "Show points per hour",
		description = "Display session slayer points per hour in an infobox"
	)
	default boolean showPointsPerHourInfobox()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showSessionPointsInfobox",
		name = "Show session points",
		description = "Display slayer points gained this session in an infobox"
	)
	default boolean showSessionPointsInfobox()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showSuperiorsSpawnedInfobox",
		name = "Show superiors spawned",
		description = "Display superior slayer monsters spawned this session in an infobox"
	)
	default boolean showSuperiorsSpawnedInfobox()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showSuperiorsPerHourInfobox",
		name = "Show superiors per hour",
		description = "Display superior spawn rate this session in an infobox"
	)
	default boolean showSuperiorsPerHourInfobox()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showTasksCompletedInfobox",
		name = "Show tasks completed",
		description = "Display slayer tasks completed this session in an infobox"
	)
	default boolean showTasksCompletedInfobox()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showTasksPerHourInfobox",
		name = "Show tasks per hour",
		description = "Display slayer task completion rate this session in an infobox"
	)
	default boolean showTasksPerHourInfobox()
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
