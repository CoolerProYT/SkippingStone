package com.coolerpromc.skippingstone.platform;

import com.coolerpromc.skippingstone.Constants;
import com.coolerpromc.skippingstone.network.HandledCustomPacketPayload;
import com.coolerpromc.skippingstone.platform.services.IRegistryHelper;
import com.coolerpromc.skippingstone.platform.util.CreativeTabOutput;
import com.coolerpromc.skippingstone.platform.util.RegistryHandler;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class NeoForgeRegistryHelper implements IRegistryHelper {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Constants.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Constants.MODID);
    public static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(Constants.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, Constants.MODID);
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, Constants.MODID);
    public static final DeferredRegister<Identifier> STATS = DeferredRegister.create(BuiltInRegistries.CUSTOM_STAT, Constants.MODID);
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Constants.MODID);

    private final List<ServerBoundPayloadEntry<?>> serverboundPayloads = new ArrayList<>();

    @Override
    public <T extends Block> RegistryHandler.Blocks<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> func, BlockBehaviour.Properties p) {
        DeferredBlock<T> deferredBlock = BLOCKS.registerBlock(name, func, () -> p);
        return () -> deferredBlock;
    }

    @Override
    public <T extends Item> RegistryHandler.Items<T> registerItem(String name, Function<Item.Properties, T> func, Item.Properties p) {
        DeferredItem<T> deferredItem = ITEMS.registerItem(name, func, () -> p);
        return () -> deferredItem;
    }

    @Override
    public <T extends Entity> RegistryHandler.Entities<T> registerEntity(String name, EntityType.EntityFactory<T> factory, MobCategory category, UnaryOperator<EntityType.Builder<T>> builder) {
        DeferredHolder<EntityType<?>, EntityType<T>> deferredHolder = ENTITIES.registerEntityType(name, factory, category, builder);
        return () -> deferredHolder;
    }

    @Override
    public RegistryHandler<CreativeModeTab, CreativeModeTab> registerCreativeTab(String name, Supplier<ItemStack> icon, Component title, BiConsumer<CreativeTabOutput, CreativeModeTab.ItemDisplayParameters> entries) {
        DeferredHolder<CreativeModeTab, CreativeModeTab> deferredHolder = CREATIVE_TABS.register(name, () -> CreativeModeTab.builder().icon(icon).title(title).displayItems((p, o) -> entries.accept(o::accept, p)).build());
        return () -> deferredHolder;
    }

    @Override
    public <T extends ParticleType<?>> RegistryHandler<ParticleType<?>, T> registerParticleType(String name, Supplier<T> type) {
        DeferredHolder<ParticleType<?>, T> deferredHolder = PARTICLE_TYPES.register(name, type);
        return () -> deferredHolder;
    }

    @Override
    public RegistryHandler<Identifier, Identifier> registerStat(String name) {
        DeferredHolder<Identifier, Identifier> deferredHolder = STATS.register(name, () -> Constants.id(name));
        return () -> deferredHolder;
    }

    @Override
    public <T> RegistryHandler.Components<T> registerDataComponent(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        DeferredHolder<DataComponentType<?>, DataComponentType<T>> deferredHolder = DATA_COMPONENTS.registerComponentType(name, builder);
        return () -> deferredHolder;
    }

    @Override
    public <T extends HandledCustomPacketPayload> void registerServerBoundPayload(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        this.serverboundPayloads.add(new ServerBoundPayloadEntry<>(type, streamCodec));
    }

    @Override
    public void applyServerBoundPayloadRegistrations(ServerBoundPayloadRegistrar registrar) {
        for (ServerBoundPayloadEntry<?> entry : this.serverboundPayloads) {
            entry.register(registrar);
        }
    }

    public static void register(IEventBus eventBus){
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        ENTITIES.register(eventBus);
        CREATIVE_TABS.register(eventBus);
        DATA_COMPONENTS.register(eventBus);
        PARTICLE_TYPES.register(eventBus);
        STATS.register(eventBus);
    }

    private record ServerBoundPayloadEntry<T extends HandledCustomPacketPayload>(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        private void register(ServerBoundPayloadRegistrar registrar) {
            registrar.register(this.type, this.streamCodec);
        }
    }
}
