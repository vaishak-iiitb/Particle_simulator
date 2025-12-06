package custom.Pack.Emitter;

import java.util.*;
import custom.Pack.Emitter.Emitter;
import custom.Pack.ParticleSystem;

public class PulseEmitter extends Emitter {

    static {
        System.loadLibrary("ParticleSystem"); // Load native library
    }

    private long lastUpdateTime = 0; // Instance-specific timer for toggling emission
    private boolean shouldEmit = true; // Tracks whether this instance should emit particles
    private float frequency;

    public PulseEmitter(Vector<Float> position, float speed, float spread, float angle, float particlesMass, float frequency, ParticleSystem ps) {
        super(position, speed, spread, angle, particlesMass, ps);
        this.frequency = frequency;
    }

    public float getPulseFrequency()
    {
        return frequency;
    }

    public void setPulseFrequency(float frequency)
    {
        this.frequency = frequency;
    }

    @Override
    public void emitParticles() {
        long currentTime = System.currentTimeMillis();

        // Toggle emitting state every 0.5 seconds (500 milliseconds)
        if (currentTime - lastUpdateTime >= 1/frequency) {
            shouldEmit = !shouldEmit; // Switch between emitting and not emitting
            lastUpdateTime = currentTime;
        }

        if (shouldEmit) {
            float[][] velocities = getVelocities(); // Fetch new velocities
            system.addParticles(particlesMass, position, velocities); // Add the particles to the system
        }
    }
}