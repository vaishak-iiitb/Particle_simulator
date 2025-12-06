package custom.Pack.Particle;

import java.util.Arrays;
import java.util.Vector;

/**
 * Represents a Particle in the simulation with properties such as mass, charge, velocity, etc.
 * This class is part of the simulation package and is intended to interact with the C++ backend via JNI.
 */
public class Particle {
    // Attributes
    private float mass;
    private float charge;
    private Vector<Float> velocity; // Assuming 2D or 3D velocity vector
    private Vector<Float> position; // Position vector in space
    private Vector<Float> force;
    private float size;
    private int lifespan;
    private float lifetime;

    // JavaFX-specific attributes
    private String color; // Color is managed on the Java side
    private boolean hasTrail;

    // Static block to load the native library
    static {
        try {
            System.loadLibrary("ParticleSystem"); // Loads libParticleSystem.so on Unix or ParticleSystem.dll on Windows
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Native library failed to load: " + e);
            System.exit(1);
        }
    }

    // Constructor
    public Particle(float mass, float charge, Vector<Float> velocity, Vector<Float> position, Vector<Float> force, float size, int lifespan, String color, boolean hasTrail) {
        this.mass = mass;
        this.charge = charge;
        this.velocity = velocity;
        this.position = position;
        this.force = force;
        this.size = size;
        this.lifespan = lifespan;
        this.color = color;
        this.hasTrail = hasTrail;
    }

    // Getter and Setter methods
    public float getMass() {
        return mass;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }

    public float getCharge() {
        return charge;
    }

    public void setCharge(float charge) {
        this.charge = charge;
    }

    public Vector<Float> getVelocity() {
        return velocity;
    }

    public void setVelocity(float[] velocity) {
        this.velocity = new Vector<>(Arrays.asList(velocity[0], velocity[1]));
    }

    public Vector<Float> getPosition() {
        return position;
    }

    public void setPosition(float[] position) {
        this.position = new Vector<>(Arrays.asList(position[0], position[1]));
    }

    public Vector<Float> getForce() {
        return force;
    }

    public void setForce(float[] forceArray) {
        this.force = new Vector<>(Arrays.asList(forceArray[0], forceArray[1]));
    }


    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public int getLifespan() {
        return lifespan;
    }

    public void setLifespan(int lifespan) {
        this.lifespan = lifespan;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean hasTrail() {
        return hasTrail;
    }

    public void setTrail(boolean hasTrail) {
        this.hasTrail = hasTrail;
    }
    public float getLifetime() {
    return lifetime;
    }

    public void setLifetime(float lifetime) {
    this.lifetime = lifetime;
    }
    
    public native void update(); // Implemented in C++ using JNI


    @Override
    public String toString() {
        return String.format("%f,%f,%s,%s,%s,%f,%d,%s,%b",
            mass, charge, velocity, position, force, size, lifespan, color, hasTrail);
    }
    

    public static Particle parse(String line) {
        try {
            String[] parts = line.split(",");
            float mass = Float.parseFloat(parts[0]);
            float charge = Float.parseFloat(parts[1]);
            Vector<Float> velocity = parseVector(parts[2]);
            Vector<Float> position = parseVector(parts[3]);
            Vector<Float> force = parseVector(parts[4]);
            float size = Float.parseFloat(parts[5]);
            int lifespan = Integer.parseInt(parts[6]);
            String color = parts[7];
            boolean hasTrail = Boolean.parseBoolean(parts[8]);
            return new Particle(mass, charge, velocity, position, force, size, lifespan, color, hasTrail);
        } catch (Exception e) {
            System.err.println("Error parsing Particle: " + line);
            return null;
        }
    }
    

    private static Vector<Float> parseVector(String vectorString) {
        vectorString = vectorString.replaceAll("[\\[\\]]", ""); // Remove square brackets
        String[] values = vectorString.split(",\\s*");
        Vector<Float> vector = new Vector<>();
        for (String value : values) {
            vector.add(Float.parseFloat(value));
        }
        return vector;
    }
    


}