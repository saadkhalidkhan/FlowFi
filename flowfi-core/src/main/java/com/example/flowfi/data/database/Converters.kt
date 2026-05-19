package com.example.flowfi.data.database

import androidx.room.TypeConverter
import com.example.flowfi.data.entity.TransactionType

class Converters {
  @TypeConverter
  fun fromTransactionType(value: TransactionType): String = value.name

  @TypeConverter
  fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)
}
