package com.example.foodclassify

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.BufferedReader
import java.io.InputStreamReader


data class MacrosTable(
    val label:String,
    var kcal:String,
    var carbs:String,
    var fat:String,
    var protein:String)

class MainViewModel : ViewModel()  {


    val currentResult: MutableLiveData<String> by lazy{
     MutableLiveData<String>()
    }
    val currentMacro: MutableLiveData<MacrosTable> by lazy {
        MutableLiveData<MacrosTable>()
    }
    val productList = mutableListOf<MacrosTable>()



    fun updateValue(label:String, prob:Float){

        currentResult.postValue(String.format("Produkt: %s | Wynik: %.1f%%", label, prob * 100.0f))
        val productFound = productList.find {it.label == label}

        val obj: MacrosTable = if (productFound != null){
            MacrosTable(label,
                productFound.kcal,
                productFound.carbs,
                productFound.fat,
                productFound.protein)

        } else{
            MacrosTable(label,
                "Brak danych",
                "Brak danych",
                "Brak danych",
                "Brak danych")
        }

        currentMacro.postValue(obj)

    }

    fun loadData(context: Context){
        val assetManager = context.assets
        val inputStream = assetManager.open("calories.csv")
        val buffReader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        val csvParser = CSVParser(buffReader, CSVFormat.DEFAULT.withFirstRecordAsHeader())
        for (csvLine in csvParser){
            productList.add(
                MacrosTable(csvLine["label"],
                            csvLine["kcal"],
                            csvLine["carbs"],
                            csvLine["fat"],
                            csvLine["protein"]))
        }
    }
}





