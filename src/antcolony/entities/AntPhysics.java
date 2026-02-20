package antcolony.entities;

import processing.core.PApplet;
import processing.core.PVector;

/**
 * Physics engine for the ants.
 * <p>
 * This class implements the Reynolds locomotion model (Steering Behaviors).
 * It manages position, velocity, and acceleration through Euler integration, 
 * allowing for organic and smooth movements instead of robotic linear motion.
 * </p>
 */
public class AntPhysics
{
    /**
     * Position Vector (X, Y) in the world.
     */
    public PVector pos;

    /**
     * Velocity Vector (direction and magnitude of movement).
     */
    public PVector vel;

    /**
     * Acceleration Vector (accumulates forces every frame).
     */
    public PVector acc;

    /**
     * Maximum allowed speed (limits the velocity vector).
     */
    public float maxSpeed = 2.0f;

    /**
     * Maximum steering force (limits the ability to change direction abruptly).
     */
    public float maxForce = 0.2f;

    /**
     * Distance at which the sensors (antennae) are from the ant's center.
     */
    public float sensorDist = 30.0f;

    /**
     * Aperture angle for the lateral sensors (in radians).
     * Set to PI/3 (60 degrees).
     */
    public float sensorAngle = PApplet.PI / 3;

    /**
     * Physics Constructor.
     * Initializes vectors and ensures the ant starts pointing upwards.
     * @param p Reference to PApplet.
     * @param x Initial X position.
     * @param y Initial Y position.
     */
    public AntPhysics(PApplet p, float x, float y)
    {
        this.pos = new PVector(x, y);
        this.vel = PVector.random2D();
        
        // Ensures initial velocity points upward (negative Y)
        // so they exit the nest towards the surface.
        if (this.vel.y > 0)
        {
            this.vel.y *= -1;
        }
        
        this.acc = new PVector(0, 0);
    }

    /**
     * Updates physics (Euler Integration).
     * 1. Adds Acceleration to Velocity.
     * 2. Limits Velocity.
     * 3. Adds Velocity to Position.
     * 4. Resets Acceleration.
     */
    public void update()
    {
        vel.add(acc);
        vel.limit(maxSpeed);
        pos.add(vel);
        acc.mult(0); // Clears acceleration for the next frame
    }

    /**
     * Applies a steering force.
     * Reynolds formula: Steering = Desired - Velocity.
     * @param desired The vector representing where the ant "wants" to go.
     */
    public void applySteering(PVector desired)
    {
        // Normalize and scale to maximum speed
        desired.normalize().mult(maxSpeed);
        
        // Calculate the force needed to correct the trajectory
        PVector steer = PVector.sub(desired, vel);
        steer.limit(maxForce); // Limits maneuverability
        
        acc.add(steer);
    }

    /**
     * Applies a force to continue moving in the current direction.
     */
    public void moveForward()
    {
        applySteering(vel.copy());
    }

    /**
     * Applies a force to turn in a specific direction relative to the current one.
     * @param angle The angle in radians to rotate.
     */
    public void turn(float angle)
    {
        applySteering(vel.copy().rotate(angle));
    }

    /**
     * Pursues a specific target.
     * @param target The position of the target (food, pheromone, etc.).
     */
    public void seek(PVector target)
    {
        applySteering(PVector.sub(target, pos));
    }

    /**
     * Adds random movement (noise) to simulate natural behavior.
     */
    public void wander()
    {
        acc.add(PVector.random2D().mult(0.2f));
    }

    /**
     * Calculates the absolute position of a sensor (antenna).
     * @param angle The sensor's angle relative to the ant's front (0 = front).
     * @return The (x,y) position where the sensor is "smelling".
     */
    public PVector getSensorPos(float angle)
    {
        // Takes the current direction, rotates by the sensor angle, 
        // extends by the sensor distance, and adds to the current position.
        return vel.copy().rotate(angle).normalize().mult(sensorDist).add(pos);
    }

    /**
     * Keeps the ant within simulation boundaries.
     * Makes the ant "bounce" (invert velocity) if it hits the walls.
     * @param p Reference to PApplet (for width/height).
     * @param leftW Left sidebar boundary.
     * @param rightW Right sidebar boundary.
     * @param surfaceY Top boundary (ground level).
     */
    public void checkEdges(PApplet p, float leftW, float rightW, float surfaceY)
    {
        // Collision with side walls (Left / Right)
        if (pos.x < leftW + 5 || pos.x > p.width - rightW - 5)
        {
            vel.x *= -1;
        }

        // Collision with screen bottom
        if (pos.y > p.height - 5)
        {
            vel.y *= -1;
        }

        // Collision with the surface (Top of the underground soil)
        if (pos.y < surfaceY)
        {
            pos.y = surfaceY;
            vel.y *= -1;
        }

        // Ensures the ant does not get stuck outside boundaries
        pos.x = PApplet.constrain(pos.x, leftW + 5, p.width - rightW - 5);
        pos.y = PApplet.constrain(pos.y, surfaceY, p.height - 5);
    }
}