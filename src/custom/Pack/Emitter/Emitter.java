package custom.Pack.Emitter;
import java.util.Vector;
import custom.Pack.ParticleSystem;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

// Represents a particle emitter in the system
public class Emitter
{
    protected Vector<Float> position; 
    private float spread;             
    private float angle;              
    protected float speed;            
    protected float particlesMass;    
    protected boolean isEmitting;     
    protected ParticleSystem system;  
    private Circle visualRepresentation; 

    // Constructor to initialize the emitter
    public Emitter(Vector<Float> position, float speed, float spread, float angle, float particlesMass, ParticleSystem ps)
    {
        this.position = position;
        this.speed = speed;
        this.spread = spread;
        this.angle = angle;
        this.isEmitting = false;
        this.particlesMass = particlesMass;
        this.system = ps;
        this.visualRepresentation = new Circle(position.get(0), position.get(1), 10, Color.RED); // Red circle
    }

    // Get the visual representation (for JavaFX display)
    public Circle getVisualRepresentation() {
        return visualRepresentation;
    }
    
    public Vector<Float> getPosition() {
        return position;
    }

    public float getParticlesMass() {
        return particlesMass;
    }
    
    // Update position and visual representation
    public void setPosition(Vector<Float> position) {
        this.position = position;
        visualRepresentation.setCenterX(position.get(0));
        visualRepresentation.setCenterY(position.get(1));
    }
    
    public float getSpread() {
        return spread;
    }
    
    public void setSpread(float spread) {
        this.spread = spread;
    }
    
    public float getAngle() {
        return angle;
    }
    
    public void setAngle(float angle) {
        this.angle = angle;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public boolean isEmitting() {
        return isEmitting;
    }
    
    public void setIsEmitting(boolean isEmitting) {
        this.isEmitting = isEmitting;
    }

    public void setParticlesMass(float particlesMass) {
        this.particlesMass = particlesMass;
    }

    // Native method to calculate particle velocities (implemented in C++)
    public native float[][] getVelocities();

    // Starts emitting particles
    public void emitParticles() 
    {
        this.isEmitting = true;
        float[][] velocities = getVelocities(); // Calculate velocities
        system.addParticles(particlesMass, position, velocities); // Add particles to the system
    }
    
    // Provides a string representation of the emitter
    @Override
    public String toString() {
        return String.format("Emitter{position=%s/ spread=%f/ angle=%f/ speed=%f/ particlesMass=%f/ isEmitting=%b}",
            position.toString(), spread, angle, speed, particlesMass, isEmitting);
    }

    // Parses a string to create an Emitter object
    public static Emitter parse(String line, ParticleSystem ps) {
        try {
            line = line.replace("Emitter{", "").replace("}", ""); // Remove enclosing braces
            String[] parts = line.split("/\\s*");
            
            Vector<Float> position = parseVector(parts[0].split("=")[1]);
            float spread = Float.parseFloat(parts[1].split("=")[1]);
            float angle = Float.parseFloat(parts[2].split("=")[1]);
            float speed = Float.parseFloat(parts[3].split("=")[1]);
            float particlesMass = Float.parseFloat(parts[4].split("=")[1]);
            boolean isEmitting = Boolean.parseBoolean(parts[5].split("=")[1]);

            Emitter emitter = new Emitter(position, speed, spread, angle, particlesMass, ps);
            emitter.setIsEmitting(isEmitting);
            return emitter;
        } catch (Exception e) {
            System.err.println("Error parsing Emitter: " + line);
            e.printStackTrace();
            return null;
        }
    }

    // Parses a vector from a string representation
    private static Vector<Float> parseVector(String vectorString) {
        vectorString = vectorString.replaceAll("[\\[\\]]", ""); // Remove square brackets
        String[] values = vectorString.split(",\\s*");
        Vector<Float> vector = new Vector<>();
        for (String value : values) {
            vector.add(Float.parseFloat(value));
        }
        return vector;
    }

    // Native method to update the emitter properties for oscillating emitter (implemented in C++)
    public native void updateEmitter();
}