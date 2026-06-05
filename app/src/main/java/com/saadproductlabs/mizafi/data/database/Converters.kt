package com.saadproductlabs.mizafi.data.database

import androidx.room.TypeConverter
import com.saadproductlabs.mizafi.data.entity.TransactionType

class Converters {
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String {
        return value.name
    }

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return TransactionType.valueOf(value)
    }
}
