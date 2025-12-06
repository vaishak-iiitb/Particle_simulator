package custom.Pack;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import custom.Pack.FieldPoint.FieldPoint;
import custom.Pack.Particle.Particle;
import custom.Pack.Emitter.Emitter;
import custom.Pack.Emitter.OscillatingEmitter;

public class SystemPreset {

    private ParticleSystem particleSystem;

    public SystemPreset(ParticleSystem particleSystem) {
        this.particleSystem = particleSystem;
    }

    public void savePreset(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            // Save field points
            writer.write("FieldPoints\n");
            for (FieldPoint fieldPoint : particleSystem.getFieldPoints()) {
                writer.write(fieldPoint.toString() + "\n");
            }

            // Save emitters
            writer.write("Emitters\n");
            for (Emitter emitter : particleSystem.getEmitters()) {
                writer.write(emitter.toString() + "\n");
            }

            // Save system settings
            writer.write("Settings\n");
            writer.write("GravityEnabled=" + particleSystem.isGravityEnabled() + "\n");
            writer.write("Friction=" + particleSystem.getFriction() + "\n");
            writer.write("MaxParticles=" + particleSystem.getMaxParticles() + "\n");

            System.out.println("Preset saved to " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadPreset(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
    String line;
    String section = null; // Current section being parsed (Particles, FieldPoints, Emitters, Settings)

    while ((line = reader.readLine()) != null) {
            // Check if the line indicates a new section
            if (line.equals("Particles")) {
                section = "Particles";
            } else if (line.equals("FieldPoints")) {
                section = "FieldPoints";
            } else if (line.equals("Emitters")) {
                section = "Emitters";
            } else if (line.equals("Settings")) {
                section = "Settings";
            } else {
                // Parse the line based on the current section
                switch (section) {
                    case "Particles":
                        Particle particle = Particle.parse(line);
                        if (particle != null) {
                            particleSystem.getParticles().add(particle); // Add the parsed particle to the system
                        }
                        break;
                    case "FieldPoints":
                        FieldPoint fieldPoint = FieldPoint.parse(line);
                        if (fieldPoint != null) {
                            particleSystem.getFieldPoints().add(fieldPoint); // Add the parsed field point to the system
                        }
                        break;
                    case "Emitters":
                        Emitter emitter = OscillatingEmitter.parse(line, particleSystem); // Adjust for other emitter types if needed
                        if (emitter != null) {
                            particleSystem.getEmitters().add(emitter); // Add the parsed emitter to the system
                        }
                        break;
                    case "Settings":
                        if (line.startsWith("GravityEnabled=")) {
                            particleSystem.setGravityEnabled(Integer.parseInt(line.split("=")[1])); // Set gravity enabled/disabled
                        } else if (line.startsWith("Friction=")) {
                            particleSystem.setFriction(Double.parseDouble(line.split("=")[1])); // Set friction coefficient
                        } else if (line.startsWith("MaxParticles=")) {
                            particleSystem.setMaxParticles(Integer.parseInt(line.split("=")[1])); // Set maximum number of particles
                        }
                        break;
                }
            }
        }
        System.out.println("Preset loaded from " + filename);
    } catch (IOException e) {
        e.printStackTrace();
    }
    }
}