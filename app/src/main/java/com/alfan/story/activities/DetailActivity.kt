package com.alfan.story.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.alfan.story.R
import com.alfan.story.data.response.StoriesResponse
import com.alfan.story.databinding.ActivityDetailBinding

import android.view.MenuItem




class DetailActivity : AppCompatActivity() {

    private lateinit var detBinding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        detBinding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(detBinding.root)

        val item = intent.getParcelableExtra<StoriesResponse.Story>("item;")

        if (item != null) {
            detBinding.nama.text = item.name
            detBinding.desc.text = item.desc
            detBinding.created.text = getString(R.string.label_tanggal, item.createdAt)

            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = item.name

            Glide.with(applicationContext)
                .load(item.urlPhoto)
                .centerCrop()
                .into(detBinding.banner)

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}