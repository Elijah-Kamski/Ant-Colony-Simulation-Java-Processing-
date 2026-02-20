package antcolony.entities;

import processing.core.PApplet;
import processing.core.PVector;

/**
 * Realistic physics engine for light objects (leaves).
 * <p>
 * Unlike the ant physics (which is based on behavior/steering),
 * this class uses classic Newtonian simulation:
 * Forces (Gravity + Drag + Wind) -> Acceleration -> Velocity -> Position.
 * </p>
 * <p>
 * The system converts between world units (Pixels) and the International System (Meters/kg)
 * to calculate air resistance correctly.
 * </p>
 */
public class LeafPhysics
{
    // --- State (in pixels and pixels/s) ---

    /**
     * Current position in pixels.
     */
    public PVector posPx;

    /**
     * Current velocity in pixels per second.
     */
    public PVector velPx;

    /**
     * Current acceleration in pixels per second squared.
     */
    public PVector accPx;

    // --- SI Constants (International System) ---

    /**
     * Acceleration of gravity (m/s^2).
     */
    private static final float G = 9.81f;

    /**
     * Air density at sea level (kg/m^3).
     */
    private static final float RHO_AIR = 1.29f;

    // --- Configuration ---

    /**
     * Scale: Pixels Per Meter (px/m).
     * Defines the relationship between screen size and simulated physical size.
     */
    private final float PPM;

    // --- Physical properties of the leaf (SI) ---

    /**
     * Leaf mass in kg.
     */
    private final float mass;

    /**
     * Drag Coefficient.
     * ~1.2 for irregular/flat objects.
     */
    private final float Cd;

    /**
     * Frontal area in contact with air in m^2.
     */
    private final float area;

    /**
     * Wind vector in m/s.
     */
    public PVector windMs;

    /**
     * Leaf Physics Constructor.
     * @param p Reference to PApplet.
     * @param startPosPx Initial position in pixels.
     * @param pixelsPerMeter Conversion scale (e.g., 100px = 1m).
     */
    public LeafPhysics(PApplet p, PVector startPosPx, float pixelsPerMeter)
    {
        this.PPM = pixelsPerMeter;

        this.posPx = startPosPx.copy();
        
        // Random initial velocity to provide variety to the fall
        this.velPx = new PVector(
            p.random(-15f, 15f), // px/s
            p.random(0f, 30f)    // px/s
        );
        
        this.accPx = new PVector(0, 0);

        // Configuration of plausible values for a dry leaf:
        this.mass = 0.002f;   // 2 grams = 0.002 kg
        this.Cd = 1.2f;       // Irregular shape
        this.area = 0.0025f;  // 25 cm^2 = 0.0025 m^2

        // Gentle initial wind to the right (0.6 m/s)
        this.windMs = new PVector(0.6f, 0.0f);
    }

    /**
     * Updates the physical simulation for a time step (dt).
     * @param p Reference to PApplet.
     * @param dt Delta time (time elapsed since the last frame in seconds).
     * @param groundYpx Y position of the ground in pixels.
     * @param minXpx Left boundary in pixels.
     * @param maxXpx Right boundary in pixels.
     */
    public void update(PApplet p, float dt, float groundYpx, float minXpx, float maxXpx)
    {
        // Safety against invalid delta times
        if (dt <= 0)
        {
            return;
        }

        // Optimization: If already on the ground, stop the physical simulation.
        if (posPx.y >= groundYpx)
        {
            posPx.y = groundYpx;
            velPx.set(0, 0);
            accPx.set(0, 0);
            return;
        }

        // (Optional) Wind variation for realism
        // windMs.x = 0.6f + 0.2f * PApplet.sin(p.frameCount * 0.02f);

        // Clear acceleration from the previous frame
        accPx.set(0, 0);

        // -------------------------
        // 1) Apply Gravity
        // -------------------------
        // In Processing +Y is downwards, so gravity is positive.
        // Acceleration = G * PPM (to convert m/s^2 to px/s^2)
        accPx.add(0, G * PPM);

        // -------------------------
        // 2) Apply Drag Force
        // -------------------------
        // Formula: Fd = 0.5 * rho * Cd * A * v^2
        
        // Convert current velocity from px/s to m/s
        PVector vMs = PVector.mult(velPx, 1.0f / PPM);

        // Calculate velocity relative to wind (v_rel = v_obj - v_wind)
        PVector vRel = PVector.sub(vMs, windMs);

        float speed = vRel.mag();
        
        // Apply only if there is significant movement to avoid precision errors
        if (speed > 1e-5f)
        {
            // Magnitude of the drag force (in Newtons)
            float FdMag = 0.5f * RHO_AIR * Cd * area * speed * speed;

            // Force direction is opposite to the relative velocity
            PVector Fd = vRel.copy().normalize().mult(-FdMag); // Force Vector (N)

            // Newton's Second Law: a = F / m
            // Convert resulting acceleration from m/s^2 to px/s^2
            PVector aDragPx = Fd.mult(1.0f / mass).mult(PPM);
            
            accPx.add(aDragPx);
        }

        // -------------------------
        // 3) Integration (Semi-Implicit Euler)
        // -------------------------
        velPx.add(PVector.mult(accPx, dt));
        posPx.add(PVector.mult(velPx, dt));

        // -------------------------
        // 4) Lateral Collisions (Invisible Wall)
        // -------------------------
        if (posPx.x < minXpx)
        {
            posPx.x = minXpx;
            velPx.x *= -0.2f; // Slight bounce (damping)
        }
        else if (posPx.x > maxXpx)
        {
            posPx.x = maxXpx;
            velPx.x *= -0.2f;
        }

        // -------------------------
        // 5) Ground Collision
        // -------------------------
        if (posPx.y >= groundYpx)
        {
            posPx.y = groundYpx;
            velPx.set(0, 0);
        }
    }
}