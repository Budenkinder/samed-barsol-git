package com.budenkinder.shisha.ui.main

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.budenkinder.shisha.R
import com.budenkinder.shisha.data.Item
import com.budenkinder.shisha.data.ShishaData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.IOException


/**
 * A placeholder fragment containing a simple view.
 */
class MenuFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var activity: AppCompatActivity
    private var tabPos: Int? = 0
    private lateinit var shishaData: ShishaData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tabPos = arguments?.getInt("position")
        shishaData = arguments?.getSerializable("dao") as ShishaData
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        context.let {
            activity = context as AppCompatActivity
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_main, container, false)

        val isAuthenticated = !FirebaseAuth.getInstance().currentUser?.uid.isNullOrEmpty()

        val saveToDbButton = root.findViewById<Button>(R.id.save_to_db)
        saveToDbButton?.setOnClickListener() {

            FirebaseDatabase.getInstance().reference.child("shisha_bar").push()
                .setValue(shishaData)
        }

        if (isAuthenticated) {
            saveToDbButton.visibility = View.VISIBLE
        } else {
            saveToDbButton.visibility = View.GONE
        }

        recyclerView = root.findViewById(R.id.recyclerView)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = MyAdapter(activity, shishaData, tabPos, isAuthenticated)
        }

        return root
    }

    class MyAdapter(
        private val activity: AppCompatActivity,
        private val shishaData: ShishaData, private val tabPos: Int?,
        private val isAuthenticated: Boolean
    ) :
        RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

        class MyViewHolder(val rootView: FrameLayout) : RecyclerView.ViewHolder(rootView)

        private lateinit var inputBlock: EditText
        private lateinit var inputPrice: EditText
        private lateinit var inputName: EditText
        private lateinit var inputDescription: EditText

        private lateinit var topBlock: EditText
        private lateinit var topInputPrice: EditText
        private lateinit var topInputName: EditText
        private lateinit var topInputDescription: EditText

        private lateinit var bottomBlock: EditText
        private lateinit var bottomInputPrice: EditText
        private lateinit var bottomInputName: EditText
        private lateinit var bottomInputDescription: EditText

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): MyViewHolder {
            val rootView = LayoutInflater.from(parent.context)
                .inflate(R.layout.menue_item, parent, false) as FrameLayout
            return MyViewHolder(rootView)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

            val titleView = holder.rootView.findViewById<TextView>(R.id.title)
            val nameView = holder.rootView.findViewById<TextView>(R.id.item_name)
            val priceView = holder.rootView.findViewById<TextView>(R.id.item_price)
            val descriptionView = holder.rootView.findViewById<TextView>(R.id.item_description)
            val titleImageView = holder.rootView.findViewById<ImageView>(R.id.title_image)
            val titleImageViewContainer = holder.rootView.findViewById<CardView>(R.id.item)
            val itemSeparator = holder.rootView.findViewById<View>(R.id.separator)
            val itemDescription =
                holder.rootView.findViewById<View>(R.id.item_description_separator)

            val nameAndPriceView =
                holder.rootView.findViewById<FrameLayout>(R.id.name_and_price_container)
            val itemDeleteView = holder.rootView.findViewById<ImageView>(R.id.item_delete)
            itemDeleteView.visibility = View.GONE
            if (isAuthenticated) {
                itemDeleteView.visibility = View.VISIBLE
                itemDeleteView.setOnClickListener {
                    Toast.makeText(itemDeleteView.context, "asdfsdf", Toast.LENGTH_SHORT).show()
                    shishaData.tabs!![tabPos!!].removeAt(position)
                    shishaData.tabs!![tabPos].first().header = true
                    shishaData.version = shishaData.version.inc()
                    notifyDataSetChanged()
                }


                nameAndPriceView.setOnLongClickListener {

                    val alertDialog: AlertDialog? = activity.let {
                        val mDialogView = LayoutInflater.from(it).inflate(R.layout.edit_items, null)

                        val tab = shishaData.tabs!![tabPos!!] as List<Item>
                        val item = Item()
                        item.description = tab[position].description
                        item.name = tab[position].name
                        item.price = tab[position].price

                        // get top views
                        topBlock = mDialogView.findViewById<EditText>(R.id.add_item_block_top)
                        topInputPrice = mDialogView.findViewById<EditText>(R.id.add_item_price_top)
                        topInputName = mDialogView.findViewById<EditText>(R.id.add_item_name_top)
                        topInputDescription =
                            mDialogView.findViewById<EditText>(R.id.add_item_description_top)

                        // get current views
                        inputBlock = mDialogView.findViewById<EditText>(R.id.change_block)
                        inputPrice = mDialogView.findViewById<EditText>(R.id.change_price_current)
                        inputPrice.hint = item.price
                        inputName = mDialogView.findViewById<EditText>(R.id.change_name_current)
                        inputName.hint = item.name
                        inputDescription =
                            mDialogView.findViewById<EditText>(R.id.change_description_current)
                        inputDescription.hint = item.description

                        // get bottom views
                        bottomBlock = mDialogView.findViewById<EditText>(R.id.add_item_block_bottom)
                        bottomInputPrice =
                            mDialogView.findViewById<EditText>(R.id.add_item_price_bottom)
                        bottomInputName =
                            mDialogView.findViewById<EditText>(R.id.add_item_name_bottom)
                        bottomInputDescription =
                            mDialogView.findViewById<EditText>(R.id.add_item_description_bottom)

                        val builder =
                            AlertDialog.Builder(ContextThemeWrapper(it, R.style.AlertDialogCustom))
                        builder.apply {
                            setView(mDialogView)
                            setTitle("==== Produkt bearbeiten ====")
                            setNegativeButton(
                                "Abbrechen"
                            ) { dialog, id ->
                            }
                            setPositiveButton(
                                "Anschauen"
                            ) { dialog, id ->

                                shishaData.version = shishaData.version.inc()

                                // ensure the update of the current selection

                                shishaData.tabs!![tabPos][position].block =
                                    (!TextUtils.isEmpty(inputBlock.text.toString())
                                            && "Ja".compareTo(inputBlock.text.toString().trim().capitalize()) == 0)

                                if (!TextUtils.isEmpty(inputPrice.text.toString())) {
                                    shishaData.tabs!![tabPos][position].price =
                                        inputPrice.text.toString().trim()
                                }

                                if (!TextUtils.isEmpty(inputName.text.toString())) {
                                    shishaData.tabs!![tabPos][position].name =
                                        inputName.text.toString().trim()
                                }

                                if (!TextUtils.isEmpty(inputDescription.text.toString())) {
                                    shishaData.tabs!![tabPos][position].description =
                                        inputDescription.text.toString().trim()
                                }

                                // ensure the update of the item to add above the selection

                                val above = Item()

                                above.block = false

                                if (!TextUtils.isEmpty(topBlock.text.toString())
                                    && "JA".compareTo(topBlock.text.toString().trim().capitalize()) == 0
                                ) {
                                    above.block = true
                                }

                                if (!TextUtils.isEmpty(topInputPrice.text.toString())) {
                                    above.price = topInputPrice.text.toString().trim()
                                }

                                if (!TextUtils.isEmpty(topInputPrice.text.toString())) {
                                    above.price = topInputPrice.text.toString().trim()
                                }

                                if (!TextUtils.isEmpty(topInputName.text.toString())) {
                                    above.name = topInputName.text.toString().trim()
                                }

                                if (!TextUtils.isEmpty(topInputDescription.text.toString())) {
                                    above.description = topInputDescription.text.toString().trim()
                                }

                                above.tab_title = shishaData.tabs!![tabPos][position].tab_title
                                above.title1 = shishaData.tabs!![tabPos][position].title1

                                if (above.name!!.isNotEmpty() && above.price!!.isNotEmpty()) {
                                    // only when name, price and description are not empty
                                    var newPosition = position - 1
                                    if (position == 0) {
                                        newPosition = 0
                                    }

                                    shishaData.tabs!![tabPos].add(newPosition, above)
                                }

                                // ensure the update of the item to add below selection
                                val below = Item()

                                below.block = false

                                if (!TextUtils.isEmpty(bottomBlock.text.toString())
                                    && "JA".compareTo(bottomBlock.text.toString().trim().capitalize()) == 0
                                ) {
                                    below.block = true
                                }

                                if (!TextUtils.isEmpty(bottomInputPrice.text.toString())) {
                                    below.price = bottomInputPrice.text.toString().trim()
                                }

                                if (!TextUtils.isEmpty(bottomInputPrice.text.toString())) {
                                    below.price = bottomInputPrice.text.toString().trim()
                                }

                                if (!TextUtils.isEmpty(bottomInputName.text.toString())) {
                                    below.name = bottomInputName.text.toString().trim()
                                }

                                if (!TextUtils.isEmpty(bottomInputDescription.text.toString())) {
                                    below.description =
                                        bottomInputDescription.text.toString().trim()
                                }

                                below.tab_title = shishaData.tabs!![tabPos][position].tab_title
                                below.title1 = shishaData.tabs!![tabPos][position].title1

                                if (below.name!!.isNotEmpty() && below.price!!.isNotEmpty()) {
                                    shishaData.tabs!![tabPos].add(position + 1, below)
                                }

                                // iterate over the list

                                for ((cnt, listItems) in shishaData.tabs!![tabPos].withIndex()) {
                                    shishaData.tabs!![tabPos][cnt].header = cnt == 0
                                }

                                notifyDataSetChanged()
                            }
                        }
                        // Set other dialog properties
                        // Create the AlertDialog
                        builder.create()
                    }

                    alertDialog?.show()


                    true
                }
            }

            val tab = shishaData.tabs!![tabPos!!] as List<Item>
            val tabTitle = tab[0].tab_title
            val item = Item()
            item.block = tab[position].block
            item.description = tab[position].description
            item.name = tab[position].name
            item.price = tab[position].price
            item.header = tab[position].header
            item.image_url = tab[position].image_url
            item.title1 = tab[position].title1

            item.block.let {
                if (!it!!) {
                    itemSeparator.visibility = View.GONE
                } else {
                    itemSeparator.visibility = View.VISIBLE
                }
            }

            nameView.text = item.name
            priceView.text = item.price + " €"
            if (item.description.isNullOrEmpty()) {
                descriptionView.visibility = View.GONE
                itemDescription.visibility = View.GONE
            } else {
                descriptionView.visibility = View.VISIBLE
                itemDescription.visibility = View.VISIBLE
                descriptionView.text = item.description
            }

            item.header.let {
                if (it) {
                    titleImageViewContainer.visibility = View.VISIBLE
                    titleView.visibility = View.VISIBLE
                    titleImageView.visibility = View.VISIBLE
                    titleView.text = tabTitle
                    titleView.visibility = View.GONE

                    when (tabTitle) {
                        "Softdrinks" -> {
                            titleImageView.setImageResource(R.drawable.softdrinks_1920)
                        }
                        "Säfte" -> {
                            titleImageView.setImageResource(R.drawable.saefte_1920)
                        }
                        "Hotdrinks" -> {
                            titleImageView.setImageResource(R.drawable.hotdrinks_1920)
                        }
                        "Cocktails" -> {
                            titleImageView.setImageResource(R.drawable.cocktails_1920)
                        }
                        "Shakes" -> {
                            titleImageView.setImageResource(R.drawable.shake_1920)
                        }
                        "Food" -> {
                            titleImageView.setImageResource(R.drawable.food_toast_1920)
                        }
                        "Kelloggs" -> {
                            titleImageView.setImageResource(R.drawable.kelloggs_1920)
                        }
                        "Snacks" -> {
                            titleImageView.setImageResource(R.drawable.chips_1920)
                        }
                        "Bonbons" -> {
                            titleImageView.setImageResource(R.drawable.bonbons_1920)
                        }
                        "Hookah" -> {
                            titleImageView.setImageResource(R.drawable.hookah_1920)
                        }

                    }
                } else {
                    titleView.text = ""
                    titleImageViewContainer.visibility = View.GONE
                    titleView.visibility = View.GONE
                    titleImageView.visibility = View.GONE
                }
            }

            //inflate dynamic content
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = shishaData.tabs?.get(tabPos!!)!!.size
    }

    companion object {
        @JvmStatic
        fun newInstance(dao: ShishaData?, position: Int): MenuFragment {
            return MenuFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("dao", dao)
                    putInt("position", position)
                }
            }
        }
    }
}