package me.paulf.minecraftmania;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.gui.NewChatGui;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class StickyMessageHelper {
    private static final Method ADD_MESSAGE = ObfuscationReflectionHelper.findMethod(NewChatGui.class, "func_146237_a", ITextComponent.class, int.class, int.class, boolean.class);

    private final Int2ObjectMap<ITextComponent> messages = new Int2ObjectLinkedOpenHashMap<>();

    public void add(final int id, final ITextComponent message) {
        this.messages.put(id, message);
    }

    public void remove(final int id) {
        if (this.messages.remove(id) != null) {
            Minecraft.getInstance().ingameGUI.getChatGUI().deleteChatLine(id);
        }
    }

    public void register(final IEventBus bus) {
        bus.addListener(this::onRenderChat);
    }

    private void onRenderChat(final RenderGameOverlayEvent.Chat event) {
        final IngameGui gui = Minecraft.getInstance().ingameGUI;
        final NewChatGui chat = gui.getChatGUI();
        for (final Int2ObjectMap.Entry<ITextComponent> entry : this.messages.int2ObjectEntrySet()) {
            try {
                ADD_MESSAGE.invoke(chat, entry.getValue(), entry.getIntKey(), gui.getTicks(), false);
            } catch (final IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
