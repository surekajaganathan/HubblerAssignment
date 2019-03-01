package com.example.pravin.hubblerassignment;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;

public class ReportListAdapter extends RecyclerView.Adapter<ReportListAdapter.ReportListHolder> {


    private ArrayList<DefaultModelClass> reportList;
    Context context;

    public ReportListAdapter(ArrayList<DefaultModelClass> reportList, Context context) {
        this.reportList = reportList;
        this.context = context;
        ;
    }

    @NonNull
    @Override
    public ReportListHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.row_report_list, viewGroup, false);
        return new ReportListHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportListHolder reportListHolder, int i) {
        DefaultModelClass modelClass = reportList.get(i);
        reportListHolder.tvLabel1.setText(modelClass.getKey1());
        reportListHolder.tvValue1.setText(modelClass.getValue1());

        String key2 = modelClass.getKey2();
        if(key2!=null && !key2.isEmpty()){
            reportListHolder.tableRow2.setVisibility(View.VISIBLE);
            reportListHolder.tvLabel2.setText(modelClass.getKey2());
            reportListHolder.tvValue2.setText(modelClass.getValue2());
        }else{
            reportListHolder.tableRow2.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public class ReportListHolder extends RecyclerView.ViewHolder {

        public TextView tvLabel1, tvLabel2, tvValue1, tvValue2;
        public TableRow tableRow2;

        public ReportListHolder(View itemView) {
            super(itemView);

            tvLabel1 = itemView.findViewById(R.id.tv_label_1);
            tvLabel2 = itemView.findViewById(R.id.tv_label_2);
            tvValue1 = itemView.findViewById(R.id.tv_value_1);
            tvValue2 = itemView.findViewById(R.id.tv_value_2);
            tableRow2 = itemView.findViewById(R.id.tr_2);
        }
    }
}
