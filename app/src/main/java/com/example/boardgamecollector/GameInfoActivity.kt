package com.example.boardgamecollector

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.boardgamecollector.databinding.ActivityGameInfoBinding

class GameInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGameInfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameInfoBinding.inflate(layoutInflater)

        setContentView(binding.root)
    }
}