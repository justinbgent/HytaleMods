package com.edgelinegames.cameraplugin;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

/**
 * Command to allow players to change their camera zoom distance.
 * Usage: /camerazoom <distance>
 */
public class CameraZoomCommand extends AbstractPlayerCommand {
    
    private static final Message MESSAGE_CAMERA_ZOOM_SET = Message.raw("Camera zoom set to {distance} blocks");
    private static final Message MESSAGE_CAMERA_ZOOM_INVALID = Message.raw("Invalid distance! Please use a number between 1 and 100.");
    private static final Message MESSAGE_CAMERA_ZOOM_USAGE = Message.raw("Usage: /camerazoom <distance> (1-100)");
    
    private final RequiredArg<Float> distanceArg;
    
    public CameraZoomCommand() {
        super("camerazoom", "Change your camera zoom distance");
        this.distanceArg = withRequiredArg("distance", "Camera distance (1-100 blocks)", ArgTypes.FLOAT);
    }
    
    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        // Get the distance argument
        Float distance = this.distanceArg.get(context);
        if (distance == null) {
            context.sendMessage(MESSAGE_CAMERA_ZOOM_USAGE);
            return;
        }
        
        // Validate distance range (1-100 blocks)
        if (distance < 1.0f || distance > 100.0f) {
            context.sendMessage(MESSAGE_CAMERA_ZOOM_INVALID);
            return;
        }
        
        // Apply the camera zoom (this runs on the world thread)
        CameraPlugin.getInstance().setCameraDistance(playerRef, distance.floatValue());
        
        // Send confirmation message
        context.sendMessage(MESSAGE_CAMERA_ZOOM_SET.param("distance", String.format("%.1f", distance)));
    }
}

