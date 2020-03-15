package me.paulf.minecraftmania.function;

import me.paulf.minecraftmania.MinecraftMania;
import net.minecraft.potion.Effect;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class EffectFunction implements CommandFunction {
    private final Effect effect;

    private final int duration;

    private final int amplifier;

    public EffectFunction(final Effect effect, final int duration, final int amplifier) {
        this.effect = effect;
        this.duration = duration;
        this.amplifier = amplifier;
    }

    @Override
    public ITextComponent getMessage(final MinecraftMania.Context context) {
        return new TranslationTextComponent("mania.effect", context.getViewerName(), this.effect.getDisplayName().applyTextStyle(this.effect.isBeneficial() ? TextFormatting.GREEN : TextFormatting.RED));
    }

    @Override
    public void run(final MinecraftMania.Context context) {
        context.commands().effect(this.effect, this.duration, this.amplifier, this.effect.isInstant());
    }
}
