package com.simplemobiletools.clock.models

enum class AlarmSort(open val value: Int) {
    CREATED_AT(0),
    TIME_OF_DAY(1);

    companion object {
        fun default(): AlarmSort {
            return CREATED_AT
        }

        fun valueOf(value: Int): AlarmSort? {
            return AlarmSort.values()
                    .filter { it.value == value }
                    .getOrNull(0)
        }
    }
}
