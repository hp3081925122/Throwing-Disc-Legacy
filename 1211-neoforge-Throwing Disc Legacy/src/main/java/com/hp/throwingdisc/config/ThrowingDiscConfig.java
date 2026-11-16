package com.hp.throwingdisc.config;

import java.util.EnumMap;
import java.util.Map;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ThrowingDiscConfig {
    public static final Startup STARTUP;
    public static final ModConfigSpec STARTUP_SPEC;
    public static final Common COMMON;
    public static final ModConfigSpec COMMON_SPEC;
    public static final Client CLIENT;
    public static final ModConfigSpec CLIENT_SPEC;

    static {
        var startup = new ModConfigSpec.Builder().configure(Startup::new);
        STARTUP = startup.getLeft();
        STARTUP_SPEC = startup.getRight();

        var common = new ModConfigSpec.Builder().configure(Common::new);
        COMMON = common.getLeft();
        COMMON_SPEC = common.getRight();

        var client = new ModConfigSpec.Builder().configure(Client::new);
        CLIENT = client.getLeft();
        CLIENT_SPEC = client.getRight();
    }

    private ThrowingDiscConfig() {
    }

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.STARTUP, STARTUP_SPEC);
        container.registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
    }

    public static final class Startup {
        private final Map<DiscDefaults, StartupStats> stats = new EnumMap<>(DiscDefaults.class);

        Startup(ModConfigSpec.Builder builder) {
            builder.comment(
                "各材质圆盘基础物品数值，修改后需重启游戏",
                "Base item stats per disc material, restart required after changes"
            ).push("discs");
            for (DiscDefaults defaults : DiscDefaults.values()) {
                this.stats.put(defaults, new StartupStats(builder, defaults));
            }
            builder.pop();
        }

        public StartupStats stats(DiscDefaults defaults) {
            return this.stats.get(defaults);
        }
    }

    public static final class StartupStats {
        public final ModConfigSpec.IntValue durability;
        public final ModConfigSpec.DoubleValue meleeDamageBonus;
        public final ModConfigSpec.DoubleValue attackSpeedBonus;

        StartupStats(ModConfigSpec.Builder builder, DiscDefaults defaults) {
            builder.push(defaults.id());
            this.durability = builder.comment("最大耐久度", "Maximum durability")
                .defineInRange("durability", defaults.durability(), 1, 1000000);
            this.meleeDamageBonus = builder.comment("近战伤害加成（在玩家 1 点基础伤害上增加）", "Melee attack damage bonus (added to base 1.0)")
                .defineInRange("melee_damage_bonus", defaults.meleeDamageBonus(), 0.0, 1024.0);
            this.attackSpeedBonus = builder.comment("近战攻击速度修正（在基础 4.0 上增加）", "Melee attack speed modifier (added to base 4.0)")
                .defineInRange("attack_speed_bonus", defaults.attackSpeedBonus(), -3.9, 16.0);
            builder.pop();
        }
    }

    public static final class Common {
        public final ModConfigSpec.BooleanValue allowOffhandThrow;
        public final ModConfigSpec.BooleanValue useSlotPlaceholder;
        public final ModConfigSpec.BooleanValue pierceMultipleTargets;
        public final ModConfigSpec.DoubleValue returnDamageMultiplier;
        public final ModConfigSpec.DoubleValue returnSpeedMultiplier;
        public final ModConfigSpec.IntValue maxReturnTicks;

        public final ModConfigSpec.IntValue ricochetExtraBouncesPerLevel;
        public final ModConfigSpec.DoubleValue ricochetExtraRangePerLevel;
        public final ModConfigSpec.DoubleValue ricochetBounceDamageBonusPerLevel;
        public final ModConfigSpec.DoubleValue maxRicochetDamageMultiplier;
        public final ModConfigSpec.DoubleValue tailwindSpeedBonusPerLevel;
        public final ModConfigSpec.DoubleValue tailwindReturnBonusPerLevel;
        public final ModConfigSpec.DoubleValue reboundEdgeBonusDamagePerLevel;
        public final ModConfigSpec.DoubleValue magneticRecallRadius;
        public final ModConfigSpec.IntValue cleavingBlocksPerLevel;
        public final ModConfigSpec.DoubleValue cleavingBaseHardness;
        public final ModConfigSpec.DoubleValue cleavingHardnessPerLevel;
        public final ModConfigSpec.BooleanValue cleavingBreakBlockEntities;
        public final ModConfigSpec.BooleanValue cleavingAllowDispenser;
        public final ModConfigSpec.IntValue veinHarvestBlocksPerLevel;

        public final ModConfigSpec.BooleanValue enableLootInjection;
        public final ModConfigSpec.BooleanValue lootTrialChambers;
        public final ModConfigSpec.BooleanValue lootVillageWeaponsmith;
        public final ModConfigSpec.BooleanValue lootBastionTreasure;
        public final ModConfigSpec.BooleanValue lootEndCityTreasure;
        public final ModConfigSpec.BooleanValue lootBuriedTreasure;

        private final Map<DiscDefaults, FlightStats> flightStats = new EnumMap<>(DiscDefaults.class);

        Common(ModConfigSpec.Builder builder) {
            builder.comment(
                "投掷圆盘通用战斗与飞行机制",
                "General combat and flight mechanics for throwing discs"
            ).push("general");
            this.allowOffhandThrow = builder.comment("是否允许从副手投掷圆盘", "Allow throwing discs from the off-hand")
                .define("allow_offhand_throw", true);
            this.useSlotPlaceholder = builder.comment(
                "投掷圆盘后是否在原栏位生成占位物品，防止拾取掉落物占据手持栏位",
                "Place a slot-reserving placeholder item after throwing a disc so picked-up drops do not occupy the hand"
            ).define("use_slot_placeholder", true);
            this.pierceMultipleTargets = builder.comment("是否允许单次飞行轨迹穿透多个目标", "Allow piercing through multiple targets in one trajectory")
                .define("pierce_multiple_targets", true);
            this.returnDamageMultiplier = builder.comment("返程阶段基础伤害倍率", "Base damage multiplier while returning to owner")
                .defineInRange("return_damage_multiplier", 0.85, 0.0, 10.0);
            this.returnSpeedMultiplier = builder.comment("返程阶段基础飞行速度倍率", "Base speed multiplier while returning to owner")
                .defineInRange("return_speed_multiplier", 1.0, 0.2, 5.0);
            this.maxReturnTicks = builder.comment("返程最长持续刻数，超时将直接归还至玩家背包或脚下", "Maximum ticks spent returning before directly returning to inventory/feet")
                .defineInRange("max_return_ticks", 200, 20, 2400);
            builder.pop();

            builder.comment(
                "各材质圆盘投掷飞行属性",
                "Per-material thrown flight properties"
            ).push("discs");
            for (DiscDefaults defaults : DiscDefaults.values()) {
                this.flightStats.put(defaults, new FlightStats(builder, defaults));
            }
            builder.pop();

            builder.comment(
                "专属附魔数值设置",
                "Custom enchantment tuning values"
            ).push("enchantments");
            this.ricochetExtraBouncesPerLevel = builder.comment("弹射（Ricochet）每级额外增加的最大反弹次数", "Extra max bounces per level of Ricochet")
                .defineInRange("ricochet_extra_bounces_per_level", 2, 0, 32);
            this.ricochetExtraRangePerLevel = builder.comment("弹射（Ricochet）每级额外增加的最大射程（格）", "Extra max range in blocks per level of Ricochet")
                .defineInRange("ricochet_extra_range_per_level", 3.0, 0.0, 64.0);
            this.ricochetBounceDamageBonusPerLevel = builder.comment("弹射（Ricochet）每级在每次墙壁反弹后叠加的伤害比例", "Damage bonus ratio per wall bounce per level of Ricochet")
                .defineInRange("ricochet_bounce_damage_bonus_per_level", 0.08, 0.0, 2.0);
            this.maxRicochetDamageMultiplier = builder.comment("弹射反弹叠加伤害倍率上限", "Maximum total damage multiplier from wall ricochets")
                .defineInRange("max_ricochet_damage_multiplier", 2.5, 1.0, 10.0);
            this.tailwindSpeedBonusPerLevel = builder.comment("疾旋（Tailwind）每级投掷初速度加成比例", "Throw speed bonus ratio per level of Tailwind")
                .defineInRange("tailwind_speed_bonus_per_level", 0.16, 0.0, 2.0);
            this.tailwindReturnBonusPerLevel = builder.comment(
                "疾旋（Tailwind）每级返程额外速度加成比例（默认 0，与去程保持同速）",
                "Extra return speed bonus ratio per level of Tailwind (0 by default to match outbound speed)"
            ).defineInRange("tailwind_return_bonus_per_level", 0.0, 0.0, 2.0);
            this.reboundEdgeBonusDamagePerLevel = builder.comment("回旋斩（Returning Edge）每级在返程命中时额外增加的伤害", "Extra damage dealt while returning per level of Returning Edge")
                .defineInRange("rebound_edge_bonus_damage_per_level", 2.0, 0.0, 64.0);
            this.magneticRecallRadius = builder.comment("磁吸召回（Magnetic Recall）牵引沿途掉落物与经验球的半径（格）", "Item and XP orb pickup attraction radius in blocks for Magnetic Recall")
                .defineInRange("magnetic_recall_radius", 2.75, 0.5, 16.0);
            this.cleavingBlocksPerLevel = builder.comment("旋刃采掘（Cleaving Harvest）每级单次投掷最多可破坏的方块数", "Maximum blocks broken per throw per level of Cleaving Harvest")
                .defineInRange("cleaving_blocks_per_level", 2, 1, 32);
            this.cleavingBaseHardness = builder.comment("旋刃采掘（Cleaving Harvest）基础可破坏方块硬度上限", "Base block hardness limit for Cleaving Harvest")
                .defineInRange("cleaving_base_hardness", 2.0, 0.0, 100.0);
            this.cleavingHardnessPerLevel = builder.comment("旋刃采掘（Cleaving Harvest）每级额外增加的可破坏方块硬度上限", "Extra block hardness limit per level of Cleaving Harvest")
                .defineInRange("cleaving_hardness_per_level", 2.5, 0.0, 100.0);
            this.cleavingBreakBlockEntities = builder.comment("旋刃采掘是否允许破坏带有方块实体的容器或机器方块", "Allow Cleaving Harvest to break blocks with BlockEntities (e.g. chests, machines)")
                .define("cleaving_break_block_entities", false);
            this.cleavingAllowDispenser = builder.comment("发射器射出的带旋刃采掘附魔圆盘是否允许破坏方块（同时受 mobGriefing 游戏规则约束）", "Allow dispenser-launched discs with Cleaving Harvest to break blocks (also requires mobGriefing)")
                .define("cleaving_allow_dispenser", true);
            this.veinHarvestBlocksPerLevel = builder.comment("连锁采集（Vein Harvest）每级触发矿脉连锁时最多额外破坏的相连同类矿石数", "Maximum connected same-type ore blocks broken per level of Vein Harvest")
                .defineInRange("vein_harvest_blocks_per_level", 6, 1, 64);
            builder.pop();

            builder.comment(
                "结构战利品箱圆盘生成开关",
                "Structure chest loot generation toggles"
            ).push("loot");
            this.enableLootInjection = builder.comment("是否启用原版战利品箱生成投掷圆盘", "Enable injecting throwing discs into vanilla structure loot tables")
                .define("enable_loot_injection", true);
            this.lootTrialChambers = builder.comment("试炼密室宝箱与宝库辅助战利品", "Trial Chambers chests")
                .define("trial_chambers", true);
            this.lootVillageWeaponsmith = builder.comment("村庄武器匠宝箱", "Village weaponsmith chests")
                .define("village_weaponsmith", true);
            this.lootBastionTreasure = builder.comment("堡垒遗迹宝藏箱", "Bastion treasure chests")
                .define("bastion_treasure", true);
            this.lootEndCityTreasure = builder.comment("末地城宝箱", "End city treasure chests")
                .define("end_city_treasure", true);
            this.lootBuriedTreasure = builder.comment("埋藏的宝藏", "Buried treasure chests")
                .define("buried_treasure", true);
            builder.pop();
        }

        public FlightStats flight(DiscDefaults defaults) {
            return this.flightStats.get(defaults);
        }
    }

    public static final class FlightStats {
        public final ModConfigSpec.DoubleValue thrownDamage;
        public final ModConfigSpec.DoubleValue throwSpeed;
        public final ModConfigSpec.DoubleValue maxRange;
        public final ModConfigSpec.IntValue maxBounces;
        public final ModConfigSpec.IntValue cooldownTicks;

        FlightStats(ModConfigSpec.Builder builder, DiscDefaults defaults) {
            builder.push(defaults.id());
            this.thrownDamage = builder.comment("投掷基础伤害", "Base thrown damage")
                .defineInRange("thrown_damage", defaults.thrownDamage(), 0.5, 1024.0);
            this.throwSpeed = builder.comment("投掷飞行速度（格/刻）", "Thrown flight speed (blocks/tick)")
                .defineInRange("throw_speed", defaults.throwSpeed(), 0.2, 8.0);
            this.maxRange = builder.comment("去程最大飞行射程（格）", "Maximum outbound flight range (blocks)")
                .defineInRange("max_range", defaults.maxRange(), 2.0, 256.0);
            this.maxBounces = builder.comment("基础最大坚硬表面弹射次数", "Base maximum ricochet bounces off solid surfaces")
                .defineInRange("max_bounces", defaults.maxBounces(), 0, 64);
            this.cooldownTicks = builder.comment("投掷后同材质物品冷却时间（刻）", "Cooldown ticks after throwing")
                .defineInRange("cooldown_ticks", defaults.cooldownTicks(), 0, 1200);
            builder.pop();
        }
    }

    public static final class Client {
        public final ModConfigSpec.DoubleValue spinDegreesPerTick;
        public final ModConfigSpec.BooleanValue showBounceParticles;

        Client(ModConfigSpec.Builder builder) {
            builder.comment(
                "投掷圆盘客户端渲染设置",
                "Client rendering settings for throwing discs"
            ).push("rendering");
            this.spinDegreesPerTick = builder.comment("飞行时每刻自转角度", "Spin rotation degrees per tick while flying")
                .defineInRange("spin_degrees_per_tick", 38.0, 0.0, 360.0);
            this.showBounceParticles = builder.comment("是否显示飞行尾迹粒子", "Show subtle flight trail particles")
                .define("show_trail_particles", true);
            builder.pop();
        }
    }
}
