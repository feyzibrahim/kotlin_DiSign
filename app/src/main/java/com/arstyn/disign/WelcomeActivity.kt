package com.arstyn.disign

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.arstyn.disign.adapters.ViewPagerAdapter
import kotlinx.android.synthetic.main.activity_welcome.*

class WelcomeActivity : AppCompatActivity() {
    private var textList = mutableListOf(
        "Tired of Signing In Paper?",
        "It is so easy. Just scan\nA QR Code",
        "Confused to find the Scanner?",
        "How to Create a QR Code?",
        "Can I see my visiting history?"
    )
    private var imageList = mutableListOf(
        R.drawable.ic__signing_in_paper,
        R.drawable.ic_qrcode,
        R.drawable.ic_scanner__2,
        R.drawable.ic_new_event,
        R.drawable.ic_navigation_drawer,
    )
    private var subTextList = mutableListOf(
        "We know how today works, right? To fight the pandemic we have to register in every place we visit with our name and phone number. " +
                "Isn't it little annoying? DiSign can help you get ride of signing in every place you visit. Swipe left to know how this app works ->",
        "As it said only a scanning is required. Scan the QR code in the places where DiSign app is available. You know what you can help others too by " +
                "informing about this app. Where DiSign app is not available.",
        "Just press the yellow button in the bottom-right corner. It has scanner icon in it. It will open up the scanner and just show it to the QR code " +
                "on the wall.",
        "If you are shop owner or organizing an event you can create a QR Code by pressing the NEW button on the top-right of main page. Enter the details " +
                "in the form and click on the create button. After that you can see the event in the home page. You can get the details of people visited" +
                " daily in your shop or event.",
        "Yes just swipe right and open the navigation drawer and you can see a button for Visit History. Just press it and you can see your visiting history" +
                " in chronological order."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        viewPagerWelcome.adapter = ViewPagerAdapter(textList, imageList, subTextList)
        pageIndicatorView.setViewPager(viewPagerWelcome)

        skipButtonInWelcome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}
