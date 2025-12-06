package custom.Pack.Emitter;
import java.util.*;
import custom.Pack.Emitter.Emitter;
import custom.Pack.ParticleSystem;
public class OscillatingEmitter extends Emitter
{
    private float amplitude;
    private float frequency;
    private Vector<Float> position;
    private Vector<Float> meanPosition;
    private float theta;

    public OscillatingEmitter(Vector<Float> position, float speed, float spread, float angle, float particlesMass, float amplitude, float frequency, ParticleSystem ps)
    {
        super(position, speed, spread, angle, particlesMass, ps);
        this.amplitude = amplitude;
        this.frequency = frequency;
        this.theta = 0;
        this.meanPosition = position;
    }

    public float getAmplitude()
    {
        return amplitude;
    }

    public void setAmplitude(float amplitude)
    {
        this.amplitude = amplitude;
    }

    public float getFrequency()
    {
        return frequency;
    }

    public void setFrequency(float frequency)
    {
        this.frequency = frequency;
    }


    public float getTheta()
    {
        return this.theta;
    }

    public void setTheta(float theta)
    {
        this.theta = theta;
    }

    public Vector<Float> getMeanPosition()
    {
        return meanPosition;
    }
    public void setMeanPosition(Vector<Float> meanPosition)
    {
        this.meanPosition = meanPosition;
    }
    
    // Native method to update the emitter properties for each frame for oscillation (implemented in C++)
    public native void updateEmitter();
}