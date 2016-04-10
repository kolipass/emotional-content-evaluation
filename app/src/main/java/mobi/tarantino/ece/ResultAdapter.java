package mobi.tarantino.ece;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import mobi.tarantino.ece.api.projectoxford.RecognizeResponse;

/**
 * Created by kolipass on 11.04.16.
 */
public class ResultAdapter extends RecyclerView.Adapter<ResultViewHolder> {
    private List<Map.Entry<RecognizeResponse.Scores, Double>> entries = new ArrayList<>();

    @Override
    public ResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ResultViewHolder(
                inflateView(parent, android.R.layout.simple_list_item_1));
    }

    @Override
    public void onBindViewHolder(ResultViewHolder holder, int position) {
        holder.set(entries.get(position));
    }

    private View inflateView(ViewGroup viewGroup, int resId) {
        return LayoutInflater.from(viewGroup.getContext()).inflate(resId, viewGroup, false);
    }

    public void setValues(RecognizeResponse values) {
        entries.clear();

        entries.addAll(values.getScores().entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<RecognizeResponse.Scores, Double>>() {
            @Override
            public int compare(Map.Entry<RecognizeResponse.Scores, Double> lhs, Map.Entry<RecognizeResponse.Scores, Double> rhs) {
                return lhs.getValue().compareTo(rhs.getValue());
            }
        });

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

}
