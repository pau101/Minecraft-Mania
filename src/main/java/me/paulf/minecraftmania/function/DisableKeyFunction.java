package me.paulf.minecraftmania.function;

import me.paulf.minecraftmania.MinecraftMania;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.event.InputEvent;

public class DisableKeyFunction implements CommandFunction {
    private final KeyBinding key;
    private final int seconds;

    public DisableKeyFunction(final KeyBinding key, final int seconds) {
        this.key = key;
        this.seconds = seconds;
    }

    @Override
    public void run(final MinecraftMania.Context context) {
        context.<InputEvent.KeyInputEvent>addTimedEventListener(this.seconds, e -> {
            if (this.key.getKey() == InputMappings.getInputByCode(e.getKey(), e.getScanCode())) {
                this.key.setPressed(false);
                while (this.key.isPressed());
            }
        });
    }
}
