package dev.itsmeow.imdlib.client.render;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class ImplRenderer<T extends MobEntity, A extends EntityModel<T>> extends BaseRenderer<T, A> {

    private final TextureContainer<T, A> textureContainer;
    private final ModelContainer<T, A> modelContainer;
    private final PreRenderCallback<T> preRenderCallback;
    private final HandleRotation<T> handleRotation;
    private final ApplyRotations<T> applyRotations;
    private final SuperCallApplyRotations applyRotationsSuper;

    public ImplRenderer(EntityRendererManager mgr, float shadow, @Nonnull TextureContainer<T, A> textureContainer, @Nonnull ModelContainer<T, A> modelContainer, @Nullable PreRenderCallback<T> preRenderCallback, @Nullable HandleRotation<T> handleRotation, @Nullable ApplyRotations<T> applyRotations, SuperCallApplyRotations applyRotationsSuper) {
        super(mgr, modelContainer.getBaseModel(), shadow);
        this.textureContainer = textureContainer;
        this.modelContainer = modelContainer;
        this.preRenderCallback = preRenderCallback;
        this.handleRotation = handleRotation;
        this.applyRotations = applyRotations;
        this.applyRotationsSuper = applyRotationsSuper;
    }

    @Override
    protected void applyRotations(T e, MatrixStack s, float a, float y, float p) {
        if(applyRotations == null) {
            super.applyRotations(e, s, a, y, p);
        } else {
            if(applyRotationsSuper == SuperCallApplyRotations.PRE) {
                super.applyRotations(e, s, a, y, p);
            }
            applyRotations.applyRotations(e, s, a, y, p);
            if(applyRotationsSuper == SuperCallApplyRotations.POST) {
                super.applyRotations(e, s, a, y, p);
            }
        }
    }

    @Override
    protected void preRenderCallback(T e, MatrixStack s, float p) {
        if(preRenderCallback != null) {
            preRenderCallback.preRenderCallback(e, s, p);
        }
    }

    @Override
    protected float handleRotationFloat(T e, float p) {
        return handleRotation == null ? super.handleRotationFloat(e, p) : handleRotation.handleRotation(e, p);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void render(T e, float p_225623_2_, float p_225623_3_, MatrixStack p_225623_4_, IRenderTypeBuffer p_225623_5_, int p_225623_6_) {
        this.entityModel = (A) modelContainer.getModel(e);
        super.render(e, p_225623_2_, p_225623_3_, p_225623_4_, p_225623_5_, p_225623_6_);
    }

    @Override
    public ResourceLocation getEntityTexture(T entity) {
        return textureContainer.getTexture(entity);
    }

    public static class TextureContainer<T extends MobEntity, A extends EntityModel<T>> {

        private Strategy strategy;
        private ResourceLocation singleTexture;
        private Function<T, ResourceLocation> texMapper;
        private ResourceLocation trueTex;
        private ResourceLocation falseTex;
        private Predicate<T> condition;

        public TextureContainer(ResourceLocation singleTexture) {
            this.strategy = Strategy.SINGLE;
            this.singleTexture = singleTexture;
        }

        public TextureContainer(Function<T, ResourceLocation> texMapper) {
            this.strategy = Strategy.MAPPER;
            this.texMapper = texMapper;
        }

        public TextureContainer(Predicate<T> condition, ResourceLocation trueTex, ResourceLocation falseTex) {
            this.strategy = Strategy.CONDITION;
            this.condition = condition;
            this.trueTex = trueTex;
            this.falseTex = falseTex;
        }

        public ResourceLocation getTexture(T entity) {
            switch(strategy) {
            case SINGLE:
                return singleTexture;
            case MAPPER:
                return texMapper.apply(entity);
            case CONDITION:
                return condition.test(entity) ? trueTex : falseTex;
            }
            return null;
        }
    }

    public static class ModelContainer<T extends MobEntity, A extends EntityModel<T>> {

        private final Strategy strategy;
        private final A baseModel;
        private Function<T, EntityModel<T>> modelMapper;
        private A trueModel;
        private EntityModel<T> falseModel;
        private Predicate<T> condition;

        public ModelContainer(A baseModel) {
            this.strategy = Strategy.SINGLE;
            this.baseModel = baseModel;
        }

        public ModelContainer(Function<T, EntityModel<T>> modelMapper, A baseModel) {
            this.strategy = Strategy.MAPPER;
            this.modelMapper = modelMapper;
            this.baseModel = baseModel;
        }

        public ModelContainer(Predicate<T> condition, A trueModel, EntityModel<T> falseModel) {
            this.strategy = Strategy.CONDITION;
            this.condition = condition;
            this.trueModel = trueModel;
            this.falseModel = falseModel;
            this.baseModel = trueModel;
        }

        public EntityModel<T> getModel(T entity) {
            switch(strategy) {
            case SINGLE:
                return baseModel;
            case MAPPER:
                return modelMapper.apply(entity);
            case CONDITION:
                return condition.test(entity) ? trueModel : falseModel;
            }
            return null;
        }

        public A getBaseModel() {
            return baseModel;
        }
    }

    public enum Strategy {
        SINGLE,
        MAPPER,
        CONDITION;
    }

    public enum SuperCallApplyRotations {
        PRE,
        NONE,
        POST;
    }

    public static class Builder<T extends MobEntity, A extends EntityModel<T>> {

        private final String modid;
        private final float shadow;
        private TextureContainer<T, A> tex;
        private ModelContainer<T, A> model;
        private PreRenderCallback<T> preRender;
        private ArrayList<Function<BaseRenderer<T, A>, LayerRenderer<T, A>>> layers = new ArrayList<>();
        private HandleRotation<T> handleRotation;
        private ApplyRotations<T> applyRotations;
        private SuperCallApplyRotations superCallApplyRotations = SuperCallApplyRotations.NONE;

        protected Builder(String modid, float shadow) {
            this.modid = modid;
            this.shadow = shadow;
        }

        public Builder<T, A> layer(Function<BaseRenderer<T, A>, LayerRenderer<T, A>> layer) {
            layers.add(layer);
            return this;
        }

        public Builder<T, A> tSingle(String texture) {
            this.tex = new TextureContainer<T, A>(tex(modid, texture));
            return this;
        }

        public Builder<T, A> tCondition(Predicate<T> condition, String trueTex, String falseTex) {
            this.tex = new TextureContainer<T, A>(condition, tex(modid, trueTex), tex(modid, falseTex));
            return this;
        }

        public Builder<T, A> tMapped(Function<T, String> texMapper) {
            this.tex = new TextureContainer<T, A>(entity -> tex(modid, texMapper.apply(entity)));
            return this;
        }

        public Builder<T, A> tSingleRaw(ResourceLocation texture) {
            this.tex = new TextureContainer<T, A>(texture);
            return this;
        }

        public Builder<T, A> tConditionRaw(Predicate<T> condition, ResourceLocation trueTex, ResourceLocation falseTex) {
            this.tex = new TextureContainer<T, A>(condition, trueTex, falseTex);
            return this;
        }

        public Builder<T, A> tMappedRaw(Function<T, ResourceLocation> texMapper) {
            this.tex = new TextureContainer<T, A>(texMapper);
            return this;
        }

        public Builder<T, A> mSingle(A model) {
            this.model = new ModelContainer<T, A>(model);
            return this;
        }

        public Builder<T, A> mMapped(Function<T, EntityModel<T>> modelMapper, A baseModel) {
            this.model = new ModelContainer<T, A>(modelMapper, baseModel);
            return this;
        }

        public Builder<T, A> mCondition(Predicate<T> condition, A trueModel, EntityModel<T> falseModel) {
            this.model = new ModelContainer<T, A>(condition, trueModel, falseModel);
            return this;
        }

        public Builder<T, A> preRender(PreRenderCallback<T> preRender) {
            this.preRender = preRender;
            return this;
        }

        public Builder<T, A> childScale(Predicate<T> isChild, float xScale, float yScale, float zScale) {
            preRender((e, s, p) -> {
                if(isChild.test(e)) {
                    s.scale(xScale, yScale, zScale);
                }
            });
            return this;
        }

        public Builder<T, A> childScale(Predicate<T> isChild, float scale) {
            return childScale(isChild, scale, scale, scale);
        }

        public Builder<T, A> handleRotation(HandleRotation<T> handleRotationFunc) {
            this.handleRotation = handleRotationFunc;
            return this;
        }

        public Builder<T, A> applyRotations(ApplyRotations<T> applyRotationsFunc, SuperCallApplyRotations superCall) {
            this.superCallApplyRotations = superCall;
            return applyRotations(applyRotationsFunc);
        }

        public Builder<T, A> applyRotations(ApplyRotations<T> applyRotationsFunc) {
            this.applyRotations = applyRotationsFunc;
            return this;
        }

        public IRenderFactory<T> done() {
            if(tex == null || model == null) {
                throw new IllegalArgumentException("Must define both a texture and a model before calling build()!");
            }
            return mgr -> new ImplRenderer<T, A>(mgr, shadow, tex, model, preRender, handleRotation, applyRotations, superCallApplyRotations).layers(layers);
        }
    }

    public static <T extends MobEntity, A extends EntityModel<T>> Builder<T, A> factory(String modid, float shadow) {
        return new Builder<T, A>(modid, shadow);
    }

    private static ResourceLocation tex(String modid, String location) {
        return new ResourceLocation(modid, "textures/entity/" + location + ".png");
    }

    @FunctionalInterface
    public static interface PreRenderCallback<T extends MobEntity> {
        public void preRenderCallback(T entity, MatrixStack stack, float partialTicks);
    }

    @FunctionalInterface
    public static interface HandleRotation<T extends MobEntity> {
        public float handleRotation(T entity, float partialTicks);
    }

    @FunctionalInterface
    public static interface ApplyRotations<T extends MobEntity> {
        public void applyRotations(T entity, MatrixStack stack, float ageInTicks, float rotationYaw, float partialTicks);
    }

    @FunctionalInterface
    public static interface RenderDef<T extends MobEntity, A extends EntityModel<T>> {
        public abstract ImplRenderer.Builder<T, A> apply(ImplRenderer.Builder<T, A> renderer);
    }

}
