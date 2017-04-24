package net.gusakov.newnettiauto.fragments;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.ListFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import net.gusakov.newnettiauto.Constants;
import net.gusakov.newnettiauto.R;
import net.gusakov.newnettiauto.adapters.AutoCursorAdapter;
import net.gusakov.newnettiauto.classes.Auto;


import timber.log.Timber;


public class AutoListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private Button clearBtn;
    private AutoCursorAdapter mAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AutoListFragment() {
    }

    @Override
    public void onDestroy() {
        Timber.d("onDestroy");
        super.onDestroy();
    }

    @Override
    public void onStart() {
        Timber.d("onStart");
        Loader loader=getLoaderManager().getLoader(0);
        Timber.d("loader="+loader);
        super.onStart();
    }

    @Override
    public void onStop() {
        Timber.d("onStop");
        super.onStop();
    }

    @Override
    public void onResume() {
        Timber.d("onResume");
        mAdapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Timber.d("onActivityCreated");
        mAdapter = new AutoCursorAdapter(getActivity(), null);
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(0,null,this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Timber.d("onCreateView");
        View view = inflater.inflate(R.layout.fragment_auto_list, container, false);
        clearBtn= (Button) view.findViewById(R.id.clearBtnId);
        initClearButton(clearBtn);
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Timber.d("onAttach");

    }

    @Override
    public void onDetach() {
        Timber.d("onDetach");
        super.onDetach();
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Timber.d("onCreateLoader");
        return new CursorLoader(getActivity(), Constants.ProviderConstants.AUTO_CONTENT_URI,
                Constants.ProviderConstants.projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {
        Timber.d("onLoadFinished");
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        Timber.d("onloaderResert");
        mAdapter.swapCursor(null);
    }

    private void initClearButton(View v) {
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAllAutoRecords();
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Auto auto=(Auto)v.getTag();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(auto.getLink()));
        startActivity(browserIntent);
    }

    private void removeAllAutoRecords() {
        getActivity().getContentResolver().delete(Constants.ProviderConstants.AUTO_CONTENT_URI,null,null);
    }


}
