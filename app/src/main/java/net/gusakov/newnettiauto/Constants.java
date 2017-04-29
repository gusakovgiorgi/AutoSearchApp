package net.gusakov.newnettiauto;

import android.net.Uri;
import android.provider.BaseColumns;

import net.gusakov.newnettiauto.fragments.MainFragment;

/**
 * Created by hasana on 4/2/2017.
 */

public interface Constants {

    String OPEN_LIST_ACTION = "net.gusakov.newnettiauto.fragments.action_open_list";

    interface DBConstants {
        String DB_NAME = "auto.db";
        String DB_TABLE_AUTO = "auto_table";
        String TB_AUTO_ID = "auto_id";
        String TB_AUTO_IMAGE_URL="auto_image_url";
        String TB_AUTO_NAME = "name";
        String TB_AUTO_DESCRIPTION = "description";
        String TB_AUTO_PRICE = "price";
        String TB_AUTO_YEAR_AND_MILLEAGE = "year_and_milleage";
        String TB_AUTO_SELLER = "seller";
        String TB_AUTO_LINK = "link";
        String TB_AUTO_PHONE_URI = "phone_uri";
        String TB_AUTO_TIMESTAMP = "timestamp";


        int DB_VERSION = 1;

        String DB_CREATE_STRING = "create table " + DB_TABLE_AUTO + " ("
                + "_id integer primary key autoincrement, "
                + TB_AUTO_ID + " integer, "
                + TB_AUTO_IMAGE_URL+" text, "
                + TB_AUTO_NAME + " text, "
                + TB_AUTO_DESCRIPTION + " text, "
                + TB_AUTO_PRICE + " integer, "
                + TB_AUTO_YEAR_AND_MILLEAGE + " text, "
                + TB_AUTO_SELLER + " text, "
                + TB_AUTO_LINK + " text, "
                + TB_AUTO_PHONE_URI + " text, "
                + TB_AUTO_TIMESTAMP + " integer"
                + ");";
        String DB_DROP_STRING = "DROP TABLE IF EXISTS " + DB_TABLE_AUTO;
    }

    interface ProviderConstants {
        int URI_AUTOS = 1;
        int URI_AUTOS_ID = 2;
        String AUTHORITY = "net.gusakov.newnettiauto.provider.contentprovider";
        String AUTO_PATH = "autos_table";
        Uri AUTO_CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/" + AUTO_PATH);
        String AUTO_CONTENT_TYPE = "vnd.android.cursor.dir/vnd."
                + AUTHORITY + "." + AUTO_PATH;

        String AUTO_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd."
                + AUTHORITY + "." + AUTO_PATH;
        String DEFAULT_SORT_ORDER = DBConstants.TB_AUTO_TIMESTAMP + " DESC";
        String[] projection = new String[]{BaseColumns._ID, DBConstants.TB_AUTO_ID, DBConstants.TB_AUTO_NAME, DBConstants.TB_AUTO_DESCRIPTION, DBConstants.TB_AUTO_PRICE,
                DBConstants.TB_AUTO_YEAR_AND_MILLEAGE, DBConstants.TB_AUTO_SELLER, DBConstants.TB_AUTO_LINK, DBConstants.TB_AUTO_PHONE_URI, DBConstants.TB_AUTO_TIMESTAMP};
    }

    interface MainFragmentConstants {
        int POLLING_FREQYENCY_DEFAULT = 1;
        String SEARCHING = "searching";
        String STOPED = "stopped";
        String START_BUTTON_TAG = "start";
        String INTENT_EXTRTRA_STRING_FIRST_URL = "firstUrlExtra";
        String INTENT_EXTRTRA_STRING_SECOND_URL = "secondtUrlExtra";
        String INTENT_EXTRA_INT_POLL_FREQ_TIME = "pollFreqTime";

        String STOP_BUTTON_TAG = "stop";
        int MINIMUM_TIME = 0;
        int MAXIMUM_TIME = 24 * 60 * 60;
    }

    interface ServiceConstants {
        String STOP_ACTION = "stop_autosearchservice";
        String INTENT_EXTRTRA_STRING_FIRST_URL = MainFragmentConstants.INTENT_EXTRTRA_STRING_FIRST_URL;
        String INTENT_EXTRTRA_STRING_SECOND_URL = MainFragmentConstants.INTENT_EXTRTRA_STRING_SECOND_URL;
        String INTENT_EXTRA_INT_POLL_FREQ_TIME = MainFragmentConstants.INTENT_EXTRA_INT_POLL_FREQ_TIME;
        int POLLING_FREQYENCY_DEFAULT = MainFragmentConstants.POLLING_FREQYENCY_DEFAULT;
        String SEARCH_STATUS = "Searching";
        String WAIT_STATUS = "Waiting for internet connection";
    }

    interface MainFragmentAndServiceSharedConstants {
        String SHARED_PREF_NAME = "mainfagment";
        String SHARED_PARAMETER_STRING_URL1 = "url1";
        String SHARED_PARAMETER_STRING_URL2 = "url2";
        String SHARED_PARAMETER_INT_TIME = "time";
        String FIRST_DEFAULT_URL = "https://m.nettiauto.com/toyota/hiace?sortCol=datecreate&ord=DESC&id_make=79&id_model=902&yfrom=1997&yto=2012";
        String SECOND_DEFAULT_URL = "https://m.nettiauto.com/toyota/hilux?sortCol=datecreate&ord=DESC&id_make=79&id_model=903&yfrom=1989&yto=2010";
    }

    interface InternetSearcherConstants {
        int READ_TIMEOUT = 10000;
        int CONNECT_TIMEOUT = 10000;
    }

    interface HTMLParserConstants {
        String AUTO_MAIN_ELEMENT_CLASS_NAME = "block_list_li";
        String AUTO_MAIN_RECLAME_ELEMENT_CLASS_NAME = "upsellAd";
        String AUTO_DETAIL_BOX_CLASS_NAME = "detail_box";
        String AUTO_DETAIL_DEALER_NAME = "dealer_logo block_box fl";
        int QUEUE_MAX_CAPACITY = 10;
        int READ_TIMEOUT = 2000;
        int CONNECT_TIMEOUT = 2000;
    }

    interface NotificationManagerConstants {
        int NOTIFICATION_NOTIFY_ID = 101;
        int NOTIFICATION_FOREGROUND_ID = 111;
    }
}
