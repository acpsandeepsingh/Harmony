package com.sansoft.harmony.local.subscription.item

import android.view.View
import com.sansoft.harmony.R
import com.sansoft.harmony.databinding.FeedGroupAddNewGridItemBinding
import com.xwray.groupie.viewbinding.BindableItem

class FeedGroupAddNewGridItem : BindableItem<FeedGroupAddNewGridItemBinding>() {
    override fun getLayout(): Int = R.layout.feed_group_add_new_grid_item
    override fun initializeViewBinding(view: View) = FeedGroupAddNewGridItemBinding.bind(view)
    override fun bind(viewBinding: FeedGroupAddNewGridItemBinding, position: Int) {
        // this is a static item, nothing to do here
    }
}
