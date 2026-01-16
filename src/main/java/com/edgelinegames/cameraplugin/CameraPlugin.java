package com.edgelinegames.cameraplugin;

/**
 * Main plugin class.
 * 
 * Made with template from:
 * https://github.com/realBritakee/hytale-template-plugin?tab=readme-ov-file
 * 
 * @author JustinGent
 * @version 1.0.0
 */
public class CameraPlugin {

    private static CameraPlugin instance;
    
    /**
     * Constructor - Called when plugin is loaded.
     */
    public CameraPlugin() {
        instance = this;
        System.out.println("CameraPlugin Plugin loaded!");
    }
    
    /**
     * Called when plugin is enabled.
     */
    public void onEnable() {
        System.out.println("CameraPlugin Plugin enabled!");
        
        // TODO: Initialize your plugin here
        // - Load configuration
        // - Register event listeners
        // - Register commands
        // - Start services
    }
    
    /**
     * Called when plugin is disabled.
     */
    public void onDisable() {
        System.out.println("CameraPlugin Plugin disabled!");
        
        // TODO: Cleanup your plugin here
        // - Save data
        // - Stop services
        // - Close connections
    }
    
    /**
     * Get plugin instance.
     */
    public static CameraPlugin getInstance() {
        return instance;
    }
}
