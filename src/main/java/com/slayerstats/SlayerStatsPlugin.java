package com.slayerstats;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
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
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "Slayer Stats",
	description = "Track slayer session stats such as points gained per hour",
	tags = {"slayer", "combat", "skilling", "infobox"}
)
public class SlayerStatsPlugin extends Plugin
{
	private static final String CHAT_SUPERIOR_MESSAGE = "A superior foe has appeared...";

	@Inject
	private Client client;

	@Inject
	private SlayerStatsConfig config;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	private ItemManager itemManager;

	private final SlayerSession session = new SlayerSession();
	private final Map<SlayerStatsInfoBox.StatDisplay, SlayerStatsInfoBox> infoBoxes = new EnumMap<>(SlayerStatsInfoBox.StatDisplay.class);

	private int previousSlayerXp = -1;
	private int lastKnownSlayerPoints = -1;
	private int lastKnownTasksCompleted = -1;
	private int lastKnownWildernessTasksCompleted = -1;

	@Override
	protected void startUp()
	{
		previousSlayerXp = client.getSkillExperience(Skill.SLAYER);
		syncSlayerVarbits();
		updateInfoBoxes();
	}

	@Override
	protected void shutDown()
	{
		removeInfoBoxes();
		session.end();
		previousSlayerXp = -1;
		lastKnownSlayerPoints = -1;
		lastKnownTasksCompleted = -1;
		lastKnownWildernessTasksCompleted = -1;
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
				previousSlayerXp = -1;
				lastKnownSlayerPoints = -1;
				lastKnownTasksCompleted = -1;
				lastKnownWildernessTasksCompleted = -1;
				break;
			case LOGGED_IN:
				previousSlayerXp = client.getSkillExperience(Skill.SLAYER);
				syncSlayerVarbits();
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

		int currentXp = event.getXp();
		if (previousSlayerXp < 0)
		{
			previousSlayerXp = currentXp;
			return;
		}

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

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!SlayerStatsConfig.GROUP_NAME.equals(event.getGroup()))
		{
			return;
		}

		if (event.getKey().startsWith("show") && event.getKey().endsWith("Infobox"))
		{
			updateInfoBoxes();
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
			updateInfoBoxes();
		}
		else
		{
			session.recordXp(now);
		}
	}

	private void ensureSessionActive()
	{
		if (!session.isActive())
		{
			session.start(Instant.now());
			updateInfoBoxes();
		}
	}

	private void onSlayerPointsGained(int points)
	{
		ensureSessionActive();
		session.addPoints(points);
		session.recordXp(Instant.now());
		log.debug("Gained {} slayer points this session ({} total)", points, session.getPointsGained());
	}

	private void onSuperiorSpawned()
	{
		ensureSessionActive();
		session.addSuperior();
		session.recordXp(Instant.now());
		log.debug("Superior spawned this session ({} total)", session.getSuperiorsSpawned());
	}

	private void onTasksCompleted(int count)
	{
		ensureSessionActive();
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
		removeInfoBoxes();
	}

	private void updateInfoBoxes()
	{
		if (!session.isActive())
		{
			removeInfoBoxes();
			return;
		}

		BufferedImage icon = itemManager.getImage(ItemID.SLAYER_GEM);

		for (SlayerStatsInfoBox.StatDisplay display : SlayerStatsInfoBox.StatDisplay.values())
		{
			updateInfoBox(display, icon, isInfoboxEnabled(display));
		}
	}

	private boolean isInfoboxEnabled(SlayerStatsInfoBox.StatDisplay display)
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

	private void updateInfoBox(SlayerStatsInfoBox.StatDisplay display, BufferedImage icon, boolean enabled)
	{
		SlayerStatsInfoBox infoBox = infoBoxes.get(display);

		if (enabled)
		{
			if (infoBox == null)
			{
				infoBox = new SlayerStatsInfoBox(icon, this, session, config, display);
				infoBoxes.put(display, infoBox);
				infoBoxManager.addInfoBox(infoBox);
			}
		}
		else if (infoBox != null)
		{
			infoBoxManager.removeInfoBox(infoBox);
			infoBoxes.remove(display);
		}
	}

	private void removeInfoBoxes()
	{
		for (SlayerStatsInfoBox infoBox : infoBoxes.values())
		{
			infoBoxManager.removeInfoBox(infoBox);
		}
		infoBoxes.clear();
	}
}
