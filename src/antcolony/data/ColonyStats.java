package antcolony.data;

/**
 * Maintains the vital statistics of a specific colony.
 * <p>
 * This class operates on a double-buffering system: it accumulates temporary 
 * data during the simulated day and, at midnight, archives this data 
 * for display in the graphical interface, resetting the counters 
 * for the following day.
 * </p>
 */
public class ColonyStats
{
    // --- Current day counters (reset at midnight) ---

    /**
     * Food collected accumulated during the current day.
     */
    public int tempFood = 0;

    /**
     * Births recorded during the current day.
     */
    public int tempBirths = 0;

    /**
     * Deaths recorded during the current day.
     */
    public int tempDeaths = 0;

    // --- Previous day records (for Sidebar display) ---

    /**
     * Total food from the previous day (static value for UI display).
     */
    public int dailyFood = 0;

    /**
     * Total births from the previous day (static value for UI display).
     */
    public int dailyBirths = 0;

    /**
     * Total deaths from the previous day (static value for UI display).
     */
    public int dailyDeaths = 0;

    /**
     * Records the collection of one food unit.
     * Increments the temporary counter.
     */
    public void registerFood()
    {
        tempFood++;
    }

    /**
     * Records the birth of a new ant.
     * Increments the temporary counter.
     */
    public void registerBirth()
    {
        tempBirths++;
    }

    /**
     * Records the death of an ant.
     * Increments the temporary counter.
     */
    public void registerDeath()
    {
        tempDeaths++;
    }

    /**
     * Finalizes the daily statistics cycle.
     * <p>
     * This method should be called by {@link WorldTime} at midnight (00:00).
     * It transfers temporary values to daily records and clears 
     * the accumulators for the new day.
     * </p>
     */
    public void endOfDay()
    {
        // Update values visible in the UI with the total from the day that just ended
        dailyFood = tempFood;
        dailyBirths = tempBirths;
        dailyDeaths = tempDeaths;

        // Reset counters to start counting for the new day
        tempFood = 0;
        tempBirths = 0;
        tempDeaths = 0;
    }

    /**
     * Resets all colony statistics.
     * <p>
     * Clears both current accumulators and historical records.
     * Should be called when the simulation is restarted by the user.
     * </p>
     */
    public void reset()
    {
        tempFood = 0;
        tempBirths = 0;
        tempDeaths = 0;
        
        dailyFood = 0;
        dailyBirths = 0;
        dailyDeaths = 0;
    }
}