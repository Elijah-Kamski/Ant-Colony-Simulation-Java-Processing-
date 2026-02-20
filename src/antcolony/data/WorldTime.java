package antcolony.data;

/**
 * Simulation time manager.
 * <p>
 * This class converts continuous time (ticks) into readable calendar units
 * (Days, Hours, Minutes, Seasons). It is responsible for notifying
 * colony statistics when a new day begins.
 * </p>
 */
public class WorldTime
{
    /**
     * Global elapsed time counter (in ticks).
     * This value increments continuously with every frame.
     */
    public float worldTime = 0;

    /**
     * Day duration in ticks (Copy of configuration for local access).
     */
    public int dayLength = AntColonyConfig.DAY_LENGTH;

    /**
     * Season duration in days (Copy of configuration).
     */
    public int seasonLengthDays = AntColonyConfig.SEASON_LENGTH_DAYS;

    // --- Calendar Variables ---

    /**
     * Current simulation day (starts at 1).
     */
    public int curDay = 1;

    /**
     * Current hour of the day (0-23).
     */
    public int curHour = 0;

    /**
     * Current minute of the hour (0-59).
     */
    public int curMin = 0;

    /**
     * Current season index (0 to 3).
     */
    public int curSeasonIdx = 0;

    // --- Progress Variables (0.0 to 1.0) ---

    /**
     * Completion percentage of the current day (0.0f to 1.0f).
     * Useful for color interpolation (day/night cycle).
     */
    public float dayProgress = 0f;

    /**
     * Completion percentage of the current season (0.0f to 1.0f).
     * Useful for visual transitions between seasons.
     */
    public float seasonProgress = 0f;

    /**
     * Season names for UI display.
     */
    public final String[] seasonNames = { "Spring", "Summer", "Autumn", "Winter" };

    /**
     * Internal record to detect day changes.
     */
    private int lastDayChecked = 0;

    /**
     * Resets the simulation time to the initial state (Day 1, 00:00).
     */
    public void reset()
    {
        worldTime = 0;
        curDay = 1;
        curHour = 0;
        curMin = 0;
        curSeasonIdx = 0;
        dayProgress = 0;
        seasonProgress = 0;
        lastDayChecked = 0;
    }

    /**
     * Advances the simulation clock.
     * @param amount Amount of time to add (affected by the speed slider).
     */
    public void tick(float amount)
    {
        worldTime += amount;
    }

    /**
     * Recalculates calendar variables based on total elapsed time.
     * <p>
     * This method converts the accumulated `worldTime` into days, hours, and minutes.
     * It also checks if the day has changed since the last frame and, if so, triggers
     * the daily statistics wrap-up for the colonies.
     * </p>
     * @param statsA Blue Colony statistics (for end-of-day notification).
     * @param statsB Red Colony statistics (for end-of-day notification).
     */
    public void recalc(ColonyStats statsA, ColonyStats statsB)
    {
        // 1. Calculate current day and hour
        curDay = (int) (worldTime / dayLength) + 1;
        
        int mins = (int) (worldTime % dayLength);
        curHour = mins / 60;
        curMin = mins % 60;

        // 2. Calculate day progress (0.0 to 1.0)
        dayProgress = (float) mins / (float) dayLength;

        // 3. Calculate Season
        int totalDaysPassed = curDay - 1;
        int daysInCurrentSeason = totalDaysPassed % seasonLengthDays;
        
        // The % 4 operator ensures the index stays between 0 and 3
        curSeasonIdx = (totalDaysPassed / seasonLengthDays) % 4;
        
        // Season progress (days passed + fraction of the current day)
        seasonProgress = (daysInCurrentSeason + dayProgress) / (float) seasonLengthDays;

        // 4. Check for day change ("Midnight" Event)
        if (curDay > lastDayChecked)
        {
            // If we are at startup (day 1 and lastDay 0), we avoid resetting immediately,
            // but if curDay increased, it means we passed midnight.
            if (lastDayChecked > 0) 
            {
                statsA.endOfDay();
                statsB.endOfDay();
            }
            
            lastDayChecked = curDay;
        }
    }
}