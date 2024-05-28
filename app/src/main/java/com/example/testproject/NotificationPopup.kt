import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.testproject.DBstudent
import com.example.testproject.R

class NotificationPopup(context: Context, var onYesClick: (() -> Unit)? = null) : Dialog(context) {

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.continue_layout, null)
        setContentView(view)

        val button_yes: Button = findViewById(R.id.yes)
        val button_no: Button = findViewById(R.id.no)

        button_yes.setOnClickListener {
            onYesClick?.invoke()
            dismiss()
        }

        button_no.setOnClickListener{
            dismiss()
        }
    }

    override fun show() {
        super.show()
    }
}
    
