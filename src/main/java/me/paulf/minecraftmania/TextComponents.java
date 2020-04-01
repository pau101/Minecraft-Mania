package me.paulf.minecraftmania;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.function.Predicate;

public final class TextComponents {
    public static Predicate<ITextComponent> translation(final String key) {
        return component -> component instanceof TranslationTextComponent && key.equals(((TranslationTextComponent) component).getKey());
    }

    public static Predicate<ITextComponent> string(final String text) {
        return component -> component instanceof StringTextComponent && text.equals(((StringTextComponent) component).getText());
    }

    public static boolean matches(final ITextComponent message, final Predicate<ITextComponent> predicate) {
        if (predicate.test(message)) {
            return true;
        }
        if (!message.getUnformattedComponentText().isEmpty()) {
            return false;
        }
        boolean matched = false;
        for (final ITextComponent sibling : message.getSiblings()) {
            if (!matched && predicate.test(sibling)) {
                matched = true;
            } else if (!sibling.getString().isEmpty()) {
                return false;
            }
        }
        return matched;
    }
}
