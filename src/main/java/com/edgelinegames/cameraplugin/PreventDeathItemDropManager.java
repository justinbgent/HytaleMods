package com.edgelinegames.cameraplugin;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandRegistry;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import javax.annotation.Nonnull;

/**
 * Manager class for handling prevent death item drop functionality.
 * Tracks which players should have item drops prevented on death.
 */
public class PreventDeathItemDropManager {
    
    // Set of player UUIDs that should have item drops prevented on death
    private final Set<UUID> preventDeathItemDropPlayers = ConcurrentHashMap.newKeySet();
    private final HytaleLogger logger;
    
    /**
     * Constructor.
     * @param logger The logger to use for logging messages
     */
    public PreventDeathItemDropManager(@Nonnull HytaleLogger logger) {
        this.logger = logger;
    }
    
    /**
     * Setup the prevent death item drop functionality by registering the command and system.
     * @param commandRegistry The command registry to register the command (from getCommandRegistry())
     * @param entityStoreRegistry The entity store registry to register the system (from getEntityStoreRegistry())
     */
    public void setup(@Nonnull CommandRegistry commandRegistry, @Nonnull ComponentRegistryProxy<EntityStore> entityStoreRegistry) {
        // Register command to prevent item drops on death for specific players
        try {
            commandRegistry.registerCommand((AbstractCommand) new PreventDeathItemDropCommand(this));
            logger.at(Level.INFO).log("PreventDeathItemDropManager: Prevent death item drop command registered!");
        } catch (Exception e) {
            logger.at(Level.SEVERE).withCause(e).log("PreventDeathItemDropManager: Could not register prevent death item drop command");
        }
        
        // Register system to prevent item drops on death
        try {
            entityStoreRegistry.registerSystem(new PreventDeathItemDropSystem(this));
            logger.at(Level.INFO).log("PreventDeathItemDropManager: Prevent death item drop system registered!");
        } catch (Exception e) {
            logger.at(Level.SEVERE).withCause(e).log("PreventDeathItemDropManager: Could not register prevent death item drop system");
        }
        
        logger.at(Level.INFO).log("PreventDeathItemDropManager: Use /preventdeathdrop [player] [enable/disable/status] to manage item drop prevention");
    }
    
    /**
     * Enable or disable item drop prevention for a specific player.
     * @param playerRef The player reference
     * @param enabled true to prevent item drops, false to allow them
     */
    public void setPreventDeathItemDrop(@Nonnull PlayerRef playerRef, boolean enabled) {
        if (!playerRef.isValid()) {
            return;
        }
        
        UUID playerUuid = playerRef.getUuid();
        if (enabled) {
            preventDeathItemDropPlayers.add(playerUuid);
            logger.at(Level.INFO).log("PreventDeathItemDropManager: Enabled item drop prevention for player: " + playerRef.getUsername());
        } else {
            preventDeathItemDropPlayers.remove(playerUuid);
            logger.at(Level.INFO).log("PreventDeathItemDropManager: Disabled item drop prevention for player: " + playerRef.getUsername());
        }
    }
    
    /**
     * Check if item drop prevention is enabled for a specific player.
     * @param playerRef The player reference
     * @return true if item drops are prevented, false otherwise
     */
    public boolean isPreventDeathItemDropEnabled(@Nonnull PlayerRef playerRef) {
        if (!playerRef.isValid()) {
            return false;
        }
        
        return preventDeathItemDropPlayers.contains(playerRef.getUuid());
    }
    
    /**
     * Remove a player from the prevention list (called when player disconnects).
     * @param playerRef The player reference
     */
    public void removePreventDeathItemDrop(@Nonnull PlayerRef playerRef) {
        if (!playerRef.isValid()) {
            return;
        }
        
        preventDeathItemDropPlayers.remove(playerRef.getUuid());
    }
}

