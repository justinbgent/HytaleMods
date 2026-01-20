package com.edgelinegames.cameraplugin;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.asset.type.gameplay.DeathConfig;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * System that prevents specific players from dropping items on death.
 * This system runs before DeathSystems.DropPlayerDeathItems to set the itemsLossMode to NONE
 * for players that have been marked to prevent item drops.
 */
public class PreventDeathItemDropSystem extends EntityTickingSystem<EntityStore> {
    
    private static final Query<EntityStore> QUERY = Query.and(
        DeathComponent.getComponentType(),
        EntityModule.get().getPlayerComponentType()
    );
    
    private static final Set<Dependency<EntityStore>> DEPENDENCIES = Set.<Dependency<EntityStore>>of(
        new SystemDependency<EntityStore, DeathSystems.DropPlayerDeathItems>(Order.BEFORE, DeathSystems.DropPlayerDeathItems.class)
    );
    
    private final PreventDeathItemDropManager manager;
    
    public PreventDeathItemDropSystem(@Nonnull PreventDeathItemDropManager manager) {
        this.manager = manager;
    }
    
    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return QUERY;
    }
    
    @Nonnull
    @Override
    public Set<Dependency<EntityStore>> getDependencies() {
        return DEPENDENCIES;
    }
    
    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, 
                     @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        DeathComponent deathComponent = archetypeChunk.getComponent(index, DeathComponent.getComponentType());
        if (deathComponent == null) {
            return;
        }
        
        // Get the Ref for this entity
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        if (ref == null || !ref.isValid()) {
            return;
        }
        
        // Get the PlayerRef from the store
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) {
            return;
        }
        
        // Check if this player should have item drops prevented
        if (manager.isPreventDeathItemDropEnabled(playerRef)) {
            // Set itemsLossMode to NONE to prevent item drops
            deathComponent.setItemsLossMode(DeathConfig.ItemsLossMode.NONE);
        }
    }
}

