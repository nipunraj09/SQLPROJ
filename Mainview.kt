package com.example.katelinex

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.katelinex.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

   private lateinit var binding: ActivityMainBinding

   private var nameList : MutableList<SampleModel> = mutableListOf()
    private lateinit var sampleAdapter: SampleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadData()
        sampleAdapter = SampleAdapter(nameList)
        binding.apply{
            rvMain.apply{
                layoutManager= LinearLayoutManager(this@MainActivity)
                adapter= sampleAdapter
            }
        }

        }
    fun loadData(){
        nameList.add(SampleModel(1,"Sample title"))
        nameList.add(SampleModel(2,"Sample title"))
        nameList.add(SampleModel(3,"Sample title"))
        nameList.add(SampleModel(4,"Sample title"))
        nameList.add(SampleModel(5,"Sample title"))
        nameList.add(SampleModel(6,"Sample title"))
        nameList.add(SampleModel(7,"Sample title"))
    }
}
