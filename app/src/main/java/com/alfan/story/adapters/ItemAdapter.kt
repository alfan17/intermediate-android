package com.alfan.story.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.alfan.story.R
import com.alfan.story.data.response.StoriesResponse
import com.alfan.story.databinding.RecyclerviewItemBinding

class ItemAdapter(private var onClickListener: (RecyclerviewItemBinding, StoriesResponse.Story) -> Unit): PagingDataAdapter<StoriesResponse.Story, ItemAdapter.ViewHolder>(diffUtils) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(RecyclerviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let { item ->
            holder.bindItem(item)
            holder.itemBinding.root.setOnClickListener {
                onClickListener.invoke(holder.itemBinding, item)
            }
        }
    }

    class ViewHolder(val itemBinding: RecyclerviewItemBinding): RecyclerView.ViewHolder(itemBinding.root) {
        fun bindItem(item: StoriesResponse.Story) {
            itemBinding.nama.text = item.name
            itemBinding.desc.text = item.desc
            itemBinding.created.text = itemBinding.root.resources.getString(R.string.label_tanggal, item.createdAt)

            Glide.with(itemView.context)
                .load(item.urlPhoto)
                .centerCrop()
                .into(itemBinding.banner)
        }
    }

    companion object {
        private val diffUtils = object : DiffUtil.ItemCallback<StoriesResponse.Story>() {
            override fun areItemsTheSame(
                oldItem: StoriesResponse.Story,
                newItem: StoriesResponse.Story
            ): Boolean = oldItem.idStory == newItem.idStory

            override fun areContentsTheSame(
                oldItem: StoriesResponse.Story,
                newItem: StoriesResponse.Story
            ): Boolean = oldItem.idStory == newItem.idStory
        }
    }
}