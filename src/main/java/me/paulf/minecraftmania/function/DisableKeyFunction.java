package me.paulf.minecraftmania.function;

import me.paulf.minecraftmania.MinecraftMania;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.event.InputEvent;

import java.time.Duration;

public class DisableKeyFunction implements CommandFunction {
    private final KeyBinding key;

    private final Duration duration;

    public DisableKeyFunction(final KeyBinding key, final Duration duration) {
        this.key = key;
        this.duration = duration;
    }

    @Override
    public void run(final MinecraftMania.Context context) {
        context.<InputEvent.KeyInputEvent>addRunningEventListener(this.duration, e -> {
            if (this.key.getKey() == InputMappings.getInputByCode(e.getKey(), e.getScanCode())) {
                this.key.setPressed(false);
                while (this.key.isPressed());
            }
        });
    }
}
