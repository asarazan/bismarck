package net.sarazan.bismarck.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import net.sarazan.bismarck.sample.HelloWorld
import kotlinx.android.synthetic.main.activity_main.txtBody
import net.sarazan.bismarck.Bismarcks
import net.sarazan.bismarck.fetcher

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtBody.text = HelloWorld.commonCode()

        val b = Bismarcks.dedupingBismarck<String>().fetcher {
            "Foo"
        }
    }
}
