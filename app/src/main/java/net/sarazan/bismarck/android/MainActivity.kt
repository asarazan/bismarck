package net.sarazan.bismarck.android

import FooViewModel
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_main.*
import sample.R

class MainActivity : AppCompatActivity() {

    private val vm = FooViewModel(lifecycleScope)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        vm.foo.onValue {
            main_text.text = "Hello and $it"
        }
    }
}
