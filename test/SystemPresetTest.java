import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.Vector;
import java.util.Arrays;
import java.io.File;

import custom.Pack.ParticleSystem;
import custom.Pack.SystemPreset;
import custom.Pack.Emitter.Emitter;

public class SystemPresetTest {

    // JUnit 5 creates a temporary folder that deletes itself after test
    @TempDir
    Path tempDir;

    @Test
    public void testSaveAndLoadPreset() {
        // 1. Setup a system with 1 Emitter
        ParticleSystem originalSystem = new ParticleSystem();
        originalSystem.addEmitter(new Vector<>(Arrays.asList(50f, 50f)), 5f, 1f, 0f, 1f);
        originalSystem.setFriction(0.5);

        // 2. Save to temp file
        File file = tempDir.resolve("test_preset.txt").toFile();
        SystemPreset presetManager = new SystemPreset(originalSystem);
        presetManager.savePreset(file.getAbsolutePath());

        // 3. Create a NEW empty system and load the file
        ParticleSystem newSystem = new ParticleSystem();
        SystemPreset loader = new SystemPreset(newSystem);
        loader.loadPreset(file.getAbsolutePath());

        // 4. Verify Data Persisted
        assertThat(newSystem.getEmitters(), hasSize(1));
        
        Emitter loadedEmitter = newSystem.getEmitters().get(0);
        assertThat(loadedEmitter.getPosition().get(0), is(50f));
        
        // Verify Settings
        assertThat(newSystem.getFriction(), is(0.5));
    }
}