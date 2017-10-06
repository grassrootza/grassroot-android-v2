package za.org.grassroot2.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import za.org.grassroot2.R;
import za.org.grassroot2.model.enums.GrassrootEntityType;
import za.org.grassroot2.model.task.Meeting;
import za.org.grassroot2.model.task.Task;
import za.org.grassroot2.model.task.Todo;
import za.org.grassroot2.model.task.Vote;
import za.org.grassroot2.rxbinding.RxView;
import za.org.grassroot2.util.LastModifiedFormatter;

public class GroupTasksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_TODO_NEW    = 1;
    private static final int VIEW_TYPE_VOTE_NEW    = 2;
    private static final int VIEW_TYPE_MEETING_NEW = 3;
    private static final int VIEW_TYPE_HEADER      = 4;
    private static final int VIEW_TYPE_OLD_POST    = 5;
    private final long       olderTimestamp;
    private       List<Task> data;
    private final Context    context;

    private PublishSubject<String> viewClickSubject = PublishSubject.create();

    public Observable<String> getViewClickObservable() {
        return viewClickSubject;
    }

    public GroupTasksAdapter(Context c, List<Task> data, long oldPostsCutOff) {
        super();
        context = c;
        olderTimestamp = oldPostsCutOff;
        this.data = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder v;
        if (viewType == VIEW_TYPE_HEADER) {
            v = new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_header, parent, false));
        } else if (viewType == VIEW_TYPE_TODO_NEW) {
            v = new TodoViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_generic, parent, false));
        } else if (viewType == VIEW_TYPE_MEETING_NEW) {
            v = new MeetingViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_generic, parent, false));
        } else if (viewType == VIEW_TYPE_OLD_POST) {
            v = new OldPostViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_older, parent, false));
        } else {
            v = new VoteViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vote, parent, false));

        }
        return v;
    }

    @Override
    public int getItemViewType(int position) {
        Task task = data.get(position);
        GrassrootEntityType type = task.getType();
        if (type == GrassrootEntityType.GROUP) {
            return VIEW_TYPE_HEADER;
        }
        if (task.getDeadlineMillis() <= olderTimestamp) {
            return VIEW_TYPE_OLD_POST;
        } else {
            if (type == GrassrootEntityType.MEETING) {
                return VIEW_TYPE_MEETING_NEW;
            } else if (type == GrassrootEntityType.TODO) {
                return VIEW_TYPE_TODO_NEW;
            } else {
                return VIEW_TYPE_VOTE_NEW;
            }
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Task item = data.get(position);
        int type = getItemViewType(position);
        if (type == VIEW_TYPE_HEADER) {
            bindHeader((HeaderViewHolder) holder, (HeaderItem) item);
        } else if (type == VIEW_TYPE_TODO_NEW) {
            bindTodo((TodoViewHolder) holder, (Todo) item);
        } else if (type == VIEW_TYPE_VOTE_NEW) {
            bindVote((VoteViewHolder) holder, (Vote) item);
        } else if (type == VIEW_TYPE_OLD_POST) {
            bindOldPost((OldPostViewHolder) holder, item);
        } else {
            bindMeeting((MeetingViewHolder) holder, (Meeting) item);
        }
    }

    private void bindOldPost(OldPostViewHolder holder, Task item) {
        holder.title.setText(item.getName());
        holder.lastModified.setText(LastModifiedFormatter.lastSeen(context, item.getDeadlineMillis()));
        holder.subtitle.setText("happend");
    }

    private void bindHeader(HeaderViewHolder holder, HeaderItem item) {
        holder.title.setText(item.getName());
    }

    private void bindMeeting(MeetingViewHolder holder, Meeting item) {
        holder.title.setText(context.getString(R.string.text_meeting_title, item.getCallerName(), item.getName()));
        holder.lastModified.setText(LastModifiedFormatter.lastSeen(context, item.getDeadlineMillis()));
        holder.option1.setText(R.string.text_rsvp);
        holder.option2.setText(R.string.text_view);
        setupClick(holder.root, item.getUid());
    }

    private void bindVote(VoteViewHolder holder, Vote item) {
        holder.lastModified.setText(LastModifiedFormatter.lastSeen(context, item.getDeadlineMillis()));
        holder.title.setText(context.getString(R.string.text_vote_title, item.getCallerName(), item.getName()));
        renderOptions(item.getVoteOptions(), holder);
        setupClick(holder.root, item.getUid());
    }

    private void renderOptions(HashMap<String, Integer> options, VoteViewHolder holder) {
        if (holder.voteOptionsContainer.getChildCount() > 1) {
            holder.voteOptionsContainer.removeViews(0, holder.voteOptionsContainer.getChildCount() - 1);
        }
        int total = 0;
        for (Integer i : options.values()) {
            total += i;
        }
        int viewPostition = 0;
        for (Map.Entry<String, Integer> e : options.entrySet()) {
            View option = LayoutInflater.from(context).inflate(R.layout.vote_option, holder.voteOptionsContainer, false);
            ProgressBar progress = (ProgressBar) option.findViewById(R.id.progress);
            progress.setMax(total);
            progress.setProgress(e.getValue());
            TextView title = (TextView) option.findViewById(R.id.title);
            title.setText(e.getKey());
            TextView count = (TextView) option.findViewById(R.id.count);
            count.setText(context.getResources().getQuantityString(R.plurals.vote_count, e.getValue(), e.getValue()));
            holder.voteOptionsContainer.addView(option, viewPostition++);
        }
    }

    private void bindTodo(TodoViewHolder holder, Todo item) {
        holder.title.setText(context.getString(R.string.text_todo_title, item.getRecorderName(), item.getName()));
        holder.lastModified.setText(LastModifiedFormatter.lastSeen(context, item.getDeadlineMillis()));
        setupClick(holder.root, item.getUid());
    }

    private void setupClick(View view, String uid) {
        RxView.clicks(view)
                .map(o -> uid)
                .subscribe(viewClickSubject);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<Task> tasks) {
        int index = -1;
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            if (t.getDeadlineMillis() <= olderTimestamp) {
                index = i;
                break;
            }
        }
        if (index == 0) {
            tasks.add(index, new HeaderItem(context.getString(R.string.text_old_header)));
        } else {
            if (index > 0) {
                tasks.add(index, new HeaderItem(context.getString(R.string.text_old_header)));
            }
            tasks.add(0, new HeaderItem(context.getString(R.string.text_group_header)));
        }
        data = tasks;
        notifyDataSetChanged();
    }

    static class VoteViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.root)                 View         root;
        @BindView(R.id.image)                ImageView    image;
        @BindView(R.id.title)                TextView     title;
        @BindView(R.id.modified)             TextView     lastModified;
        @BindView(R.id.voteButton)           TextView     vote;
        @BindView(R.id.voteTotalCount)       TextView     voteTotalCount;
        @BindView(R.id.voteOptionsContainer) LinearLayout voteOptionsContainer;

        private VoteViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class OldPostViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.root)     View      root;
        @BindView(R.id.image)    ImageView image;
        @BindView(R.id.title)    TextView  title;
        @BindView(R.id.subtitle) TextView  subtitle;
        @BindView(R.id.modified) TextView  lastModified;

        private OldPostViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class MeetingViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.root)     View      root;
        @BindView(R.id.image)    ImageView image;
        @BindView(R.id.title)    TextView  title;
        @BindView(R.id.modified) TextView  lastModified;
        @BindView(R.id.option1)  TextView  option1;
        @BindView(R.id.option2)  TextView  option2;

        private MeetingViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class TodoViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.root)     View      root;
        @BindView(R.id.image)    ImageView image;
        @BindView(R.id.title)    TextView  title;
        @BindView(R.id.modified) TextView  lastModified;
        @BindView(R.id.option1)  TextView  option1;
        @BindView(R.id.option2)  TextView  option2;

        public TodoViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.title) TextView title;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
