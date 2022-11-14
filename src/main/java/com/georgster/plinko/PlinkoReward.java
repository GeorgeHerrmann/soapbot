package com.georgster.plinko;

public class PlinkoReward<T> {
    T reward;

    PlinkoReward(T rewardType) {
        reward = rewardType;
    }

    public T getReward() {
        return reward;
    }
}
