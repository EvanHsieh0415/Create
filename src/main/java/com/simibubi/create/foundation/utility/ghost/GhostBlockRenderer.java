package com.simibubi.create.foundation.utility.ghost;

import java.util.List;

import com.jozufozu.flywheel.core.model.ModelUtil;
import com.jozufozu.flywheel.core.virtual.VirtualEmptyBlockGetter;
import com.jozufozu.flywheel.fabric.model.DefaultLayerFilteringBakedModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.placement.PlacementHelpers;

import io.github.fabricators_of_create.porting_lib.render.virtual.FixedLightBakedModel;
import io.github.fabricators_of_create.porting_lib.render.virtual.TranslucentBakedModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public abstract class GhostBlockRenderer {

	private static final GhostBlockRenderer STANDARD = new DefaultGhostBlockRenderer();

	public static GhostBlockRenderer standard() {
		return STANDARD;
	}

	private static final GhostBlockRenderer TRANSPARENT = new TransparentGhostBlockRenderer();

	public static GhostBlockRenderer transparent() {
		return TRANSPARENT;
	}

	public abstract void render(PoseStack ms, SuperRenderTypeBuffer buffer, GhostBlockParams params);

	private static class DefaultGhostBlockRenderer extends GhostBlockRenderer {

		@Override
		public void render(PoseStack ms, SuperRenderTypeBuffer buffer, GhostBlockParams params) {
			ms.pushPose();

			BlockRenderDispatcher dispatcher = Minecraft.getInstance()
				.getBlockRenderer();

			BakedModel model = dispatcher.getBlockModel(params.state);

			RenderType layer = ItemBlockRenderTypes.getRenderType(params.state, false);
			VertexConsumer vb = buffer.getEarlyBuffer(layer);

			BlockPos pos = params.pos;
			ms.translate(pos.getX(), pos.getY(), pos.getZ());

			model = DefaultLayerFilteringBakedModel.wrap(model);
			dispatcher.getModelRenderer()
					.tesselateBlock(VirtualEmptyBlockGetter.FULL_BRIGHT, model, params.state, pos, ms, vb, false, RandomSource.create(), 42L, OverlayTexture.NO_OVERLAY);

			ms.popPose();
		}

	}

	private static class TransparentGhostBlockRenderer extends GhostBlockRenderer {

		@Override
		public void render(PoseStack ms, SuperRenderTypeBuffer buffer, GhostBlockParams params) {
			Minecraft mc = Minecraft.getInstance();
			BlockRenderDispatcher dispatcher = mc.getBlockRenderer();

			BlockState state = params.state;
			BlockPos pos = params.pos;
			float alpha = params.alphaSupplier.get() * .75f * PlacementHelpers.getCurrentAlpha();

			BakedModel model = dispatcher.getBlockModel(state);
			RenderType layer = RenderType.translucent();
			VertexConsumer vb = buffer.getEarlyBuffer(layer);

			ms.pushPose();
			ms.translate(pos.getX(), pos.getY(), pos.getZ());

			ms.translate(.5, .5, .5);
			ms.scale(.85f, .85f, .85f);
			ms.translate(-.5, -.5, -.5);

			model = DefaultLayerFilteringBakedModel.wrap(model);
			model = FixedLightBakedModel.wrap(model, LevelRenderer.getLightColor(mc.level, pos));
			model = TranslucentBakedModel.wrap(model, () -> params.alphaSupplier.get() * .75f * PlacementHelpers.getCurrentAlpha());
			dispatcher.getModelRenderer()
					.tesselateBlock(VirtualEmptyBlockGetter.INSTANCE, model, params.state, pos, ms, vb, false, RandomSource.create(), 42L, OverlayTexture.NO_OVERLAY);

			ms.popPose();
		}

	}

}
