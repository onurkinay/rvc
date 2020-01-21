package tr.com.onurkinay.rvc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

private const val TAG = "MyBroadcastReceiver"

class MyBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {


        val action = intent.getStringExtra("volume")
        if (action == "volumeDown") {
            Toast.makeText(context, "Volume -", Toast.LENGTH_LONG).show()
        } else if (action == "volumeUp") {

                        Toast.makeText(context, "Volume +", Toast.LENGTH_LONG).show()

                }
        }
    }

