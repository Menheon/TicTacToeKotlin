package org.pondar.tictactoegridkotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {


    var turn = 0
    private var fields = IntArray(9)



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //we add clicklisteners, this, to all our fields
        table.setOnClickListener(this)
        field0.setOnClickListener(this)
        field1.setOnClickListener(this)
        field2.setOnClickListener(this)

    }

    override fun onClick(view: View?) {
        // TODO Here you need to get the ID of the view
        // being pressed and then if the view is pressed
        // you need to first put a "X", then next time
        // put a "O" and also make sure that you cannot
        // put a "O" or a "X" if there is already something.

        if (view?.id == R.id.field0) {
            val image = view as ImageView

            Log.d("Field_Clicked", "field 0 pressed")

            //TODO something here

            //An example of how to set the image is shown below
            //you of course need to check if the field is empty
            //before setting a new image
            //and also if the turn is X or O
            if (turn == 0)
                image.setImageResource(R.drawable.kryds)
            else
                image.setImageResource(R.drawable.bolle)
            //then you need to update your field int[] array also to save the state
        }

    } //end of clicklistener
}
