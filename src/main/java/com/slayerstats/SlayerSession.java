package com.slayerstats;

import java.time.Duration;
import java.time.Instant;
import lombok.Getter;

class SlayerSession
{
	private static final long MIN_SECONDS_FOR_RATE = 60;

	@Getter
	private boolean active;

	@Getter
	private Instant startTime;

	@Getter
	private Instant lastXpTime;

	@Getter
	private int pointsGained;

	@Getter
	private int superiorsSpawned;

	@Getter
	private int tasksCompleted;

	void start(Instant now)
	{
		active = true;
		startTime = now;
		lastXpTime = now;
		pointsGained = 0;
		superiorsSpawned = 0;
		tasksCompleted = 0;
	}

	void end()
	{
		active = false;
		startTime = null;
		lastXpTime = null;
	}

	void recordXp(Instant now)
	{
		lastXpTime = now;
	}

	void addPoints(int points)
	{
		pointsGained += points;
	}

	void addSuperior()
	{
		superiorsSpawned++;
	}

	void addTasks(int count)
	{
		tasksCompleted += count;
	}

	Duration getActiveDuration()
	{
		if (!active || startTime == null)
		{
			return Duration.ZERO;
		}

		return Duration.between(startTime, Instant.now());
	}

	double getPointsPerHour()
	{
		return perHour(pointsGained);
	}

	double getSuperiorsPerHour()
	{
		return perHour(superiorsSpawned);
	}

	double getTasksPerHour()
	{
		return perHour(tasksCompleted);
	}

	private double perHour(int count)
	{
		long seconds = getActiveDuration().getSeconds();
		if (seconds < MIN_SECONDS_FOR_RATE)
		{
			return 0;
		}

		return count * 3600.0 / seconds;
	}
}
