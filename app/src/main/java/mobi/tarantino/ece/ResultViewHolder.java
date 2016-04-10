package mobi.tarantino.ece;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.Map;

import mobi.tarantino.ece.api.projectoxford.RecognizeResponse;

/**
 * Created by kolipass on 11.04.16.
 */
class ResultViewHolder extends RecyclerView.ViewHolder {
    TextView textView;

    public ResultViewHolder(View itemView) {
        super(itemView);
        textView = (TextView) itemView.findViewById(android.R.id.text1);
    }

    public void set(Map.Entry<RecognizeResponse.Scores, Double> entry) {
        textView.setText(entry.getKey() + ": " + entry.getValue());
    }
}
