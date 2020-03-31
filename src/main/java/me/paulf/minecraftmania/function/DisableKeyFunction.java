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

public class DisableKeyFunction extends DurationFunction {
    private final KeyBinding key;

    public DisableKeyFunction(final KeyBinding key, final Duration duration) {
        super(duration);
        this.key = key;
    }

    @Override
    public ITextComponent getMessage(final MinecraftMania.CommandContext context) {
        return new TranslationTextComponent("mania.disable_key", context.getViewerName(), this.getKeyName());
    }

    private ITextComponent getKeyName() {
        return new TranslationTextComponent(this.key.getKeyDescription()).applyTextStyle(TextFormatting.YELLOW);
    }

    @Override
    protected RunningFunction createFunction() {
        final KeyBinding key = this.key;
        return new RunningFunction() {
            @Override
            public ITextComponent getMessage(final ViewerCommand command, final int seconds) {
                return new TranslationTextComponent("mania.disable_key.running", DisableKeyFunction.this.getKeyName(), seconds).applyTextStyle(TextFormatting.ITALIC);
            }

            @SubscribeEvent
            public void onInput(final InputEvent.KeyInputEvent e) {
                if (key.getKey() == InputMappings.getInputByCode(e.getKey(), e.getScanCode())) {
                    key.setPressed(false);
                    while (key.isPressed());
                }
            }
        };
    }
}
