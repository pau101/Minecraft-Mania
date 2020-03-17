package me.paulf.minecraftmania;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.eventbus.api.Event;

public class StickyChatEvent extends Event {
    private final Int2ObjectMap<ITextComponent> message;

    public StickyChatEvent(final Int2ObjectMap<ITextComponent> message) {
        this.message = message;
    }

    public void addMessage(final int id, final ITextComponent message) {
        this.message.put(id, message);
    }
}
