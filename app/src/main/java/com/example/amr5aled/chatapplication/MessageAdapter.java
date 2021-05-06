package com.example.amr5aled.chatapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MessageAdapter extends BaseAdapter {
    Context context;
    public static String last_message_statues = "send";

    public MessageAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return MessageActivity.list_message.size();
    }

    @Override
    public Object getItem(int position) {
        return MessageActivity.list_message.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = null;

        if (MessageActivity.list_message.get(position).getBody().contains("fileuploadingtoyourfiendthismessagenotify")) {

            view = layoutInflater.inflate(R.layout.file_uploaded_item, null);
            TextView textView = (TextView) view.findViewById(R.id.file_upload);
            if (!MessageActivity.list_message.get(position).getSender().equals(context.getSharedPreferences("logged", context.MODE_PRIVATE).getString("phone", "-1"))) {
                textView.setText(R.string.friend_uploading);
            } else if (MessageActivity.list_message.get(position).getSender().equalsIgnoreCase(context.getSharedPreferences("logged", context.MODE_PRIVATE).getString("phone", "-1"))) {
                textView.setText(R.string.uploading);
            }
        } else if (MessageActivity.list_message.get(position).getBody().contains("fileuploadedtoyourfiendthismessagenotify")) {
            view = layoutInflater.inflate(R.layout.file_uploaded_item, null);
            TextView textView = (TextView) view.findViewById(R.id.file_upload);
            textView.setEnabled(true);
            textView.setText(R.string.download);
        } else {
            if (MessageActivity.list_message.get(position).getSender().
                    equals(context.getSharedPreferences("logged", context.MODE_PRIVATE).getString("phone", "-1")))
                view = layoutInflater.inflate(R.layout.message_send, null);
            else
                view = layoutInflater.inflate(R.layout.message_recieve, null);

            TextView message_body = (TextView) view.findViewById(R.id.message_body);
            message_body.setText(MessageActivity.list_message.get(position).getBody());
            ImageView imageView = (ImageView) view.findViewById(R.id.status);

            if (position == MessageActivity.list_message.size() - 1 && MessageActivity.list_message.get(position).getSender().
                    equalsIgnoreCase(context.getSharedPreferences("logged", context.MODE_PRIVATE).getString("phone", "-1"))) {
                if (last_message_statues.equalsIgnoreCase("send"))
                    imageView.setImageResource(R.drawable.send);
                else if (last_message_statues.equalsIgnoreCase("rec"))
                    imageView.setImageResource(R.drawable.rec);
                else if (last_message_statues.equalsIgnoreCase("seen"))
                    imageView.setImageResource(R.drawable.seen);
            }
        }

        return view;
    }
}