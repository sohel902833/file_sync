package com.example.filesync.Adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filesync.Models.ApprovedFolder;
import com.example.filesync.R;

import java.text.MessageFormat;
import java.util.List;

public class ApprovedFileListAdapter extends RecyclerView.Adapter<ApprovedFileListAdapter.MyViewHolder>{

    private Context context;
    private List<ApprovedFolder> dataList;
    private  OnItemClickListner listner;

    public ApprovedFileListAdapter(Context context, List<ApprovedFolder> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(context).inflate(R.layout.permitted_folder_list_item,parent,false);
        return new MyViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ApprovedFolder item=dataList.get(position);
        holder.folderNameTv.setText(item.getFolderName());
        holder.uploadedFilesTv.setText(MessageFormat.format("Uploaded {0}/{1}", item.getUploadedFiles(), item.getTotalFiles()));



//
//        ApiRef.departmentRef.child(item.getDepartmentId()).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if(dataSnapshot.exists()){
//                    Department department=dataSnapshot.getValue(Department.class);
//                    holder.departmentTv.setText(""+department.getDepartmentName());
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//
//        holder.groupTv.setText("Group:"+item.getGroup());
//        holder.shiftTv.setText("Shift: "+item.getShift());
//        holder.semesterTv.setText("Sem: "+item.getSemester());
//        holder.sessionTv.setText("Session: "+item.getSession());
//        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(listner!=null){
//                    listner.onDelete(holder.getAdapterPosition(),item);
//                }
//            }
//        });
//        holder.editButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(listner!=null){
//                    listner.onEdit(holder.getAdapterPosition(),item);
//                }
//            }
//        });



    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class MyViewHolder extends  RecyclerView.ViewHolder implements View.OnClickListener{
        TextView folderNameTv,uploadedFilesTv;
        Button removePermissionButton;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            folderNameTv=itemView.findViewById(R.id.pf_folderNameTV);
            uploadedFilesTv=itemView.findViewById(R.id.pf_uploadedFilesTV);
            removePermissionButton=itemView.findViewById(R.id.pf_rmvPermission);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(listner!=null){
                int position=getAdapterPosition();
                if(position!= RecyclerView.NO_POSITION){
                    listner.onItemClick(position);
                }
            }
        }

    }
    public interface  OnItemClickListner{
        void onItemClick(int position);
        void onEdit(int position,ApprovedFolder batch);
    }

    public void setOnItemClickListner(OnItemClickListner listner){
        this.listner=listner;
    }


}
