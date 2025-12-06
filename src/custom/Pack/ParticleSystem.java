package custom.Pack;

import java.util.List;
import java.util.Vector;

import custom.Pack.Emitter.Emitter;
import custom.Pack.Emitter.OscillatingEmitter;
import custom.Pack.Emitter.PulseEmitter;
import custom.Pack.FieldPoint.FieldPoint;
import custom.Pack.Particle.Particle;
import javafx.scene.paint.Color;

public class ParticleSystem
{
    static {
        try {
            System.loadLibrary("ParticleSystem"); // Loads libParticleSystem.so on Unix or ParticleSystem.dll on Windows
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Native library failed to load: " + e);
            System.exit(1);
        }
    }

    private Vector<Particle> particles;
    private Vector<FieldPoint> fieldPoints;
    private Vector<Emitter> emitters;
    private int gravityEnabled;
    private double friction;
    private int maxParticles;

    public ParticleSystem()
    {
        particles = new Vector<>();
        fieldPoints = new Vector<>();
        emitters= new Vector<>();
        gravityEnabled = 0;
    }

    //Getters and Setters
    public Vector<Particle> getParticles()
    {
        return particles;
    }
    public Vector<FieldPoint>getFieldPoints()
    {
        return fieldPoints;
    }
    public Vector<Emitter> getEmitters()
    {
        return emitters;
    }
    public double getFriction()
    {
        return friction;
    }

    public int getMaxParticles()
    {
        return maxParticles;
    }

    public void setFriction(double friction)
    {
        this.friction = friction;
    }

    public void setMaxParticles(int maxParticles)
    {
        this.maxParticles = maxParticles;
    }

    //Adds a particle
    public void addParticle(float mass, float charge, Vector<Float> velocity, Vector<Float> position, Vector<Float> force, float size, int lifespan, String color, boolean hasTrai)
    {
        particles.add(new Particle(mass,charge,velocity,position,force,size,lifespan,color,hasTrai));
    }   
    public void addParticles(float particlesMass, Vector<Float> position, float[][] velocities)
    {
        for(float[] v : velocities)
        {
            particles.add(new Particle(particlesMass, 1.0f, new Vector<>(List.of(v[0], v[1])), position, new Vector<>(List.of(0.0f, 0.0f)),5.0f, 100, "red", true));
        }
    }
    
    //Adds a field point
    public void addFieldPoint(Vector<Float> position, float fieldStrength, String type)
    {
        fieldPoints.add(new FieldPoint(position, fieldStrength, type));
    }
    //Adds an emitter
    public void addEmitter(Vector<Float> position, float speed, float spread, float angle, float particlesMass)
    {
        emitters.add(new Emitter(position, speed, spread, angle, particlesMass, this));
    }

    //Adds an oscillating emitter
    public void addOscillatingEmitter(Vector<Float> position, float speed, float spread, float angle, float particlesMass, float amplitude, float frequency)
    {
        emitters.add(new OscillatingEmitter(position, speed, spread, angle, particlesMass, amplitude, frequency, this));
    }
    
    //Adds a pulse emitter
    public void addPulseEmitter(Vector<Float> position, float speed, float spread, float angle, float particlesMass, float frequency)
    {
        emitters.add(new PulseEmitter(position, speed, spread, angle, particlesMass, frequency, this));
    }
    
    //Removes all particles that are out of the screen
    public void removeParticlesOutOfScreen(int width, int height)
    {
        for(int i=0 ; i<particles.size() ; i++)
        {        
            if(particles.get(i).getPosition().get(0)<0 || particles.get(i).getPosition().get(0)>width || particles.get(i).getPosition().get(1)<0 || particles.get(i).getPosition().get(1)>height)
            {
                particles.remove(i);
            }
        }
    }
    
    //Displays all the particles in the system
    public void display()
    {
        for(int i=0;i<particles.size();i++)
        {
            System.out.println(particles.get(i).toString());
        }
    }

    //Checks if gravity is enabled in the system
    public int isGravityEnabled()
    {
        return gravityEnabled;
    }

    //Sets the gravity enabled flag in the system
    public void setGravityEnabled(int gravityEnabled)
    {
        this.gravityEnabled = gravityEnabled;
    }    

    public native void setForces(); //Native method to calculate forces efficiently 


    //Updates the particle positions and emitter positions
    public void updateAll()
    {
        for(int i=0; i<emitters.size(); i++)
        {
            emitters.get(i).emitParticles();
            //Updates the emitter position if it is an oscillating emitter
            if(emitters.get(i) instanceof OscillatingEmitter)
                emitters.get(i).updateEmitter();
        }        
        for(int i=0;i<particles.size();i++)
        {
            particles.get(i).update();
        }
    }
}