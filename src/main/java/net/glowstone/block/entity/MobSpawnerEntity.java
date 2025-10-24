package net.glowstone.block.entity;

import lombok.Getter;
import lombok.Setter;
import net.glowstone.block.GlowBlock;
import net.glowstone.block.GlowBlockState;
import net.glowstone.block.entity.state.GlowCreatureSpawner;
import net.glowstone.util.nbt.CompoundTag;
import org.bukkit.entity.EntityType;

public class MobSpawnerEntity extends BlockEntity {

    private static final EntityType DEFAULT = EntityType.PIG;

    @Getter
    @Setter
    private EntityType spawning;
    @Getter
    @Setter
    private int delay;

    public MobSpawnerEntity(GlowBlock block) {
        super(block);
        setSaveId("minecraft:mob_spawner");
    }

    @Override
    public void loadNbt(CompoundTag tag) {
        super.loadNbt(tag);
        // 1.13+ stores entity id inside SpawnData compound as namespaced id
        spawning = tag.tryGetCompound("SpawnData")
                .flatMap(sd -> sd.tryGetString("id"))
                .map(id -> {
                    String simple = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
                    EntityType t = EntityType.fromName(simple);
                    return t != null ? t : DEFAULT;
                })
                .orElseGet(() -> tag.tryGetString("EntityId").map(EntityType::fromName).orElse(DEFAULT));
        delay = tag.tryGetInt("Delay").orElse(0);
    }

    @Override
    public GlowBlockState getState() {
        return new GlowCreatureSpawner(block);
    }

    @Override
    public void saveNbt(CompoundTag tag) {
        super.saveNbt(tag);
        String simple = spawning == null ? "" : spawning.getName();
        String namespaced = simple == null || simple.isEmpty() ? "" : ("minecraft:" + simple.toLowerCase());
        // Write both legacy and modern fields for compatibility
        tag.putString("EntityId", simple == null ? "" : simple);
        CompoundTag spawnData = new CompoundTag();
        if (!namespaced.isEmpty()) {
            spawnData.putString("id", namespaced);
        }
        tag.putCompound("SpawnData", spawnData);
        tag.putInt("Delay", delay);
    }
}
