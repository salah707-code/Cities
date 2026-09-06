package com.example.areadirectory.data.loader

import android.content.Context
import com.example.areadirectory.data.model.Category
import com.example.areadirectory.data.model.Country
import com.example.areadirectory.data.model.District
import com.example.areadirectory.data.model.Governorate
import com.example.areadirectory.data.model.LocalDataDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object LocalDataLoader {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    fun loadData(context: Context, assetPath: String = "data/yemen.json"): Pair<List<Country>, List<Category>> {
        val jsonString = context.assets.open(assetPath).bufferedReader().use { it.readText() }
        return parseData(jsonString)
    }

    fun parseData(jsonString: String): Pair<List<Country>, List<Category>> {
        val adapter = moshi.adapter(LocalDataDto::class.java)
        val dto = adapter.fromJson(jsonString) ?: return Pair(emptyList(), emptyList())

        val countries = dto.countries.orEmpty().map { cDto ->
            Country(
                id = cDto.id.orEmpty(),
                nameAr = cDto.nameAr.orEmpty(),
                nameEn = cDto.nameEn.orEmpty(),
                altNames = cDto.altNames.orEmpty(),
                governorates = cDto.governorates.orEmpty().map { gDto ->
                    Governorate(
                        id = gDto.id.orEmpty(),
                        nameAr = gDto.nameAr.orEmpty(),
                        nameEn = gDto.nameEn.orEmpty(),
                        altNames = gDto.altNames.orEmpty(),
                        districts = gDto.districts.orEmpty().map { dDto ->
                            District(
                                id = dDto.id.orEmpty(),
                                nameAr = dDto.nameAr.orEmpty(),
                                nameEn = dDto.nameEn.orEmpty(),
                                altNames = dDto.altNames.orEmpty()
                            )
                        }
                    )
                }
            )
        }

        val categories = dto.categories.orEmpty().map { catDto ->
            Category(
                id = catDto.id.orEmpty(),
                nameAr = catDto.nameAr.orEmpty(),
                nameEn = catDto.nameEn.orEmpty(),
                googleQuery = catDto.googleQuery.orEmpty()
            )
        }

        return Pair(countries, categories)
    }
}
