package com.jash.myutils.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class CommonAdapter<D,VH extends CommonAdapter.ViewHolder> extends BaseAdapter {
    private Context context;
    private List<D> list;
    private int[] layoutIds;
    private LayoutInflater inflater;

    public CommonAdapter(Context context, @NonNull List<D> list, int[] layoutIds) {
        this.context = context;
        this.list = list;
        this.layoutIds = layoutIds;
        inflater = LayoutInflater.from(context);
    }

    public CommonAdapter(Context context, int layoutId) {
        this(context, new ArrayList<D>(), new int[]{layoutId});
    }

    public CommonAdapter(Context context, int[] layoutIds) {
        this(context, new ArrayList<D>(), layoutIds);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public D getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return layoutIds.length;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(layoutIds[getItemViewType(position)], parent, false);
            Class type = (Class) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
            try {
                Constructor constructor = type.getConstructor(View.class);
                Object o = constructor.newInstance(convertView);
                convertView.setTag(o);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        VH holder = (VH) convertView.getTag();
        D data = list.get(position);
        onBindView(holder, data);
        return convertView;
    }

    public void add(D data) {
        list.add(data);
        notifyDataSetChanged();
    }
    public void add(int index, D data) {
        list.add(index, data);
        notifyDataSetChanged();
    }

    public void remove(D data) {
        list.remove(data);
        notifyDataSetChanged();
    }

    public void remove(int index) {
        list.remove(index);
        notifyDataSetChanged();
    }
    public void addAll(Collection<? extends D> collection) {
        list.addAll(collection);
        notifyDataSetChanged();
    }
    public void clear() {
        list.clear();
        notifyDataSetChanged();
    }
    public abstract void onBindView(VH holder, D data);

    public static class ViewHolder {
        private View itemView;

        public ViewHolder(View itemView) {
            this.itemView = itemView;
        }
    }
}
