package antcolony.entities;

import antcolony.AntColonySimulation;
import processing.core.PApplet;
import processing.core.PVector;

/**
 * Represents an autonomous agent (ant) in the simulation.
 * <p>
 * The ant possesses a finite state machine (Search, Return, Wander),
 * manages its own energy, and interacts with the environment through 
 * pheromones and food detection.
 * </p>
 */
public class Ant
{
    /**
     * Enumeration of the possible states for the ant's AI.
     */
    public enum AntState
    {
        SEARCHING,
        RETURNING,
        WANDERING
    }

    /**
     * Current state of the ant.
     */
    public AntState state = AntState.SEARCHING;

    /**
     * Physics component controlling movement and position.
     */
    public AntPhysics phys;

    /**
     * Indicates whether the ant is carrying food.
     */
    public boolean hasFood = false;

    /**
     * Current energy of the ant. If it reaches 0, the ant dies.
     */
    public float nrg;

    /**
     * Maximum allowed energy.
     */
    public float maxNrg = 1500;

    /**
     * Current age in frames/ticks.
     */
    public float age = 0;

    /**
     * Maximum age (lifespan) randomly defined at birth.
     */
    public float maxAge;

    /**
     * Strength of the pheromone the ant is currently leaving in its trail.
     * Decays over time.
     */
    public float pherStr = 1.0f;

    /**
     * Colony identifier (0 = Blue/A, 1 = Red/B).
     */
    public int colonyId;

    /**
     * Olfactory detection radius for finding food.
     */
    public float smellRadius = 120.0f;

    // --- Pheromone Channels (Calculated in the constructor) ---
    
    private int homeChannel;
    private int foodChannel;

    /**
     * Ant Constructor.
     * @param p Reference to the PApplet (for randomness functions).
     * @param sim Reference to the main simulation (for config access).
     * @param x Initial X position.
     * @param y Initial Y position.
     * @param colId ID of the colony it belongs to.
     */
    public Ant(PApplet p, AntColonySimulation sim, float x, float y, int colId)
    {
        this.colonyId = colId;
        
        // Calculates channels based on ID (0->0,1 | 1->2,3)
        this.homeChannel = colId * 2;
        this.foodChannel = colId * 2 + 1;
        
        this.phys = new AntPhysics(p, x, y);
        
        this.nrg = maxNrg;
        // Defines a random life expectancy for population variety
        this.maxAge = p.random(2000, 5000);
        this.state = AntState.SEARCHING;
    }

    /**
     * Checks if the ant should be removed from the simulation.
     * @return true if it died of hunger or old age.
     */
    public boolean isDead()
    {
        if (nrg <= 0)
        {
            return true;
        }
        
        if (age >= maxAge)
        {
            return true;
        }
        
        return false;
    }

    /**
     * Updates the main ant logic (AI, Physics, and Metabolism).
     * @param p Reference to the PApplet.
     * @param sim Reference to the simulation.
     */
    public void run(PApplet p, AntColonySimulation sim)
    {
        age++;

        // 1. Metabolism: Energy consumption based on colony configuration
        float meta;
        if (colonyId == 0)
        {
            meta = sim.metaA;
        }
        else
        {
            meta = sim.metaB;
        }
        
        nrg -= meta;

        // 2. Decay of the ant's own pheromone trail strength
        if (pherStr > 0)
        {
            pherStr = Math.max(0, pherStr - 0.002f);
        }

        // 3. State Update
        if (hasFood)
        {
            state = AntState.RETURNING;
        }
        else
        {
            state = AntState.SEARCHING;
        }

        // 4. AI Execution based on state
        switch (state)
        {
            case SEARCHING:
                // If it doesn't smell food directly, follow food pheromones
                if (!smellFood(p, sim))
                {
                    state = followPheromones(p, sim, foodChannel);
                }
                break;

            case RETURNING:
                returnHome(p, sim);
                break;

            case WANDERING:
                phys.wander();
                break;
        }

        // 5. Physics Update
        phys.update();
        phys.checkEdges(p, sim.leftSidebarW, sim.rightSidebarW, sim.surfaceY);
        
        // 6. Interaction with the environment (leaving trail, picking up food)
        interact(p, sim);
    }

    /**
     * Logic to return home by following "Home" pheromones.
     */
    private void returnHome(PApplet p, AntColonySimulation sim)
    {
        // Obtain sensor positions
        PVector f = phys.getSensorPos(0);
        PVector l = phys.getSensorPos(-phys.sensorAngle);
        PVector r = phys.getSensorPos(phys.sensorAngle);

        // Read pheromone intensity at these points
        float vf = sim.pheromones.getWorld(f.x, f.y, homeChannel);
        float vl = sim.pheromones.getWorld(l.x, l.y, homeChannel);
        float vr = sim.pheromones.getWorld(r.x, r.y, homeChannel);

        // If no home trail is detected, use "GPS navigation" (cheat) to find the queen
        if (vf == 0 && vl == 0 && vr == 0)
        {
            PVector queen;
            if (colonyId == 0)
            {
                queen = sim.queenLocA;
            }
            else
            {
                queen = sim.queenLocB;
            }

            PVector homing = PVector.sub(queen, phys.pos);
            phys.applySteering(homing.normalize().mult(phys.maxSpeed));
            phys.wander(); // Adds noise to avoid looking robotic
            return;
        }

        // Steering logic based on highest intensity
        if (vf >= vl && vf >= vr)
        {
            phys.moveForward();
        }
        else if (vl > vr)
        {
            phys.turn(-phys.sensorAngle * 0.8f);
        }
        else
        {
            phys.turn(phys.sensorAngle * 0.8f);
        }
    }

    /**
     * Generic logic to follow a pheromone channel.
     * @param channelToFollow The channel to follow (Food or Home).
     * @return The suggested next state (SEARCHING or WANDERING if lost).
     */
    private AntState followPheromones(PApplet p, AntColonySimulation sim, int channelToFollow)
    {
        PVector f = phys.getSensorPos(0);
        PVector l = phys.getSensorPos(-phys.sensorAngle);
        PVector r = phys.getSensorPos(phys.sensorAngle);

        float vf = sim.pheromones.getWorld(f.x, f.y, channelToFollow);
        float vl = sim.pheromones.getWorld(l.x, l.y, channelToFollow);
        float vr = sim.pheromones.getWorld(r.x, r.y, channelToFollow);

        if (vf == 0 && vl == 0 && vr == 0)
        {
            phys.wander();
            return AntState.WANDERING;
        }
        
        if (vf > vl && vf > vr)
        {
            phys.moveForward();
            return AntState.SEARCHING;
        }
        else if (vl > vr)
        {
            phys.turn(-phys.sensorAngle * 0.5f);
            return AntState.SEARCHING;
        }
        else if (vr > vl)
        {
            phys.turn(phys.sensorAngle * 0.5f);
            return AntState.SEARCHING;
        }
        else
        {
            phys.wander();
            return AntState.WANDERING;
        }
    }

    /**
     * Checks if there is food (FallingLeaf) within the vision radius.
     * If so, moves towards the nearest one.
     * @return true if food was found and is being pursued.
     */
    private boolean smellFood(PApplet p, AntColonySimulation sim)
    {
        PVector closest = null;
        float recordDist = smellRadius;

        // Iterates over all leaves to find the closest one
        for (FallingLeaf leaf : sim.fallingLeaves)
        {
            // Ignores leaves still in the air (above the surface)
            if (leaf.phys.posPx.y < sim.surfaceY)
            {
                continue;
            }

            float d = PVector.dist(phys.pos, leaf.phys.posPx);
            if (d < recordDist)
            {
                recordDist = d;
                closest = leaf.phys.posPx;
            }
        }

        if (closest != null)
        {
            phys.seek(closest);
            return true;
        }
        
        return false;
    }

    /**
     * Manages physical interactions: picking up food, dropping food at the queen,
     * and depositing pheromones on the ground.
     */
    private void interact(PApplet p, AntColonySimulation sim)
    {
        PVector queen;
        if (colonyId == 0)
        {
            queen = sim.queenLocA;
        }
        else
        {
            queen = sim.queenLocB;
        }

        // 1. Deposit Pheromones (Only if on the ground)
        if (phys.pos.y > sim.surfaceY)
        {
            if (hasFood)
            {
                // If carrying food, leaves a "Food Found" trail
                sim.pheromones.addWorld(phys.pos.x, phys.pos.y, foodChannel, 0.5f);
            }
            else
            {
                // If not carrying food, leaves a "Path to Home" trail
                // Only updates if the ant's current pheromone strength is stronger than the floor's
                float cur = sim.pheromones.getWorld(phys.pos.x, phys.pos.y, homeChannel);
                if (pherStr > cur)
                {
                    sim.pheromones.setWorld(phys.pos.x, phys.pos.y, homeChannel, pherStr);
                }
            }
        }

        // 2. Object Interaction
        if (!hasFood)
        {
            // Try to pick up food
            for (FallingLeaf l : sim.fallingLeaves)
            {
                boolean onGround = l.phys.posPx.y >= sim.surfaceY;
                boolean closeEnough = PVector.dist(phys.pos, l.phys.posPx) < 15;
                
                if (onGround && closeEnough)
                {
                    hasFood = true;
                    l.amount -= 50; // Takes a piece of the leaf
                    nrg = maxNrg;   // Restores energy by eating a bit
                    phys.vel.rotate(PApplet.PI); // Turns 180 degrees to head back
                    pherStr = 1.0f; // Resets pheromone strength
                    
                    // Record statistics
                    if (colonyId == 0)
                    {
                        sim.statsA.registerFood();
                    }
                    else
                    {
                        sim.statsB.registerFood();
                    }
                    
                    break; // Picked up, exit loop
                }
            }
        }
        else
        {
            // If already has food, check if it reached the Queen/Base
            if (PVector.dist(phys.pos, queen) < 28)
            {
                hasFood = false;
                pherStr = 1.0f;
                phys.vel.rotate(PApplet.PI); // Turns 180 degrees to search again
                
                // Increase colony stock
                if (colonyId == 0)
                {
                    sim.foodStockA++;
                }
                else
                {
                    sim.foodStockB++;
                }
            }
        }
    }

    /**
     * Draws the ant on the screen.
     * @param p Reference to the PApplet.
     */
    public void display(PApplet p)
    {
        int colorNormal;
        int colorCarry;

        // Define colors based on the colony
        if (colonyId == 0)
        {
            colorNormal = p.color(30, 80, 200);   // Blue
            colorCarry = p.color(100, 200, 255);  // Light Blue
        }
        else
        {
            colorNormal = p.color(200, 30, 30);   // Red
            colorCarry = p.color(255, 150, 150);  // Light Red
        }

        // Configure fill based on state (carrying or empty)
        if (hasFood)
        {
            p.stroke(colorNormal);
            p.strokeWeight(1);
            p.fill(colorCarry);
        }
        else
        {
            p.stroke(colorNormal);
            p.strokeWeight(1);
            p.fill(colorNormal);
        }

        // Draw body
        p.circle(phys.pos.x, phys.pos.y, 5);
        p.noStroke();
        
        // Draw Debug Text (State) above the ant
        p.textAlign(PApplet.CENTER, PApplet.BOTTOM);
        p.textSize(12);
        
        String s;
        if (state == AntState.SEARCHING)
        {
            s = "S";
        }
        else if (state == AntState.RETURNING)
        {
            s = "R";
        }
        else
        {
            s = "W";
        }

        // Shadow effect on text for readability
        p.fill(0);
        p.text(s, phys.pos.x + 1, phys.pos.y - 7); 
        
        p.fill(255);
        p.text(s, phys.pos.x, phys.pos.y - 8);
        
        // Restore default alignment
        p.textAlign(PApplet.LEFT, PApplet.TOP);
        p.noStroke();
    }
}