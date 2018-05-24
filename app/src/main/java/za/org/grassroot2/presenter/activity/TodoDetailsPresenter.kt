/**
 * Created by frtnx on 2018/03/19.
 */

package za.org.grassroot2.presenter.activity

import timber.log.Timber
import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.model.Post
import za.org.grassroot2.model.task.Todo
import za.org.grassroot2.model.task.TodoResponse
import za.org.grassroot2.services.NetworkService
import za.org.grassroot2.services.rest.RestResponse
import za.org.grassroot2.view.GrassrootView
import javax.inject.Inject


class TodoDetailsPresenter @Inject
constructor(private val databaseService: DatabaseService, private val networkService: NetworkService) : BasePresenter<TodoDetailsPresenter.TodoDetailsView>() {

    private lateinit var todoUid: String
    private var forceSync = false

    fun init(todoUid: String, forceSync: Boolean) {
        this.todoUid = todoUid
        this.forceSync = forceSync
    }

    lateinit var todo: Todo
        private set


    fun loadData() {
        // Timber.d("inside load data in presenter");
        if (forceSync) {
            Timber.d("Showing progress bar 1 in TodoDetailsPresenter")
            view.showProgressBar()
            val todoUidAndType = mapOf(todoUid to "TODO")
            disposableOnDetach(networkService.getTasksByUids(todoUidAndType)
                    .subscribeOn(io()).observeOn(main())
                    .subscribe(
                            { tasksFull ->
                                view.closeProgressBar()
                                databaseService.storeTasks(tasksFull)
                                // Timber.d("stored task in DB, about to load data");
                                displayData() // data synced with server, display it
                            },
                            { throwable ->
                                view.closeProgressBar()
                                Timber.e(throwable)
                                displayData() // sync failed, display local data anyway
                            }
                    )
            )
        } else displayData()
    }


    private fun displayData() {
        // Timber.d("getting todo from db ...");
        disposableOnDetach(databaseService.load(Todo::class.java, todoUid).subscribeOn(io()).observeOn(main()).subscribe({ todo ->
            // Timber.d("todo from database: %s", todo)
            this.todo = todo
            view.render(this.todo)
        }, { it.printStackTrace() }))

        disposableOnDetach(networkService.fetchTodoResponses(todoUid!!).subscribeOn(io()).observeOn(main()).subscribe({ resource ->
            view.renderResponses(resource!!)
            // Timber.d("The contents of resource are %s", resource)
        }, { it.printStackTrace() }))
    }


    fun respondToTodo(uid: String, response: String) {
        Timber.d("Showing progress bar 2 in TodoDetailsPresenter")
        view.showProgressBar()
        disposableOnDetach(networkService.respondToTodo(uid, response).subscribeOn(io()).observeOn(main()).subscribe({ networkResponse ->
            view.closeProgressBar()
            disposableOnDetach(databaseService.load(Todo::class.javaObjectType, uid).flatMapSingle { vote ->
                // Timber.d("got back altered todo: %s", networkResponse.toString())
                return@flatMapSingle databaseService.store(Todo::class.javaObjectType, networkResponse)
            }.observeOn(main()).subscribe({ t -> view.render(t) }))
        }, {throwable ->
            // todo : handle the various types of errors
            view.closeProgressBar()
        }));
    }

    interface TodoDetailsView : GrassrootView {
        fun render(todo: Todo)
        fun renderResponses(todoResponses: Map<String, String>)
    }
}
