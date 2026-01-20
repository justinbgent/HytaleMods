package com.edgelinegames.cameraplugin;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Command to prevent a specific player from dropping items on death.
 * Usage: /preventdeathdrop [player]
 * If no player is specified, targets the command sender.
 */
public class PreventDeathItemDropCommand extends AbstractTargetPlayerCommand {
    
    private static final Message MESSAGE_ENABLED = Message.raw("Item drop prevention enabled for {player}");
    private static final Message MESSAGE_DISABLED = Message.raw("Item drop prevention disabled for {player}");
    private static final Message MESSAGE_ALREADY_ENABLED = Message.raw("Item drop prevention is already enabled for {player}");
    private static final Message MESSAGE_ALREADY_DISABLED = Message.raw("Item drop prevention is already disabled for {player}");
    private static final Message MESSAGE_STATUS_ENABLED = Message.raw("Item drop prevention is currently enabled for {player}");
    private static final Message MESSAGE_STATUS_DISABLED = Message.raw("Item drop prevention is currently disabled for {player}");
    
    private final OptionalArg<String> toggleArg;
    private final PreventDeathItemDropManager manager;
    
    public PreventDeathItemDropCommand(@Nonnull PreventDeathItemDropManager manager) {
        super("preventdeathdrop", "Prevent a player from dropping items on death");
        this.manager = manager;
        this.toggleArg = withOptionalArg("toggle", "enable/disable/status", ArgTypes.STRING);
    }
    
    @Override
    protected void execute(@Nonnull CommandContext context, @Nullable Ref<EntityStore> sourceRef, 
                          @Nonnull Ref<EntityStore> targetRef, @Nonnull PlayerRef targetPlayerRef, 
                          @Nonnull World world, @Nonnull Store<EntityStore> store) {
        String toggle = this.toggleArg.get(context);
        boolean isEnabled = manager.isPreventDeathItemDropEnabled(targetPlayerRef);
        
        // If toggle argument is provided, use it; otherwise toggle the current state
        if (toggle != null) {
            String lowerToggle = toggle.toLowerCase();
            if (lowerToggle.equals("enable") || lowerToggle.equals("on") || lowerToggle.equals("true")) {
                if (isEnabled) {
                    context.sendMessage(MESSAGE_ALREADY_ENABLED.param("player", targetPlayerRef.getUsername()));
                } else {
                    manager.setPreventDeathItemDrop(targetPlayerRef, true);
                    context.sendMessage(MESSAGE_ENABLED.param("player", targetPlayerRef.getUsername()));
                }
            } else if (lowerToggle.equals("disable") || lowerToggle.equals("off") || lowerToggle.equals("false")) {
                if (!isEnabled) {
                    context.sendMessage(MESSAGE_ALREADY_DISABLED.param("player", targetPlayerRef.getUsername()));
                } else {
                    manager.setPreventDeathItemDrop(targetPlayerRef, false);
                    context.sendMessage(MESSAGE_DISABLED.param("player", targetPlayerRef.getUsername()));
                }
            } else if (lowerToggle.equals("status") || lowerToggle.equals("check")) {
                if (isEnabled) {
                    context.sendMessage(MESSAGE_STATUS_ENABLED.param("player", targetPlayerRef.getUsername()));
                } else {
                    context.sendMessage(MESSAGE_STATUS_DISABLED.param("player", targetPlayerRef.getUsername()));
                }
            } else {
                // Invalid toggle value, just toggle the state
                manager.setPreventDeathItemDrop(targetPlayerRef, !isEnabled);
                if (!isEnabled) {
                    context.sendMessage(MESSAGE_ENABLED.param("player", targetPlayerRef.getUsername()));
                } else {
                    context.sendMessage(MESSAGE_DISABLED.param("player", targetPlayerRef.getUsername()));
                }
            }
        } else {
            // No toggle argument, just toggle the current state
            manager.setPreventDeathItemDrop(targetPlayerRef, !isEnabled);
            if (!isEnabled) {
                context.sendMessage(MESSAGE_ENABLED.param("player", targetPlayerRef.getUsername()));
            } else {
                context.sendMessage(MESSAGE_DISABLED.param("player", targetPlayerRef.getUsername()));
            }
        }
    }
}

