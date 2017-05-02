package net.gusakov.newnettiauto.adapters;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import net.gusakov.newnettiauto.Constants;
import net.gusakov.newnettiauto.R;
import net.gusakov.newnettiauto.classes.Auto;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by hasana on 2/19/2017.
 */

public class AutoCursorAdapter extends CursorAdapter {
    private final DisplayImageOptions options;
    private final ImageLoader mImageLoader;
    private Context context;
    private SimpleDateFormat dateFormat;
    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

    public AutoCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        this.context = context;
        dateFormat = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss");
        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .displayer(new SimpleBitmapDisplayer())
                .build();
        mImageLoader= ImageLoader.getInstance();
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_layout, parent, false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        ImageView imageView=(ImageView)view.findViewById(R.id.imageViewId);
        TextView name = (TextView) view.findViewById(R.id.nameId);
        TextView description = (TextView) view.findViewById(R.id.descriptionId);
        TextView price = (TextView) view.findViewById(R.id.priceId);
        TextView yearAndMileage = (TextView) view.findViewById(R.id.yearsAndMileageId);
        TextView seller = (TextView) view.findViewById(R.id.sellerId);
        TextView date = (TextView) view.findViewById(R.id.dateId);
        Button callBtn = (Button) view.findViewById(R.id.callBtnId);
        ImageView image= (ImageView) view.findViewById(R.id.imageViewId);

        // Extract properties from cursor


        int idInt = cursor.getInt(cursor.getColumnIndex(Constants.DBConstants.TB_AUTO_ID));
        String nameStr = cursor.getString(cursor.getColumnIndex(Constants.DBConstants.TB_AUTO_NAME));
        String descriptionStr = cursor.getString(cursor.getColumnIndex(Constants.DBConstants.TB_AUTO_DESCRIPTION));
        int priceInt = cursor.getInt(cursor.getColumnIndex(Constants.DBConstants.TB_AUTO_PRICE));
        String yearAndMileageStr = cursor.getString(cursor.getColumnIndex(Constants.DBConstants.TB_AUTO_YEAR_AND_MILLEAGE));
        String sellerStr = cursor.getString(cursor.getColumnIndex(Constants.DBConstants.TB_AUTO_SELLER));
        long timestamp = cursor.getLong(cursor.getColumnIndex(Constants.DBConstants.TB_AUTO_TIMESTAMP));
        String linkStr = cursor.getString(cursor.getColumnIndex(Constants.DBConstants.TB_AUTO_LINK));
        final String phoneUri = cursor.getString(cursor.getColumnIndex(Constants.DBConstants.TB_AUTO_PHONE_URI));
        String imageUrl=cursor.getString(cursor.getColumnIndex(Constants.DBConstants.TB_AUTO_IMAGE_URL));

        //initial views
        //        imageLoader.DisplayImage(data[position].getIconUrlString(), image);
        mImageLoader.displayImage(imageUrl, image, options, animateFirstListener);
        name.setText(nameStr);
        description.setText(descriptionStr);
        price.setText(priceInt + " €");
        yearAndMileage.setText(yearAndMileageStr);
        seller.setText(sellerStr);
        Date foundDate = new Date(timestamp);
        date.setText(dateFormat.format(foundDate));
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (phoneUri!=null) {
                    String phUri = phoneUri;
                    final Context ctx = context;
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse(phUri));
                    if (ContextCompat.checkSelfPermission(ctx,
                            Manifest.permission.CALL_PHONE)
                            != PackageManager.PERMISSION_GRANTED) {
                        // No explanation needed, we can request the permission.

                        ActivityCompat.requestPermissions((Activity) ctx,
                                new String[]{Manifest.permission.CALL_PHONE},
                                0);

                    } else {

                        ctx.startActivity(intent);
                    }
                }else{
                    Toast.makeText(context,"no phone number",Toast.LENGTH_SHORT).show();
                }
            }
        });
        view.setTag(new Auto(idInt,nameStr,descriptionStr,priceInt,yearAndMileageStr,sellerStr,linkStr,phoneUri,false,timestamp,imageUrl));
    }


    private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

        static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());


        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }

    }
}
