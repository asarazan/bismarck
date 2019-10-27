package com.jetbrains.handson.mpp.mobile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.gson.Gson
import net.sarazan.bismarck.Bismarcks
import net.sarazan.bismarck.android.persisters.AndroidPersisters
import net.sarazan.bismarck.fetcher
import net.sarazan.bismarck.serializers.GsonSerializer

class MainActivity : AppCompatActivity() {

    val persister = AndroidPersisters.account(this, "foo", "net.sarazan.bismarck", GsonSerializer(Any::class.java, Gson()))
    val bismarck = Bismarcks.dedupingBismarck<String>()
        .fetcher {
            "HELLOOOOOO from bismarck"
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bismarck.refresh()
        bismarck.observe().subscribe {
            runOnUiThread {
                findViewById<TextView>(R.id.main_text).text = bismarck.peek()
            }
        }
    }
}
