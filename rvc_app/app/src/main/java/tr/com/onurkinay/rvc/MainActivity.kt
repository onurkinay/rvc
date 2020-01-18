package tr.com.onurkinay.rvc

import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    var volume: SeekBar? = null
    var left: Button? = null
    var right: Button? = null
    val ip = "192.168.1.8:8080" // your computer IP and port
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        volume = findViewById(R.id.volume)
        left = findViewById(R.id.left)
        right = findViewById(R.id.right)


        val queue = Volley.newRequestQueue(this)
        val url = "http://$ip/"
        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                val stringRequest = StringRequest(Request.Method.GET, url,
                    Response.Listener<String> { response ->
                        println(response)
                        volume?.progress = response.toInt()
                    },
                    Response.ErrorListener { /* any error */})

                queue.add(stringRequest)
                handler.postDelayed(this, 1000) // 1 sec delay
            }
        }, 0)

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
            Snackbar.make(view, "ben@onurkinay.com.tr -> having any problem, write me :)", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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
}
