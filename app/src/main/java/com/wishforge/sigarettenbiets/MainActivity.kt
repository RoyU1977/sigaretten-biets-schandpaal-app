package com.wishforge.sigarettenbiets

import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private val colleagues = listOf("Sam", "Noor", "Milan", "Sophie", "Daan")
    private val verdicts = listOf(
        "DENIED. BUY YOUR OWN.",
        "APPROVED. YOU PATHETIC LEECH.",
        "NO. WALLET FIRST.",
        "APPROVED. SHAME +1.",
        "DENIED. FREERIDER DETECTED."
    )

    private lateinit var prefs: SharedPreferences
    private lateinit var verdictText: TextView
    private val dayCounterViews = mutableMapOf<String, TextView>()
    private val totalCounterViews = mutableMapOf<String, TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("sigaretten_biets_local_store", MODE_PRIVATE)

        val rootScroll = ScrollView(this)
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }

        verdictText = TextView(this).apply {
            text = "READY. PICK A COLLEAGUE."
            gravity = Gravity.CENTER
            setTypeface(typeface, Typeface.BOLD)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f)
            setPadding(0, 0, 0, dp(16))
        }
        rootLayout.addView(verdictText)

        colleagues.forEach { colleague ->
            rootLayout.addView(createColleagueBlock(colleague))
        }

        val resetAllDayButton = Button(this).apply {
            text = "Reset all day counters"
            setOnClickListener {
                colleagues.forEach { colleague -> setDayCount(colleague, 0) }
                refreshAllCounters()
                verdictText.text = "DAY RESET COMPLETE."
            }
        }
        rootLayout.addView(resetAllDayButton)

        rootScroll.addView(rootLayout)
        setContentView(rootScroll)
        refreshAllCounters()
    }

    private fun createColleagueBlock(colleague: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(12)
            }

            addView(TextView(context).apply {
                text = colleague
                setTypeface(typeface, Typeface.BOLD)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            })

            val dayText = TextView(context)
            val totalText = TextView(context)
            dayCounterViews[colleague] = dayText
            totalCounterViews[colleague] = totalText

            addView(dayText)
            addView(totalText)

            addView(Button(context).apply {
                text = "Register cigarette request"
                setOnClickListener {
                    setDayCount(colleague, getDayCount(colleague) + 1)
                    setTotalCount(colleague, getTotalCount(colleague) + 1)
                    refreshCountersFor(colleague)
                    verdictText.text = verdicts[Random.nextInt(verdicts.size)]
                }
            })

            addView(Button(context).apply {
                text = "Reset day counter"
                setOnClickListener {
                    setDayCount(colleague, 0)
                    refreshCountersFor(colleague)
                    verdictText.text = "DAY COUNTER RESET."
                }
            })
        }
    }

    private fun refreshAllCounters() {
        colleagues.forEach { refreshCountersFor(it) }
    }

    private fun refreshCountersFor(colleague: String) {
        dayCounterViews[colleague]?.text = "Day requests: ${getDayCount(colleague)}"
        totalCounterViews[colleague]?.text = "Total requests: ${getTotalCount(colleague)}"
    }

    private fun getDayCount(colleague: String): Int = prefs.getInt("day_$colleague", 0)

    private fun getTotalCount(colleague: String): Int = prefs.getInt("total_$colleague", 0)

    private fun setDayCount(colleague: String, value: Int) {
        prefs.edit().putInt("day_$colleague", value).apply()
    }

    private fun setTotalCount(colleague: String, value: Int) {
        prefs.edit().putInt("total_$colleague", value).apply()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
