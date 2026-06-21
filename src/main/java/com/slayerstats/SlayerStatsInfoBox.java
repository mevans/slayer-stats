package com.slayerstats;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.time.Duration;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.infobox.InfoBox;

class SlayerStatsInfoBox extends InfoBox
{
	enum StatDisplay
	{
		POINTS_PER_HOUR,
		SESSION_POINTS,
		SUPERIORS_SPAWNED,
		SUPERIORS_PER_HOUR,
		TASKS_COMPLETED,
		TASKS_PER_HOUR
	}

	private final SlayerSession session;
	private final SlayerStatsConfig config;
	private final StatDisplay display;

	SlayerStatsInfoBox(
		BufferedImage image,
		Plugin plugin,
		SlayerSession session,
		SlayerStatsConfig config,
		StatDisplay display
	)
	{
		super(image, plugin);
		this.session = session;
		this.config = config;
		this.display = display;
	}

	@Override
	public String getName()
	{
		return super.getName() + "_" + display.name();
	}

	@Override
	public String getText()
	{
		if (!session.isActive())
		{
			return "-";
		}

		switch (display)
		{
			case SESSION_POINTS:
				return Integer.toString(session.getPointsGained());
			case SUPERIORS_SPAWNED:
				return Integer.toString(session.getSuperiorsSpawned());
			case TASKS_COMPLETED:
				return Integer.toString(session.getTasksCompleted());
			case SUPERIORS_PER_HOUR:
				return String.format("%.1f", session.getSuperiorsPerHour());
			case TASKS_PER_HOUR:
				return String.format("%.1f", session.getTasksPerHour());
			case POINTS_PER_HOUR:
			default:
				return String.format("%.1f", session.getPointsPerHour());
		}
	}

	@Override
	public Color getTextColor()
	{
		return Color.WHITE;
	}

	@Override
	public String getTooltip()
	{
		if (!session.isActive())
		{
			return "Slayer Stats";
		}

		Duration duration = session.getActiveDuration();
		long minutes = duration.toMinutes();
		long seconds = duration.minusMinutes(minutes).getSeconds();

		return String.format(
			"<html>Slayer session<br/>"
				+ "Points: %d (%.1f/h)<br/>"
				+ "Tasks: %d (%.1f/h)<br/>"
				+ "Superiors: %d (%.1f/h)<br/>"
				+ "Duration: %dm %ds</html>",
			session.getPointsGained(),
			session.getPointsPerHour(),
			session.getTasksCompleted(),
			session.getTasksPerHour(),
			session.getSuperiorsSpawned(),
			session.getSuperiorsPerHour(),
			minutes,
			seconds
		);
	}

	@Override
	public boolean render()
	{
		switch (display)
		{
			case SESSION_POINTS:
				return config.showSessionPointsInfobox();
			case SUPERIORS_SPAWNED:
				return config.showSuperiorsSpawnedInfobox();
			case SUPERIORS_PER_HOUR:
				return config.showSuperiorsPerHourInfobox();
			case TASKS_COMPLETED:
				return config.showTasksCompletedInfobox();
			case TASKS_PER_HOUR:
				return config.showTasksPerHourInfobox();
			case POINTS_PER_HOUR:
			default:
				return config.showPointsPerHourInfobox();
		}
	}
}
