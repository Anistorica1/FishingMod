package com.example.fullmod;

public enum FishingState {
    IDLE,           // 未启动
    CASTING,        // 抛竿
    WAITING,        // 等待上钩
    BITE,           // 检测到鱼上钩
    REELING,        // 收竿
    COOLDOWN,       // 冷却
    HYPERION        // Hype右键
}
