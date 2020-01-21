package tr.com.onurkinay.rvc

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.session.MediaSession
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.dialog_ip_setting.view.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    var volume: SeekBar? = null
    var left: Button? = null
    var right: Button? = null

    var ip = "192.168.1.8:8080" // your computer IP and port

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val queue = Volley.newRequestQueue(this)

        volume = findViewById(R.id.volume)
        left = findViewById(R.id.left)
        right = findViewById(R.id.right)

        volume?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {
                Toast.makeText(applicationContext, "volume level: : $progress", Toast.LENGTH_SHORT).show()
                sendVol(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                //starting touch
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                //stopping touch
            }
        })

        // Decreasing volume
        left?.setOnClickListener {

            var value = volume?.progress?.minus(3) as Int
            volume?.progress = value
        }

        //Increasing volume
        right?.setOnClickListener {

            var value = volume?.progress?.plus(3) as Int
            volume?.progress = value

        }
        setSupportActionBar(toolbar)



        fab.setOnClickListener { view ->
            Snackbar.make(view, "ben@onurkinay.com.tr \n-> having any problem, write me :)", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                val stringRequest = StringRequest(Request.Method.GET, "http://$ip/",
                    Response.Listener<String> { response ->
                        println(response)
                        volume?.progress = response.toInt()

                    },
                    Response.ErrorListener { /* any error */ })

                queue.add(stringRequest)
                handler.postDelayed(this, 1000) // 1 sec delay

            }
        }, 0)

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings) {
            showIPDialog()
        }
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun sendVol(sVol: Int) {//Sending volume level to computer

        val url = "http://$ip/"


        val params = HashMap<String, String>()
        params["volume"] = sVol.toString() //get volume level from the seekbar
        val jsonObject = JSONObject(params as Map<*, *>)

        // Volley post request with parameters
        val request = JsonObjectRequest(Request.Method.POST, url, jsonObject,
            Response.Listener { response ->
                try {
                } catch (e: Exception) {
                }

            }, Response.ErrorListener {

            })


        // Volley request policy, only one time request to avoid duplicate transaction
        request.retryPolicy = DefaultRetryPolicy(
            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
            // 0 means no retry
            0, // DefaultRetryPolicy.DEFAULT_MAX_RETRIES = 2
            1f // DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        // Add the volley post request to the request queue
        VolleySingleton.getInstance(this).addToRequestQueue(request)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                var value = volume?.progress?.plus(3) as Int
                volume?.progress = value
                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                var value = volume?.progress?.minus(3) as Int
                volume?.progress = value
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onItemSelected(arg0: AdapterView<*>, arg1: View, position: Int, id: Long) {
        // use position to know the selected item
    }

    override fun onNothingSelected(arg0: AdapterView<*>) {

    }

    fun showIPDialog() {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ip_setting, null)

        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
            .setTitle("Enter IP Address")
        //show dialog
        mDialogView.dialogIP.setText(ip)
        val mAlertDialog = mBuilder.show()
        //login button click of custom layout
        mDialogView.dialogLoginBtn.setOnClickListener {
            mAlertDialog.dismiss()
            ip = mDialogView.dialogIP.text.toString()

        }
        //cancel button click of custom layout
        mDialogView.dialogCancelBtn.setOnClickListener {
            mAlertDialog.dismiss()
        }
    }
}


