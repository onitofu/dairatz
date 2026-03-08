package ru.nyansus.mc.models;

import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.Identifier;
import ru.nyansus.mc.Dairatz;
import ru.nyansus.mc.animations.IdleAnimation;
import ru.nyansus.mc.renderer.DairatzRenderState;

public class DairatzModel extends EntityModel<DairatzRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(Dairatz.MOD_ID, "dairatz_entity"), "main"
    );

    private final ModelPart main;
    private final ModelPart body;
    private final ModelPart wings;
    private final ModelPart rightWing;
    private final ModelPart leftWing;
    private final ModelPart paws;
    private final KeyframeAnimation idleAnimation;

    public DairatzModel(ModelPart root) {
        super(root);
        this.main = root.getChild("main");
        this.body = this.main.getChild("body");
        this.wings = this.body.getChild("wings");
        this.rightWing = this.wings.getChild("rightWing");
        this.leftWing = this.wings.getChild("leftWing");
        this.paws = this.main.getChild("paws");
        this.idleAnimation = KeyframeAnimation.bake(root, IdleAnimation.IDLE);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition main = root.addOrReplaceChild("main",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body = main.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -3.0F, -2.0F, 4.0F, 4.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(2.0F, -3.425F, 0.0F, 0.0371F, 0.0F, 0.0F));

        body.addOrReplaceChild("nose_r1",
                CubeListBuilder.create()
                        .texOffs(12, 2).addBox(-1.0F, -2.0F, 1.0F, 2.0F, 2.0F, 0.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-1.0F, -1.0F, -2.5F, 0.0F, 1.5708F, -3.1416F));

        body.addOrReplaceChild("rightCilia_r1",
                CubeListBuilder.create()
                        .texOffs(12, 0).mirror().addBox(-2.5F, -2.0F, 1.5F, 2.0F, 2.0F, 0.0F, CubeDeformation.NONE).mirror(false),
                PartPose.offsetAndRotation(-1.25F, -1.0F, -2.3F, 0.0F, -0.7854F, 0.0F));

        body.addOrReplaceChild("leftCilia_r1",
                CubeListBuilder.create()
                        .texOffs(12, 0).addBox(0.5F, -2.0F, 1.5F, 2.0F, 2.0F, 0.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-2.75F, -1.0F, -2.3F, 0.0F, 0.7854F, 0.0F));

        PartDefinition wings = body.addOrReplaceChild("wings",
                CubeListBuilder.create(),
                PartPose.offset(-2.0F, 3.0F, 0.0F));

        PartDefinition rightWing = wings.addOrReplaceChild("rightWing",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(-0.425F, -4.0F, 2.0F, 0.0F, -0.1039F, 0.0F));

        rightWing.addOrReplaceChild("rightWing_r1",
                CubeListBuilder.create()
                        .texOffs(16, 0).addBox(-6.5F, -4.325F, 0.0F, 8.0F, 8.0F, 0.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-0.6357F, 0.0F, 0.8965F, 0.0F, 0.7854F, 0.0F));

        PartDefinition leftWing = wings.addOrReplaceChild("leftWing",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.425F, -4.0F, 2.0F, 0.0F, 0.1039F, 0.0F));

        leftWing.addOrReplaceChild("leftWing_r1",
                CubeListBuilder.create()
                        .texOffs(16, 0).mirror().addBox(-1.0F, -6.825F, -1.0F, 8.0F, 8.0F, 0.0F, CubeDeformation.NONE).mirror(false),
                PartPose.offsetAndRotation(-0.425F, 2.5F, 1.25F, 0.0F, -0.7854F, 0.0F));

        PartDefinition paws = main.addOrReplaceChild("paws",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, -0.4675F, 0.0F));

        paws.addOrReplaceChild("hindLeftPaw",
                CubeListBuilder.create()
                        .texOffs(12, 8).addBox(0.0F, -2.0F, 1.0F, 2.0F, 3.0F, 0.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        paws.addOrReplaceChild("hindRightPaw",
                CubeListBuilder.create()
                        .texOffs(8, 8).mirror().addBox(-2.0F, -2.0F, 1.0F, 2.0F, 3.0F, 0.0F, CubeDeformation.NONE).mirror(false),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        paws.addOrReplaceChild("frontLeftPaw",
                CubeListBuilder.create()
                        .texOffs(4, 8).mirror().addBox(0.0F, -2.0F, -1.0F, 2.0F, 3.0F, 0.0F, CubeDeformation.NONE).mirror(false),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        paws.addOrReplaceChild("frontRightPaw",
                CubeListBuilder.create()
                        .texOffs(0, 8).addBox(-2.0F, -2.0F, -1.0F, 2.0F, 3.0F, 0.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(DairatzRenderState state) {
        super.setupAnim(state);
        idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
    }
}
