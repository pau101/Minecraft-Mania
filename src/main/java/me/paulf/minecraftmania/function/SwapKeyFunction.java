package me.paulf.minecraftmania.function;

import me.paulf.minecraftmania.MinecraftMania;
import me.paulf.minecraftmania.RunningFunction;
import me.paulf.minecraftmania.ViewerCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.settings.KeyModifier;

import java.time.Duration;

public class SwapKeyFunction extends DurationFunction {
    private final KeyBinding first;

    private final KeyBinding second;

    public SwapKeyFunction(final KeyBinding first, final KeyBinding second, final Duration duration) {
        super(duration);
        this.first = first;
        this.second = second;
    }

    @Override
    public ITextComponent getMessage(final MinecraftMania.CommandContext context) {
        return new TranslationTextComponent("mania.key_swap", context.getViewerName(), this.getKeyName(this.first), this.getKeyName(this.second));
    }

    private ITextComponent getKeyName(final KeyBinding key) {
        return new TranslationTextComponent(key.getKeyDescription()).applyTextStyle(TextFormatting.YELLOW);
    }

    @Override
    protected RunningFunction createFunction() {
        final KeyBinding first = this.first;
        final KeyBinding second = this.second;
        final InputMappings.Input firstInput = first.getKey();
        final InputMappings.Input secondInput = second.getKey();
        final KeyModifier firstModifier = first.getKeyModifier();
        final KeyModifier secondModifier = second.getKeyModifier();
        return new RunningFunction() {
            @Override
            public ITextComponent getMessage(final ViewerCommand command, final int seconds) {
                return new TranslationTextComponent("mania.key_swap.running", SwapKeyFunction.this.getKeyName(first), SwapKeyFunction.this.getKeyName(second), seconds).applyTextStyle(TextFormatting.ITALIC);
            }

            @Override
            public void start() {
                this.set(first, KeyModifier.NONE, secondInput);
                this.set(second, KeyModifier.NONE, firstInput);
                this.refresh();
            }

            @Override
            public void stop() {
                this.set(first, firstModifier, firstInput);
                this.set(second, secondModifier, secondInput);
                this.refresh();
            }

            private void set(final KeyBinding key, final KeyModifier modifier, final InputMappings.Input input) {
                key.setKeyModifierAndCode(modifier, input);
                key.bind(input);
            }

            private void refresh() {
                Minecraft.getInstance().gameSettings.saveOptions();
                KeyBinding.resetKeyBindingArrayAndHash();
            }
        };
    }
}
