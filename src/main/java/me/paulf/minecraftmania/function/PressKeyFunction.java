package me.paulf.minecraftmania.function;

import me.paulf.minecraftmania.MinecraftMania;
import me.paulf.minecraftmania.RunningFunction;
import me.paulf.minecraftmania.ViewerCommand;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.time.Duration;

public class PressKeyFunction implements CommandFunction {
    private final KeyBinding key;

    private final Duration duration;

    public PressKeyFunction(final KeyBinding key, final Duration duration) {
        this.key = key;
        this.duration = duration;
    }

    @Override
    public ITextComponent getMessage(final MinecraftMania.Context context) {
        return new TranslationTextComponent("mania.press_key", context.getViewerName(), this.getKeyName());
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
                return new TranslationTextComponent("mania.press_key.running", PressKeyFunction.this.getKeyName(), seconds).applyTextStyle(TextFormatting.ITALIC);
            }

            @SubscribeEvent
            public void onTick(final TickEvent.ClientTickEvent e) {
                if (e.phase == TickEvent.Phase.START) {
                    key.setPressed(true);
                }
            }
        });
    }
}
