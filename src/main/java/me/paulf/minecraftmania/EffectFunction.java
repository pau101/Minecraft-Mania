package me.paulf.minecraftmania;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

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
    public void run(final ViewerCommand command, final CommandSender sender, final World world, final PlayerEntity player) {
        sender.effect(this.effect, this.duration, this.amplifier, false);
        sender.tellraw(
            "@s",
            new TranslationTextComponent("mania.effect",
                new StringTextComponent(command.getViewer()).applyTextStyle(TextFormatting.GOLD),
                this.effect.getDisplayName().applyTextStyle(this.effect.isBeneficial() ? TextFormatting.GREEN : TextFormatting.RED)
            )
        );
    }
}
