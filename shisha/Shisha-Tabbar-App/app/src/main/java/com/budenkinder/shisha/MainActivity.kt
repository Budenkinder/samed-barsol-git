package com.budenkinder.shisha

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.budenkinder.shisha.data.Item
import com.budenkinder.shisha.data.ShishaData
import com.budenkinder.shisha.ui.main.SectionsPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private var currentVersion: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        onFirebaseConfig()
    }

    private fun onFirebaseConfig() {
        val databaseReference = FirebaseDatabase.getInstance().reference

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    this@MainActivity,
                    "Database error: $databaseError.message", Toast.LENGTH_SHORT
                ).show()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val newSnapshotHashMap = dataSnapshot.children.iterator().next().value

                val keys = (newSnapshotHashMap as HashMap<*, *>).keys

                var cversion: Long = 0
                var ckey = ""
                for (key in keys) {
                    val k = key as String
                    val v = (newSnapshotHashMap[k] as HashMap<*, *>)["version"] as Long
                    if (v > cversion) {
                        cversion = v
                        ckey = k
                    }
                }

                if (currentVersion >= cversion) {
                    return
                }

                currentVersion = cversion

                val dao = ShishaData()

                val tabbs = (newSnapshotHashMap[ckey] as HashMap<*, *>)["tabs"] as ArrayList<*>
                dao.version = currentVersion

                val tabbsArrayList = ArrayList<ArrayList<Item>>()
                for (tab in tabbs) {
                    val tabItemAsArrayList = ArrayList<Item>()
                    tabItemAsArrayList.clear()
                    val tabItems = tab as ArrayList<*> // * => HashMap
                    for (tabItem in tabItems) {
                        //converting hashmap to Item
                        val item = Item()
                        item.block = (tabItem as HashMap<*, *>)["block"] as Boolean
                        item.description = (tabItem as HashMap<*, *>)["description"] as String
                        item.name = (tabItem as HashMap<*, *>)["name"] as String
                        item.price = (tabItem as HashMap<*, *>)["price"] as String
                        item.header = (tabItem as HashMap<*, *>)["header"] as Boolean
                        item.image_url = (tabItem as HashMap<*, *>)["image_url"] as String
                        item.title1 = (tabItem as HashMap<*, *>)["title1"] as String
                        item.tab_title = (tabItem as HashMap<*, *>)["tab_title"] as String
                        tabItemAsArrayList.add(item)
                    }
                    tabbsArrayList.add(tabItemAsArrayList)
                }

                dao.tabs = tabbsArrayList


                val sectionsPagerAdapter =
                    SectionsPagerAdapter(dao, supportFragmentManager)
                val viewPager: ViewPager = findViewById(R.id.view_pager)
                viewPager.adapter = sectionsPagerAdapter
                viewPager.offscreenPageLimit = 10
                val tabs: TabLayout = findViewById(R.id.tabs)
                tabs.tabMode = TabLayout.MODE_SCROLLABLE
                tabs.setupWithViewPager(viewPager)
            }
        })
    }
}