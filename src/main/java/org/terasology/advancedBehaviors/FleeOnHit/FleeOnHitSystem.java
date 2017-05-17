/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.advancedBehaviors.FleeOnHit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.pathfinding.components.FleeComponent;
import org.terasology.advancedBehaviors.UpdateBehaviorEvent;
import org.terasology.assets.management.AssetManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.behavior.BehaviorComponent;
import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.health.OnDamagedEvent;
import org.terasology.registry.In;

@RegisterSystem(RegisterMode.AUTHORITY)
public class FleeOnHitSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(FleeOnHitSystem.class);

    @In
    private AssetManager assetManager;

    /**
     * Updates the FleeOnHitComponent with information about the hit
     */
    @ReceiveEvent(components = FleeOnHitComponent.class)
    public void onDamage(OnDamagedEvent event, EntityRef entity, FleeOnHitComponent fleeOnHitComponent) {
        FleeComponent fleeComponent = new FleeComponent();
        fleeComponent.minDistance = fleeOnHitComponent.minDistance;
        fleeComponent.instigator = event.getInstigator();
        entity.saveComponent(fleeComponent);
        entity.send(new UpdateBehaviorEvent());
    }


    @ReceiveEvent(priority = EventPriority.PRIORITY_HIGH, components = FleeOnHitComponent.class)
    public void onUpdateBehaviorFlee(UpdateBehaviorEvent event, EntityRef entity, FleeOnHitComponent fleeOnHitComponent, FleeComponent fleeComponent) {
        if (fleeComponent.instigator != null) {
            event.consume();

            // Start fleeing behavior, when a hit that is recorded is recent
            BehaviorComponent behaviorComponent = entity.getComponent(BehaviorComponent.class);
            behaviorComponent.tree = assetManager.getAsset("AdvancedBehaviors:flee", BehaviorTree.class).get();
            logger.info("Changed behavior to Flee");
            // Increase speed by multiplier factor
            CharacterMovementComponent characterMovementComponent = entity.getComponent(CharacterMovementComponent.class);
            characterMovementComponent.speedMultiplier = fleeOnHitComponent.speedMultiplier;
            entity.saveComponent(characterMovementComponent);
            entity.saveComponent(behaviorComponent);
        }
    }
}
