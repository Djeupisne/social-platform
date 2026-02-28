package com.social.main.enums;

import lombok.Getter;

@Getter
public enum TrancheSalariale {
    TRANCHE_1(0, 30000, 10, "[0 ; 30.000["),
    TRANCHE_2(30000, 100000, 20, "[30.000 ; 100.000["),
    TRANCHE_3(100000, 200000, 30, "[100.000 ; 200.000["),
    TRANCHE_4(200000, 700000, 40, "[200.000 ; 700.000["),
    TRANCHE_5(700000, 1000000, 45, "[700.000 ; 1.000.000["),
    TRANCHE_6(1000000, Integer.MAX_VALUE, 55, "[1.000.000 ; plus[");

    private final int min;
    private final int max;
    private final int points;
    private final String label;

    TrancheSalariale(int min, int max, int points, String label) {
        this.min = min;
        this.max = max;
        this.points = points;
        this.label = label;
    }
}