package com.slayerstats;

import com.google.inject.Provides;
import java.time.Duration;
import java.time.Instant;
import java.util.function.IntConsumer;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "Slayer Stats",
	description = "Track slayer session stats such as points gained per hour",
	tags = {"slayer", "combat", "skilling", "overlay"}
)
public class SlayerStatsPlugin extends Plugin
{
	private static final String CHAT_SUPERIOR_MESSAGE = "A superior foe has appeared...";

	@Inject
	private Client client;

	@Inject
	private SlayerStatsConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private SlayerStatsOverlay overlay;

	private final SlayerSession session = new SlayerSession();

	private static final int LOGIN_SYNC_TICKS = 2;

	private int previousSlayerXp = -1;
	private int pendingSlayerXp = -1;
	private int lastKnownSlayerPoints = -1;
	private int lastKnownTasksCompleted = -1;
	private int lastKnownWildernessTasksCompleted = -1;
	private int ticksUntilLoginReady = -1;
	private boolean slayerXpReady;
	private boolean slayerVarbitsReady;

	SlayerSession getSession()
	{
		return session;
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			prepareForLogin();
		}
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		session.end();
		resetTrackingState();
	}

	@Provides
	SlayerStatsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SlayerStatsConfig.class);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		switch (event.getGameState())
		{
			case LOGGING_IN:
				endSession();
				resetTrackingState();
				break;
			case LOGGED_IN:
				prepareForLogin();
				break;
			case HOPPING:
			case CONNECTION_LOST:
				endSession();
				break;
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		if (event.getSkill() != Skill.SLAYER || client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		if (!slayerXpReady)
		{
			pendingSlayerXp = event.getXp();
			return;
		}

		int currentXp = event.getXp();
		if (currentXp <= previousSlayerXp)
		{
			previousSlayerXp = currentXp;
			return;
		}

		previousSlayerXp = currentXp;
		onSlayerXpGained();
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM)
		{
			return;
		}

		if (!CHAT_SUPERIOR_MESSAGE.equals(Text.removeTags(event.getMessage())))
		{
			return;
		}

		onSuperiorSpawned();
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (!slayerVarbitsReady)
		{
			return;
		}

		switch (event.getVarbitId())
		{
			case VarbitID.SLAYER_POINTS:
				trackPositiveDelta(
					event.getValue(),
					lastKnownSlayerPoints,
					this::onSlayerPointsGained,
					value -> lastKnownSlayerPoints = value
				);
				break;
			case VarbitID.SLAYER_TASKS_COMPLETED:
				trackPositiveDelta(
					event.getValue(),
					lastKnownTasksCompleted,
					this::onTasksCompleted,
					value -> lastKnownTasksCompleted = value
				);
				break;
			case VarbitID.SLAYER_WILDERNESS_TASKS_COMPLETED:
				trackPositiveDelta(
					event.getValue(),
					lastKnownWildernessTasksCompleted,
					this::onTasksCompleted,
					value -> lastKnownWildernessTasksCompleted = value
				);
				break;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (ticksUntilLoginReady >= 0)
		{
			if (ticksUntilLoginReady == 0)
			{
				ticksUntilLoginReady = -1;
				if (client.getGameState() == GameState.LOGGED_IN)
				{
					finishLoginSync();
				}
			}
			else
			{
				ticksUntilLoginReady--;
			}
		}

		if (!session.isActive() || config.sessionIdleTimeout() <= 0)
		{
			return;
		}

		Instant lastXpTime = session.getLastXpTime();
		if (lastXpTime == null)
		{
			return;
		}

		Duration idleTime = Duration.between(lastXpTime, Instant.now());
		if (idleTime.compareTo(Duration.ofMinutes(config.sessionIdleTimeout())) >= 0)
		{
			log.debug("Ending slayer session after {} minutes idle", idleTime.toMinutes());
			endSession();
		}
	}

	private void trackPositiveDelta(int newValue, int lastKnown, IntConsumer onPositiveDelta, IntConsumer lastKnownUpdater)
	{
		if (lastKnown < 0)
		{
			lastKnownUpdater.accept(newValue);
			return;
		}

		int delta = newValue - lastKnown;
		lastKnownUpdater.accept(newValue);

		if (delta > 0)
		{
			onPositiveDelta.accept(delta);
		}
	}

	private void resetTrackingState()
	{
		previousSlayerXp = -1;
		pendingSlayerXp = -1;
		lastKnownSlayerPoints = -1;
		lastKnownTasksCompleted = -1;
		lastKnownWildernessTasksCompleted = -1;
		ticksUntilLoginReady = -1;
		slayerXpReady = false;
		slayerVarbitsReady = false;
	}

	private void prepareForLogin()
	{
		endSession();
		previousSlayerXp = -1;
		pendingSlayerXp = -1;
		slayerXpReady = false;
		slayerVarbitsReady = false;
		ticksUntilLoginReady = LOGIN_SYNC_TICKS;
	}

	private void finishLoginSync()
	{
		previousSlayerXp = pendingSlayerXp >= 0
			? pendingSlayerXp
			: client.getSkillExperience(Skill.SLAYER);
		pendingSlayerXp = -1;
		slayerXpReady = true;
		syncSlayerVarbits();
		slayerVarbitsReady = true;
	}

	private void syncSlayerVarbits()
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		lastKnownSlayerPoints = client.getVarbitValue(VarbitID.SLAYER_POINTS);
		lastKnownTasksCompleted = client.getVarbitValue(VarbitID.SLAYER_TASKS_COMPLETED);
		lastKnownWildernessTasksCompleted = client.getVarbitValue(VarbitID.SLAYER_WILDERNESS_TASKS_COMPLETED);
	}

	private void onSlayerXpGained()
	{
		Instant now = Instant.now();
		if (!session.isActive())
		{
			session.start(now);
			log.debug("Started slayer session");
		}
		else
		{
			session.recordXp(now);
		}
	}

	private void onSlayerPointsGained(int points)
	{
		if (!session.isActive())
		{
			return;
		}

		session.addPoints(points);
		session.recordXp(Instant.now());
		log.debug("Gained {} slayer points this session ({} total)", points, session.getPointsGained());
	}

	private void onSuperiorSpawned()
	{
		if (!session.isActive())
		{
			return;
		}

		session.addSuperior();
		session.recordXp(Instant.now());
		log.debug("Superior spawned this session ({} total)", session.getSuperiorsSpawned());
	}

	private void onTasksCompleted(int count)
	{
		if (!session.isActive())
		{
			return;
		}

		session.addTasks(count);
		session.recordXp(Instant.now());
		log.debug("Completed {} slayer task(s) this session ({} total)", count, session.getTasksCompleted());
	}

	private void endSession()
	{
		if (!session.isActive())
		{
			return;
		}

		session.end();
	}
}
