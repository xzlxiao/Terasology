/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.dag.nodes;

import org.terasology.engine.ComponentSystemManager;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.dag.WireframeCapableNode;
import org.terasology.rendering.dag.stateChanges.SetViewportToSizeOf;
import org.terasology.rendering.world.WorldRenderer;

import static org.terasology.rendering.opengl.DefaultDynamicFBOs.READ_ONLY_GBUFFER;

/**
 * This node renders the opaque (as opposed to semi-transparent)
 * objects present in the world. This node -does not- render the landscape.
 *
 * Objects to be rendered must be registered as implementing the interface RenderSystem and
 * take advantage of the RenderSystem.renderOpaque() method, which is called in process().
 */
public class OpaqueObjectsNode extends WireframeCapableNode {

    @In
    private ComponentSystemManager componentSystemManager;

    @In
    private WorldRenderer worldRenderer;

    private Camera playerCamera;

    /**
     * Initialises this node. -Must- be called once after instantiation.
     */
    @Override
    public void initialise() {
        super.initialise();
        playerCamera = worldRenderer.getActiveCamera();
        addDesiredStateChange(new SetViewportToSizeOf(READ_ONLY_GBUFFER));
    }

    /**
     * Iterates over any registered RenderSystem instance and calls its renderOpaque() method.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/opaqueObjects");

        READ_ONLY_GBUFFER.bind();

        playerCamera.lookThrough(); // TODO: remove. Placed here to make the dependency explicit.

        for (RenderSystem renderer : componentSystemManager.iterateRenderSubscribers()) {
            renderer.renderOpaque();
        }

        PerformanceMonitor.endActivity();
    }
}
