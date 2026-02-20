package antcolony;

import antcolony.data.AntColonyConfig;
import antcolony.data.ColonyStats;
import antcolony.data.WorldTime;
import antcolony.entities.Ant;
import antcolony.entities.FallingLeaf;
import antcolony.entities.StaticTree;
import antcolony.environment.ColorScheme;
import antcolony.environment.EnvironmentRenderer;
import antcolony.environment.PheromoneField;
import antcolony.environment.Stars;
import antcolony.ui.SidebarLeft;
import antcolony.ui.SidebarRight;
import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Core of the "Ant Colony" simulation.
 * <p>
 * This class orchestrates all subsystems:
 * 1. Manages entities (Ants, Leaves, Trees).
 * 2. Controls time and environment (Seasons, Day/Night Cycle).
 * 3. Processes physics and game logic (Ant AI).
 * 4. Links the Graphical User Interface (UI) to the data.
 * 5. Delegates visual rendering.
 * </p>
 */
public class AntColonySimulation
{
    // --- Simulation State ---

    /** Indicates if the physics engine and time are currently frozen. */
    public boolean isPaused = false;

    // --- Entity Lists ---
    
    /** List of all living ants. */
    public ArrayList<Ant> ants = new ArrayList<>();
    
    /** List of leaves falling or on the ground (food). */
    public ArrayList<FallingLeaf> fallingLeaves = new ArrayList<>();
    
    /** List of static trees (scenery). */
    public ArrayList<StaticTree> forest = new ArrayList<>();
    
    /** List of background stars (environment). */
    public ArrayList<Stars> stars = new ArrayList<>();

    // --- World State ---
    
    private int lastStarRegenSeason = -1;
    
    public int cols;
    public int rows;
    public int resolution = AntColonyConfig.RESOLUTION;
    
    /** Pheromone grid (4 channels). */
    public PheromoneField pheromones;

    /** Y position of the ground surface. */
    public float surfaceY;
    
    /** Queen A location (Blue). */
    public PVector queenLocA;
    
    /** Queen B location (Red). */
    public PVector queenLocB;
    
    /** Colony A food stock. */
    public int foodStockA = 0;
    
    /** Colony B food stock. */
    public int foodStockB = 0;

    // --- Simulation Parameters (Controlled by UI) ---
    
    public float evapRateA;
    public float evapRateB;
    public float metaA;
    public float metaB;
    public int costA;
    public int costB;

    // --- Subsystems ---
    
    public WorldTime time = new WorldTime();
    public ColonyStats statsA = new ColonyStats();
    public ColonyStats statsB = new ColonyStats();
    public ColorScheme colors = new ColorScheme();
    public EnvironmentRenderer renderer = new EnvironmentRenderer();
    
    // --- Graphical User Interface (UI) ---
    
    public SidebarLeft sidebarLeft;
    public SidebarRight sidebarRight;

    // UI Dimensions
    public int leftSidebarW = AntColonyConfig.LEFT_SIDEBAR_W;
    public int rightSidebarW = AntColonyConfig.RIGHT_SIDEBAR_W;
    
    // Reset Button (Position calculated in setup)
    public float btnResetX;
    public float btnResetY;
    public float btnResetW;
    public float btnResetH;

    /**
     * Generates stars in the sky.
     * Called at start and whenever the season changes (for variety).
     */
    public void initStars(PApplet p)
    {
        stars.clear();
        
        float xMin = leftSidebarW;
        float xMax = p.width - rightSidebarW;
        float yMin = 10;
        float yMax = surfaceY - 40;
        int N = 140;
        
        for (int i = 0; i < N; i++)
        {
            stars.add(new Stars(p, xMin, xMax, yMin, yMax));
        }
        
        lastStarRegenSeason = time.curSeasonIdx;
    }

    /**
     * Generates the background forest with fixed positions.
     */
    private void initForest(PApplet p)
    {
        forest.clear();
        
        float playableStart = leftSidebarW;
        float playableWidth = p.width - leftSidebarW - rightSidebarW;
        
        // Percentage positions to distribute trees aesthetically
        float[] treePosPerc = { 0.1f, 0.3f, 0.5f, 0.75f, 0.9f };
        float[] treeSizes = { 55, 45, 65, 50, 55 };
        
        for (int i = 0; i < treePosPerc.length; i++)
        {
            float actualX = playableStart + (playableWidth * treePosPerc[i]);
            forest.add(new StaticTree(p, actualX, surfaceY, treeSizes[i]));
        }
    }

    /**
     * Initial simulation configuration.
     * Called only once at application startup.
     */
    public void setup(PApplet p)
    {
        // 1. Physical World Definition
        surfaceY = p.height * 0.35f;
        cols = p.width / resolution;
        rows = p.height / resolution;
        
        pheromones = new PheromoneField(cols, rows, resolution);

        float playableStart = leftSidebarW;
        float playableWidth = p.width - leftSidebarW - rightSidebarW;
        
        // Position queens at 25% and 75% of the playable area
        queenLocA = new PVector(playableStart + playableWidth * 0.25f, p.height - 30);
        queenLocB = new PVector(playableStart + playableWidth * 0.75f, p.height - 30);

        pheromones.reset(queenLocA, queenLocB);
        initForest(p);

        // 2. Initial Population Creation
        ants.clear();
        for (int i = 0; i < AntColonyConfig.INITIAL_ANTS / 2; i++)
        {
            spawnAnt(p, 0); // Blue Colony
            spawnAnt(p, 1); // Red Colony
        }
        
        // 3. UI Configuration
        btnResetW = 220;
        btnResetH = 30;
        btnResetX = (leftSidebarW - btnResetW) / 2f;
        btnResetY = p.height - 55;

        sidebarLeft = new SidebarLeft(this);
        sidebarRight = new SidebarRight(this);

        // 4. Auxiliary Systems Initialization
        time.recalc(statsA, statsB);
        colors.updateSeasonalColors(p, this);
        initStars(p);
        renderer.resetTexture();
    }

    /**
     * Restarts the simulation while maintaining UI settings.
     */
    public void resetSimulation(PApplet p)
    {
        ants.clear();
        fallingLeaves.clear();
        
        foodStockA = 0;
        foodStockB = 0;
        
        time.reset();
        statsA.reset();
        statsB.reset();
        
        pheromones.reset(queenLocA, queenLocB);
        initForest(p);

        for (int i = 0; i < AntColonyConfig.INITIAL_ANTS / 2; i++)
        {
            spawnAnt(p, 0);
            spawnAnt(p, 1);
        }

        // Ensure simulation is unpaused on reset
        isPaused = false;
    }

    /**
     * Creates a new ant at the corresponding queen's position.
     */
    public void spawnAnt(PApplet p, int colonyId)
    {
        PVector q;
        if (colonyId == 0)
        {
            q = queenLocA;
        }
        else
        {
            q = queenLocB;
        }
        
        ants.add(new Ant(p, this, q.x, q.y, colonyId));
        
        // Record birth statistics
        if (colonyId == 0)
        {
            statsA.registerBirth();
        }
        else
        {
            statsB.registerBirth();
        }
    }

    /**
     * Input Management: Mouse.
     */
    public void mousePressed(PApplet p)
    {
        if (sidebarLeft.isResetHit(p.mouseX, p.mouseY))
        {
            resetSimulation(p);
        }
    }

    /**
     * Input Management: Keyboard.
     */
    public void keyPressed(PApplet p)
    {
        if (p.key == 'r' || p.key == 'R')
        {
            resetSimulation(p);
        }

        // SPACEBAR to toggle pause
        if (p.key == ' ')
        {
            if (isPaused == true)
            {
                isPaused = false;
            }
            else
            {
                isPaused = true;
            }
        }
    }

    /**
     * Main Simulation Loop.
     * Executed every frame (typically 60 FPS).
     */
    public void draw(PApplet p, float dt)
    {
        // 1. UPDATE LEFT UI (Sliders) - Always updated to allow real-time tweaks during pause
        sidebarLeft.update(p.mouseX, p.mouseY, p.mousePressed);

        // 2. READ SLIDER VALUES (Real-time parameter updates)
        metaA = sidebarLeft.metaASlider.value;
        costA = (int) sidebarLeft.costASlider.value;
        evapRateA = sidebarLeft.evapASlider.value;
        
        metaB = sidebarLeft.metaBSlider.value;
        costB = (int) sidebarLeft.costBSlider.value;
        evapRateB = sidebarLeft.evapBSlider.value;

        // 3. PHYSICS AND LOGIC (Sub-stepping)
        // Only executed if the simulation is NOT paused
        if (isPaused == false)
        {
            int simSpeed = (int) sidebarLeft.speedSlider.value;
            
            for (int k = 0; k < simSpeed; k++)
            {
                updatePhysics(p, sidebarLeft.leafSlider.value, dt);
            }
        }

        // 4. ENVIRONMENTAL LOGIC
        time.recalc(statsA, statsB);
        colors.updateSeasonalColors(p, this);

        // 5. RENDERING (Always active to allow observation/analysis during pause)
        renderer.drawSky(p, this);
        renderer.drawCelestialBodies(p, this);
        renderer.drawForestAndGround(p, this);
        renderer.drawPheromones(p, this);
        
        renderer.drawQueen(p, queenLocA, 0);
        renderer.drawQueen(p, queenLocB, 1);
        
        renderer.drawFallingLeaves(p, this);
        renderer.drawAnts(p, this);

        // 6. UI OVERLAY (Drawn on top of everything)
        sidebarLeft.draw(p);
        sidebarRight.draw(p);
    }

    /**
     * Physics Step.
     * Contains all logic that should be accelerated by the speed slider.
     */
    public void updatePhysics(PApplet p, float leafRate, float dt)
    {
        // Advance time
        time.tick(1.0f);
        
        // Process pheromones and environment
        pheromones.evaporate(p, this);
        updateSkyActors(p);

        // Animate roots
        for (StaticTree t : forest)
        {
            t.updateRoots(dt);
        }

        // --- Leaf Spawning Management ---
        // Calculate modifier based on season
        float seasonMod;
        if (time.curSeasonIdx == 2)
        {
            seasonMod = 1.5f;  // Autumn: High leaf count
        }
        else if (time.curSeasonIdx == 3)
        {
            seasonMod = 0.05f; // Winter: Almost none
        }
        else
        {
            seasonMod = 0.2f;  // Spring/Summer: Normal
        }

        if (p.random(1) < leafRate * seasonMod)
        {
            PVector spawn = getLeafSpawnPoint(p);
            fallingLeaves.add(new FallingLeaf(p, this, spawn));
        }

        // --- Leaf Update (Safe iteration with removal) ---
        Iterator<FallingLeaf> lit = fallingLeaves.iterator();
        while (lit.hasNext())
        {
            FallingLeaf l = lit.next();
            l.update(p, this, dt);
            
            // Remove if the leaf has been fully consumed
            if (l.amount <= 0)
            {
                lit.remove();
            }
        }

        // --- Ant Update ---
        Iterator<Ant> ait = ants.iterator();
        while (ait.hasNext())
        {
            Ant a = ait.next();
            a.run(p, this);
            
            if (a.isDead())
            {
                ait.remove();
                
                // Record death in statistics
                if (a.colonyId == 0)
                {
                    statsA.registerDeath();
                }
                else
                {
                    statsB.registerDeath();
                }
            }
        }

        // --- Colony Reproduction ---
        int MAX_PER_COLONY = 1000; // Performance safety limit
        
        long countA = ants.stream().filter(a -> a.colonyId == 0).count();
        long countB = ants.stream().filter(a -> a.colonyId == 1).count();

        // Colony A attempts to create a new ant
        if (countA < MAX_PER_COLONY && foodStockA >= costA)
        {
            foodStockA -= costA;
            spawnAnt(p, 0);
        }
        
        // Colony B attempts to create a new ant
        if (countB < MAX_PER_COLONY && foodStockB >= costB)
        {
            foodStockB -= costB;
            spawnAnt(p, 1);
        }
    }

    /**
     * Checks if starry sky regeneration is required (seasonal change).
     */
    public void updateSkyActors(PApplet p)
    {
        if (time.curSeasonIdx != lastStarRegenSeason)
        {
            initStars(p);
        }
    }

    /**
     * Finds a valid position on a tree to spawn a falling leaf.
     */
    public PVector getLeafSpawnPoint(PApplet p)
    {
        if (forest.isEmpty())
        {
            return new PVector(p.width / 2, 50);
        }
        
        // Select a random tree
        StaticTree t = forest.get((int) p.random(forest.size()));
        
        if (t.leafPositions == null || t.leafPositions.isEmpty())
        {
            return new PVector(p.width / 2, 50);
        }
        
        // Select a leaf from that tree as the origin point
        PVector lp = t.leafPositions.get((int) p.random(t.leafPositions.size()));
        
        // Add minor variation so they don't all originate from the exact same pixel
        return new PVector(lp.x + p.random(-2, 2), lp.y + p.random(-2, 2));
    }
}