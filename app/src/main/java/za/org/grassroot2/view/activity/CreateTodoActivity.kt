package za.org.grassroot2.view.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.tbruyelle.rxpermissions2.RxPermissions
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.presenter.activity.CreateActionPresenter
import javax.inject.Inject

class CreateTodoActivity : GrassrootActivity() {

    @Inject
    lateinit var presenter: CreateActionPresenter
    @Inject
    lateinit var rxPermissions: RxPermissions // inspect

    override fun getLayoutResourceId(): Int = R.layout.activity_create_todo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var todoName = findViewById(R.id.et_create_todo_name) as EditText
        var todoDescription = findViewById(R.id.et_create_todo_description) as EditText

        var button = findViewById(R.id.bt_create_group) as Button // TODO: switch back to todo button

        button.setOnClickListener {
            fun onClick(which: Int) {
                var name = todoName.text.toString()
                var description = todoDescription.text.toString()
                presenter.createMeeting(name, description)
            }
        }
    }

    override fun onInject(component: ActivityComponent) {
        component.inject(this)
    }

    companion object {

        fun start(activity: Activity) {
            val intent = Intent(activity, CreateTodoActivity::class.java)
            activity.startActivity(intent)
        }
    }

}