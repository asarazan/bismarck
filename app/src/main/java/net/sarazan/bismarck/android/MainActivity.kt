package net.sarazan.bismarck.android

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch
import net.sarazan.bismarck.mobile.FeedViewModel
import net.sarazan.bismarck.mobile.FooViewModel
import sample.R

class MainActivity : AppCompatActivity() {

    private val feedViewModel: FeedViewModel by lazy {
        FeedViewModel(lifecycleScope)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lifecycleScope.launch {
            feedViewModel.cache.eachValue {
                Log.d("@@@", "eachValue - $it")
                it ?: return@eachValue
                main_text.text = it.items.firstOrNull()?.title
            }
        }

    }
}
