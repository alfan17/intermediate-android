package com.alfan.story.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alfan.story.databinding.RecyclerviewLoadingBinding

class LoadingAdapter(private val retry: () -> Unit):
    LoadStateAdapter<LoadingAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState) = ViewHolder(
        RecyclerviewLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, loadState: LoadState) {
        holder.loadBinding.retry.setOnClickListener { retry.invoke() }
        holder.bindItem(loadState)
    }

    class ViewHolder(val loadBinding: RecyclerviewLoadingBinding): RecyclerView.ViewHolder(loadBinding.root) {
        fun bindItem(loadState: LoadState) {
            if (loadState is LoadState.Error) {
                loadBinding.error.text = loadState.error.localizedMessage
            }
            loadBinding.progressBar.isVisible = loadState is LoadState.Loading
            loadBinding.retry.isVisible = loadState is LoadState.Error
            loadBinding.error.isVisible = loadState is LoadState.Error
        }
    }

}