package net.avene.twitter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class StatusViewHolder {
	
	private View view;
	private TextView name;
	private TextView screenName;
	private TextView createdAt;
	private TextView body;
	private ImageView icon;

	public StatusViewHolder(View view){
		this.view = view;
	}

	public TextView getName() {
		if(name == null){
			name = (TextView)view.findViewById(R.id.status_name);
		}
		return name;
	}

	public TextView getScreenName() {
		if(screenName == null){
			screenName = (TextView)view.findViewById(R.id.status_screen_name);
		}
		return screenName;
	}

	public TextView getCreatedAt() {
		if(createdAt == null){
			createdAt = (TextView)view.findViewById(R.id.status_created_at);
		}
		return createdAt;
	}

	public TextView getBody() {
		if(body == null){
			body = (TextView)view.findViewById(R.id.status_body);
		}
		return body;
	}
	
	public ImageView getIcon(){
		if(icon == null){
			icon = (ImageView)view.findViewById(R.id.status_icon);
		}
		return icon;
	}

}
