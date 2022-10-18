package com.iman.submission1_storyapp.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import com.iman.submission1_storyapp.network.ApiConfig
import com.iman.submission1_storyapp.network.GetAllStoriesRespone
import com.iman.submission1_storyapp.R
import com.iman.submission1_storyapp.view.main.StoryModel
import retrofit2.Response

internal class StackRemoteViewsFactory(private val mContext: Context) :
    RemoteViewsService.RemoteViewsFactory {
    private val mWidgetItems = ArrayList<Bitmap>()
    override fun onCreate() {
    }

    override fun onDataSetChanged() {
        try {
            for (i in getPhotostory.indices) {
                val bitmap: Bitmap = Glide.with(mContext)
                    .asBitmap()
                    .load(getPhotostory[i].photoUrl)
                    .submit()
                    .get()
                mWidgetItems.add(bitmap)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val getPhotostory: ArrayList<StoryModel>
        get() {
            val listStory = ArrayList<StoryModel>()
            val token =
                "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLXh3ZGk2RzlzVHo1V0YyMS0iLCJpYXQiOjE2NjM4NTQ4NTN9.UDQQ0Sxr8YwMetITIiNpFepU9GI5RTbj5NMaOi3M09Y"
            val service = ApiConfig().getApiService().getAllStories(token)
            try {
                val response: Response<GetAllStoriesRespone> = service.execute()
                val apiResponse: GetAllStoriesRespone? = response.body()

                if (apiResponse != null) {
                    listStory.addAll(apiResponse.stories)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            return listStory
        }

    override fun onDestroy() {
    }

    override fun getCount(): Int = mWidgetItems.size

    override fun getViewAt(position: Int): RemoteViews {
        val rv = RemoteViews(mContext.packageName, R.layout.widget_item)
        rv.setImageViewBitmap(R.id.imageView, mWidgetItems[position])

        val extras = bundleOf(
            ImagesBannerWidget.EXTRA_ITEM_NAME to getPhotostory[position].name,
            ImagesBannerWidget.EXTRA_ITEM_DESCRIPTION to getPhotostory[position].description
        )
        val fillInIntent = Intent()
        fillInIntent.putExtras(extras)
        rv.setOnClickFillInIntent(R.id.imageView, fillInIntent)
        return rv
    }
    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(i: Int): Long = 0
    override fun hasStableIds(): Boolean = false
}