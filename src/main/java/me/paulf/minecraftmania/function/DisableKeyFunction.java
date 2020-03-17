package me.paulf.minecraftmania.function;

import me.paulf.minecraftmania.MinecraftMania;
import me.paulf.minecraftmania.RunningFunction;
import me.paulf.minecraftmania.ViewerCommand;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.time.Duration;

public class DisableKeyFunction implements CommandFunction {
    private final KeyBinding key;

    private final Duration duration;

    public DisableKeyFunction(final KeyBinding key, final Duration duration) {
        this.key = key;
        this.duration = duration;
    }

    @Override
    public ITextComponent getMessage(final MinecraftMania.Context context) {
        return new TranslationTextComponent("mania.key", context.getViewerName(), this.getKeyName());
    }

    private ITextComponent getKeyName() {
        return new TranslationTextComponent(this.key.getKeyDescription()).applyTextStyle(TextFormatting.YELLOW);
    }

    @Override
    public void run(final MinecraftMania.Context context) {
        final KeyBinding key = this.key;
        context.addRunningFunction(this.duration, new RunningFunction() {
            @Override
            public ITextComponent getMessage(final ViewerCommand command, final int seconds) {
                return new TranslationTextComponent("mania.key.running", DisableKeyFunction.this.getKeyName(), seconds).applyTextStyle(TextFormatting.ITALIC);
            }

            @SubscribeEvent
            public void onInput(final InputEvent.KeyInputEvent e) {
                if (key.getKey() == InputMappings.getInputByCode(e.getKey(), e.getScanCode())) {
                    key.setPressed(false);
                    while (key.isPressed());
                }
            }
        });
    }
}
