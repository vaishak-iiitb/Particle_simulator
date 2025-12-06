import java.util.Arrays;
import java.util.Vector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import custom.Pack.Emitter.Emitter;
import custom.Pack.Particle.Particle;
import custom.Pack.ParticleSystem;

public class ParticleIntegrationTest {
    
    @BeforeAll
    public static void setupNativeLib() {
        System.loadLibrary("ParticleSystem"); 
    }

    @Test
    public void testNativePhysicsUpdate() {
        Vector<Float> pos = new Vector<>(Arrays.asList(0f, 0f));
        Vector<Float> vel = new Vector<>(Arrays.asList(10f, 0f));
        Vector<Float> force = new Vector<>(Arrays.asList(0f, 0f));
        
        Particle p = new Particle(1.0f, 1.0f, vel, pos, force, 5.0f, 100, "red", false);
        p.update(); 
        assertThat((double) p.getPosition().get(0), closeTo(10.0, 0.001)); 
    }

    @Test
    public void testGravityForce() {
        ParticleSystem system = new ParticleSystem();
        system.setGravityEnabled(1); // Enable Gravity

        // Add 1 particle
        Vector<Float> pos = new Vector<>(Arrays.asList(0f, 0f));
        system.addParticle(1.0f, 1.0f, new Vector<>(Arrays.asList(0f, 0f)), pos, new Vector<>(Arrays.asList(0f, 0f)), 5f, 100, "red", false);

        // Calculate Forces (C++)
        system.setForces();

        // Get the particle back
        Particle p = system.getParticles().get(0);
        
        // Gravity in your C++ is: mass * 0.1
        // Mass = 1.0, so Force Y should be 0.1
        // FIXED: Cast to (double)
        assertThat((double) p.getForce().get(1), closeTo(0.1, 0.0001));
    }

    @Test
    public void testAttractionForce() {
        ParticleSystem system = new ParticleSystem();
        system.setGravityEnabled(0);

        // Particle at (0,0)
        system.addParticle(1.0f, 1.0f, new Vector<>(Arrays.asList(0f, 0f)), new Vector<>(Arrays.asList(0f, 0f)), new Vector<>(Arrays.asList(0f, 0f)), 5f, 100, "red", false);

        // Attractor at (20,0) with Strength 10
        // We use 20.0 distance because your C++ code ignores forces if distance < 10.0
        system.addFieldPoint(new Vector<>(Arrays.asList(20f, 0f)), 10.0f, "A");

        system.setForces();

        Particle p = system.getParticles().get(0);

        // Expected Force Calculation:
        // F = (Charge * Strength * K) / Distance^2
        // Charge=1, Strength=10, K=200 (hardcoded in cpp), Dist=20
        // F = (1 * 10 * 200) / 400 = 2000 / 400 = 5.0
        
        // FIXED: Cast to (double)
        assertThat((double) p.getForce().get(0), closeTo(5.0, 0.1));
    }

    @Test
    public void testEmitterVelocityGeneration() {
        // Test if Emitter generates the correct batch of particles
        ParticleSystem system = new ParticleSystem();
        Vector<Float> pos = new Vector<>(Arrays.asList(0f, 0f));
        Emitter emitter = new Emitter(pos, 5.0f, 1.0f, 0.0f, 1.0f, system);

        // Call Native Method
        float[][] velocities = emitter.getVelocities();

        // Your C++ loop runs 10 times
        assertThat(velocities.length, is(10));
        
        // Check first particle's velocity magnitude roughly matches speed (5.0)
        float vx = velocities[0][0];
        float vy = velocities[0][1];
        double speed = Math.sqrt(vx*vx + vy*vy);
        
        // Speed is already double, no cast needed
        assertThat(speed, closeTo(5.0, 0.01));
    }
}