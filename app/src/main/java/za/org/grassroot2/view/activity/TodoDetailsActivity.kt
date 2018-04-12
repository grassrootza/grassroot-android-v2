package za.org.grassroot2.view.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.Menu
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_todo_details.*
import timber.log.Timber
import za.org.grassroot2.R
import za.org.grassroot2.R.id.todoTextBox
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.model.Post
import za.org.grassroot2.model.task.Todo
import za.org.grassroot2.model.task.TodoResponse
import za.org.grassroot2.presenter.activity.TodoDetailsPresenter
import za.org.grassroot2.util.DateFormatter
import za.org.grassroot2.view.adapter.PostAdapter
import za.org.grassroot2.view.adapter.TodoResponseAdapter
import za.org.grassroot2.view.dialog.OptionPickDialog
import javax.inject.Inject


class TodoDetailsActivity : GrassrootActivity(), TodoDetailsPresenter.TodoDetailsView {

    private var todoUid: String? = null

    @Inject lateinit var presenter: TodoDetailsPresenter
    @Inject lateinit var rxPermissions: RxPermissions
    @Inject lateinit var postAdapter: PostAdapter
    @Inject lateinit var resultsAdapter: TodoResponseAdapter

    //var mEdit: EditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val triggeredByNotification = intent.getBooleanExtra(TRIGGERED_BY_NOTIFICATION, false)
        todoUid = intent.getStringExtra(EXTRA_TODO_UID)
        initView()
        presenter.attach(this)
        presenter.init(todoUid!!, true) // todo: switch this back to triggerByNotification when done debugging
        todoStatusText.setOnClickListener {
            val attendenceDialog = OptionPickDialog.attendenceChoiceDialog()
            disposables.add(attendenceDialog.clickAction().subscribe( { clickId ->
                attendenceDialog.dismiss()
                when (clickId) {
                    // Do some stuff
                    R.id.optionA -> presenter.respondToTodo(todoUid!!, Todo.TODO_YES)
                    R.id.optionB -> presenter.respondToTodo(todoUid!!, Todo.TODO_NO)
                    R.id.todoTextBoxButton -> presenter.respondToTodo(todoUid!!, Todo.TODO_INFO)
                }
            }, {t -> t.printStackTrace() }))
            attendenceDialog.show(supportFragmentManager, "")
        }
        fab.setOnClickListener { writePost() }
        writePostButton.setOnClickListener { writePost() }
        posts.adapter = postAdapter
        todo_results.adapter = resultsAdapter
        todo_results.layoutManager = LinearLayoutManager(this)
        posts.layoutManager = LinearLayoutManager(this)
    }

    private fun writePost() {
        CreatePostActivity.start(this, presenter.todo.uid, presenter.todo.parentUid)
    }

    override fun onResume() {
        super.onResume()
        Timber.d("inside activity, telling presenter to load data");
        presenter.loadData()
    }

    override fun onInject(component: ActivityComponent) {
        component.inject(this)
    }

    override fun getLayoutResourceId(): Int = R.layout.activity_todo_details

    override fun onDestroy() {
        super.onDestroy()
        presenter.detach()
    }

    private fun initView() {
        initToolbar()
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        toolbar.title = ""
        toolbar.setNavigationIcon(R.drawable.ic_arrow_left_white_24dp)
        toolbar.setNavigationOnClickListener { v -> finish() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_group_details, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun render(todo: Todo) {
        todoTitle.text = todo.name
        // Timber.d("The contents of todo are: %s", todo.toString())
        // Timber.d("The contents of todo.toString() are: %s", todo.toString())
        if (todo.todoType == "VOLUNTEERS_NEEDED") {
            theBigQuestion.visibility = View.VISIBLE
            optionA.visibility = View.VISIBLE
            optionB.visibility = View.VISIBLE
            theBigQuestion.text = "Will You Volunteer?"
        }
        else if (todo.todoType == "VALIDATION_REQUIRED") {
            theBigQuestion.visibility = View.VISIBLE
            optionA.visibility = View.VISIBLE
            optionB.visibility = View.VISIBLE
            theBigQuestion.text = "Is The Action Complete?"
        }
        else if (todo.todoType == "INFORMATION_REQUIRED") {
            todoTextBoxButton.visibility = View.VISIBLE
            todoTextBox.visibility = View.VISIBLE
        }
        else if (todo.todoType == "ACTION_REQUIRED") {
            // This option is informative so display no additional info
        }
        todo.deadlineMillis?.let { todoDate.text = DateFormatter.formatMeetingDate(it) }
        renderDescription(todo)
        renderResponseSection(todo)
    }

    private fun renderResponseSection(todo: Todo) {
        // Here we display available options.
        if (todo.hasResponded()) {
            if (todo.todoType != "INFORMATION_REQUIRED" && todo.todoType != "ACTION_REQUIRED") {
                todoStatusText.visibility = View.VISIBLE
                when (todo.response) {
                    Todo.TODO_YES -> {
                        todoStatusText.text = "YES"
                        todoStatusText.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_attend, 0, 0)
                    }
                    Todo.TODO_NO -> {
                        todoStatusText.text = "NO"
                        todoStatusText.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_not_attending, 0, 0)
                    }
                }
            }
            else if (todo.todoType == "INFORMATION_REQUIRED") {
                todoStatusText.text = "You Have Responded"
                todoTextBox.visibility = View.GONE

            }
            optionContainer.visibility = View.GONE
        } else {
            if (todo.todoType != "INFORMATION_REQUIRED" && todo.todoType != "ACTION_REQUIRED") {
                optionA.setOnClickListener({ _ -> presenter.respondToTodo(todo.uid, Todo.TODO_YES) })
                optionB.setOnClickListener({ _ -> presenter.respondToTodo(todo.uid, Todo.TODO_NO) })
            }
            else if (todo.todoType == "INFORMATION_REQUIRED") {
                var mEdit = findViewById(R.id.todoTextBox) as EditText
                Todo.TODO_INFO = mEdit.text.toString()
                todoTextBoxButton.setOnClickListener({ _ -> presenter.respondToTodo(todo.uid, Todo.TODO_INFO+mEdit.text.toString())})
            }
        }
    }

    private fun renderDescription(todo: Todo) {
        if (TextUtils.isEmpty(todo.description)) {
            todoDescription.visibility = View.GONE
        } else {
            todoDescription.visibility = View.VISIBLE
            todoDescription.text = todo.description
        }
    }

    override fun renderResponses(todoResponses: Map<String, String>) {
        resultsAdapter.setData(convertTodoResponses(todoResponses));
    }

    private fun convertTodoResponses(todoResponse: Map<String, String>): List<TodoResponse> {
        if (todoResponse == null) {
            val list = listOf<TodoResponse>()
            return list
        } else {
            return todoResponse.map { entry -> TodoResponse(entry.key, entry.value) }
        }
    }

    /*override fun renderPosts(posts: List<Post>) {
        if (posts.isNotEmpty()) {
            listTitle.visibility = View.VISIBLE
            postAdapter.setData(posts)
        }
    }*/

    companion object {

        val EXTRA_TODO_UID = "todo_uid"
        val TRIGGERED_BY_NOTIFICATION = "triggered_by_notification"

        fun start(activity: Activity, todoUid: String) {
            val intent = Intent(activity, TodoDetailsActivity::class.java)
            intent.putExtra(EXTRA_TODO_UID, todoUid)
            activity.startActivity(intent)
        }
    }
}

