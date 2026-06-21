package com.slayerstats;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.time.Duration;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

class SlayerStatsOverlay extends OverlayPanel
{
	private final SlayerStatsPlugin plugin;
	private final SlayerStatsConfig config;

	@Inject
	private SlayerStatsOverlay(SlayerStatsPlugin plugin, SlayerStatsConfig config)
	{
		super(plugin);
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.TOP_LEFT);
		addMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Slayer Stats overlay");
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		SlayerSession session = plugin.getSession();
		if (!config.showOverlay() || !session.isActive())
		{
			return null;
		}

		panelComponent.getChildren().add(TitleComponent.builder()
			.text("Slayer Stats")
			.build());

		if (config.showSessionPoints())
		{
			addLine("Points:", Integer.toString(session.getPointsGained()));
		}

		if (config.showPointsPerHour())
		{
			addLine("Points/hr:", String.format("%.1f", session.getPointsPerHour()));
		}

		if (config.showTasksCompleted())
		{
			addLine("Tasks:", Integer.toString(session.getTasksCompleted()));
		}

		if (config.showTasksPerHour())
		{
			addLine("Tasks/hr:", String.format("%.1f", session.getTasksPerHour()));
		}

		if (config.showSuperiorsSpawned())
		{
			addLine("Superiors:", Integer.toString(session.getSuperiorsSpawned()));
		}

		if (config.showSuperiorsPerHour())
		{
			addLine("Superiors/hr:", String.format("%.1f", session.getSuperiorsPerHour()));
		}

		if (config.showExpeditiousProcs())
		{
			addLine("Expeditious:", Integer.toString(session.getExpeditiousProcs()));
		}

		if (config.showExpeditiousPerHour())
		{
			addLine("Expeditious/hr:", String.format("%.1f", session.getExpeditiousPerHour()));
		}

		if (config.showSlaughterProcs())
		{
			addLine("Slaughter:", Integer.toString(session.getSlaughterProcs()));
		}

		if (config.showSlaughterPerHour())
		{
			addLine("Slaughter/hr:", String.format("%.1f", session.getSlaughterPerHour()));
		}

		Duration duration = session.getActiveDuration();
		long minutes = duration.toMinutes();
		long seconds = duration.minusMinutes(minutes).getSeconds();
		addLine("Duration:", String.format("%dm %ds", minutes, seconds));

		return super.render(graphics);
	}

	private void addLine(String left, String right)
	{
		panelComponent.getChildren().add(LineComponent.builder()
			.left(left)
			.right(right)
			.build());
	}
}
