package com.edgelinegames.cameraplugin;

import com.hypixel.hytale.protocol.ClientCameraView;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.ServerCameraSettings;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import java.util.logging.Level;
import javax.annotation.Nonnull;

/**
 * Main plugin class.
 * 
 * Made with template from:
 * https://github.com/realBritakee/hytale-template-plugin?tab=readme-ov-file
 * Run this to build mod: "./gradlew build"
 * Add this to the end when needed: " --refresh-dependencies"
 * 
 * @author JustinGent
 * @version 1.0.0
 */
public class CameraPlugin extends JavaPlugin {

    private static CameraPlugin instance;
    private float defaultCameraDistance = 15.0f; // Default zoom distance
    
    // Shoulder offset distance for third-person camera
    // This value can be adjusted to match the game's default camera shoulder offset
    // Common values: 0.3-0.5 blocks (positive = right, negative = left)
    private static final double SHOULDER_OFFSET_DISTANCE = 0.4;
    
    // Manager for prevent death item drop functionality
    private PreventDeathItemDropManager preventDeathItemDropManager;
    
    /**
     * Constructor - Called when plugin is loaded.
     */
    public CameraPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
        this.preventDeathItemDropManager = new PreventDeathItemDropManager(getLogger());
        getLogger().at(Level.INFO).log("CameraPlugin Plugin loaded!");
    }
    
    /**
     * Called when plugin is enabled - setup commands and event listeners.
     */
    @Override
    protected void setup() {
        getLogger().at(Level.INFO).log("CameraPlugin Plugin enabled!");
        
        // Register the camera zoom command
        try {
            getCommandRegistry().registerCommand((AbstractCommand) new CameraZoomCommand());
            getLogger().at(Level.INFO).log("CameraPlugin: Camera zoom command registered!");
        } catch (Exception e) {
            getLogger().at(Level.SEVERE).withCause(e).log("CameraPlugin: Could not register command");
        }
        
        // Setup prevent death item drop functionality
        preventDeathItemDropManager.setup(getCommandRegistry(), getEntityStoreRegistry());
        
        getLogger().at(Level.INFO).log("CameraPlugin: Camera zoom command registered! Players can use /camerazoom <distance>");
    }
    
    /**
     * Called when plugin is shutting down.
     */
    @Override
    protected void shutdown() {
        getLogger().at(Level.INFO).log("CameraPlugin Plugin disabled!");
        
        // Reset camera for all players
        if (Universe.get() != null) {
            Universe.get().getPlayers().forEach(this::resetCamera);
        }
    }
    
    /**
     * Apply camera zoom to a specific player using the default distance.
     */
    public void applyCameraDistance(@Nonnull PlayerRef playerRef) {
        setCameraDistance(playerRef, this.defaultCameraDistance);
    }
    
    /**
     * Set camera zoom (distance) for a specific player.
     * @param playerRef The player reference
     * @param distance The camera distance (zoom level). Lower = closer, Higher = farther
     */
    public void setCameraZoom(@Nonnull PlayerRef playerRef, float distance) {
        setCameraDistance(playerRef, distance);
    }
    
    /**
     * Set camera zoom (distance) for a specific player.
     * @param playerRef The player reference
     * @param distance The camera distance (zoom level). Lower = closer, Higher = farther
     */
    public void setCameraDistance(@Nonnull PlayerRef playerRef, float distance) {
        setCameraDistance(playerRef, distance, true); // Default to right shoulder
    }
    
    /**
     * Set camera zoom (distance) for a specific player with shoulder preference.
     * @param playerRef The player reference
     * @param distance The camera distance (zoom level). Lower = closer, Higher = farther
     * @param rightShoulder true for right shoulder, false for left shoulder
     */
    public void setCameraDistance(@Nonnull PlayerRef playerRef, float distance, boolean rightShoulder) {
        if (!playerRef.isValid()) {
            return;
        }
        
        ServerCameraSettings cameraSettings = new ServerCameraSettings();
        cameraSettings.distance = distance;
        cameraSettings.positionLerpSpeed = 1.0f; // Smooth transition speed
        cameraSettings.rotationLerpSpeed = 1.0f;
        cameraSettings.isFirstPerson = false; // Third-person view
        cameraSettings.displayCursor = true;
        cameraSettings.displayReticle = true;
        cameraSettings.allowPitchControls = true; // Allow mouse pitch/yaw control
        cameraSettings.eyeOffset = true; // Use eye offset for proper third-person positioning
        // Set shoulder offset for third-person camera
        // X offset: positive = right shoulder, negative = left shoulder
        // Adjust SHOULDER_OFFSET_DISTANCE constant to match game's default if needed
        double shoulderOffset = rightShoulder ? SHOULDER_OFFSET_DISTANCE : -SHOULDER_OFFSET_DISTANCE;
        cameraSettings.positionOffset = new Position(shoulderOffset, 0.0, 0.0);
        // Keep defaults for rotation and look that follow the player:
        // - rotationType = RotationType.AttachedToPlusOffset (default) - follows player rotation
        // - applyLookType = ApplyLookType.LocalPlayerLookOrientation (default) - applies player look
        // - mouseInputType = MouseInputType.LookAtTarget (default) - mouse controls look direction
        // - positionDistanceOffsetType = PositionDistanceOffsetType.DistanceOffset (default) - uses distance field
        // - attachedToType = AttachedToType.LocalPlayer (default) - attached to local player
        // - movementForceRotationType = MovementForceRotationType.AttachedToHead (default) - follows head rotation

        // int sa = ClientCameraView.VALUES.length;

        // Send the camera settings to the client
        // isLocked = false allows the player to control the camera with mouse
        playerRef.getPacketHandler().writeNoCache(
            (Packet) new SetServerCamera(ClientCameraView.Custom, false, cameraSettings)
        );
        
        getLogger().at(Level.INFO).log("CameraPlugin: Set camera zoom to " + distance + " for player");
    }
    
    /**
     * Reset camera to default for a player.
     */
    public void resetCamera(@Nonnull PlayerRef playerRef) {
        if (!playerRef.isValid()) {
            return;
        }
        
        playerRef.getPacketHandler().writeNoCache(
            (Packet) new SetServerCamera(ClientCameraView.Custom, false, null)
        );
        
        getLogger().at(Level.INFO).log("CameraPlugin: Reset camera for player");
    }
    
    /**
     * Set camera zoom for all online players.
     */
    public void setCameraZoomForAllPlayers(float distance) {
        Universe.get().getPlayers().forEach(playerRef -> {
            setCameraDistance(playerRef, distance);
        });
    }
    
    /**
     * Reset camera for all online players.
     */
    public void resetCameraForAllPlayers() {
        Universe.get().getPlayers().forEach(this::resetCamera);
    }
    
    /**
     * Set the default camera distance (used for new players).
     */
    public void setDefaultCameraDistance(float distance) {
        this.defaultCameraDistance = distance;
    }
    
    /**
     * Get the default camera distance.
     */
    public float getDefaultCameraDistance() {
        return this.defaultCameraDistance;
    }
    
    /**
     * Get plugin instance.
     */
    public static CameraPlugin getInstance() {
        return instance;
    }
}
